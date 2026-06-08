<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar Navigation -->
    <aside class="w-64 bg-white border-r border-gray-200 flex flex-col">
      <!-- Header with Logo -->
      <div class="p-4 border-b border-gray-200">
        <div class="flex flex-col items-center gap-2">
          <img 
            src="/src/assets/taki_logo.png"
            alt="Taki - Não inventes!" 
            class="h-20 w-auto object-contain"
          />
          <div class="text-center">
            <p class="text-xs text-gray-500 font-medium">Sistema de Gestão</p>
          </div>
        </div>
      </div>

      <!-- Shop Selector (apenas no nó central) -->
      <div v-if="canManageShops && isHubNode" class="p-4 border-b border-gray-200">
        <div class="flex items-center gap-2 mb-2">
          <MapMarkerOutline :size="12" class="text-gray-600" />
          <label class="text-xs font-medium text-gray-600 block">Loja Selecionada</label>
        </div>
        <Select v-model="selectedShop" class="w-full">
          <option v-if="isAdminOrOwner" value="all">Todas as Lojas</option>
          <option v-for="shop in shops" :key="shop.id" :value="shop.id">
            {{ shop.name }}
          </option>
        </Select>
      </div>
      <!-- Em modo local mostra apenas a loja activa, sem permitir troca -->
      <div v-else-if="canManageShops" class="p-4 border-b border-gray-200">
        <div class="flex items-center gap-2 mb-2">
          <MapMarkerOutline :size="12" class="text-gray-600" />
          <label class="text-xs font-medium text-gray-600 block">Loja Activa</label>
        </div>
        <div class="px-3 py-2 rounded-md border border-gray-200 bg-gray-50 text-sm text-gray-700">
          {{ activeShopName }}
        </div>
      </div>

      <!-- Navigation Links -->
      <nav class="flex-1 p-4 space-y-1 overflow-y-auto">
        <!-- POS (Operador de Caixa apenas) -->
        <router-link
          v-if="canAccessPOS"
          to="/app/pos"
          :class="getLinkClass('/app/pos')"
        >
          <StorefrontOutline class="size-5" />
          <span>Ponto de Venda</span>
        </router-link>

        <!-- Dashboard -->
        <router-link
          v-if="canAccessDashboard"
          to="/app/dashboard"
          :class="getLinkClass('/app/dashboard')"
        >
          <ViewDashboardOutline class="size-5" />
          <span>Dashboard</span>
        </router-link>

        <!-- Products -->
        <router-link
          to="/app/products"
          :class="getLinkClass('/app/products')"
        >
          <PackageVariantClosed class="size-5" />
          <span>Inventário</span>
        </router-link>

        <!-- Suppliers -->
        <router-link
          v-if="canManageSupply"
          to="/app/suppliers"
          :class="getLinkClass('/app/suppliers')"
        >
          <TruckOutline class="size-5" />
          <span>Fornecedores</span>
        </router-link>

        <!-- Orders -->
        <router-link
          v-if="canManageSupply"
          to="/app/orders"
          :class="getLinkClass('/app/orders')"
        >
          <CartCheck class="size-5" />
          <span>Encomendas</span>
        </router-link>

        <!-- Invoices -->
        <router-link
          to="/app/invoices"
          :class="getLinkClass('/app/invoices')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><line x1="10" y1="9" x2="8" y2="9"></line></svg>
          <span>Faturas</span>
        </router-link>

        <!-- Reports -->
        <router-link
          v-if="isAdminOrOwner"
          to="/app/reports"
          :class="getLinkClass('/app/reports')"
        >
          <FileChartOutline class="size-5" />
          <span>Relatórios</span>
        </router-link>

        <!-- Statistics -->
        <router-link
          v-if="canAccessStatistics"
          to="/app/statistics"
          :class="getLinkClass('/app/statistics')"
        >
          <ChartLine class="size-5" />
          <span>Estatísticas</span>
        </router-link>

        <!-- Employees below Reports for all allowed roles -->
        <router-link
          v-if="canAccessEmployees"
          to="/app/employees"
          :class="getLinkClass('/app/employees')"
        >
          <AccountMultipleOutline class="size-5" />
          <span>Funcionários</span>
        </router-link>
      </nav>

      <!-- User Profile & Network Status -->
      <div class="p-4 border-t border-gray-200 space-y-3">
        <!-- Network Status -->
        <div v-if="!isOnline" class="flex items-center gap-2 p-2 bg-red-50 rounded-lg border border-red-200">
          <WifiOff class="size-4 text-red-600" />
          <span class="text-xs text-red-700 font-medium">Sem conexão</span>
        </div>
        <div v-else class="flex items-center gap-2 p-2 bg-green-50 rounded-lg border border-green-200">
          <Wifi class="size-4 text-green-600" />
          <span class="text-xs text-green-700 font-medium">Online</span>
        </div>

        <!-- User Info -->
        <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
          <div class="size-10 rounded-full bg-green-600 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="size-5 text-white" viewBox="0 0 24 24" fill="currentColor"><path d="M12 4C14.21 4 16 5.79 16 8S14.21 12 12 12 8 10.21 8 8 9.79 4 12 4M12 14C16.42 14 20 15.79 20 18V20H4V18C4 15.79 7.58 14 12 14Z" /></svg>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-900 truncate">{{ currentUser?.name }}</p>
            <p class="text-xs text-gray-500 truncate">{{ currentUser?.role }}</p>
          </div>
        </div>

        <!-- Ajuda Button -->
        <Button
          variant="outline"
          class="w-full text-blue-600 border-blue-200 hover:bg-blue-50 justify-start"
          @click="handleHelp"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="size-4 mr-2" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2M12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20M13 16H11V14H13V16M13 12H11V6H13V12Z" /></svg>
          Ajuda
        </Button>

        <!-- Logout Button -->
        <Button
          variant="outline"
          class="w-full text-red-600 border-red-200 hover:bg-red-50 justify-start"
          @click="showLogoutDialog = true"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="size-4 mr-2" viewBox="0 0 24 24" fill="currentColor"><path d="M10.08 15.58L11.5 17L16.5 12L11.5 7L10.08 8.41L12.67 11H3V13H12.67L10.08 15.58M19 3H5C3.89 3 3 3.89 3 5V9H5V5H19V19H5V15H3V19C3 20.11 3.89 21 5 21H19C20.11 21 21 20.11 21 19V5C21 3.89 20.11 3 19 3Z" /></svg>
          Terminar Sessão
        </Button>
      </div>
    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 overflow-y-auto">
      <RouterView />
    </main>

    <!-- Logout Confirmation Dialog -->
    <Dialog :open="showLogoutDialog" @update:open="showLogoutDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Confirmar Saída</DialogTitle>
          <DialogDescription>
            Tem a certeza que deseja terminar a sessão?
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" class="text-gray-900 border-gray-300" @click="showLogoutDialog = false">
            Cancelar
          </Button>
          <Button class="bg-red-600 hover:bg-red-700 text-white" @click="handleLogout">
            Terminar Sessão
          </Button>
        </DialogFooter>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { RouterView, useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useShopStore } from '../stores/shop';
