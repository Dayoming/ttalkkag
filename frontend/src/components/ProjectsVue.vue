<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mt-4 mb-5">
      <!-- Project Dropdown -->
      <div>
        <label for="projectSelect" class="form-label me-2">Project:</label>
        <select
          id="projectSelect"
          class="form-select d-inline-block w-auto"
          v-model="localSelectedProject"
          @change="fetchItems"
        >
          <option
            v-for="project in localProjects"
            :key="project.id"
            :value="project"
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
        <button v-if="isMyProject" class="btn btn-dark" @click="deleteProject">
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
          @keyup.enter="addNewProject"
        />
      </template>
    </CommonModal>

    <!-- Request Actions -->
    <div class="mt-4">
      <div class="d-flex align-items-center mb-4 w-75">
        <i class="bi bi-search" style="margin-right: 5px"></i>
        <input
          type="text"
          class="form-control me-2 input-search"
          v-model="searchQuery"
        />
        <select
          class="form-select me-2"
          v-model="selectedType"
          style="width: 230px"
        >
          <option value="">ALL</option>
          <option value="folder">Folder</option>
          <option value="api">API</option>
        </select>
        <select
          v-show="this.selectedType === 'api'"
          class="form-select me-2"
          v-model="selectedMethod"
          style="width: 230px"
        >
          <option value="">ALL</option>
          <option value="GET">GET</option>
          <option value="POST">POST</option>
          <option value="PUT">PUT</option>
          <option value="DELETE">DELETE</option>
          <option value="PATCH">PATCH</option>
        </select>
      </div>
      <button
        v-if="projectAuth === 'write'"
        class="btn btn-dark me-2"
        @click="addFolder"
      >
        새 폴더 추가
      </button>
      <button
        v-if="projectAuth === 'write'"
        class="btn btn-dark me-2"
        @click="this.$router.push('/test-api')"
      >
        새 요청 추가
      </button>
      <button
        v-if="isMyProject"
        class="btn btn-dark me-2"
        @click="showInviteCodeModal = true"
      >
        프로젝트 초대
      </button>
      <button
        v-if="isMyProject"
        class="btn btn-dark"
        @click="manageParticipants"
      >
        참여자 관리
      </button>
      <button
        v-if="!isMyProject"
        class="btn btn-dark"
        @click="exitParticipant"
      >
        나가기
      </button>
    </div>

    <!-- 모든 폴더 열기/닫기 버튼 -->
    <div class="d-flex justify-content-end mb-2">
      <button
        class="btn btn-dark mt-2 me-2 folder-toggle-btn"
        @click="toggleAllFolders(true)"
      >
        Open
      </button>
      <button
        class="btn btn-dark mt-2 folder-toggle-btn"
        @click="toggleAllFolders(false)"
      >
        Close
      </button>
    </div>
    <!-- 테이블 -->
    <div
      class="table-responsive mt-4"
      @dragover.prevent
      @drop="handleDropOutside"
    >
      <table class="table table-borderless">
        <th>API명/폴더명</th>
        <th>URL</th>
        <th>METHOD</th>
        <tbody>
          <RecursiveFolderItem
            v-for="item in localItems"
            :key="item.id + '_' + updateKey"
            :item="item"
            :depth="0"
            :selected-file-id="selectedFileId"
            :projectAuth="projectAuth"
            :apiSelections="apiSelections"
            :profileImageUrl="profileImageUrl"
            @selection-change="handleSelectionChange"
            @toggle-folder="toggleFolder"
            @update-items="$emit('update-items')"
            @api-selected="handleApiSelected"
          />
        </tbody>
      </table>
    </div>
  </div>
  <!-- 초대 코드 발급 모달 -->
  <div v-if="showInviteCodeModal" class="modal fade show d-block" tabindex="-1">
    <div class="modal-dialog text-center">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">프로젝트 초대</h5>
          <button
            type="button"
            class="btn-close"
            @click="closeInviteCodeModal"
          ></button>
        </div>
        <div class="modal-body">
          <p class="mb-4">
            프로젝트에 초대하고자 하는 사용자의 이메일을 입력해 주세요. <br />
            초대코드의 유효시간은 <strong>1시간</strong>입니다.
          </p>
          <input
            type="text"
            class="form-control"
            v-model="inviteUserEmail"
            placeholder="이메일을 입력하세요."
            @keyup.enter="generateInviteCode"
          />
        </div>
        <div class="modal-footer justify-content-center">
          <button
            class="btn btn-dark w-100"
            type="button"
            @click="generateInviteCode"
          >
            초대코드 전송
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 참여자 관리 모달 -->
  <div
    v-if="showManageParticipantsModal"
    class="modal fade show d-block"
    tabindex="-1"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">참여자 관리</h5>
          <button
            type="button"
            class="btn-close"
            @click="this.showManageParticipantsModal = false"
          ></button>
        </div>
        <div class="modal-body">
          <template v-if="participants.length > 0">
            <div
              v-for="participant in participants"
              :key="participant.id"
              class="d-flex align-items-center mb-3"
            >
              <img
                src="../assets/profiles/profile-default-icon.png"
                class="rounded-circle me-2"
                width="40"
                height="40"
              />
              <div class="participants-info">
                <p class="mb-0">{{ participant.email }}</p>
                <div>
                  <label>
                    <input
                      type="radio"
                      :value="'read'"
                      v-model="participant.permissionLevel"
                    />
                    읽기
                  </label>
                  <label class="ms-2">
                    <input
                      type="radio"
                      :value="'write'"
                      v-model="participant.permissionLevel"
                    />
                    수정
                  </label>
                </div>
              </div>
              <div class="remove-btn-div">
                <button
                  class="btn btn-danger"
                  @click="removeParticipant(participant)"
                  style="margin-left: 200px"
                >
                  강퇴
                </button>
              </div>
            </div>
          </template>
          <template v-else>
            <p>참여자가 없습니다.</p>
          </template>
        </div>
        <div class="modal-footer">
          <button
            v-if="participants.length > 0"
            class="btn btn-dark w-100"
            @click="saveParticipants"
          >
            저장
          </button>
          <button
            v-else
            class="btn btn-secondary"
            @click="this.showManageParticipantsModal = false"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CommonModal from "./layouts/CommonModal.vue";
