<template>
  <div class="container mt-4">
    <h5><b>Reports</b></h5>
    <!-- 선택 박스 -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div class="d-flex align-items-center w-100">
        <select
          class="form-select me-3 w-25"
          v-model="selectedFilterType"
          @change="handleFilterTypeChange"
        >
          <option value="ALL">ALL</option>
          <option value="Site">Site</option>
          <option value="Projects">Projects</option>
        </select>

        <!-- 동적 필터: Site -->
        <div
          v-if="selectedFilterType === 'Site'"
          class="d-flex align-items-center w-25"
        >
          <select
            class="form-select me-3"
            v-model="selectedSite"
            @change="handleSiteChange"
          >
            <option v-if="uniqueSites.length > 0" disabled hidden value="">
              사이트를 선택해 주세요.
            </option>
            <option v-for="site in uniqueSites" :key="site" :value="site">
              {{ site }}
            </option>
          </select>
          <select
            class="form-select"
            v-if="selectedSite"
            v-model="selectedEnvironment"
            @change="filterLogs"
          >
            <option
              v-if="uniqueEnvironments.length > 0"
              disabled
              hidden
              value=""
            >
              환경을 선택해 주세요.
            </option>
            <option v-for="env in uniqueEnvironments" :key="env" :value="env">
              {{ env }}
            </option>
          </select>
        </div>

        <!-- 동적 필터: Projects -->
        <select
          class="form-select w-25"
          v-if="selectedFilterType === 'Projects'"
          v-model="selectedProject"
          @change="filterLogs"
        >
          <option v-if="uniqueProjects.length > 0" disabled hidden value="">
            프로젝트를 선택해 주세요.
          </option>
          <option
            v-for="project in uniqueProjects"
            :key="project"
            :value="project"
          >
            {{ project }}
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
      logs: [],
      filteredLogs: [],
      historyData: [],
      filteredHistoryData: [],
      errorData: [],
      projects: [],
      selectedFilterType: "ALL",
      selectedProject: "",
      selectedSite: "",
      selectedEnvironment: "",
      selectedTimeUnit: "day",
      selectedErrorRange: "4XX",
      selectedErrorDetails: [],
      successFailureChart: null,
      errorTimeChart: null,
      responseTimeChart: null,
      errorFrequencyChart: null,
    };
  },
  computed: {
    uniqueSites() {
      // 중복 제거 후 Site 목록 생성
      return [...new Set(this.logs.map((log) => log.siteName))];
    },
    uniqueEnvironments() {
      // 선택된 Site에 해당하는 환경 목록 생성
      return this.logs
        .filter((log) => log.siteName === this.selectedSite)
        .map((log) => log.environmentName)
        .filter((value, index, self) => self.indexOf(value) === index); // 중복 제거
    },
    uniqueProjects() {
      // 중복 제거 후 Project 목록 생성
      return [...new Set(this.logs.map((log) => log.projectName))];
    },
    successFailureData() {
      const successCount = this.filteredLogs.filter(
        (log) => log.responseCode >= 200 && log.responseCode < 300
      ).length;
      const failureCount = this.filteredLogs.length - successCount;

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
      const errorCounts = this.filteredLogs.reduce((acc, log) => {
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
    async fetchHistoryData() {
      try {
        const response = await this.$axios.get("/api/history");
        if (Array.isArray(response.data)) {
          // 데이터가 배열일 때만 처리
          this.logs = response.data.map((log) => ({
            ...log,
            showDetails: false, // 기본적으로 상세 정보는 숨김
          }));
          this.filteredLogs = this.logs;
          this.updateFilteredHistoryData(); // 필터링된 데이터를 기반으로 초기화
        } else {
          console.error("Unexpected response format:", response.data);
          this.logs = [];
        }
      } catch (error) {
        console.error("Error fetching history data:", error);
      }
    },
    handleFilterTypeChange() {
      if (this.selectedFilterType === "ALL") {
        this.filterLogs();
      }
      // 필터 타입 변경 시
      this.selectedSite = "";
      this.selectedEnvironment = "";
      this.selectedProject = "";
    },
    handleSiteChange() {
      // Site 선택 시 환경 초기화
      this.selectedEnvironment = "";
      this.filterLogs();
    },
    filterLogs() {
      if (this.selectedFilterType === "ALL") {
        this.filteredLogs = this.logs; // 모든 로그
      } else if (this.selectedFilterType === "Site") {
        this.filteredLogs = this.logs.filter(
          (log) =>
            log.siteName === this.selectedSite &&
            (this.selectedEnvironment === "" ||
              log.environmentName === this.selectedEnvironment)
        );
      } else if (this.selectedFilterType === "Projects") {
        this.filteredLogs = this.logs.filter(
          (log) => log.projectName === this.selectedProject
        );
      }
    },
    async updateFilteredHistoryData() {
      if (this.selectedFilterType === "ALL") {
        this.filteredLogs = this.logs; // 전체 데이터 표시
        this.renderCharts();
      } else if (this.selectedFilterType === "Projects") {
        this.filteredLogs = this.logs.filter(
          (log) => log.projectName === this.selectedProject
        );
        this.renderCharts(); // 데이터 업데이트 후 차트 렌더링
      } else if (this.selectedFilterType === "Site") {
        this.filteredLogs = this.logs.filter(
          (log) =>
            log.siteName === this.selectedSite &&
            (this.selectedEnvironment === "" ||
              log.environmentName === this.selectedEnvironment)
        );
        this.renderCharts();
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
                this.selectedErrorDetails = this.filteredLogs.filter(
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

      this.filteredLogs.forEach((log) => {
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
      this.filteredLogs.forEach((log) => {
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
                this.selectedErrorDetails = this.filteredLogs.filter(
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
