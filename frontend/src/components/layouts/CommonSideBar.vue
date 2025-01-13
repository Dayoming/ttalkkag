<template>
  <div
    class="sidebar border border-right col-md-3 col-lg-2 p-0"
    @dragover.prevent
  >
    <div
      class="offcanvas-md offcanvas-end d-flex flex-column"
      tabindex="-1"
      id="sidebarMenu"
      aria-labelledby="sidebarMenuLabel"
      @drop="handleDropOutside"
    >
      <div class="offcanvas-header">
        <h5 class="offcanvas-title" id="sidebarMenuLabel">Company name</h5>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="offcanvas"
          data-bs-target="#sidebarMenu"
          aria-label="Close"
        ></button>
        <div class="table-responsive mt-4"></div>
      </div>
      <div class="sidebar-hierarchy">
        <!-- 모든 폴더 열기/닫기 버튼 -->
        <div class="d-flex justify-content-end mb-2">
          <button
            class="btn btn-dark mt-2 me-2 folder-toggle-btn"
            @click="toggleAllFolders(true)"
          >
            <i class="bi bi-folder2-open"></i>
          </button>
          <button
            class="btn btn-dark mt-2 folder-toggle-btn"
            @click="toggleAllFolders(false)"
          >
            <i class="bi bi-folder"></i>
          </button>
        </div>
        <div class="table-responsive mt-2">
          <table class="table table-borderless">
            <tbody>
              <RecursiveFolderItem
                v-for="item in localItems"
                :key="item.id + '_' + updateKey"
                :item="item"
                :depth="0"
                :saved-item-id="savedItemId"
                :selected-file-id="selectedFileId"
                :projectAuth="projectAuth"
                :apiSelections="apiSelections"
                @selection-change="handleSelectionChange"
                @toggle-folder="toggleFolder"
                @update-items="$emit('update-items')"
                @api-selected="handleApiSelected"
              />
            </tbody>
          </table>
        </div>
      </div>
      <!-- 하단 고정된 메뉴 -->
      <div class="border-top mt-auto fix-menu">
        <ul class="nav flex-column">
          <li class="nav-item">
            <router-link
              to="/test-api"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-arrow-through-heart-fill"></i>
              API Test
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/environment"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-globe-asia-australia"></i>
              Environment
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/dataset"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-egg"></i>
              Dataset
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/history"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-activity"></i>
              History
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/reports"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-bar-chart-line"></i>
              Reports
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </div>
  <!-- Offcanvas 메뉴 (모바일에서만 표시) -->
  <div
    class="offcanvas offcanvas-start"
    tabindex="-1"
    id="offcanvasSidebar"
    aria-labelledby="offcanvasSidebarLabel"
  >
    <div class="offcanvas-header">
      <h5 class="offcanvas-title" id="offcanvasSidebarLabel"></h5>
      <button
        type="button"
        class="btn-close"
        data-bs-dismiss="offcanvas"
        aria-label="Close"
      ></button>
    </div>
    <div class="sidebar-hierarchy">
      <div class="table-responsive mt-4">
        <table class="table table-borderless">
          <tbody>
            <RecursiveFolderItem
              v-for="item in localItems"
              :key="item.id + '_' + updateKey"
              :item="item"
              :depth="0"
              :savedItemId="savedItemId"
              :selected-file-id="selectedFileId"
              :projectAuth="projectAuth"
              :apiSelections="apiSelections"
              @selection-change="handleSelectionChange"
              @update-items="$emit('update-items')"
              @toggle-folder="toggleFolder"
              @api-selected="handleApiSelected"
            />
          </tbody>
        </table>
      </div>
    </div>
    <div class="offcanvas-body mobile-body d-flex flex-column vh-100">
      <div class="mt-auto border-top pt-3">
        <ul class="nav flex-column">
          <li class="nav-item">
            <router-link
              to="/test-api"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-arrow-through-heart-fill"></i>
              API Test
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/environment"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-globe-asia-australia"></i>
              Environment
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/dataset"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-egg"></i>
              Dataset
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/history"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-activity"></i>
              History
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/reports"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-bar-chart-line"></i>
              Reports
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/projects"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i class="bi bi-archive-fill"></i>
              Projects
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import RecursiveFolderItem from "../RecursiveFolderItem.vue";

