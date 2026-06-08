import { defineStore } from 'pinia';
import { ref } from 'vue';
import { localApiClient, globalApiClient } from '../services/apiClients';

export const useSuppliersStore = defineStore('suppliers', () => {
  const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
  const client = isGlobal ? globalApiClient : localApiClient;

  const suppliers = ref([]);

  const fetchSuppliers = async () => {
    try {
      const data = await client.get('/fornecedores');
      suppliers.value = data.map(s => ({
        id: s.idFornecedor,
        name: s.nome,
        nif: s.nif || '',
        phone: s.telefone || s.contacto || '',
        email: s.email,
        status: s.estado || (s.ativo === false ? 'Inativo' : 'Ativo'),
        contact: s.nome, // Using name as contact if not present in backend
        address: 'N/A', // Backend Fornecedor doesn't have address
        categories: []
      }));
    } catch (error) {
      console.error('Error fetching suppliers:', error);
      throw error;
    }
  };

  const addSupplier = async (supplier) => {
    try {
      await client.post('/fornecedores', supplier);
      await fetchSuppliers();
    } catch (error) {
      console.error('Error adding supplier:', error);
      throw error;
    }
  };

  const updateSupplier = async (id, updatedSupplier) => {
    try {
      const result = await client.patch(`/fornecedores/${id}`, updatedSupplier);
      const index = suppliers.value.findIndex(s => s.id === id);
      if (index !== -1) {
        suppliers.value[index] = {
          ...suppliers.value[index],
          id: result.idFornecedor ?? suppliers.value[index].id,
          name: result.nome ?? suppliers.value[index].name,
          nif: result.nif ?? suppliers.value[index].nif,
          phone: result.telefone ?? suppliers.value[index].phone,
          email: result.email ?? suppliers.value[index].email,
          status: result.estado ?? suppliers.value[index].status
        };
      }
    } catch (error) {
      console.error('Error updating supplier:', error);
      throw error;
    }
  };

  const deleteSupplier = async (id) => {
    try {
      await client.patch(`/fornecedores/${id}/inativar`);
      const index = suppliers.value.findIndex(s => s.id === id);
      if (index !== -1) {
        suppliers.value[index] = { ...suppliers.value[index], status: 'Inativo' };
      }
    } catch (error) {
      console.error('Error deleting supplier:', error);
      throw error;
    }
  };

  const $reset = () => {
    suppliers.value = [];
  };

  return {
    suppliers,
    $reset,
    fetchSuppliers,
    addSupplier,
    updateSupplier,
    deleteSupplier
  };
});
