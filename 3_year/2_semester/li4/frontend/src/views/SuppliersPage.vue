<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-7xl mx-auto space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Gestão de Fornecedores</h1>
          <p class="text-sm text-gray-600 mt-1">Gerir parceiros e fornecedores da loja</p>
        </div>
        <Button
          v-if="canEdit"
          @click="openNewDialog"
          class="bg-green-600 hover:bg-green-700"
        >
          <Plus class="size-4 mr-2" />
          Novo Fornecedor
        </Button>
      </div>

      <!-- Search -->
      <div class="bg-white rounded-lg border border-gray-200 p-4">
        <label class="text-sm font-medium text-gray-700 block mb-2">Pesquisar</label>
            <div class="relative">
              <Magnify class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-gray-400" />
              <Input
                v-model="searchTerm"
                placeholder="Pesquisar por nome, contacto, email ou NIF..."
                class="pl-10 bg-gray-100 border-none"
              />
            </div>
      </div>

      <!-- Stats Tables -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-white rounded-lg border border-gray-200 p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Total de Fornecedores</p>
            <p class="text-3xl font-bold text-gray-900 mt-1">{{ suppliers.length }}</p>
          </div>
          <div class="size-12 rounded-full bg-blue-100 flex items-center justify-center">
<TruckOutline class="size-6 text-blue-600" />
          </div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Fornecedores Ativos</p>
            <p class="text-3xl font-bold text-gray-900 mt-1">{{ suppliers.filter(s => s.status === 'Ativo').length }}</p>
          </div>
          <div class="size-12 rounded-full bg-green-100 flex items-center justify-center">
            <TruckOutline class="size-6 text-green-600" />
          </div>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">Fornecedores Inativos</p>
            <p class="text-3xl font-bold text-gray-900 mt-1">{{ suppliers.filter(s => s.status === 'Inativo').length }}</p>
          </div>
          <div class="size-12 rounded-full bg-gray-100 flex items-center justify-center">
            <TruckOutline class="size-6 text-gray-400" />
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="supplier in filteredSuppliers"
          :key="supplier.id"
          class="bg-white rounded-lg border border-gray-200 flex flex-col hover:shadow-md transition-shadow"
        >
          <div class="p-6 flex-1">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-4">
                <div class="size-12 rounded-full bg-blue-50 flex items-center justify-center shrink-0">
                  <TruckOutline class="size-6 text-blue-600" />
                </div>
                <h3 class="text-lg font-bold text-gray-900">{{ supplier.name }}</h3>
              </div>
              <span 
                :class="[
                  'px-2 py-1 rounded-full text-xs font-medium',
                  supplier.status === 'Ativo' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                ]"
              >
                {{ supplier.status }}
              </span>
            </div>

            <div class="space-y-4 text-sm">
              <div class="flex items-start gap-4 text-gray-600">
                <PhoneOutline class="size-5 shrink-0 text-gray-400" />
                <div class="flex flex-col">
                  <span class="font-medium text-gray-900">{{ supplier.contact }}</span>
                  <span>{{ supplier.phone }}</span>
                </div>
              </div>
              
              <div class="flex items-center gap-4 text-gray-600">
                <EmailOutline class="size-5 shrink-0" />
                <span class="truncate">{{ supplier.email }}</span>
              </div>
              <div class="flex items-start gap-4 text-gray-600">
                <MapMarkerOutline class="size-5 mt-0.5 shrink-0" />
                <span>{{ supplier.address }}</span>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-900 mb-1">Categorias:</p>
                <div class="flex flex-wrap gap-1">
                  <span 
                    v-for="cat in supplier.categories" 
                    :key="cat"
                    class="bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded text-[10px]"
                  >
                    {{ cat }}
                  </span>
                  <span v-if="!supplier.categories?.length" class="italic text-gray-400">Nenhuma categoria</span>
                </div>
              </div>
            </div>

            <div class="mt-6 pt-4 border-t border-gray-100">
              <p class="text-[10px] uppercase font-semibold text-gray-400 tracking-wider">Última Encomenda</p>
              <p class="text-sm text-gray-600">{{ supplier.lastOrderDate ? formatDate(supplier.lastOrderDate) : 'Sem registo' }}</p>
            </div>
          </div>

          <div v-if="canEdit" class="p-4 pt-0 grid grid-cols-3 gap-3">
            <button 
              @click="openEditDialog(supplier)"
              class="py-2.5 px-4 text-sm font-medium text-gray-600 hover:bg-gray-50 flex items-center justify-center relative border border-gray-200 rounded-lg transition-colors overflow-hidden group"
            >
              <PencilOutline class="size-4 text-gray-500 mr-2 group-hover:text-gray-700" />
              <span>Editar</span>
            </button>
            <button 
              @click="openDeleteDialog(supplier)"
              class="py-2.5 px-4 text-sm font-medium text-red-600 hover:bg-red-50 flex items-center justify-center relative border border-red-200 rounded-lg transition-colors overflow-hidden group"
            >
              <TrashCanOutline class="size-4 text-red-500 mr-2 group-hover:text-red-700" />
              <span>Eliminar</span>
            </button>
            <button
              v-if="canManageSupply"
              @click="openAssociateProductDialog(supplier)"
              class="py-2.5 px-4 text-sm font-medium text-blue-600 hover:bg-blue-50 flex items-center justify-center relative border border-blue-200 rounded-lg transition-colors overflow-hidden group"
            >
              <TagOutline class="size-4 text-blue-500 mr-2 group-hover:text-blue-700" />
              <span>Associar</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- New/Edit Supplier Dialog -->
    <Dialog :open="showDialog" @update:open="showDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>{{ isEditing ? 'Editar Fornecedor' : 'Novo Fornecedor' }}</DialogTitle>
          <DialogDescription>
            {{ isEditing ? 'Atualize as informações do fornecedor' : 'Preencha os dados do novo fornecedor' }}
          </DialogDescription>
        </DialogHeader>

        <div class="py-4 space-y-4">
          <!-- Name -->
          <div>
            <Label htmlFor="name">Nome da Empresa *</Label>
            <Input
              id="name"
              v-model="formData.name"
              placeholder="Ex: Fornecedor ABC Lda."
              :class="errors.name ? 'border-red-300' : ''"
            />
            <p v-if="errors.name" class="text-xs text-red-600 mt-1">{{ errors.name }}</p>
          </div>

          <!-- Contact Person -->
          <div>
            <Label htmlFor="contact">Pessoa de Contacto *</Label>
            <Input
              id="contact"
              v-model="formData.contact"
              placeholder="Ex: João Silva"
              :class="errors.contact ? 'border-red-300' : ''"
            />
            <p v-if="errors.contact" class="text-xs text-red-600 mt-1">{{ errors.contact }}</p>
          </div>

          <!-- Email -->
          <div>
            <Label htmlFor="email">Email *</Label>
            <Input
              id="email"
              v-model="formData.email"
              type="email"
              placeholder="exemplo@fornecedor.pt"
              :class="errors.email ? 'border-red-300' : ''"
            />
            <p v-if="errors.email" class="text-xs text-red-600 mt-1">{{ errors.email }}</p>
          </div>

          <!-- Phone -->
          <div>
            <Label htmlFor="phone">Telefone *</Label>
            <Input
              id="phone"
              v-model="formData.phone"
              placeholder="912345678"
              maxlength="9"
              @input="handlePhoneInput"
              :class="errors.phone ? 'border-red-300' : ''"
            />
            <p v-if="errors.phone" class="text-xs text-red-600 mt-1">{{ errors.phone }}</p>
            <p class="text-xs text-gray-500 mt-1">Formato: 9 dígitos</p>
          </div>

          <!-- NIF -->
          <div>
            <Label htmlFor="nif">NIF *</Label>
            <Input
              id="nif"
              v-model="formData.nif"
              placeholder="123456789"
              maxlength="9"
              @input="handleNifInput"
              :class="errors.nif ? 'border-red-300' : ''"
            />
            <p v-if="errors.nif" class="text-xs text-red-600 mt-1">{{ errors.nif }}</p>
            <p class="text-xs text-gray-500 mt-1">Formato: 9 dígitos numéricos</p>
          </div>

          <!-- Address -->
          <div>
            <Label htmlFor="address">Morada *</Label>
            <Input
              id="address"
              v-model="formData.address"
              placeholder="Rua, número, cidade"
              :class="errors.address ? 'border-red-300' : ''"
            />
            <p v-if="errors.address" class="text-xs text-red-600 mt-1">{{ errors.address }}</p>
          </div>

          <!-- Categories -->
          <div>
            <Label htmlFor="categories">Categorias (separadas por vírgula)</Label>
            <Input
              id="categories"
              v-model="formData.categories"
              placeholder="Ex: Bebidas, Snacks, Padaria"
            />
          </div>

          <!-- Status -->
          <div v-if="isEditing">
            <Label htmlFor="status">Estado</Label>
            <Select id="status" v-model="formData.status">
              <option value="Ativo">Ativo</option>
              <option value="Inativo">Inativo</option>
            </Select>
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
            {{ isEditing ? 'Guardar Alterações' : 'Criar Fornecedor' }}
          </Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Associate Product Dialog -->
    <Dialog :open="showAssociateDialog" @update:open="showAssociateDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Associar Produto ao Fornecedor</DialogTitle>
          <DialogDescription>
            {{ supplierToAssociate?.name }} - definir produto e preço de custo.
          </DialogDescription>
        </DialogHeader>
        <div class="py-4 space-y-4">
          <div>
            <Label>Produto *</Label>
            <Select v-model="associateForm.idProduto">
              <option value="">Selecione um produto</option>
              <option v-for="product in products" :key="product.id" :value="product.id">
                {{ product.name }}
              </option>
            </Select>
          </div>
          <div>
            <Label>Preço de custo *</Label>
            <Input
              type="number"
              min="0.01"
              step="0.01"
              :model-value="associateForm.precoCusto"
              @update:model-value="updateAssociatePriceInput"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="showAssociateDialog = false">Cancelar</Button>
          <Button class="bg-blue-600 hover:bg-blue-700 text-white" @click="confirmAssociateProduct">Associar</Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Delete Confirmation Dialog -->
    <Dialog :open="showDeleteDialog" @update:open="showDeleteDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Eliminar Fornecedor</DialogTitle>
          <DialogDescription>
            Tem a certeza que deseja eliminar "{{ supplierToDelete?.name }}"? Esta ação não pode ser revertida.
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
import { useSuppliersStore } from '../stores/suppliers';
import { useProductsStore } from '../stores/products';
import { useShopStore } from '../stores/shop';
import { localApiClient, globalApiClient } from '../services/apiClients';

