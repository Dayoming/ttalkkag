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
app.use(router);
app.use(BootstrapVue3);
app.use(Colors);
app.mount('#app');
