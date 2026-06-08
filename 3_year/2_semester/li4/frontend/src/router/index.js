import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import LoginPage from '../views/LoginPage.vue';
import MainLayout from '../views/MainLayout.vue';
import POSScreen from '../views/POSScreen.vue';
import Dashboard from '../views/Dashboard.vue';
import ProductsPage from '../views/ProductsPage.vue';
import SuppliersPage from '../views/SuppliersPage.vue';
import EmployeesPage from '../views/EmployeesPage.vue';
import ReportsPage from '../views/ReportsPage.vue';
import StatisticsPage from '../views/StatisticsPage.vue';
import InvoicesPage from '../views/InvoicesPage.vue';
import OrdersPage from '../views/OrdersPage.vue';
import AccessDeniedPage from '../views/AccessDeniedPage.vue';
import HelpPage from '../views/HelpPage.vue';

const routes = [
  {
    path: '/',
    redirect: '/app/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage
  },
  {
    path: '/app',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { requiredRole: 'Gerente de Loja' }
      },
      {
        path: 'pos',
        name: 'POS',
        component: POSScreen,
        meta: { requiredRole: 'Operador de Caixa', requiresPOSAccess: true }
      },
      {
        path: 'products',
        name: 'Products',
        component: ProductsPage,
        meta: { requiredRole: 'Operador de Caixa' }
      },
      {
        path: 'suppliers',
        name: 'Suppliers',
        component: SuppliersPage,
        meta: { requiredRole: 'Gerente de Loja' }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: OrdersPage,
        meta: { requiredRole: 'Gerente de Loja' }
      },
      {
        path: 'employees',
        name: 'Employees',
        component: EmployeesPage,
        meta: { requiredRole: 'Gerente de Loja' }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: ReportsPage,
        meta: { requiredRole: 'Gerente de Loja' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: StatisticsPage,
        meta: { requiredRole: 'Gerente de Loja' }
      },
      {
        path: 'invoices',
        name: 'Invoices',
        component: InvoicesPage,
        meta: { requiredRole: 'Operador de Caixa' }
      },
      {
        path: 'help',
        name: 'Help',
        component: HelpPage,
        meta: { requiredRole: 'Operador de Caixa' }
      },
      {
        path: '',
        redirect: () => {
          const auth = useAuthStore();
          return auth.defaultLandingPath || '/app/dashboard';
        }
      }
    ]
  },
  {
    path: '/access-denied',
    name: 'AccessDenied',
    component: AccessDeniedPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  const isAuthenticated = auth.isAuthenticated;

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
    return;
  }

  if (to.path === '/login' && isAuthenticated) {
    next(auth.defaultLandingPath);
    return;
  }

  const requiredRole = to.meta?.requiredRole;
  if (requiredRole && !auth.hasAccess(requiredRole)) {
// Logging removed
    next('/access-denied');
    return;
  }

  if (to.meta?.requiresPOSAccess && !auth.canAccessPOS) {
    next('/access-denied');
    return;
  }

  next();
});

export default router;
