<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-7xl mx-auto space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p class="text-sm text-gray-600 mt-1">Visão geral do desempenho da loja</p>
        </div>
        <Button v-if="authStore.canEditData" @click="forceSync" :disabled="isSyncing" class="bg-blue-600 hover:bg-blue-700 text-white flex items-center gap-2">
          <svg v-if="isSyncing" class="animate-spin size-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" class="size-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12,18A6,6 0 0,1 6,12C6,11 6.25,10.03 6.7,9.2L5.24,7.74C4.46,8.97 4,10.43 4,12A8,8 0 0,0 12,20V23L16,19L12,15M12,4V1L8,5L12,9V6A6,6 0 0,1 18,12C18,13 17.75,13.97 17.3,14.8L18.76,16.26C19.54,15.03 20,13.57 20,12A8,8 0 0,0 12,4Z" /></svg>
          {{ isSyncing ? 'A sincronizar...' : 'Forçar Sincronização' }}
        </Button>
      </div>

      <!-- KPI Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Today's Revenue -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">Vendas Hoje</p>
              <p class="text-2xl font-bold text-gray-900 mt-1">€{{ todayRevenue.toFixed(2) }}</p>
              <p v-if="statsStore.kpis.receitaOntem > 0" :class="['text-xs mt-1 flex items-center gap-1', revenueDiff >= 0 ? 'text-green-600' : 'text-red-600']">
                <svg v-if="revenueDiff >= 0" xmlns="http://www.w3.org/2000/svg" class="size-3" viewBox="0 0 24 24" fill="currentColor"><path d="M16 6L18.29 8.29L13.41 13.17L9.41 9.17L2 16.59L3.41 18L9.41 12L13.41 16L19.71 9.71L22 12V6H16Z" /></svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" class="size-3" viewBox="0 0 24 24" fill="currentColor"><path d="M16 18L18.29 15.71L13.41 10.83L9.41 14.83L2 7.41L3.41 6L9.41 12L13.41 8L19.71 14.29L22 12V18H16Z" /></svg>
                {{ revenueDiff >= 0 ? '+' : '' }}{{ revenueDiff.toFixed(1) }}% vs ontem
              </p>
              <p v-else class="text-xs text-gray-400 mt-1">S/ dados ontem</p>
            </div>
            <div class="size-12 rounded-full bg-green-100 flex items-center justify-center">
              <CurrencyEur class="size-6 text-green-600" />
            </div>
          </div>
        </div>

        <!-- Today's Transactions -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">Transações Hoje</p>
              <p class="text-2xl font-bold text-gray-900 mt-1">{{ todaySalesCount }}</p>
              <p v-if="statsStore.kpis.vendasOntem > 0" :class="['text-xs mt-1 flex items-center gap-1', salesDiff >= 0 ? 'text-green-600' : 'text-red-600']">
                <svg v-if="salesDiff >= 0" xmlns="http://www.w3.org/2000/svg" class="size-3" viewBox="0 0 24 24" fill="currentColor"><path d="M16 6L18.29 8.29L13.41 13.17L9.41 9.17L2 16.59L3.41 18L9.41 12L13.41 16L19.71 9.71L22 12V6H16Z" /></svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" class="size-3" viewBox="0 0 24 24" fill="currentColor"><path d="M16 18L18.29 15.71L13.41 10.83L9.41 14.83L2 7.41L3.41 6L9.41 12L13.41 8L19.71 14.29L22 12V18H16Z" /></svg>
                {{ salesDiff >= 0 ? '+' : '' }}{{ salesDiff.toFixed(1) }}% vs ontem
              </p>
              <p v-else class="text-xs text-gray-400 mt-1">S/ dados ontem</p>
            </div>
            <div class="size-12 rounded-full bg-blue-100 flex items-center justify-center">
              <CartOutline class="size-6 text-blue-600" />
            </div>
          </div>
        </div>

        <!-- Total Products -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">Total de Produtos</p>
              <p class="text-2xl font-bold text-gray-900 mt-1">{{ totalProducts }}</p>
              <p class="text-xs text-gray-500 mt-1">
                {{ lowStockCount }} com stock baixo
              </p>
            </div>
            <div class="size-12 rounded-full bg-purple-100 flex items-center justify-center">
              <PackageVariantClosed class="size-6 text-purple-600" />
            </div>
          </div>
        </div>

        <!-- Average Transaction -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">Ticket Médio</p>
              <p class="text-2xl font-bold text-gray-900 mt-1">€{{ averageTransaction.toFixed(2) }}</p>
              <p class="text-xs text-green-600 mt-1 flex items-center gap-1">
                <svg xmlns="http://www.w3.org/2000/svg" class="size-3" viewBox="0 0 24 24" fill="currentColor"><path d="M16 6L18.29 8.29L13.41 13.17L9.41 9.17L2 16.59L3.41 18L9.41 12L13.41 16L19.71 9.71L22 12V6H16Z" /></svg>
                +5.1% vs ontem
              </p>
            </div>
            <div class="size-12 rounded-full bg-orange-100 flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="size-6 text-orange-600" viewBox="0 0 24 24" fill="currentColor"><path d="M22 21H2V3H4V19H6V10H10V19H12V6H16V19H18V14H22V21Z" /></svg>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts and Top Products Section -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Sales Chart -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900">Vendas da Semana</h2>
            <div class="flex items-center gap-4 text-xs">
              <div class="flex items-center gap-1.5">
                <div class="size-3 rounded-full bg-[#16a34a]"></div>
                <span class="text-gray-600">Vendas (€)</span>
              </div>
              <div class="flex items-center gap-1.5">
                <div class="size-3 rounded-full bg-[#3b82f6]"></div>
                <span class="text-gray-600">Transações</span>
              </div>
            </div>
          </div>
          <div class="h-[230px] flex items-center justify-center overflow-hidden">
            <div class="w-full flex justify-center ml-[55px]">
              <CustomLineChart
                :data="formattedChartData"
                :width="650"
                :height="220"
              />
            </div>
          </div>

          <!-- Sub-widgets: Payment Methods, Pending Orders, Active Promotions -->
          <div class="mt-6 pt-4 border-t border-gray-100 space-y-4">
            <!-- Payment Methods -->
            <div>
              <div class="flex items-center justify-between mb-2">
                <h3 class="text-sm font-semibold text-gray-700">Métodos de Pagamento</h3>
                <span class="text-xs text-gray-500">{{ paymentTotalCount }} vendas</span>
              </div>
              <div class="space-y-1.5">
                <div
                  v-for="item in paymentBreakdownPct"
                  :key="item.name"
                  class="flex items-center gap-2"
                >
                  <span class="text-xs text-gray-700 w-20 truncate">{{ item.name }}</span>
                  <div class="flex-1 bg-gray-100 rounded-full h-2 overflow-hidden">
                    <div
                      class="h-full rounded-full"
                      :style="{ width: item.pct + '%', backgroundColor: item.color }"
                    ></div>
                  </div>
                  <span class="text-xs font-semibold text-gray-700 w-10 text-right">{{ item.pct }}%</span>
                </div>
                <div v-if="paymentBreakdownPct.length === 0" class="text-xs text-gray-400 italic">
                  Sem vendas registadas.
                </div>
              </div>
            </div>

            <!-- Pending Orders -->
            <div>
              <div class="flex items-center justify-between mb-2">
                <h3 class="text-sm font-semibold text-gray-700">Encomendas em Curso</h3>
                <span class="text-xs font-semibold text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">
                  {{ pendingOrders.length }}
                </span>
              </div>
              <div class="space-y-1.5 max-h-[110px] overflow-y-auto">
                <div
                  v-for="o in pendingOrders.slice(0, 4)"
                  :key="o.id"
                  class="flex items-center justify-between text-xs py-1.5 px-2 bg-gray-50 rounded"
                >
                  <span class="truncate flex-1 text-gray-700">{{ o.supplier }}</span>
                  <span class="font-semibold text-blue-700 ml-2">€{{ Number(o.total || 0).toFixed(2) }}</span>
                </div>
                <div v-if="pendingOrders.length === 0" class="text-xs text-gray-400 italic">
                  Sem encomendas pendentes.
                </div>
              </div>
            </div>

            <!-- Active Promotions -->
            <div>
              <div class="flex items-center justify-between mb-2">
                <h3 class="text-sm font-semibold text-gray-700">Promoções Ativas</h3>
                <span class="text-xs font-semibold text-purple-600 bg-purple-50 px-2 py-0.5 rounded-full">
                  {{ activePromotions.length }}
                </span>
              </div>
              <div class="space-y-1.5 max-h-[110px] overflow-y-auto">
                <div
                  v-for="promo in activePromotions.slice(0, 4)"
                  :key="promo.idPromocao || promo.id"
                  class="flex items-center justify-between text-xs py-1.5 px-2 bg-gray-50 rounded"
                >
                  <span class="truncate flex-1 text-gray-700">{{ promo.designacao || 'Promoção' }}</span>
                  <span class="font-semibold text-purple-700 ml-2">-{{ Math.round(Number(promo.desconto ?? promo.discount ?? 0)) }}%</span>
                </div>
                <div v-if="activePromotions.length === 0" class="text-xs text-gray-400 italic">
                  Sem promoções ativas.
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Inventory Alert Widget -->
        <div class="bg-white rounded-lg border border-gray-200 p-6 flex flex-col">
          <div class="flex items-center gap-3 mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="size-6 text-orange-600" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2M13 17H11V15H13V17M13 13H11V7H13V13Z" /></svg>
            <div>
              <h2 class="text-lg font-semibold text-gray-900">Alerta de Inventário</h2>
              <p class="text-sm text-gray-600">Produtos com stock baixo ou crítico</p>
            </div>
          </div>
          
          <div class="flex-1 overflow-y-auto space-y-3 pr-2">
            <div
              v-for="product in lowStockProducts"
              :key="product.id"
              class="flex items-center justify-between p-3 rounded-lg border"
              :class="[
                product.stock <= product.minStock / 2 
                  ? 'bg-red-50 border-red-100' 
                  : 'bg-orange-50 border-orange-100'
              ]"
            >
              <div class="flex items-center gap-3">
                <span class="text-xl">{{ product.image }}</span>
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ product.name }}</p>
                  <p 
                    class="text-xs font-medium"
                    :class="[
                      product.stock <= product.minStock / 2 
                        ? 'text-red-600' 
                        : 'text-orange-600'
                    ]"
                  >
                    Stock: {{ product.stock }} (Mín: {{ product.minStock }})
                  </p>
                </div>
              </div>
              <Button 
                v-if="authStore.canManageSupply"
                size="sm" 
                variant="outline" 
                class="text-xs h-8"
                :class="[
                  product.stock <= product.minStock / 2 
                    ? 'border-red-200 text-red-700 hover:bg-red-100' 
                    : 'border-orange-200 text-orange-700 hover:bg-orange-100'
                ]"
                @click="orderProduct(product)"
              >
                Encomendar Agora
              </Button>
            </div>
            <div v-if="lowStockProducts.length === 0" class="text-center py-8 text-gray-500 italic text-sm">
              Nenhum produto com stock baixo.
            </div>
          </div>
        </div>
      </div>

      <!-- Second Row: Top Products and Transactions -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Top Products -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Produtos Mais Vendidos</h2>
          <div class="space-y-3">
            <div
              v-if="topProducts.length === 0"
              class="text-center py-10 text-gray-500 italic text-sm"
            >
              Nenhuma venda registada para gerar o top de produtos.
            </div>
            <div
              v-for="(product, index) in topProducts"
              :key="product.name"
              class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg"
            >
              <div class="size-8 rounded-full bg-green-600 text-white flex items-center justify-center font-bold text-sm">
                {{ index + 1 }}
              </div>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">{{ product.name }}</p>
                <p class="text-xs text-gray-500">{{ product.sales }} vendas</p>
              </div>
              <p class="text-sm font-bold text-green-600">€{{ product.revenue.toFixed(2) }}</p>
            </div>
          </div>
        </div>

        <!-- Recent Transactions -->
        <div class="lg:col-span-2 bg-white rounded-lg border border-gray-200 p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Transações Recentes</h2>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b border-gray-200">
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">ID</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Data/Hora</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Total</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Pagamento</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="sale in recentSales.slice(0, 12)"
                  :key="sale.id"
                  class="border-b border-gray-100 hover:bg-gray-50 cursor-pointer"
                  @click="openTransactionOverlay(sale)"
                >
                  <td class="py-3 px-4 text-sm text-gray-900">{{ sale.id }}</td>
                  <td class="py-3 px-4 text-sm text-gray-600">{{ formatDate(sale.date) }}</td>
                  <td class="py-3 px-4 text-sm font-medium text-green-600">€{{ Number(sale.total || 0).toFixed(2) }}</td>
                  <td class="py-3 px-4 text-sm">
                    <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-700">
                      {{ sale.paymentMethod }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>

    <!-- Transaction Overlay -->
    <Dialog :open="!!selectedTransaction" @update:open="selectedTransaction = null">
      <div class="p-6 max-w-lg">
        <DialogHeader>
          <DialogTitle>Detalhes da Transação #{{ selectedTransaction?.id }}</DialogTitle>
          <DialogDescription>{{ formatDate(selectedTransaction?.date) }}</DialogDescription>
        </DialogHeader>
        
        <div class="py-4 space-y-4">
          <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
            <span class="text-sm text-gray-600">Total</span>
            <span class="text-xl font-bold text-green-600">€{{ Number(selectedTransaction?.total || 0).toFixed(2) }}</span>
          </div>
          
          <div class="space-y-2">
            <p class="text-xs font-bold text-gray-400 uppercase">Itens</p>
            <div class="max-h-[200px] overflow-y-auto space-y-2">
              <div v-for="item in selectedTransaction?.items" :key="item.id" class="flex justify-between text-sm">
                <span>{{ item.produto?.nome || 'Produto' }} (x{{ item.quantidade }})</span>
                <span class="font-medium">€{{ Number(item.totalFinal || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="selectedTransaction = null">Fechar</Button>
          <Button class="bg-blue-600 text-white" @click="reprintReceipt">Reimprimir Recibo</Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Order Dialog -->
    <Dialog :open="showOrderDialog" @update:open="showOrderDialog = $event">
      <div class="p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-4">Encomendar Produto</h2>
        <div v-if="selectedProduct" class="space-y-4">
          <div class="p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div class="flex items-center gap-3">
              <span class="text-3xl">{{ selectedProduct.image }}</span>
              <div>
                <p class="font-bold text-gray-900">{{ selectedProduct.name }}</p>
                <p class="text-sm text-gray-600">Fornecedor: {{ selectedProduct.supplier }}</p>
                <p class="text-xs text-gray-500">Stock Atual: {{ selectedProduct.stock }}</p>
              </div>
            </div>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Quantidade a Encomendar</label>
            <Input type="number" v-model="orderQuantity" min="1" class="w-full" />
          </div>

          <div class="flex justify-end gap-3 mt-6">
            <Button variant="outline" @click="showOrderDialog = false">Cancelar</Button>
            <Button class="bg-green-600 hover:bg-green-700 text-white" @click="confirmOrder">
              Confirmar Encomenda
            </Button>
          </div>
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useProductsStore } from '../stores/products';
import { useSalesStore } from '../stores/sales';
import { useOrdersStore } from '../stores/orders';
import { useStatisticsStore } from '../stores/statistics';
import { useAuthStore } from '../stores/auth';
import { useShopStore } from '../stores/shop';
import { localApiClient, globalApiClient } from '../services/apiClients';

const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
const client = isGlobal ? globalApiClient : localApiClient;
import { toast } from 'vue-sonner';
import { format, isValid } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import CustomLineChart from '../components/CustomLineChart.vue';
import { usePromotionsStore } from '../stores/promotions';
import Dialog from '../components/ui/Dialog.vue';
import Button from '../components/ui/Button.vue';
import Input from '../components/ui/Input.vue';
import CurrencyEur from 'vue-material-design-icons/CurrencyEur.vue';
import CartOutline from 'vue-material-design-icons/CartOutline.vue';
import PackageVariantClosed from 'vue-material-design-icons/PackageVariantClosed.vue';

const productsStore = useProductsStore();
const ordersStore = useOrdersStore();
const promotionsStore = usePromotionsStore();
const authStore = useAuthStore();
const statsStore = useStatisticsStore();
const salesStore = useSalesStore();
const shopStore = useShopStore();

const isSyncing = ref(false);
const selectedTransaction = ref(null);

const openTransactionOverlay = (sale) => {
  selectedTransaction.value = sale;
};

const reprintReceipt = async () => {
    if (!selectedTransaction.value) return;
    try {
        const idFatura = selectedTransaction.value.numFatura || selectedTransaction.value.id;
        const response = await client.get(`/faturas/${encodeURIComponent(idFatura)}/pdf`, { responseType: 'blob' });
        const url = window.URL.createObjectURL(response);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `SegundaVia_${idFatura}.pdf`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        toast.success('Segunda via gerada.');
    } catch (error) {
        toast.error('Erro ao gerar segunda via.');
    }
};

const forceSync = async () => {
  isSyncing.value = true;
  try {
    await client.post('/sincronizacao/executar', {});
    toast.success('Sincronização iniciada com sucesso!');
  } catch (error) {
    console.error('Sync error:', error);
    toast.error('Erro ao iniciar sincronização.');
  } finally {
    isSyncing.value = false;
  }
};

const loadDashboardData = async () => {
  await Promise.allSettled([
    productsStore.fetchProducts(),
    salesStore.fetchSales(),
    ordersStore.fetchOrders(),
    statsStore.fetchAll(),
    promotionsStore.fetchPromotions(),
  ]);
};

onMounted(() => {
  loadDashboardData();
});

watch(
  () => shopStore.selectedShopId,
  () => {
    loadDashboardData();
  }
);

const showOrderDialog = ref(false);
const selectedProduct = ref(null);
const orderQuantity = ref(10);
const asNumber = (value) => {
  const n = Number(value);
  return Number.isFinite(n) ? n : 0;
};

const orderProduct = (product) => {
  selectedProduct.value = product;
  orderQuantity.value = 10;
  showOrderDialog.value = true;
};

const confirmOrder = async () => {
  try {
    await ordersStore.addOrder(selectedProduct.value.id, orderQuantity.value);
    toast.success(`Encomenda de ${orderQuantity.value} unidades de ${selectedProduct.value.name} realizada!`);
    showOrderDialog.value = false;
  } catch (error) {
    toast.error('Erro ao realizar encomenda.');
  }
};

const todayRevenue = computed(() => asNumber(statsStore.kpis.receitaHoje));
const todaySalesCount = computed(() => asNumber(statsStore.kpis.vendasHoje));
const totalProducts = computed(() => productsStore.products.length);
const lowStockProducts = computed(() => {
  return [...productsStore.getLowStockProducts()].sort((a, b) => {
    const isCriticalA = a.stock <= a.minStock / 2;
    const isCriticalB = b.stock <= b.minStock / 2;
    if (isCriticalA && !isCriticalB) return -1;
    if (!isCriticalA && isCriticalB) return 1;
    return 0;
  });
});
const pendingOrders = computed(() => ordersStore.orders.filter(o => o.status === 'Pendente'));
const lowStockCount = computed(() => lowStockProducts.value.length);

const averageTransaction = computed(() => {
  if (todaySalesCount.value === 0) return 0;
  return todayRevenue.value / todaySalesCount.value;
});

const revenueDiff = computed(() => {
  const hoje = asNumber(statsStore.kpis.receitaHoje);
  const ontem = asNumber(statsStore.kpis.receitaOntem);

  if (ontem === 0) {
    return hoje > 0 ? 100 : 0;
  }
  return ((hoje - ontem) / ontem) * 100;
});

const salesDiff = computed(() => {
  const hoje = asNumber(statsStore.kpis.vendasHoje);
  const ontem = asNumber(statsStore.kpis.vendasOntem);

  if (ontem === 0) {
    return hoje > 0 ? 100 : 0;
  }
  return ((hoje - ontem) / ontem) * 100;
});

const recentSales = computed(() => {
  return [...salesStore.sales]
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .slice(0, 12);
});

const weekData = computed(() => {
  if (statsStore.monthlySales.length > 0) {
    return statsStore.monthlySales.map(d => ({ 
      name: d.mes, 
      sales: d.totalVendas, 
      transactions: d.totalTransacoes 
    }));
  }
  return [
    { name: 'Seg', sales: 0, transactions: 0 },
    { name: 'Ter', sales: 0, transactions: 0 },
    { name: 'Qua', sales: 0, transactions: 0 },
    { name: 'Qui', sales: 0, transactions: 0 },
    { name: 'Sex', sales: 0, transactions: 0 },
    { name: 'Sáb', sales: 0, transactions: 0 },
    { name: 'Dom', sales: 0, transactions: 0 }
  ];
});

const formattedChartData = computed(() => [
  {
    title: 'Vendas (€)',
    values: weekData.value.map(d => ({ x: d.name, y: d.sales }))
  },
  {
    title: 'Transações',
    values: weekData.value.map(d => ({ x: d.name, y: d.transactions }))
  }
]);

const topProducts = computed(() => {
  const productSales = {};
  
  salesStore.sales.forEach(sale => {
    const lines = sale.items || sale.linhas || [];
    lines.forEach(line => {
      const productId = line.idProduto || line.productId || line.produto?.idProduto || line.produto?.id;
      if (!productId) return;
      const productName = line.produto?.nome || productsStore.products.find(p => p.id === productId)?.name || productId;
      const qty = Number(line.quantidade || 0);
      const revenue = Number(line.subtotal || line.totalFinal || (qty * (line.preco || 0)));
      
      if (!productSales[productId]) {
        productSales[productId] = { name: productName, sales: 0, revenue: 0 };
      }
      productSales[productId].sales += qty;
      productSales[productId].revenue += revenue;
    });
  });

  return Object.values(productSales)
    .sort((a, b) => b.sales - a.sales)
    .slice(0, 10);
});

const paymentTotalCount = computed(() =>
  salesStore.sales.reduce((acc, s) => acc + (s.isDevolucao ? 0 : 1), 0)
);

const paymentBreakdownPct = computed(() => {
  const buckets = {};
  for (const sale of salesStore.sales) {
    if (sale.isDevolucao) continue;
    const method = sale.paymentMethod || 'Numerário';
    buckets[method] = (buckets[method] || 0) + 1;
  }
  const total = Object.values(buckets).reduce((a, b) => a + b, 0);
  if (total === 0) return [];
  const colors = { 'Numerário': '#16a34a', 'Cartão': '#3b82f6' };
  const palette = ['#16a34a', '#3b82f6', '#f97316', '#a855f7', '#eab308'];
  return Object.entries(buckets)
    .sort((a, b) => b[1] - a[1])
    .map(([name, value], i) => ({
      name,
      pct: Math.round((value / total) * 100),
      color: colors[name] || palette[i % palette.length],
    }));
});

const activePromotions = computed(() => promotionsStore.getActivePromotions || []);

const ordersByStatus = computed(() => {
  const buckets = {};
  for (const o of ordersStore.orders) {
    const s = o.status || '—';
    buckets[s] = (buckets[s] || 0) + 1;
  }
  return buckets;
});

const formatDate = (date) => {
  if (!date) return 'N/A';
  const parsedDate = new Date(date);
  if (!isValid(parsedDate)) return 'N/A';
  return format(parsedDate, 'dd/MM/yyyy HH:mm', { locale: ptBR });
};
</script>