export default {
  name: "CommonSideBar",
  components: { RecursiveFolderItem },
  props: {
    projectOwner: String,
    projectAuth: String,
    selectedProject: Object,
    items: Array,
    savedItemId: Number,
    selectedItem: Object,
    apiSelections: Object,
  },
  data() {
    return {
      localProjects: [],
      localSelectedProject: "",
      localItems: [],
      updateKey: 0, // 화면 강제 갱신을 위한 키
      selectedFileId: null, // 선택된 파일 ID
    };
  },
  computed: {
    currentRoute() {
      return this.$route.path;
    },
  },
  watch: {
    // Nav에서 선택한 프로젝트가 변경되면 사이드바 폴더 구조 변경
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
    selectedItem: {
      handler() {
        this.handleSelectionChange(this.selectedItem);
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
    toggleAllFolders(open) {
      const toggleRecursive = (items) => {
        items.forEach((item) => {
          if (item.type === "folder") {
            item.isOpen = open;
            if (item.children.length > 0) {
              toggleRecursive(item.children);
            }
          }
        });
      };
      toggleRecursive(this.localItems);
      this.updateKey++; // 화면 갱신
    },
    handleApiSelected(selectedTempApi) {
      this.$emit("api-selected", selectedTempApi);
    },
    handleSelectionChange(selectedItem) {
      if (selectedItem) {
        // 선택된 파일 ID 업데이트
        if (selectedItem.type === "api") {
          this.selectedFileId = selectedItem.id;
          return;
        }
      } else {
        this.selectedFileId = null;
        return;
      }
    },
    async handleDropOutside(event) {
      const draggedItemId = event.dataTransfer.getData("draggedItemId");
      if (!draggedItemId) return;
      if (this.projectAuth === "read") {
        console.log("Side");
        alert("폴더나 파일 이동 권한이 없습니다.");
        return;
      }
      try {
        await this.$axios.patch(`/api/projects/update/parentId`, {
          id: Number(draggedItemId),
          parentId: null,
        });
        this.$emit("update-items");
        this.fetchItems(); // 갱신 요청
      } catch (error) {
        console.error("바깥 영역 드롭 중 오류 발생:", error);
        alert("이동 중 오류가 발생했습니다.");
      }
    },
  },
  mounted() {
    this.fetchItems();
  },
  beforeUnmount() {
    this.selectedFileId = null;
  },
};
</script>

<style scoped>
.sidebar {
  position: relative;
  height: 100vh;
}

.nav-item a {
  color: black;
  padding: 10px;
  padding-left: 15px;
  font-size: small;
}

.nav-item a:hover {
  background-color: #f2f2f2;
  font-size: small;
}

.nav-item .active {
  background-color: #f2f2f2;
  font-weight: bold;
}

.border-top {
  margin-top: 10px;
  padding-top: 10px;
  padding-bottom: 10px;
}

.mobile-body {
  padding: 0;
}

.sidebar-hierarchy {
  font-size: small;
  padding-left: 20px;
  padding-right: 20px;
}

.folder-toggle-btn {
  font-size: small;
}

.table-responsive {
  overflow: scroll;
  height: 450px;
}

/* ( 크롬, 사파리, 오페라, 엣지 ) 동작 */
.table-responsive:-webkit-scrollbar {
  display: none;
}

.table-responsive {
  -ms-overflow-style: none; /* 인터넷 익스플로러 */
  scrollbar-width: none; /* 파이어폭스 */
}

.fix-menu {
  position: absolute;
  bottom: 0;
  width: 100%;
}
</style>
