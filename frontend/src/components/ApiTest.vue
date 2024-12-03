<template>
  <div
    class="d-flex flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3"
  >
    <form class="d-flex w-100">
      <!-- Input Box -->
      <div class="me-3 flex-grow-1">
        <input
          type="text"
          class="form-control input-api-name"
          placeholder="API Name"
        />
      </div>

      <!-- Select Box and Buttons -->
      <div class="d-flex align-items-center justify-content-end">
        <select
          class="form-select me-2"
          style="width: auto"
          v-model="selectedEnvironment"
        >
          <option v-for="env in environments" :key="env.id" :value="env">
            {{ env.name }}
          </option>
        </select>
        <button type="button" class="btn btn-dark me-2">Save</button>
        <button type="button" class="btn btn-dark">Load</button>
        <a href="#" class="request-add"><i class="bi bi-plus-lg"></i></a>
      </div>
    </form>
  </div>
  <div
    class="d-flex flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3"
  >
    <form class="d-flex w-100">
      <select v-model="method" class="form-select me-2" style="width: auto">
        <option value="GET" selected>GET</option>
        <option value="POST">POST</option>
        <option value="PUT">PUT</option>
        <option value="DELETE">DELETE</option>
        <option value="PATCH">PATCH</option>
      </select>
      <input v-model="url" type="text" class="form-control" placeholder="URL" />
      <button class="btn btn-dark ms-3" @click.prevent="sendRequest">
        Send
      </button>
    </form>
  </div>
  <div class="container mt-4">
    <!-- Tab Navigation -->
    <ul class="nav nav-tabs" id="tabMenu" role="tablist">
      <!-- Headers Tab -->
      <li class="nav-item" role="presentation">
        <button
          class="nav-link active"
          id="headers-tab"
          data-bs-toggle="tab"
          data-bs-target="#headers"
          type="button"
          role="tab"
          aria-controls="headers"
          aria-selected="true"
        >
          HEADERS
        </button>
      </li>
      <!-- Query Parameters Tab -->
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          id="query-tab"
          data-bs-toggle="tab"
          data-bs-target="#query-parameters"
          type="button"
          role="tab"
          aria-controls="query-parameters"
          aria-selected="false"
        >
          Query Parameter
        </button>
      </li>
      <!-- Body Tab -->
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          id="body-tab"
          data-bs-toggle="tab"
          data-bs-target="#body-content"
          type="button"
          role="tab"
          aria-controls="body-content"
          aria-selected="false"
        >
          Body
        </button>
      </li>
    </ul>

    <!-- Tab Contents -->
    <div class="tab-content mt-3" id="tabContent">
      <!-- Headers Content -->
      <div
        class="tab-pane fade show active"
        id="headers"
        role="tabpanel"
        aria-labelledby="headers-tab"
      >
        <h5>Headers</h5>
        <hr />
        <form>
          <div
            v-for="(header, index) in headers"
            :key="index"
            class="row g-3 align-items-center mb-2"
          >
            <div class="col">
              <input
                type="text"
                class="form-control"
                placeholder="Key"
                v-model="header.key"
              />
            </div>
            <div class="col-auto">=</div>
            <div class="col">
              <input
                type="text"
                class="form-control"
                placeholder="Value"
                v-model="header.value"
              />
            </div>
          </div>
          <button
            type="button"
            class="btn btn-dark me-2 mt-4"
            @click="addHeader"
          >
            + Add header
          </button>
        </form>
      </div>

      <!-- Query Parameters Content -->
      <div
        class="tab-pane fade"
        id="query-parameters"
        role="tabpanel"
        aria-labelledby="query-tab"
      >
        <h5>Query Parameters</h5>
        <hr />
        <form>
          <div
            v-for="(queryParam, index) in queryParameters"
            :key="index"
            class="row g-3 align-items-center mb-2"
          >
            <div class="col">
              <input
                type="text"
                class="form-control"
                placeholder="Key"
                v-model="queryParam.key"
                @input="updateUrl"
              />
            </div>
            <div class="col-auto">=</div>
            <div class="col">
              <input
                type="text"
                class="form-control"
                placeholder="Value"
                v-model="queryParam.value"
                @input="updateUrl"
              />
            </div>
          </div>
          <button
            type="button"
            class="btn btn-dark me-2 mt-4"
            @click="addQueryParameter"
          >
            + Add query parameter
          </button>
          <button type="button" class="btn btn-dark mt-4">
            ✓ Select Object variable
          </button>
        </form>
      </div>

      <!-- Body Parameters Content -->
      <div
        class="tab-pane fade"
        id="body-content"
        role="tabpanel"
        aria-labelledby="body-tab"
      >
        <div class="mb-3">
          <label for="bodyType" class="form-label">Body</label>
          <select class="form-select" v-model="selectedBodyType" id="bodyType">
            <option value="text">Text</option>
            <option value="file">File</option>
            <option value="form">Form</option>
          </select>
        </div>

        <!-- Text Body -->
        <div v-if="selectedBodyType === 'text'">
          <label for="textBody" class="form-label">Body</label>
          <textarea
            id="textBody"
            class="form-control"
            rows="10"
            placeholder="Enter your body text here..."
          ></textarea>
        </div>

        <!-- File Upload Body -->
        <div v-if="selectedBodyType === 'file'" class="border p-4 text-center">
          <p>파일을 여기에 올려주세요.</p>
          <input type="file" class="form-control" />
        </div>

        <!-- Form Parameters -->
        <div v-if="selectedBodyType === 'form'">
          <div
            v-for="(formParam, index) in formParameters"
            :key="index"
            class="row g-3 align-items-center mb-2"
          >
            <div class="col">
              <input
                type="text"
                class="form-control"
                placeholder="Key"
                v-model="formParam.key"
              />
            </div>
            <div class="col">
              <select class="form-select" v-model="formParam.type">
                <option value="text">Text</option>
                <option value="file">File</option>
              </select>
            </div>
            <div class="col">
              <input
                v-if="formParam.type === 'text'"
                type="text"
                class="form-control"
                placeholder="Value"
                v-model="formParam.value"
              />
              <input
                v-if="formParam.type === 'file'"
                type="file"
                class="form-control"
              />
            </div>
          </div>
          <button class="btn btn-dark me-2 mt-4" @click="addFormParameter">
            + Add Form parameter
          </button>
          <button class="btn btn-dark mt-4">✓ Select Object variable</button>
        </div>
      </div>

      <!-- Response Section -->
      <div class="response-section mt-4">
        <h5>Response</h5>

        <!-- HTTP Status Code -->
        <div class="status-code bg-secondary text-white p-2 mb-3">
          <span v-if="response.statusCode === null">HTTP Status Code</span>
          <span v-else
            >{{ response.statusCode }} {{ response.statusMessage }}</span
          >
        </div>

        <!-- Response Details -->
        <div v-if="response.statusCode === null" class="text-muted">
          아직 요청이 보내지지 않았습니다.
        </div>

        <div v-else class="d-flex">
          <!-- Response Headers -->
          <div class="response-headers border p-3 me-3">
            <h6>Headers</h6>
            <ul class="list-unstyled">
              <li v-for="(value, key) in response.headers" :key="key">
                <strong>{{ key }}:</strong> {{ value }}
              </li>
            </ul>
          </div>
          <!-- Response Body -->
          <div class="response-body border p-3">
            <h6>Body</h6>
            <pre class="formatted-body">
              <code v-html="formattedBody"></code>
            </pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import hljs from "highlight.js";
