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
        <select class="form-select me-2" style="width: auto">
          <option selected>Development</option>
          <option value="Production">Production</option>
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
      <select class="form-select me-2" style="width: auto">
        <option selected>GET</option>
        <option>POST</option>
        <option>PUT</option>
        <option>DELETE</option>
        <option>PATCH</option>
      </select>
      <input type="text" class="form-control" placeholder="URL" />
      <button class="btn btn-dark ms-3">Send</button>
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
        <div class="row g-3 align-items-center mb-2">
          <div class="col">
            <input type="text" class="form-control" placeholder="Key" />
          </div>
          <div class="col-auto">=</div>
          <div class="col">
            <input type="text" class="form-control" placeholder="Value" />
          </div>
        </div>
        <button type="button" class="btn btn-dark me-2 mt-4">
          + Add header
        </button>
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
          <div class="row g-3 align-items-center mb-2">
            <div class="col">
              <input type="text" class="form-control" placeholder="Key" />
            </div>
            <div class="col-auto">=</div>
            <div class="col">
              <input type="text" class="form-control" placeholder="Value" />
            </div>
          </div>
          <button type="button" class="btn btn-dark me-2 mt-4">
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
            <pre>{{ response.body }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ApiTest",
  data() {
    return {
      selectedBodyType: "text",
      response: {
        statusCode: null, // 응답 상태 코드
        statusMessage: "",
        headers: {}, // 응답 헤더
        body: "", // 응답 바디
      },
      formParameters: [
        { key: "id", type: "text", value: "" },
        { key: "image", type: "file", value: "" },
      ],
    };
  },
  methods: {
    addFormParameter() {
      this.formParameters.push({ key: "", type: "text", value: "" });
    },
  },
  mounted() {},
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
  margin: 0;
  background: #f8f9fa;
  padding: 1rem;
}

/* Response CSS End */
</style>
