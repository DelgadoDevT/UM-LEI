import { defineStore } from 'pinia';
import { ref } from 'vue';
import { localApiClient, globalApiClient } from '../services/apiClients';
import { useShopStore } from './shop';

export const useStatisticsStore = defineStore('statistics', () => {
  const kpis = ref({
    vendasHoje: 0,
    receitaHoje: 0,
    valorStock: 0
  });
  
  const monthlySales = ref([]);
  const hourlyTraffic = ref([]);
  const categorySales = ref([]);
  const isGlobalMode = ['global', 'central'].includes(import.meta.env.APP_MODE);

  const getClientAndUrl = (localUrl) => {
    if (!isGlobalMode) {
      return { client: localApiClient, url: localUrl };
    }
    const globalMap = {
      '/estatisticas/kpi': '/estatisticas/kpi-gerais',
      '/estatisticas/vendas-mensais': '/estatisticas/vendas-mensais',
      '/estatisticas/vendas-hora': '/estatisticas/vendas-horarias',
      '/estatisticas/vendas-categoria': '/estatisticas/vendas-categoria'
    };
    const base = localUrl.split('?')[0];
    const query = localUrl.includes('?') ? `?${localUrl.split('?')[1]}` : '';
    return { client: globalApiClient, url: `${globalMap[base] || base}${query}` };
  };

  const fetchKpis = async () => {
    try {
      const shopStore = useShopStore();
      const shopId = shopStore.isAllShops() ? 0 : Number(shopStore.selectedShopId);
      const { client, url } = getClientAndUrl(`/estatisticas/kpi?idLoja=${shopId}`);
      kpis.value = await client.get(url);
    } catch (error) {
      console.error('Error fetching KPIs:', error);
    }
  };

  const fetchMonthlySales = async (meses = 3) => {
    try {
      const shopStore = useShopStore();
      const shopId = shopStore.isAllShops() ? 0 : Number(shopStore.selectedShopId);
      const { client, url } = getClientAndUrl(`/estatisticas/vendas-mensais?idLoja=${shopId}&meses=${meses}`);
      monthlySales.value = await client.get(url);
    } catch (error) {
      console.error('Error fetching monthly sales:', error);
    }
  };

  const fetchHourlyTraffic = async () => {
    try {
      const shopStore = useShopStore();
      const shopId = shopStore.isAllShops() ? 0 : Number(shopStore.selectedShopId);
      const { client, url } = getClientAndUrl(`/estatisticas/vendas-hora?idLoja=${shopId}`);
      hourlyTraffic.value = await client.get(url);
    } catch (error) {
      console.error('Error fetching hourly traffic:', error);
    }
  };

  const fetchCategorySales = async () => {
    try {
      const shopStore = useShopStore();
      const shopId = shopStore.isAllShops() ? 0 : Number(shopStore.selectedShopId);
      const { client, url } = getClientAndUrl(`/estatisticas/vendas-categoria?idLoja=${shopId}`);
      categorySales.value = await client.get(url);
    } catch (error) {
      console.error('Error fetching category sales:', error);
    }
  };

  const fetchAll = async () => {
    await Promise.all([
      fetchKpis(),
      fetchMonthlySales(),
      fetchHourlyTraffic(),
      fetchCategorySales()
    ]);
  };

  const $reset = () => {
    kpis.value = {
      vendasHoje: 0,
      receitaHoje: 0,
      valorStock: 0
    };
    monthlySales.value = [];
    hourlyTraffic.value = [];
    categorySales.value = [];
  };

  return {
    kpis,
    monthlySales,
    hourlyTraffic,
    categorySales,
    $reset,
    fetchKpis,
    fetchMonthlySales,
    fetchHourlyTraffic,
    fetchCategorySales,
    fetchAll
  };
});
