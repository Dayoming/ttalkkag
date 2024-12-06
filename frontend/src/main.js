import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import BootstrapVue3 from 'bootstrap-vue-3'
import Colors from 'chart.js'
import axios from './axios'
import 'bootstrap-vue-3/dist/bootstrap-vue-3.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'


const app = createApp(App);
app.config.globalProperties.$axios = axios;

axios.interceptors.response.use(
    (response) => response,
    async (error) => {
      if (error.response.status === 403) {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          try {
            const { data } = await this.$axios.post('/api/auth/refresh-token', { refreshToken });
            localStorage.setItem('accessToken', data.accessToken);
            error.config.headers['Authorization'] = `Bearer ${data.accessToken}`;
            return this.$axios(error.config); // 원래 요청 재시도
          } catch (refreshError) {
            console.error('리프레시 토큰으로 재발급 실패:', refreshError);
            this.$router.push('/login');
          }
        }
      }
      return Promise.reject(error);
    }
);

// 모든 요청에서 Authorization 헤더에 추가하도록 axios 설정
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

app.use(router);
app.use(BootstrapVue3);
app.use(Colors);
app.mount('#app');
