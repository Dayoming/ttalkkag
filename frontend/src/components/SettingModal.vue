<template>
  <!-- Bootstrap 모달 -->
  <div class="modal fade show d-block" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false"
    aria-labelledby="settingsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <!-- 모달 헤더 -->
        <div class="modal-header">
          <h5 class="modal-title fw-bold" id="settingsModalLabel">Settings</h5>
          <button type="button" class="btn-close" aria-label="Close" @click="closeModal"></button>
        </div>

        <!-- 모달 본문 -->
        <div class="modal-body">
          <!-- 프로필 사진 업로드 -->
          <div class="d-flex flex-column align-items-center mb-3">
            <span>프로필 사진</span>
            <img :src="previewImage" alt="프로필 사진" class="profile-preview mb-2" />
            <input type="file" @change="handleFileUpload" accept="image/*" />
          </div>

          <!-- 응답 결과만 보기 -->
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span>응답 결과만 보기</span>
            <div class="form-check form-switch">
              <input class="form-check-input" type="checkbox" v-model="showResponse" id="viewResultOnly" />
            </div>
          </div>

          <!-- 자동 저장 -->
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span>자동 저장</span>
            <div class="form-check form-switch">
              <input class="form-check-input" type="checkbox" v-model="autoSave" id="autoSave" />
            </div>
          </div>
          <div class="ms-3">
            <!-- 입력 중 자동 저장 간격 -->
            <div class="d-flex justify-content-between align-items-center mb-3">
              <span>입력 중 자동 저장 간격</span>
              <div class="d-flex align-items-center">
                <input type="number" class="form-control form-control-sm w-25 text-end" v-model="autoSaveTime" min="1"
                  :disabled="!autoSave" @keyup.enter="confirmSettings" />
                <span class="ms-2">초</span>
              </div>
            </div>

            <!-- 입력 후 자동 저장 간격 -->
            <div class="d-flex justify-content-between align-items-center mb-3">
              <span>입력 후 자동 저장 간격</span>
              <div class="d-flex align-items-center">
                <input type="number" class="form-control form-control-sm w-25 text-end" v-model="autoSaveTerm" min="1"
                  :disabled="!autoSave" @keyup.enter="confirmSettings" />
                <span class="ms-2">초</span>
              </div>
            </div>

            <!-- 자동 저장 경로 -->
            <div class="d-flex justify-content-between align-items-center">
              <span>자동 저장 경로</span>
              <button class="btn btn-dark">경로 선택</button>
            </div>
          </div>
        </div>
        <div v-if="errorMessage" class="alert alert-danger" role="alert">
          {{ errorMessage }}
        </div>
        <!-- 모달 푸터 -->
        <div class="modal-footer">
          <button type="button" class="btn btn-dark w-100" @click="confirmSettings">
            OK
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "SettingsModal",
  data() {
    return {
      userId: null,
      showResponse: false, // 응답 결과만 보기
      autoSave: false, // 자동 저장 설정
      autoSaveTime: 0, // 입력 중 자동 저장 간격 (초)
      autoSaveTerm: 0, // 입력 후 자동 저장 간격 (초)
      savePath: 0, // 자동 저장 경로
      errorMessage: "",
      profileImage: null, // 업로드된 파일
      previewImage: null, // 기본 이미지 경로
    };
  },
  methods: {
    closeModal() {
      this.$emit("close"); // 부모 컴포넌트에 모달 닫기 이벤트 전달
    },
    async confirmSettings() {
      if (this.autoSave) {
        if (!this.autoSaveTime || this.autoSaveTime < 1) {
          this.errorMessage = "입력 중 자동 저장 간격은 1초 이상이어야 합니다.";
          return;
        }
        if (!this.autoSaveTerm || this.autoSaveTerm < 1) {
          this.errorMessage = "입력 후 자동 저장 간격은 1초 이상이어야 합니다.";
          return;
        }
      }

      const formData = new FormData();
      formData.append("showResponse", this.showResponse);
      formData.append("autoSaveUse", this.autoSave);
      formData.append("autoSaveTime", this.autoSaveTime);
      formData.append("autoSaveTerm", this.autoSaveTerm);
      formData.append("savePath", this.savePath);
      if (this.profileImage) {
        formData.append("profileImage", this.profileImage);
      }

      try {
        const response = await this.$axios.patch(
          "/api/user/setting",
          formData,
          {
            headers: {
              "Content-Type": "multipart/form-data",
            },
          }
        );
        if (response.data.errorMessage) {
          alert(response.data.errorMessage);
          return;
        }
        this.$emit("modal-setting-confirm");
        alert(response.data.message);
        this.closeModal();
      } catch (error) {
        console.error("Failed to update settings:", error);
      }
    },
    handleFileUpload(event) {
      const file = event.target.files[0];
      console.log(file);
      if (file) {
        this.profileImage = file;

        // 서버에 파일 업로드
        const formData = new FormData();
        formData.append("profileImage", file);
        formData.append("userId", this.userId);

        this.$axios
          .post("/api/user/upload-profile", formData, {
            headers: {
              "Content-Type": "multipart/form-data",
            },
          })
          .then((response) => {
            console.log(response.data);
            // 서버에서 반환된 URL로 미리보기 업데이트
            this.previewImage = response.data.profileImageUrl ?
              process.env.VUE_APP_SERVER_IP + response.data.profileImageUrl :
              process.env.VUE_APP_SERVER_IP +
              "/uploads/profiles/profile-default-icon.png";
            console.log("Uploaded profile image URL:", this.previewImage);
          })
          .catch((error) => {
            console.error("Failed to upload profile image:", error);
          });
      }
    },
    async fetchUserSetting() {
      try {
        const response = await this.$axios.get("/api/user/findUserByEmail");
        this.userId = response.data.user.id;
        this.showResponse = response.data.user.showResponse;
        this.autoSave = response.data.user.autoSaveUse;
        this.autoSaveTime = response.data.user.autoSaveTime;
        this.autoSaveTerm = response.data.user.autoSaveTerm;
        this.savePath = response.data.user.autoSavePath;

        // 사용자 프로필 이미지 경로 설정 - 기본 로그인인 경우
        if (response.data.user.socialProvider == null) {
          this.previewImage = response.data.user.profileImage ?
            process.env.VUE_APP_SERVER_IP + response.data.user.profileImage :
            process.env.VUE_APP_SERVER_IP + "/uploads/profiles/profile-default-icon.png";
        } else {
          // 사용자 프로필 이미지 경로 설정 - 소셜 로그인인 경우
          this.previewImage = response.data.user.profileImage ?
            response.data.user.profileImage :
            process.env.VUE_APP_SERVER_IP + "/uploads/profiles/profile-default-icon.png";
        }

      } catch (error) {
        console.log("Failed to load user settings:", error);
      }
    },
  },
  mounted() {
    this.fetchUserSetting();
  },
};
</script>

<style scoped>
/* 모달 헤더와 버튼 스타일 조정 */
.modal-title {
  font-size: 1.2rem;
}

.profile-preview {
  width: 30%;
  border-radius: 50%;
}

.form-check-input {
  cursor: pointer;
}

.form-control-sm {
  font-size: 0.9rem;
}

.modal-footer {
  border-top: none;
}

.alert-danger {
  margin: 20px;
}
</style>
