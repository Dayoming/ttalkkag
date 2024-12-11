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
          :value="selectedProject"
          @change="selectProject"
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
        <select class="form-select me-2" v-model="selectedMethod" style="width: 230px">
          <option value="">ALL</option>
          <option value="GET">GET</option>
          <option value="POST">POST</option>
          <option value="PUT">PUT</option>
          <option value="DELETE">DELETE</option>
          <option value="PATCH">PATCH</option>
        </select>
        <button class="btn btn-dark" @click="fetchFilteredItems">
          Search
        </button>
      </div>
      <button class="btn btn-dark me-2" @click="addFolder">새 폴더 추가</button>
      <button class="btn btn-dark me-2" @click="addAPI">새 요청 추가</button>
      <button class="btn btn-dark" @click="deleteSelected">선택 삭제</button>
    </div>

    <!-- 테이블 -->
    <div class="table-responsive mt-4">
      <table class="table table-borderless">
        <th>API명/폴더명</th>
        <th>URL</th>
        <th>METHOD</th>
        <tbody>
          <RecursiveFolderItem
            v-for="item in items"
            :key="item.id"
            :item="item"
            :depth="0"
            @selection-change="handleSelectionChange"
            @toggle-folder="toggleFolder"
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
  props: ["projects", "selectedProject"],
  data() {
    return {
      localProjects: [],
      items: [],
      newProjectName: null,
      showNewProjectModal: false,
      localSelectedProject: "",
      searchQuery: "",
      selectedMethod: "",
    };
  },
  methods: {
    async fetchProjects() {
      try {
        const response = await this.$axios.get("/api/projects");
        this.localProjects = response.data;
        if (this.localProjects.length > 0) {
          this.localSelectedProject = this.localProjects[0].name;
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
        this.items = this.buildTreeStructure(response.data); // 최상위 항목만 로드
      } catch (error) {
        console.error("아이템을 불러오는 중 오류가 발생했습니다:", error);
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
              method: this.selectedMethod,
            },
          }
        );
        this.items = this.buildTreeStructure(response.data);
      } catch (error) {
        console.error("검색 중 오류가 발생했습니다:", error);
      }
    },
    buildTreeStructure(items) {
      const idToItemMap = {};
      items.forEach((item) => {
        idToItemMap[item.id] = { ...item, children: [], isOpen: false };
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
    async toggleFolder(item) {
      console.log("폴더 토글됨:", item);
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
    async selectProject() {
      this.$emit("select-project", this.localSelectedProject); // 선택된 프로젝트를 부모로 전달
      await this.fetchItems();
    },
    async addNewProject() {
      if (this.newProjectName) {
        try {
          const response = await this.$axios.post("/api/projects", {
            name: this.newProjectName,
          });
          this.localProjects.push(response.data);
          this.$emit("update-projects", response.data);
          this.selectProject(response.data.name); // 새로운 프로젝트 선택
          this.showNewProjectModal = false;
        } catch (error) {
          console.error("Failed to create project:", error);
        }
      }
      this.selectProject();
    },
    async addFolder() {
      const folderName = prompt("새 폴더 이름을 입력하세요: ");
      if (!folderName) {
        alert("폴더 이름을 입력해주세요.");
        return;
      }

      try {
        await this.$axios.post(`/api/projects/add-folder`, {
          projectId: this.localProjects.find(
            (project) => project.name === this.localSelectedProject
          ).id,
          parentId: null,
          name: folderName,
          depth: 1,
        });
        this.fetchItems();
      } catch (error) {
        console.error("폴더 추가 중 오류가 발생했습니다:", error);
        alert("폴더 추가에 실패했습니다.");
      }
    },
    async deleteProject() {
      if (confirm(`${this.selectedProject}를 삭제하시겠습니까?`)) {
        const selectedProject = this.localProjects.find(
          (project) => project.name === this.selectedProject
        );
        if (selectedProject) {
          try {
            await this.$axios.delete(`/api/projects/${selectedProject.id}`);
            this.localProjects = this.localProjects.filter(
              (project) => project.id !== selectedProject.id
            );

            this.$emit("delete-projects", selectedProject.id);

            if (this.localProjects.length > 0) {
              this.selectProject(this.localProjects[0].name);
            } else {
              this.localSelectedProject = null;
              this.items = [];
            }
          } catch (error) {
            console.error("Failed to delete project:", error);
          }
        }
      }
      this.fetchProjects();
    },
    async deleteSelected() {
      // 선택된 항목 ID 수집
      const selectedIds = this.getSelectedIds(this.items);

      if (selectedIds.length === 0) {
        alert("삭제할 항목을 선택해주세요.");
        return;
      }

      if (!confirm("선택된 항목과 모든 하위 항목을 삭제하시겠습니까?")) {
        return;
      }

      try {
        // 서버에 삭제 요청
        await this.$axios.delete("/api/projects/items", {
          data: selectedIds,
        });

        // UI에서 삭제
        this.removeItemsFromUI(this.items, selectedIds);
        alert("삭제되었습니다.");
      } catch (error) {
        console.error("삭제 중 오류가 발생했습니다:", error);
        alert("삭제에 실패했습니다.");
      }
    },
    getSelectedIds(items) {
      const selectedIds = [];

      for (const item of items) {
        if (item.selected) {
          selectedIds.push(item.id);
        }
        if (item.children && item.children.length > 0) {
          selectedIds.push(...this.getSelectedIds(item.children));
        }
      }

      return selectedIds;
    },
    removeItemsFromUI(items, selectedIds) {
      for (let i = items.length - 1; i >= 0; i--) {
        const item = items[i];

        // 현재 항목 삭제
        if (selectedIds.includes(item.id)) {
          items.splice(i, 1);
          continue;
        }

        // 하위 항목 삭제
        if (item.children && item.children.length > 0) {
          this.removeItemsFromUI(item.children, selectedIds);
        }
      }
    },
    handleSelectionChange(updatedItem) {
      const findAndUpdateItem = (items) => {
        for (let i = 0; i < items.length; i++) {
          if (items[i].id === updatedItem.id) {
            items[i] = updatedItem;
            return;
          }
          if (items[i].children && items[i].children.length > 0) {
            findAndUpdateItem(items[i].children);
          }
        }
      };

      findAndUpdateItem(this.items);
    },
  },
  mounted() {
    this.fetchProjects().then(() => {
      if (this.localProjects.length > 0) {
        this.localSelectedProject = this.localProjects[0].name;
        this.fetchItems();
      }
    });
  },
  watch: {
    // 부모에서 전달받은 projects가 변경되면 localProjects도 동기화
    projects: {
      handler(newProjects) {
        this.localProjects = [...newProjects];
      },
      immediate: true, // 컴포넌트가 처음 로드될 때도 동기화
    },
    // 부모로부터 받은 selectedProject가 변경되면 로컬 데이터 업데이트
    selectedProject: {
      handler(newSelectedProject) {
        this.localSelectedProject = newSelectedProject;
      },
      immediate: true,
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

.table .folder-children {
  padding-left: 20px; /* 하위 항목의 들여쓰기 */
}

.input-search {
  border-width: 0 0 1px;
  border-radius: 0;
}
</style>
