import { createRouter, createWebHistory } from 'vue-router';
import AdminDashboard from '@/features/admin/view/AdminDashboard.vue';
import ScaledElectionResults from "@/features/admin/view/ScaledElectionResults.vue";
import PartiesView from '@/features/admin/view/PartiesView.vue';

const routes = [
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
  },
  {
    path: '/ScaledElectionResults',
    name: 'ScaledElectionResults',
    component: ScaledElectionResults,
  },
  {
    path: '/parties',
    name: 'Parties',
    component: PartiesView,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
