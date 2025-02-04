<template>
  <div>
    <div class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Dataset</h5>
            <button
              type="button"
              class="btn-close"
              @click="$emit('close')"
            ></button>
          </div>
          <div class="modal-body">
            <div>
              <select
                id="datasetSelect"
                class="form-select"
                v-model="selectedDataset"
                @change="fetchDatasetVariables"
              >
                <option
                  v-for="dataset in datasets"
                  :key="dataset.id"
                  :value="dataset"
                >
                  {{ dataset.name }}
                </option>
              </select>
            </div>
            <div v-if="datasetVariables.length > 0" class="mt-3">
              <div
                v-for="variable in datasetVariables"
                :key="variable.key"
                class="form-check"
              >
                <input
                  type="checkbox"
                  class="form-check-input"
                  :id="variable.key"
                  v-model="selectedVariables"
                  :value="variable"
                />
                <label :for="variable.key" class="form-check-label">
                  {{ variable.type }} <b>{{ variable.name }}</b>
                  <i
                    v-if="variable.description"
                    class="bi bi-info-circle tooltip-icon"
                    data-bs-toggle="tooltip"
                    data-placement="right"
                    :title="variable.description"
                  ></i>
                </label>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="$emit('close')"
            >
              Cancel
            </button>
            <button type="button" class="btn btn-dark" @click="applyVariables">
              OK
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show"></div>
  </div>
</template>

<script>
export default {
  props: ["currentTab", "selectedProject"],
  data() {
    return {
      datasets: [],
      selectedDataset: null,
      datasetVariables: [],
      selectedVariables: [],
    };
  },
  methods: {
    async fetchDatasets() {
      try {
        const response = await this.$axios.get(
          `/api/dataset/getAllDatasetsWithVariables/${this.selectedProject.id}`
        );
        this.datasets = response.data;
        if (this.datasets.length > 0) {
          this.selectedDataset = this.datasets[0]; // 첫 번째 요소 자동 선택
          this.fetchDatasetVariables();
        }
      } catch (error) {
        console.error("Failed to fetch datasets:", error);
      }
    },
    async fetchDatasetVariables() {
      if (this.selectedDataset) {
        try {
          const response = await this.$axios.get(
            `/api/dataset/variables/${this.selectedDataset.id}`
          );
          this.datasetVariables = response.data;
        } catch (error) {
          console.error("Failed to fetch dataset variables:", error);
        }
      }
    },
    applyVariables() {
      this.$emit("add-variables", {
        variables: this.selectedVariables,
        currentTab: this.currentTab,
      });
      this.$emit("close");
    },
  },
  mounted() {
    this.fetchDatasets();
  },
};
</script>
<style scoped>
/* 아이콘 기본 스타일 */
.tooltip-icon {
  margin-left: 5px;
  color: #6c757d;
  transition: color 0.2s ease, transform 0.2s ease;
}

/* 마우스를 올렸을 때 효과 */
.tooltip-icon:hover {
  color: #000;
  transform: scale(1.2);
}
</style>
