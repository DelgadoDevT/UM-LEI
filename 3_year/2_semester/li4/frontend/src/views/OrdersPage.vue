<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-7xl mx-auto space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Gestão de Encomendas</h1>
          <p class="text-sm text-gray-600 mt-1">Acompanhamento e receção de mercadorias (Pipeline de Abastecimento)</p>
        </div>
        <Button
          v-if="authStore.canEditData"
          @click="openNewOrderDialog"
          class="bg-blue-600 hover:bg-blue-700"
        >
          <Plus class="size-4 mr-2" />
          Nova Encomenda
        </Button>
      </div>

      <!-- KPI Cards -->
      <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-medium text-gray-500 uppercase tracking-wider">Total</span>
            <div class="size-9 rounded-lg bg-gray-100 flex items-center justify-center">
              <CartCheck class="size-5 text-gray-600" />
            </div>
          </div>
          <p class="text-3xl font-bold text-gray-900">{{ orders.length }}</p>
          <p class="text-xs text-gray-500 mt-1">Encomendas no sistema</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-medium text-blue-600 uppercase tracking-wider">Pendentes</span>
            <div class="size-9 rounded-lg bg-blue-50 flex items-center justify-center">
              <ClockOutline class="size-5 text-blue-600" />
            </div>
          </div>
          <p class="text-3xl font-bold text-blue-700">{{ counts.pendente }}</p>
          <p class="text-xs text-gray-500 mt-1">Aguardam validação</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-medium text-orange-600 uppercase tracking-wider">Em Trânsito</span>
            <div class="size-9 rounded-lg bg-orange-50 flex items-center justify-center">
              <TruckOutline class="size-5 text-orange-600" />
            </div>
          </div>
          <p class="text-3xl font-bold text-orange-700">{{ counts.enviada }}</p>
          <p class="text-xs text-gray-500 mt-1">A caminho da loja</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-medium text-green-600 uppercase tracking-wider">Concluídas</span>
            <div class="size-9 rounded-lg bg-green-50 flex items-center justify-center">
              <CheckCircleOutline class="size-5 text-green-600" />
            </div>
          </div>
          <p class="text-3xl font-bold text-green-700">{{ counts.concluida }}</p>
          <p class="text-xs text-gray-500 mt-1">Recebidas e fechadas</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-medium text-purple-600 uppercase tracking-wider">Valor Aberto</span>
            <div class="size-9 rounded-lg bg-purple-50 flex items-center justify-center">
              <CurrencyEur class="size-5 text-purple-600" />
            </div>
          </div>
          <p class="text-3xl font-bold text-purple-700">€{{ openValue.toFixed(0) }}</p>
          <p class="text-xs text-gray-500 mt-1">Compromissos pendentes</p>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-lg border border-gray-200 p-4 flex flex-wrap gap-4">
        <div class="flex-1 min-w-[200px]">
          <label class="text-xs font-medium text-gray-500 uppercase mb-1 block">Pesquisar</label>
          <div class="relative">
            <Magnify class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-gray-400" />
            <Input v-model="searchTerm" placeholder="ID ou Fornecedor..." class="pl-10" />
          </div>
        </div>
        <div class="w-48">
          <label class="text-xs font-medium text-gray-500 uppercase mb-1 block">Estado</label>
          <Select v-model="statusFilter">
            <option value="all">Todos os Estados</option>
            <option value="Rascunho">Rascunho</option>
            <option value="Pendente">Pendente</option>
            <option value="Enviada">Enviada</option>
            <option value="Concluída">Concluída</option>
            <option value="Cancelada">Cancelada</option>
          </Select>
        </div>
      </div>

      <!-- Orders Table -->
      <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="text-left py-3 px-4 text-xs font-semibold text-gray-500 uppercase">ID</th>
              <th class="text-left py-3 px-4 text-xs font-semibold text-gray-500 uppercase">Produto</th>
              <th class="text-left py-3 px-4 text-xs font-semibold text-gray-500 uppercase">Fornecedor</th>
              <th class="text-left py-3 px-4 text-xs font-semibold text-gray-500 uppercase">Total</th>
              <th class="text-left py-3 px-4 text-xs font-semibold text-gray-500 uppercase">Estado</th>
              <th class="text-right py-3 px-4 text-xs font-semibold text-gray-500 uppercase">Ações</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="order in filteredOrders" :key="order.id" class="hover:bg-gray-50 transition-colors">
              <td class="py-4 px-4 text-sm font-medium text-gray-900">#{{ order.id.slice(0, 5) }}</td>
              <td class="py-4 px-4 text-sm text-gray-900 font-medium">{{ order.productName }}</td>
              <td class="py-4 px-4 text-sm text-gray-600">{{ order.supplier }}</td>
              <td class="py-4 px-4 text-sm font-semibold text-gray-900">€{{ order.total?.toFixed(2) }}</td>
              <td class="py-4 px-4">
                <span :class="getStatusBadgeClass(order.status)">
                  {{ order.status }}
                </span>
              </td>
              <td class="py-4 px-4 text-right">
                <div class="inline-flex items-center justify-end gap-2 min-w-[180px]">
                  <Button variant="ghost" size="sm" class="h-8 w-8 p-0" @click="viewDetails(order)" title="Ver Detalhes">
                    <EyeOutline :size="18" class="text-gray-600" />
                  </Button>

                  <!-- Pipeline Actions -->
                  <Button
                    v-if="authStore.canEditData && order.status !== 'Concluída' && order.status !== 'Cancelada'"
                    size="sm"
                    :class="['h-8 inline-flex items-center justify-center gap-1.5', getNextActionClass(order.status)]"
                    @click="advanceOrder(order)"
                  >
                    <component :is="getNextActionIcon(order.status)" :size="14" />
                    <span class="leading-none">{{ getNextActionLabel(order.status) }}</span>
                  </Button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredOrders.length === 0">
              <td colspan="6" class="py-12 text-center text-gray-500 italic">
                Nenhuma encomenda encontrada.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Order Details Dialog -->
    <Dialog :open="!!selectedOrder" @update:open="selectedOrder = null">
      <div class="p-6 max-w-2xl">
        <DialogHeader>
          <DialogTitle>Detalhes da Encomenda #{{ selectedOrder?.id }}</DialogTitle>
          <DialogDescription>Criada em {{ formatDate(selectedOrder?.date) }}</DialogDescription>
        </DialogHeader>
        
        <div class="py-4 space-y-6">
          <div class="grid grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg border border-gray-100">
            <div>
              <p class="text-xs font-semibold text-gray-400 uppercase">Fornecedor</p>
              <p class="text-sm font-medium text-gray-900">{{ selectedOrder?.supplier }}</p>
            </div>
            <div>
              <p class="text-xs font-semibold text-gray-400 uppercase">Estado Atual</p>
              <span :class="getStatusBadgeClass(selectedOrder?.status || '')">
                {{ selectedOrder?.status }}
              </span>
            </div>
          </div>

          <div class="space-y-3">
            <p class="text-xs font-semibold text-gray-400 uppercase">Itens da Encomenda</p>
            <div class="border rounded-lg overflow-hidden">
              <table class="w-full text-sm">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left py-2 px-3">Produto</th>
                    <th class="text-center py-2 px-3">Qtd</th>
                    <th class="text-right py-2 px-3">P. Custo</th>
                    <th class="text-right py-2 px-3">Subtotal</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  <tr v-for="line in selectedOrder?.lines" :key="line.idProduto">
                    <td class="py-2 px-3">{{ getProductName(line.idProduto) }}</td>
                    <td class="py-2 px-3 text-center">{{ line.quantidade }}</td>
                    <td class="py-2 px-3 text-right">€{{ Number(line.precoCusto ?? line.precoCustoAplicado ?? 0).toFixed(2) }}</td>
                    <td class="py-2 px-3 text-right font-medium">€{{ (Number(line.quantidade ?? 0) * Number(line.precoCusto ?? line.precoCustoAplicado ?? 0)).toFixed(2) }}</td>
                  </tr>
                </tbody>
                <tfoot class="bg-gray-50 text-sm">
                  <tr>
                    <td colspan="3" class="py-2 px-3 text-right text-gray-600">Subtotal (sem IVA):</td>
                    <td class="py-2 px-3 text-right">€{{ orderBreakdown.subtotal.toFixed(2) }}</td>
                  </tr>
                  <tr>
                    <td colspan="3" class="py-2 px-3 text-right text-gray-600">IVA ({{ orderIvaPctLabel }}):</td>
                    <td class="py-2 px-3 text-right">€{{ orderBreakdown.iva.toFixed(2) }}</td>
                  </tr>
                  <tr class="font-bold">
                    <td colspan="3" class="py-2 px-3 text-right">Total:</td>
                    <td class="py-2 px-3 text-right">€{{ orderBreakdown.total.toFixed(2) }}</td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="selectedOrder = null">Fechar</Button>
          <Button 
            v-if="authStore.canEditData && selectedOrder?.status !== 'Concluída' && selectedOrder?.status !== 'Cancelada'"
            :class="getNextActionClass(selectedOrder?.status)"
            @click="advanceOrder(selectedOrder)"
          >
            {{ getNextActionLabel(selectedOrder?.status) }}
          </Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- New Order Dialog -->
    <Dialog :open="showNewOrderDialog" @update:open="showNewOrderDialog = $event">
      <div class="p-6 max-w-xl">
        <DialogHeader>
          <DialogTitle>Nova Encomenda</DialogTitle>
          <DialogDescription>Selecione o fornecedor e adicione produtos.</DialogDescription>
        </DialogHeader>

        <div class="py-4 space-y-4">
          <div v-if="isAdminOrOwner">
            <Label>Loja *</Label>
            <Select v-model="newOrderForm.idLoja">
              <option v-for="shop in shops" :key="shop.id" :value="shop.id">{{ shop.name }}</option>
            </Select>
          </div>

          <div>
            <Label>Fornecedor *</Label>
            <Select v-model="newOrderForm.supplierId" @change="loadSupplierProducts">
              <option value="">Selecione um fornecedor</option>
              <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
            </Select>
          </div>

          <div v-if="newOrderForm.supplierId" class="space-y-4">
            <div class="flex gap-2 items-end">
              <div class="flex-1">
                <Label>Produto</Label>
                <Select v-model="newItem.idProduto">
                  <option value="">Selecione um produto</option>
                  <option v-for="p in supplierProducts" :key="p.idProduto" :value="p.idProduto">
                    {{ getProductName(p.idProduto) }} - €{{ p.precoCusto?.toFixed(2) }}
                  </option>
                </Select>
              </div>
              <div class="w-24">
                <Label>Qtd</Label>
                <Input type="number" v-model="newItem.quantidade" min="1" />
              </div>
              <Button @click="addItem" variant="outline" class="bg-gray-50">Add</Button>
            </div>

            <div class="border rounded-md max-h-40 overflow-y-auto">
              <table class="w-full text-xs">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left p-2">Produto</th>
                    <th class="text-center p-2">Qtd</th>
                    <th class="text-right p-2">Ação</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in newOrderForm.lines" :key="index">
                    <td class="p-2">{{ getProductName(item.idProduto) }}</td>
                    <td class="p-2 text-center">{{ item.quantidade }}</td>
                    <td class="p-2 text-right">
                      <Button variant="ghost" size="xs" @click="newOrderForm.lines.splice(index, 1)" class="text-red-600">Remover</Button>
                    </td>
                  </tr>
                  <tr v-if="newOrderForm.lines.length === 0">
                    <td colspan="3" class="p-4 text-center text-gray-400">Nenhum item adicionado</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="showNewOrderDialog = false">Cancelar</Button>
          <Button 
            class="bg-blue-600 text-white" 
            :disabled="!newOrderForm.supplierId || newOrderForm.lines.length === 0"
            @click="submitNewOrder"
          >
            Criar Encomenda
          </Button>
        </DialogFooter>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useOrdersStore } from '../stores/orders';