import SHOPS_DATA from '../data/shops.json';
import { toast } from 'vue-sonner';
import Wifi from 'vue-material-design-icons/Wifi.vue';
import WifiOff from 'vue-material-design-icons/WifiOff.vue';
import ViewDashboardOutline from 'vue-material-design-icons/ViewDashboardOutline.vue';
import PackageVariantClosed from 'vue-material-design-icons/PackageVariantClosed.vue';
import ChartLine from 'vue-material-design-icons/ChartLine.vue';
import AccountMultipleOutline from 'vue-material-design-icons/AccountMultipleOutline.vue';
import StorefrontOutline from 'vue-material-design-icons/StorefrontOutline.vue';
import TruckOutline from 'vue-material-design-icons/TruckOutline.vue';
import FileChartOutline from 'vue-material-design-icons/FileChartOutline.vue';
import MapMarkerOutline from 'vue-material-design-icons/MapMarkerOutline.vue';
import CartCheck from 'vue-material-design-icons/CartCheck.vue';
import Button from '../components/ui/Button.vue';
import Select from '../components/ui/Select.vue';
import Dialog from '../components/ui/Dialog.vue';
import DialogHeader from '../components/ui/DialogHeader.vue';
import DialogTitle from '../components/ui/DialogTitle.vue';
import DialogDescription from '../components/ui/DialogDescription.vue';
import DialogFooter from '../components/ui/DialogFooter.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const shopStore = useShopStore();

