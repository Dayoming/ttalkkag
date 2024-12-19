<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mt-4 mb-5">
      <!-- Project Dropdown -->
      <div>
        <label for="projectSelect" class="form-label me-2">Project:</label>
        <select
          id="projectSelect"
          class="form-select d-inline-block w-auto"
          v-model="localSelectedProject"
          @change="fetchItems"
        >
          <option
            v-for="project in localProjects"
            :key="project.id"
            :value="project.name"
          >
            {{ project.name }}
          </option>
        </select>
      </div>
      <!-- New/Delete Project Buttons -->
      <div>
        <button class="btn btn-dark me-2" @click="showNewProjectModal = true">
          New Project
        </button>
        <button class="btn btn-dark" @click="deleteProject">
          Delete Project
        </button>
      </div>
    </div>

    <CommonModal
      v-if="showNewProjectModal"
      :isVisible="showNewProjectModal"
      title="새로운 프로젝트명을 입력하세요."
      confirmText="OK"
      @confirm="addNewProject"
      @close="showNewProjectModal = false"
    >
      <template #body>
        <input
          type="text"
          class="form-control"
          v-model="newProjectName"
          placeholder="프로젝트명을 입력하세요."
        />
      </template>
    </CommonModal>

    <!-- Request Actions -->
    <div class="mt-4">
      <div class="d-flex align-items-center mb-4 w-75">
        <i class="bi bi-search"></i>
        <input
          type="text"
          class="form-control me-2 input-search"
          v-model="searchQuery"
        />
        <select
          class="form-select me-2"
          v-model="selectedType"
          style="width: 230px"
        >
          <option value="">ALL</option>
          <option value="folder">Folder</option>
          <option value="api">API</option>
        </select>
        <select
          v-show="this.selectedType === 'api'"
          class="form-select me-2"
          v-model="selectedMethod"
          style="width: 230px"
        >
          <option value="">ALL</option>
          <option value="GET">GET</option>
          <option value="POST">POST</option>
          <option value="PUT">PUT</option>
          <option value="DELETE">DELETE</option>
          <option value="PATCH">PATCH</option>
        </select>
        <button class="btn btn-dark" @click="fetchFilteredItems">Search</button>
      </div>
      <button class="btn btn-dark me-2" @click="addFolder">새 폴더 추가</button>
      <button class="btn btn-dark me-2" @click="this.$router.push('/test-api')">
        새 요청 추가
      </button>
    </div>

    <!-- 테이블 -->
    <div class="table-responsive mt-4">
      <table class="table table-borderless">
        <th>API명/폴더명</th>
        <th>URL</th>
        <th>METHOD</th>
        <tbody>
          <RecursiveFolderItem
            v-for="item in localItems"
            :key="item.id + '_' + updateKey"
            :item="item"
            :depth="0"
            @selection-change="handleSelectionChange"
            @toggle-folder="toggleFolder"
            @update-items="$emit('update-items')"
            @api-selected="handleApiSelected"
          />
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import CommonModal from "./layouts/CommonModal.vue";
import RecursiveFolderItem from "./RecursiveFolderItem.vue";

