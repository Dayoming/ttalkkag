<template>
  <template v-if="!$route.meta.noHeaderSidebar">
    <NavHeader />
    <div class="container-fluid">
      <div class="row">
        <CommonSideBar
          :projects="projects"
          :tempApis="tempApis"
          @select-temp-api="loadTempApi"
          @select-project="handleSidebarProjectSelection"
        />
        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 main-contents">
          <router-view
            v-bind:projects="projects"
            :tempApi="selectedTempApi"
            @temp-save-api="saveTempApi"
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
      selectedTempApi: null, // 현재 선택된 임시 저장된 API
      tempApis: [], // 임시 저장된 API 데이터
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
    loadTempApi(apiName) {
      const tempApi = this.tempApis.find((api) => api.name === apiName);
      if (tempApi) {
        this.selectedTempApi = tempApi;
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

.main-contents {
  height: 100vh;
}
</style>
