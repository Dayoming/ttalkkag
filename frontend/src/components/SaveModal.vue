<template>
  <div>
    <!-- 모달 -->
    <div class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Select Folder</h5>
            <button
              type="button"
              class="btn-close"
              @click="$emit('close')"
            ></button>
          </div>
          <div class="modal-body">
            <ul class="folder-list">
              <SaveModalItem
                v-for="item in localItems"
                :key="item.id"
                :item="item"
                :items="items"
                :apiData="apiData"
                :depth="0"
                :isLoad="isLoad"
                @toggle-folder="toggleFolder"
                @project-saved="handleFolderSelected"
                @update-items="$emit('update-items')"
                @load-api="handleLoadApi"
              />
            </ul>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="addFolder"
            >
              New Folder
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- New Project Modal -->
    <div
      v-if="showNewProjectModal"
      class="modal fade show d-block"
      tabindex="-1"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">새로운 프로젝트명을 입력하세요.</h5>
            <button
              type="button"
              class="btn-close"
              @click="closeNewProjectModal"
            ></button>
          </div>
          <div class="modal-body">
            <input
              type="text"
              v-model="newProjectName"
              class="form-control"
              placeholder="NewProject"
              @keyup.enter="createNewProject"
            />
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-primary"
              @click="createNewProject"
            >
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
import SaveModalItem from "./SaveModalItem.vue";

export default {
  name: "SaveModal",
  props: ["apiData", "projects", "items", "isLoad", "selectedProject"],
  components: { SaveModalItem },
  data() {
    return {
      localProjects: [],
      localItems: [],
      localSelectedProject: "",
      showNewProjectModal: false,
      newProjectName: "",
    };
  },
  watch: {
    selectedProject: {
      handler(newSelectedProject) {
        this.localSelectedProject = newSelectedProject;
        this.fetchItems();
      },
      immediate: true,
    },
    localItems: {
      handler() {
        this.updateKey++;
      },
      deep: true, // items 내부의 객체 변경 감지
    },
    items: {
      handler(newItems) {
        this.localItems = newItems;
      },
    },
  },
  methods: {
    async fetchItems() {
      try {
        const response = await this.$axios.get(
          `/api/projects/${this.localSelectedProject.id}`
        );
        this.localItems = [...this.buildTreeStructure(response.data)];
        this.expandAll(this.localItems);
      } catch (error) {
        console.log("Failed load items: " + error);
      }
    },
    buildTreeStructure(items) {
      const idToItemMap = {};
      items.forEach((item) => {
        idToItemMap[item.id] = {
          ...item,
          children: [],
          isOpen: false,
          isSelected: false,
        };
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
    handleFolderSelected(newApis) {
      this.$emit("project-saved", newApis);
    },
    handleLoadApi(api) {
      this.$emit("load-api", api);
    },
    async toggleFolder(selectedProject) {
      if (!selectedProject) {
        console.error("선택된 프로젝트를 찾을 수 없습니다.");
        return;
      }

      try {
        const response = await this.$axios.get(
          `/api/projects/${selectedProject.id}`
        );
        selectedProject.children = this.buildTreeStructure(response.data); // 최상위 항목만 로드
        selectedProject.isOpen = !selectedProject.isOpen;
      } catch (error) {
        console.error("아이템을 불러오는 중 오류가 발생했습니다:", error);
      }
    },
    closeNewProjectModal() {
      this.showNewProjectModal = false;
    },
    async addFolder() {
      const folderName = prompt("새 폴더 이름을 입력하세요: ");
      if (!folderName) {
        alert("폴더 이름을 입력해주세요.");
        return;
      }

      try {
        await this.$axios.post(`/api/projects/add-folder`, {
          projectId: this.localSelectedProject.id,
          parentId: null,
          name: folderName,
          depth: 1,
        });
        this.$emit("update-items");
      } catch (error) {
        console.error("폴더 추가 중 오류가 발생했습니다:", error);
      }
    },
  },
};
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1040;
}

.folder-list {
  padding: 10px;
}

li {
  list-style: none;
}

li i {
  margin-right: 5px;
  cursor: pointer;
}

.project-list:hover {
  cursor: pointer;
  text-decoration: underline;
}
</style>
