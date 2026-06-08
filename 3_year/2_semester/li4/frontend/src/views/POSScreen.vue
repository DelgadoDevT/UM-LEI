<template>
  <div class="h-full bg-gray-50 p-6">
    <div class="max-w-[1800px] mx-auto h-full flex flex-col gap-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Ponto de Venda</h1>
          <p class="text-sm text-gray-600 mt-1">Operador: {{ authStore.currentUser?.name }}</p>
        </div>
        <div class="flex items-center gap-3">
          <button
              @click="openReturnModal"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium border border-gray-300 bg-white px-3 py-2 hover:bg-gray-50 h-9"
          >
            <span style="margin-right: 8px; font-style: normal;">🔄</span> Devolução
          </button>

          <button
              @click="showKeyboardHelp = true"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium border border-gray-300 bg-white px-3 py-2 hover:bg-gray-50 h-9"
          >
            <span style="margin-right: 8px; font-style: normal;">⌨️</span> Atalhos (F1)
          </button>

          <button
              @click="undoLastAction"
              :disabled="!lastAction"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium border border-gray-300 bg-white px-3 py-2 hover:bg-gray-50 h-9 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span style="margin-right: 8px; font-style: normal;">↩️</span> Desfazer (Ctrl+Z)
          </button>
        </div>
      </div>

      <div class="flex-1 grid grid-cols-3 gap-6 min-h-0">
        <div class="col-span-2 flex flex-col gap-4 min-h-0">
          <div class="bg-white rounded-lg border border-gray-200 p-4">
            <form @submit.prevent="handleBarcodeSubmit">
              <label class="text-sm font-medium text-gray-700 block mb-2">Código de Barras</label>
              <div class="flex gap-2">
                <div class="relative flex-1">
                  <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">🔍</span>
                  <Input ref="barcodeInputRef" v-model="barcode" placeholder="Digitalize o código" class="pl-11" />
                </div>
                <Button type="submit" class="bg-green-600 text-white">
                  <span style="margin-right: 8px; font-style: normal;">➕</span> Adicionar
                </Button>
              </div>
            </form>
          </div>

          <div class="bg-white rounded-lg border border-gray-200 p-4">
            <Input v-model="searchTerm" placeholder="Pesquisar produtos por nome..." class="w-full" />
          </div>

          <div class="flex-1 bg-white rounded-lg border border-gray-200 p-4 overflow-y-auto">
            <div class="grid grid-cols-4 gap-3">
              <button
                v-for="product in filteredProducts"
                :key="product.id"
                @click="addToCart(product)"
                :disabled="Number(product.stock ?? 0) <= 0"
                class="p-3 border border-gray-200 rounded-lg text-left transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-green-50"
              >
                <div class="text-4xl mb-2 flex items-center justify-center h-12">{{ product.image }}</div>
                <p class="text-sm font-medium truncate text-gray-900">{{ product.name }}</p>
                <div v-if="product.hasPromotion">
                  <p class="text-[10px] text-red-600 line-through">€{{ product.price.toFixed(2) }}</p>
                  <p class="text-base font-bold text-green-600">€{{ product.discountedPrice.toFixed(2) }} <span class="text-[10px] bg-red-100 text-red-700 px-1 rounded">-{{ product.discountPercentage }}%</span></p>
                </div>
                <p v-else class="text-base font-bold text-green-600">€{{ product.price.toFixed(2) }}</p>
                <p class="text-xs mt-1" :class="Number(product.stock ?? 0) > 0 ? 'text-gray-500' : 'text-red-600'">
                  Stock: {{ Number(product.stock ?? 0) > 0 ? product.stock : 'Sem stock' }}
                </p>
              </button>
            </div>
          </div>
        </div>

        <div class="flex flex-col gap-4 min-h-0">
          <div class="flex-1 bg-white rounded-lg border border-gray-200 p-4 flex flex-col min-h-0">
            <h2 class="text-lg font-semibold mb-4 text-gray-900">Carrinho</h2>
            <div class="flex-1 overflow-y-auto space-y-2">
              <div v-if="cart.length === 0" class="text-center py-12 text-gray-400">
                <span style="font-size: 3rem; opacity: 0.2;">🛒</span>
                <p>Carrinho vazio</p>
              </div>

              <div v-for="item in cart" :key="item.product.id" class="flex items-center gap-3 p-3 border border-gray-100 rounded-lg">
                <div class="text-2xl w-10 h-10 flex items-center justify-center">{{ item.product.image || '📦' }}</div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium truncate">{{ item.product.name }}</p>
                  <p class="text-xs text-gray-500">
                    <template v-if="item.product.hasPromotion">
                      <span class="line-through text-red-500">€{{ item.product.price.toFixed(2) }}</span>
                      <span class="text-green-600 font-semibold ml-1">€{{ item.product.discountedPrice.toFixed(2) }}</span>
                      <span class="ml-1 text-[10px] bg-red-100 text-red-700 px-1 rounded">-{{ item.product.discountPercentage }}%</span>
                    </template>
                    <template v-else>€{{ item.product.price.toFixed(2) }}</template>
                    unid.
                  </p>
                </div>

                <div class="flex items-center gap-1 bg-gray-50 border border-gray-200 rounded-full p-1">
                  <button
                      @click="updateQuantity(item.product.id, item.quantity - 1)"
                      class="w-7 h-7 rounded-full flex items-center justify-center bg-white border border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-600 transition-colors"
                  >
                    <span style="font-style: normal; font-weight: bold;">−</span>
                  </button>

                  <span class="text-sm font-bold w-8 text-center text-gray-900">{{ item.quantity }}</span>

                  <button
                      @click="updateQuantity(item.product.id, item.quantity + 1)"
                      class="w-7 h-7 rounded-full flex items-center justify-center bg-white border border-gray-300 hover:bg-green-50 hover:border-green-300 hover:text-green-600 transition-colors"
                  >
                    <span style="font-style: normal; font-weight: bold;">+</span>
                  </button>
                </div>

                <button
                    @click="removeFromCart(item.product.id)"
                    class="text-gray-400 hover:text-red-500 p-1 transition-colors"
                    title="Remover artigo"
                >
                  <span style="font-style: normal;">🗑️</span>
                </button>
              </div>
            </div>

            <div class="border-t pt-4 mt-4">
              <div class="flex justify-between items-center mb-4">
                <span class="font-semibold text-gray-700">Total</span>
                <span class="text-2xl font-bold text-green-600">€{{ calculateTotal().toFixed(2) }}</span>
              </div>
              <Button class="w-full bg-green-600 text-white" :disabled="cart.length === 0" @click="handleCheckout">Finalizar Venda</Button>
            </div>
          </div>
        </div>
      </div>
    </div>

        <Dialog :open="showReturnDialog" @update:open="showReturnDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Processar Devolução</DialogTitle>
          <DialogDescription>Identifique a fatura para iniciar o reembolso.</DialogDescription>
        </DialogHeader>

        <div class="py-4 space-y-4">
          <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700">Número da Fatura</label>
            <div class="flex gap-2">
              <Input v-model="invoiceSearch" placeholder="Ex: VD-001" />
              <Button @click="findInvoice" class="bg-blue-600 text-white">Identificar</Button>
            </div>
          </div>
        </div>

        <div v-if="selectedSale" class="py-4 border-t border-gray-100 space-y-4">
          <div class="bg-blue-50 p-3 rounded-lg text-sm">
            <p><b>Fatura:</b> {{ selectedSale.numFatura }}</p>
            <p><b>Data:</b> {{ formatDate(selectedSale.date) }}</p>
            <p v-if="!isWithinReturnPeriod(selectedSale.date)" class="text-red-600 font-bold mt-1">⚠️ Prazo de 30 dias expirado.</p>
          </div>

          <div v-if="isWithinReturnPeriod(selectedSale.date)" class="space-y-3">
            <label class="text-sm font-medium">Itens Disponíveis para Devolução:</label>
            <div v-for="item in returnableItems" :key="item.idLinhaVenda || item.name"
                 class="flex items-center justify-between p-3 border rounded-lg"
                 :class="{'bg-green-50 border-green-200': returnForm.idLinhaVenda === item.idLinhaVenda}">
              <div class="flex-1">
                <p class="text-sm font-medium">{{ item.name }}</p>
                <p class="text-xs text-gray-500">Qtd original: {{ item.quantity }} | €{{ item.price }}</p>
              </div>

              <Button v-if="!isProductBlocked(item.name)"
                      variant="outline" size="sm"
                      @click="selectItemForReturn(item)">
                Selecionar
              </Button>
              <span v-else class="text-[10px] bg-red-100 text-red-700 px-2 py-1 rounded">Bloqueado</span>
            </div>
            <p v-if="returnableItems.length === 0" class="text-sm text-gray-500">
              Esta fatura não tem linhas disponíveis para devolução.
            </p>
          </div>

          <div v-if="returnForm.idLinhaVenda" class="pt-4 border-t space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <label class="text-sm font-medium text-gray-700">Qtd a Devolver</label>
                <Input type="number" v-model="returnForm.quantity" min="1" :max="maxReturnQty" />
              </div>
              <div class="space-y-2">
                <label class="text-sm font-medium text-gray-700">Método Reembolso</label>
                <Select v-model="returnForm.refundMethod">
                  <option value="Dinheiro">Dinheiro</option>
                  <option value="Cartão">Cartão</option>
                </Select>
              </div>
            </div>
            
            <div v-if="returnForm.idLinhaVenda" class="bg-green-50 p-4 rounded-lg border border-green-100 flex justify-between items-center">
              <span class="text-green-800 font-bold">Total a Reembolsar:</span>
              <span class="text-2xl font-black text-green-700">€{{ (returnForm.quantity * (returnableItems.find(i => i.idLinhaVenda === returnForm.idLinhaVenda)?.price || 0)).toFixed(2) }}</span>
            </div>

            <Input v-model="returnForm.reason" placeholder="Motivo da devolução (Obrigatório)" />
          </div>
        </div>

        <DialogFooter class="mt-6">
          <Button variant="outline" @click="resetReturn">Cancelar</Button>
          <Button v-if="selectedSale && isWithinReturnPeriod(selectedSale.date)"
                  class="bg-green-600 text-white"
                  :disabled="!canSubmitReturn"
                  @click="processReturn">
            Confirmar Reembolso
          </Button>
        </DialogFooter>
      </div>
    </Dialog>

    <Dialog :open="showKeyboardHelp" @update:open="showKeyboardHelp = $event">
      <div class="p-6">
        <DialogHeader><DialogTitle>Atalhos</DialogTitle></DialogHeader>
        <div class="py-4 space-y-2 text-sm">
          <p><b>F1:</b> Ver Atalhos</p>
          <p><b>F12:</b> Focar Código de Barras</p>
          <p><b>Ctrl+Z:</b> Desfazer</p>
        </div>
        <DialogFooter><Button @click="showKeyboardHelp = false">Fechar</Button></DialogFooter>
      </div>
    </Dialog>

    <Dialog :open="showCancelDialog" @update:open="showCancelDialog = $event">
      <div class="p-6">
        <DialogHeader><DialogTitle>Anular Venda?</DialogTitle></DialogHeader>
        <DialogFooter>
          <Button variant="outline" @click="showCancelDialog = false">Não</Button>
          <Button class="bg-red-600 text-white" @click="confirmCancel">Sim</Button>
        </DialogFooter>
      </div>
    </Dialog>

    <Dialog :open="showPaymentDialog" @update:open="showPaymentDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>
            <span v-if="paymentStep === 'selection'">Finalizar Pagamento</span>
            <span v-else-if="paymentStep === 'cash'">💵 Pagamento em Dinheiro</span>
            <span v-else-if="paymentStep === 'card'">💳 Pagamento com Cartão</span>
            <span v-else-if="paymentStep === 'mbway'">📱 Pagamento via MBWay</span>
          </DialogTitle>
        </DialogHeader>

        <div v-if="paymentStep === 'selection'" class="py-4 space-y-3">
          <div>
            <label class="text-sm font-medium text-gray-700">Formato do Comprovativo</label>
            <Select v-model="receiptFormat">
              <option value="FISICO">Físico</option>
              <option value="DIGITAL">Digital</option>
            </Select>
          </div>
          <Button @click="selectPaymentMethod('Numerário')" class="w-full justify-start gap-3" variant="outline">
            <span class="text-xl">💵</span> <span>Dinheiro</span>
          </Button>
          <Button @click="selectPaymentMethod('Cartão')" class="w-full justify-start gap-3" variant="outline">
            <span class="text-xl">💳</span> <span>Cartão Bancário</span>
          </Button>
          <Button @click="selectPaymentMethod('MBWay')" class="w-full justify-start gap-3" variant="outline">
            <span class="text-xl">📱</span> <span>MBWay</span>
          </Button>
        </div>

        <div v-else-if="paymentStep === 'cash'" class="py-4 space-y-4">
          <div class="bg-gray-50 p-4 rounded-lg border border-gray-200 text-center">
            <p class="text-xs text-gray-500 uppercase font-bold">Total a Pagar</p>
            <p class="text-3xl font-black text-green-600">€{{ calculateTotal().toFixed(2) }}</p>
          </div>
          <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700">Valor Entregue (€)</label>
            <Input
              type="number"
              step="0.01"
              min="0"
              :model-value="cashAmount"
              @update:model-value="updateCashAmount"
              class="text-xl font-bold"
            />
          </div>
          <div v-if="canCompleteCash" class="bg-blue-50 p-4 rounded-lg border border-blue-100 flex justify-between items-center">
            <span class="text-blue-800 font-bold">Troco a devolver:</span>
            <span class="text-2xl font-black text-blue-700">€{{ changeAmount.toFixed(2) }}</span>
          </div>
        </div>

        <div v-else-if="paymentStep === 'card'" class="py-4 space-y-4 text-center">
          <div class="bg-blue-50 p-4 rounded-lg border border-blue-100">
             <span class="text-4xl">💳</span>
             <p class="text-lg font-bold text-blue-700 mt-2">Total: €{{ calculateTotal().toFixed(2) }}</p>
          </div>
          <p class="text-sm text-gray-600">Por favor, utilize o Terminal de Pagamento Automático (TPA) para concluir a transação.</p>
        </div>

        <div v-else-if="paymentStep === 'mbway'" class="py-4 space-y-4">
          <div class="bg-pink-50 p-4 rounded-lg border border-pink-100 text-center">
             <img src="https://www.mbway.pt/wp-content/themes/mbway/assets/images/logo-mbway.png" alt="MBWay" class="h-8 mx-auto mb-2" />
             <p class="text-lg font-bold text-pink-700">€{{ calculateTotal().toFixed(2) }}</p>
          </div>
          <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700">Número de Telemóvel</label>
            <Input type="tel" v-model="mbwayPhone" placeholder="9xxxxxxxx" maxlength="9" class="text-xl text-center tracking-widest" />
          </div>
          <p class="text-xs text-gray-500 italic text-center">O cliente receberá uma notificação na aplicação MBWay.</p>
        </div>

        <DialogFooter class="flex gap-2">
          <Button v-if="paymentStep === 'selection'" variant="outline" @click="showPaymentDialog = false" class="flex-1">Cancelar</Button>
          <Button v-else variant="outline" @click="paymentStep = 'selection'" class="flex-1">Voltar</Button>

          <Button v-if="paymentStep === 'cash'" 
                  class="bg-green-600 text-white flex-1" 
                  :disabled="!canCompleteCash"
                  @click="completePayment('Numerário')">
            Finalizar (Troco: €{{ changeAmount.toFixed(2) }})
          </Button>
          <Button v-else-if="paymentStep === 'mbway'" 
                  class="bg-pink-600 text-white flex-1" 
                  :disabled="!canCompleteMBWay"
                  @click="completePayment('MBWay')">
            Enviar Pedido
          </Button>
          <Button v-else-if="paymentStep === 'card'" 
                  class="bg-blue-600 text-white flex-1" 
                  @click="completePayment('Cartão')">
            Confirmar Pagamento TPA
          </Button>
        </DialogFooter>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useProductsStore } from '../stores/products';
