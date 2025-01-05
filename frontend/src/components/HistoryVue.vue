<template>
  <div class="container mt-4">
    <h5 class="mb-5"><b>History</b></h5>
    <!-- 필터 셀렉트 박스 -->
    <div class="d-flex align-items-center mb-4">
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
        <option
          v-for="project in uniqueProjects"
          :key="project"
          :value="project"
        >
          {{ project }}
        </option>
      </select>
    </div>
    <!-- 요청 기록 테이블 -->
    <div class="table-responsive mt-4 table-container">
      <table class="table table-bordered mb-5">
        <thead>
          <tr>
            <th>Method</th>
            <th>URL</th>
            <th>Response Code</th>
            <th>Response Time (ms)</th>
            <th style="border-right: 0">Logged Time</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <template v-for="(log, index) in filteredLogs" :key="index">
            <tr>
              <td style="text-align: center">
                <span class="badge text-bg-dark">{{ log.method }}</span>
              </td>
              <td>{{ log.url }}</td>
              <td>{{ log.responseCode }} {{ log.responseMessage }}</td>
              <td>{{ log.responseTime }}</td>
              <td style="border-right: 0">{{ log.loggedTime }}</td>
              <td>
                <button class="btn btn-light" @click="toggleDetails(index)">
                  <i
                    class="bi"
                    :class="
                      log.showDetails ? 'bi-chevron-up' : 'bi-chevron-down'
                    "
                  ></i>
                </button>
              </td>
            </tr>
            <!-- 상세 페이지 -->
            <tr v-if="logs[index]?.showDetails" :key="`details-${index}`">
              <td colspan="6">
                <div class="p-3 border history-detail">
                  <p><b>Method:</b> {{ log.method }}</p>
                  <p><b>URL:</b> {{ log.url }}</p>
                  <p><b>Header:</b> {{ log.header }}</p>
                  <p><b>Parameter:</b> {{ log.parameter }}</p>
                  <p><b>Requested By:</b> {{ log.request_user_email }}</p>
                  <p><b>Project Name:</b> {{ log.project_name }}</p>
                  <p><b>Project Owner:</b> {{ log.project_owner_email }}</p>
                  <p><b>Site Name:</b> {{ log.site_name }}</p>
                  <p><b>Environment Name:</b> {{ log.environment_name }}</p>
                  <hr />
                  <div class="row">
                    <div class="col-md-6">
                      <h6>Response Body</h6>
                      <pre class="formatted-body">
                        <code v-html="formattedBody(index)"></code>
                      </pre>
                    </div>
                    <div class="col-md-6">
                      <h6>Response Header</h6>
                      <pre>{{ log.responseHeader }}</pre>
                    </div>
                  </div>
                  <div class="d-flex justify-content-end mt-3">
                    <button class="btn btn-dark me-2" @click="reRequest(index)">
                      다시 요청하기
                    </button>
                    <button
                      class="btn btn-secondary"
                      @click="editRequest(index)"
                    >
                      수정하기
                    </button>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import hljs from "highlight.js";
import "highlight.js/styles/default.css";