const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
const client = isGlobal ? globalApiClient : localApiClient;
import { toast } from 'vue-sonner';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { generateUUID } from '../utils/uuid';
import TruckOutline from 'vue-material-design-icons/TruckOutline.vue';
import Magnify from 'vue-material-design-icons/Magnify.vue';
import PencilOutline from 'vue-material-design-icons/PencilOutline.vue';
import TrashCanOutline from 'vue-material-design-icons/TrashCanOutline.vue';
import AccountOutline from 'vue-material-design-icons/AccountOutline.vue';
import EmailOutline from 'vue-material-design-icons/EmailOutline.vue';
import PhoneOutline from 'vue-material-design-icons/PhoneOutline.vue';
import FileDocumentOutline from 'vue-material-design-icons/FileDocumentOutline.vue';
import MapMarkerOutline from 'vue-material-design-icons/MapMarkerOutline.vue';
import TagOutline from 'vue-material-design-icons/TagOutline.vue';
import Plus from 'vue-material-design-icons/Plus.vue';
import Button from '../components/ui/Button.vue';
import Input from '../components/ui/Input.vue';
import Label from '../components/ui/Label.vue';
import Dialog from '../components/ui/Dialog.vue';
import DialogHeader from '../components/ui/DialogHeader.vue';
import DialogTitle from '../components/ui/DialogTitle.vue';
import DialogDescription from '../components/ui/DialogDescription.vue';
import DialogFooter from '../components/ui/DialogFooter.vue';
import Select from '../components/ui/Select.vue';

