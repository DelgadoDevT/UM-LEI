<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-full mx-auto space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Gestão de Funcionários</h1>
          <p class="text-sm text-gray-600 mt-1">Gerir perfis e permissões dos funcionários</p>
        </div>
      </div>

      <!-- Search and Filter -->
      <div class="bg-white rounded-lg border border-gray-200 p-4">
        <div class="flex flex-col md:flex-row items-end gap-4">
          <div class="w-full md:w-1/2">
            <label class="text-sm font-medium text-gray-700 block mb-2">Pesquisar</label>
            <div class="relative">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-gray-400" />
              <Input
                v-model="searchTerm"
                placeholder="Pesquisar funcionários..."
                class="pl-10 bg-gray-100 border-none"
              />
            </div>
          </div>
          <div class="flex-1"></div>
          <Button
            v-if="authStore.canModifyEmployeesAndProfiles"
            @click="openProfileDialog"
            variant="outline"
            class="w-full md:w-auto border-blue-600 text-blue-700 hover:bg-blue-50"
          >
            <Shield class="size-4 mr-2" />
            Gerir Perfis
          </Button>
          <Button
            v-if="authStore.canModifyEmployeesAndProfiles"
            @click="openNewDialog"
            class="bg-green-600 hover:bg-green-700 w-full md:w-auto"
          >
            <Plus class="size-4 mr-2" />
            Adicionar Funcionário
          </Button>
        </div>
      </div>

      <!-- Employees Table -->
      <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-200">
              <tr>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Funcionario</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Utilizador</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Contato</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Funcao</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Data de Registo</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Estado</th>
                <th class="text-right py-3 px-4 text-sm font-medium text-gray-700">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="employee in filteredEmployees"
                :key="employee.id"
                class="border-b border-gray-100 hover:bg-gray-50"
              >
                <td class="py-4 px-4 text-sm font-medium text-gray-900">
                  <div class="flex items-center gap-3">
                    <div class="p-2 bg-green-100 rounded-full border border-green-200">
                      <User class="size-5 text-green-700" />
                    </div>
                    {{ employee.name }}
                  </div>
                </td>
                <td class="py-4 px-4 text-sm text-gray-600">{{ employee.username }}</td>
                <td class="py-4 px-4 text-sm text-gray-600 text-xs">
                  <div class="flex flex-col gap-1">
                    <div class="flex items-center gap-1.5">
                      <Mail class="size-3.5 text-gray-400" />
                      {{ employee.email }}
                    </div>
                    <div class="flex items-center gap-1.5">
                      <Phone class="size-3.5 text-gray-400" />
                      {{ employee.phone }}
                    </div>
                  </div>
                </td>
                <td class="py-4 px-4 text-sm">
                  <span
                    :class="[
                      'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium gap-1.5',
                      getRoleBadgeClass(employee.role)
                    ]"
                  >
                    <Shield class="size-3.5" />
                    {{ employee.role }}
                  </span>
                </td>
                <td class="py-4 px-4 text-sm text-gray-600">
                  <div class="flex items-center gap-2">
                    <Calendar class="size-4 text-gray-400" />
                    {{ formatDate(employee.hireDate) }}
                  </div>
                </td>
                <td class="py-4 px-4">
                  <span
                    :class="[
                      'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium',
                      employee.status === 'Ativo' ? 'bg-green-600 text-white shadow-sm' : 'bg-gray-100 text-gray-700'
                    ]"
                  >
                    {{ employee.status || 'Ativo' }}
                  </span>
                </td>
                <td v-if="authStore.canModifyEmployeesAndProfiles" class="py-4 px-4 text-right">
                  <div class="flex items-center justify-end gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      @click="openEditDialog(employee)"
                      :disabled="!canModifyTarget(employee)"
                      :class="!canModifyTarget(employee) ? 'opacity-50 cursor-not-allowed' : ''"
                      :title="canModifyTarget(employee) ? 'Editar' : 'Não pode modificar contas iguais ou superiores'"
                    >
                      <SquarePen class="size-4 text-gray-500 hover:text-gray-700" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      @click="openAssignProfileDialog(employee)"
                      title="Atribuir Perfil"
                      :disabled="!canModifyTarget(employee)"
                      :class="!canModifyTarget(employee) ? 'opacity-50 cursor-not-allowed' : ''"
                    >
                      <Shield class="size-4 text-blue-600" />
                    </Button>
                    <Button
                      v-if="employee.status !== 'Inativo'"
                      variant="ghost"
                      size="sm"
                      @click="openBlockDialog(employee)"
                      :disabled="employee.email === 'dono@taki.pt' || !canModifyTarget(employee)"
                      :title="canModifyTarget(employee) ? 'Bloquear Conta' : 'Não pode modificar contas iguais ou superiores'"
                      :class="(employee.email === 'dono@taki.pt' || !canModifyTarget(employee)) ? 'opacity-50 cursor-not-allowed' : ''"
                    >
                      <Ban :class="['size-4', (employee.email === 'dono@taki.pt' || !canModifyTarget(employee)) ? 'text-gray-400' : 'text-orange-600']" />
                    </Button>
                    <Button
                      v-if="employee.status === 'Bloqueado'"
                      variant="ghost"
                      size="sm"
                      @click="openUnblockDialog(employee)"
                      :disabled="!canModifyTarget(employee)"
                      :class="!canModifyTarget(employee) ? 'opacity-50 cursor-not-allowed' : ''"
                      :title="canModifyTarget(employee) ? 'Desbloquear Conta' : 'Não pode modificar contas iguais ou superiores'"
                    >
                      <Shield class="size-4 text-green-600" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      @click="openDeleteDialog(employee)"
                      :disabled="employee.email === 'dono@taki.pt' || !canModifyTarget(employee)"
                      :class="(employee.email === 'dono@taki.pt' || !canModifyTarget(employee)) ? 'opacity-50 cursor-not-allowed' : ''"
                      :title="canModifyTarget(employee) ? 'Eliminar' : 'Não pode modificar contas iguais ou superiores'"
                    >
                      <Trash2 :class="['size-4', (employee.email === 'dono@taki.pt' || !canModifyTarget(employee)) ? 'text-gray-400' : 'text-red-600']" />
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- New/Edit Employee Dialog -->
    <Dialog :open="showDialog" @update:open="showDialog = $event">
      <div class="p-6 max-w-2xl">
        <DialogHeader>
          <DialogTitle>{{ isEditing ? 'Editar Funcionário' : 'Adicionar Funcionário' }}</DialogTitle>
          <DialogDescription>
            {{ isEditing ? 'Atualize as informações do funcionário' : 'Preencha os dados do novo funcionário' }}
          </DialogDescription>
        </DialogHeader>

        <div class="py-4 space-y-4 max-h-[60vh] overflow-y-auto">
          <!-- Name -->
          <div>
            <Label htmlFor="name">Nome Completo *</Label>
            <Input
              id="name"
              v-model="formData.name"
              placeholder="Ex: João Silva"
              :class="errors.name ? 'border-red-300' : ''"
            />
            <p v-if="errors.name" class="text-xs text-red-600 mt-1">{{ errors.name }}</p>
          </div>

          <!-- Role -->
          <div>
            <Label htmlFor="role">Função *</Label>
            <Select id="role" v-model="formData.role">
              <option value="">Selecione...</option>
              <option v-for="role in availableRoles" :key="role.id" :value="role.id">{{ role.name }}</option>
            </Select>
            <p v-if="errors.role" class="text-xs text-red-600 mt-1">{{ errors.role }}</p>
          </div>

          <!-- Username -->
          <div>
            <Label htmlFor="username">Nome de Utilizador *</Label>
            <Input
              id="username"
              v-model="formData.username"
              placeholder="Ex: joao.silva"
              :class="errors.username ? 'border-red-300' : ''"
            />
            <p v-if="errors.username" class="text-xs text-red-600 mt-1">{{ errors.username }}</p>
          </div>

          <!-- Password (only for new employees) -->
          <div v-if="!isEditing">
            <Label htmlFor="password">Palavra-passe *</Label>
            <Input
              id="password"
              v-model="formData.password"
              type="password"
              placeholder="Mínimo 6 caracteres"
              :class="errors.password ? 'border-red-300' : ''"
            />
            <p v-if="errors.password" class="text-xs text-red-600 mt-1">{{ errors.password }}</p>
          </div>

          <!-- Confirm Password (only for new employees) -->
          <div v-if="!isEditing">
            <Label htmlFor="confirmPassword">Confirmar Palavra-passe *</Label>
            <Input
              id="confirmPassword"
              v-model="formData.confirmPassword"
              type="password"
              placeholder="Repita a palavra-passe"
              :class="errors.confirmPassword ? 'border-red-300' : ''"
            />
            <p v-if="errors.confirmPassword" class="text-xs text-red-600 mt-1">{{ errors.confirmPassword }}</p>
          </div>

          <!-- Email -->
          <div>
            <Label htmlFor="email">Email *</Label>
            <Input
              id="email"
              v-model="formData.email"
              type="email"
              placeholder="exemplo@taki.pt"
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

          <!-- Hire Date -->
          <div>
            <Label htmlFor="hireDate">Data de Admissão *</Label>
            <Input
              id="hireDate"
              v-model="formData.hireDate"
              type="date"
              :class="errors.hireDate ? 'border-red-300' : ''"
            />
            <p v-if="errors.hireDate" class="text-xs text-red-600 mt-1">{{ errors.hireDate }}</p>
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
            {{ isEditing ? 'Guardar Alterações' : 'Adicionar Funcionário' }}
          </Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Profiles Management Dialog -->
    <Dialog :open="showProfilesDialog" @update:open="showProfilesDialog = $event">
      <div class="p-6 max-w-3xl">
        <DialogHeader>
          <DialogTitle>Gestão de Perfis de Acesso</DialogTitle>
          <DialogDescription>Criar e editar perfis (CU28/CU29).</DialogDescription>
        </DialogHeader>
        <div class="py-4 space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <Input v-model="profileForm.id" placeholder="ID do perfil (ex: GERENTE_LOJA)" />
            <Input v-model="profileForm.nome" placeholder="Nome do perfil" />
          </div>
          <div>
            <Label>Permissões</Label>
            <div class="mt-2 flex flex-wrap gap-2">
              <label v-for="perm in permissionOptions" :key="perm" class="inline-flex items-center gap-2 text-sm border rounded px-2 py-1">
                <input
                  type="checkbox"
                  :checked="profileForm.permissoes.includes(perm)"
                  @change="togglePermission(perm)"
                />
                {{ perm }}
              </label>
            </div>
          </div>
          <div v-if="authStore.canModifyEmployeesAndProfiles" class="flex gap-2">
            <Button class="bg-blue-600 hover:bg-blue-700 text-white" @click="submitProfile">
              {{ isEditingProfile ? 'Guardar Perfil' : 'Criar Perfil' }}
            </Button>
            <Button variant="outline" @click="resetProfileForm">Limpar</Button>
          </div>

          <div class="border rounded-md max-h-72 overflow-y-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left px-3 py-2">ID</th>
                  <th class="text-left px-3 py-2">Nome</th>
                  <th class="text-left px-3 py-2">Permissões</th>
                  <th class="text-right px-3 py-2">Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="profile in profiles" :key="profile.id || profile.nome" class="border-t">
                  <td class="px-3 py-2">{{ profile.id }}</td>
                  <td class="px-3 py-2">{{ profile.nome }}</td>
                  <td class="px-3 py-2">{{ (profile.permissoes || []).join(', ') }}</td>
                  <td v-if="authStore.canModifyEmployeesAndProfiles" class="px-3 py-2 text-right">
                    <Button size="sm" variant="outline" @click="openEditProfileDialog(profile)">Editar</Button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="showProfilesDialog = false">Fechar</Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Assign Profile Dialog -->
    <Dialog :open="showAssignProfileDialog" @update:open="showAssignProfileDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Atribuir Perfil</DialogTitle>
          <DialogDescription>
            Atribuir perfil de acesso ao funcionário {{ employeeToAssignProfile?.name }}.
          </DialogDescription>
        </DialogHeader>
        <div class="py-4">
          <Label>Perfil</Label>
          <Select v-model="selectedProfileName">
            <option value="">Selecione um perfil</option>
            <option v-for="profile in profiles" :key="profile.id || profile.nome" :value="profile.nome">
              {{ profile.nome }}
            </option>
          </Select>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="showAssignProfileDialog = false">Cancelar</Button>
          <Button class="bg-blue-600 hover:bg-blue-700 text-white" @click="confirmAssignProfile">Atribuir</Button>
        </DialogFooter>
      </div>
    </Dialog>

    <!-- Delete Confirmation Dialog -->
    <Dialog :open="showDeleteDialog" @update:open="showDeleteDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Eliminar Funcionário</DialogTitle>
          <DialogDescription>
            Tem a certeza que deseja eliminar "{{ employeeToDelete?.name }}"? Esta ação não pode ser revertida.
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
import { useEmployeesStore } from '../stores/employees';
import { useAuthStore } from '../stores/auth';

