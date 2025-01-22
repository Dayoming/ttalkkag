<template>
  <div
    class="d-flex justify-content-center align-items-center vh-100 bg-body-tertiary"
  >
    <div class="signup-form p-4">
      <h1 class="h3 mb-3 fw-normal">Sign Up</h1>

      <!-- 에러 메시지 -->
      <div v-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="register">
        <div v-if="isLoading" class="text-center text-secondary mb-2">
          Loading...
          <div class="spinner-border" role="status">
            <span v-if="isLoading" class="visually-hidden">Loading...</span>
          </div>
        </div>

        <div class="input-group mb-3">
          <input
            type="email"
            class="form-control"
            placeholder="이메일을 입력해 주세요."
            v-model="email"
            @keyup.enter="sendCode"
          />
          <button
            class="btn btn-dark"
            type="button"
            @click="sendCode"
            :disabled="isLoading"
          >
            코드 전송
          </button>
        </div>

        <div class="mb-3">
          <input
            type="text"
            class="form-control"
            id="verificationCode"
            placeholder="입력하신 이메일로 전송된 코드를 입력해 주세요."
            v-model="verificationCode"
          />
        </div>

        <div class="mb-3">
          <input
            type="password"
            class="form-control"
            id="password"
            placeholder="비밀번호를 입력해 주세요."
            v-model="password"
          />
        </div>

        <div class="mb-3">
          <input
            type="password"
            class="form-control"
            id="confirmPassword"
            placeholder="비밀번호를 한 번 더 입력해 주세요."
            v-model="confirmPassword"
          />
        </div>

        <button class="btn btn-dark w-100 py-2" type="submit">OK</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: "SignUpForm",
  data() {
    return {
      email: "",
      verificationCode: "",
      password: "",
      confirmPassword: "",
      errorMessage: "",
      isLoading: false,
    };
  },
  methods: {
    async sendCode() {
      // 이메일 정규식 패턴
      const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      if (!this.email) {
        this.errorMessage = "이메일을 입력해 주세요.";
        return;
      }

      if (!emailPattern.test(this.email)) {
        this.errorMessage = "이메일 형식이 일치하지 않습니다.";
        return;
      }

      // 오류 메시지 초기화
      this.errorMessage = "";
      this.isLoading = true;

      try {
        const response = await this.$axios.post("/api/auth/send-code", {
          email: this.email,
        });

        if (response.data.errorMessage) {
          this.errorMessage = response.data.errorMessage;
          return;
        }

        alert(response.data.message);
      } catch (error) {
        this.errorMessage = "코드 전송 중 오류가 발생했습니다.";
      } finally {
        this.isLoading = false;
      }
    },
    async register() {
      if (this.email === "") {
        this.errorMessage = "이메일을 입력해주세요.";
        return;
      }

      if (this.verificationCode === "") {
        this.errorMessage = "코드를 입력해주세요.";
        return;
      }

      if (this.password === "") {
        this.errorMessage = "비밀번호를 입력해주세요.";
        return;
      }

      if (this.confirmPassword === "") {
        this.errorMessage = "비밀번호를 한 번 더 입력해주세요.";
        return;
      }

      if (this.password !== this.confirmPassword) {
        this.errorMessage =
          "비밀번호가 일치하지 않습니다.\n다시 입력해 주세요.";
        this.password = "";
        this.confirmPassword = "";
        return;
      }

      await this.$axios
        .post("/api/auth/register", {
          email: this.email,
          password: this.password,
          code: this.verificationCode,
        })
        .then((response) => {
          if (response.data.errorMessage) {
            this.errorMessage = response.data.errorMessage;
            return;
          } else {
            alert(response.data.message);
            this.$router.push("/login");
          }
        });
    },
  },
};
</script>

<style scoped>
.form-floating {
  font-size: small;
}

h1 {
  text-align: center;
}

.vh-100 {
  height: 100vh;
}

.signup-form {
  max-width: 400px;
  font-size: small;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e4e4e4;
}

.signup-form input {
  font-size: small;
}

.form-floating label {
  font-size: 0.9rem;
}

button {
  font-size: 0.9rem;
}

.spinner-border {
  width: 1rem;
  height: 1rem;
}
</style>
