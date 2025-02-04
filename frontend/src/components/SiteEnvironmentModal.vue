<template>
  <!-- Bootstrap Modal -->
  <div
    class="modal fade show"
    tabindex="-1"
    style="display: block"
    aria-labelledby="siteEnvironmentModal"
    aria-hidden="true"
    v-if="isVisible"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <!-- 모달 헤더 -->
        <div class="modal-header">
          <h5 class="modal-title">환경 선택</h5>
          <button
            type="button"
            class="btn-close"
            aria-label="Close"
            @click="closeModal"
          ></button>
        </div>

        <!-- 모달 본문 -->
        <div class="modal-body">
          <!-- 사이트 선택 -->
          <div class="mb-3">
            <label class="form-label fw-bold">사이트 선택</label>
            <select
              class="form-select"
              v-model="selectedSite"
              @change="fetchEnvironments"
            >
              <option disabled value="" selected>사이트를 선택하세요</option>
              <option v-for="site in sites" :key="site.id" :value="site.id">
                {{ site.name }}
              </option>
            </select>
          </div>

          <!-- 환경 선택 -->
          <div class="mb-3" v-if="selectedSite">
            <label class="form-label fw-bold"> 환경 선택 </label>
            <select
              class="form-select"
              v-model="selectedEnvironment"
              @change="fetchSites"
            >
              <option disabled value="">환경을 선택하세요</option>
              <option
                v-for="env in environments"
                :key="env.id"
                :value="env.id"
              >
                {{ env.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- 모달 푸터 -->
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">
            닫기
          </button>
          <button
            type="button"
            class="btn btn-primary"
            :disabled="!selectedEnvironment"
            @click="confirmSelection"
          >
            OK
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "SiteEnvironmentModal",
  props: {
    isVisible: Boolean, // 모달 표시 여부
    selectedProject: Object, // 선택 프로젝트
  },
  data() {
    return {
      selectedSite: "", // 선택된 사이트
      selectedEnvironment: "", // 선택된 환경
      sites: [], // 사이트 목록
      environments: [], // 환경 목록
    };
  },
  watch: {
    selectedProject: {
      handler(newProject) {
        if (newProject) {
          this.fetchSites(); // 프로젝트 변경 시 사이트 목록 새로 로드
        }
      },
      immediate: true, // 컴포넌트 로드시에도 실행
    },
  },
  methods: {
    async fetchSites() {
      try {
        const response = await this.$axios.get(
          `/api/environments/sites/${this.selectedProject.id}`
        );
        this.sites = response.data; // 사이트 목록 저장
      } catch (error) {
        console.error("사이트 목록을 가져오는 중 오류 발생:", error);
      }
    },
    async fetchEnvironments() {
      try {
        const response = await this.$axios.get(`/api/environments/${this.selectedSite}`);
        this.environments = response.data; // 환경 목록 저장
      } catch (error) {
        console.error("Failed to fetch environments:", error);
      }
    },
    confirmSelection() {
      // 선택된 사이트와 환경을 부모로 전달
      this.$emit("modal-selected-environment", {
        site: this.selectedSite,
        environmentId: this.selectedEnvironment,
      });
      this.closeModal();
    },
    closeModal() {
      // 모달 닫기 이벤트
      this.$emit("close");
    },
  },
  mounted() {
    this.fetchSites();
  },
};
</script>

<style scoped>
.modal.show {
  background-color: rgba(0, 0, 0, 0.5); /* 배경을 반투명하게 */
}
.text-danger {
  font-weight: bold;
}
</style>
