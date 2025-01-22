<template>
  <template v-if="!$route.meta.noHeaderSidebar">
    <NavHeader
      :propProjects="projects"
      :propSites="sites"
      :propEnvironments="environments"
      :propSelectedProject="selectedProject"
      :projectOwner="projectOwner"
      :projectAuth="projectAuth"
      :participantsList="participantsList"
      :participants="participants"
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
        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 main-contents">
          <router-view
            :projects="projects"
            :projectAuth="projectAuth"
            :propParticipants="participants"
            :propFileConflict="fileConflict"
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
            :stompClient="stompClient"
            @input-modified="updateModifiedStatus"
            @drop-item="handleDropOutside"
            @temp-save-api="saveTempApi"
            @delete-projects="deleteProjects"
            @update-projects="updateProjects"
            @update-items="fetchItems"
            @update-sites="fetchSites"
            @update-environments="fetchEnvironments"
            @edit-request="handleEditRequest"
            @re-request="handleReRequest"
            @api-selected="handleApiSelected"
            @api-usage-message="handleApiUsageMessage"
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
    <!-- 알림 창 -->
    <div class="toast-container position-fixed bottom-0 end-0 p-3">
      <div
        v-for="(toast, index) in toasts"
        :key="index"
        class="toast show align-items-center text-white bg-dark border-0 mb-2"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
      >
        <div class="d-flex">
          <div class="toast-body">{{ toast.message }}</div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            @click="removeToast(index)"
          ></button>
        </div>
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
      fileConflict: false,
      projects: [],
      sites: [],
      environments: [],
      items: [],
      participants: [], // 프로젝트 주인 미포함 참가자 리스트
      participantsList: [], // 프로젝트 주인 포함 참가자 리스트
      toasts: [], // Toast 메시지 배열
      user: null,
      isSetting: false,
      isModified: false, // API 파일 변경 여부
      message: "",
      changeMessage: "", // API가 변경됐을 때 메시지
      projectAuth: null,
      projectOwner: null,
      currentApiSubscription: null, // 현재 구독 중인 API 경로
      updatedTempApi: null, // 다른 사용자가 수정한 Api 정보
      currentStatusApi: null,
      apiSelections: {}, // 작업 중인 사용자 목록
    };
  },
  watch: {
    selectedProject: {
      handler() {
        if (this.selectedProject === null) {
          return;
        }

        this.selectedTempApi = null;

        this.fetchProjectAuthInfo();
        this.fetchParticipants();
        this.fetchParticipantsList();
      },
      immediate: true,
    },
    selectedTempApi: {
      handler(newApi) {
        if (!this.stompClient || !this.selectedProject) {
          return; // WebSocket이 연결되지 않았거나 프로젝트가 선택되지 않은 경우
        }

        if (newApi && newApi.id) {
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
        this.addToast("폴더나 파일 이동 권한이 없습니다.");
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
        this.addToast("이동 중 오류가 발생했습니다.");
      }
    },
    async handleApiSelected(loadApi) {
      if (this.isModified === true) {
        const confirmed = confirm(
          "변경사항이 저장되지 않았습니다. 저장하시겠습니까?"
        );
        if (confirmed) {
          this.saveApiData(this.currentStatusApi); // 저장 로직
          this.addToast("저장되었습니다.");
          // 현재 프로젝트와 API의 사용자 정보 조회
          const apiId = this.currentStatusApi?.itemId;

          // `apiSelections`에서 현재 유저 제외
          const otherUsers = this.apiSelections[apiId]?.filter(
            (user) => user.email !== this.user.email
          );

          // 메시지 형식 정의
          const notificationMessage = {
            type: "API",
            content: "API_UPDATE",
            data: this.currentStatusApi, // 현재 API 데이터 전달
          };

          // 다른 사용자들에게 메시지 전송
          if (otherUsers && otherUsers.length > 0) {
            otherUsers.forEach((user) => {
              this.stompClient.send(
                `/topic/user/${user.id}`, // WebSocket 경로
                {},
                JSON.stringify(notificationMessage) // 메시지 전송
              );
            });
          }
        }
      }
      this.isModified = false; // 상태 초기화

      this.selectedTempApi = loadApi;

      this.$router.push({
        name: "ApiTest",
        params: { tempApi: this.selectedTempApi }, // 라우터에 데이터 전달
      });
    },

    handleApiUsageMessage(loadApi) {
      this.addToast("저장되었습니다.");
      // 현재 프로젝트와 API의 사용자 정보 조회
      const apiId = this.loadApi?.itemId;

      // `apiSelections`에서 현재 유저 제외
      const otherUsers = this.apiSelections[apiId]?.filter(
        (user) => user.email !== this.user.email
      );

      // 메시지 형식 정의
      const notificationMessage = {
        type: "API",
        content: "API_UPDATE",
        data: loadApi, // 현재 API 데이터 전달
      };

      console.log(otherUsers);

      // 다른 사용자들에게 메시지 전송
      if (otherUsers && otherUsers.length > 0) {
        otherUsers.forEach((user) => {
          this.stompClient.send(
            `/topic/user/${user.id}`, // WebSocket 경로
            {},
            JSON.stringify(notificationMessage) // 메시지 전송
          );
        });
      }
    },

    async saveApiData(selectApi) {
      if (this.projectAuth === "read") return;
      try {
        // this.apiId가 있는 경우(이미 저장된 파일의 경우) 업데이트
        if (selectApi.id !== null) {
          const project = await this.$axios.patch(
            "/api/projects/update/projectItemName",
            {
              id: selectApi.itemId,
              projectId: Number(this.selectedProject.id),
              parentId: null,
              type: "api",
              name: selectApi.name || "TempAPI",
              depth: 1,
            },
            { showSpinner: false }
          );

          const apiData = {
            name: selectApi.name || "TempAPI",
            itemId: Number(project.data.updatedItem.id),
            method: selectApi.method,
            url: selectApi.url,
            headers: selectApi.headers,
            queryParameters: selectApi.queryParameters,
            formParameters: selectApi.formParameters,
            file: selectApi.file,
            selectedBodyType: selectApi.selectedBodyType,
            selectedEnvironment: selectApi.selectedEnvironment,
          };

          await this.$axios.patch(
            "/api/apis",
            {
              id: selectApi.id,
              ...apiData,
            },
            { showSpinner: false }
          );

          this.handleRefreshSidebar(selectApi.itemId);
          console.log("저장 완료");
        } else {
          // this.apiId가 없는 경우 추가
          const project = await this.$axios.post(
            "/api/projects/add-api",
            {
              projectId: Number(this.selectedProject.id),
              parentId: null,
              type: "api",
              name: selectApi.name || "TempAPI",
              depth: 1,
            },
            { showSpinner: false }
          );

          const apiData = {
            name: selectApi.name || "TempAPI",
            itemId: Number(project.data.id),
            method: selectApi.method,
            url: selectApi.url,
            headers: selectApi.headers,
            queryParameters: selectApi.queryParameters,
            formParameters: selectApi.formParameters,
            file: selectApi.file,
            selectedBodyType: selectApi.selectedBodyType,
            selectedEnvironment: selectApi.selectedEnvironment,
          };

          const savedApi = await this.$axios.post("/api/apis", apiData, {
            showSpinner: false,
          });

          this.handleRefreshSidebar(selectApi.itemId);
          console.log("API " + savedApi.data + "저장 완료");
        }
      } catch (error) {
        console.log("Failed save projectItem: " + error);
        alert("API 저장에 실패했습니다.");
      }
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
      if (data) {
        this.selectedSite = data.site;
        this.selectedEnvironment = data.environmentId;
      } else {
        this.selectedSite = null;
        this.selectedEnvironment = null;
      }
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
        localStorage.removeItem("profileImageUrl");

        this.addToast("로그아웃 되었습니다.");

        // 로그인 화면으로 리다이렉트
        this.$router.push("/login");
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

        console.log(response.data);

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

      this.fetchParticipants();
      this.fetchParticipantsList();
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
    async fetchParticipants() {
      try {
        const response = await this.$axios.get(
          `/api/projects/${this.selectedProject.id}/participants`,
          { showSpinner: false }
        );

        const participants = response.data;

        // 각 userId로 이메일 조회 및 병합
        const updatedParticipants = await Promise.all(
          participants.map(async (participant) => {
            try {
              const emailResponse = await this.$axios.get(
                `/api/user/findById/${participant.userId}`,
                { showSpinner: false }
              );
              return {
                ...participant, // 기존 데이터 복사
                email: emailResponse.data.user.email, // 이메일 추가
              };
            } catch (error) {
              console.error(
                `Failed to fetch email for userId ${participant.userId}: `,
                error
              );
              return participant; // 이메일을 가져오지 못한 경우 기존 데이터 유지
            }
          })
        );

        this.participants = updatedParticipants; // 업데이트된 데이터를 저장
      } catch (error) {
        console.error("Failed to fetch participants: ", error);
      }
    },
    async fetchParticipantsList() {
      try {
        const response = await this.$axios.get(
          `/api/projects/${this.selectedProject.id}/participants`,
          { showSpinner: false }
        );

        const ownerResponse = await this.$axios.get(
          `/api/projects/find/${this.selectedProject.id}`,
          { showSpinner: false }
        );

        const participants = [...response.data, ownerResponse.data];

        // 각 userId로 이메일 조회 및 병합
        const updatedParticipants = await Promise.all(
          participants.map(async (participant) => {
            try {
              const emailResponse = await this.$axios.get(
                `/api/user/findById/${participant.userId}`,
                { showSpinner: false }
              );
              return {
                ...participant, // 기존 데이터 복사
                email: emailResponse.data.user.email, // 이메일 추가
              };
            } catch (error) {
              console.error(
                `Failed to fetch email for userId ${participant.userId}: `,
                error
              );
              return participant; // 이메일을 가져오지 못한 경우 기존 데이터 유지
            }
          })
        );

        this.participantsList = updatedParticipants; // 업데이트된 데이터를 저장
      } catch (error) {
        console.error("Failed to fetch participantsList: ", error);
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

        this.apiSelections = await this.mapUsersByApi(response.data);
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
    updateModifiedStatus({ hasChanges, currentState }) {
      this.isModified = hasChanges;
      this.currentStatusApi = currentState;
      console.log("Has Changes: ", hasChanges);
      console.log("Current State: ", currentState);
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
        // 현재 API 사용 설정 업데이트
        await this.$axios.post(`/api/apis/usage/${projectId}/${itemId}`);

        // 프로젝트 내 모든 API 사용자 목록 조회
        const response = await this.$axios.get(
          `/api/apis/usage/list/${projectId}`,
          {
            showSpinner: false,
          }
        );

        console.log("Updated API Usage:", response.data);

        // 데이터 처리 및 UI 업데이트
        this.apiSelections = this.mapUsersByApi(response.data);
      } catch (error) {
        console.error("Failed to update API Selection:", error);
      }
    },

    async updateAutoSaveForParticipants(autoSaveStatus) {
      try {
        // 프로젝트의 모든 참여자 중 Owner를 제외
        const participantsToUpdate = this.participants.filter(
          (participant) => participant.email !== this.projectOwner
        );

        // 각 참여자의 자동 저장 권한을 업데이트
        for (const participant of participantsToUpdate) {
          await this.$axios.patch(`/api/user/setting/${participant.userId}`, {
            autoSaveUse: autoSaveStatus,
          });
        }
      } catch (error) {
        console.error("Failed to update auto-save for participants:", error);
        this.addToast("자동 저장 권한 변경 중 오류가 발생했습니다.");
      }
    },

    async updateAutoSaveForThisUser(autoSaveStatus) {
      try {
        console.log("This user: " + this.user);
        // 현재 참여자의 자동 저장 권한을 업데이트
        await this.$axios.patch(`/api/user/setting/${this.user.id}`, {
          autoSaveUse: autoSaveStatus,
        });
      } catch (error) {
        console.error("Failed to update auto-save for participants:", error);
        this.addToast("자동 저장 권한 변경 중 오류가 발생했습니다.");
      }
    },

    mapUsersByApi(apiUsageMap) {
      const groupedUsers = {};

      // apiUsageMap: { userId: { itemId, email, profileImageUrl }, ... }
      Object.entries(apiUsageMap).forEach(([userId, usageInfo]) => {
        const { itemId, email, profileImageUrl } = usageInfo;

        if (!groupedUsers[itemId]) {
          groupedUsers[itemId] = [];
        }

        // 카카오나 구글 프로필 경로인지 확인 후 처리
        const finalProfileImageUrl = profileImageUrl?.startsWith("http")
          ? profileImageUrl // 카카오/구글 프로필 URL
          : `${process.env.VUE_APP_SERVER_IP}${
              profileImageUrl || "/uploads/profiles/profile-default-icon.png"
            }`; // 로컬 프로필 경로 또는 기본 이미지 경로

        groupedUsers[itemId].push({
          id: Number(userId),
          email,
          profileImageUrl: finalProfileImageUrl,
        });
      });

      return groupedUsers;
    },
    // 사용자 이메일 가져오기
    async getUserEmail(userId) {
      const userResponse = await this.$axios.get(
        `/api/user/findById/${userId}`,
        { showSpinner: false }
      );
      const userEmail = userResponse.data.user.email;

      return userResponse ? userEmail : "Unknown User";
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
    connectWebSocket(userId) {
      if (this.user === null) {
        this.loadUser();
      }

      const socket = new SockJS("http://localhost:8081/ws");
      this.stompClient = Stomp.over(socket);

      this.stompClient.connect({}, () => {
        this.stompClient.subscribe(`/topic/user/${userId}`, async (message) => {
          const parsedMessage = JSON.parse(message.body);

          switch (parsedMessage.type) {
            case "PROJECT":
              // 폴더 추가/삭제, 파일 추가/삭제, 순서 변경, 이름 변경
              if (parsedMessage.content === "UPDATE_PROJECT_ITEM") {
                this.fetchItems();
              }

              if (parsedMessage.content === "API_SELECT") {
                await this.fetchApiSelection();

                // 현재 선택된 API ID 확인
                const selectedApiId = this.selectedTempApi?.itemId;
                console.log("selectedApiId: " + selectedApiId);

                console.log(this.apiSelections[selectedApiId]);

                // 해당 API를 사용 중인 유저가 있는지 검사
                const isApiInUse =
                  selectedApiId &&
                  this.apiSelections[selectedApiId] &&
                  this.apiSelections[selectedApiId].length > 1;

                // 해당 API를 사용 중인 유저가 있고,
                // 현재 로그인한 유저가 프로젝트 주인이라면 다른 참가자들 자동 저장 OFF
                if (isApiInUse && this.user.email === this.projectOwner) {
                  this.updateAutoSaveForParticipants(false);
                }
              }

              // API 저장
              if (parsedMessage.content === "API_SAVE") {
                this.handleRefreshSidebar();
              }

              // 새 참여자
              if (parsedMessage.content === "NEW_PARTICIPANT") {
                this.fetchParticipants();
                this.fetchParticipantsList();
              }

              // 프로젝트 나가기
              if (parsedMessage.content === "EXIT_PROJECT") {
                this.addToast(message.body);
                await this.fetchParticipants();
              }

              // 권한 변경
              if (parsedMessage.content === "UPDATE_AUTH") {
                this.addToast("권한이 변경되었습니다.");
                this.fetchProjectAuthInfo();
              }

              // 프로젝트 강퇴
              if (parsedMessage.content === "SEND_OUT_PARTICIPANT") {
                this.addToast(message.body);
                this.fetchProjects();
              }
              break;
            case "API":
              // 프로젝트 주인 입장
              if (parsedMessage.content === "ENTER_OWNER") {
                this.addToast(
                  "프로젝트 Owner가 입장했습니다. 자동 저장 권한이 비활성화 되었습니다."
                );
              }

              if (parsedMessage.content === "API_UPDATE") {
                this.fileConflict = true;
              }

              break;
            default:
              console.log("알 수 없는 메시지 타입:", parsedMessage);
          }
        });
      });
    },
    addToast(message) {
      this.toasts.push({ message });
      setTimeout(() => {
        this.toasts.shift();
        console.log(this.toasts);
      }, 5000); // 5초 후 자동 삭제
    },
    removeToast(index) {
      this.toasts.splice(index, 1);
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
  async mounted() {
    await this.loadUser();
    this.fetchProjects();
    this.fetchApiSelection();
    this.fetchItems();
    this.fetchSites();
    this.fetchEnvironments();
    if (this.user !== null) {
      this.connectWebSocket(this.user.id);
    }
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

.toast-container {
  z-index: 1055; /* 알림이 다른 요소 위에 나타나도록 설정 */
}

.toast {
  transition: opacity 1s ease, transform 1s ease;
}

.toast.fade-out {
  opacity: 0;
  transform: translateY(20px);
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
