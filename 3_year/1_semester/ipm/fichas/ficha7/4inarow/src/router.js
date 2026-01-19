import { createRouter, createWebHistory } from 'vue-router';
import GamePage from './pages/GamePage.vue';
import StatisticsPage from './pages/StatisticsPage.vue';
import SimulationPage from './pages/SimulationPage.vue';
import NotFoundPage from './pages/NotFoundPage.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/game"},
    { path: "/game", component: GamePage },
    { path: "/statistics", component: StatisticsPage },
    { path: "/simulation", component: SimulationPage },
    { path: "/:notFound(.*)*", component: NotFoundPage}, // (:*)* significa "corresponder a qualquer caminho"
  ]
});

export default router;
