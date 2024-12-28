<template>
  <nav class="navbar bg-dark">
    <div class="container-fluid">
      <button
        class="btn bi bi-list text-white d-md-none m-2"
        type="button"
        data-bs-toggle="offcanvas"
        data-bs-target="#offcanvasSidebar"
        aria-controls="offcanvasSidebar"
      ></button>
      <a class="navbar-brand text-white" href="#">API Tester</a>
      <div class="d-flex align-items-center ms-auto">
        <!-- Projects 메뉴 (화면이 작아지면 숨김) -->
        <ul class="navbar-nav d-none d-md-block me-2">
          <li class="nav-item">
            <router-link
              to="/projects"
              class="nav-link d-flex align-items-center text-white"
              active-class="active"
            >
              <i class="bi bi-archive-fill me-2"></i>
              Projects
            </router-link>
          </li>
        </ul>

        <!-- 프로젝트 선택 드롭다운 -->
        <select
          v-model="selectedProject"
          class="form-select me-2 project-select"
          @change="selectOtherProject"
        >
          <option
            v-for="project in projects"
            :key="project.id"
            :value="project"
          >
            {{ project.name }}
          </option>
          <option value="new-project">New Project...</option>
        </select>

        <!-- 사이트 선택 드롭다운 -->
        <select
          v-model="selectedSite"
          class="form-select me-2 site-select"
          @change="fetchEnvironments"
        >
          <option v-if="sites.length == 0" value="" disabled selected>
            사이트가 없습니다.
          </option>
          <option v-for="site in sites" :key="site.id" :value="site">
            {{ site.name }}
          </option>
        </select>

        <!-- 환경 선택 드롭다운 -->
        <select
          class="form-select"
          v-model="selectedEnvironment"
          @change="confirmSelection"
        >
          <option v-if="environments.length == 0" value="" disabled selected>
            환경이 없습니다.
          </option>
          <option
            v-for="environment in environments"
            :key="environment.id"
            :value="environment"
            @change="confirmSelection"
          >
            {{ environment.name }}
          </option>
        </select>

        <!-- 사용자 프로필 -->
        <div class="dropdown login-user-info">
          <button
            class="btn btn-dark dropdown-toggle text-white"
            type="button"
            data-bs-toggle="dropdown"
          >
            <img src="../../assets/icon/profile-default-icon.png" />
            <span class="d-none d-lg-inline">{{ email }}</span>
            <!-- 이메일 숨김 조건 -->
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <a
                class="dropdown-item"
                href="#"
                @click="showSettingsModal = true"
                ><i class="bi bi-gear"></i> Setting</a
              >
            </li>
            <li>
              <a class="dropdown-item" href="#" @click="logout"
                ><i class="bi bi-door-closed"></i>Logout</a
              >
            </li>
          </ul>
        </div>
      </div>
    </div>
  </nav>
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
        @keyup.enter="addNewProject"
      />
    </template>
  </CommonModal>
  <SettingModal
    v-if="showSettingsModal"
    @modal-setting-confirm="this.$emit('modal-setting-confirm')"
    @close="closeSettingsModal"
  />
</template>

<script>
import CommonModal from "./CommonModal.vue";
import SettingModal from "../SettingModal.vue";

