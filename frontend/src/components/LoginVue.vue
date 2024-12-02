<template>
  <div
    class="d-flex justify-content-center align-items-center vh-100 bg-body-tertiary"
  >
    <main class="form-signin w-100 m-auto">
      <form @submit.prevent="login">
        <h1 class="h3 mb-3 fw-normal">Sign in</h1>

        <div class="form-floating">
          <input
            type="email"
            class="form-control"
            id="floatingInput"
            placeholder="name@example.com"
            v-model="email"
          />
          <label for="floatingInput">이메일을 입력해 주세요.</label>
        </div>
        <div class="form-floating">
          <input
            type="password"
            class="form-control"
            id="floatingPassword"
            placeholder="Password"
            v-model="password"
          />
          <label for="floatingPassword">비밀번호를 입력해 주세요.</label>
        </div>
        <div class="mt-2 mb-2 sign-up-link">
          <router-link to="/sign-up">Sign up</router-link>
        </div>
        <button type="submit" class="btn btn-dark w-100 py-2">Login</button>
      </form>
    </main>
  </div>
</template>

<script>
export default {
  name: "LoginVue",
  data() {
    return {
      email: "",
      password: "",
    };
  },
  methods: {
    async login() {
      const response = await this.$axios.post("/api/auth/login", {
        email: this.email,
        password: this.password,
      });

      if (response.data.errorMessage) {
        alert(response.data.errorMessage);
        return;
      }

      localStorage.setItem("accessToken", response.data.accessToken);
      localStorage.setItem("refreshToken", response.data.refreshToken);
      this.$router.push("/test-api");
    },
  },
};
</script>

<style scoped>
html,
body {
  height: 100%;
  font-size: small;
}

h1 {
  text-align: center;
}

.sign-up-link {
  text-align: right;
}

.d-flex {
  display: flex;
}

.vh-100 {
  height: 100vh;
}

.form-signin {
  max-width: 330px;
  padding: 1rem;
  border: 1px solid #e4e4e4;
  background-color: white;
  border-radius: 10px;
}

.form-signin .form-floating:focus-within {
  z-index: 2;
}

.form-signin input[type="email"] {
  margin-bottom: -1px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}

.form-signin input[type="password"] {
  margin-bottom: 10px;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}
</style>