const authStore = useAuthStore();
const suppliersStore = useSuppliersStore();
const productsStore = useProductsStore();
const shopStore = useShopStore();

onMounted(() => {
  suppliersStore.fetchSuppliers();
  productsStore.fetchProducts();
});

watch(
  () => shopStore.selectedShopId,
  () => {
    suppliersStore.fetchSuppliers();
    productsStore.fetchProducts();
  }
);

const canEdit = computed(() => authStore.canEditData);
const canManageSupply = computed(() => authStore.canManageSupply);
const suppliers = computed(() => suppliersStore.suppliers);
const products = computed(() => productsStore.products);

const searchTerm = ref('');
const showDialog = ref(false);
const showDeleteDialog = ref(false);
const showPasswordConfirm = ref(false);
const passwordConfirm = ref('');
const passwordConfirmError = ref('');
const pendingAction = ref(null);
const isEditing = ref(false);
const supplierToDelete = ref(null);
const currentSupplier = ref(null);
const showAssociateDialog = ref(false);
const supplierToAssociate = ref(null);
const associateForm = ref({
  idProduto: '',
  precoCusto: ''
});

const formData = ref({
  name: '',
  contact: '',
  email: '',
  phone: '',
  nif: '',
  address: ''
});

const errors = ref({});

const filteredSuppliers = computed(() => {
  if (!searchTerm.value) return suppliers.value;
  
  const term = searchTerm.value.toLowerCase();
  return suppliers.value.filter(s =>
    s.name.toLowerCase().includes(term) ||
    s.contact.toLowerCase().includes(term) ||
    s.email.toLowerCase().includes(term) ||
    s.nif.includes(term)
  );
});