import { useShopStore } from '../stores/shop';
import { toast } from 'vue-sonner';
import { format } from 'date-fns';
import { generateUUID } from '../utils/uuid';
import { Plus, Search, SquarePen, Trash2, User, Mail, Phone, Shield, Calendar, Ban } from 'lucide-vue-next';
import Button from '../components/ui/Button.vue';
import Input from '../components/ui/Input.vue';
import Select from '../components/ui/Select.vue';
import Label from '../components/ui/Label.vue';
import Dialog from '../components/ui/Dialog.vue';
import DialogHeader from '../components/ui/DialogHeader.vue';
import DialogTitle from '../components/ui/DialogTitle.vue';
import DialogDescription from '../components/ui/DialogDescription.vue';
import DialogFooter from '../components/ui/DialogFooter.vue';

const employeesStore = useEmployeesStore();
const authStore = useAuthStore();
const shopStore = useShopStore();

onMounted(() => {
  employeesStore.fetchEmployees();
  employeesStore.fetchProfiles();
});

watch(
  () => shopStore.selectedShopId,
  () => {
    employeesStore.fetchEmployees();
  }
);

const employees = computed(() => employeesStore.employees);
const profiles = computed(() => employeesStore.profiles);
const canManageShops = computed(() => authStore.hasAccess('Gerente de Loja'));
const selectedShopId = computed(() => shopStore.selectedShopId);

