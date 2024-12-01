<template>
  <div class="container mt-4">
    <h5 class="mb-5"><b>Dataset</b></h5>
    <!-- 검색 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <form class="d-flex align-items-center">
        <i class="bi bi-search me-2"></i>
        <input
          type="text"
          class="form-control w-100 me-2 input-search-dataset"
          placeholder="Search"
          v-model="searchQuery"
        />
        <button class="btn btn-dark" @click="searchDatasets">SEARCH</button>
      </form>
      <!-- New, 선택 삭제 버튼 -->
      <div>
        <button class="btn btn-dark me-2" @click="openModal">NEW</button>
        <button
          class="btn btn-dark"
          :disabled="!selectedDatasets.length"
          @click="deleteSelected"
        >
          선택 삭제
        </button>
      </div>
    </div>

    <!-- 테이블 -->
    <div class="table-responsive">
      <table class="table table-bordered">
        <thead>
          <tr>
            <th>
              <input
                type="checkbox"
                @change="selectAll($event)"
                :checked="allSelected"
              />
            </th>
            <th>No</th>
            <th>Dataset Name</th>
            <th>Description</th>
            <th style="width: 15%"></th>
          </tr>
        </thead>
        <tbody>
          <template v-for="(dataset, index) in datasets" :key="dataset.id">
            <!-- 기본 정보 Row -->
            <tr>
              <td>
                <input
                  type="checkbox"
                  :value="dataset.id"
                  v-model="selectedDatasets"
                />
              </td>
              <td>{{ index + 1 }}</td>
              <td>{{ dataset.name }}</td>
              <td>{{ dataset.description }}</td>
              <td class="text-center">
                <button
                  class="btn btn-light"
                  @click="toggleDetails(dataset.id)"
                >
                  <i
                    class="bi"
                    :class="
                      isExpanded(dataset.id)
                        ? 'bi-chevron-up'
                        : 'bi-chevron-down'
                    "
                  ></i>
                </button>
                <button class="btn" @click="editDataset(dataset.id)">
                  <i class="bi bi-pen-fill"></i>
                </button>
                <button class="btn" @click="deleteDataset(dataset.id)">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>

            <!-- 상세 정보 Row -->
            <tr v-if="isExpanded(dataset.id)" class="variable-details">
              <td colspan="5">
                <div class="p-3 border">
                  <ul>
                    <li
                      v-for="variable in dataset.variables"
                      :key="variable.id"
                    >
                      {{ variable.type }} 
                      <b>{{ variable.name }}</b>
                      <p v-if="variable.description"># {{ variable.description }}</p>
                    </li>
                  </ul>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
    <!-- Common Modal -->
    <CommonModal
      v-if="showModal"
      :isVisible="showModal"
      :title="editingDatasetId ? 'Modify Dataset' : 'New Dataset'"
      :confirmText="editingDatasetId ? '수정' : '추가'"
      @confirm="addDataset"
      @close="closeModal"
    >
      <template #body>
        <div class="mb-3">
          <input
            type="text"
            v-model="newDataset.name"
            class="form-control"
            placeholder="Dataset Name"
          />
        </div>

        <div v-for="(variable, index) in newDataset.variables" :key="index">
          <div class="row mb-3">
            <div class="col-md-4">
              <select
                v-model="variable.type"
                class="form-select"
                :class="{ 'is-invalid': variableError && !variable.type }"
              >
                <option value="" disabled>Type</option>
                <option value="String">string</option>
                <option value="Integer">int</option>
                <option value="Float">long</option>
                <option value="Boolean">datetime</option>
                <option value="Boolean">boolean</option>
                <option value="Boolean">float</option>
                <option value="Boolean">double</option>
                <option value="Boolean">byte</option>
                <option value="Boolean">char</option>
                <option value="Boolean">short</option>
              </select>
            </div>
            <div class="col-md-4">
              <input
                type="text"
                v-model="variable.name"
                class="form-control"
                placeholder="Variable name"
                :class="{ 'is-invalid': variableError && !variable.name }"
              />
            </div>
            <div class="col-md-4">
              <input
                type="text"
                v-model="variable.description"
                class="form-control"
                placeholder="Description"
              />
            </div>
          </div>
        </div>
        <p v-if="variableError" class="text-danger">
          변수의 타입과 이름을 적어주세요.
        </p>
        <button class="btn btn-light" @click="addVariable">
          Add Variable...
        </button>
      </template>
    </CommonModal>
  </div>
</template>

<script>
import CommonModal from "./layouts/CommonModal.vue";