import "highlight.js/styles/default.css";

export default {
  name: "ApiTest",
  data() {
    return {
      method: "GET",
      url: "",
      environments: [], // 사용 가능한 환경 목록
      selectedEnvironment: null, // 선택된 환경
      environmentVariables: {}, // 선택된 환경의 변수들
      formParameters: [{ key: "id", type: "text", value: "" }],
      queryParameters: [{ key: "", value: "" }],
      headers: [{ key: "", value: "" }],
      file: null,
      selectedBodyType: "text",
      response: {
        statusCode: null, // 응답 상태 코드
        statusMessage: "",
        headers: {}, // 응답 헤더
        body: "", // 응답 바디
      },
    };
  },
  methods: {
    addFormParameter() {
      this.formParameters.push({ key: "", type: "text", value: "" });
    },
    removeFormParameter(index) {
      this.formParameters.splice(index, 1);
    },
    addQueryParameter() {
      this.queryParameters.push({ key: "", value: "" });
    },
    removeQueryParam(index) {
      this.queryParams.splice(index, 1);
    },
    addHeader() {
      this.headers.push({ key: "", value: "" });
    },
    removeHeader(index) {
      this.headers.splice(index, 1);
    },
    handleFileUpload(event) {
      this.file = event.target.files[0];
    },
    async fetchEnvironments() {
      try {
        const response = await this.$axios.get("/api/environments");
        this.environments = response.data; // 환경 목록 저장
        if (this.environments.length > 0) {
          this.selectedEnvironment = this.environments[0]; // 첫 번째 요소 자동 선택
        }
      } catch (error) {
        console.error("Failed to fetch environments:", error);
      }
    },
    async fetchEnvironmentVariables() {
      if (!this.selectedEnvironment) {
        this.environmentVariables = {};
        return;
      }

      const environmentId = this.selectedEnvironment.id;
      try {
        const response = await this.$axios.get(
          `/api/environments/variables/${environmentId}`
        );
        // 환경 변수를 키-값으로 변환
        this.environmentVariables = response.data.reduce((acc, variable) => {
          acc[variable.key] = variable.value;
          return acc;
        }, {});
      } catch (error) {
        console.error("Failed to load environment variables:", error);
        this.environmentVariables = {};
      }
    },
    saveEnvironment() {
      // 현재 선택된 환경을 저장하는 로직
      console.log("Selected Environment:", this.selectedEnvironment);
    },
    loadEnvironment() {
      if (this.selectedEnvironment) {
        // 선택한 환경을 기반으로 다른 값을 설정
        console.log("Loading environment:", this.selectedEnvironment);
      } else {
        console.warn("No environment selected!");
      }
    },
    updateUrl() {
      const baseUrl = this.url.split("?")[0]; // URL에서 Query Parameters 제거
      const queryString = this.queryParameters
        .filter((param) => param.key.trim() !== "") // 빈 키 제거
        .map(
          (param) =>
            `${encodeURIComponent(param.key)}=${encodeURIComponent(
              param.value
            )}`
        )
        .join("&");
      this.url = queryString ? `${baseUrl}?${queryString}` : baseUrl; // Query Parameters 추가
    },
    resolveTemplateVariables(template) {
      if (!template || typeof template !== "string") return template;

      return template.replace(/{{\s*(\w+)\s*}}/g, (match, key) => {
        // 해당 키가 존재하면 대체
        if (
          Object.prototype.hasOwnProperty.call(this.environmentVariables, key)
        ) {
          return this.environmentVariables[key];
        }
        // 만약 변수를 찾을 수 없다면 기존 템플릿 변수 사용
        console.warn(`Environment variable ${key} not found.`);
        return match;
      });
    },

    preprocessParameters() {
      // URL 템플릿 변수 대체
      const processedUrl = this.resolveTemplateVariables(this.url);

      // Form Parameters 템플릿 변수 대체
      const processedFormParameters = this.formParameters.map((param) => ({
        key: param.key,
        value: this.resolveTemplateVariables(param.value),
      }));

      // Query Parameters 템플릿 변수 대체
      const processedQueryParameters = this.queryParameters.map((param) => ({
        key: param.key,
        value: this.resolveTemplateVariables(param.value),
      }));

      return {
        processedUrl,
        processedFormParameters,
        processedQueryParameters,
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
    async sendRequest() {
      try {
        // 요청 데이터 전처리
        const { processedUrl, processedFormParameters } =
          this.preprocessParameters();

        // 헤더를 JSON 객체로 생성
        const headers = this.headers.reduce((acc, header) => {
          if (header.key) acc[header.key] = header.value;
          return acc;
        }, {});

        // FormData 객체 생성
        let data = new FormData();

        const requestBody = {};
        processedFormParameters.forEach((param) => {
          requestBody[param.key] = param.value; // key-value 매핑
        });

        // ProxyRequest의 JSON 데이터를 문자열로 추가
        const jsonRequest = {
          method: this.method,
          url: processedUrl,
          headers: headers,
          body: requestBody,
        };
        data.append(
          "request",
          new Blob([JSON.stringify(jsonRequest)], { type: "application/json" })
        );

        // 파일 추가 (선택사항)
        if (this.selectedBodyType === "file" && this.file) {
          data.append("file", this.file);
        }

        // 요청 전송
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
      } catch (error) {
        // 에러 처리
        this.response = {
          statusCode: error.response?.status || "Error",
          statusMessage: error.response?.statusText || "Request failed",
          body: error.response?.data || "Request failed.",
        };
      }
    },
  },
  mounted() {
    this.fetchEnvironments();
  },
  computed: {
    formattedBody() {
      if (!this.response.body) return "";

      // Ensure body is a string
      const body =
        typeof this.response.body === "string"
          ? this.response.body
          : JSON.stringify(this.response.body, null, 2);

      // If JSON
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

      // 기본 포맷 (텍스트)
      return hljs.highlight(body, { language: "plaintext" }).value;
    },
  },
  watch: {
    // URL이 직접 변경될 경우 Query Parameters 업데이트
    url(newUrl) {
      const queryIndex = newUrl.indexOf("?");
      if (queryIndex !== -1) {
        const queryString = newUrl.substring(queryIndex + 1);
        const queryArray = queryString.split("&").map((param) => {
          const [key, value] = param.split("=");
          return {
            key: decodeURIComponent(key),
            value: decodeURIComponent(value || ""),
          };
        });
        this.queryParameters = queryArray;
      } else {
        this.queryParameters = [{ key: "", value: "" }];
      }
    },
    selectedEnvironment: {
      handler() {
        this.fetchEnvironmentVariables(); // 환경 변수 로드
      },
      immediate: true, // 초기 로드 시에도 호출
    },
  },
};
</script>

<style scoped>
.input-api-name {
  border-width: 0 0 1px;
  border-radius: 0;
}

.request-add {
  margin-left: 10px;
  color: black;
}

textarea {
  resize: none;
}

/* Response CSS Start */

.response-section h5 {
  margin-bottom: 1rem;
}

.status-code {
  font-size: 1.2rem;
}

.response-headers,
.response-body {
  flex: 1;
  overflow: auto;
}

.response-body pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
}

/* Response CSS End */
</style>
