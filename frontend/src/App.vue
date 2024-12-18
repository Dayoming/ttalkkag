<template>
  <template v-if="!$route.meta.noHeaderSidebar">
    <NavHeader @project-selected="handleProjectSelected" />
    <div class="container-fluid">
      <div class="row">
        <CommonSideBar
          :projects="projects"
          :items="items"
          :tempApi="selectedTempApi"
          :selectedProject="selectedProject"
          @select-temp-api="loadTempApi"
          @delete-projects="deleteProjects"
          @update-projects="updateProjects"
          @update-items="fetchItems"
          @api-selected="handleApiSelected"
        />
        <main
          class="col-md-9 ms-sm-auto col-lg-10 px-md-4 main-contents"
          @dragover.prevent
          @drop="handleDropOutside"
        >
          <router-view
            :projects="projects"
            :items="items"
            :tempApi="selectedTempApi"
            :selectedProject="selectedProject"
            @temp-save-api="saveTempApi"
            @delete-projects="deleteProjects"
            @update-projects="updateProjects"
            @update-items="fetchItems"
            @edit-request="handleEditRequest"
            @re-request="handleReRequest"
            @api-selected="handleApiSelected"
          />
          <div
            id="global-spinner"
            class="spinner-backdrop"
            style="display: none"
          >
            <div class="spinner-border text-dark" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
        </main>
      </div>
    </div>
  </template>
  <template v-else>
    <div class="container-fluid">
      <div class="row">
        <router-view />
      </div>
    </div>
  </template>
</template>

<script>
import NavHeader from "./components/layouts/NavHeader.vue";
import CommonSideBar from "./components/layouts/CommonSideBar.vue";

export default {
  name: "App",
  data() {
    return {
      selectedTempApi: null, // 현재 선택된 API
      selectedProject: null, // 현재 선택된 프로젝트
      projects: [],
      items: [],
    };
  },
  methods: {
    handleProjectSelected(project) {
      this.selectedProject = project;
    },
    handleEditRequest(log) {
      // HistoryVue에서 전달된 로그 데이터를 ApiTestVue로 전달
      this.selectedTempApi = { ...log, isEdit: true };

      // ApiTestVue로 이동
      this.$router.push({ name: "ApiTest" });
    },
    handleReRequest(log) {
      this.selectedTempApi = { ...log, isRequest: true };
      this.$router.push({ name: "ApiTest" });
    },
    async handleDropOutside(event) {
      const draggedItemId = event.dataTransfer.getData("draggedItemId");
      if (!draggedItemId) return;

      try {
        await this.$axios.patch(`/api/projects/update/parentId`, {
          id: Number(draggedItemId),
          parentId: null,
        });
        this.fetchItems(); // 갱신 요청
      } catch (error) {
        console.error("바깥 영역 드롭 중 오류 발생:", error);
        alert("이동 중 오류가 발생했습니다.");
      }
    },
    handleApiSelected(loadApi) {
      this.selectedTempApi = loadApi;
      this.$router.push({
        name: "ApiTest",
        params: { tempApi: this.selectedTempApi }, // 라우터에 데이터 전달
      });
    },
    // API를 임시 저장
    saveTempApi(apiData) {
      const existingApiIndex = this.tempApis.findIndex(
        (api) => api.name === apiData.name
      );
      if (existingApiIndex !== -1) {
        // 동일 이름의 API가 이미 존재하면 업데이트
        this.tempApis[existingApiIndex] = apiData;
      } else {
        // 새 API 데이터 추가
        this.tempApis.push(apiData);
      }
    },
    // 사이드바에서 임시 저장된 API 선택
    loadTempApi(loadTempApi) {
      this.selectedTempApi = loadTempApi;
    },
    async fetchProjects() {
      try {
        const response = await this.$axios.get("/api/projects");
        this.projects = response.data;
        if (this.localProjects.length > 0) {
          this.selectedProject = this.projects[0];
        }
      } catch (error) {
        console.error("Failed to fetch projects:", error);
      }
    },
    updateProjects(newProject) {
      this.projects.push(newProject);
    },
    async fetchItems() {
      try {
        const response = await this.$axios.get(
          `/api/projects/${this.selectedProject.id}`
        );
        this.items = [...this.buildTreeStructure(response.data)];
      } catch (error) {
        console.log("Failed load items: " + error);
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
    deleteProjects(projectId) {
      // projects 배열에서 삭제된 프로젝트 제거
      this.projects = this.projects.filter(
        (project) => project.id !== projectId
      );
    },
  },
  mounted() {
    this.fetchProjects();
    this.fetchItems();
  },
  components: {
    NavHeader,
    CommonSideBar,
  },
};
</script>

<style>
main {
  font-size: small;
}

.main-contents {
  height: 100vh;
}

.spinner-backdrop {
  position: fixed; /* 화면 전체를 덮기 위해 fixed 사용 */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 반투명 배경 */
  display: flex;
  align-items: center; /* 수직 정렬 */
  justify-content: center; /* 수평 정렬 */
  z-index: 9999; /* 최상위 레이어 */
}

.spinner-border {
  display: block;
  position: fixed;
  top: calc(50% - (58px / 2));
  right: calc(50% - (58px / 2));
  width: 5rem;
  height: 5rem;
}
</style>