export default {
  name: "NavHeader",
  components: { CommonModal, SettingModal },
  props: {
    propProjects: Array,
    propSites: Array,
    propEnvironments: Array,
  },
  data() {
    return {
      email: "", // 유저 이메일
      loginVerified: false, // 최초 로그인 여부
      projects: [], // 유저 프로젝트 목록
      sites: [], // 프로젝트 하위 사이트 목록
      environments: [], // 프로젝트 하위 환경 목록
      selectedProject: "", // 선택 프로젝트
      selectedSite: "", // 선택 사이트
      selectedEnvironment: "", // 선택 환경
      showNewProjectModal: false, // 새 프로젝트 모달 표시 상태
      showSettingsModal: false, // 설정 모달 표시 상태
      newProjectName: "", // 새 프로젝트명 입력값
    };
  },
  watch: {
    propProjects: {
      handler() {
        this.fetchProjects();
      },
    },
    propSites: {
      handler() {
        this.fetchSites();
      },
    },
    propEnvironments: {
      handler() {
        this.fetchEnvironments();
      },
    },
  },
  mounted() {
    // 이미 저장해둔 이메일이 있다면 해당 이메일 사용
    const storedEmail = localStorage.getItem("userEmail");
    if (storedEmail) {
      this.email = storedEmail;
    } else {
      this.fetchUserEmail();
    }
    this.fetchLoginUserInfo();
    this.fetchProjects();
  },
  methods: {
    closeSettingsModal() {
      this.showSettingsModal = false; // 모달 닫기 처리
    },
    async fetchUserEmail() {
      try {
        const token = localStorage.getItem("accessToken");
        if (!token) {
          console.error("No access token found");
          return;
        }
        const response = await this.$axios.get("/api/auth/user-info", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        this.email = response.data.email;
        localStorage.setItem("userEmail", this.email); // 캐싱
      } catch (error) {
        console.error("Failed to fetch user email:", error);
      }
    },
    async fetchLoginUserInfo() {
      try {
        const response = await this.$axios.get("/api/user/findUserByEmail");
        if (!response.data.user.verified) {
          this.showSettingsModal = true;
          await this.$axios.post("/api/user/renewVerified", {
            verified: true,
          });
        }
      } catch (error) {
        console.log("Failed Login Verified: " + error);
      }
    },
    async fetchProjects() {
      try {
        const response = await this.$axios.get("/api/projects");
        this.projects = response.data;

        // 첫 번째 요소를 자동 선택
        if (this.projects.length > 0) {
          this.selectedProject = this.projects[0];
        }

        this.fetchSites();

        // SideBar 선택 프로젝트 초기값
        this.$emit("project-selected", this.selectedProject);
      } catch (error) {
        console.error("Failed to fetch projects:", error);
      }
    },
    async fetchUserSetting() {
      try {
        const response = await this.$axios.get("/api/user/findUserByEmail");
        this.autoSave = response.data.user.autoSaveUse;
        this.autoSaveTime = response.data.user.autoSaveTime;
        this.autoSaveTerm = response.data.user.autoSaveTerm;
        this.savePath = response.data.user.autoSavePath;
      } catch (error) {
        console.log("Failed load user setting: " + error);
      }
    },
    async fetchSites() {
      try {
        const response = await this.$axios.get(
          `/api/environments/sites/${this.selectedProject.id}`
        );
        this.sites = response.data; // 사이트 목록 저장

        // 첫 번째 요소를 자동 선택
        if (this.sites.length > 0) {
          this.selectedSite = this.sites[0];
        }

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
        // 첫 번째 요소를 자동 선택
        if (this.environments.length > 0) {
          this.selectedEnvironment = this.environments[0];
        }
        this.confirmSelection();
      } catch (error) {
        console.error("Failed to fetch environments:", error);
      }
    },
    confirmSelection() {
      // 선택된 사이트와 환경을 부모로 전달
      this.$emit("selected-environment", {
        site: this.selectedSite,
        environmentId: this.selectedEnvironment,
      });
    },
    selectOtherProject() {
      if (this.selectedProject === "new-project") {
        this.showNewProjectModal = true;
        this.selectedProject = this.projects[0];
      } else {
        this.$emit("project-selected", this.selectedProject);
        this.sites = [];
        this.environments = [];
        this.selectedSite = null;
        this.selectedEnvironment = null;
        this.fetchSites();
      }
    },
    async addNewProject() {
      if (!this.newProjectName.trim()) {
        alert("프로젝트명을 입력해주세요.");
        return;
      }

      // 중복 이름 확인
      const isDuplicate = this.projects.some(
        (project) =>
          project.name.trim().toLowerCase() ===
          this.newProjectName.trim().toLowerCase()
      );

      if (isDuplicate) {
        alert("이미 존재하는 프로젝트명입니다. 다른 이름을 입력하세요."); // 중복 경고
        return;
      }

      try {
        await this.$axios.post("/api/projects", { name: this.newProjectName });
        this.showNewProjectModal = false;
        this.$emit("update-projects");
        this.fetchSites(); // 관련 사이트 목록 갱신
        alert("프로젝트 생성이 완료되었습니다.");
      } catch (error) {
        console.log("Failed Create Project:" + error);
      }

      this.fetchProjects();
    },
    logout() {
      if (confirm("로그아웃 하시겠습니까?")) {
        // localStorage 초기화
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("userEmail");

        alert("로그아웃 되었습니다.");

        this.$emit("logout");
        // 로그인 화면으로 리다이렉트
        this.$router.push("/login");
      }
    },
  },
};
</script>

<style scoped>
/* Navbar CSS Start */
#navbarNav {
  font-size: small;
}

.navbar {
  padding: 0;
}

.project-select {
  width: 200px;
}

.login-user-info {
  padding: 10px;
  margin-left: 10px;
}

.login-user-info button,
ul {
  font-size: small;
  text-align: center;
}

.login-user-info img {
  width: 35px;
  margin-right: 10px;
}

.login-user-info i {
  margin-right: 5px;
}

.navbar a.nav-link {
  padding: 25px 20px;
}

.navbar a.nav-link:hover {
  background-color: black;
}

.navbar a.nav-link.active {
  background-color: black;
  color: white;
}

.form-select {
  font-size: small;
}

.login-user-email {
  color: white;
}
</style>
