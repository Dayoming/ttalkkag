<template>
  <div
    class="sidebar border border-right col-md-3 col-lg-2 p-0 bg-body-tertiary"
  >
    <div
      class="offcanvas-md offcanvas-end bg-body-tertiary d-flex flex-column vh-100"
      tabindex="-1"
      id="sidebarMenu"
      aria-labelledby="sidebarMenuLabel"
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
      </div>
      <!-- 하단 고정된 메뉴 -->
      <div class="border-top mt-auto">
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
    class="offcanvas offcanvas-start bg-body-tertiary"
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
export default {
  name: "CommonSideBar",
  props: ["projects", "tempApis", "selectedProject"],
  computed: {
    currentRoute() {
      return this.$route.path;
    },
  },
  methods: {
    selectProject(projectName) {
      this.$emit("select-project", projectName);
    },
  },
};
</script>

<style scoped>
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
</style>