import { useSuppliersStore } from '../stores/suppliers';
import { useProductsStore } from '../stores/products';
import { useShopStore } from '../stores/shop';
import { useAuthStore } from '../stores/auth';
import SHOPS_DATA from '../data/shops.json';
import { localApiClient, globalApiClient } from '../services/apiClients';

const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
const client = isGlobal ? globalApiClient : localApiClient;
import { toast } from 'vue-sonner';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import Magnify from 'vue-material-design-icons/Magnify.vue';
import Plus from 'vue-material-design-icons/Plus.vue';
import EyeOutline from 'vue-material-design-icons/EyeOutline.vue';
import CheckCircleOutline from 'vue-material-design-icons/CheckCircleOutline.vue';
import TruckOutline from 'vue-material-design-icons/TruckOutline.vue';
import SendOutline from 'vue-material-design-icons/SendOutline.vue';
import ClockOutline from 'vue-material-design-icons/ClockOutline.vue';
import CartCheck from 'vue-material-design-icons/CartCheck.vue';
import CurrencyEur from 'vue-material-design-icons/CurrencyEur.vue';
import Button from '../components/ui/Button.vue';
import Input from '../components/ui/Input.vue';
import Select from '../components/ui/Select.vue';
import Label from '../components/ui/Label.vue';
import Dialog from '../components/ui/Dialog.vue';
import DialogHeader from '../components/ui/DialogHeader.vue';
import DialogTitle from '../components/ui/DialogTitle.vue';
import DialogDescription from '../components/ui/DialogDescription.vue';
import DialogFooter from '../components/ui/DialogFooter.vue';

