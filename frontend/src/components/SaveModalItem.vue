<template>
  <li>
    <div :style="{ paddingLeft: `${depth * 10}px` }">
      <span class="folder-item">
        <i
          :class="{
            'bi bi-folder-fill': localItem.type === 'folder',
            'bi bi-file-earmark': localItem.type === 'api',
          }"
        ></i>
        <span
          v-if="localItem.type === 'folder'"
          :class="!isLoad ? 'folder-item-select-able' : ''"
          @click="!isLoad ? selectFolder(localItem) : null"
          >{{ localItem.name }}</span
        >
        <span
          v-if="localItem.type === 'api'"
          :class="isLoad ? 'api-label-click' : 'api-label'"
          @click="isLoad ? loadApi(localItem) : null"
        >
          <span class="badge text-bg-dark">
            {{ localItem.method }}
          </span>
          {{ localItem.name }}
        </span>
        <i
          :class="{
            'bi bi-chevron-down':
              !localItem.isOpen && localItem.type === 'folder',
            'bi bi-chevron-up': localItem.isOpen && localItem.type === 'folder',
          }"
          style="cursor: pointer"
          @click="handleToggle(localItem)"
        ></i>
      </span>
      <!-- 하위 항목 -->
      <ul v-show="localItem.isOpen">
        <SaveModalItem
          v-for="child in localItem.children"
          :key="child.id"
          :item="child"
          :depth="depth + 1"
          :apiData="apiData"
          @toggle-folder="handleToggle"
          @update-items="$emit('update-items')"
          @select-folder="selectFolder"
        />
      </ul>
    </div>
  </li>
</template>

<script>
export default {
  name: "SaveModalItem",
  props: {
    item: Object,
    depth: {
      type: Number,
      default: 0, // 들여쓰기 깊이
    },
    apiData: Array,
    isLoad: Boolean,
  },
  data() {
    return {
      localItem: { ...this.item },
    };
  },
  watch: {
    // props로 받은 item에 변경이 생기면 localItem을 업데이트
    item: {
      handler(newItem) {
        this.localItem = { ...newItem };
      },
      deep: true, // 객체 내부의 변화도 감지
    },
  },
  methods: {
    async handleToggle() {
      if (!this.localItem.isOpen) {
        try {
          // 하위 데이터 로드
          const response = await this.$axios.get(
            `/api/projects/items/${this.localItem.id}`
          );
          this.localItem.children = response.data.map((child) => ({
            ...child,
            isOpen: false, // 기본적으로 닫힌 상태
          }));
          console.log(this.localItem.children);
        } catch (error) {
          console.error("하위 항목을 불러오는 중 오류 발생:", error);
        }
      }
      this.localItem.isOpen = !this.localItem.isOpen; // 폴더 열기/닫기 상태 토글
      this.$emit("selection-change", this.localItem);
    },
    async selectFolder(item) {
      if (item.type === "folder") {
        try {
          const apiProjectItem = {
            projectId: item.projectId,
            parentId: item.id,
            name: this.apiData.name,
            depth: this.depth,
          };

          // 서버로 전송
          const responseNewProjectItem = await this.$axios.post(
            "/api/projects/add-api",
            apiProjectItem,
            {
              headers: {
                MID: "P03002",
              },
            }
          );

          // 저장할 데이터
          const payload = {
            ...this.apiData,
            projectId: item.projectId,
            itemId: responseNewProjectItem.data.id, // 만들어진 project_items의 id
          };

          const newApis = await this.$axios.post("/api/apis", payload);

          alert("API 정보가 저장되었습니다.");
          // 부모 컴포넌트로 저장된 api 전달
          this.$emit("project-saved", newApis);
        } catch (error) {
          console.error("API 저장 중 오류 발생:", error);
        }
      }
    },
    async loadApi(localItem) {
      try {
        const response = await this.$axios.get(`/api/apis/${localItem.id}`);
        this.$emit("load-api", response.data.api);
      } catch (error) {
        console.log(error);
      }
    },
  },
};
</script>

<style scoped>
.folder-item-select-able:hover {
  cursor: pointer;
  text-decoration: underline;
}

.folder-item i {
  margin-right: 5px;
}

.api-label .badge {
  margin-right: 5px;
}

ul {
  list-style: none;
  padding-left: 0;
}

.api-label-click {
  cursor: pointer;
}
</style>
