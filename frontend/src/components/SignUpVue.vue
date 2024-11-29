<template>
  <div
    class="d-flex justify-content-center align-items-center vh-100 bg-body-tertiary"
  >
    <div class="signup-form p-4">
      <h1 class="h3 mb-3 fw-normal">Sign Up</h1>
      <form @submit.prevent="register">
        <div class="input-group mb-3">
          <input
            type="email"
            class="form-control"
            placeholder="이메일을 입력해 주세요."
            v-model="email"
            required
          />
          <button class="btn btn-dark" type="button" @click="sendCode">
            코드 전송
          </button>
        </div>

        <div class="form-floating mb-3">
          <input
            type="text"
            class="form-control"
            id="verificationCode"
            placeholder="입력하신 이메일로 전송된 코드를 입력해 주세요."
            v-model="verificationCode"
            required
          />
          <label for="verificationCode">전송된 코드를 입력해 주세요. </label>
        </div>

        <div class="form-floating mb-3">
          <input
            type="password"
            class="form-control"
            id="password"
            placeholder="비밀번호를 입력해 주세요."
            v-model="password"
            required
          />
          <label for="password">비밀번호를 입력해 주세요.</label>
        </div>

        <div class="form-floating mb-3">
          <input
            type="password"
            class="form-control"
            id="confirmPassword"
            placeholder="비밀번호를 한 번 더 입력해 주세요."
            v-model="confirmPassword"
            required
          />
          <label for="confirmPassword"
            >비밀번호를 한 번 더 입력해 주세요.</label
          >
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
    };
  },
  methods: {
    async sendCode() {
      if (!this.email) {
        alert("이메일을 입력해 주세요.");
        return;
      }
      await this.$axios.post('/api/auth/send-code', { email: this.email })
      .then((response) => {
        alert(response.data.message);
      });
    },
    async register() {
        if (this.password !== this.confirmPassword) {
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }

        await this.$axios.post('/api/auth/register', {
            email: this.email,
            password: this.password,
            code: this.verificationCode,
        })
        .then((response) => {
          if (response.data.errorMessage) {
            alert(response.data.errorMessage);
            return;
          }
          alert(response.data.message);
          this.$router.push('/login');
        });
    },
  },
};
</script>

<style scoped>
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

.form-floating label {
  font-size: 0.9rem;
}

button {
  font-size: 0.9rem;
}
</style>