const ordersStore = useOrdersStore();
const suppliersStore = useSuppliersStore();
const productsStore = useProductsStore();
const authStore = useAuthStore();
const shopStore = useShopStore();

const shops = SHOPS_DATA;
const isAdminOrOwner = computed(() =>
  authStore.currentUser?.role?.toUpperCase().includes('PROPRIET') ||
  authStore.currentUser?.role?.toUpperCase().includes('ADMIN')
);

const searchTerm = ref('');
const statusFilter = ref('all');
const selectedOrder = ref(null);
const showNewOrderDialog = ref(false);

const newOrderForm = ref({
  supplierId: '',
  idLoja: 1,
  lines: []
});
const newItem = ref({ idProduto: '', quantidade: 1 });
const supplierProducts = ref([]);

onMounted(() => {
  ordersStore.fetchOrders();
  suppliersStore.fetchSuppliers();
  productsStore.fetchProducts();
});

watch(
  () => shopStore.selectedShopId,
  () => {
    ordersStore.fetchOrders();
    suppliersStore.fetchSuppliers();
    productsStore.fetchProducts();
  }
);

const orders = computed(() => ordersStore.orders);
const suppliers = computed(() => suppliersStore.suppliers);
const products = computed(() => productsStore.products);

const counts = computed(() => {
  const c = { pendente: 0, enviada: 0, concluida: 0 };
  for (const o of orders.value) {
    if (o.status === 'Pendente' || o.status === 'Rascunho') c.pendente++;
    else if (o.status === 'Enviada' || o.status === 'Em Trânsito') c.enviada++;
    else if (o.status === 'Concluída' || o.status === 'Entregue') c.concluida++;
  }
  return c;
});

