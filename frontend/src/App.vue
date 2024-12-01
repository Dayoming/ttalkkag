<template>
  <template v-if="!$route.meta.noHeaderSidebar">
    <NavHeader />
    <div class="container-fluid">
      <div class="row">
        <CommonSideBar
          :projects="projects"
          @select-project="handleSidebarProjectSelection"
        />
        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
          <router-view
            v-bind:projects="projects"
            @select-project="handleSidebarProjectSelection"
          />
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
      selectedProject: "MyProject",
      projects: [
        {
          id: 1,
          name: "myProject",
          items: [
            { type: "folder", name: "SubFolder1" },
            { type: "api", name: "API1", url: "/example/url1", method: "GET" },
          ],
        },
        {
          id: 2,
          name: "newProject",
          items: [
            { type: "folder", name: "SubFolder2" },
            { type: "api", name: "API2", url: "/example/url2", method: "POST" },
          ],
        },
      ],
    };
  },
  methods: {
    handleSidebarProjectSelection(projectName) {
      // `ProjectsVue`의 `selectProject` 메서드를 호출
      const projectsVue = this.$refs.projectsVue;
      if (projectsVue) {
        projectsVue.selectProject(projectName);
      }
    },
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
</style>