const formatDate = (date) => {
  return format(new Date(date), "dd/MM/yyyy", { locale: ptBR });
};

const handleNifInput = (e) => {
  formData.value.nif = e.target.value.replace(/\D/g, '');
};

const handlePhoneInput = (e) => {
  formData.value.phone = e.target.value.replace(/\D/g, '');
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

const updateAssociatePriceInput = (value) => {
  associateForm.value.precoCusto = sanitizeMoneyInput(value);
};

const validateNif = (nif) => {
  if (!nif) return 'NIF é obrigatório';
  if (!/^\d{9}$/.test(nif)) return 'NIF deve ter exatamente 9 dígitos';
  return null;
};

const validateForm = () => {
  const newErrors = {};

  if (!formData.value.name.trim()) newErrors.name = 'Nome é obrigatório';
  if (!formData.value.contact.trim()) newErrors.contact = 'Contacto é obrigatório';
  if (!formData.value.email.trim()) {
    newErrors.email = 'Email é obrigatório';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    newErrors.email = 'Email inválido';
  }
  if (!formData.value.phone) {
    newErrors.phone = 'Telefone é obrigatório';
  } else if (!/^\d{9}$/.test(formData.value.phone)) {
    newErrors.phone = 'Telefone deve ter 9 dígitos';
  }
  if (!formData.value.address.trim()) newErrors.address = 'Morada é obrigatória';
  
  const nifError = validateNif(formData.value.nif);
  if (nifError) newErrors.nif = nifError;

  errors.value = newErrors;
  return Object.keys(newErrors).length === 0;
};

const openNewDialog = () => {
  isEditing.value = false;
  currentSupplier.value = null;
  formData.value = {
    name: '',
    contact: '',
    email: '',
    phone: '',
    nif: '',
    address: '',
    categories: '',
    status: 'Ativo'
  };
  errors.value = {};
  showDialog.value = true;
};

const openEditDialog = (supplier) => {
  isEditing.value = true;
  currentSupplier.value = supplier;
  formData.value = {
    name: supplier.name,
    contact: supplier.contact,
    email: supplier.email,
    phone: supplier.phone,
    nif: supplier.nif,
    address: supplier.address,
    categories: supplier.categories ? supplier.categories.join(', ') : '',
    status: supplier.status || 'Ativo'
  };
  errors.value = {};
  showDialog.value = true;
};

const closeDialog = () => {
  showDialog.value = false;
  formData.value = {
    name: '',
    contact: '',
    email: '',
    phone: '',
    nif: '',
    address: '',
    categories: '',
    status: 'Ativo'
  };
  errors.value = {};
};

const handleSubmit = () => {
  if (!validateForm()) {
    toast.error('Por favor, corrija os erros no formulário');
    return;
  }

  pendingAction.value = 'save';
  passwordConfirm.value = '';
  passwordConfirmError.value = '';
  showPasswordConfirm.value = true;
};

const executeSubmit = async () => {
  try {
    const supplierData = {
      idFornecedor: isEditing.value ? currentSupplier.value.id : generateUUID(),
      nome: formData.value.name.trim(),
      nif: formData.value.nif,
      telefone: formData.value.phone,
      email: formData.value.email.trim(),
      estado: formData.value.status
    };

    if (isEditing.value) {
      await suppliersStore.updateSupplier(currentSupplier.value.id, supplierData);
      toast.success('Fornecedor atualizado com sucesso');
    } else {
      await suppliersStore.addSupplier(supplierData);
      toast.success('Fornecedor criado com sucesso');
    }

    closeDialog();
    showPasswordConfirm.value = false;
  } catch (error) {
    console.error('Submit error:', error);
    toast.error(error.body?.message || error.message || 'Erro ao guardar fornecedor. Verifique os dados.');
  }
};

const openDeleteDialog = (supplier) => {
  supplierToDelete.value = supplier;
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
    await suppliersStore.deleteSupplier(supplierToDelete.value.id);
    toast.success('Fornecedor eliminado com sucesso');
    showDeleteDialog.value = false;
    supplierToDelete.value = null;
    showPasswordConfirm.value = false;
  } catch (error) {
    toast.error('Erro ao eliminar fornecedor.');
  }
};