const searchTerm = ref('');
const filterRole = ref('');
const showDialog = ref(false);
const showDeleteDialog = ref(false);
const showPasswordConfirm = ref(false);
const passwordConfirm = ref('');
const passwordConfirmError = ref('');
const pendingAction = ref(null);
const isEditing = ref(false);
const employeeToDelete = ref(null);
const currentEmployee = ref(null);
const employeeToBlock = ref(null);
const employeeToUnblock = ref(null);
const showProfilesDialog = ref(false);
const showAssignProfileDialog = ref(false);
const employeeToAssignProfile = ref(null);
const selectedProfileName = ref('');
const isEditingProfile = ref(false);
const editingProfileOriginalName = ref('');

const permissionOptions = ['REGISTAR_VENDA', 'GERIR_PRODUTOS', 'ADMINISTRAR_SISTEMA'];
const profileForm = ref({
  id: '',
  nome: '',
  permissoes: []
});

const roles = [
  { id: 'GESTOR_CENTRAL', name: 'Proprietário da Cadeia' },
  { id: 'ADMIN', name: 'Administrador do Sistema' },
  { id: 'GERENTE', name: 'Gerente de Loja' },
  { id: 'OPERADOR', name: 'Operador de Caixa' }
];
const ROLE_LEVEL = (perfil) => {
  if (!perfil) return 0;
  const p = perfil.toString().trim().toUpperCase()
    .normalize('NFD').replace(/\p{Diacritic}/gu, '');
  if (p.includes('PROPRIET') || p.includes('GESTOR_CENTRAL')) return 4;
  if (p.includes('ADMIN')) return 3;
  if (p.includes('GERENTE') || p.includes('GESTOR_LOJA')) return 2;
  if (p.includes('OPERADOR')) return 1;
  return 0;
};

