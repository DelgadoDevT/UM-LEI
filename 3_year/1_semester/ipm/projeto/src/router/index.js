import { createRouter, createWebHistory } from 'vue-router'

// Import view components for routing
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Maintenance from '../views/Maintenance.vue'

// Define application routes
const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/maintenance',
    name: 'Maintenance',
    component: Maintenance
  }
]

// Create and configure the router instance
const router = createRouter({
  history: createWebHistory(), // Use HTML5 History API
  routes
})

export default router