export default {
  name: "DatasetVue",
  components: {
    CommonModal,
  },
  data() {
    return {
      datasets: [],
      newDataset: {
        name: "",
        description: "",
        variables: [{ type: "", name: "", description: "" }],
      },
      searchQuery: "", // 검색어
      selectedDatasets: [], // 체크 버튼으로 선택된 데이터셋 ID
      expandedDatasets: [], // 상세 보기가 켜져있는 데이터셋 ID
      showModal: false,
      variableError: false,
      editingDatasetId: null,
    };
  },
  computed: {
    allSelected() {
      return (
        this.selectedDatasets.length > 0 &&
        this.selectedDatasets.length === this.datasets.length
      );
    },
  },
  methods: {
    async fetchDatasets() {
      try {
        const response = await this.$axios.get(
          "/api/dataset/getAllDatasetsWithVariables"
        );
        this.datasets = response.data;
      } catch (error) {
        console.error("Error fetching datasets: ", error);
      }
    },
    openModal() {
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
      this.resetNewDataset();
    },
    addVariable() {
      this.newDataset.variables.push({ type: "", name: "", description: "" });
    },
    resetNewDataset() {
      this.newDataset = {
        name: "",
        description: "",
        variables: [{ type: "", name: "", description: "" }],
      };
      this.editingDatasetId = null; // 수정 상태 초기화
      this.variableError = false;
    },
    async addDataset() {
      // 유효성 검사
      if (
        !this.newDataset.name ||
        this.newDataset.variables.some((v) => !v.type || !v.name)
      ) {
        this.variableError = true;
        return;
      }

      try {
        if (this.editingDatasetId) {
          // 데이터셋 수정
          await this.$axios.put(
            `/api/dataset/update/${this.editingDatasetId}`,
            {
              name: this.newDataset.name,
              description: this.newDataset.description,
              variables: this.newDataset.variables,
            }
          );
          alert("Dataset 수정 완료!");
          this.fetchDatasets();
        } else {
          // 새 데이터셋 추가
          await this.$axios.post("/api/dataset/addDataset", {
            name: this.newDataset.name,
            // description을 추가할 자리
            variables: this.newDataset.variables,
          });
          alert("Dataset 추가 완료!");
        }
        this.fetchDatasets(); // 데이터 갱신
        this.closeModal();
      } catch (error) {
        console.error("Error saving dataset:", error);
      }
    },
    async deleteSelected() {
      if (confirm("선택한 Dataset을 삭제하시겠습니까?")) {
        try {
          // 선택된 데이터셋 ID 배열에 대해 삭제 요청을 보냄
          await Promise.all(
            this.selectedDatasets.map((id) =>
              this.$axios.delete(`/api/dataset/delete/${id}`)
            )
          );
          this.fetchDatasets(); // 데이터 다시 로드
          this.selectedDatasets = []; // 선택된 데이터 초기화
        } catch (error) {
          console.error("Error deleting datasets: ", error);
        }
      }
    },
    toggleDetails(datasetId) {
      const index = this.expandedDatasets.indexOf(datasetId);
      if (index === -1) {
        // 배열에 없으면 추가 (열기)
        this.expandedDatasets.push(datasetId);
      } else {
        // 배열에 있으면 제거 (닫기)
        this.expandedDatasets.splice(index, 1);
      }
    },
    isExpanded(datasetId) {
      // 배열에 포함 여부를 반환
      return this.expandedDatasets.includes(datasetId);
    },
    editDataset(id) {
      const dataset = this.datasets.find((d) => d.id === id);
      if (dataset) {
        this.newDataset = {
          name: dataset.name,
          description: dataset.description,
          variables: dataset.variables.map((v) => ({
            type: v.type,
            name: v.name,
            description: v.description,
          })),
        };
        this.editingDatasetId = id; // 현재 수정 중인 데이터셋 ID를 저장
        this.showModal = true;
      }
    },
    deleteDataset(id) {
      this.$axios.get(`/api/dataset/${id}`).then((response) => {
        if (
          confirm(
            `Dataset ${response.data.dataset.name}을(를) 삭제하시겠습니까?`
          )
        ) {
          this.$axios.delete(`/api/dataset/delete/${id}`).then((response) => {
            this.fetchDatasets();
            alert(response.data.message);
          });
        }
      });
    },
    async searchDatasets(event) {
      event.preventDefault(); // 폼 기본 동작 방지
      try {
        const response = await this.$axios.get(
          `/api/dataset/search?query=${this.searchQuery}`
        );
        this.datasets = response.data;
      } catch (error) {
        console.error("Error searching datasets:", error);
      }
    },
    selectAll(event) {
      this.selectedDatasets = event.target.checked
        ? this.datasets.map((d) => d.id)
        : [];
    },
  },
  mounted() {
    this.fetchDatasets();
  },
};
</script>

<style scoped>
.input-search-dataset {
  border-width: 0 0 1px;
  border-radius: 0;
}

.table th,
.table td {
  vertical-align: middle;
  text-align: center;
}

.table th:first-child,
.table td:first-child {
  width: 5%;
}

.table td:last-child {
  text-align: right;
}

button i {
  font-size: 1.2rem;
}

.is-invalid {
  border-color: red !important;
}

.variable-details {
  background-color: #f9f9f9;
}

.variable-details ul {
  list-style: disc;
  text-align: left;
  margin: 0;
}

.variable-details li {
  margin-bottom: 5px;
}

.variable-details li p {
  display: inline-block;
  margin: 0 0 0 5px;
  color: gray;
}
</style>