export default {
  name: "ReportVue",
  data() {
    return {
      logs: [],
      filteredLogs: [],
      method: "",
      url: "",
      parameter: [],
      formParamter: [],
      header: [],
      selectedFilterType: "ALL",
      selectedSite: "",
      selectedEnvironment: "",
      selectedProject: "",
    };
  },
  methods: {
    async fetchHistory() {
      try {
        const response = await this.$axios.get("/api/history");
        if (Array.isArray(response.data)) {
          // 데이터가 배열일 때만 처리
          this.logs = response.data.map((log) => ({
            ...log,
            showDetails: false, // 기본적으로 상세 정보는 숨김
          }));
          this.filteredLogs = this.logs; // 초기 필터링된 로그 설정
        } else {
          console.error("Unexpected response format:", response.data);
          this.logs = [];
        }
      } catch (error) {
        console.error("Failed to fetch history:", error);
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
            log.siteId === this.selectedSite &&
            (this.selectedEnvironment === "" ||
              log.environmentId === this.selectedEnvironment)
        );
      } else if (this.selectedFilterType === "Projects") {
        this.filteredLogs = this.logs.filter(
          (log) => log.projectId === this.selectedProject
        );
      }
    },
    addHistory(response, elapsedTime) {
      const newLog = {
        method: this.method,
        url: this.url,
        responseCode: response.status,
        responseMessage: response.statusText,
        responseTime: elapsedTime,
        loggedTime: new Date().toISOString().replace("T", " ").split(".")[0],
        header: JSON.stringify(this.header),
        parameter: JSON.stringify(this.parameter),
        responseBody: JSON.stringify(response.data, null, 2),
        responseHeader: JSON.stringify(response.headers, null, 2),
      };

      // 서버에 기록 저장 API 호출
      this.$axios.post("/api/history", newLog);
    },
    async reRequest(index) {
      const startTime = performance.now(); // 요청 시작 시간

      try {
        let data = new FormData();

        const jsonRequest = {
          method: this.logs[index].method,
          url: this.logs[index].url,
          headers: this.logs[index].headers,
          body: this.logs[index].body,
        };

        data.append(
          "request",
          new Blob([JSON.stringify(jsonRequest)], { type: "application/json" })
        );

        const response = await this.$axios.post("/api/proxy", data, {
          headers: {
            "Content-Type": "multipart/form-data", // FormData 형식
          },
        });

        // 응답 데이터 설정
        this.response = {
          statusCode: response.status,
          statusMessage: response.statusText,
          body: response.data,
          headers: response.headers,
        };

        const endTime = performance.now(); // 요청 완료 시간
        const elapsedTime = Math.round(endTime - startTime); // 소요 시간(ms)

        this.addHistory(response, elapsedTime);
        this.fetchHistory();
      } catch (error) {
        const failedResponse = {
          statusCode: error.response?.status || "Error",
          statusMessage: error.response?.statusText || "Request failed",
          body: error.response?.data || "Request failed.",
          headers: error.response?.headers || {},
        };

        this.response = failedResponse;

        const endTime = performance.now(); // 요청 완료 시간
        const elapsedTime = Math.round(endTime - startTime); // 소요 시간(ms)

        this.addHistory(this.response, elapsedTime);
        this.fetchHistory();
      }
    },
    editRequest(index) {
      const log = this.logs[index];
      // 로그 데이터를 emit하여 App.vue로 전달
      this.$emit("edit-request", log);
    },
    toggleDetails(index) {
      if (this.logs[index]) {
        this.url = this.logs[index].url;
        this.method = this.logs[index].method;
        this.header = this.logs[index].header;
        this.parameter = this.logs[index].parameter;
        this.formParamter = this.logs[index].formParameter;
        this.logs[index].showDetails = !this.logs[index].showDetails;
        // 다른 로그의 상세 정보는 닫기
        this.logs = this.logs.map((log, i) => {
          if (i !== index) {
            log.showDetails = false;
          }
          return log;
        });
      } else {
        console.error("Invalid index:", index);
      }
    },
  },
  computed: {
    formattedBody() {
      return (index) => {
        const log = this.logs[index];
        if (!log || !log.responseBody) return "";

        const body =
          typeof log.responseBody === "string"
            ? log.responseBody
            : JSON.stringify(log.responseBody, null, 2);

        // JSON 포맷
        try {
          const json = JSON.parse(body);
          return `<code class="hljs json">${
            hljs.highlight(JSON.stringify(json, null, 2), { language: "json" })
              .value
          }</code>`;
        } catch (err) {
          // If not JSON, continue to other formats
        }

        // HTML 포맷
        if (
          body.trim().startsWith("<!doctype html") ||
          body.trim().startsWith("<")
        ) {
          const formatted = this.formatHTML(body);
          return hljs.highlight(formatted, { language: "html" }).value;
        }

        // XML 포맷
        if (body.trim().startsWith("<?xml")) {
          const formatted = this.formatXML(body);
          return hljs.highlight(formatted, { language: "xml" }).value;
        }

        // 기본 텍스트 포맷
        return hljs.highlight(body, { language: "plaintext" }).value;
      };
    },
    formatHTML(html) {
      const formatted = html
        .replace(/>\s*</g, ">\n<") // 태그 간 개행
        .split("\n")
        .map((line, index, array) => {
          const indent = array.slice(0, index).reduce((level, previousLine) => {
            if (previousLine.match(/<[^/!][^>]*[^/]>/)) return level + 1;
            if (previousLine.match(/<\/[^>]+>/)) return level - 1;
            return level;
          }, 0);
          return "  ".repeat(Math.max(indent, 0)) + line;
        })
        .join("\n");
      return formatted;
    },
    formatXML(xml) {
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xml, "application/xml");
      const serializer = new XMLSerializer();
      const formattedXML = serializer.serializeToString(xmlDoc);
      return formattedXML.replace(/>\s*</g, ">\n<");
    },
    uniqueSites() {
      // 중복 제거 후 Site 목록 생성
      return [...new Set(this.logs.map((log) => log.siteId))];
    },
    uniqueEnvironments() {
      // 선택된 Site에 해당하는 환경 목록 생성
      return this.logs
        .filter((log) => log.siteId === this.selectedSite)
        .map((log) => log.environmentId)
        .filter((value, index, self) => self.indexOf(value) === index); // 중복 제거
    },
    uniqueProjects() {
      // 중복 제거 후 Project 목록 생성
      return [...new Set(this.logs.map((log) => log.projectId))];
    },
  },
  mounted() {
    this.fetchHistory(); // 컴포넌트 마운트 시 기록 불러오기
  },
};
</script>

<style scoped>
.badge {
  font-size: 0.6rem;
}

pre {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 0.5rem;
  font-size: 0.9rem;
  overflow: auto;
}

table th {
  text-align: center;
}

.table th,
.table td {
  vertical-align: middle;
  word-wrap: break-word; /* 단어를 강제로 줄바꿈 */
  white-space: normal; /* 줄바꿈을 허용 */
  max-width: 200px; /* 최대 너비 설정 (필요 시 조정) */
}

table th:last-child,
table td:last-child {
  text-align: right;
}

.table-container {
  max-height: 500px; /* 테이블 최대 높이 */
  overflow-y: auto; /* 세로 스크롤 추가 */
  overflow-x: hidden;
}

.history-detail {
  text-align: left;
}
</style>