const openValue = computed(() =>
  orders.value
    .filter(o => o.status !== 'Concluída' && o.status !== 'Cancelada' && o.status !== 'Entregue')
    .reduce((sum, o) => sum + (Number(o.total) || 0), 0)
);

const getProductName = (id) => {
  const product = products.value?.find(p => p.id === id);
  return product ? product.name : id;
};

const ORDER_IVA_RATE = 0.23;
const orderIvaPctLabel = ref('23%');

const orderBreakdown = computed(() => {
  const order = selectedOrder.value;
  const baseSubtotal = Array.isArray(order?.lines)
    ? order.lines.reduce((acc, l) => acc + Number(l.quantidade ?? 0) * Number(l.precoCusto ?? l.precoCustoAplicado ?? 0), 0)
    : Number(order?.total ?? 0);
  const iva = baseSubtotal * ORDER_IVA_RATE;
  return {
    subtotal: baseSubtotal,
    iva,
    total: baseSubtotal + iva
  };
});

const filteredOrders = computed(() => {
  let result = orders.value;
  
  if (statusFilter.value !== 'all') {
    result = result.filter(o => o.status === statusFilter.value);
  }
  
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase();
    result = result.filter(o => 
      o.id.toLowerCase().includes(term) || 
      o.supplier.toLowerCase().includes(term)
    );
  }
  
  return [...result].sort((a, b) => new Date(b.date) - new Date(a.date));
});