const currentUserLevel = computed(() => ROLE_LEVEL(authStore.currentUser?.role));

const canModifyTarget = (employee) => {
  if (!employee || !authStore.currentUser) return false;
  if (employee.id === authStore.currentUser.id) return false;
  return ROLE_LEVEL(employee.role) < currentUserLevel.value;
};

const availableRoles = computed(() => {
  const dynamicProfiles = profiles.value.map(p => ({ id: p.id, name: p.nome }));
  const all = [...roles];
  dynamicProfiles.forEach(dp => {
    if (!all.find(a => a.id === dp.id)) all.push(dp);
  });
  // Filtra para esconder perfis de nível >= ao do utilizador autenticado
  return all.filter(r => ROLE_LEVEL(r.id) < currentUserLevel.value);
});

const formData = ref({
  name: '',
  role: '',
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nif: '',
  hireDate: '',
  status: 'Ativo'
});

const errors = ref({});

const filteredEmployees = computed(() => {
  let result = employees.value;

  // Filter by allowed usernames logic removed so we can see the DB users

  // If role has shop management access, filter by selected shop
  if (canManageShops.value && selectedShopId.value && selectedShopId.value !== 'all') {
    result = result.filter(e => String(e.shopId) === String(selectedShopId.value) || String(e.shopId) === 'all');
  }

  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase();
    result = result.filter(e =>
      e.name.toLowerCase().includes(term) ||
      e.username.toLowerCase().includes(term) ||
      e.email.toLowerCase().includes(term)
    );
  }

  if (filterRole.value) {
    result = result.filter(e => e.role === filterRole.value);
  }

  return result;
});

