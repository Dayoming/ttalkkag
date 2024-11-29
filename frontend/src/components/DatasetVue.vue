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
        <button class="btn btn-dark me-2" @click="addDataset">NEW</button>
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
          <tr v-for="(dataset, index) in filteredDatasets" :key="dataset.id">
            <!-- 체크박스 -->
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
            <!-- Actions -->
            <td class="text-center">
              <button class="btn btn-light" @click="toggleDetails(index)">
                <i
                  class="bi"
                  :class="
                    detailsIndex === index ? 'bi-chevron-up' : 'bi-chevron-down'
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
          <!-- 상세보기 영역 -->
          <tr v-if="detailsIndex !== null">
            <td colspan="5">
              <div class="p-3 border">
                <h6>Details</h6>
                <p><b>Name:</b> {{ filteredDatasets[detailsIndex].name }}</p>
                <p>
                  <b>Description:</b>
                  {{ filteredDatasets[detailsIndex].description }}
                </p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
export default {
  name: "DatasetVue",
  data() {
    return {
      datasets: [
        { id: 1, name: "Dataset A", description: "Description for Dataset A" },
        { id: 2, name: "Dataset B", description: "Description for Dataset B" },
        { id: 3, name: "Dataset C", description: "Description for Dataset C" },
      ],
      searchQuery: "", // 검색어
      selectedDatasets: [], // 선택된 데이터셋 ID
      detailsIndex: null, // 상세보기 인덱스
    };
  },
  computed: {
    filteredDatasets() {
      if (!this.searchQuery) return this.datasets;
      return this.datasets.filter(
        (dataset) =>
          dataset.name.includes(this.searchQuery) ||
          dataset.description.includes(this.searchQuery)
      );
    },
    allSelected() {
      return (
        this.selectedDatasets.length > 0 &&
        this.selectedDatasets.length === this.datasets.length
      );
    },
  },
  methods: {
    searchDatasets() {
      console.log(`Searching for: ${this.searchQuery}`);
    },
    addDataset() {
      const name = prompt("Enter dataset name:");
      const description = prompt("Enter dataset description:");
      if (name && description) {
        const newId = this.datasets.length + 1;
        this.datasets.push({ id: newId, name, description });
      }
    },
    deleteSelected() {
      if (confirm("Delete selected datasets?")) {
        this.datasets = this.datasets.filter(
          (dataset) => !this.selectedDatasets.includes(dataset.id)
        );
        this.selectedDatasets = [];
      }
    },
    toggleDetails(index) {
      this.detailsIndex = this.detailsIndex === index ? null : index;
    },
    editDataset(id) {
      const dataset = this.datasets.find((d) => d.id === id);
      if (dataset) {
        const newName = prompt("Edit dataset name:", dataset.name);
        const newDescription = prompt(
          "Edit dataset description:",
          dataset.description
        );
        if (newName) dataset.name = newName;
        if (newDescription) dataset.description = newDescription;
      }
    },
    deleteDataset(id) {
      if (confirm("Are you sure you want to delete this dataset?")) {
        this.datasets = this.datasets.filter((dataset) => dataset.id !== id);
      }
    },
    selectAll(event) {
      this.selectedDatasets = event.target.checked
        ? this.datasets.map((d) => d.id)
        : [];
    },
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
</style>
