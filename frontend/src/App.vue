<template>
  <template v-if="!$route.meta.noHeaderSidebar">
    <NavHeader
      :propProjects="projects"
      :propSites="sites"
      :propEnvironments="environments"
      :propSelectedProject="selectedProject"
      :message="message"
      @logout="handleLogout"
      @project-selected="handleProjectSelected"
      @modal-setting-confirm="handleModalSettingConfirm"
      @update-projects="fetchProjects"
      @selected-environment="handleSelectedEnvironment"
      @new-project-participant="handleNewProjectParticipant"
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
          :projectAuth="projectAuth"
          :projectOwner="projectOwner"
          :apiSelections="apiSelections"
          @select-temp-api="loadTempApi"
          @delete-projects="deleteProjects"
          @update-projects="updateProjects"
          @update-participant-auth="fetchProjectAuthInfo"
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
            :projectAuth="projectAuth"
            :items="items"
            :propSites="sites"
            :propEnvironments="environments"
            :tempApi="selectedTempApi"
            :updatedTempApi="updatedTempApi"
            :selectedProject="selectedProject"
            :propSelectedEnvironment="selectedEnvironment"
            :propSelectedSite="selectedSite"
            :isSetting="isSetting"
            :message="message"
            :apiSelections="apiSelections"
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
import SockJS from "sockjs-client";
import Stomp from "stompjs";
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
      stompClient: null,
      projects: [],
      sites: [],
      environments: [],
      items: [],
      user: null,
      isSetting: false,
      message: "",
      projectAuth: null,
      projectOwner: null,
      currentApiSubscription: null, // 현재 구독 중인 API 경로
      updatedTempApi: null, // 다른 사용자가 수정한 Api 정보
      apiSelections: {}, // 작업 중인 사용자 목록
    };
  },
  watch: {
    selectedProject: {
      handler(newProject, oldProject) {
        if (this.selectedProject === null) {
          return;
        }

        if (oldProject && newProject.id !== oldProject.id) {
          this.disconnect(); // 이전 프로젝트 연결 해제
        }

        this.selectedTempApi = null;

        this.disconnect();
        this.connectWebSocket(this.selectedProject.id);
        this.fetchProjectAuthInfo();
      },
      immediate: true,
    },
    selectedTempApi: {
      handler(newApi) {
        if (!this.stompClient || !this.selectedProject) {
          return; // WebSocket이 연결되지 않았거나 프로젝트가 선택되지 않은 경우
        }

        // 기존 구독 해제
        if (this.currentApiSubscription) {
          this.stompClient.unsubscribe(this.currentApiSubscription);
        }

        // 새로운 구독 설정
        if (newApi && newApi.id) {
          const newSubscription = `/topic/project/${this.selectedProject.id}/api/${newApi.id}`;
          this.currentApiSubscription = newSubscription;

          this.stompClient.subscribe(newSubscription, (message) => {
            this.handleApiMessage(message.body);
          });

          this.updateApiSelections();
        }
      },
      immediate: true, // 컴포넌트 초기화 시 감시
    },
  },
  methods: {
    handleProjectSelected(project) {
      this.selectedProject = project;
      // 프로젝트 권한 정보 가져오기
      this.fetchProjectAuthInfo();
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
      if (this.projectAuth === "read") {
        console.log("App");
        alert("폴더나 파일 이동 권한이 없습니다.");
        return;
      }
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
      this.selectedSite = data.site;
      this.selectedEnvironment = data.environmentId;
    },
    async handleLogout() {
      if (confirm("로그아웃 하시겠습니까?")) {
        try {
          await this.deleteMyApiSelection(
            this.user.id,
            this.selectedProject.id
          ); // 삭제 완료 대기
        } catch (error) {
          console.error("Failed to delete API selection:", error);
        }
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
        this.user = null;
        this.message = "";
        this.projectAuth = null;
        this.projectOwner = null;
        this.apiSelections = {};
        this.disconnect();
        // localStorage 초기화
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("userEmail");

        alert("로그아웃 되었습니다.");

        // 로그인 화면으로 리다이렉트
        this.$router.push("/login");
      }
    },
    handleApiMessage(messageBody) {
      try {
        const apiData = JSON.parse(messageBody); // 메시지 파싱

        // selectedTempApi에 반영
        this.updatedTempApi = {
          ...this.selectedTempApi, // 기존 데이터 유지
          ...apiData, // 새로운 데이터 덮어쓰기
        };
      } catch (error) {
        console.error("Failed to parse API message:", error);
      }
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
    async loadUser() {
      try {
        const response = await this.$axios.get("/api/user/findUserByEmail");
        this.user = response.data.user;
      } catch (error) {
        console.log("load User Id Failed: " + error);
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
    async fetchProjectAuthInfo() {
      try {
        const projectResponse = await this.$axios.get(
          `/api/projects/find/${this.selectedProject.id}`
        );
        const projectOwner = await this.$axios.get(
          `/api/user/findById/${projectResponse.data.userId}`
        );
        this.projectOwner = projectOwner.data.user.email;

        // 프로젝트가 내 소유면 권한은 write
        if (projectResponse.data.userId === this.user.id) {
          this.projectAuth = "write";
        } else {
          const participantResponse = await this.$axios.get(
            `/api/projects/${this.selectedProject.id}/${this.user.id}/participants`
          );
          this.projectAuth = participantResponse.data.permissionLevel;
        }
      } catch (error) {
        console.error("Failed to fetch project Auth Info:", error);
      }
    },
    async fetchApiSelection() {
      try {
        const response = await this.$axios.get(
          `/api/apis/usage/list/${this.selectedProject.id}`,
          { showSpinner: false }
        );

        this.apiSelections = this.groupUsersByApi(response.data);
      } catch (error) {
        console.error("Failed update Api Selection: " + error);
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
    async updateApiSelections() {
      const projectId = this.selectedProject.id;
      const itemId = this.selectedTempApi.itemId;

      try {
        this.$axios.post(`/api/apis/usage/${projectId}/${itemId}`);
        const response = await this.$axios.get(
          `/api/apis/usage/list/${projectId}`,
          { showSpinner: false }
        );

        this.apiSelections = this.groupUsersByApi(response.data);
      } catch (error) {
        console.error("Failed update Api Selection: " + error);
      }
    },
    groupUsersByApi(usageGroup) {
      const groupedUsers = {};

      Object.values(usageGroup).forEach((user) => {
        const itemId = user.apiUsage.itemId;

        if (!groupedUsers[itemId]) {
          groupedUsers[itemId] = [];
        }

        groupedUsers[itemId].push({
          id: user.id,
          email: user.email,
          autoSaveUse: user.autoSaveUse,
          autoSaveTime: user.autoSaveTime,
          autoSaveTerm: user.autoSaveTerm,
          showResponse: user.showResponse,
          verified: user.verified,
        });
      });

      return groupedUsers;
    },
    deleteProjects() {
      // projects 배열에서 삭제된 프로젝트 제거
      this.fetchProjects();
    },
    async deleteMyApiSelection(userId, projectId) {
      try {
        await this.$axios.delete(`/api/apis/usage/out/${projectId}/${userId}`);
      } catch (error) {
        console.log(error);
      }
    },
    connectWebSocket(projectId) {
      if (this.user === null) {
        this.loadUser();
      }
      const socket = new SockJS("http://localhost:8081/ws");
      this.stompClient = Stomp.over(socket);

      this.stompClient.connect({}, (frame) => {
        this.stompClient.subscribe(
          `/topic/project/${projectId}`,
          async (message) => {
            console.log(frame);
            if (message.body === "api save") {
              this.handleRefreshSidebar();
            }

            if (
              message.body === "add folder" ||
              message.body === "delete folder"
            ) {
              this.fetchItems();
            }

            if (message.body === "new participant") {
              // 현재 로그인한 사용자가 자동 저장을 off로 설정했다면 on으로 변경
              if (this.user.autoSaveUse === false) {
                alert(
                  "프로젝트에 초대된 사람이 있습니다. 자동 저장을 ON으로 변경합니다. 자동 저장 간격은 사용자 정보 > Settings 에서 변경할 수 있습니다."
                );
                try {
                  const response = await this.$axios.put("/api/user/setting", {
                    autoSaveUse: true,
                  });
                  console.log(response);
                } catch (error) {
                  console.log("User Setting Failed: " + error);
                }
              }
            }

            if (
              message.body === "update parentId" ||
              message.body === "update itemName"
            ) {
              this.fetchItems();
            }

            if (message.body === "api select") {
              await this.fetchApiSelection();
            }
          }
        );

        this.stompClient.subscribe(
          `/topic/project/${projectId}/${this.user.id}`,
          (message) => {
            console.log(frame);
            if (message.body === "update auth") {
              alert("권한이 변경되었습니다.");
              this.fetchProjectAuthInfo();
            }
          }
        );
      });
    },
    async disconnect() {
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.disconnect(() => {
          console.log("WebSocket disconnected");
        });
        this.stompClient = null;
        this.currentApiSubscription = null; // 구독 초기화
      }
    },
    async handleUnload(event) {
      event.preventDefault();
      try {
        // API 삭제 요청
        await this.deleteMyApiSelection(this.user.id, this.selectedProject.id);
      } catch (error) {
        console.error("Error during unload:", error);
      }
      event.returnValue = ""; // 브라우저에서 기본 확인 메시지 표시
    },
  },
  mounted() {
    this.fetchProjects();
    this.fetchApiSelection();
    this.fetchItems();
    this.fetchSites();
    this.fetchEnvironments();
    this.loadUser();
    window.addEventListener("beforeunload", this.handleUnload);
  },
  beforeUnmount() {
    this.disconnect();
    window.removeEventListener("beforeunload", this.handleUnload);
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