import { useSalesStore } from '../stores/sales';
import { useStatisticsStore } from '../stores/statistics';
import { useShopStore } from '../stores/shop';
import { toast } from 'vue-sonner';

import Button from '../components/ui/Button.vue';
import Input from '../components/ui/Input.vue';
import Select from '../components/ui/Select.vue';
import Dialog from '../components/ui/Dialog.vue';
import DialogHeader from '../components/ui/DialogHeader.vue';
import DialogTitle from '../components/ui/DialogTitle.vue';
import DialogDescription from '../components/ui/DialogDescription.vue';
import DialogFooter from '../components/ui/DialogFooter.vue';

const authStore = useAuthStore();
const productsStore = useProductsStore();
const salesStore = useSalesStore();
const statsStore = useStatisticsStore();
const shopStore = useShopStore();

const cart = ref([]);
const barcode = ref('');
const searchTerm = ref('');
const barcodeInputRef = ref(null);
const showKeyboardHelp = ref(false);
const lastAction = ref(null);
const showCancelDialog = ref(false);

// ESTADO DE DEVOLUÇÃO
const showReturnDialog = ref(false);
const invoiceSearch = ref('');
const selectedSale = ref(null);
const maxReturnQty = ref(1);
const returnForm = ref({ idLinhaVenda: '', itemName: '', quantity: 1, reason: '', refundMethod: 'Dinheiro', date: new Date().toISOString().split('T')[0] });