export default {
  name: "ProjectsVue",
  components: {
    CommonModal,
    RecursiveFolderItem,
  },
  props: ["projects", "selectedProject", "items"],
  data() {
    return {
      localProjects: [],
      localItems: [],
      localApi: [],
      updateKey: 0,
      newProjectName: null,
      showNewProjectModal: false,
      localSelectedProject: "",
      searchQuery: "",
      selectedMethod: "",
      selectedType: "",
    };
  },
  methods: {
    async fetchProjects() {
      try {
        const response = await this.$axios.get("/api/projects");
        this.localProjects = response.data;

        // 첫 번째 프로젝트 자동 선택
        if (this.localProjects.length > 0) {
          this.localSelectedProject = this.localProjects[0].name;
          this.fetchItems(); // 첫 번째 프로젝트의 아이템 불러오기
        }
      } catch (error) {
        console.error("Failed to fetch projects:", error);
      }
    },
    async fetchItems() {
      const selectedProject = this.localProjects.find(
        (project) => project.name === this.localSelectedProject
      );

      if (!selectedProject) {
        console.error("선택된 프로젝트를 찾을 수 없습니다.");
        return;
      }

      try {
        const response = await this.$axios.get(
          `/api/projects/${selectedProject.id}`
        );
        this.localItems = [...this.buildTreeStructure(response.data)]; // 최상위 항목만 로드
      } catch (error) {
        console.error("아이템을 불러오는 중 오류가 발생했습니다:", error);
      }
    },
    async fetchApi(itemId) {
      try {
        const apiResponse = await this.$axios.get(`/api/apis/${itemId}`);
        this.localApi = apiResponse.data.api;
        return this.localApi;
      } catch (error) {
        console.log("Failed Api Response Fetch: " + error);
      }
    },
    async fetchFilteredItems() {
      const selectedProject = this.localProjects.find(
        (project) => project.name === this.localSelectedProject
      );

      if (!selectedProject) {
        console.error("선택된 프로젝트를 찾을 수 없습니다.");
        return;
      }

      try {
        const response = await this.$axios.get(
          `/api/projects/search/${selectedProject.id}`,
          {
            params: {
              query: this.searchQuery,
              type: this.selectedType,
              method:
                this.selectedMethod === "api" ? this.selectedMethod : null,
            },
          }
        );

        console.log(response.data);
        this.localItems = this.buildTreeWithParents(response.data);
        this.expandAll(this.localItems);
      } catch (error) {
        console.error("검색 중 오류가 발생했습니다:", error);
      }
    },
    buildTreeWithParents(items) {
      const itemMap = {};

      // 모든 항목을 맵에 저장
      items.forEach((item) => {
        item.children = []; // 자식 초기화
        itemMap[item.id] = item;
      });

      const tree = [];

      items.forEach((item) => {
        if (item.parentId === null) {
          // 루트 노드
          tree.push(item);
        } else if (itemMap[item.parentId]) {
          // 부모가 존재하면 연결
          const parent = itemMap[item.parentId];
          if (!parent.children.some((child) => child.id === item.id)) {
            parent.children.push(item);
          }
        }
      });

      return tree; // 트리 반환
    },

    buildTreeStructure(items) {
      const idToItemMap = {};
      items.forEach((item) => {
        try {
          if (item.type === "api") {
            this.fetchApi(item.id);
            idToItemMap[item.id] = {
              ...item,
              children: [],
              apiUrl: "",
              apiMethod: "",
              isOpen: false,
              isProjectsVue: true,
            };
          } else {
            idToItemMap[item.id] = {
              ...item,
              children: [],
              isOpen: false,
              isProjectsVue: true,
            };
          }
        } catch (error) {
          console.log(error);
        }
      });

      const tree = [];
      items.forEach((item) => {
        if (item.parentId === null) {
          // 최상위 항목은 트리에 추가
          tree.push(idToItemMap[item.id]);
        } else if (idToItemMap[item.parentId]) {
          // 부모가 있는 항목은 해당 부모의 children에 추가
          idToItemMap[item.parentId].children.push(idToItemMap[item.id]);
        }
      });
      return tree; // 최종 트리 반환
    },

    expandAll(items) {
      items.forEach((item) => {
        item.isOpen = true;
        if (item.children.length > 0) {
          this.expandAll(item.children);
        }
      });
    },

    findFolderById(folderId, items) {
      for (const item of items) {
        if (item.id === folderId) {
          return item;
        }
        if (item.children && item.children.length > 0) {
          const found = this.findFolderById(folderId, item.children);
          if (found) {
            return found;
          }
        }
      }
      return null;
    },
    async addNewProject() {
      if (this.newProjectName) {
        try {
          const response = await this.$axios.post("/api/projects", {
            name: this.newProjectName,
          });
          this.$emit("update-projects", response.data);
          this.localSelectedProject = this.localProjects[0].name;
          this.showNewProjectModal = false;
          this.fetchProjects();
        } catch (error) {
          console.error("Failed to create project:", error);
        }
      }
    },
    async addFolder() {
      const folderName = prompt("새 폴더 이름을 입력하세요: ");
      if (!folderName) {
        alert("폴더 이름을 입력해주세요.");
        return;
      }

      const selectedProject = this.localProjects.find(
        (project) => project.name === this.localSelectedProject
      );

      try {
        await this.$axios.post(`/api/projects/add-folder`, {
          projectId: selectedProject.id,
          parentId: null,
          name: folderName,
          depth: 1,
        });
        this.$emit("update-items");
        this.fetchItems();
      } catch (error) {
        console.error("폴더 추가 중 오류가 발생했습니다:", error);
      }
    },
    async deleteProject() {
      if (this.selectedProject.name === this.localSelectedProject) {
        alert(
          "현재 사용하고 있는 프로젝트는 삭제할 수 없습니다. 사용 중인 프로젝트를 변경한 후 다시 시도해 주세요."
        );
        return;
      }

      if (this.localProjects.length === 1) {
        alert("프로젝트는 1개 이상 소유해야 합니다.");
        return;
      }

      if (confirm(`${this.localSelectedProject}을(를) 삭제하시겠습니까?`)) {
        const selectedProject = this.localProjects.find(
          (project) => project.name === this.localSelectedProject
        );
        if (selectedProject) {
          try {
            await this.$axios.delete(`/api/projects/${selectedProject.id}`);
            this.localProjects = this.localProjects.filter(
              (project) => project.id !== selectedProject.id
            );

            this.$emit("delete-projects", selectedProject.id);
            this.fetchProjects();
          } catch (error) {
            console.error("Failed to delete project:", error);
          }
        }
      }
    },
    handleSelectionChange(selectedItem) {
      if (selectedItem.type === "api") {
        this.selectedFileId = selectedItem.id;
      }
    },
    handleApiSelected(selectedTempApi) {
      this.$emit("api-selected", selectedTempApi);
    },
  },
  mounted() {
    this.fetchProjects();
  },
  watch: {
    // 부모에서 전달받은 projects가 변경되면 localProjects도 동기화
    projects: {
      handler(newProjects) {
        this.localProjects = [...newProjects];
      },
      immediate: true, // 컴포넌트가 처음 로드될 때도 동기화
    },
    localItems: {
      handler() {
        this.updateKey++;
      },
      deep: true, // items 내부의 객체 변경 감지
    },
    items: {
      handler(newItems) {
        console.log(newItems);
        this.localItems = newItems;
        this.fetchItems();
      },
    },
  },
};
</script>

<style scoped>
.input-search-method {
  border-width: 0 0 1px;
  border-radius: 0;
}

/* 테이블 기본 스타일 */
.table {
  margin-top: 1rem;
}

.table td {
  vertical-align: middle;
}

.bi-folder-fill {
  color: #6c757d;
}

.bi-file-earmark-text {
  color: #007bff;
}

.folder-toggle {
  float: inline-end;
  cursor: pointer;
}

.table td {
  vertical-align: middle;
}

.table-responsive {
  padding: 20px;
}

.table .folder-children {
  padding-left: 20px; /* 하위 항목의 들여쓰기 */
}

.input-search {
  border-width: 0 0 1px;
  border-radius: 0;
}
</style>