const getRoleBadgeClass = (role) => {
  const r = (role || '').toUpperCase();
  if (r === 'PROPRIETÁRIO DA CADEIA' || r === 'PROPRIETARIO') return 'bg-red-100 text-red-700';
  if (r === 'ADMINISTRADOR DO SISTEMA' || r === 'ADMIN') return 'bg-purple-100 text-purple-700';
  if (r === 'GERENTE DE LOJA' || r === 'GERENTE' || r === 'GESTOR_LOJA') return 'bg-blue-100 text-blue-700';
  if (r === 'OPERADOR DE CAIXA' || r === 'OPERADOR' || r === 'OPERADOR_CAIXA') return 'bg-green-100 text-green-700';
  return 'bg-gray-100 text-gray-700';
};

const formatDate = (dateString) => {
  return format(new Date(dateString), 'dd/MM/yyyy');
};

const handleNifInput = (e) => {
  formData.value.nif = e.target.value.replace(/\D/g, '');
};

const handlePhoneInput = (e) => {
  formData.value.phone = e.target.value.replace(/\D/g, '');
};

const validateNif = (nif) => {
  if (!nif) return 'NIF é obrigatório';
  if (!/^\d{9}$/.test(nif)) return 'NIF deve ter exatamente 9 dígitos';
  return null;
};

const validateForm = () => {
  const newErrors = {};

  if (!formData.value.name.trim()) newErrors.name = 'Nome é obrigatório';
  if (!formData.value.role) newErrors.role = 'Função é obrigatória';
  if (!formData.value.username.trim()) newErrors.username = 'Utilizador é obrigatório';
  
  if (!isEditing.value) {
    if (!formData.value.password) {
      newErrors.password = 'Palavra-passe é obrigatória';
    } else if (formData.value.password.length < 6) {
      newErrors.password = 'Palavra-passe deve ter pelo menos 6 caracteres';
    }
    
    if (!formData.value.confirmPassword) {
      newErrors.confirmPassword = 'Confirmação de palavra-passe é obrigatória';
    } else if (formData.value.password !== formData.value.confirmPassword) {
      newErrors.confirmPassword = 'As palavras-passe não coincidem';
    }
  }

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
  
  const nifError = validateNif(formData.value.nif);
  if (nifError) newErrors.nif = nifError;

  if (!formData.value.hireDate) newErrors.hireDate = 'Data de admissão é obrigatória';

  errors.value = newErrors;
  return Object.keys(newErrors).length === 0;
};

