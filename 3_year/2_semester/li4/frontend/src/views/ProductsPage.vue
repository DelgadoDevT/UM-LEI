<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-7xl mx-auto space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Gestão de Produtos</h1>
          <p class="text-sm text-gray-600 mt-1">{{ products.length }} produtos registados</p>
        </div>
        <div class="flex items-center gap-2">
          <Button
            v-if="canManageSupply"
            @click="openPromotionsDialog"
            variant="outline"
            class="border-purple-600 text-purple-700 hover:bg-purple-50"
          >
            <Tag class="size-4 mr-2" />
            Promoções
          </Button>
          <Button
            v-if="canEdit"
            @click="openCategoryDialog"
            variant="outline"
            class="border-blue-600 text-blue-700 hover:bg-blue-50"
          >
            <Tag class="size-4 mr-2" />
            Categorias
          </Button>
          <Button
            v-if="canEdit"
            @click="openInventoryDialog"
            variant="outline"
            class="border-amber-600 text-amber-700 hover:bg-amber-50"
          >
            <Tag class="size-4 mr-2" />
            Movimento Inventário
          </Button>
          <Button
            v-if="canEdit"
            @click="openNewDialog"
            class="bg-green-600 hover:bg-green-700"
          >
            <Plus class="size-4 mr-2" />
            Novo Produto
          </Button>
        </div>
      </div>

      <!-- Active Promotions Section -->
      <div v-if="activePromotions.length > 0" class="space-y-4">
        <h2 class="text-lg font-semibold text-gray-900">Promoções Ativas</h2>
        <div class="bg-white rounded-lg border border-purple-200 overflow-hidden shadow-sm">
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-purple-50 border-b border-purple-100">
                <tr>
                  <th class="text-left py-3 px-4 font-semibold text-purple-700 uppercase tracking-wider">Produto</th>
                  <th class="text-left py-3 px-4 font-semibold text-purple-700 uppercase tracking-wider">Desconto</th>
                  <th class="text-left py-3 px-4 font-semibold text-purple-700 uppercase tracking-wider">Início</th>
                  <th class="text-left py-3 px-4 font-semibold text-purple-700 uppercase tracking-wider">Fim</th>
                  <th v-if="canManageSupply" class="text-right py-3 px-4 font-semibold text-purple-700 uppercase tracking-wider">Ações</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-purple-50">
                <tr v-for="promo in activePromotions" :key="promo.idPromocao || promo.id" class="hover:bg-purple-50/50 transition-colors">
                  <td class="py-3 px-4 font-medium text-purple-900">{{ getPromotionProductLabel(promo) }}</td>
                  <td class="py-3 px-4 text-gray-900">{{ promo.desconto ?? promo.discount }}%</td>
                  <td class="py-3 px-4 text-gray-500">{{ formatDate(promo.dataInicio ?? promo.startDate) }}</td>
                  <td class="py-3 px-4 text-gray-500">{{ formatDate(promo.dataFim ?? promo.endDate) }}</td>
                  <td v-if="canManageSupply" class="py-3 px-4 text-right">
                    <Button
                      size="sm"
                      variant="outline"
                      class="text-xs h-8 border-red-200 text-red-700 hover:bg-red-100"
                      @click="openCancelPromotionDialog(promo)"
                    >
                      Cancelar
                    </Button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Search and Filter -->
      <div class="bg-white rounded-lg border border-gray-200 p-4">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="md:col-span-2">
            <label class="text-sm font-medium text-gray-700 block mb-2">Pesquisar</label>
            <div class="relative">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-gray-400" />
              <Input
                v-model="searchTerm"
                placeholder="Pesquisar por nome, código de barras ou categoria..."
                class="pl-10 bg-gray-100/50 border-none"
              />
            </div>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700 block mb-2">Categoria</label>
            <Select v-model="filterCategory" class="bg-gray-100/50 border-none">
              <option value="">Todas as Categorias</option>
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </Select>
          </div>
        </div>
      </div>

      <!-- Products Table -->
      <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-200">
              <tr>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Produto</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Código Barras</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Categoria</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Preço</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Stock</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Fornecedor</th>
                <th v-if="canEdit" class="text-right py-3 px-4 text-sm font-medium text-gray-700">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="product in pagedProducts"
                :key="product.id"
                class="border-b border-gray-100 hover:bg-gray-50"
              >
                <td class="py-3 px-4">
                  <div class="flex items-center gap-3">
                    <span class="text-2xl">{{ product.image }}</span>
                    <span class="text-sm font-medium text-gray-900">{{ product.name }}</span>
                  </div>
                </td>
                <td class="py-3 px-4 text-sm text-gray-600 font-mono">{{ product.barcode }}</td>
                <td class="py-3 px-4">
                  <span :class="['inline-flex items-center px-2 py-1 rounded-full text-xs font-medium', getCategoryClass(product.category)]">
                    {{ product.category }}
                  </span>
                </td>
                <td class="py-3 px-4 text-sm font-medium text-gray-900">
                  <template v-if="product.hasPromotion">
                    <span class="line-through text-gray-400 mr-1">€{{ product.price.toFixed(2) }}</span>
                    <span class="text-green-600 font-semibold">€{{ product.discountedPrice.toFixed(2) }}</span>
                    <span class="ml-1 text-[10px] bg-red-100 text-red-700 px-1 rounded">-{{ product.discountPercentage }}%</span>
                  </template>
                  <template v-else>€{{ product.price.toFixed(2) }}</template>
                </td>
                <td class="py-3 px-4">
                  <span
                    :class="[
                      'inline-flex items-center px-2 py-1 rounded-full text-xs font-medium',
                      isLowStock(product)
                        ? 'bg-red-100 text-red-700'
                        : 'bg-green-100 text-green-700'
                    ]"
                  >
                    {{ product.stock }}
                  </span>
                </td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ product.supplier }}</td>
                <td v-if="canEdit" class="py-3 px-4 text-right">
                  <div class="flex items-center justify-end gap-2">
                    <Button variant="ghost" size="sm" @click="openDetailsDialog(product)">
                      <Search class="size-4" />
                    </Button>
                    <Button variant="ghost" size="sm" @click="openEditDialog(product)">
                      <Edit2 class="size-4" />
                    </Button>
                    <Button variant="ghost" size="sm" @click="openDeleteDialog(product)">
                      <Trash2 class="size-4 text-red-600" />
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <Pagination
          :total-items="filteredProducts.length"
          :current-page="productsPage"
          :page-size="productsPageSize"
          @update:current-page="productsPage = $event"
          @update:page-size="productsPageSize = $event"
        />
      </div>
    </div>

    <!-- Promotions Dialog -->
    <Dialog :open="showPromotionsDialog" @update:open="showPromotionsDialog = $event">
      <div class="p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-4">Criar Promoção</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nome da Campanha *</label>
            <Input v-model="promotionForm.designacao" placeholder="Ex: Saldos de Verão" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Produto *</label>
            <Select v-model="promotionForm.productId">
              <option value="">Selecione um produto...</option>
              <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
            </Select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Desconto (%) *</label>
            <Input type="number" v-model="promotionForm.discount" min="1" max="100" placeholder="Ex: 25" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Data de Início *</label>
              <Input type="date" v-model="promotionForm.startDate" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Data de Fim *</label>
              <Input type="date" v-model="promotionForm.endDate" />
            </div>
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-8">
          <Button variant="outline" @click="showPromotionsDialog = false">Cancelar</Button>
          <Button @click="confirmPromotion" class="bg-purple-600 hover:bg-purple-700 text-white">Confirmar Promoção</Button>
        </div>
      </div>
    </Dialog>

    <!-- New/Edit Product Dialog -->
    <Dialog :open="showDialog" @update:open="showDialog = $event">
      <div class="p-6 max-w-2xl">
        <DialogHeader>
          <DialogTitle>{{ isEditing ? 'Editar Produto' : 'Novo Produto' }}</DialogTitle>
          <DialogDescription>
            {{ isEditing ? 'Atualize as informações do produto' : 'Preencha os dados do novo produto' }}
          </DialogDescription>
        </DialogHeader>

        <div class="py-4 space-y-4">
          <!-- Name -->
          <div>
            <Label htmlFor="name">Nome do Produto *</Label>
            <Input
              id="name"
              v-model="formData.name"
              placeholder="Ex: Água Mineral 1.5L"
              :class="errors.name ? 'border-red-300' : ''"
            />
            <p v-if="errors.name" class="text-xs text-red-600 mt-1">{{ errors.name }}</p>
          </div>

          <!-- Barcode -->
          <div>
            <Label htmlFor="barcode">Código de Barras *</Label>
            <Input
              id="barcode"
              v-model="formData.barcode"
              placeholder="Ex: 5600123456789"
              maxlength="13"
              @input="handleBarcodeInput"
              :class="errors.barcode ? 'border-red-300' : ''"
            />
            <p v-if="errors.barcode" class="text-xs text-red-600 mt-1">{{ errors.barcode }}</p>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <!-- Category -->
            <div>
              <Label htmlFor="category">Categoria *</Label>
              <Select id="category" v-model="formData.category">
                <option value="">Selecione...</option>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
              </Select>
              <p v-if="errors.category" class="text-xs text-red-600 mt-1">{{ errors.category }}</p>
            </div>

            <!-- Price -->
            <div>
              <Label htmlFor="price">Preço (€) *</Label>
              <Input
                id="price"
                :model-value="formData.price"
                @update:model-value="updatePriceInput"
                type="number"
                step="0.01"
                min="0"
                placeholder="0.00"
                :class="errors.price ? 'border-red-300' : ''"
              />
              <p v-if="errors.price" class="text-xs text-red-600 mt-1">{{ errors.price }}</p>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <!-- Stock -->
            <div>
              <Label htmlFor="stock">Stock Atual *</Label>
              <Input
                id="stock"
                v-model="formData.stock"
                type="number"
                min="0"
                placeholder="0"
                :class="errors.stock ? 'border-red-300' : ''"
              />
              <p v-if="errors.stock" class="text-xs text-red-600 mt-1">{{ errors.stock }}</p>
            </div>

            <!-- Min Stock -->
            <div>
              <Label htmlFor="minStock">Stock Mínimo *</Label>
              <Input
                id="minStock"
                v-model="formData.minStock"
                type="number"
                min="0"
                placeholder="0"
                :class="errors.minStock ? 'border-red-300' : ''"
              />
              <p v-if="errors.minStock" class="text-xs text-red-600 mt-1">{{ errors.minStock }}</p>
            </div>
          </div>

          <!-- Supplier -->
          <div>
            <Label htmlFor="supplier">Fornecedor *</Label>
            <Select id="supplier" v-model="formData.supplier">
              <option value="">Selecione...</option>
              <option v-for="sup in suppliers" :key="sup.id" :value="sup.name">{{ sup.name }}</option>
            </Select>
            <p v-if="errors.supplier" class="text-xs text-red-600 mt-1">{{ errors.supplier }}</p>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" class="text-gray-900 border-gray-300" @click="closeDialog">
            Cancelar
          </Button>
          <Button
            @click="handleSubmit"
            class="bg-green-600 hover:bg-green-700 text-white"
          >
            {{ isEditing ? 'Guardar Alterações' : 'Criar Produto' }}
          </Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Delete Confirmation Dialog -->
    <Dialog :open="showDeleteDialog" @update:open="showDeleteDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Eliminar Produto</DialogTitle>
          <DialogDescription>
            Tem a certeza que deseja eliminar "{{ productToDelete?.name }}"? Esta ação não pode ser revertida.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" class="text-gray-900 border-gray-300" @click="showDeleteDialog = false">
            Cancelar
          </Button>
          <Button
            @click="confirmDelete"
            class="bg-red-600 hover:bg-red-700 text-white"
          >
            Eliminar
          </Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Product Details Dialog -->
    <Dialog :open="!!selectedProductDetail" @update:open="(open) => !open && (selectedProductDetail = null)">
      <div v-if="selectedProductDetail" class="p-6">
        <DialogHeader>
          <DialogTitle>Detalhes do Produto</DialogTitle>
          <DialogDescription>{{ selectedProductDetail.name }}</DialogDescription>
        </DialogHeader>
        <div class="py-4 grid grid-cols-2 gap-4 text-sm">
          <div><span class="font-medium">ID:</span> {{ selectedProductDetail.id }}</div>
          <div><span class="font-medium">Código de barras:</span> {{ selectedProductDetail.barcode }}</div>
          <div><span class="font-medium">Categoria:</span> {{ selectedProductDetail.category }}</div>
          <div><span class="font-medium">Fornecedor:</span> {{ selectedProductDetail.supplier }}</div>
          <div><span class="font-medium">Preço:</span> €{{ selectedProductDetail.price.toFixed(2) }}</div>
          <div><span class="font-medium">Stock:</span> {{ selectedProductDetail.stock }}</div>
          <div><span class="font-medium">Stock mínimo:</span> {{ selectedProductDetail.minStock }}</div>
          <div><span class="font-medium">Estado:</span> {{ selectedProductDetail.status || 'Ativo' }}</div>
        </div>
        <DialogFooter>
          <Button @click="selectedProductDetail = null">Fechar</Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Category Dialog -->
    <Dialog :open="showCategoryDialog" @update:open="showCategoryDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Gestão de Categorias</DialogTitle>
          <DialogDescription>Criar, editar ou inativar categorias.</DialogDescription>
        </DialogHeader>
        <div class="py-4 space-y-3">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-2">
            <Input v-model="categoryForm.designacao" placeholder="Designação da categoria" />
            <Select v-model="categoryForm.idCategoriaPai">
              <option value="">Sem categoria pai</option>
              <option v-for="cat in categoriesData" :key="cat.idCategoria" :value="cat.idCategoria">
                {{ cat.designacao }}
              </option>
            </Select>
            <div class="flex gap-2">
              <Button class="bg-blue-600 hover:bg-blue-700 text-white" @click="submitCategory">Criar</Button>
              <Button variant="outline" @click="fetchCategories">Atualizar</Button>
            </div>
          </div>
          <div class="max-h-64 overflow-y-auto border rounded-md">
            <table class="w-full text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left px-3 py-2">ID</th>
                  <th class="text-left px-3 py-2">Designação</th>
                  <th class="text-right px-3 py-2">Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cat in categoriesData" :key="cat.idCategoria" class="border-t">
                  <td class="px-3 py-2">{{ cat.idCategoria }}</td>
                  <td class="px-3 py-2">
                    <Input :model-value="cat.designacao" @update:model-value="(v) => cat.designacao = v" />
                  </td>
                  <td class="px-3 py-2">
                    <div class="flex justify-end gap-2">
                      <Button size="sm" variant="outline" @click="editCategory(cat)">Guardar</Button>
                      <Button
                        size="sm"
                        variant="outline"
                        class="border-red-300 text-red-700"
                        :disabled="inactivatingCategoryIds.has(cat.idCategoria)"
                        @click="inactivateCategory(cat.idCategoria)"
                      >
                        {{ inactivatingCategoryIds.has(cat.idCategoria) ? 'A inativar...' : 'Inativar' }}
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="showCategoryDialog = false">Fechar</Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Inventory Movement Dialog -->
    <Dialog :open="showInventoryDialog" @update:open="showInventoryDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Registar Movimento de Inventário</DialogTitle>
          <DialogDescription>Registo manual de entrada/saída/quebra de stock.</DialogDescription>
        </DialogHeader>
        <div class="py-4 space-y-4">
          <div>
            <Label>Produto *</Label>
            <Select v-model="inventoryForm.productId">
              <option value="">Selecione um produto</option>
              <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
            </Select>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <Label>Tipo *</Label>
              <Select v-model="inventoryForm.tipo">
                <option value="ENTRADA">ENTRADA</option>
                <option value="SAIDA">SAIDA</option>
                <option value="QUEBRA">QUEBRA</option>
              </Select>
            </div>
            <div>
              <Label>Quantidade *</Label>
              <Input type="number" min="1" v-model="inventoryForm.quantidade" />
            </div>
          </div>
          <div>
            <Label>Motivo *</Label>
            <Input v-model="inventoryForm.motivo" placeholder="Ex: Receção de encomenda com fatura FT-123" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="showInventoryDialog = false">Cancelar</Button>
          <Button class="bg-amber-600 hover:bg-amber-700 text-white" @click="submitInventory">Registar Movimento</Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Password Confirmation Dialog -->
    <Dialog :open="showPasswordConfirm" @update:open="showPasswordConfirm = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Confirmar Identidade</DialogTitle>
          <DialogDescription>
            Por questões de segurança, insira a sua palavra-passe para confirmar esta ação.
          </DialogDescription>
        </DialogHeader>
        <div class="py-4">
          <Label htmlFor="confirmPassword">A sua Palavra-passe</Label>
          <Input
            id="confirmPassword"
            v-model="passwordConfirm"
            type="password"
            placeholder="Insira a sua password"
            :class="passwordConfirmError ? 'border-red-300' : ''"
            @keyup.enter="handlePasswordConfirm"
          />
          <p v-if="passwordConfirmError" class="text-xs text-red-600 mt-1">{{ passwordConfirmError }}</p>
        </div>
        <DialogFooter>
          <Button variant="outline" class="text-gray-900 border-gray-300" @click="showPasswordConfirm = false" :disabled="isVerifying">
            Cancelar
          </Button>
          <Button
            @click="handlePasswordConfirm"
            class="bg-green-600 hover:bg-green-700 text-white"
            :disabled="isVerifying"
          >
            {{ isVerifying ? 'A validar...' : 'Confirmar' }}
          </Button>
        </DialogFooter>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useProductsStore } from '../stores/products';