const returnableItems = computed(() => {
  const sale = selectedSale.value;
  if (!sale) return [];

  const originalItems = sale.items || sale.linhas || [];
  return originalItems
    .map((item) => {
      const id = String(item?.idLinhaVenda || item?.id || '');
      const returnedQty = salesStore.returnedItemsQuantities[id] || 0;
      return {
        idLinhaVenda: id,
        name: item?.name || item?.produto?.nome || 'Artigo sem nome',
        quantity: Math.max(0, Number(item?.quantity ?? item?.quantidade ?? 0) - returnedQty),
        price: Number(item?.price ?? item?.precoFinal ?? item?.totalFinal ?? item?.subtotal ?? 0)
      };
    })
    .filter((item) => item.idLinhaVenda && item.quantity > 0);
});

const isProductBlocked = (name) => {
  if (!name) return false;
  const blocked = ['água', 'pão', 'leite', 'batatas', 'coca', 'cola', 'delta', 'iogurte', 'cerveja', 'snack', 'doce', 'chocolate', 'arroz', 'massa'];
  return blocked.some(k => name.toLowerCase().includes(k));
};

const findInvoice = () => {
  const searchId = invoiceSearch.value.trim().toUpperCase();
  const sale = salesStore.sales.find(s => 
    String(s.id).toUpperCase() === searchId || 
    String(s.numFatura).toUpperCase() === searchId
  );

  if (sale) {
    selectedSale.value = sale;
    returnForm.value.idLinhaVenda = '';
    returnForm.value.itemName = '';
    toast.success('Fatura Identificada');
  } else {
    selectedSale.value = null;
    toast.error('Fatura não encontrada');
  }
};

