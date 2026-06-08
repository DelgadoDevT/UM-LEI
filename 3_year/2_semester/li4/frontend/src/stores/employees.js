import { defineStore } from 'pinia';
import { ref } from 'vue';
import { localApiClient, globalApiClient } from '../services/apiClients';

export const useEmployeesStore = defineStore('employees', () => {
  const employees = ref([]);
  const profiles = ref([]);
  const isGlobalMode = ['global', 'central'].includes(import.meta.env.APP_MODE);
  const apiClient = isGlobalMode ? globalApiClient : localApiClient;

  const fetchEmployees = async () => {
    try {
      const shopStore = (await import('./shop')).useShopStore();
      const selectedShopId = shopStore.selectedShopId;
      const shopId = selectedShopId === 'all' || selectedShopId == null ? 0 : Number(selectedShopId);
      const data = await apiClient.get(`/funcionarios?idLoja=${shopId}`);
      employees.value = data.map(e => ({
        id: e.id,
        name: e.nome,
        username: e.nome.toLowerCase().replace(/\s/g, '.'),
        email: e.email,
        role: e.role || e.idPerfilAcesso,
        shopId: e.idLoja == null ? 'all' : String(e.idLoja),
        status: e.estadoConta === 'ATIVO' ? 'Ativo' : (e.estadoConta === 'BLOQUEADO' ? 'Bloqueado' : 'Inativo'),
        hireDate: '2025-01-01' // Placeholder
      }));
    } catch (error) {
      console.error('Error fetching employees:', error);
      throw error;
    }
  };

  const addEmployee = async (employee) => {
    try {
      await apiClient.post('/funcionarios', employee);
      await fetchEmployees();
    } catch (error) {
      console.error('Error adding employee:', error);
      throw error;
    }
  };

  const updateEmployee = async (id, updatedEmployee) => {
    try {
      const result = await apiClient.patch(`/funcionarios/${id}`, updatedEmployee);
      const index = employees.value.findIndex(e => e.id === id);
      if (index !== -1) {
        employees.value[index] = {
          ...employees.value[index],
          id: result.id ?? employees.value[index].id,
          name: result.nome ?? employees.value[index].name,
          email: result.email ?? employees.value[index].email,
          role: result.idPerfilAcesso ?? employees.value[index].role,
          shopId: result.idLoja ?? employees.value[index].shopId,
          status: result.estadoConta === 'ATIVO' ? 'Ativo' : (result.estadoConta === 'BLOQUEADO' ? 'Bloqueado' : employees.value[index].status)
        };
      }
    } catch (error) {
      console.error('Error updating employee:', error);
      throw error;
    }
  };

  const deleteEmployee = async (id, passwordAdministrador) => {
    try {
      await apiClient.delete(`/funcionarios/${id}`, { body: { passwordAdministrador } });
      employees.value = employees.value.filter(e => e.id !== id);
    } catch (error) {
      console.error('Error deleting employee:', error);
      throw error;
    }
  };

  const blockEmployee = async (id, passwordAdministrador) => {
    try {
      await apiClient.post(`/funcionarios/${id}/bloquear`, { passwordAdministrador });
      const index = employees.value.findIndex(e => e.id === id);
      if (index !== -1) {
        employees.value[index] = { ...employees.value[index], status: 'Bloqueado' };
      }
    } catch (error) {
      console.error('Error blocking employee:', error);
      throw error;
    }
  };

  const unblockEmployee = async (id, passwordAdministrador) => {
    try {
      await apiClient.post(`/funcionarios/${id}/desbloquear`, { passwordAdministrador });
      const index = employees.value.findIndex(e => e.id === id);
      if (index !== -1) {
        employees.value[index] = { ...employees.value[index], status: 'Ativo' };
      }
    } catch (error) {
      console.error('Error unblocking employee:', error);
      throw error;
    }
  };

  const fetchProfiles = async () => {
    try {
      profiles.value = await apiClient.get('/perfis');
    } catch (error) {
      console.error('Error fetching profiles:', error);
      throw error;
    }
  };

  const createProfile = async (profile) => {
    try {
      await apiClient.post('/perfis', profile);
      await fetchProfiles();
    } catch (error) {
      console.error('Error creating profile:', error);
      throw error;
    }
  };

  const updateProfile = async (profileName, profile) => {
    try {
      await apiClient.patch(`/perfis/${encodeURIComponent(profileName)}`, profile);
      await fetchProfiles();
    } catch (error) {
      console.error('Error updating profile:', error);
      throw error;
    }
  };

  const assignProfile = async (employeeId, nomePerfil) => {
    try {
      await apiClient.patch(`/funcionarios/${employeeId}/perfil`, { nomePerfil });
      await fetchEmployees();
    } catch (error) {
      console.error('Error assigning profile:', error);
      throw error;
    }
  };

  const $reset = () => {
    employees.value = [];
  };

  return {
    employees,
    profiles,
    $reset,
    fetchEmployees,
    addEmployee,
    updateEmployee,
    deleteEmployee,
    blockEmployee,
    unblockEmployee,
    fetchProfiles,
    createProfile,
    updateProfile,
    assignProfile
  };
});
