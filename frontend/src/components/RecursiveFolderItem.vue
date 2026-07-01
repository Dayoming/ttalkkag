<template>
  <tr draggable="true" :class="isDrop ? 'top-line' : ''" @dragstart="handleDragStart" @dragover.prevent="handleDragOver"
    @dragleave="handleDragLeave" @drop.stop="handleDrop">
    <td :style="{ paddingLeft: `${depth * 20}px` }" :class="isHighlighted ? 'saved-folder' : ''">
      <!-- 수정 모드일 때 -->
      <input v-if="isEditing" v-model="editingName" @blur="saveEdit(localItem)" @keyup.enter="saveEdit(localItem)"
        class="form-control form-control-sm" autofocus />
      <!-- 폴더 아이콘 -->
      <span v-else-if="localItem.type === 'folder'" @mousedown="startLongPress" @mouseup="clearLongPress"
        @mouseleave="clearLongPress" @click="toggleFolder" class="toggle-class">
        <i class="bi bi-folder-fill"></i>
        {{ localItem.name }}
        <i v-if="localItem.isProjectsVue && projectAuth === 'write'" class="bi bi-plus" @click="addFolder"></i>
        <i v-if="localItem.isProjectsVue && projectAuth === 'write'" class="bi bi-trash-fill" @click="deleteFolder"></i>
      </span>
      <!-- API 아이콘 -->
      <span v-else-if="localItem.type === 'api'" @mousedown="startLongPress" @mouseup="clearLongPress"
        @mouseleave="clearLongPress" @click="editApiFile(localItem.id)">
        <i :class="isSelected ? 'bi bi-check-lg' : 'bi bi-file-earmark'"></i>
        {{ localItem.name }}
        <i v-if="localItem.isProjectsVue && projectAuth === 'write'" class="bi bi-trash-fill"
          @click="deleteFile($event)"></i>
      </span>
      <span class="user-icons" v-if="apiSelections[localItem.id]">
        <span v-for="user in apiSelections[localItem.id]" :key="user.id" class="user-icon-wrapper">
          <img :src="user.profileImageUrl" class="profile-img" :alt="user.email" :title="user.email" />
        </span>
      </span>
    </td>
    <td v-if="localItem.type === 'api' && localItem.isProjectsVue" class="url-column">
      {{ localItem.apiUrl }}
    </td>
    <td v-if="localItem.type === 'api' && localItem.isProjectsVue" class="method-column">
      <span class="badge text-bg-dark">{{ localItem.apiMethod }}</span>
    </td>
  </tr>
  <!-- 재귀적으로 하위 항목 렌더링 -->
  <tr v-if="localItem.isOpen && localItem.children.length">
    <td colspan="3" class="child-table-cell">
      <table class="child-table">
        <tbody>
          <RecursiveFolderItem v-for="child in localItem.children" :key="child.id" :item="child" :depth="depth + 1"
            :savedItemId="savedItemId" :selected-file-id="selectedFileId" :projectAuth="projectAuth"
            :apiSelections="apiSelections" :profileImageUrl="profileImageUrl" @selection-change="onChildSelectionChange"
            @toggle-folder="onToggleFolder" @update-items="$emit('update-items')" @api-selected="handleApiSelected" />
        </tbody>
      </table>
    </td>
  </tr>
</template>

