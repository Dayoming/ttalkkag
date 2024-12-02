<template>
  <div class="container mt-4">
    <h5><b>Reports</b></h5>
    <!-- 선택 박스 -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div class="d-flex align-items-center w-100">
        <label class="me-2"><b>현재 선택된 통계:</b></label>
        <select v-model="selectedProject" class="form-select w-25">
          <option value="History">History</option>
          <option
            v-for="project in projects"
            :key="project.id"
            :value="project.name"
          >
            {{ project.name }}
          </option>
        </select>
      </div>
    </div>

    <!-- 차트 섹션 -->
    <div class="row">
      <!-- 원형 차트 -->
      <div class="col-md-6">
        <h6 class="text-center">성공 및 실패</h6>
        <canvas id="successFailureChart"></canvas>
      </div>
      <!-- 막대 차트 -->
      <div class="col-md-6">
        <h6 class="text-center">API 호출 횟수/평균 응답 시간</h6>
        <canvas id="responseTimeChart"></canvas>
      </div>
    </div>

    <!-- 라인 차트 -->
    <!-- 에러 발생 시간대 -->
    <div class="mt-4">
      <h6 class="text-center">에러 발생 시간대</h6>
      <canvas id="errorTimeChart"></canvas>
    </div>

    <!-- 에러 발생 빈도 -->
    <div class="mt-4">
      <h6 class="text-center">에러 발생 빈도</h6>
      <div class="d-flex justify-content-between align-items-center mb-3">
        <label for="errorRange">HTTP 에러 범위 선택:</label>
        <select
          id="errorRange"
          v-model="selectedErrorRange"
          class="form-select w-25"
        >
          <option value="4XX">4XX</option>
          <option value="5XX">5XX</option>
        </select>
      </div>
      <canvas id="errorFrequencyChart"></canvas>
    </div>

    <!-- 에러 이력 테이블 -->
    <div v-if="selectedErrorDetails.length" class="mt-4">
      <h6 class="text-center">선택된 에러 이력</h6>
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>에러 코드</th>
            <th>메시지</th>
            <th>발생 시간</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(error, index) in selectedErrorDetails" :key="index">
            <td>{{ error.code }}</td>
            <td>{{ error.message }}</td>
            <td>{{ error.timestamp }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { onMounted, ref, watch } from "vue";
import Chart from "chart.js/auto";

export default {
  name: "ReportsVue",
  setup() {
    const selectedProject = ref("History");
    const projects = ref([
      { id: 1, name: "Project A" },
      { id: 2, name: "Project B" },
      { id: 3, name: "Project C" },
    ]);

    const selectedErrorRange = ref("4XX"); // 선택된 에러 범위
    const selectedErrorDetails = ref([]); // 클릭된 막대의 에러 이력
    const errorData = ref([]); // 전체 에러 데이터

    // 차트 인스턴스 저장용
    let successFailureChart = null;
    let responseTimeChart = null;
    let errorFrequencyChart = null;
    let errorTimeChart = null;

    // 에러 데이터 초기화
    const loadErrorData = () => {
      errorData.value = [
        {
          code: 401,
          message: "Unauthorized",
          timestamp: "2024-05-01 12:00:00",
        },
        { code: 404, message: "Not Found", timestamp: "2024-05-01 13:00:00" },
        {
          code: 500,
          message: "Internal Server Error",
          timestamp: "2024-05-01 14:00:00",
        },
        { code: 403, message: "Forbidden", timestamp: "2024-05-02 15:00:00" },
        {
          code: 502,
          message: "Bad Gateway",
          timestamp: "2024-05-02 16:00:00",
        },
      ];
    };

    // 에러 빈도 데이터를 필터링
    const getFilteredErrorFrequency = () => {
      const range =
        selectedErrorRange.value === "4XX" ? [400, 499] : [500, 599];
      const filtered = errorData.value.filter(
        (error) => error.code >= range[0] && error.code <= range[1]
      );

      const frequency = {};
      filtered.forEach((error) => {
        frequency[error.code] = (frequency[error.code] || 0) + 1;
      });

      return {
        labels: Object.keys(frequency),
        datasets: [
          {
            label: "에러 발생 빈도",
            data: Object.values(frequency),
            backgroundColor: "rgba(255, 99, 132, 0.5)",
            borderColor: "rgba(255, 99, 132, 1)",
          },
        ],
      };
    };

    // 차트 업데이트
    const updateErrorFrequencyChart = () => {
      const data = getFilteredErrorFrequency();
      if (errorFrequencyChart) errorFrequencyChart.destroy();
      errorFrequencyChart = new Chart(
        document.getElementById("errorFrequencyChart"),
        {
          type: "bar",
          data,
          options: {
            onClick(event, elements) {
              if (elements.length > 0) {
                const index = elements[0].index;
                const errorCode = data.labels[index];
                selectedErrorDetails.value = errorData.value.filter(
                  (error) => error.code == errorCode
                );
              }
            },
            plugins: {
              legend: { display: false },
            },
            scales: {
              x: { beginAtZero: true },
            },
          },
        }
      );
    };

    // 차트 데이터 초기화
    const updateCharts = () => {
      // 원형 차트 데이터
      const successFailureData = {
        labels: ["성공", "실패"],
        datasets: [
          {
            data: [72, 28], // 임의 데이터
          },
        ],
      };

      // 메소드 호출 횟수
      const responseTimeData = {
        labels: ["GET", "POST", "PATCH", "PUT", "DELETE"],
        datasets: [
          {
            label: "호출 횟수",
            data: [3, 12, 8, 6, 2], // 임의 데이터
            fill: true,
          },
        ],
      };

      // 에러 발생 시간대  차트 데이터
      const errorTimeData = {
        labels: [
          "2024-05-01",
          "2024-05-02",
          "2024-05-03",
          "2024-05-04",
          "2024-05-05",
        ],
        datasets: [
          {
            label: "에러 발생 일별 데이터",
            data: [4, 2, 6, 8, 5], // 임의 데이터
            fill: true,
            borderColor: "#ff9800",
            backgroundColor: "rgba(255, 152, 0, 0.2)",
          },
        ],
      };

      // 원형 차트 생성
      if (successFailureChart) successFailureChart.destroy();
      successFailureChart = new Chart(
        document.getElementById("successFailureChart"),
        {
          type: "doughnut",
          data: successFailureData,
        }
      );

      // 막대 차트 생성
      if (responseTimeChart) responseTimeChart.destroy();
      responseTimeChart = new Chart(
        document.getElementById("responseTimeChart"),
        {
          type: "bar",
          data: responseTimeData,
          options: {
            indexAxis: "y",
            responsive: true,
            plugins: {
              legend: {
                position: "top",
              },
            },
            scales: {
              x: {
                beginAtZero: true,
              },
            },
          },
        }
      );

      // 라인 차트 생성
      if (errorTimeChart) errorTimeChart.destroy();
      errorTimeChart = new Chart(
        document.getElementById("errorTimeChart"),
        {
          type: "line",
          data: errorTimeData,
        }
      );
    };

    // 초기화
    onMounted(() => {
      loadErrorData();
      updateErrorFrequencyChart();
      updateCharts();
    });

    // 프로젝트 선택 시 업데이트
    watch(selectedProject, () => {
      console.log(`Selected Project: ${selectedProject.value}`);
      updateCharts();
    });

    watch(selectedErrorRange, () => {
      updateErrorFrequencyChart();
    });

    return {
      selectedProject,
      projects,
      selectedErrorRange,
      selectedErrorDetails,
    };
  },
};
</script>

<style scoped>
.container {
  max-width: 1200px;
}

canvas {
  max-height: 300px;
}

select {
  border: 1px solid #ccc;
  padding: 0.5rem;
  font-size: 1rem;
}

.table {
  text-align: center;
}

.canvas-container {
  margin-top: 20px;
  max-height: 400px;
}

table th,
table td {
  vertical-align: middle;
}
</style>
