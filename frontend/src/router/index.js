import { createRouter, createWebHistory } from 'vue-router';
import ApiTest from '@/components/ApiTest';
import ProjectsVue from '@/components/ProjectsVue';
import EnvironmentVue from '@/components/EnvironmentVue.vue';
import HistoryVue from '@/components/HistoryVue.vue';
import DatasetVue from '@/components/DatasetVue.vue';
import ReportsVue from '@/components/ReportsVue.vue';
import LoginVue from '@/components/LoginVue.vue';
import SignUpVue from '@/components/SignUpVue.vue';

const routes = [
    {
        path: '/login',
        name: 'LoginVue',
        component: LoginVue,
        meta: { noHeaderSidebar: true },
    },
    {
        path: '/sign-up',
        name: SignUpVue,
        component: SignUpVue,
        meta: { noHeaderSidebar: true },
    },
    {
        path: '/test-api',
        name: 'ApiTest',
        component: ApiTest,
    },
    {
        path: '/projects',
        name: 'ProjectsVue',
        component: ProjectsVue,
    },
    {
        path: '/environment',
        name: 'EnvironmentVue',
        component: EnvironmentVue,
    },
    {
        path: '/history',
        name: 'HistoryVue',
        component: HistoryVue,
    },
    {
        path: '/reports',
        name: 'ReportsVue',
        component: ReportsVue,
    },
    {
        path: '/dataset',
        name: 'DatasetVue',
        component: DatasetVue,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem("accessToken");
  
    if (to.name !== "LoginVue" && !token) {
      // 로그인 페이지로 리다이렉트
      return next({ name: "LoginVue" });
    }
    
    next(); // 다른 경우는 정상적으로 이동
});

export default router;
