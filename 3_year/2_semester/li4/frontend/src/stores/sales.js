import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { localApiClient, globalApiClient } from '../services/apiClients';

export const useSalesStore = defineStore('sales', () => {
  const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
  const client = isGlobal ? globalApiClient : localApiClient;

  const sales = ref([]);
  const returnedItemsQuantities = ref({});

  const normalizeNumber = (value, fallback = 0) => {
    const n = Number(value);
    return Number.isFinite(n) ? n : fallback;
  };

  const normalizeText = (value, fallback = '') => {
    if (value === null || value === undefined) return fallback;
    return String(value);
  };

  const mapDevolucao = (d = {}) => ({
    id: d.idDevolucao ?? d.id ?? '',
    numFatura: normalizeText(d.idVenda ?? d.numFatura ?? d.idVendaOriginal, ''),
    date: d.dataHora ?? d.data ?? null,
    total: -normalizeNumber(d.valor, 0),
    paymentMethod: `Devolução (${normalizeText(d.metodoReembolso, 'Original')})`,
    cashier: d.idFuncionario ?? '',
    cashierId: d.idFuncionario ?? '',
    items: [],
    isDevolucao: true
  });

  const mapSale = (v = {}) => ({
    id: v.idVenda ?? v.id ?? '',
    numFatura: normalizeText(v.fatura?.numFatura ?? v.numFatura ?? v.idVenda ?? v.id, ''),
    date: v.dataHora ?? v.data ?? null,
    total: normalizeNumber(v.total ?? v.valorTotal, 0),
    paymentMethod: normalizeText(v.metodoPagamento, 'Numerário'),
    cashier: v.nomeFuncionario ?? v.idFuncionario ?? '',
    cashierId: v.idFuncionario ?? '',
    items: Array.isArray(v.linhas) ? v.linhas : []
  });

  const fetchSales = async () => {
    try {
      const shopStore = (await import('./shop')).useShopStore();
      const shopId = shopStore.selectedShopId === 'all' || shopStore.selectedShopId == null
        ? 0
        : Number(shopStore.selectedShopId);

      const devolucoesPromise = isGlobal
        ? client.get(`/devolucoes?idLoja=${shopId}`)
        : localApiClient.get(`/devolucoes?idLoja=${shopId}`);
      const linhasDevolucaoPromise = isGlobal
        ? Promise.resolve({})
        : localApiClient.get('/devolucoes/linhas');

      const [data, devData, qData] = await Promise.allSettled([
        client.get(`/vendas?idLoja=${shopId}`),
        devolucoesPromise,
        linhasDevolucaoPromise,
      ]);

      const vendas = data.status === 'fulfilled' && Array.isArray(data.value)
        ? data.value
        : [];
      let allTransactions = vendas.map(mapSale);

      if (devData.status === 'fulfilled' && Array.isArray(devData.value)) {
        const devolucoes = devData.value.map(mapDevolucao);
        allTransactions = [...allTransactions, ...devolucoes];
      }

      if (qData.status === 'fulfilled') {
        returnedItemsQuantities.value = qData.value || {};
      }

      sales.value = allTransactions;
    } catch (error) {
      console.error('Error fetching sales:', error);
      throw error;
    }
  };

  const addSale = async (sale) => {
    try {
      // intentionally local-only
      const venda = await localApiClient.post('/vendas/iniciar', {
        idLoja: Number(sale.idLoja || 1),
        idFuncionario: sale.idFuncionario || sale.cashierId || sale.cashier || ''
      });

      for (const item of sale.items || []) {
        // intentionally local-only
        await localApiClient.post('/vendas/' + venda.idVenda + '/linhas', {
          idProduto: item.productId || item.idProduto || item.id,
          quantidade: Number(item.quantity || item.quantidade || 0)
        });
      }

      const pedidoRegisto = {
        metodoPagamento: sale.paymentMethod || 'Numerário'
      };

      if (sale.valorEntregue != null) {
        const valorEntregue = Number(sale.valorEntregue);
        if (Number.isFinite(valorEntregue) && valorEntregue >= 0) {
          pedidoRegisto.valorEntregue = valorEntregue;
        }
      }

      // intentionally local-only
      const result = await localApiClient.post('/vendas/' + venda.idVenda + '/registar', pedidoRegisto);

      const mappedSale = mapSale(result);
      sales.value = [mappedSale, ...sales.value.filter(s => s.id !== mappedSale.id)];

      // Refresca lista em background
      fetchSales().catch(() => {});

      return result;
    } catch (error) {
      console.error('Error adding sale:', error);
      throw error;
    }
  };

  const processReturn = async (idVenda, linhas) => {
    try {
      // intentionally local-only
      await localApiClient.post('/vendas/' + idVenda + '/devolucoes', { linhas });
      await fetchSales();
    } catch (error) {
      console.error('Error processing return:', error);
      throw error;
    }
  };

  const getTodaySales = computed(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return sales.value.filter(sale => {
      const saleDate = new Date(sale.date);
      saleDate.setHours(0, 0, 0, 0);
      return saleDate.getTime() === today.getTime();
    });
  });

  const $reset = () => {
    sales.value = [];
    returnedItemsQuantities.value = {};
  };

  return {
    sales,
    returnedItemsQuantities,
    $reset,
    fetchSales,
    addSale,
    processReturn,
    getTodaySales
  };
});