const selectItemForReturn = (item) => {
  returnForm.value.idLinhaVenda = item.idLinhaVenda;
  returnForm.value.itemName = item.name;
  returnForm.value.quantity = 1;
  maxReturnQty.value = item.quantity;
};

const isWithinReturnPeriod = (date) => {
  const diffTime = Math.abs(new Date() - new Date(date));
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays <= 30;
};

const canSubmitReturn = computed(() => {
  return selectedSale.value && returnForm.value.idLinhaVenda && returnForm.value.reason.length > 2;
});

const processReturn = async () => {
  try {
    const itemToReturn = returnableItems.value.find(i => i.idLinhaVenda === returnForm.value.idLinhaVenda);
    
    if (!itemToReturn) {
      toast.error('Artigo não encontrado na fatura.');
      return;
    }
    
    await salesStore.processReturn(selectedSale.value.id, [{
      idLinhaVenda: itemToReturn.idLinhaVenda,
      quantidade: parseInt(returnForm.value.quantity, 10)
    }]);

    const precoFinal = itemToReturn.price || 0;
    toast.success('Devolução Processada!', {
      description: `Reembolso de €${(returnForm.value.quantity * precoFinal).toFixed(2)} via ${returnForm.value.refundMethod}`
    });

    // Atualizar stock em tempo real após a devolução
    await productsStore.fetchProducts();

    await salesStore.fetchSales();
    resetReturn();
  } catch (error) {
    toast.error(error.body?.message || 'Erro a processar a devolução com a Base de Dados.');
  }
};

