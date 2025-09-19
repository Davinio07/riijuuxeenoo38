import { createRouter, createWebHistory } from 'vue-router';
import AdminDashboard from '@/features/admin/views/AdminDashboard.vue';

const routes = [
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
  },
  // Hier kunnen je teamgenoten later hun routes toevoegen
  // {
  //   path: '/elections',
  //   name: 'ElectionResults',
  //   component: () => import('@/features/elections/views/ElectionResults.vue')
  // }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