const formatDate = (date) => {
  if (!date) return '---';
  return format(new Date(date), "dd/MM/yyyy HH:mm", { locale: ptBR });
};

const getStatusBadgeClass = (status) => {
  const base = 'px-2.5 py-1 rounded-full text-xs font-semibold';
  switch (status) {
    case 'Rascunho': return `${base} bg-gray-100 text-gray-600`;
    case 'Pendente': return `${base} bg-blue-100 text-blue-700`;
    case 'Enviada': return `${base} bg-orange-100 text-orange-700`;
    case 'Concluída': return `${base} bg-green-100 text-green-700`;
    case 'Cancelada': return `${base} bg-red-100 text-red-700`;
    default: return `${base} bg-gray-100 text-gray-600`;
  }
};

const getNextActionLabel = (status) => {
  switch (status) {
    case 'Rascunho': return 'Validar';
    case 'Pendente': return 'Enviar';
    case 'Enviada': return 'Receber';
    default: return '';
  }
};

const getNextActionIcon = (status) => {
  switch (status) {
    case 'Rascunho': return ClockOutline;
    case 'Pendente': return SendOutline;
    case 'Enviada': return CheckCircleOutline;
    default: return null;
  }
};

const getNextActionClass = (status) => {
  switch (status) {
    case 'Rascunho': return 'bg-gray-600 hover:bg-gray-700 text-white';
    case 'Pendente': return 'bg-blue-600 hover:bg-blue-700 text-white';
    case 'Enviada': return 'bg-green-600 hover:bg-green-700 text-white';
    default: return '';
  }
};

const viewDetails = (order) => {
  selectedOrder.value = order;
};

const advanceOrder = async (order) => {
  try {
    await ordersStore.markAsDelivered(order.id);
    toast.success(`Estado da encomenda #${order.id.slice(0,8)} atualizado.`);
    if (selectedOrder.value?.id === order.id) {
        selectedOrder.value = orders.value.find(o => o.id === order.id);
    }
  } catch (error) {
    toast.error('Erro ao atualizar estado da encomenda.');
  }
};

const openNewOrderDialog = () => {
  const currentShopId = shopStore.selectedShopId === 'all' ? 1 : Number(shopStore.selectedShopId);
  newOrderForm.value = { supplierId: '', idLoja: currentShopId, lines: [] };
  newItem.value = { idProduto: '', quantidade: 1 };
  showNewOrderDialog.value = true;
};

const loadSupplierProducts = async () => {
    if (!newOrderForm.value.supplierId) {
        supplierProducts.value = [];
        return;
    }
    try {
        const products = await client.get(`/fornecedores/${newOrderForm.value.supplierId}/produtos`);
        if (!products || products.length === 0) {
            toast.info('Nenhum produto encontrado para este fornecedor.');
            supplierProducts.value = [];
        } else {
            supplierProducts.value = products;
        }
    } catch (error) {
        toast.error('Erro ao carregar produtos do fornecedor.');
        supplierProducts.value = [];
    }
};

const addItem = () => {
    if (!newItem.value.idProduto || newItem.value.quantidade <= 0) return;
    const prod = supplierProducts.value.find(p => p.idProduto === newItem.value.idProduto);
    newOrderForm.value.lines.push({
        idProduto: newItem.value.idProduto,
        quantidade: Number(newItem.value.quantidade),
        precoCusto: prod.precoCusto
    });
    newItem.value = { idProduto: '', quantidade: 1 };
};

const submitNewOrder = async () => {
    try {
        await client.post('/encomendas', {
            idEncomenda: crypto.randomUUID(),
            idFornecedor: newOrderForm.value.supplierId,
            idLoja: newOrderForm.value.idLoja,
            linhas: newOrderForm.value.lines
        });
        toast.success('Encomenda criada com sucesso.');
        showNewOrderDialog.value = false;
        ordersStore.fetchOrders();
    } catch (error) {
        toast.error(error.body?.message || 'Erro ao criar encomenda.');
    }
};
</script>
