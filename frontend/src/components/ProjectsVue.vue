<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mt-4 mb-5">
      <!-- Project Dropdown -->
      <div>
        <label for="projectSelect" class="form-label me-2">Project:</label>
        <select
          id="projectSelect"
          class="form-select d-inline-block w-auto"
          v-model="selectedProject"
          @change="selectProject(selectedProject)"
        >
          <option
            v-for="project in projects"
            :key="project.id"
            :value="project.name"
          >
            {{ project.name }}
          </option>
        </select>
      </div>
      <!-- New/Delete Project Buttons -->
      <div>
        <button class="btn btn-dark me-2" @click="showNewProjectModal = true">
          New Project
        </button>
        <button class="btn btn-dark" @click="deleteProject">
          Delete Project
        </button>
      </div>
    </div>

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
        />
      </template>
    </CommonModal>

    <!-- Request Actions -->
    <div class="mt-4">
      <button class="btn btn-dark me-2" @click="addFolder">새 폴더 추가</button>
      <button class="btn btn-dark me-2" @click="addAPI">새 요청 추가</button>
      <button class="btn btn-dark" @click="deleteSelected">선택 삭제</button>
    </div>

    <!-- 테이블 -->
    <div class="table-responsive mt-4">
      <table class="table table-borderless">
        <tbody>
          <tr v-for="(item, index) in items" :key="index">
            <!-- 체크박스 -->
            <td>
              <input type="checkbox" v-model="item.selected" />
            </td>
            <td>
              <!-- 폴더나 API 조건을 래핑 -->
              <span v-if="item.type === 'folder'">
                <i class="bi bi-folder-fill me-2"></i>
                {{ item.name }}
              </span>
              <span v-else-if="item.type === 'api'">
                <i class="bi bi-file-earmark me-2"></i>
                {{ item.name }}
              </span>
            </td>
            <!-- API 전용 필드 -->
            <template v-if="item.type === 'api'">
              <td>{{ item.url }}</td>
              <td>
                <span class="badge bg-secondary">{{ item.method }}</span>
              </td>
            </template>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import CommonModal from "./layouts/CommonModal.vue";

export default {
  name: "ProjectsVue",
  components: {
    CommonModal,
  },
  props: ["projects"],
  data() {
    return {
      localProjects: [...this.projects],
      selectedProject: null, // 현재 선택된 프로젝트
      items: [],
      newProjectName: null,
      showNewProjectModal: false,
    };
  },
  methods: {
    selectProject(projectName) {
      // 선택된 프로젝트 처리 로직
      const selectedProject = this.localProjects.find(
        (project) => project.name === projectName
      );
      if (selectedProject) {
        this.selectedProject = selectedProject.name; // 셀렉트 박스 갱신
        this.items = selectedProject.items; // 프로젝트 하위 데이터 업데이트
      }
    },
    addNewProject() {
      if (this.newProjectName) {
        this.localProjects.push({
          id: this.localProjects.length + 1,
          name: this.newProjectName,
          items: [],
        });
        this.selectProject(this.newProjectName); // 새로운 프로젝트 선택
        this.showNewProjectModal = false;
      }
    },
    deleteProject() {
      if (
        confirm(
          `Are you sure you want to delete project "${this.selectedProject}"?`
        )
      ) {
        this.localProjects = this.localProjects.filter(
          (project) => project.name !== this.selectedProject
        );
        this.selectedProject = this.localProjects[0]?.name || null; // 첫 번째 프로젝트 선택
        this.items = this.localProjects[0]?.items || []; // 데이터 초기화
      }
    },
  },
  mounted() {
    // 초기 선택 프로젝트 설정
    if (this.localProjects.length) {
      this.selectProject(this.localProjects[0].name);
    }
  },
};
</script>

<style scoped>
.input-search-method {
  border-width: 0 0 1px;
  border-radius: 0;
}

/* 테이블 기본 스타일 */
.table {
  margin-top: 1rem;
}

.table td {
  vertical-align: middle;
}

.bi-folder-fill {
  color: #6c757d;
}

.bi-file-earmark-text {
  color: #007bff;
}
</style>