const openNewDialog = () => {
  isEditing.value = false;
  currentEmployee.value = null;
  formData.value = {
    name: '',
    role: profiles.value[0]?.nome || '',
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: '',
    nif: '',
    hireDate: format(new Date(), 'yyyy-MM-dd'),
    status: 'Ativo'
  };
  errors.value = {};
  showDialog.value = true;
};

const openProfileDialog = async () => {
  try {
    await employeesStore.fetchProfiles();
    showProfilesDialog.value = true;
  } catch (error) {
    toast.error('Erro ao carregar perfis.');
  }
};

const resetProfileForm = () => {
  profileForm.value = { id: '', nome: '', permissoes: [] };
  isEditingProfile.value = false;
  editingProfileOriginalName.value = '';
};

const togglePermission = (permission) => {
  if (profileForm.value.permissoes.includes(permission)) {
    profileForm.value.permissoes = profileForm.value.permissoes.filter(p => p !== permission);
  } else {
    profileForm.value.permissoes = [...profileForm.value.permissoes, permission];
  }
};

const openEditProfileDialog = (profile) => {
  profileForm.value = {
    id: profile.id || profile.nome,
    nome: profile.nome,
    permissoes: [...(profile.permissoes || [])]
  };
  isEditingProfile.value = true;
  editingProfileOriginalName.value = profile.nome;
};

const submitProfile = async () => {
  if (!profileForm.value.id || !profileForm.value.nome || profileForm.value.permissoes.length === 0) {
    toast.error('Preencha ID, nome e pelo menos uma permissão.');
    return;
  }
  try {
    if (isEditingProfile.value) {
      await employeesStore.updateProfile(editingProfileOriginalName.value, { permissoes: profileForm.value.permissoes });
      toast.success('Perfil atualizado com sucesso.');
    } else {
      await employeesStore.createProfile({
        id: profileForm.value.id,
        nome: profileForm.value.nome,
        permissoes: profileForm.value.permissoes
      });
      toast.success('Perfil criado com sucesso.');
    }
    await employeesStore.fetchProfiles();
    resetProfileForm();
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao guardar perfil.');
  }
};

const openAssignProfileDialog = async (employee) => {
  try {
    employeeToAssignProfile.value = employee;
    selectedProfileName.value = employee.role || '';
    await employeesStore.fetchProfiles();
    showAssignProfileDialog.value = true;
  } catch (error) {
    toast.error('Erro ao carregar perfis.');
  }
};

const confirmAssignProfile = async () => {
  if (!employeeToAssignProfile.value || !selectedProfileName.value) {
    toast.error('Selecione um perfil.');
    return;
  }
  try {
    await employeesStore.assignProfile(employeeToAssignProfile.value.id, selectedProfileName.value);
    toast.success('Perfil atribuído com sucesso.');
    showAssignProfileDialog.value = false;
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao atribuir perfil.');
  }
};

const openEditDialog = (employee) => {
  isEditing.value = true;
  currentEmployee.value = employee;
  
  // Encontrar o ID do perfil se o que temos é o nome (para compatibilidade)
  let roleId = employee.role;
  const found = availableRoles.value.find(r => r.name === employee.role || r.id === employee.role);
  if (found) roleId = found.id;

  formData.value = {
    name: employee.name,
    role: roleId,
    username: employee.username,
    password: '',
    confirmPassword: '',
    email: employee.email,
    phone: employee.phone,
    nif: employee.nif,
    hireDate: employee.hireDate,
    status: employee.status || 'Ativo'
  };
  errors.value = {};
  showDialog.value = true;
};

const closeDialog = () => {
  showDialog.value = false;
  formData.value = {
    name: '',
    role: '',
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: '',
    nif: '',
    hireDate: '',
    status: 'Ativo'
  };
  errors.value = {};
};