import { useSuppliersStore } from '../stores/suppliers';
import { usePromotionsStore } from '../stores/promotions';
import { useShopStore } from '../stores/shop';
import { localApiClient, globalApiClient } from '../services/apiClients';

const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
const client = isGlobal ? globalApiClient : localApiClient;
import { toast } from 'vue-sonner';
import { generateUUID } from '../utils/uuid';
import { Plus, Search, Edit2, Trash2, Tag } from 'lucide-vue-next';
import Button from '../components/ui/Button.vue';
import Input from '../components/ui/Input.vue';
import Select from '../components/ui/Select.vue';
import Label from '../components/ui/Label.vue';
import Dialog from '../components/ui/Dialog.vue';
import Pagination from '../components/ui/Pagination.vue';
import DialogHeader from '../components/ui/DialogHeader.vue';
import DialogTitle from '../components/ui/DialogTitle.vue';
import DialogDescription from '../components/ui/DialogDescription.vue';
import DialogFooter from '../components/ui/DialogFooter.vue';

const authStore = useAuthStore();
const productsStore = useProductsStore();
const suppliersStore = useSuppliersStore();
const promotionsStore = usePromotionsStore();
const shopStore = useShopStore();

onMounted(() => {
  productsStore.fetchProducts();
  suppliersStore.fetchSuppliers();
  promotionsStore.fetchPromotions();
  fetchCategories();
  fetchInventario();
});

