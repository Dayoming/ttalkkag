<template>
    <div class="container mt-4">
      <h5><b>Reports</b></h5>
      <!-- 선택 박스 -->
      <div class="d-flex align-items-center justify-content-between mb-4">
        <div class="d-flex align-items-center w-100">
          <label class="me-2"><b>현재 선택된 통계:</b></label>
          <select v-model="selectedProject" class="form-select w-25">
            <option value="History">History</option>
            <option v-for="project in projects" :key="project.id" :value="project.name">
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
      <div class="mt-4">
        <h6 class="text-center">에러 발생 빈도</h6>
        <canvas id="errorFrequencyChart"></canvas>
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
  
      // 차트 인스턴스 저장용
      let successFailureChart = null;
      let responseTimeChart = null;
      let errorFrequencyChart = null;
  
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
  
        // 라인 차트 데이터
        const errorFrequencyData = {
          labels: ["2024-05-01", "2024-05-02", "2024-05-03", "2024-05-04", "2024-05-05"],
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
        successFailureChart = new Chart(document.getElementById("successFailureChart"), {
          type: "doughnut",
          data: successFailureData,
        });
  
        // 막대 차트 생성
        if (responseTimeChart) responseTimeChart.destroy();
        responseTimeChart = new Chart(document.getElementById("responseTimeChart"), {
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
      });
  
        // 라인 차트 생성
        if (errorFrequencyChart) errorFrequencyChart.destroy();
        errorFrequencyChart = new Chart(document.getElementById("errorFrequencyChart"), {
          type: "line",
          data: errorFrequencyData,
        });
      };
  
      // 초기화
      onMounted(() => {
        updateCharts();
      });
  
      // 프로젝트 선택 시 업데이트
      watch(selectedProject, () => {
        console.log(`Selected Project: ${selectedProject.value}`);
        updateCharts();
      });
  
      return {
        selectedProject,
        projects,
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
  </style>
  