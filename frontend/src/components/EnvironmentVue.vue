<template>
  <div class="container mt-4">
    <h5 class="mb-5"><b>Environment Variables</b></h5>

    <!-- 상단: 환경 선택과 버튼 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <select
          class="form-select d-inline-block w-auto me-2"
          v-model="selectedSite"
          @change="fetchEnvironments"
        >
          <option disabled value="" v-if="sites.length === 0">
            사이트를 만들어 주세요.
          </option>
          <option v-for="site in sites" :key="site.id" :value="site">
            {{ site.name }}
          </option>
        </select>
        <button class="btn btn-dark me-2" @click="showSiteModal">
          사이트 추가
        </button>
        <button class="btn btn-dark" @click="deleteSite">사이트 삭제</button>
      </div>
    </div>

    <!-- 선택된 환경 영역 -->
    <div class="container environment-area">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
          <select
            class="form-select d-inline-block w-auto me-2"
            v-model="selectedEnvironment"
          >
            <option disabled value="" v-if="environments.length === 0">
              환경을 만들어 주세요.
            </option>
            <option
              v-for="environment in environments"
              :key="environment.id"
              :value="environment"
            >
              {{ environment.name }}
            </option>
          </select>
          <button class="btn btn-dark me-2" @click="showAddEnvironmentModal">
            환경 추가
          </button>
          <button class="btn btn-dark me-2" @click="deleteEnvironment">
            환경 삭제
          </button>
        </div>
        <div class="ms-auto">
          <!-- ms-auto를 사용하면 오른쪽 끝으로 이동 -->
          <button
            class="btn btn-dark me-2"
            @click="downloadJson"
            :disabled="this.environments.length === 0"
          >
            다운로드
          </button>
          <button
            class="btn btn-dark"
            @click="addVariable"
            :disabled="this.environments.length === 0"
          >
            변수 추가
          </button>
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
                  @keyup.enter="toggleEditMode(variable.id)"
                />
              </td>
              <!-- Actions -->
              <td>
                <button
                  class="btn btn-link text-danger"
                  @click="openDeleteVariableModal(variable.id, variable.key)"
                >
                  <i class="bi bi-x-lg"></i>
                </button>
                <button
                  class="btn btn-light"
                  @click="toggleEditMode(variable.id)"
                >
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
  </div>

  <!-- 환경 추가 모달 -->
  <div v-if="showAddEnvironment" class="modal fade show d-block" tabindex="-1">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">새로운 환경명을 입력하세요.</h5>
          <button
            type="button"
            class="btn-close"
            @click="closeAddEnvironmentModal"
          ></button>
        </div>
        <div class="modal-body">
          <input
            v-model="newEnvironmentName"
            type="text"
            class="form-control"
            placeholder="환경명을 입력하세요."
            @keyup.enter="addEnvironment"
          />
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeAddEnvironmentModal">
            취소
          </button>
          <button class="btn btn-primary" @click="addEnvironment">확인</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 사이트 추가 모달 -->
  <div v-if="showAddSite" class="modal fade show d-block" tabindex="-1">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">새로운 사이트명을 입력하세요.</h5>
          <button
            type="button"
            class="btn-close"
            @click="closeSiteModal"
          ></button>
        </div>
        <div class="modal-body">
          <input
            v-model="newSiteName"
            type="text"
            class="form-control"
            placeholder="사이트명을 입력하세요."
            @keyup.enter="addSite"
          />
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeSiteModal">
            취소
          </button>
          <button class="btn btn-primary" @click="addSite">확인</button>
        </div>
      </div>
    </div>
  </div>
  <!-- 삭제 옵션 선택 Modal -->
  <div
    v-if="showDeleteEnvironment"
    class="modal fade show d-block" tabindex="-1"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="deleteVariableModalLabel">
            환경 변수 삭제
          </h5>
          <button
            type="button"
            class="btn-close"
            data-bs-dismiss="modal"
            aria-label="Close"
            @click="showDeleteEnvironment = false"
          ></button>
        </div>
        <div class="modal-body">
          <p>환경 변수를 어디에서 삭제하시겠습니까?</p>
          <p>
            <strong>{{ selectedVariableKey }}</strong> 변수를 삭제하시려면 아래
            옵션 중 하나를 선택하세요.
          </p>
        </div>
        <div class="modal-footer">
          <button
            type="button"
            class="btn btn-secondary"
            data-bs-dismiss="modal"
            @click="deleteFromCurrentEnvironment"
          >
            현재 환경에서만 삭제
          </button>
          <button
            type="button"
            class="btn btn-danger"
            data-bs-dismiss="modal"
            @click="deleteFromAllEnvironments"
          >
            전체 환경에서 삭제
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "EnvironmentVue",
  props: {
    selectedProject: Object,
  },
  data() {
    return {
      sites: [],
      selectedSite: "",
      environments: [], // 환경 목록
      selectedEnvironment: "", // 현재 선택된 환경
      selectedEnvironmentName: "",
      selectedVariableId: null,
      selectedVariableKey: null,
      variables: [], // 환경 변수 목록
      showAddSite: false,
      showAddEnvironment: false,
      showDeleteEnvironment: false,
      newEnvironmentName: "",
    };
  },
  methods: {
    showSiteModal() {
      this.showAddSite = true;
      this.newSiteName = "";
    },
    closeSiteModal() {
      this.showAddSite = false;
    },
    showAddEnvironmentModal() {
      this.showAddEnvironment = true; // 모달 표시
      this.newEnvironmentName = ""; // 입력 필드 초기화
    },
    closeAddEnvironmentModal() {
      this.showAddEnvironment = false; // 모달 닫기
    },
    openDeleteVariableModal(variableId, variableKey) {
      this.selectedVariableId = variableId;
      this.selectedVariableKey = variableKey;
      this.showDeleteEnvironment = true;
    },
    async fetchSites() {
      try {
        const response = await this.$axios.get(
          `/api/environments/sites/${this.selectedProject.id}`
        );
        this.sites = response.data; // 사이트 목록 저장
        if (this.sites.length > 0) {
          this.selectedSite = this.sites[0];
        }
        this.fetchEnvironments();
      } catch (error) {
        console.error("사이트 목록을 가져오는 중 오류 발생:", error);
      }
    },
    async fetchEnvironments() {
      try {
        this.selectedEnvironment = "";
        const response = await this.$axios.get(
          `/api/environments/${this.selectedSite.id}`
        );
        this.environments = response.data; // 환경 목록 저장
        if (this.environments.length > 0) {
          this.selectedEnvironment = this.environments[0];
        }
      } catch (error) {
        console.error("Failed to fetch environments:", error);
      }
    },
    async fetchVariables() {
      if (this.selectedEnvironment) {
        const response = await this.$axios.get(
          `/api/environments/variables/${this.selectedEnvironment.id}`
        );
        this.variables = response.data;
      }
    },
    async addEnvironment() {
      // 환경명을 입력하지 않은 경우
      if (!this.newEnvironmentName.trim()) {
        alert("환경명을 입력해주세요.");
        return;
      }

      // 같은 이름의 환경이 이미 있는 경우
      // 클라이언트 측 중복 검사
      const duplicateEnv = this.environments.find(
        (env) => env.name === this.newEnvironmentName.trim()
      );
      if (duplicateEnv) {
        alert("이미 존재하는 환경명입니다.");
        return;
      }

      try {
        const response = await this.$axios.post("/api/environments", {
          siteId: this.selectedSite.id,
          name: this.newEnvironmentName,
        });

        const newEnvironmentId = response.data.id;

        if (this.environments.length > 0) {
          const copyFromEnvironmentId = this.environments[0].id;
          const variableResponse = await this.$axios.get(
            `/api/environments/variables/${copyFromEnvironmentId}`
          );
          const variablesToCopy = variableResponse.data;
          for (const variable of variablesToCopy) {
            await this.$axios.post(`/api/environments/variables`, {
              environmentId: newEnvironmentId,
              key: variable.key,
              value: "",
            });
          }
        }
        this.showAddEnvironment = false; // 모달 닫기
        alert("환경이 성공적으로 추가되었습니다.");
        this.fetchEnvironments(); // 환경 목록 갱신
      } catch (error) {
        console.error("환경 추가 실패", error);
      }
    },
    async addSite() {
      if (!this.newSiteName.trim()) {
        alert("사이트명을 입력해 주세요.");
        return;
      }

      // 클라이언트 측 중복 검사
      const duplicateSite = this.sites.find(
        (site) => site.name === this.newSiteName.trim()
      );
      if (duplicateSite) {
        alert("이미 존재하는 사이트명입니다.");
        return;
      }

      try {
        await this.$axios.post(`/api/environments/site`, {
          projectId: this.selectedProject.id,
          name: this.newSiteName,
        });
        this.closeSiteModal();
        this.fetchSites(); // 사이트 목록 갱신
        alert("사이트가 성공적으로 추가되었습니다.");
      } catch (error) {
        console.error("사이트 추가 중 오류 발생:", error);
        alert("사이트를 추가하는 중 오류가 발생했습니다.");
      }
    },
    async deleteSite() {
      if (!this.selectedSite) return;

      if (
        confirm(
          `${this.selectedSite.name} 사이트를 삭제하시겠습니까? 하위 환경과 변수가 모두 사라집니다.`
        )
      ) {
        try {
          await this.$axios.delete(
            `/api/environments/sites/${this.selectedSite.id}`
          );
          this.fetchSites();
          this.selectedSite = "";
          alert("사이트가 삭제되었습니다.");
        } catch (error) {
          console.error("사이트 삭제 중 오류 발생:", error);
          alert("사이트를 삭제하는 중 오류가 발생했습니다.");
        }
      }
    },
    async deleteEnvironment() {
      if (this.environments.length === 0) {
        alert("현재 선택된 환경이 없습니다.");
        return;
      }
      if (
        confirm(`${this.selectedEnvironment.name} 환경을 삭제하시겠습니까?`)
      ) {
        try {
          await this.$axios.delete(
            `/api/environments/${this.selectedEnvironment.id}`
          );
          this.fetchEnvironments();
        } catch (error) {
          console.error("환경 삭제 실패", error);
        }
      }
    },
    async deleteFromCurrentEnvironment() {
      if (!this.selectedVariableId) {
        alert("변수 ID가 누락되었습니다.");
        return;
      }

      try {
        await this.$axios.delete(
          `/api/environments/variables/${this.selectedVariableId}`
        );
        alert("현재 환경에서 변수 삭제 성공");
        this.fetchVariables(); // 현재 환경 변수 새로고침
      } catch (error) {
        console.error("현재 환경에서 변수 삭제 실패:", error);
        alert("현재 환경에서 변수를 삭제하는 데 실패했습니다.");
      } finally {
        this.showDeleteEnvironment = false;
      }
    },

    async deleteFromAllEnvironments() {
      if (!this.selectedVariableKey) {
        alert("변수 키가 누락되었습니다.");
        return;
      }

      try {
        const environments = this.environments; // 모든 환경 가져오기
        await Promise.all(
          environments.map(async (env) => {
            try {
              // 각 환경에서 동일한 키의 변수를 삭제
              const variablesResponse = await this.$axios.get(
                `/api/environments/variables/${env.id}`
              );
              const matchingVariable = variablesResponse.data.find(
                (variable) => variable.key === this.selectedVariableKey
              );
              if (matchingVariable) {
                await this.$axios.delete(
                  `/api/environments/variables/${matchingVariable.id}`
                );
              }
            } catch (error) {
              console.error(`환경 ${env.name}에서 변수 삭제 실패:`, error);
            }
          })
        );
        alert("전체 환경에서 변수 삭제 성공");
        this.fetchVariables(); // 현재 환경 변수 새로고침
      } catch (error) {
        console.error("전체 환경에서 변수 삭제 실패:", error);
        alert("전체 환경에서 변수를 삭제하는 데 실패했습니다.");
      } finally {
        this.showDeleteEnvironment = false;
      }
    },
    async editVariable(variableId) {
      const variable = this.variables.find((v) => v.id === variableId);
      const newValue = prompt("새로운 값을 입력하세요:", variable.value);
      if (newValue !== null) {
        await this.$axios.put(`/api/environments/variables/${variableId}`, {
          value: newValue,
        });
        this.fetchVariables();
      }
    },
    addVariable() {
      if (this.environments.length === 0) {
        alert("변수를 입력할 환경을 먼저 선택해 주세요.");
        return;
      }
      this.variables.push({
        id: null, // 새 변수는 ID가 없습니다.
        key: "",
        value: "",
        isEditing: true,
      });
    },
    async deleteVariable(variableId, variableKey) {
      if (!variableId || !variableKey) {
        alert("삭제할 변수 정보가 부족합니다.");
        return;
      }

      // 삭제 옵션 선택
      const userChoice = confirm(
        `현재 환경에서만 삭제하려면 '확인'을 누르세요.\n전체 환경에서 삭제하려면 '취소'를 누르고 다시 시도하세요.`
      );

      if (userChoice) {
        // 현재 환경에서만 삭제
        try {
          await this.$axios.delete(`/api/environments/variables/${variableId}`);
          alert("현재 환경에서 변수 삭제 성공");
          this.fetchVariables(); // 현재 환경 변수 새로고침
        } catch (error) {
          console.error("현재 환경에서 변수 삭제 실패:", error);
          alert("현재 환경에서 변수를 삭제하는 데 실패했습니다.");
        }
      } else {
        // 전체 환경에서 삭제
        try {
          const environments = this.environments; // 모든 환경 가져오기
          await Promise.all(
            environments.map(async (env) => {
              console.log(env);
              try {
                // 각 환경에서 동일한 키의 변수를 삭제
                const variablesResponse = await this.$axios.get(
                  `/api/environments/env/${env.id}`
                );
                const matchingVariable = variablesResponse.data.find(
                  (variable) => variable.key === variableKey
                );
                if (matchingVariable) {
                  await this.$axios.delete(
                    `/api/environments/variables/${matchingVariable.id}`
                  );
                }
              } catch (error) {
                console.error(`환경 ${env.name}에서 변수 삭제 실패:`, error);
              }
            })
          );
          alert("전체 환경에서 변수 삭제 성공");
          this.fetchVariables(); // 현재 환경 변수 새로고침
        } catch (error) {
          console.error("전체 환경에서 변수 삭제 실패:", error);
          alert("전체 환경에서 변수를 삭제하는 데 실패했습니다.");
        }
      }
    },
    toggleEditMode(variableId) {
      const variable = this.variables.find((v) => v.id === variableId);

      if (!variable) {
        console.error(`Variable with ID ${variableId} not found.`);
        return;
      }

      if (variable.isEditing) {
        // 키와 값 검증
        if (!variable.key.trim() || !variable.value.trim()) {
          alert("Key와 Value를 모두 입력해주세요.");
          return;
        }

        // 새 변수인지 기존 변수인지 확인
        if (!variable.id) {
          // 새 변수 저장
          this.saveVariable(variable);
        } else {
          // 기존 변수 업데이트
          this.updateVariable(variable);
        }

        // 수정 모드 종료
        variable.isEditing = false;
      } else {
        // 수정 모드로 전환
        variable.isEditing = true;
      }
    },
    async saveVariable(variable) {
      try {
        const response = await this.$axios.post(`/api/environments/variables`, {
          environmentId: this.selectedEnvironment.id,
          key: variable.key,
          value: variable.value,
        });
        variable.id = response.data.id;
        alert("변수가 정상적으로 저장되었습니다.");
      } catch (error) {
        console.error("변수 저장 실패", error);
        alert("변수 저장 중 오류가 발생했습니다.");
      }
    },
    async updateVariable(variable) {
      try {
        await this.$axios.put(`/api/environments/variables/${variable.id}`, {
          key: variable.key,
          value: variable.value,
        });
        alert("변수가 성공적으로 업데이트되었습니다.");
      } catch (error) {
        console.error("변수 업데이트 실패", error);
        alert("변수를 업데이트하는 중 오류가 발생했습니다.");
      }
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
      link.download = `${this.selectedSite.name}_${this.selectedEnvironment.name}.json`;
      link.click();
      URL.revokeObjectURL(url);
    },
  },
  mounted() {
    this.fetchSites();
  },
  watch: {
    selectedProject: {
      handler(newProject) {
        if (newProject) {
          this.fetchSites(); // 프로젝트 변경 시 사이트 목록 새로 로드
        }
      },
      immediate: true, // 컴포넌트 로드시에도 실행
    },
    selectedEnvironment(newEnv) {
      if (newEnv) {
        this.fetchVariables();
      }
    },
  },
  updated() {
    const spinner = document.getElementById("global-spinner");
    if (spinner) spinner.style.display = "none";
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

.environment-area {
  border: 1px solid #dee2e6;
  border-radius: 5px;
  padding: 30px;
}
</style>