const resetReturn = () => {
  showReturnDialog.value = false;
  selectedSale.value = null;
  invoiceSearch.value = '';
  returnForm.value = { idLinhaVenda: '', itemName: '', quantity: 1, reason: '', refundMethod: 'Dinheiro', date: new Date().toISOString().split('T')[0] };
};

const openReturnModal = () => { showReturnDialog.value = true; };

// LÓGICA DE VENDA
const filteredProducts = computed(() => {
  return productsStore.products.filter(p => 
    (p.status || 'Ativo') === 'Ativo' &&
    p.name.toLowerCase().includes(searchTerm.value.toLowerCase())
  );
});

const addToCart = (product) => {
  const stockDisponivel = Number(product.stock ?? 0);
  if (stockDisponivel <= 0) {
    toast.error('Sem stock disponível para este produto.');
    return;
  }

  const item = cart.value.find(i => i.product.id === product.id);
  if (item) {
    if (item.quantity >= stockDisponivel) {
      toast.error(`Stock insuficiente. Disponível: ${stockDisponivel}.`);
      return;
    }
    item.quantity++;
  } else {
    cart.value.push({ product, quantity: 1 });
  }

  lastAction.value = { type: 'add', data: product };
  barcode.value = '';
  nextTick(() => barcodeInputRef.value?.focus());
};