const handleSubmit = () => {
  if (!validateForm()) {
    toast.error('Por favor, corrija os erros no formulário');
    return;
  }

  // Solicitar confirmação de password por questões de segurança
  pendingAction.value = 'save';
  passwordConfirm.value = '';
  passwordConfirmError.value = '';
  showPasswordConfirm.value = true;
};

const executeSubmit = async () => {
  try {
    const lojaSelecionada = selectedShopId.value && selectedShopId.value !== 'all'
      ? Number(selectedShopId.value)
      : (authStore.currentUser?.idLoja || 1);
    const employeeData = {
      id: isEditing.value ? currentEmployee.value.id : generateUUID(),
      nome: formData.value.name.trim(),
      email: formData.value.email.trim(),
      password: formData.value.password,
      idPerfilAcesso: formData.value.role,
      idLoja: lojaSelecionada
    };

    if (isEditing.value) {
      await employeesStore.updateEmployee(currentEmployee.value.id, employeeData);
      toast.success('Funcionário atualizado com sucesso');
    } else {
      await employeesStore.addEmployee(employeeData);
      toast.success('Funcionário adicionado com sucesso');
    }

    closeDialog();
    showPasswordConfirm.value = false;
  } catch (error) {
    console.error('Submit error:', error);
    toast.error(error.body?.message || error.message || 'Erro ao guardar funcionário.');
  }
};

const openDeleteDialog = (employee) => {
  employeeToDelete.value = employee;
  showDeleteDialog.value = true;
};

const confirmDelete = () => {
  // Solicitar confirmação de password por questões de segurança
  pendingAction.value = 'delete';
  passwordConfirm.value = '';
  passwordConfirmError.value = '';
  showPasswordConfirm.value = true;
};

const executeDelete = async () => {
  if (employeeToDelete.value.username === 'owner' || employeeToDelete.value.username === 'admin') {
    toast.error('Não é possível eliminar a conta do Proprietário ou do Administrador');
    showDeleteDialog.value = false;
    employeeToDelete.value = null;
    showPasswordConfirm.value = false;
    return;
  }
  try {
    await employeesStore.deleteEmployee(employeeToDelete.value.id, passwordConfirm.value);
    toast.success('Funcionário eliminado com sucesso');
    showDeleteDialog.value = false;
    employeeToDelete.value = null;
    showPasswordConfirm.value = false;
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao eliminar funcionário');
  }
};

const openBlockDialog = (employee) => {
  employeeToBlock.value = employee;
  pendingAction.value = 'block';
  passwordConfirm.value = '';
  passwordConfirmError.value = '';
  showPasswordConfirm.value = true;
};

const openUnblockDialog = (employee) => {
  employeeToUnblock.value = employee;
  pendingAction.value = 'unblock';
  passwordConfirm.value = '';
  passwordConfirmError.value = '';
  showPasswordConfirm.value = true;
};

const executeBlock = async () => {
  if (employeeToBlock.value.username === 'owner' || employeeToBlock.value.username === 'admin') {
    toast.error('Não é possível bloquear a conta do Proprietário ou do Administrador');
    employeeToBlock.value = null;
    showPasswordConfirm.value = false;
    return;
  }
  try {
    await employeesStore.blockEmployee(employeeToBlock.value.id, passwordConfirm.value);
    toast.success('Funcionário bloqueado com sucesso');
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao bloquear funcionário');
  }
  employeeToBlock.value = null;
  showPasswordConfirm.value = false;
};

const executeUnblock = async () => {
  if (!employeeToUnblock.value) return;
  try {
    await employeesStore.unblockEmployee(employeeToUnblock.value.id, passwordConfirm.value);
    toast.success('Funcionário desbloqueado com sucesso');
  } catch (error) {
    toast.error(error.body?.message || 'Erro ao desbloquear funcionário');
  }
  employeeToUnblock.value = null;
  showPasswordConfirm.value = false;
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
    } else if (pendingAction.value === 'block') {
      await executeBlock();
    } else if (pendingAction.value === 'unblock') {
      await executeUnblock();
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
</script>
