<template>
  <div class="container mt-4">
    <h5><b>Reports</b></h5>
    <!-- 선택 박스 -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div class="d-flex align-items-center w-100">
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
      <div class="d-flex justify-content-between align-items-center mb-3">
        <label for="selectedTimeUnit"></label>
        <select v-model="selectedTimeUnit" class="form-select w-25">
          <option value="day">일</option>
          <option value="month">월</option>
          <option value="hour">시간</option>
        </select>
      </div>
      <canvas id="errorTimeChart"></canvas>
    </div>

    <!-- 에러 발생 빈도 -->
    <div class="mt-4">
      <h6 class="text-center">에러 발생 빈도</h6>
      <div class="d-flex justify-content-between align-items-center mb-3">
        <label for="errorRange"></label>
        <select
          id="errorRange"
          v-model="selectedErrorRange"
          class="form-select w-25"
        >
          <option value="4XX">4XX</option>
          <option value="5XX">5XX</option>
        </select>
      </div>
      <canvas id="errorFrequencyChart" class="mb-5"></canvas>
    </div>

    <!-- 에러 이력 테이블 -->
    <div v-if="selectedErrorDetails.length" class="mt-4">
      <div class="table-wrapper">
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>METHOD</th>
              <th>URL</th>
              <th>응답 코드</th>
              <th>시간(ms)</th>
              <th>기록 시간</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(log, index) in selectedErrorDetails" :key="index">
              <td>{{ log.method }}</td>
              <td>{{ log.url }}</td>
              <td>{{ log.responseCode }}</td>
              <td>{{ log.responseTime }}ms</td>
              <td>{{ log.loggedTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import Chart from "chart.js/auto";
import ChartDataLabels from "chartjs-plugin-datalabels";
import moment from "moment";

export default {
  name: "ReportsVue",
  data() {
    return {
      historyData: [],
      filteredHistoryData: [],
      errorData: [],
      projects: [],
      selectedTimeUnit: "day",
      selectedErrorRange: "4XX",
      selectedProject: "History",
      selectedErrorDetails: [],
      successFailureChart: null,
      errorTimeChart: null,
      responseTimeChart: null,
      errorFrequencyChart: null,
    };
  },
  computed: {
    successFailureData() {
      const successCount = this.filteredHistoryData.filter(
        (log) => log.responseCode >= 200 && log.responseCode < 300
      ).length;
      const failureCount = this.filteredHistoryData.length - successCount;

      return {
        labels: ["성공", "실패"],
        datasets: [
          {
            data: [successCount, failureCount],
          },
        ],
      };
    },
    errorFrequencyData() {
      const range = this.selectedErrorRange === "4XX" ? [400, 499] : [500, 599];
      const errorCounts = this.filteredHistoryData.reduce((acc, log) => {
        if (log.responseCode >= range[0] && log.responseCode <= range[1]) {
          acc[log.responseCode] = (acc[log.responseCode] || 0) + 1;
        }
        return acc;
      }, {});

      return {
        labels: Object.keys(errorCounts),
        datasets: [
          {
            label: "에러 발생 빈도",
            data: Object.values(errorCounts),
            backgroundColor: "rgba(75, 192, 192, 0.5)",
            borderColor: "rgba(75, 192, 192, 1)",
            borderWidth: 1,
          },
        ],
      };
    },
  },
  methods: {
    async fetchProjects() {
      try {
        const response = await this.$axios.get("/api/projects");
        this.projects = response.data;
      } catch (error) {
        console.error("Failed to fetch projects:", error);
      }
    },
    async fetchHistoryData() {
      try {
        const response = await this.$axios.get("/api/history");
        this.historyData = response.data;
        this.updateFilteredHistoryData(); // 필터링된 데이터를 기반으로 초기화
      } catch (error) {
        console.error("Error fetching history data:", error);
      }
    },
    async updateFilteredHistoryData() {
      if (this.selectedProject === "History") {
        this.filteredHistoryData = this.historyData; // 전체 데이터 표시
        this.renderCharts();
      } else {
        // 선택된 프로젝트의 ID 찾기
        const selectedProject = this.projects.find(
          (project) => project.name === this.selectedProject
        );

        console.log(selectedProject);

        if (selectedProject) {
          const selectedProjectId = selectedProject.id;

          try {
            // 프로젝트 ID로 필터링된 데이터 가져오기
            const response = await this.$axios.get(
              `/api/history/projects/${selectedProjectId}`
            );
            this.filteredHistoryData = response.data || []; // 데이터 할당
            this.renderCharts(); // 데이터 업데이트 후 차트 렌더링
          } catch (error) {
            console.error("Failed to fetch filtered history data:", error);
            this.filteredHistoryData = []; // 에러 시 기본값 설정
          }
        } else {
          console.error(
            "Selected project not found in projects list:",
            this.selectedProject
          );
          this.filteredHistoryData = []; // 프로젝트를 찾지 못한 경우 기본값
          this.renderCharts();
        }
      }
    },
    renderErrorTimeChart() {
      if (this.errorTimeChart) this.errorTimeChart.destroy();

      const groupedData = this.getErrorDataByTimeUnit();
      const labels = Object.keys(groupedData);
      const data = labels.map((label) => groupedData[label].length);

      this.errorTimeChart = new Chart(
        document.getElementById("errorTimeChart"),
        {
          type: "line",
          data: {
            labels,
            datasets: [
              {
                label: "에러 발생 횟수",
                data,
                fill: true,
                borderColor: "#8884d8",
                backgroundColor: "rgba(136, 132, 216, 0.2)",
              },
            ],
          },
          options: {
            plugins: {
              tooltip: {
                callbacks: {
                  title: (context) => {
                    const timeKey = context[0].label;
                    const logs = groupedData[timeKey];
                    return logs[0].loggedTime;
                  },
                  afterBody: (context) => {
                    const timeKey = context[0].label;
                    const logs = groupedData[timeKey];
                    return logs
                      .map(
                        (log) =>
                          `Method: ${log.method}\nURL: ${log.url}\nError: ${log.responseCode} ${log.responseMessage}`
                      )
                      .join("\n\n");
                  },
                },
              },
            },
            scales: {
              x: {
                title: {
                  display: true,
                  text:
                    this.selectedTimeUnit === "day"
                      ? "날짜"
                      : this.selectedTimeUnit === "month"
                      ? "월"
                      : "시간",
                },
              },
              y: {
                beginAtZero: true,
                title: {
                  display: true,
                  text: "에러 발생 횟수",
                },
              },
            },
          },
        }
      );
    },
    renderErrorFrequencyChart() {
      if (this.errorFrequencyChart) this.errorFrequencyChart.destroy();

      this.errorFrequencyChart = new Chart(
        document.getElementById("errorFrequencyChart"),
        {
          type: "bar",
          data: this.errorFrequencyData,
          options: {
            responsive: true,
            plugins: {
              tooltip: {
                callbacks: {
                  label: (context) => {
                    return `발생 빈도: ${context.raw}`;
                  },
                },
              },
            },
            scales: {
              x: {
                title: {
                  display: true,
                  text: "에러 코드",
                },
              },
              y: {
                beginAtZero: true,
                title: {
                  display: true,
                  text: "발생 빈도 수",
                },
              },
            },
            onClick: (event, elements) => {
              if (elements.length > 0) {
                const index = elements[0].index;
                const errorCode = this.errorFrequencyData.labels[index];
                this.selectedErrorDetails = this.filteredHistoryData.filter(
                  (log) => log.responseCode == errorCode
                );
              }
            },
          },
        }
      );
    },
    getErrorDataByTimeUnit() {
      const groupedData = {};
      const timeFormat = {
        day: "YYYY-MM-DD",
        month: "YYYY-MM",
        hour: "YYYY-MM-DD HH",
      }[this.selectedTimeUnit];

      this.filteredHistoryData.forEach((log) => {
        if (log.responseCode >= 400) {
          const timeKey = moment(log.loggedTime).format(timeFormat);
          if (!groupedData[timeKey]) groupedData[timeKey] = [];
          groupedData[timeKey].push(log);
        }
      });

      return groupedData;
    },
    renderCharts() {
      if (this.successFailureChart) this.successFailureChart.destroy();
      if (this.responseTimeChart) this.responseTimeChart.destroy();
      if (this.errorTimeChart) this.errorTimeChart.destroy();
      if (this.errorFrequencyChart) this.errorFrequencyChart.destroy();

      this.successFailureChart = new Chart(
        document.getElementById("successFailureChart"),
        {
          type: "doughnut",
          data: this.successFailureData,
        }
      );

      const methodStats = {};
      this.filteredHistoryData.forEach((log) => {
        const method = log.method;
        if (!methodStats[method]) {
          methodStats[method] = { count: 0, totalTime: 0 };
        }
        methodStats[method].count += 1;
        methodStats[method].totalTime += log.responseTime || 0;
      });

      const labels = Object.keys(methodStats);
      const counts = labels.map((method) => methodStats[method].count);
      const averageTimes = labels.map((method) =>
        Math.round(methodStats[method].totalTime / methodStats[method].count)
      );

      this.responseTimeChart = new Chart(
        document.getElementById("responseTimeChart"),
        {
          type: "bar",
          data: {
            labels,
            datasets: [
              {
                label: "호출 횟수",
                data: counts,
                backgroundColor: "rgba(75, 192, 192, 0.5)",
                borderColor: "rgba(75, 192, 192, 1)",
                borderWidth: 1,
              },
            ],
          },
          options: {
            indexAxis: "y", // 가로 막대 차트
            responsive: true,
            plugins: {
              tooltip: {
                callbacks: {
                  label: (context) => {
                    const avgTime = averageTimes[context.dataIndex];
                    return `평균 응답 시간: ${avgTime} ms`;
                  },
                },
              },
              datalabels: {
                anchor: "end",
                align: "end",
                formatter: (value, context) => {
                  const avgTime = averageTimes[context.dataIndex];
                  return `${avgTime} ms`;
                },
                color: "black",
              },
            },
            scales: {
              x: {
                beginAtZero: true,
              },
            },
          },
          plugins: [ChartDataLabels], // DataLabels 플러그인 활성화
        }
      );

      this.renderErrorTimeChart();

      this.errorFrequencyChart = new Chart(
        document.getElementById("errorFrequencyChart"),
        {
          type: "bar",
          data: this.errorFrequencyData,
          options: {
            onClick: (event, elements) => {
              if (elements.length > 0) {
                const index = elements[0].index;
                const errorCode = this.errorFrequencyData.labels[index];
                this.selectedErrorDetails = this.filteredHistoryData.filter(
                  (log) => log.responseCode == errorCode
                );
              }
            },
          },
        }
      );
    },
  },
  mounted() {
    this.fetchHistoryData();
    this.fetchProjects();
  },
  watch: {
    selectedTimeUnit() {
      this.renderErrorTimeChart(); // 시간 단위 변경 시 에러 차트 업데이트
    },
    selectedErrorRange() {
      this.renderErrorFrequencyChart();
    },
    selectedProject() {
      this.updateFilteredHistoryData(); // 선택된 프로젝트가 변경될 때 필터링된 데이터로 차트 업데이트
    },
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

.table-wrapper {
  max-height: 300px;
  overflow-y: auto;
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