const handleBarcodeSubmit = () => {
  const p = productsStore.getProductByBarcode(barcode.value);
  if (p) addToCart(p); else toast.error('Código inválido');
};

const removeFromCart = (id) => {
  const item = cart.value.find(i => i.product.id === id);
  lastAction.value = { type: 'remove', data: item };
  cart.value = cart.value.filter(i => i.product.id !== id);
};

// ATUALIZAR QUANTIDADE (Mantido - Se Qtd < 1 remove do carrinho)
const updateQuantity = (id, q) => {
  if (q <= 0) { removeFromCart(id); return; }
  const item = cart.value.find(i => i.product.id === id);
  if (!item) return;

  const stockDisponivel = Number(item.product.stock ?? 0);
  if (q > stockDisponivel) {
    toast.error(`Stock insuficiente. Disponível: ${stockDisponivel}.`);
    return;
  }
  item.quantity = q;
};

const undoLastAction = () => {
  if (!lastAction.value) return;
  if (lastAction.value.type === 'add') {
    const item = cart.value.find(i => i.product.id === lastAction.value.data.id);
    if (item && item.quantity > 1) {
      item.quantity--;
    } else {
      cart.value = cart.value.filter(i => i.product.id !== lastAction.value.data.id);
    }
  } else {
    cart.value.push(lastAction.value.data);
  }
  lastAction.value = null;
  toast.info('Ação desfeita');
};

const showPaymentDialog = ref(false);
const receiptFormat = ref('FISICO');
const paymentStep = ref('selection'); // 'selection', 'cash', 'card', 'mbway'
const cashAmount = ref(0);
const mbwayPhone = ref('');

const sanitizeMoneyInput = (value) => {
  const normalized = String(value ?? '').replace(',', '.').replace(/[^\d.]/g, '');
  if (!normalized) return '';

  const [inteiro, ...decimaisPartes] = normalized.split('.');
  const decimais = decimaisPartes.join('').slice(0, 2);
  if (decimaisPartes.length > 0) {
    return `${inteiro}.${decimais}`;
  }
  return inteiro;
};

const hasAtMostTwoDecimals = (value) => {
  const normalized = String(value ?? '').trim();
  if (!normalized) return false;
  if (!/^\d+(\.\d+)?$/.test(normalized)) return false;
  const decimais = normalized.split('.')[1] || '';
  return decimais.length <= 2;
};

const updateCashAmount = (value) => {
  cashAmount.value = sanitizeMoneyInput(value);
};

