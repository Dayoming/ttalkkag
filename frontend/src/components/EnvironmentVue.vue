<template>
    <div class="container mt-4">
      <h5 class="mb-5"><b>Environment Variables</b></h5>
  
      <!-- 상단: 환경 선택과 버튼 -->
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="d-flex align-items-center">
          <select
            v-model="selectedEnvironment"
            class="form-select me-2"
            style="width: auto"
          >
            <option
              v-for="(env, index) in environments"
              :key="index"
              :value="env"
            >
              {{ env }}
            </option>
          </select>
          <button class="btn btn-dark me-2" @click="addEnvironment">
            환경 추가
          </button>
          <button class="btn btn-dark" @click="deleteEnvironment">
            환경 삭제
          </button>
        </div>
      </div>
  
      <!-- 선택된 환경 텍스트와 다운로드/변수 추가 버튼 -->
      <div class="d-flex justify-content-between align-items-center mb-3">
        <p class="text-muted mb-0">
          현재 선택된 환경: <strong>{{ selectedEnvironment }}</strong>
        </p>
        <div>
          <button class="btn btn-dark me-2" @click="downloadJson">다운로드</button>
          <button class="btn btn-dark" @click="addVariable">변수 추가</button>
        </div>
      </div>
  
      <!-- 테이블 -->
      <div class="table-responsive">
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>Key</th>
              <th style="border-right: 0;">Value</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(variable, index) in variables" :key="index">
              <!-- Key -->
              <td>
                <span v-if="!variable.isEditing">{{ variable.key }}</span>
                <input
                  v-else
                  type="text"
                  class="form-control"
                  v-model="variable.key"
                  placeholder="Enter Key"
                />
              </td>
              <!-- Value -->
              <td style="border-right: 0;">
                <span v-if="!variable.isEditing">{{ variable.value }}</span>
                <input
                  v-else
                  type="text"
                  class="form-control"
                  v-model="variable.value"
                  placeholder="Enter Value"
                />
              </td>
              <!-- Actions -->
              <td>
                <button class="btn btn-link text-danger" @click="removeVariable(index)">
                    <i class="bi bi-x-lg"></i>
                </button>
                <button
                  class="btn btn-light"
                  @click="toggleEditMode(index)"
                >
                  <i :class="variable.isEditing ? 'bi bi-check-lg' : 'bi bi-pen-fill'"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "EnvironmentVue",
    data() {
      return {
        environments: ["Development", "Staging", "Production"], // 환경 목록
        selectedEnvironment: "Development", // 현재 선택된 환경
        variables: [
          { key: "API_URL", value: "https://example.com", isEditing: false },
          { key: "API_KEY", value: "123456", isEditing: false },
        ], // 환경 변수 목록
      };
    },
    methods: {
      addEnvironment() {
        const newEnvironment = prompt("새 환경 이름을 입력하세요:");
        if (newEnvironment) {
          this.environments.push(newEnvironment);
          this.selectedEnvironment = newEnvironment;
        }
      },
      deleteEnvironment() {
        if (
          confirm(`현재 환경(${this.selectedEnvironment})을 삭제하시겠습니까?`)
        ) {
          this.environments = this.environments.filter(
            (env) => env !== this.selectedEnvironment
          );
          this.selectedEnvironment = this.environments[0] || "";
        }
      },
      addVariable() {
        this.variables.push({ key: "", value: "", isEditing: true });
      },
      removeVariable(index) {
        this.variables.splice(index, 1);
      },
      toggleEditMode(index) {
        const variable = this.variables[index];
        variable.isEditing = !variable.isEditing;
      },
      downloadJson() {
        const jsonContent = JSON.stringify(
          this.variables.reduce((acc, variable) => {
            acc[variable.key] = variable.value;
            return acc;
          }, {}),
          null,
          2
        );
        const blob = new Blob([jsonContent], { type: "application/json" });
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = `${this.selectedEnvironment}_variables.json`;
        link.click();
        URL.revokeObjectURL(url);
      },
    },
  };
  </script>

<style scoped>
.table {
    text-align: left;
}
.table th,
.table td {
  vertical-align: middle;
  text-align: center;
}
.table td {
    text-align: left;
}

.table input {
  text-align: left;
}

.text-muted {
  margin-bottom: 1rem;
  font-size: 1rem;
}

table th:first-child,
table td:first-child {
  border-left: 0;
  border-right: 1;
}
table th:last-child,
table td:last-child {
  border-right: 0;
  border-left: 0;
  text-align: right;
}
</style>
