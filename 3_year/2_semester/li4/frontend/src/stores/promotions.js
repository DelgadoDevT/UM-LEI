import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { toast } from 'vue-sonner';
import { localApiClient, globalApiClient } from '../services/apiClients';
import { generateUUID } from '../utils/uuid';
import { useShopStore } from './shop';

export const usePromotionsStore = defineStore('promotions', () => {
  const promotions = ref([]);
  const isGlobalMode = ['global', 'central'].includes(import.meta.env.APP_MODE);
  const apiClient = isGlobalMode ? globalApiClient : localApiClient;

  const fetchPromotions = async () => {
    try {
      promotions.value = await apiClient.get('/promocoes/ativas');
    } catch (error) {
      console.error('Error fetching promotions:', error);
    }
  };

  const getActivePromotions = computed(() => {
    const now = new Date();
    return promotions.value.filter(p => {
      const start = new Date(p.dataInicio || p.startDate);
      const end = new Date(p.dataFim || p.endDate);
      const estado = (p.estado || '').toLowerCase();
      return now >= start && now <= end && (estado === '' || estado === 'ativa');
    });
  });

  const addPromotion = async (promotion) => {
    try {
      const shopStore = useShopStore();
      const authStore = (await import('./auth')).useAuthStore();
      
      // Formatar datas para o formato que o backend espera (LocalDateTime)
      let start = promotion.startDate || promotion.dataInicio;
      if (start && start.length === 10) start += 'T00:00:00';
      
      let end = promotion.endDate || promotion.dataFim;
      if (end && end.length === 10) end += 'T23:59:59';

      // Garantir que idLoja é um número válido e não 'all'
      let shopId = shopStore.selectedShopId;
      if (shopId === 'all' || !shopId) {
        shopId = authStore.currentUser?.idLoja || 1;
      }
      shopId = parseInt(shopId);

      const payload = {
        idPromocao: promotion.idPromocao || generateUUID(),
        designacao: promotion.designacao || ('Promocao ' + (promotion.productId || '')),
        desconto: parseFloat(promotion.desconto ?? promotion.discount ?? 0),
        dataInicio: start,
        dataFim: end,
        idLoja: shopId,
        produtos: promotion.produtos || (promotion.productId ? [promotion.productId] : []),
        categorias: promotion.categorias || []
      };
      
      console.log('Sending promotion payload:', payload);
      await apiClient.post('/promocoes', payload);
      await fetchPromotions();
    } catch (error) {
      console.error('Error adding promotion details:', error.body || error);
      throw error;
    }
  };

  const removePromotion = async (promotionId) => {
    try {
      await apiClient.post(`/promocoes/${promotionId}/cancelar`, { motivo: 'Cancelamento manual' });
      const index = promotions.value.findIndex(p => (p.idPromocao || p.id) === promotionId);
      if (index !== -1) {
        promotions.value[index] = { ...promotions.value[index], estado: 'Cancelada' };
      }
      await fetchPromotions();
      toast.success('Promocao removida!');
    } catch (error) {
      console.error('Error removing promotion:', error);
      throw error;
    }
  };

  const $reset = () => {
    promotions.value = [];
  };

  return {
    promotions,
    $reset,
    fetchPromotions,
    getActivePromotions,
    addPromotion,
    removePromotion,
  };
});
