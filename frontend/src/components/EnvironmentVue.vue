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
          <option v-for="env in environments" :key="env.id" :value="env">
            {{ env.name }}
          </option>
        </select>
        <button class="btn btn-dark me-2" @click="showAddEnvironmentModal">
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
        <button class="btn btn-dark me-2" @click="downloadJson">
          다운로드
        </button>
        <button class="btn btn-dark" @click="addVariable">변수 추가</button>
      </div>
    </div>

    <!-- 테이블 -->
    <div class="table-responsive">
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>Key</th>
            <th style="border-right: 0">Value</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="variable in variables" :key="variable.id">
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
            <td style="border-right: 0">
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
              <button
                class="btn btn-link text-danger"
                @click="removeVariable(index)"
              >
                <i class="bi bi-x-lg"></i>
              </button>
              <button class="btn btn-light" @click="toggleEditMode(index)">
                <i
                  :class="
                    variable.isEditing ? 'bi bi-check-lg' : 'bi bi-pen-fill'
                  "
                ></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
  <CommonModal
    v-if="showAddEnvironment"
    :isVisible="showAddEnvironment"
    title="새로운 환경명을 입력하세요."
    @close="closeAddEnvironmentModal"
    @confirm="addEnvironment"
  >
    <template #body>
      <div>
        <input v-model="newEnvironmentName" type="text" class="form-control" />
        <p v-if="!newEnvironmentName" class="text-danger">
          환경명을 입력해주세요.
        </p>
      </div>
    </template>
  </CommonModal>
</template>

<script>
import CommonModal from "./layouts/CommonModal.vue";

export default {
  name: "EnvironmentVue",
  components: {
    CommonModal,
  },
  data() {
    return {
      environments: [], // 환경 목록
      selectedEnvironment: null, // 현재 선택된 환경
      variables: [], // 환경 변수 목록
      showAddEnvironment: false,
      newEnvironmentName: "",
    };
  },
  methods: {
    async fetchEnvironments() {
      const response = await this.$axios.get("/environments");
      this.environments = response.data;
      if (!this.selectedEnvironment) {
        this.selectedEnvironment =
          this.environments.find((env) => env.is_default) ||
          this.environments[0];
      }
    },
    async fetchVariables() {
      if (this.selectedEnvironment) {
        const response = await this.$axios.get(
          `/environments/${this.selectedEnvironment.id}/variables`
        );
        this.variables = response.data;
      }
    },
    async addEnvironment() {
      if (!this.newEnvironmentName) return;

      try {
        await this.$axios.post("/environments", {
          name: this.newEnvironmentName,
        });
        this.showAddEnvironment = false;
        this.fetchEnvironments();
      } catch (error) {
        console.error(error);
      }
    },
    async deleteEnvironment() {
      if (this.selectedEnvironment.is_default) {
        alert("기본 환경은 삭제할 수 없습니다.");
        return;
      }
      if (
        confirm(`${this.selectedEnvironment.name} 환경을 삭제하시겠습니까?`)
      ) {
        try {
          await axios.delete(`/environments/${this.selectedEnvironment.id}`);
          this.fetchEnvironments();
        } catch (error) {
          console.error("환경 삭제 실패", error);
        }
      }
    },
    async editVariable(variableId) {
      const variable = this.variables.find((v) => v.id === variableId);
      const newValue = prompt("새로운 값을 입력하세요:", variable.value);
      if (newValue !== null) {
        await this.$axios.put(`/variables/${variableId}`, { value: newValue });
        this.fetchVariables();
      }
    },
    addVariable() {
      this.variables.push({ key: "", value: "", isEditing: true });
    },
    async deleteVariable(variableId) {
      await this.$axios.delete(`/variables/${variableId}`);
      this.fetchVariables();
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