import RecursiveFolderItem from "./RecursiveFolderItem.vue";

export default {
  name: "ProjectsVue",
  components: {
    CommonModal,
    RecursiveFolderItem,
  },
  props: [
    "projects",
    "selectedProject",
    "items",
    "projectAuth",
    "apiSelections",
    "propParticipants",
  ],
  data() {
    return {
      localProjects: [],
      localItems: [],
      localApi: [],
      updateKey: 0,
      profileImageUrl: "../assets/profiles/profile-defualt-icon.png",
      newProjectName: null,
      showNewProjectModal: false,
      localSelectedProject: "",
      searchQuery: "",
      selectedMethod: "",
      selectedType: "",
      selectedFileId: null,
      showInviteCodeModal: false,
      showManageParticipantsModal: false,
      userId: null,
      isMyProject: false,
      inviteUserEmail: "",
      participants: null,
    };
  },
  methods: {
    closeInviteCodeModal() {
      this.showInviteCodeModal = false;
    },
    async fetchProjects(selectedProject = null) {
      try {
        const response = await this.$axios.get("/api/projects");
        this.localProjects = response.data;

        // 특정 프로젝트를 선택하거나 기본적으로 첫 번째 프로젝트를 선택
        if (this.localProjects.length > 0) {
          const selectProject = selectedProject
            ? this.localProjects.find(
                (project) => project.id === selectedProject.id
              )
            : this.localProjects[0];

          if (selectProject) {
            this.localSelectedProject = selectProject;
          }
        }

        this.fetchItems();
      } catch (error) {
        console.error("Failed to fetch projects:", error);
      }
    },
    async loadUserId() {
      try {
        const response = await this.$axios.get("/api/user/findUserByEmail");
        this.userId = response.data.user.id;
        this.profileImageUrl = response.data.user.profileImage;
      } catch (error) {
        console.log("load User Id Failed: " + error);
      }
    },
    async fetchItems() {
      const selectedProject = this.localProjects.find(
        (project) => project.id === this.localSelectedProject.id
      );

      if (!selectedProject) {
        console.error("선택된 프로젝트를 찾을 수 없습니다.");
        return;
      }

      try {
        const response = await this.$axios.get(
          `/api/projects/${selectedProject.id}`
        );

        const updatedItems = await Promise.all(
          response.data.map(async (item) => {
            if (item.type === "api") {
              try {
                const apiResponse = await this.$axios.get(
                  `/api/apis/${item.id}`
                , { showSpinner: false });
                return {
                  ...item,
                  apiId: apiResponse.data.api.id,
                  apiUrl: apiResponse.data.api.url || "",
                  apiMethod: apiResponse.data.api.method || "",
                };
              } catch (apiError) {
                console.error(`API 호출 실패: ${item.id}`, apiError);
                return { ...item }; // 오류 발생 시 원래 데이터를 반환
              }
            }
            return { ...item }; // `type`이 `api`가 아닌 경우 그대로 반환
          })
        );

        if (selectedProject.userId === this.userId) {
          this.isMyProject = true;
        } else {
          this.isMyProject = false;
        }

        this.fetchParticipants(selectedProject.id);

        this.localItems = this.buildTreeStructure(updatedItems); // 최상위 항목만 로드
        this.expandAll(this.localItems);
      } catch (error) {
        console.error("아이템을 불러오는 중 오류가 발생했습니다:", error);
      }
    },
    async fetchApi(itemId) {
      try {
        const apiResponse = await this.$axios.get(`/api/apis/${itemId}`);
        return apiResponse.data.api;
      } catch (error) {
        console.log("Failed Api Response Fetch: " + error);
      }
    },
    async fetchFilteredItems() {
      const selectedProject = this.localProjects.find(
        (project) => project.id === this.localSelectedProject.id
      );

      if (!selectedProject) {
        console.error("선택된 프로젝트를 찾을 수 없습니다.");
        return;
      }

      try {
        const response = await this.$axios.get(
          `/api/projects/search/${selectedProject.id}`,
          {
            params: {
              query: this.searchQuery,
              type: this.selectedType,
              method:
                this.selectedMethod === "api" ? this.selectedMethod : null,
            },
          }, { showSpinner: false }
        );

        const updatedItems = await Promise.all(
          response.data.map(async (item) => {
            if (item.type === "api") {
              try {
                const apiResponse = await this.$axios.get(
                  `/api/apis/${item.id}`
                , { showSpinner: false });
                return {
                  ...item,
                  apiId: apiResponse.data.api.id,
                  apiUrl: apiResponse.data.api.url || "",
                  apiMethod: apiResponse.data.api.method || "",
                };
              } catch (apiError) {
                console.error(`API 호출 실패: ${item.id}`, apiError);
                return { ...item }; // 오류 발생 시 원래 데이터를 반환
              }
            }
            return { ...item }; // `type`이 `api`가 아닌 경우 그대로 반환
          })
        );

        this.localItems = this.buildTreeStructure(updatedItems);
        this.expandAll(this.localItems);
      } catch (error) {
        console.error("검색 중 오류가 발생했습니다:", error);
      }
    },
    buildTreeWithParents(items) {
      const itemMap = {};
      console.log(items);

      // 모든 항목을 맵에 저장
      items.forEach((item) => {
        item.children = []; // 자식 초기화
        itemMap[item.id] = item;
      });

      const tree = [];

      items.forEach((item) => {
        if (item.parentId === null) {
          // 루트 노드
          tree.push(item);
        } else if (itemMap[item.parentId]) {
          // 부모가 존재하면 연결
          const parent = itemMap[item.parentId];
          if (!parent.children.some((child) => child.id === item.id)) {
            parent.children.push(item);
          }
        }
      });

      return tree; // 트리 반환
    },

    buildTreeStructure(items) {
      const idToItemMap = {};
      items.forEach((item) => {
        try {
          if (item.type === "api") {
            idToItemMap[item.id] = {
              ...item,
              children: [],
              isOpen: false,
              isProjectsVue: true,
            };
          } else {
            idToItemMap[item.id] = {
              ...item,
              children: [],
              isOpen: false,
              isProjectsVue: true,
            };
          }
        } catch (error) {
          console.log(error);
        }
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

    toggleAllFolders(open) {
      const toggleRecursive = (items) => {
        items.forEach((item) => {
          if (item.type === "folder") {
            item.isOpen = open;
            if (item.children.length > 0) {
              toggleRecursive(item.children);
            }
          }
        });
      };
      toggleRecursive(this.localItems);
      this.updateKey++; // 화면 갱신
    },

    async fetchParticipants(projectId) {
      try {
        const response = await this.$axios.get(
          `/api/projects/${projectId}/participants`, { showSpinner: false }
        );
        const participants = response.data;

        // 각 userId로 이메일 조회 및 병합
        const updatedParticipants = await Promise.all(
          participants.map(async (participant) => {
            try {
              const emailResponse = await this.$axios.get(
                `/api/user/findById/${participant.userId}`, { showSpinner: false }
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
    async saveParticipants() {
      try {
        const selectedProject = this.localProjects.find(
          (project) => project.id === this.localSelectedProject.id
        );

        await this.$axios.post(
          `/api/projects/${selectedProject.id}/participants`,
          this.participants
        );
        alert("변경 사항이 저장되었습니다.");
        this.showManageParticipantsModal = false;
      } catch (error) {
        console.error("Failed to save participants:", error);
        alert("저장 중 오류가 발생했습니다.");
      }
    },
    findFolderById(folderId, items) {
      for (const item of items) {
        if (item.id === folderId) {
          return item;
        }
        if (item.children && item.children.length > 0) {
          const found = this.findFolderById(folderId, item.children);
          if (found) {
            return found;
          }
        }
      }
      return null;
    },
    async addNewProject() {
      if (!this.newProjectName || !this.newProjectName.trim()) {
        alert("프로젝트명을 입력하세요."); // 입력값이 비어있을 때 경고
        return;
      }

      // 중복 이름 확인
      const isDuplicate = this.localProjects.some(
        (project) =>
          project.name.trim().toLowerCase() ===
          this.newProjectName.trim().toLowerCase()
      );

      if (isDuplicate) {
        alert("이미 존재하는 프로젝트명입니다. 다른 이름을 입력하세요."); // 중복 경고
        return;
      }

      try {
        const response = await this.$axios.post("/api/projects", {
          name: this.newProjectName,
        });
        this.$emit("update-projects", response.data);
        this.fetchProjects(response.data);
        this.localSelectedProject = response.data;
        this.showNewProjectModal = false;
        this.newProjectName = "";
      } catch (error) {
        console.error("Failed to create project:", error);
      }
    },
    async addFolder() {
      const folderName = prompt("새 폴더 이름을 입력하세요: ");
      if (!folderName) {
        alert("폴더 이름을 입력해주세요.");
        return;
      }

      const selectedProject = this.localProjects.find(
        (project) => project.id === this.localSelectedProject.id
      );

      try {
        await this.$axios.post(`/api/projects/add-folder`, {
          projectId: selectedProject.id,
          parentId: null,
          name: folderName,
          depth: 1,
        });
        this.$emit("update-items");
      } catch (error) {
        console.error("폴더 추가 중 오류가 발생했습니다:", error);
      }
    },
    async deleteProject() {
      if (this.selectedProject.id === this.localSelectedProject.id) {
        alert(
          "현재 사용하고 있는 프로젝트는 삭제할 수 없습니다. 사용 중인 프로젝트를 변경한 후 다시 시도해 주세요."
        );
        return;
      }

      if (this.localProjects.length === 1) {
        alert("프로젝트는 1개 이상 소유해야 합니다.");
        return;
      }

      if (
        confirm(`${this.localSelectedProject.name}을(를) 삭제하시겠습니까?`)
      ) {
        const selectedProject = this.localProjects.find(
          (project) => project.id === this.localSelectedProject.id
        );
        if (selectedProject) {
          try {
            await this.$axios.delete(`/api/projects/${selectedProject.id}`);
            this.localProjects = this.localProjects.filter(
              (project) => project.id !== selectedProject.id
            );

            this.$emit("delete-projects", selectedProject.id);
            this.fetchProjects();
          } catch (error) {
            console.error("Failed to delete project:", error);
          }
        }
      }
    },
    async removeParticipant(participant) {
      if (!confirm(`${participant.email} 님을 강퇴하시겠습니까?`)) {
        return;
      }

      try {
        // API 요청: 유저 강퇴
        await this.$axios.delete(
          `/api/projects/${this.localSelectedProject.id}/participants/${participant.id}`
        );

        // participants 리스트에서 제거
        this.participants = this.participants.filter(
          (p) => p.id !== participant.id
        );
      } catch (error) {
        console.error("Failed to remove participant:", error);
        alert("강퇴 중 오류가 발생했습니다.");
      }
    },
    async exitParticipant() {
      if (!confirm(`${this.localSelectedProject.name}을(를) 나가시겠습니까?`)) {
        return;
      }

      try {
        // API 요청: 프로젝트 탈퇴
        await this.$axios.delete(
          `/api/projects/${this.localSelectedProject.id}/participants`
        );

        this.$emit("update-projects");
        this.localSelectedProject = this.localProjects[0];
        alert("탈퇴가 완료되었습니다.");
      } catch (error) {
        console.error("Failed to remove participant:", error);
        alert("강퇴 중 오류가 발생했습니다.");
      }
    },
    handleSelectionChange(selectedItem) {
      this.$emit("item-selected", selectedItem);
    },
    handleApiSelected(selectedTempApi) {
      this.$emit("api-selected", selectedTempApi);
    },
    handleDropOutside(event) {
      this.$emit("drop-item", event);
    },
    async generateInviteCode() {
      try {
        const selectedProject = this.localProjects.find(
          (project) => project.id === this.localSelectedProject.id
        );

        await this.$axios.post("/api/projects/invite", {
          projectId: selectedProject.id,
          userEmail: this.inviteUserEmail,
        });

        alert("초대가 완료되었습니다.");
        this.inviteUserEmail = "";
        this.showInviteCodeModal = false;
      } catch (error) {
        console.error("Failed to generate invite code:", error);
        alert("초대 코드를 생성하는 중 오류가 발생했습니다.");
      }
    },
    async manageParticipants() {
      this.showManageParticipantsModal = true;
    },
  },
  mounted() {
    this.fetchProjects();
    this.loadUserId();
  },
  watch: {
    // 부모에서 전달받은 projects가 변경되면 localProjects도 동기화
    projects: {
      handler(newProjects) {
        this.localProjects = [...newProjects];
      },
      immediate: true, // 컴포넌트가 처음 로드될 때도 동기화
    },
    localItems: {
      handler() {
        this.updateKey++;
      },
      deep: true, // items 내부의 객체 변경 감지
    },
    items: {
      handler(newItems) {
        this.localItems = newItems;
        this.fetchItems();
      },
    },
    searchQuery: {
      handler(newQuery) {
        if (newQuery.trim() !== "") {
          this.fetchFilteredItems();
        } else {
          this.fetchItems();
        }
      },
      immediate: false,
    },
    propParticipants: {
      handler(newParticipants) {
        this.participants = [...newParticipants];
      },
    },
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

.folder-toggle {
  float: inline-end;
  cursor: pointer;
}

.table td {
  vertical-align: middle;
}

.table-responsive {
  padding: 20px;
  overflow: scroll;
  height: 450px;
  -ms-overflow-style: none; /* 인터넷 익스플로러 */
  scrollbar-width: none; /* 파이어폭스 */
}

/* ( 크롬, 사파리, 오페라, 엣지 ) 동작 */
.table-responsive:-webkit-scrollbar {
  display: none;
}

.table .folder-children {
  padding-left: 20px; /* 하위 항목의 들여쓰기 */
}

.input-search {
  border-width: 0 0 1px;
  border-radius: 0;
}
</style>
