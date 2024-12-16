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
        props: true,
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
    const publicPages = ['/login', '/sign-up'];
    const isPublicPage = publicPages.includes(to.path);
  
    // 이미 로그인 된 상태에서 로그인 페이지로 이동하려고 하는 경우
    if (to.path === '/login' && token) {
      next('/test-api');
    }

    if (!isPublicPage && !token) {
        // 인증되지 않은 사용자가 보호된 경로로 접근 시 /login으로 리다이렉트
        return next('/login');
    }

    next(); // 다른 경우는 정상적으로 이동
});

export default router;
