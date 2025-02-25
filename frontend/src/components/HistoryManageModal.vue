<template>
  <div>
    <!-- 모달 -->
    <div class="modal fade show d-block" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-xl" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              이력 관리
              <span class="badge rounded-pill text-bg-dark">{{
                apiHistory.length
              }}</span>
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="$emit('close')"
            ></button>
          </div>
          <div class="modal-body d-flex">
            <!-- API 변경 이력 목록 -->
            <div class="history-list me-3">
              <!-- 이력 전체보기, 유저별 보기 선택 -->
              <select class="form-select mb-2" v-model="selectedUser" @change="filterHistory">
                <option value="all">ALL</option>
                <option v-for="user in userList" :key="user" :value="user">
                  {{ user }}
                </option>
              </select>
              <ul class="list-group">
                <li
                  v-for="api in filteredHistory"
                  :key="api.id"
                  class="list-group-item d-flex justify-content-between align-items-center"
                  :class="{ active: selectedApi?.id === api.id }"
                  @click="selectApi(api)"
                >
                  <img
                    class="profile-img"
                    alt="사용자 프로필"
                    :src="parsedProfileImageUrl(api.profileImage)"
                  />
                  <span>
                    <strong>{{ api.name }}</strong>
                    <span class="badge bg-dark ms-2">{{ api.method }}</span>
                    <small>{{ api.email }}</small>
                  </span>
                  <small class="text-muted list-group-item-date">{{
                    api.savedAt
                  }}</small
                  ><br />
                </li>
              </ul>
            </div>

            <!-- API 상세 정보 -->
            <div v-if="selectedApi" class="api-details flex-grow-1">
              <input
                type="text"
                class="form-control mb-4 input-api-name"
                v-model="selectedApi.name"
                readonly
              />
              <p style="margin-bottom: 0">METHOD</p>
              <div class="d-flex flex-wrap flex-md-nowrap align-items-center">
                <input
                  type="text"
                  class="form-control"
                  v-model="selectedApi.method"
                  style="width: 20%; margin-right: 5px"
                  readonly
                />

                <input
                  type="text"
                  class="form-control"
                  v-model="selectedApi.url"
                  readonly
                />
              </div>
              <!-- 아코디언 스타일로 Headers, Query Parameter, Body 정리 -->
              <div class="accordion mt-3" id="apiDetailsAccordion">
                <!-- Headers -->
                <div class="accordion-item">
                  <h2 class="accordion-header" id="headersHeading">
                    <button
                      class="accordion-button"
                      type="button"
                      data-bs-toggle="collapse"
                      data-bs-target="#headersCollapse"
                      aria-expanded="true"
                    >
                      Headers
                    </button>
                  </h2>
                  <div
                    id="headersCollapse"
                    class="accordion-collapse collapse show"
                  >
                    <div class="accordion-body">
                      <table class="table table-bordered">
                        <thead class="table-light">
                          <tr>
                            <th>Key</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="(header, index) in parsedHeaders"
                            :key="index"
                          >
                            <td>{{ header.key }}</td>
                            <td class="text-wrap">{{ header.value }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>

                <!-- Query Parameter -->
                <div class="accordion-item">
                  <h2 class="accordion-header" id="queryHeading">
                    <button
                      class="accordion-button collapsed"
                      type="button"
                      data-bs-toggle="collapse"
                      data-bs-target="#queryCollapse"
                    >
                      Query Parameters
                    </button>
                  </h2>
                  <div id="queryCollapse" class="accordion-collapse collapse">
                    <div class="accordion-body">
                      <p v-if="parsedQueryParameters.length === 0">
                        No Query Parameters
                      </p>
                      <table v-else class="table table-bordered">
                        <thead class="table-light">
                          <tr>
                            <th>Key</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="(param, index) in parsedQueryParameters"
                            :key="index"
                          >
                            <td>{{ param.key }}</td>
                            <td class="text-wrap">{{ param.value }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>

                <!-- Body -->
                <div class="accordion-item">
                  <h2 class="accordion-header" id="bodyHeading">
                    <button
                      class="accordion-button collapsed"
                      type="button"
                      data-bs-toggle="collapse"
                      data-bs-target="#bodyCollapse"
                    >
                      Body
                    </button>
                  </h2>
                  <div id="bodyCollapse" class="accordion-collapse collapse">
                    <div class="accordion-body">
                      <pre class="bg-light p-3 rounded">{{
                        selectedApi.body
                      }}</pre>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="applyApi()">적용</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 모달 배경 -->
    <div v-if="showModal" class="modal-backdrop fade show"></div>
  </div>
</template>

<script>
export default {
  name: "HistoryManageModal",
  props: {
    apiData: Object,
  },
  data() {
    return {
      showModal: false,
      selectedUser: "all",
      selectedApi: null,
      apiHistory: [],
    };
  },
  computed: {
    userList() {
      return [...new Set(this.apiHistory.map(api => api.email))];
    },
    filteredHistory() {
        if (this.selectedUser === "all") {
            return this.apiHistory;
        }
        return this.apiHistory.filter(api => api.email === this.selectedUser);
    },
    parsedHeaders() {
      try {
        return JSON.parse(this.selectedApi?.headers || "[]");
      } catch (error) {
        console.error("Invalid JSON format for headers:", error);
        return [];
      }
    },
    parsedQueryParameters() {
      try {
        return JSON.parse(this.selectedApi?.queryParameters || "[]");
      } catch (error) {
        console.error("Invalid JSON format for queryParameters:", error);
        return [];
      }
    },
  },
  methods: {
    async fetchApiHistory() {
      console.log(this.apiData);
      const response = await this.$axios.get(
        `/api/apis/change-history/${this.apiData.id}`
      );
      this.apiHistory = response.data.apiChangeHistoryList;
      console.log(this.apiHistory);
    },
    selectApi(api) {
      this.selectedApi = api;
    },
    parsedProfileImageUrl(profileImage) {
      try {
        let profileImageUrl = "";
        if (profileImage !== null) {
          profileImageUrl = `${process.env.VUE_APP_SERVER_IP}${profileImage}`;
        } else {
          profileImageUrl = `${process.env.VUE_APP_SERVER_IP}/uploads/profiles/profile-default-icon.png`;
        }
        return profileImageUrl;
      } catch (error) {
        console.error("Failed load profile Image", error);
      }
    },
    applyApi() {
        this.$emit('load-api', this.selectedApi);
        this.$emit('close');
    },
  },
  mounted() {
    this.fetchApiHistory();
  },
};
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1040;
}

.history-list {
  width: 350px;
  overflow-y: auto;
}

.profile-img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 8px;
}

/* 테이블 스타일 */
.table {
  margin-bottom: 0;
}

.api-details {
  flex-grow: 1;
}

.input-api-name {
  border-width: 0 0 1px;
  border-radius: 0;
}

.list-group-item {
  cursor: pointer;
}

.list-group-item-date {
  margin-left: 60px;
}

.list-group-item.active {
  background-color: #f2f2f2;
  color: black;
}

.accordion-button:not(.collapsed) {
  background-color: #ffffff;
  color: black;
}
</style>
