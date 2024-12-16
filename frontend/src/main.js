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

let isRefreshing = false; // 리프레시 토큰 요청 플래그
let failedQueue = []; // 실패한 요청 저장 큐

// 실패한 요청을 큐에 저장하고 재시도하는 함수
const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (token) {
      prom.resolve(token);
    } else {
      prom.reject(error);
    }
  });
  failedQueue = [];
};

axios.interceptors.response.use(
  (response) => {
    const spinner = document.getElementById('global-spinner');
    if (spinner) spinner.style.display = 'none';

    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    const spinner = document.getElementById('global-spinner');
    if (spinner) spinner.style.display = 'none';

    if (error.response.status === 401 && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        router.push('/login');
        return Promise.reject(error);
      }

      // 중복된 리프레시 토큰 요청 방지
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers['Authorization'] = `Bearer ${token}`;
          return axios(originalRequest);
        });
      }

      originalRequest._retry = true; // 재시도 플래그 설정
      isRefreshing = true;

      try {
        const { data } = await axios.post('/api/auth/refresh-token', { refreshToken });

        // 새 액세스 토큰 저장
        localStorage.setItem('accessToken', data.accessToken);
        axios.defaults.headers.common['Authorization'] = `Bearer ${data.accessToken}`;

        processQueue(null, data.accessToken); // 큐에 저장된 요청 재시도
        return axios(originalRequest); // 원래 요청 재시도
      } catch (refreshError) {
        processQueue(refreshError, null); // 큐에 저장된 요청 모두 실패 처리
        console.error('리프레시 토큰으로 재발급 실패:', refreshError);
        router.push('/login');
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// 모든 요청에서 Authorization 헤더에 추가하도록 axios 설정
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    
    // /api/auth/refresh-token 요청은 Authorization 헤더를 추가하지 않음
    if (token && config.url !== '/api/auth/refresh-token') {
      config.headers.Authorization = `Bearer ${token}`;
    }

    const spinner = document.getElementById('global-spinner');
    if (spinner) spinner.style.display = 'block';

    return config;
}, error => {
  const spinner = document.getElementById('global-spinner');
  if (spinner) spinner.style.display = 'none';
    return Promise.reject(error);
});

app.use(router);
app.use(BootstrapVue3);
app.use(Colors);
app.mount('#app');
