<template>
  <div class="api-tester d-flex flex-column">
    <div class="request-section flex-glow-1">
      <div
        class="d-flex flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3"
      >
        <form class="d-flex w-100">
          <!-- Input Box -->
          <div class="me-3 flex-grow-1">
            <span v-if="apiPath" style="font-size: x-small">{{ apiPath }}</span>
            <input
              v-model="apiName"
              type="text"
              class="form-control input-api-name"
              placeholder="API Name"
              maxlength="50"
              style="margin-bottom: 10px"
            />
            <span v-if="fileConflict" class="change-warning">
              ⚠️ 파일에 변경 사항이 있습니다. 적용하시려면
              <a href="#" @click.prevent="reloadApi">여기</a>를 눌러 주세요.
            </span>
          </div>

          <!-- Select Box and Buttons -->
          <div class="d-flex align-items-center justify-content-end">
            <button
              type="button"
              class="btn btn-dark me-2"
              @click.prevent="openSaveModal"
            >
              저장
            </button>
            <a
              href="#"
              class="me-2 position-relative request-add"
              @click.prevent="openHistoryManageModal"
            >
            <i class="bi bi-file-text"></i>
            </a>
            <a href="#" class="request-add" @click="saveApiDataPlus">
              <i class="bi bi-plus-lg"></i>
            </a>
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
          <input
            v-model="url"
            type="text"
            class="form-control"
            placeholder="URL"
            @keyup.enter="sendRequest"
            :title="resolveTooltip(url)"
          />
          <button
            v-if="!isRequest"
            class="btn btn-dark ms-3"
            @click.prevent="sendRequest"
          >
            Send
          </button>
          <button
            v-else
            class="btn btn-dark ms-3"
            style="width: 150px"
            disabled
          >
            <i class="bi bi-stop-fill"></i>Wait...
          </button>
          <div class="spinner-container">
            <div
              v-if="isSaving"
              class="spinner-border spinner-border-sm ms-2"
              role="status"
            >
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
        </form>
      </div>
      <div v-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>
      <div class="container mt-4 pb-4">
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
              @click="updateCurrentTab('queryParameters')"
            >
              Query Parameter
            </button>
          </li>
          <!-- Body Tab -->
          <li class="nav-item" role="presentation">
            <button
              v-if="method === 'POST' || method === 'PUT' || method === 'PATCH'"
              class="nav-link"
              id="body-tab"
              data-bs-toggle="tab"
              data-bs-target="#body-content"
              type="button"
              role="tab"
              aria-controls="body-content"
              aria-selected="false"
              @click="updateCurrentTab('formParameters')"
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
                    :title="resolveTooltip(header.key)"
                  />
                </div>
                <div class="col-auto">=</div>
                <div class="col">
                  <input
                    type="text"
                    class="form-control"
                    placeholder="Value"
                    v-model="header.value"
                    :title="resolveTooltip(header.value)"
                    @keyup.enter="addHeader"
                  />
                </div>
                <div class="col-auto">
                  <i
                    class="bi bi-x-lg remove-icon"
                    @click="removeHeader(index)"
                  ></i>
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
                    :title="resolveTooltip(queryParam.key)"
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
                    :title="resolveTooltip(queryParam.value)"
                    @input="updateUrl"
                    @keyup.enter="addQueryParameter"
                  />
                </div>
                <div class="col-auto">
                  <i
                    class="bi bi-x-lg remove-icon"
                    @click="removeQueryParam(index)"
                  ></i>
                </div>
              </div>
              <button
                type="button"
                class="btn btn-dark me-2 mt-4"
                @click="addQueryParameter"
              >
                + Add query parameter
              </button>
              <button
                type="button"
                class="btn btn-dark mt-4"
                @click="openDatasetModal"
              >
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
              <select
                class="form-select"
                v-model="selectedBodyType"
                id="bodyType"
              >
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
            <div
              v-if="selectedBodyType === 'file'"
              class="border p-4 text-center"
            >
              <p>파일을 여기에 올려주세요.</p>
              <input
                type="file"
                class="form-control"
                @change="handleFileUpload"
              />
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
                    :title="resolveTooltip(formParam.key)"
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
                    :title="resolveTooltip(formParam.value)"
                    @keyup.enter="addFormParameter"
                  />
                  <input
                    v-if="formParam.type === 'file'"
                    type="file"
                    class="form-control"
                  />
                </div>
                <div class="col-auto">
                  <i
                    class="bi bi-x-lg remove-icon"
                    @click="removeFormParameter(index)"
                  ></i>
                </div>
              </div>
              <button class="btn btn-dark me-2 mt-4" @click="addFormParameter">
                + Add Form parameter
              </button>
              <button class="btn btn-dark mt-4" @click="openDatasetModal">
                ✓ Select Object variable
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Resizer -->
    <div class="resizer" @mousedown="startResize">
      <i class="bi bi-grip-horizontal" @click="handleResponseSize"></i>
    </div>

    <!-- Response Section -->
    <div
      class="response-section mt-4"
      :style="{
        height: responseHeight + 'px',
        overflow: showResponse ? 'hidden' : 'auto',
      }"
    >
      <h5>Response</h5>

      <!-- HTTP Status Code -->
      <div
        class="status-code text-white p-2 mb-3"
        :style="{ backgroundColor: statusColor || '#6c757d' }"
      >
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
  <!-- SaveModal 추가 -->
  <SaveModal
    v-if="showSaveModal"
    :selectedProject="selectedProject"
    :projects="projects"
    :apiData="apiData"
    :isLoad="isLoad"
    :items="items"
    @close="closeSaveModal"
    @project-saved="updateSavedProject"
    @save-api="saveApiToProject"
    @load-api="handleLoadApi"
    @update-items="this.$emit('update-items')"
  />
  <HistoryManageModal
    v-if="showHistoryManageModal"
    :apiData="apiData"
    @close="closeHistoryManageModal"
    @project-saved="updateSavedProject"
    @save-api="saveApiToProject"
    @load-api="handleLoadApi"
    @update-items="this.$emit('update-items')"
  />
  <DatasetModal
    v-if="showDatasetModal"
    :current-tab="currentTab"
    :selectedProject="selectedProject"
    @add-variables="handleAddVariables"
    @close="showDatasetModal = false"
  />
  <SiteEnvironmentModal
    :isVisible="showSiteEnvironmentModal"
    :selectedProject="selectedProject"
    @modal-selected-environment="handleModalSelectedEnvironment"
    @close="showSiteEnvironmentModal = false"
  />
  <!-- 모달 컴포넌트 -->
