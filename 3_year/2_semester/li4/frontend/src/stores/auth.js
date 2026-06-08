import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { globalApiClient, localApiClient } from '../services/apiClients';

const ROLE_HIERARCHY = {
  'PROPRIETARIO DA CADEIA': 4,
  'PROPRIETÁRIO DA CADEIA': 4,
  'PROPRIETARIO': 4,
  'GESTOR_CENTRAL': 4,
  'ADMIN': 3,
  'ADMINISTRADOR DO SISTEMA': 3,
  'ADMINISTRADOR': 3,
  'GERENTE': 2,
  'GERENTE DE LOJA': 2,
  'GESTOR_LOJA': 2,
  'GESTOR_ARMAZEM': 2,
  'OPERADOR': 1,
  'OPERADOR DE CAIXA': 1,
  'OPERADOR_CAIXA': 1
};

const normalizeRole = (rawRole) => {
  const role = (rawRole || '').toString().trim().toUpperCase();
  if (!role) return '';
  if (role.includes('GESTOR_CENTRAL')) return 'GESTOR_CENTRAL';
  if (role.includes('PROPRIET')) return 'PROPRIETÁRIO DA CADEIA';
  if (role.includes('ADMIN')) return 'ADMINISTRADOR DO SISTEMA';
  if (role.includes('GERENTE') || role.includes('GESTOR_LOJA')) return 'GERENTE DE LOJA';
  if (role.includes('OPERADOR')) return 'OPERADOR DE CAIXA';
  return role;
};

const normalizeUser = (user) => {
  if (!user) return null;
  return {
    ...user,
    name: user.name || user.nome || user.username || '',
    username: user.username || user.nome || user.name || '',
    role: normalizeRole(user.role || user.nomePerfil || user.idPerfilAcesso)
  };
};

// Decide qual API usar para autenticação com base no modo do frontend
const authApiClient = ['global', 'central'].includes(import.meta.env.APP_MODE) ? globalApiClient : localApiClient;

export const useAuthStore = defineStore('auth', () => {
  const savedUserRaw = sessionStorage.getItem('taki_user');
  const savedUser = savedUserRaw ? normalizeUser(JSON.parse(savedUserRaw)) : null;
  const currentUser = ref(savedUser);
  const isAuthenticated = ref(!!savedUser);
  const nodeRole = ref(['global', 'central'].includes(import.meta.env.APP_MODE) ? 'HUB' : 'BRANCH');

  const login = async (username, password) => {
    try {
      const response = await authApiClient.post('/auth/login', { email: username, password });
      if (response && response.funcionario) {
        const f = response.funcionario;
        const user = normalizeUser({
          ...f,
          role: f.role || f.nomePerfil || f.idPerfilAcesso,
          name: f.nome,
          username: f.nome
        });
        currentUser.value = user;
        isAuthenticated.value = true;
        sessionStorage.setItem('taki_user', JSON.stringify(user));
        return true;
      }
      return false;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      await authApiClient.post('/auth/logout');
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      currentUser.value = null;
      isAuthenticated.value = false;
      sessionStorage.removeItem('taki_user');
      window.location.href = '/login';
    }
  };

  const hasAccess = (requiredRole) => {
    if (!currentUser.value) return false;
    const userRole = (currentUser.value.role || '').toUpperCase();
    const reqRole = (requiredRole || '').toUpperCase();
    return (ROLE_HIERARCHY[userRole] || 0) >= (ROLE_HIERARCHY[reqRole] || 0);
  };

  const verifyCurrentUserPassword = async (password) => {
    if (!currentUser.value) return false;
    try {
      const response = await authApiClient.post('/auth/verify-password', { password });
      return !!response.valid;
    } catch (error) {
      console.error('Verify password failed:', error);
      return false;
    }
  };

  const isHubNode = computed(() => nodeRole.value === 'HUB');

  const canAccessPOS = computed(() => {
    const role = (currentUser.value?.role || '').toUpperCase();
    return role.includes('OPERADOR');
  });

  const defaultLandingPath = computed(() => {
    const role = (currentUser.value?.role || '').toUpperCase();
    if (role.includes('OPERADOR')) return '/app/pos';
    return '/app/dashboard';
  });
  const canAccessEmployees = computed(() => {
    const role = (currentUser.value?.role || '').toUpperCase();
    return role.includes('PROPRIET') || role.includes('ADMIN') || role.includes('GERENTE') || role.includes('GESTOR');
  });
  const canAccessDashboard = computed(() => {
    const role = (currentUser.value?.role || '').toUpperCase();
    return !!currentUser.value && !role.includes('OPERADOR');
  });
  const canAccessReports = computed(() => {
    const role = (currentUser.value?.role || '').toUpperCase();
    return !!currentUser.value && !role.includes('OPERADOR');
  });
  const canAccessStatistics = computed(() => {
    const role = (currentUser.value?.role || '').toUpperCase();
    return !!currentUser.value && !role.includes('OPERADOR');
  });

  const canEditData = computed(() => {
    if (isHubNode.value) return false; // HUB node is always read-only for local data operations
    
    const role = (currentUser.value?.role || '').toUpperCase();
    if (role.includes('PROPRIET') || role.includes('ADMIN')) return true; // Owner/Admin can edit in Local Shops
    return role.includes('GERENTE') || role.includes('GESTOR_LOJA');
  });

  const canManageSupply = computed(() => {
    if (isHubNode.value) return false; // HUB node is always read-only for local data operations

    const role = (currentUser.value?.role || '').toUpperCase();
    if (role.includes('PROPRIET') || role.includes('ADMIN')) return true; // Owner/Admin can manage stock in Local Shops
    return role.includes('GERENTE') || role.includes('GESTOR_LOJA');
  });

  const canModifyEmployeesAndProfiles = computed(() => {
    if (isHubNode.value) return false; // No modifications on HUB node

    const role = (currentUser.value?.role || '').toUpperCase();
    // Proprietário, Admin e Gerente podem modificar funcionários (sujeito a hierarquia
    // verificada por linha em canModifyTarget); Operador não.
    return role.includes('PROPRIET')
      || role.includes('ADMIN')
      || role.includes('GERENTE')
      || role.includes('GESTOR_LOJA')
      || role.includes('GESTOR_CENTRAL');
  });

  return {
    currentUser,
    isAuthenticated,
    nodeRole, // Expose nodeRole
    isHubNode, // Expose isHubNode
    login,
    logout,
    hasAccess,
    verifyCurrentUserPassword,
    canAccessPOS,
    defaultLandingPath,
    canAccessEmployees,
    canAccessDashboard,
    canAccessReports,
    canAccessStatistics,
    canEditData,
    canManageSupply,
    canModifyEmployeesAndProfiles // Expose the new computed property
  };
});