watch(
  () => shopStore.selectedShopId,
  () => {
    productsStore.fetchProducts();
    suppliersStore.fetchSuppliers();
    promotionsStore.fetchPromotions();
    fetchCategories();
    fetchInventario();
  }
);

const canEdit = computed(() => authStore.canEditData);
const canManageSupply = computed(() => authStore.canManageSupply);
const products = computed(() => productsStore.products);
const suppliers = computed(() => suppliersStore.suppliers);
const activePromotions = computed(() => promotionsStore.getActivePromotions);

const getProductName = (productId) => {
  const product = products.value.find(p => p.id === productId);
  return product ? product.name : 'Produto não encontrado';
};

const isLowStock = (product) => {
  if (Array.isArray(product?.lowStockLojas) && product.lowStockLojas.length > 0) return true;
  return Number(product?.stock ?? 0) <= Number(product?.minStock ?? 0);
};

const getPromotionProductLabel = (promotion) => {
  if (promotion.productId) return getProductName(promotion.productId);
  if (Array.isArray(promotion.produtos) && promotion.produtos.length > 0) {
    return promotion.produtos.map(id => getProductName(id)).join(', ');
  }
  if (Array.isArray(promotion.categorias) && promotion.categorias.length > 0) {
    return `Categorias: ${promotion.categorias.join(', ')}`;
  }
  return promotion.designacao || 'Promoção';
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date)) return '';
  return date.toLocaleDateString('pt-PT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

const searchTerm = ref('');
const filterCategory = ref('');
const showDialog = ref(false);
const showDeleteDialog = ref(false);
const showPasswordConfirm = ref(false);
const passwordConfirm = ref('');
const passwordConfirmError = ref('');
const pendingAction = ref(null);
const isEditing = ref(false);
const productToDelete = ref(null);
const currentProduct = ref(null);
const promotionToCancel = ref(null);
const selectedProductDetail = ref(null);

const categoriesData = ref([]);
const inactivatingCategoryIds = ref(new Set());
const categories = computed(() => categoriesData.value.map(c => c.designacao));
const inventarioData = ref([]);

const getCategoryClass = (category) => {
  switch (category) {
    case 'Bebidas':
      return 'bg-blue-100 text-blue-700';
    case 'Padaria':
      return 'bg-orange-100 text-orange-700';
    case 'Laticínios':
      return 'bg-purple-100 text-purple-700';
    case 'Snacks':
      return 'bg-yellow-100 text-yellow-700';
    case 'Doces':
      return 'bg-pink-100 text-pink-700';
    case 'Mercearia':
      return 'bg-green-100 text-green-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
};

const fetchCategories = async () => {
  try {
    categoriesData.value = await client.get('/categorias');
  } catch (error) {
    categoriesData.value = [];
    console.error('Erro ao obter categorias:', error);
  }
};

const fetchInventario = async () => {
  try {
    inventarioData.value = await client.get('/inventario');
  } catch (error) {
    inventarioData.value = [];
    console.error('Erro ao obter inventário:', error);
  }
};

const formData = ref({
  name: '',
  barcode: '',
  category: '',
  price: '',
  stock: '',
  minStock: '',
  supplier: '',
  nif: '',
  image: '📦'
});

const errors = ref({});

const filteredProducts = computed(() => {
  let result = products.value.filter(p => (p.status || 'Ativo') === 'Ativo');

  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase();
    result = result.filter(p =>
      p.name.toLowerCase().includes(term) ||
      p.barcode.includes(term) ||
      p.category.toLowerCase().includes(term)
    );
  }

  if (filterCategory.value) {
    result = result.filter(p => p.category === filterCategory.value);
  }

  return result;
});

const productsPage = ref(1);
const productsPageSize = ref(20);

watch([searchTerm, filterCategory], () => {
  productsPage.value = 1;
});

const pagedProducts = computed(() => {
  const start = (productsPage.value - 1) * productsPageSize.value;
  return filteredProducts.value.slice(start, start + productsPageSize.value);
});

const handleBarcodeInput = (e) => {
  formData.value.barcode = e.target.value.replace(/\D/g, '');
};

const hasAtMostTwoDecimals = (value) => {
  const normalized = String(value ?? '').trim();
  if (!normalized) return false;
  if (!/^\d+(\.\d+)?$/.test(normalized)) return false;
  const decimals = normalized.split('.')[1] || '';
  return decimals.length <= 2;
};

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

const updatePriceInput = (value) => {
  formData.value.price = sanitizeMoneyInput(value);
};

const validateForm = () => {
  const newErrors = {};

  if (!formData.value.name.trim()) newErrors.name = 'Nome é obrigatório';
  if (!formData.value.barcode.trim()) newErrors.barcode = 'Código de barras é obrigatório';
  if (formData.value.barcode.length !== 13) newErrors.barcode = 'Código de barras deve ter 13 dígitos';
  if (!formData.value.category) newErrors.category = 'Categoria é obrigatória';
  if (!formData.value.price || parseFloat(formData.value.price) <= 0) {
    newErrors.price = 'Preço inválido';
  } else if (!hasAtMostTwoDecimals(formData.value.price)) {
    newErrors.price = 'Preço deve ter no máximo 2 casas decimais';
  }
  if (formData.value.stock === '' || parseInt(formData.value.stock) < 0) newErrors.stock = 'Stock inválido';
  if (formData.value.minStock === '' || parseInt(formData.value.minStock) < 0) newErrors.minStock = 'Stock mínimo inválido';
  if (!formData.value.supplier) newErrors.supplier = 'Fornecedor é obrigatório';

  errors.value = newErrors;
  return Object.keys(newErrors).length === 0;
};

const openNewDialog = () => {
  isEditing.value = false;
  currentProduct.value = null;
  formData.value = {
    name: '',
    barcode: '',
    category: '',
    price: '',
    stock: '',
    minStock: '',
    supplier: '',
    image: '📦'
  };
  errors.value = {};
  showDialog.value = true;
};

const openEditDialog = (product) => {
  isEditing.value = true;
  currentProduct.value = product;
  formData.value = {
    name: product.name,
    barcode: product.barcode,
    category: product.category,
    price: product.price.toString(),
    stock: product.stock.toString(),
    minStock: product.minStock.toString(),
    supplier: product.supplier,
    image: product.image
  };
  errors.value = {};
  showDialog.value = true;
};

const closeDialog = () => {
  showDialog.value = false;
  formData.value = {
    name: '',
    barcode: '',
    category: '',
    price: '',
    stock: '',
    minStock: '',
    supplier: '',
    image: '📦'
  };
  errors.value = {};
};

const handleSubmit = () => {
  if (!validateForm()) {
    toast.error('Por favor, corrija os erros no formulário');
    return;
  }

  executeSubmit();
};

const executeSubmit = async () => {
  try {
    const productBaseData = {
      codigoBarras: formData.value.barcode,
      nome: formData.value.name.trim(),
      descricao: formData.value.name.trim(),
      precoCusto: parseFloat(formData.value.price),
      precoVenda: parseFloat(formData.value.price),
      taxaIva: 'NORMAL_23',
      unidadeMedida: 'unidade',
      estado: 'Ativo',
      stock: parseFloat(formData.value.stock),
      minStock: parseFloat(formData.value.minStock)
    };

    if (isEditing.value) {
      await productsStore.updateProduct(currentProduct.value.id, productBaseData);
      toast.success('Produto atualizado com sucesso');
    } else {
      await productsStore.addProduct({
        idProduto: generateUUID(),
        ...productBaseData
      });
      toast.success('Produto criado com sucesso');
    }

    closeDialog();
    showPasswordConfirm.value = false;
  } catch (error) {
    console.error('Submit error:', error);
    toast.error(error.body?.message || error.message || 'Erro ao guardar produto.');
  }
};

const openDeleteDialog = (product) => {
  productToDelete.value = product;
  showDeleteDialog.value = true;
};

const confirmDelete = () => {
  pendingAction.value = 'delete';
  passwordConfirm.value = '';
  passwordConfirmError.value = '';
  showPasswordConfirm.value = true;
};

const executeDelete = async () => {
  try {
    await productsStore.deleteProduct(productToDelete.value.id);
    toast.success('Produto eliminado com sucesso');
    showDeleteDialog.value = false;
    productToDelete.value = null;
    showPasswordConfirm.value = false;
  } catch (error) {
    toast.error('Erro ao eliminar produto.');
  }
};

const openDetailsDialog = (product) => {
  selectedProductDetail.value = product;
};

const isVerifying = ref(false);

const handlePasswordConfirm = async () => {
  if (isVerifying.value) return;
  
  isVerifying.value = true;
  passwordConfirmError.value = '';
  
  try {
    const passwordValida = await authStore.verifyCurrentUserPassword(passwordConfirm.value);
    
    if (!passwordValida) {
      passwordConfirmError.value = 'Palavra-passe incorreta';
      isVerifying.value = false;
      return;
    }

    if (pendingAction.value === 'save') {
      await executeSubmit();
    } else if (pendingAction.value === 'delete') {
      await executeDelete();
    } else if (pendingAction.value === 'addPromotion') {
      await executeAddPromotion();
    } else if (pendingAction.value === 'cancelPromotion') {
      await executeCancelPromotion();
    }
    
    passwordConfirm.value = '';
    showPasswordConfirm.value = false;
  } catch (error) {
    console.error('Action error:', error);
    toast.error('Erro ao processar operação.');
  } finally {
    isVerifying.value = false;
  }
};

const showPromotionsDialog = ref(false);
const showCategoryDialog = ref(false);
const showInventoryDialog = ref(false);

const categoryForm = ref({ designacao: '', idCategoriaPai: '' });
const inventoryForm = ref({ productId: '', tipo: 'QUEBRA', quantidade: 1, motivo: '' });

const openCategoryDialog = () => {
  categoryForm.value = { designacao: '', idCategoriaPai: '' };
  fetchCategories();
  showCategoryDialog.value = true;
};

const openInventoryDialog = () => {
  inventoryForm.value = { productId: '', tipo: 'QUEBRA', quantidade: 1, motivo: '' };
  fetchInventario();
  showInventoryDialog.value = true;
};

const submitCategory = async () => {
  if (!categoryForm.value.designacao) {
    toast.error('A designação é obrigatória.');
    return;
  }
  try {
    // In production we would look up the ID of the parent category by name if needed, or pass null
    const payload = {
      idCategoria: generateUUID(),
      designacao: categoryForm.value.designacao,
      idCategoriaPai: categoryForm.value.idCategoriaPai || null
    };
    await client.post('/categorias', payload);
    toast.success('Categoria adicionada com sucesso!');
    await fetchCategories();
    showCategoryDialog.value = false;
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao adicionar categoria.');
  }
};

const editCategory = async (categoria) => {
  try {
    await client.patch(`/categorias/${categoria.idCategoria}`, {
      designacao: categoria.designacao,
      idCategoriaPai: categoria.idCategoriaPai || ''
    });
    toast.success('Categoria atualizada com sucesso!');
    await fetchCategories();
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao atualizar categoria.');
  }
};

const inactivateCategory = async (idCategoria) => {
  if (inactivatingCategoryIds.value.has(idCategoria)) return;

  inactivatingCategoryIds.value = new Set(inactivatingCategoryIds.value).add(idCategoria);
  try {
    await client.patch(`/categorias/${idCategoria}/inativar`);
    categoriesData.value = categoriesData.value.filter(cat => cat.idCategoria !== idCategoria);
    toast.success('Categoria inativada com sucesso!');
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao inativar categoria.');
  } finally {
    const updated = new Set(inactivatingCategoryIds.value);
    updated.delete(idCategoria);
    inactivatingCategoryIds.value = updated;
  }
};

const submitInventory = async () => {
  if (!inventoryForm.value.productId || !inventoryForm.value.motivo) {
    toast.error('Preencha todos os campos obrigatórios.');
    return;
  }
  try {
    const inventario = inventarioData.value.find(i => i.idProduto === inventoryForm.value.productId);
    if (!inventario?.idInventario) {
      toast.error('Inventário do produto não encontrado.');
      return;
    }
    const payload = {
      id: generateUUID(),
      idProduto: inventoryForm.value.productId,
      tipo: inventoryForm.value.tipo,
      quantidade: parseFloat(inventoryForm.value.quantidade),
      dataRegisto: new Date().toISOString(),
      motivo: inventoryForm.value.motivo,
      idInventario: inventario.idInventario,
      idFuncionario: authStore.currentUser?.id || ''
    };
    await productsStore.registerMovement(payload);
    toast.success('Movimento manual registado com sucesso!');
    await fetchInventario();
    showInventoryDialog.value = false;
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao registar movimento.');
  }
};

const promotionForm = ref({
  productId: '',
  discount: '',
  startDate: '',
  endDate: ''
});

const openPromotionsDialog = () => {
  promotionForm.value = {
    productId: '',
    discount: '',
    startDate: '',
    endDate: ''
  };
  showPromotionsDialog.value = true;
};

const confirmPromotion = () => {
  if (promotionForm.value.productId && promotionForm.value.discount && promotionForm.value.startDate && promotionForm.value.endDate) {
    executeAddPromotion();
  } else {
    toast.error('Por favor, preencha todos os campos da promoção.');
  }
};

const executeAddPromotion = async () => {
  try {
    await promotionsStore.addPromotion(promotionForm.value);
    showPromotionsDialog.value = false;
    showPasswordConfirm.value = false;
  } catch (error) {
    toast.error('Erro ao adicionar promoção.');
  }
};

const openCancelPromotionDialog = (promo) => {
  promotionToCancel.value = promo;
  pendingAction.value = 'cancelPromotion';
  passwordConfirm.value = '';
  passwordConfirmError.value = '';
  showPasswordConfirm.value = true;
};

const executeCancelPromotion = async () => {
  try {
    await promotionsStore.removePromotion(promotionToCancel.value.idPromocao || promotionToCancel.value.id);
    showPasswordConfirm.value = false;
    promotionToCancel.value = null;
  } catch (error) {
    toast.error('Erro ao cancelar promoção.');
  }
};
</script>