<script>
export default {
  name: "RecursiveFolderItem",
  props: {
    item: Object,
    depth: {
      type: Number,
      default: 0, // 들여쓰기 깊이
    },
    selectedFileId: Number, // 선택된 파일 ID
    savedItemId: Number, // 저장한 Item
    projectAuth: String,
    profileImageUrl: String,
    apiSelections: {
      type: Object,
      required: true, // 반드시 필요한 props로 설정
    },
    selectedProject: Object,
  },
  data() {
    return {
      localItem: { ...this.item }, // props를 로컬 데이터로 복사
      isEditing: false,
      isSaving: false,
      isDrop: false,
      pressTimer: null, // 길게 누르기 타이머
      editingName: "", // 수정한 이름
      isHighlighted: false,
      highlightTimer: null,
    };
  },
  computed: {
    isSelected() {
      // 현재 파일이 선택된 상태인지 확인
      return this.localItem.id === this.selectedFileId;
    },
  },
  watch: {
    savedItemId: {
      handler(newId) {
        this.handleSavedItemIdChange(newId);
      },
    },
  },
  methods: {
    handleDragStart(event) {
      if (this.projectAuth === "read") {
        alert("폴더나 파일 이동 권한이 없습니다.");
        return;
      }
      event.dataTransfer.setData("draggedItemId", this.localItem.id);
      event.dataTransfer.setData("draggedParentId", this.localItem.parentId);
      // 드래그 중인 요소 스타일 설정
      event.dataTransfer.effectAllowed = "move";
    },
    handleDragOver(event) {
      event.preventDefault(); // 드롭 허용
      event.dataTransfer.dropEffect = "move";

      this.isDrop = true;
    },
    async handleDrop(event) {
      this.isDrop = false;

      const draggedItemId = event.dataTransfer.getData("draggedItemId");
      const targetParentId = this.localItem.parentId; // 드롭된 대상의 parent_id
      const targetOrder = this.localItem.itemOrder; // 드롭된 대상의 item_order

      if (!draggedItemId || draggedItemId === this.localItem.id) {
        return; // 드래그된 항목이 없거나 동일한 항목일 경우 무시
      }

      try {
        if (this.localItem.type === "api") {
          // 서버에 순서 업데이트 요청
          await this.$axios.patch("/api/projects/update/itemOrder", {
            draggedItemId,
            targetParentId,
            targetOrder,
          });
          this.$emit("update-items"); // 부모 컴포넌트에 갱신 요청
          return;
        }
      } catch (error) {
        console.error("순서 변경 중 오류 발생:", error);
      }

      // 드롭 대상이 바깥 영역인 경우 parent_id를 null로 설정
      if (!this.localItem || !this.localItem.id) {
        try {
          await this.$axios.patch(`/api/projects/update/parentId`, {
            id: Number(draggedItemId),
            parentId: null,
          });
          this.$emit("update-items"); // 부모 컴포넌트에 갱신 요청
          return;
        } catch (error) {
          console.error("바깥 영역 드롭 중 오류 발생:", error);
          alert("이동 중 오류가 발생했습니다.");
        }
      }

      // 폴더에 드래그한 경우
      if (this.localItem.type === "folder") {
        // 드래그된 파일이나 폴더의 parent_id를 이 폴더의 id로 업데이트
        try {
          await this.$axios.patch(`/api/projects/update/parentId`, {
            id: Number(draggedItemId),
            parentId: this.localItem.id,
          });
          this.$emit("update-items"); // 부모 컴포넌트에 갱신 요청
          return;
        } catch (error) {
          console.error("드래그 앤 드롭 중 오류 발생:", error);
        }
      }
    },
    handleDragLeave() {
      this.isDrop = false; // 드래그가 벗어났을 때 초기화
    },
    handleApiSelected(selectedTempApi) {
      this.$emit("api-selected", selectedTempApi);
    },
    async handleSavedItemIdChange(newId) {
      try {
        const response = await this.$axios.get(`/api/projects/item/${newId}`, {
          headers: {
            MID: "P03002",
          },
        });

        // 부모 ID가 현재 폴더의 ID와 일치하면 하이라이트
        if (response.data.parentId === this.localItem.id) {
          this.isHighlighted = true;

          // 3초 후 하이라이트 제거
          setTimeout(() => {
            this.isHighlighted = false;
          }, 3000);
        }
      } catch (error) {
        console.error("Error fetching item by ID:", error);
      }
    },
    async toggleFolder() {
      if (this.localItem.type !== "folder") return;

      if (!this.localItem.isOpen) {
        // 폴더를 열고 하위 항목 데이터 로드
        try {
          const response = await this.$axios.get(
            `/api/projects/items/${this.localItem.id}`
          );

          // 하위 항목 데이터 설정
          const childrenWithApiInfo = await Promise.all(
            response.data.map(async (child) => {
              if (child.type === "api") {
                try {
                  // API 정보 가져오기
                  const apiResponse = await this.$axios.get(
                    `/api/apis/${child.id}`
                  );
                  return {
                    ...child,
                    apiUrl: apiResponse.data.api.url || "",
                    apiMethod: apiResponse.data.api.method,
                    apiId: apiResponse.data.api.id,
                    isProjectsVue: this.localItem.isProjectsVue,
                  };
                } catch (error) {
                  console.error(`API 정보 로드 실패 (ID: ${child.id}):`, error);
                  return {
                    ...child,
                    isProjectsVue: this.localItem.isProjectsVue,
                  }; // 기본 데이터 반환
                }
              } else {
                return {
                  ...child,
                  isProjectsVue: this.localItem.isProjectsVue,
                }; // API가 아닌 경우 그대로 반환
              }
            })
          );

          this.localItem.children = childrenWithApiInfo;
        } catch (error) {
          console.error("하위 항목을 불러오는 중 오류 발생:", error);
        }
      }

      this.localItem.isOpen = !this.localItem.isOpen; // 폴더 열기/닫기 상태 토글
      this.$emit("toggle-folder", this.localItem);
      this.$emit("selection-change", this.localItem);
    },
    async addFolder() {
      const folderName = prompt("새 폴더 이름을 입력하세요: ");
      if (!folderName) {
        alert("폴더 이름을 입력해주세요.");
        return;
      }

      try {
        await this.$axios.post(`/api/projects/add-folder`, {
          projectId: this.localItem.projectId,
          parentId: this.localItem.id,
          name: folderName,
          depth: this.localItem.depth + 1,
        });
        this.$emit("update-items");
      } catch (error) {
        console.error("폴더 추가 중 오류가 발생했습니다:", error);
      }
    },
    async editApiFile(itemId) {
      try {
        const response = await this.$axios.get(`/api/apis/${itemId}`);
        if (response.data.errorMessage) {
          alert(response.data.errorMessage);
          return;
        }
        this.$emit("api-selected", response.data.api); // 부모로 선택 api 정보 전송
        this.$emit("selection-change", this.localItem); // 부모로 선택 이벤트 전송
      } catch (error) {
        console.log("Failed load api: " + error);
      }
    },
    async deleteFolder() {
      event.stopPropagation();
      if (!confirm("선택된 항목과 모든 하위 항목을 삭제하시겠습니까?")) {
        return;
      }

      try {
        // 서버에 삭제 요청
        await this.$axios.delete(
          `/api/projects/items/folder/${this.localItem.id}`
        );
        alert("삭제되었습니다.");
        this.$emit("update-items");
        this.fetchItems();
      } catch (error) {
        console.error("삭제 중 오류가 발생했습니다:", error);
      }
    },
    async deleteFile(event) {
      event.stopPropagation(); // 이벤트 전파 방지
      try {
        // 서버에 삭제 요청
        await this.$axios.delete(`/api/projects/items/${this.localItem.id}`);
        alert("삭제되었습니다.");
        this.$emit("update-items");
        this.fetchItems();
      } catch (error) {
        console.error("삭제 중 오류가 발생했습니다:", error);
      }
    },
    onSelectionChange() {
      // 선택 상태 변경 이벤트를 부모로 전달
      this.$emit("selection-change", this.localItem);
    },
    onChildSelectionChange(updatedChild) {
      // 하위 항목의 선택 상태를 업데이트하고 부모로 이벤트 전달
      const childIndex = this.localItem.children.findIndex(
        (child) => child.id === updatedChild.id
      );
      if (childIndex !== -1) {
        this.localItem.children.splice(childIndex, 1, updatedChild);
      }
      this.$emit("selection-change", updatedChild);
    },
    startLongPress() {
      this.pressTimer = setTimeout(() => {
        this.isEditing = true; // 수정 모드 활성화
      }, 2000); // 2초 이상 누르면 수정 모드
    },
    clearLongPress() {
      clearTimeout(this.pressTimer); // 길게 누르기 취소
    },
    async saveEdit(item) {
      if (this.isSaving) return; // 이미 실행 중이면 중단
      this.isSaving = true; // 실행 중 플래그 설정

      if (this.editingName === "") {
        alert("폴더나 파일 이름을 입력해 주세요.");
        this.isEditing = false;
        return;
      }

      if (this.projectAuth === "read") {
        alert("폴더나 파일명 수정 권한이 없습니다.");
        console.log("Recur");
        this.isEditing = false;
        return;
      }

      this.isEditing = false; // 수정 모드 종료

      // 변경된 이름을 project item에 적용
      const response = await this.$axios.patch(
        "/api/projects/update/projectItemName",
        {
          name: this.editingName,
          id: Number(item.id),
          projectId: Number(this.selectedProject.id)
        }
      );

      if (response.data.errorMessage) {
        alert(response.data.errorMessage);
        return;
      }
      this.$emit("update-items");
    },
  },
};
</script>

<style scoped>
.toggle-class {
  cursor: pointer;
}

span {
  cursor: pointer;
}

.toggle-check {
  margin-right: 10px;
}

.saved-folder {
  background-color: #f2f2f2;
  transition: background-color 1s ease;
}

/* 테이블 전체 레이아웃 고정 */
table {
  table-layout: fixed;
  width: 100%;
}

/* 열 스타일 */
td {
  padding: 8px;
  vertical-align: middle;
}

.child-table {
  width: 100%;
  table-layout: fixed;
  margin: 0;
  border-spacing: 0;
}

.child-table-cell {
  padding: 0;
}

.name-column {
  width: 40%;
  /* API명/폴더명 열 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.url-column {
  width: 40%;
  /* URL 열 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.method-column {
  width: 20%;
  /* METHOD 열 */
}

.profile-img {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.top-line {
  border-top: 2px solid #007bff;
  /* 선 색상과 두께 */
}
</style>
