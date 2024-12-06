<template>
  <div
    class="sidebar border border-right col-md-3 col-lg-2 p-0 bg-body-tertiary"
  >
    <div
      class="offcanvas-md offcanvas-end bg-body-tertiary"
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
      <div class="offcanvas-body d-md-flex flex-column p-0 overflow-y-auto">
        <ul class="nav flex-column">
          <!-- Reports/History 메뉴 -->
          <li
            class="nav-item"
            v-if="currentRoute === '/history' || currentRoute === '/reports'"
          >
            <router-link
              to="/history"
              class="nav-link d-flex align-items-center gap-2"
              :class="currentRoute === '/history' ? 'active' : ''"
            >
              <i class="bi bi-clock-history"></i>
              History
            </router-link>
          </li>
          <li
            class="nav-item"
            v-if="currentRoute === '/history' || currentRoute === '/reports'"
          >
            <router-link
              to="/reports"
              class="nav-link d-flex align-items-center gap-2"
              :class="currentRoute === '/reports' ? 'active' : ''"
            >
              <i class="bi bi-activity"></i>
              Reports
            </router-link>
          </li>
          <!-- Projects 메뉴 -->
          <template v-if="currentRoute === '/projects'">
            <li class="nav-item" v-for="project in projects" :key="project.id">
              <a
                class="nav-link d-flex align-items-center gap-2"
                :class="{ active: project.name === selectedProject }"
                href="#"
                @click.prevent="selectProject(project.name)"
              >
                <i class="bi bi-archive-fill"></i>
                {{ project.name }}
              </a>
            </li>
          </template>
          <template v-if="currentRoute === '/test-api'">
            <li v-for="api in tempApis" :key="api.name" class="nav-item">
              <a href="#" class="nav-link" @click.prevent="$emit('select-temp-api', api.name)">
                {{ api.name }}
              </a>
            </li>
          </template>
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
  padding: 15px;
}

.nav-item .active {
  background-color: #e4e4e4;
  font-weight: bold;
}
</style>