</template>

<script>
import hljs from "highlight.js";
import "highlight.js/styles/default.css";
import SaveModal from "./SaveModal.vue";
import DatasetModal from "./DatasetModal.vue";
import SiteEnvironmentModal from "./SiteEnvironmentModal.vue";
import HistoryManageModal from "./HistoryManageModal.vue";

export default {
  name: "ApiTest",
  components: {
    SaveModal,
    HistoryManageModal,
    DatasetModal,
    SiteEnvironmentModal,
  },
  props: {
    projectAuth: String,
    selectedProject: Object,
    propSelectedSite: Object,
    propSelectedEnvironment: Object,
    propSites: Array,
    propFileConflict: Boolean,
    propEnvironments: Array,
    tempApi: Array,
    modalSelectedEnvironment: Object,
    isSetting: Boolean,
    items: Array,
    updatedTempApi: Array,
    changeMessage: String,
  },
  data() {
    return {
      apiId: "",
      itemId: "",
      apiPath: "",
      apiName: "",
      method: "GET",
      url: "",
      savedProject: null,
      headers: [{ key: "", value: "" }],
      queryParameters: [{ key: "", value: "" }],
      formParameters: [{ key: "", type: "text", value: "" }],
      showModal: false,
      showSaveModal: false,
      showDatasetModal: false,
      showSettingsModal: false,
      showSiteEnvironmentModal: false,
      showHistoryManageModal: false,
      showResponse: false, // 결과 코드만 볼지, 결과를 모두 볼지 여부
      projects: [],
      selectedFolder: null,
      datasets: [], // 데이터셋
      selectedDataset: null, // 선택된 데이터셋
      datasetVariables: [], // 데이터셋 변수들
      selectedSite: null, // 선택된 사이트
      selectedEnvironment: null, // 선택된 환경
      selectedVariables: [], // 선택된 데이터셋 변수들
      currentTab: "queryParameters", // 선택된 탭 이름
      environments: [], // 사용 가능한 환경 목록
      environmentVariables: {}, // 선택된 환경의 변수들
      file: null,
      selectedBodyType: "text",
      response: {
        statusCode: null, // 응답 상태 코드
        statusMessage: "",
        headers: {}, // 응답 헤더
        body: "", // 응답 바디
      },
      responseHeight: 100, // 초기 Response 영역 높이
      isResizing: false,
      startY: 0, // 마우스 시작 Y 좌표
      startHeight: 0, // 초기 높이
      statusColor: "#6c757d",
      errorMessage: "",
      resizing: false,
      autoSaveUse: false, // 자동 저장 사용 여부
      autoSaveTimer: null, // 입력 중 자동 저장 타이머
      autoSaveDelayTimer: null, // 입력 후 자동 저장 타이머
      autoSaveTime: 0, // 입력 중 자동 저장 시간
      autoSaveTerm: 0, // 입력 후 자동 저장 시간
      autoSavePath: null, // 자동 저장 경로
      hasChanges: false, // 데이터 변경 상태
      initialState: null, // 초기 상태 저장
      isInitializing: true, // 초기화 상태 플래그
      isSaving: false, // 저장 중
      isSave: false, // 현재 작업 중인 파일 저장 여부
      isRequest: false,
      isLoad: false,
      isReloading: false,
      isResponseExpanded: false, // Response 아이콘 토글 상태
    };
  },
  methods: {
    resetInputs() {
      // 모든 입력 필드를 초기화
      this.apiId = "";
      this.itemId = "";
      this.apiPath = "";
      this.apiName = "";
      this.method = "GET";
      this.url = "";
      this.headers = [{ key: "", value: "" }];
      this.queryParameters = [{ key: "", value: "" }];
      this.formParameters = [{ key: "", type: "text", value: "" }];
      this.selectedBodyType = "text";
      this.response = {
        statusCode: null,
        statusMessage: "",
        headers: {},
        body: "",
      };
      this.errorMessage = "";
      this.$emit("item-selected", null);
    },
    openSaveModal() {
      if (this.apiName === "") {
        alert("API 이름을 입력해 주세요.");
        return;
      }

      if (this.projectAuth === "read") {
        alert("저장 권한이 없습니다.");
        return;
      }

      this.isLoad = false;
      this.showSaveModal = true;
    },
    openHistoryManageModal() {
      this.showHistoryManageModal = true;
    },
    openLoadModal() {
      this.isLoad = true;
      this.showSaveModal = true;
    },
    openDatasetModal() {
      this.showDatasetModal = true;
    },
    closeModal() {
      this.showModal = false;
      this.selectedDataset = null;
      this.datasetVariables = [];
      this.selectedVariables = [];
    },
    closeSaveModal() {
      this.showSaveModal = false;
      this.isLoad = false;
    },
    closeHistoryManageModal() {
      this.showHistoryManageModal = false;
    },
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
      this.queryParameters.splice(index, 1);
    },
    addHeader() {
      this.headers.push({ key: "", value: "" });
    },
    removeHeader(index) {
      this.headers.splice(index, 1);
    },
    startResize(event) {
      this.isResizing = true;
      this.startY = event.clientY; // 마우스 시작 Y 좌표
      this.startHeight = this.responseHeight; // 초기 높이 저장

      // 이벤트 리스너 추가
      document.addEventListener("mousemove", this.resize);
      document.addEventListener("mouseup", this.stopResize);
    },
    resize(event) {
      if (!this.isResizing) return;
      const deltaY = this.startY - event.clientY; // 반대로 계산
      this.responseHeight = Math.max(this.startHeight + deltaY, 150); // 최소 높이 150px 제한
    },
    stopResize() {
      this.isResizing = false;

      // 이벤트 리스너 제거
      document.removeEventListener("mousemove", this.resize);
      document.removeEventListener("mouseup", this.stopResize);
    },
    handleFileUpload(event) {
      this.file = event.target.files[0];
      console.log(this.file);
    },
    handleAddVariables({ variables, currentTab }) {
      if (!variables || variables.length === 0) {
        alert("No variables selected.");
        return;
      }

      if (currentTab === "queryParameters") {
        // queryParameters에 추가
        variables.forEach((variable) => {
          this.queryParameters.push({
            key: variable.name,
          });
        });
      } else if (currentTab === "formParameters") {
        // formParameters에 추가
        variables.forEach((variable) => {
          this.formParameters.push({
            key: variable.name,
            type: "text", // 기본값: text
          });
        });
      }
    },
    handleLoadApi(api) {
      this.apiName = api.name;
      this.method = api.method;
      this.url = api.url;
      this.headers = JSON.parse(api.headers) || [{ key: "", value: "" }];
      this.queryParameters = JSON.parse(api.queryParameters) || [
        { key: "", value: "" },
      ];
      this.formParameters = JSON.parse(api.formParameters) || [
        { key: "", type: "text", value: "" },
      ];
      this.file = api.file;
      this.selectedBodyType = api.selectedBodyType;
      this.response = {
        statusCode: null,
        statusMessage: "",
        headers: {},
        body: "",
      };
      this.selectedEnvironment = api.environmentId;
      this.closeSaveModal();
    },
    handleResponseSize() {
      // 높이를 토글
      if (this.isExpanded) {
        this.responseHeight = 100; // 줄어든 높이
      } else {
        this.responseHeight = 1200; // 늘어난 높이
      }
      this.isExpanded = !this.isExpanded; // 상태 토글
    },
    async fetchEnvironmentVariables() {
      if (!this.selectedEnvironment) {
        this.environmentVariables = {};
        return;
      }

      const environmentId = this.selectedEnvironment.id;

      try {
        const response = await this.$axios.get(
          `/api/environments/variables/${environmentId}`,
          {
            headers: {
              MID: "P02002", // 헤더에 MID 추가
            },
          }
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
    async fetchLoginUserInfo() {
      try {
        const response = await this.$axios.get("/api/user/findUserByEmail");

        this.autoSaveUse = response.data.user.autoSaveUse;
        this.autoSaveTime = response.data.user.autoSaveTime;
        this.autoSaveTerm = response.data.user.autoSaveTerm;
        this.autoSavePath = response.data.user.autoSavePath;
        this.showResponse = response.data.user.showResponse;

        if (this.showResponse) {
          this.responseHeight = 100;
        } else {
          this.responseHeight = 300;
        }
      } catch (error) {
        console.log("Failed Login Verified: " + error);
      }
    },
    applyVariables() {
      if (this.selectedVariables.length === 0) {
        alert("Please select at least one variable.");
        return;
      }
      // Apply variables to the current tab (Query Parameters or Form Data)
      this.selectedVariables.forEach((variable) => {
        const newEntry = {
          key: variable.key,
          value: `{{${variable.key}}}`, // Use template syntax for variable substitution
        };

        if (this.currentTab === "queryParameters") {
          this.queryParameters.push(newEntry);
        } else if (this.currentTab === "formParameters") {
          this.formParameters.push(newEntry);
        }
      });

      // Close modal
      this.closeModal();
    },
    getCurrentApiData() {
      return {
        apiName: this.apiName,
        method: this.method,
        url: this.url,
        headers: this.headers,
        queryParameters: this.queryParameters,
        formParameters: this.formParameters,
        selectedBodyType: this.selectedBodyType,
        selectedEnvironment: this.selectedEnvironment,
      };
    },
    saveEnvironment() {
      // 현재 선택된 환경을 저장하는 로직
      console.log("Selected Environment:", this.selectedEnvironment);
    },
    saveTempApi() {
      if (!this.apiName) {
        alert("API 이름을 입력해주세요.");
        return;
      }
      if (this.projectAuth === "read") {
        return;
      }
      // 현재 데이터를 App.vue로 전달
      this.$emit("temp-save-api", {
        name: this.apiName,
        method: this.method,
        url: this.url,
        headers: this.headers,
        queryParameters: this.queryParameters,
        formParameters: this.formParameters,
        file: this.file,
        selectedBodyType: this.selectedBodyType,
        selectedEnvironment: this.selectedEnvironment,
        response: this.response,
      });
      // 입력 필드 초기화
      this.apiName = "";
      this.method = "GET";
      this.url = "";
      this.headers = [{ key: "", value: "" }];
      this.queryParameters = [{ key: "", value: "" }];
      this.formParameters = [{ key: "", type: "text", value: "" }];
      this.file = null;
      this.selectedBodyType = "text";
      this.response = {
        statusCode: null,
        statusMessage: "",
        headers: {},
        body: "",
      };
      this.selectedEnvironment = null;
    },
    saveApiToProject(selectedFolder) {
      const apiData = {
        name: this.apiName,
        method: this.method,
        url: this.url,
        headers: this.headers,
        queryParameters: this.queryParameters,
        formParameters: this.formParameters,
        file: this.file,
        selectedBodyType: this.selectedBodyType,
        selectedEnvironment: this.selectedEnvironment,
      };

      this.$axios
        .post(
          "/api/projects/items",
          {
            projectId: selectedFolder.projectId,
            parentId: selectedFolder.id,
            type: "api",
            name: apiData.name,
            depth: selectedFolder.depth + 1,
            apiData,
          },
          { showSpinner: false }
        )
        .then(() => {
          alert("API가 성공적으로 저장되었습니다.");
          this.isSave = true;
          this.showModal = false;
        })
        .catch((error) => {
          console.error("Failed to save API:", error);
          alert("Failed to save API.");
        });

      this.showSaveModal = false;
    },
    saveInitialState() {
      this.initialState = {
        id: this.apiId,
        itemId: this.itemId,
        name: this.apiName || "TempAPI",
        method: this.method,
        url: this.url,
        headers: this.sortAndStringify(this.headers),
        queryParameters: this.sortAndStringify(this.queryParameters),
        formParameters: this.sortAndStringify(this.formParameters),
        file: this.file,
        selectedBodyType: this.selectedBodyType,
        selectedEnvironment: this.selectedEnvironment,
      };
      this.hasChanges = false; // 초기 상태에서는 변경 없음
    },
    sortAndStringify(obj) {
      if (Array.isArray(obj)) {
        return JSON.stringify(
          obj.map((item) => {
            if (typeof item === "object") {
              return this.sortObjectKeys(item);
            }
            return item;
          })
        );
      }
      return JSON.stringify(this.sortObjectKeys(obj));
    },

    sortObjectKeys(obj) {
      return Object.keys(obj)
        .sort()
        .reduce((sorted, key) => {
          sorted[key] = obj[key];
          return sorted;
        }, {});
    },
    triggerAutoSave() {
      if (!this.autoSaveUse) return; // 자동 저장 사용 안 할 경우
      if (this.projectAuth === "read") return; // 권한이 read인 경우
      const autoSaveTimeMs = this.autoSaveTime * 1000; // 초 → 밀리초 변환
      const autoSaveTermMs = this.autoSaveTerm * 1000;

      // 입력 중 자동 저장 (autoSaveTime)
      if (this.autoSaveTimer) {
        clearInterval(this.autoSaveTimer); // 기존 타이머 제거
      }
      this.autoSaveTimer = setInterval(async () => {
        if (this.hasChanges) {
          this.isSaving = true;
          await this.saveApiData(); // 자동 저장 실행
          this.isSaving = false;
        }
      }, autoSaveTimeMs);

      // 입력 후 자동 저장 (autoSaveTerm)
      if (this.autoSaveDelayTimer) {
        clearTimeout(this.autoSaveDelayTimer); // 기존 딜레이 타이머 제거
      }
      this.autoSaveDelayTimer = setTimeout(async () => {
        if (this.hasChanges) {
          this.showSpinnerAndSave();
          this.hasChanges = false; // 변경 상태 초기화
          this.$emit("input-modified", this.hasChanges);
        }
      }, autoSaveTermMs);
    },
    // 스피너를 보여준 후 저장 작업 실행
    showSpinnerAndSave() {
      this.isSaving = true;

      // 1초 대기 후 저장 작업 실행
      setTimeout(async () => {
        try {
          await this.saveApiData(); // 자동 저장 실행
        } catch (error) {
          console.error("자동 저장 중 오류 발생:", error);
        } finally {
          this.isSaving = false; // 스피너 숨김
        }
      }, 1000);
    },
    async saveApiData() {
      if (this.projectAuth === "read") return;
      try {
        // this.apiId가 있는 경우(이미 저장된 파일의 경우) 업데이트
        if (this.apiId !== "") {
          const project = await this.$axios.patch(
            "/api/projects/update/projectItemName",
            {
              id: this.itemId,
              projectId: Number(this.selectedProject.id),
              parentId: this.autoSavePath,
              type: "api",
              name: this.apiName || "TempAPI",
              depth: 1,
            },
            { showSpinner: false }
          );

          const apiData = {
            name: this.apiName || "TempAPI",
            itemId: Number(project.data.updatedItem.id),
            method: this.method,
            url: this.url,
            headers: JSON.stringify(this.headers),
            queryParameters: JSON.stringify(this.queryParameters),
            formParameters: JSON.stringify(this.formParameters),
            file: this.file,
            selectedBodyType: this.selectedBodyType,
            selectedEnvironment: this.selectedEnvironment,
          };

          await this.$axios.patch(
            "/api/apis",
            {
              id: this.apiId,
              ...apiData,
            },
            { showSpinner: false }
          );

          this.hasChanges = false; // 저장 완료 후 상태 초기화
          this.$emit("refresh-sidebar", this.itemId);
          this.$emit("input-modified", this.hasChanges);
          this.buildApiPath(this.itemId);
          console.log("자동 저장 완료");
        } else {
          // this.apiId가 없는 경우 추가
          const project = await this.$axios.post(
            "/api/projects/add-api",
            {
              projectId: Number(this.selectedProject.id),
              parentId: this.autoSavePath,
              type: "api",
              name: this.apiName || "TempAPI",
              depth: 1,
            },
            { showSpinner: false },
            {
              headers: {
                MID: "P03002",
              },
            }
          );

          const apiData = {
            name: this.apiName || "TempAPI",
            itemId: Number(project.data.id),
            method: this.method,
            url: this.url,
            headers: JSON.stringify(this.headers),
            queryParameters: JSON.stringify(this.queryParameters),
            formParameters: JSON.stringify(this.formParameters),
            file: this.file,
            selectedBodyType: this.selectedBodyType,
            selectedEnvironment: this.selectedEnvironment,
          };

          // 저장된 apiId 반환
          const savedApi = await this.$axios.post("/api/apis", apiData, {
            showSpinner: false,
          });

          this.apiId = savedApi.data.id;
          this.itemId = Number(project.data.id);
          this.hasChanges = false; // 저장 완료 후 상태 초기화
          this.$emit("refresh-sidebar", this.itemId);
          this.$emit("input-modified", this.hasChanges);
          console.log("자동 저장 완료");
        }

        this.isSave = true;
      } catch (error) {
        console.log("Failed save projectItem: " + error);
        alert("API 자동 저장에 실패했습니다.");
      } finally {
        this.isSaving = false;
      }
    },
    async saveApiDataPlus() {
      if (this.apiName === "") {
        alert("API 이름을 입력해 주세요.");
        return;
      }

      if (this.projectAuth === "read") {
        return;
      }

      try {
        if (this.apiId !== "") {
          const project = await this.$axios.patch(
            "/api/projects/update/projectItemName",
            {
              id: this.itemId,
              projectId: Number(this.selectedProject.id),
              parentId: this.autoSavePath,
              type: "api",
              name: this.apiName || "TempAPI",
              depth: 1,
            },
            { showSpinner: false }
          );

          const apiData = {
            name: this.apiName || "TempAPI",
            itemId: Number(project.data.updatedItem.id),
            method: this.method,
            url: this.url,
            headers: JSON.stringify(this.headers),
            queryParameters: JSON.stringify(this.queryParameters),
            formParameters: JSON.stringify(this.formParameters),
            file: this.file,
            selectedBodyType: this.selectedBodyType,
            selectedEnvironment: this.selectedEnvironment,
          };

          await this.$axios.patch(
            "/api/apis",
            {
              id: this.apiId,
              ...apiData,
            },
            { showSpinner: false }
          );

          this.resetInputs();

          this.hasChanges = false; // 저장 완료 후 상태 초기화
          this.$emit("refresh-sidebar", this.itemId);
          this.$emit("input-modified", this.hasChanges);
          console.log("자동 저장 완료");
        } else {
          const project = await this.$axios.post(
            "/api/projects/add-api",
            {
              projectId: Number(this.selectedProject.id),
              parentId: this.autoSavePath,
              type: "api",
              name: this.apiName || "TempAPI",
              depth: 1,
            },
            { showSpinner: false },
            {
              headers: {
                MID: "P03002",
              },
            }
          );

          const apiData = {
            name: this.apiName || "TempAPI",
            itemId: Number(project.data.updatedItem.id),
            method: this.method,
            url: this.url,
            headers: JSON.stringify(this.headers),
            queryParameters: JSON.stringify(this.queryParameters),
            formParameters: JSON.stringify(this.formParameters),
            file: this.file,
            selectedBodyType: this.selectedBodyType,
            selectedEnvironment: this.selectedEnvironment,
          };

          this.resetInputs();

          await this.$axios.post("/api/apis", apiData, { showSpinner: false });
          this.hasChanges = false; // 저장 완료 후 상태 초기화
          this.$emit("refresh-sidebar", this.itemId);
          this.$emit("input-modified", this.hasChanges);
          this.apiId = "";
          this.itemId = "";
          console.log("저장 완료");
        }
      } catch (error) {
        console.log("Failed save projectItem: " + error);
        alert("API 저장에 실패했습니다.");
      } finally {
        this.isSaving = false;
      }
    },
    markChanges() {
      if (
        this.isInitializing ||
        this.isReloading ||
        this.projectAuth === "read"
      )
        return; // 초기화, 데이터 로드, 권한이 읽기면 무시

      const currentState = {
        id: this.apiId,
        itemId: this.itemId,
        name: this.apiName || "TempAPI",
        method: this.method,
        url: this.url,
        headers: this.sortAndStringify(this.headers),
        queryParameters: this.sortAndStringify(this.queryParameters),
        formParameters: this.sortAndStringify(this.formParameters),
        file: this.file,
        selectedBodyType: this.selectedBodyType,
        selectedEnvironment: this.selectedEnvironment,
      };

      // 객체를 문자열로 변환하여 비교
      const currentStateString = JSON.stringify(currentState);
      const initialStateString = JSON.stringify(this.initialState);

      this.hasChanges = currentStateString !== initialStateString;

      if (this.hasChanges) {
        this.triggerAutoSave(); // 변경 시 자동 저장 실행
      }

      this.$emit("input-modified", {
        hasChanges: this.hasChanges,
        currentState,
      }); // 두 값 전달
    },
    loadEnvironment() {
      if (this.selectedEnvironment) {
        // 선택한 환경을 기반으로 다른 값을 설정
        console.log("Loading environment:", this.selectedEnvironment);
      } else {
        console.warn("No environment selected!");
      }
    },
    async reloadApi() {
      this.fileConflict = false;
      this.isReloading = true;
      this.hasChanges = false;

      try {
        const response = await this.$axios.get(`/api/apis/${this.itemId}`);
        if (response.data) {
          // API 데이터 다시 로드
          const apiData = response.data.api;

          this.apiName = apiData.name || "";
          this.method = apiData.method || "GET";
          this.url = apiData.url || ""; // url 기본값 설정

          // JSON 파싱
          try {
            this.headers = apiData.headers
              ? JSON.parse(apiData.headers)
              : [{ key: "", value: "" }];
          } catch (e) {
            console.error("Headers JSON 파싱 실패:", e);
            this.headers = [{ key: "", value: "" }];
          }

          try {
            this.queryParameters = apiData.queryParameters
              ? JSON.parse(apiData.queryParameters)
              : [{ key: "", value: "" }];
          } catch (e) {
            console.error("Query Parameters JSON 파싱 실패:", e);
            this.queryParameters = [{ key: "", value: "" }];
          }

          try {
            this.formParameters = apiData.formParameters
              ? JSON.parse(apiData.formParameters)
              : [{ key: "", type: "text", value: "" }];
          } catch (e) {
            console.error("Form Parameters JSON 파싱 실패:", e);
            this.formParameters = [{ key: "", type: "text", value: "" }];
          }

          this.file = apiData.file || null;
          this.selectedBodyType = apiData.selectedBodyType || "text";
          this.selectedEnvironment = apiData.selectedEnvironment || null;
        }
      } catch (error) {
        console.error("API 로드 실패:", error);
        alert("파일을 다시 로드하는 중 오류가 발생했습니다.");
      } finally {
        this.isReloading = false;
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
    updateCurrentTab(tab) {
      this.currentTab = tab;
    },
    updateSavedProject(newApis) {
      this.$emit("refresh-sidebar", newApis.data.itemId);
      this.buildApiPath(newApis.itemId);

      this.showSaveModal = false;
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
    resolveTooltip(value) {
      if (!value) return "";

      // 정규 표현식을 사용하여 {{key}} 찾기
      const matches = value.match(/{{\s*(\w+)\s*}}/);

      if (matches) {
        const key = matches[1]; // 환경 변수 키 추출
        // 환경 변수 객체에서 해당 키의 값을 찾아 반환
        return this.environmentVariables[key] || "해당 변수의 값이 없습니다.";
      }
      return "";
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
      this.errorMessage = "";
      const startTime = performance.now(); // 요청 시작 시간
      try {
        this.isRequest = true;
        if (!this.url) {
          this.errorMessage = "요청을 보낼 URL을 입력해 주세요.";
          return;
        }
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

        console.log(data);

        // 요청 전송
        const response = await this.$axios.post("/api/proxy", data, {
          headers: {
            "Content-Type": "multipart/form-data", // FormData 형식
          },
          validateStatus: (status) => status >= 100 && status < 600, // 모든 상태 코드 허용
        });

        console.log(response);

        // 응답 데이터 설정
        this.response = {
          statusCode: response.status,
          statusMessage: response.statusText,
          body: response.data.body,
          headers: response.data.headers,
        };

        const endTime = performance.now(); // 요청 완료 시간
        const elapsedTime = Math.round(endTime - startTime); // 소요 시간(ms)

        this.addHistory(this.response, elapsedTime);
      } catch (error) {
        // 에러 처리
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
      } finally {
        this.isRequest = false;
      }
    },
    async buildApiPath(itemId) {
      const pathSegments = [];
      let isFirstItem = true; // 첫 번째 아이템인지 확인하는 플래그

      while (itemId) {
        try {
          // 현재 ID로 API 호출하여 parent_id와 name 가져오기
          const response = await this.$axios.get(
            `/api/projects/item/${itemId}`,
            { showSpinner: false },
            {
              headers: {
                MID: "P03003",
              },
            }
          );
          const item = response.data;

          // 첫 번째 아이템은 제외
          if (isFirstItem) {
            isFirstItem = false;
          } else {
            // 이름 추가
            pathSegments.unshift("/" + item.name);
          }

          // 다음 parent_id 설정
          itemId = item.parentId;
        } catch (error) {
          console.error("경로를 가져오는 중 오류 발생:", error);
          break;
        }
      }

      // 경로를 연결하여 반환
      return pathSegments.join("");
    },
    addHistory(response, elapsedTime) {
      // 정규 표현식을 사용하여 {{key}} 찾기
      const matches = this.url.match(/{{\s*(\w+)\s*}}/);
      let matchUrl = "";

      if (matches) {
        const key = matches[1]; // 환경 변수 키 추출
        // 환경 변수 객체에서 해당 키의 값을 찾아 반환
        matchUrl = this.environmentVariables[key];
      } else {
        matchUrl = this.url;
      }

      const newLog = {
        projectId: this.selectedProject.id,
        environmentId: this.propSelectedEnvironment?.id || null,
        siteId: this.propSelectedSite?.id || null,
        method: this.method,
        url: matchUrl,
        responseCode: response.statusCode,
        responseMessage: response.body,
        responseTime: elapsedTime,
        loggedTime: new Date().toISOString().replace("T", " ").split(".")[0],
        header: JSON.stringify(this.headers),
        parameter: JSON.stringify(this.queryParameters),
        formParamter: JSON.stringify(this.formParameters),
        responseBody: JSON.stringify(response.data, null, 2),
        responseHeader: JSON.stringify(response.headers, null, 2),
      };

      // 서버에 기록 저장 API 호출
      this.$axios.post("/api/history", newLog);
    },
  },
  mounted() {
    this.fetchLoginUserInfo();
    this.saveInitialState(); // 초기 상태 저장
    this.isInitializing = false; // 초기화 완료 플래그 설정
    this.selectedSite = this.propSelectedSite;
    this.selectedEnvironment = this.propSelectedEnvironment;
    this.fetchEnvironmentVariables();
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
    apiData() {
      return {
        id: this.apiId,
        name: this.apiName,
        method: this.method,
        url: this.url,
        headers: JSON.stringify(this.headers),
        queryParameters: JSON.stringify(this.queryParameters),
        formParameters: JSON.stringify(this.formParameters),
        file: this.file,
        selectedBodyType: this.selectedBodyType,
        selectedEnvironment: this.selectedEnvironment,
      };
    },
  },
  watch: {
    apiName: {
      handler() {
        this.markChanges();
        console.log("apiName: " + this.hasChanges);
      },
    },
    method: {
      handler() {
        this.markChanges();
        console.log("method: " + this.hasChanges);
      },
    },
    selectedBodyType: {
      handler() {
        this.markChanges();
        console.log("selectedBodyType: " + this.hasChanges);
      },
    },
    selectedEnvironment: {
      handler() {
        this.markChanges();
        console.log("selectedEnvironment: " + this.hasChanges);
      },
    },
    headers: {
      deep: true, // 배열/객체 내부 변경 감지
      handler: "markChanges",
    },
    queryParameters: {
      deep: true,
      handler: "markChanges",
    },
    formParameters: {
      deep: true,
      handler: "markChanges",
    },
    file: {
      deep: true,
      handler: "markChanges",
    },
    isSetting: {
      handler() {
        this.fetchLoginUserInfo();
      },
    },
    // URL이 직접 변경될 경우 Query Parameters 업데이트
    url: {
      handler(newUrl) {
        // triggerAutoSave 호출
        console.log("url watch");
        this.markChanges();

        // Query Parameters 업데이트
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
    },
    selectedProject: {
      handler(newProject, oldProject) {
        if (newProject && newProject !== oldProject) {
          this.resetInputs();
        }
      },
      immediate: false,
    },
    // propSelectedEnvironment 값이 변경되었을 때 selectedEnvironment를 업데이트
    propSelectedEnvironment: {
      immediate: true, // 초기에도 실행
      handler(newValue) {
        this.selectedEnvironment = newValue;
        this.fetchEnvironmentVariables(); // 변경된 환경 변수 로드
      },
    },
    propEnvironments: {
      immediate: true, // environments 변경 시 실행
      handler(newValue) {
        if (newValue.length > 0 && !this.selectedEnvironment) {
          this.selectedEnvironment = newValue[0].id;
          this.fetchEnvironmentVariables();
        }
      },
    },
    propSites: {
      immediate: true, // sites 변경 시 실행
      handler(newValue) {
        if (newValue.length > 0 && !this.selectedSite) {
          this.selectedSite = newValue[0].id;
        }
      },
    },
    propFileConflict: {
      handler(newValue) {
        this.fileConflict = newValue;
      },
    },
    tempApi: {
      immediate: true,
      async handler(newTempApi) {
        this.isInitializing = true; // 초기화 시작
        if (newTempApi) {
          this.apiId = newTempApi.id;
          this.itemId = newTempApi.itemId;
          this.apiPath = await this.buildApiPath(newTempApi.itemId); // 경로 설정
          this.apiName = newTempApi.name || "";
          this.method = newTempApi.method || "GET";
          this.url = newTempApi.url || "";
          this.headers = JSON.parse(newTempApi.headers) || [
            { key: "", value: "" },
          ];
          this.queryParameters = JSON.parse(newTempApi.queryParameters) || [
            { key: "", value: "" },
          ];
          this.formParameters = JSON.parse(newTempApi.formParameters) || [
            { key: "", type: "text", value: "" },
          ];
          this.selectedBodyType = newTempApi.selectedBodyType || "text";
          this.response = newTempApi.response || {
            statusCode: null,
            statusMessage: "",
            headers: {},
            body: "",
          };
          this.saveInitialState(); // 초기 상태 저장
        }
        this.isInitializing = false;
      },
    },
    updatedTempApi: {
      async handler(newTempApi) {
        if (newTempApi) {
          this.apiId = newTempApi.id;
          this.itemId = newTempApi.itemId;
          this.apiPath = await this.buildApiPath(newTempApi.itemId); // 경로 설정
          this.apiName = newTempApi.name || "";
          this.method = newTempApi.method || "GET";
          this.url = newTempApi.url || "";
          this.headers = JSON.parse(newTempApi.headers) || [
            { key: "", value: "" },
          ];
          this.queryParameters = JSON.parse(newTempApi.queryParameters) || [
            { key: "", value: "" },
          ];
          this.formParameters = JSON.parse(newTempApi.formParameters) || [
            { key: "", type: "text", value: "" },
          ];
          this.selectedBodyType = newTempApi.selectedBodyType || "text";
        }
      },
    },
    "response.statusCode": {
      handler(statusCode) {
        if (statusCode === null || statusCode === undefined) {
          this.statusColor = "#6c757d"; // 기본 색상 (회색)
        } else if (statusCode >= 100 && statusCode < 200)
          this.statusColor = "#F2F1F0"; // 1xx
        else if (statusCode >= 200 && statusCode < 300)
          this.statusColor = "#80BFB4"; // 2xx
        else if (statusCode >= 300 && statusCode < 400)
          this.statusColor = "#260124"; // 3xx
        else if (statusCode >= 400 && statusCode < 500)
          this.statusColor = "#D9A3D0"; // 4xx
        else if (statusCode >= 500 && statusCode < 600)
          this.statusColor = "#F2B366"; // 5xx
        else this.statusColor = "#6c757d"; // 그 외
      },
      immediate: true, // 컴포넌트가 마운트될 때 즉시 실행
    },
  },
  beforeUnmount() {
    if (this.autoSaveTimer) {
      clearInterval(this.autoSaveTimer);
    }
    if (this.autoSaveDelayTimer) {
      clearTimeout(this.autoSaveDelayTimer);
    }
  },
};
</script>

<style scoped>
.api-tester {
  width: 95%;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.change-warning {
  color: brown;
  font-weight: bold;
}

.request-section,
.response-section {
  overflow-y: auto;
}

.request-section {
  border-bottom: 1px solid #dee2e6;
}

/* ( 크롬, 사파리, 오페라, 엣지 ) 동작 */
.request-section:-webkit-scrollbar,
.response-section::-webkit-scrollbar {
  display: none;
}

.request-section,
.response-section {
  -ms-overflow-style: none; /* 인터넷 익스플로러 */
  scrollbar-width: none; /* 파이어폭스 */
}

.resizer {
  text-align: center;
  height: 6px;
  cursor: pointer; /* 상하 리사이즈 커서 */
}

.input-api-name {
  border-width: 0 0 1px;
  border-radius: 0;
}

.request-add {
  margin-left: 10px;
  color: black;
}

.remove-icon:hover {
  cursor: pointer;
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

.response-body {
  height: calc(100% - 50px); /* 상태 코드 아래 스크롤 영역 */
  overflow-y: auto;
  background-color: #f1f1f1;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* Response CSS End */

.errorMessage {
  margin-left: 10px;
}

.auto-save-spinner {
  margin-left: 8px; /* 버튼과의 여백 */
}

.spinner-border {
  position: revert;
  width: 25px;
  height: 25px;
  margin-top: 5px;
  visibility: visible;
}

.spinner-container {
  width: 45px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  visibility: hidden;
}
</style>