const isOnline = ref(true);
const showLogoutDialog = ref(false);
const shops = SHOPS_DATA;
const selectedShop = computed({
  get: () => shopStore.selectedShopId,
  set: (val) => {
    shopStore.setShop(val);
    toast.info('A carregar dados da loja...', { duration: 1000 });
    // Atualiza as stores para refletir a nova loja selecionada (idLoja na query param)
    import('../stores/products').then(m => m.useProductsStore().fetchProducts());
    import('../stores/sales').then(m => m.useSalesStore().fetchSales());
    import('../stores/employees').then(m => m.useEmployeesStore().fetchEmployees());
    import('../stores/orders').then(m => m.useOrdersStore().fetchOrders());
    import('../stores/statistics').then(m => m.useStatisticsStore().fetchAll());
  }
});

const currentUser = computed(() => authStore.currentUser);
const isAdminOrOwner = computed(() => {
  const role = (authStore.currentUser?.role || '').toUpperCase();
  return role.includes('PROPRIET') || role.includes('ADMIN') || role.includes('GESTOR_CENTRAL');
});
const canManageShops = computed(() => isAdminOrOwner.value);
const isHubNode = computed(() => authStore.isHubNode);
const activeShopName = computed(() => {
  const idLoja = authStore.currentUser?.idLoja;
  if (!idLoja) return '—';
  const found = shops.find(s => Number(s.id) === Number(idLoja));
  return found ? found.name : `Loja ${idLoja}`;
});
const canAccessPOS = computed(() => authStore.canAccessPOS);
const canAccessEmployees = computed(() => authStore.canAccessEmployees);
const canAccessDashboard = computed(() => authStore.canAccessDashboard);
const canAccessReports = computed(() => authStore.canAccessReports);
const canAccessStatistics = computed(() => authStore.canAccessStatistics);
const canManageSupply = computed(() => authStore.canManageSupply);

const getLinkClass = (path) => {
  const isActive = route.path === path || route.path.startsWith(path + '/');
  const baseClasses = 'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors';
  
  if (isActive) {
    return `${baseClasses} bg-green-50 text-green-700 border border-green-200`;
  }
  return `${baseClasses} text-gray-700 hover:bg-gray-100`;
};

const handleLogout = () => {
  authStore.logout();
  showLogoutDialog.value = false;
  toast.success('Sessão terminada com sucesso');
  router.push('/login');
};

const handleHelp = () => {
  router.push('/app/help');
};

// Network monitoring
const handleOnline = () => {
  isOnline.value = true;
  toast.success('Conexão restabelecida', {
    description: 'O sistema está novamente online'
  });
};

const handleOffline = () => {
  isOnline.value = false;
  toast.error('Sem conexão à internet', {
    description: 'Algumas funcionalidades podem estar limitadas'
  });
};

onMounted(async () => {
  // No nó local há apenas uma loja, força sempre a loja do utilizador
  if (!isHubNode.value) {
    const userShopId = authStore.currentUser?.idLoja;
    if (userShopId) {
      shopStore.setShop(userShopId);
    }
  } else if (isAdminOrOwner.value) {
    if (shopStore.selectedShopId === null || shopStore.selectedShopId === undefined) {
      shopStore.setShop('all');
    }
  } else {
    const userShopId = authStore.currentUser?.idLoja;
    if (userShopId) {
      shopStore.setShop(userShopId);
    }
  }

  window.addEventListener('online', handleOnline);
  window.addEventListener('offline', handleOffline);
});

onUnmounted(() => {
  window.removeEventListener('online', handleOnline);
  window.removeEventListener('offline', handleOffline);
});
</script>