const changeAmount = computed(() => {
  const total = calculateTotal();
  const delivered = parseFloat(cashAmount.value) || 0;
  // Use rounded integer comparison to avoid float issues
  const changeInCents = Math.round(delivered * 100) - Math.round(total * 100);
  return Math.max(0, changeInCents / 100);
});

const canCompleteCash = computed(() => {
  if (!hasAtMostTwoDecimals(cashAmount.value)) return false;
  const total = calculateTotal();
  const delivered = parseFloat(cashAmount.value) || 0;
  // Use rounded integer comparison to avoid float issues
  return Math.round(delivered * 100) >= Math.round(total * 100);
});

const canCompleteMBWay = computed(() => {
  const phone = String(mbwayPhone.value || '');
  return phone.length === 9;
});

const selectPaymentMethod = (method) => {
  if (method === 'Numerário') {
    paymentStep.value = 'cash';
    cashAmount.value = calculateTotal().toFixed(2);
  } else if (method === 'MBWay') {
    paymentStep.value = 'mbway';
    mbwayPhone.value = '';
  } else if (method === 'Cartão') {
    paymentStep.value = 'card';
  }
};

const calculateTotal = () => cart.value.reduce((s, i) => {
  const unit = i.product.hasPromotion ? i.product.discountedPrice : i.product.price;
  return s + (unit * i.quantity);
}, 0);
const handleCancelTransaction = () => { if (cart.value.length) showCancelDialog.value = true; };
const confirmCancel = () => { cart.value = []; showCancelDialog.value = false; toast.success('Venda anulada.'); };
const handleCheckout = () => {
   if (cart.value.length) {
    // Reset payment state for a clean dialog
    paymentStep.value = 'selection';
    receiptFormat.value = 'FISICO';
    cashAmount.value = 0;
    mbwayPhone.value = '';
    showPaymentDialog.value = true;
   } // <-- FALTAVA FECHAR O IF AQUI
}; // <-- FALTAVA FECHAR A FUNÇÃO AQUI

const completePayment = async (m) => {
  try {
    const total = calculateTotal();
    const deliveredValue = m === 'Numerário' ? Number(cashAmount.value) : total;
    const saleResult = await salesStore.addSale({
      idLoja: authStore.currentUser?.idLoja || 1,
      idFuncionario: authStore.currentUser?.idFuncionario || authStore.currentUser?.id,
      items: cart.value.map(i => ({ productId: i.product.id, quantity: i.quantity })),
      total: total,
      paymentMethod: m,
      date: new Date(),
      receiptFormat: receiptFormat.value,
      valorEntregue: deliveredValue
    });
    
    const invoiceNum = saleResult?.numFatura || '';
    toast.success(`Venda concluida! ${invoiceNum}`, {
      description: m === 'Numerário' ? `Troco: €${changeAmount.value.toFixed(2)}` : `Pagamento via ${m} confirmado.`
    });
    cart.value = [];
    showPaymentDialog.value = false;
    lastAction.value = null;
    paymentStep.value = 'selection';
    statsStore.fetchKpis();
  } catch (error) {
    console.error('Erro ao finalizar pagamento:', error);
    toast.error(error.body?.details || error.body?.message || 'Erro ao finalizar venda. Verifique a ligação ao servidor.');
  }
};

const formatDate = (date) => new Date(date).toLocaleDateString('pt-PT');

onMounted(() => {
  productsStore.fetchProducts();
  salesStore.fetchSales();
  statsStore.fetchKpis();
  window.addEventListener('keydown', (e) => {
    if (e.key === 'F12') { e.preventDefault(); barcodeInputRef.value?.focus(); }
    if (e.key === 'F1') { e.preventDefault(); showKeyboardHelp.value = true; }
    if (e.ctrlKey && e.key === 'z') { e.preventDefault(); undoLastAction(); }
  });
  nextTick(() => barcodeInputRef.value?.focus());
}); // <-- FALTAVA FECHAR O ONMOUNTED AQUI

watch(
  () => shopStore.selectedShopId,
  () => {
    productsStore.fetchProducts();
    salesStore.fetchSales();
    statsStore.fetchKpis();
  }
);

</script>