const openAssociateProductDialog = (supplier) => {
  supplierToAssociate.value = supplier;
  associateForm.value = { idProduto: '', precoCusto: '' };
  showAssociateDialog.value = true;
};

const confirmAssociateProduct = async () => {
  if (!supplierToAssociate.value || !associateForm.value.idProduto || Number(associateForm.value.precoCusto) <= 0) {
    toast.error('Selecione produto e preço de custo válido.');
    return;
  }
  if (!hasAtMostTwoDecimals(associateForm.value.precoCusto)) {
    toast.error('O preço de custo deve ter no máximo 2 casas decimais.');
    return;
  }
  try {
    await client.post('/fornecedores/associar', {
      idProduto: associateForm.value.idProduto,
      idFornecedor: supplierToAssociate.value.id,
      precoCusto: Number(associateForm.value.precoCusto)
    });
    toast.success('Produto associado ao fornecedor com sucesso.');
    showAssociateDialog.value = false;
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao associar produto.');
  }
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
    }
    
    // Sucesso - Reset state
    passwordConfirm.value = '';
    showPasswordConfirm.value = false;
  } catch (error) {
    console.error('Action error:', error);
    toast.error('Ocorreu um erro ao processar a operação.');
  } finally {
    isVerifying.value = false;
  }
};
</script>
