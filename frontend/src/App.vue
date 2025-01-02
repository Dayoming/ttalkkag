<template>
  <template v-if="!$route.meta.noHeaderSidebar">
    <NavHeader
      :propProjects="projects"
      :propSites="sites"
      :propEnvironments="environments"
      :propSelectedProject="selectedProject"
      @logout="handleLogout"
      @project-selected="handleProjectSelected"
      @modal-setting-confirm="handleModalSettingConfirm"
      @update-projects="fetchProjects"
      @selected-environment="handleSelectedEnvironment"
    />
    <div class="container-fluid">
      <div class="row">
        <CommonSideBar
          :projects="projects"
          :items="items"
          :savedItemId="savedItemId"
          :tempApi="selectedTempApi"
          :selectedProject="selectedProject"
          :selectedItem="selectedItem"
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
            :propSites="sites"
            :propEnvironments="environments"
            :tempApi="selectedTempApi"
            :selectedProject="selectedProject"
            :propSelectedEnvironment="selectedEnvironment"
            :propSelectedSite="selectedSite"
            :isSetting="isSetting"
            @temp-save-api="saveTempApi"
            @delete-projects="deleteProjects"
            @update-projects="updateProjects"
            @update-items="fetchItems"
            @update-sites="fetchSites"
            @update-environments="fetchEnvironments"
            @edit-request="handleEditRequest"
            @re-request="handleReRequest"
            @api-selected="handleApiSelected"
            @item-selected="handleItemSelected"
            @refresh-sidebar="handleRefreshSidebar"
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
      selectedItem: null,
      selectedSite: null,
      selectedEnvironment: null,
      selectedFileId: null,
      savedItemId: null,
      projects: [],
      sites: [],
      environments: [],
      items: [],
      isSetting: false,
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
    handleModalSettingConfirm() {
      this.isSetting = !this.isSetting;
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
    handleItemSelected(selectedItem) {
      this.selectedItem = selectedItem;
    },
    async handleRefreshSidebar(itemId) {
      await this.fetchItems();
      // 저장이 되었을 때 해당 폴더에 인터랙션 적용
      if (itemId) {
        this.savedItemId = itemId;
      }
    },
    handleSelectedEnvironment(data) {
      this.selectedEnvironment = data.environmentId;
      this.selectedSite = data.site;
    },
    handleLogout() {
      this.selectedTempApi = null;
      this.selectedProject = null;
      this.selectedSite = null;
      this.selectedEnvironment = null;
      this.savedItemId = null;
      this.projects = [];
      this.sites = [];
      this.environments = [];
      this.items = [];
      this.isSetting = false;
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
    async fetchProjects(selectedProjectId = null) {
      try {
        const response = await this.$axios.get("/api/projects");
        this.projects = response.data;

        // 특정 프로젝트를 선택하거나 기본적으로 첫 번째 프로젝트를 선택
        if (this.projects.length > 0) {
          const selectedProject = selectedProjectId
            ? this.projects.find((project) => project.id === selectedProjectId)
            : this.projects[0];

          if (selectedProject) {
            this.selectedProject = selectedProject;
          }
        }
      } catch (error) {
        console.error("Failed to fetch projects:", error);
      }
    },
    async fetchItems() {
      try {
        const response = await this.$axios.get(
          `/api/projects/${this.selectedProject.id}`,
          { showSpinner: false }
        );
        this.items = [...this.buildTreeStructure(response.data)];
        this.expandAll(this.items);
      } catch (error) {
        console.log("Failed load items: " + error);
      }
    },
    async fetchSites() {
      try {
        const response = await this.$axios.get(
          `/api/environments/sites/${this.selectedProject.id}`
        );
        this.sites = response.data; // 사이트 목록 저장
        this.fetchEnvironments();
        
      } catch (error) {
        console.error("사이트 목록을 가져오는 중 오류 발생:", error);
      }
    },
    async fetchEnvironments() {
      try {
        const response = await this.$axios.get(
          `/api/environments/${this.selectedSite.id}`
        );
        this.environments = response.data; // 환경 목록 저장
      } catch (error) {
        console.error("Failed to fetch environments:", error);
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
    expandAll(items) {
      items.forEach((item) => {
        item.isOpen = true;
        if (item.children.length > 0) {
          this.expandAll(item.children);
        }
      });
    },
    async updateProjects() {
      try {
        const response = await this.$axios.get("/api/projects");
        this.projects = response.data;
      } catch (error) {
        console.error("Failed to fetch projects:", error);
      }
    },
    deleteProjects() {
      // projects 배열에서 삭제된 프로젝트 제거
      this.fetchProjects();
    },
  },
  mounted() {
    this.fetchProjects();
    this.fetchItems();
    this.fetchSites();
    this.fetchEnvironments();
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
