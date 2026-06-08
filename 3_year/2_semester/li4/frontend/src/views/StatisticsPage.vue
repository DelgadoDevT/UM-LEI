<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-7xl mx-auto space-y-6">
      <!-- Header -->
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Estatísticas</h1>
        <p class="text-sm text-gray-600 mt-1">Análise detalhada do desempenho do negócio</p>
      </div>

      <!-- Charts Row 1: Evolução Mensal & Vendas por Categoria -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Evolução de Vendas Mensal (Line Chart) -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-start justify-between mb-4">
            <div>
              <h2 class="text-lg font-semibold text-gray-900">Evolução de Vendas Mensal</h2>
              <p class="text-sm text-gray-500">Vendas e transações dos últimos 3 meses</p>
            </div>
            <div class="flex items-center gap-4 text-xs mt-1">
              <div class="flex items-center gap-1.5">
                <div class="size-3 rounded-full bg-[#16a34a]"></div>
                <span class="text-gray-600">Vendas (€)</span>
              </div>
              <div class="flex items-center gap-1.5">
                <div class="size-3 rounded-full bg-[#3b82f6]"></div>
                <span class="text-gray-600">Transações</span>
              </div>
            </div>
          </div>
          <div class="h-[300px] flex items-center justify-center overflow-hidden">
            <CustomLineChart
              :data="monthlySalesData"
              :width="550"
              :height="280"
            />
          </div>
        </div>

        <!-- Vendas por Categoria (Pie Chart) -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <h2 class="text-lg font-semibold text-gray-900">Vendas por Categoria</h2>
          <p class="text-sm text-gray-500 mb-4">Distribuição percentual das vendas</p>
          <div class="h-[300px] flex items-center justify-center overflow-hidden">
            <CustomPieChart
              :data="categoryData"
              :width="500"
              :height="300"
              :animations="true"
              :tooltip="true"
              :labels="true"
              :palette="pieChartPalette"
            />
          </div>
        </div>
      </div>

      <!-- Charts Row 2: Tráfego por Horas -->
      <div class="grid grid-cols-1 gap-6">
        <!-- Tráfego por Horas (Bar Chart) -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
          <h2 class="text-lg font-semibold text-gray-900">Tráfego por Horas</h2>
          <p class="text-sm text-gray-500 mb-4">Número de clientes por hora do dia</p>
          <div class="h-[350px] flex items-center justify-center overflow-hidden">
            <CustomBarChart
              :data="hourlyTrafficData"
              :width="1000"
              :height="320"
              :animations="true"
              :tooltip="true"
              :gridlines="true"
              :palette="['#3b82f6']"
            />
          </div>
        </div>
      </div>

      <!-- Resumo por Período (KPI Cards) -->
      <div class="bg-white rounded-xl border border-gray-100 p-8 shadow-sm">
        <div class="mb-8">
          <h2 class="text-xl font-bold text-gray-900">Resumo do Período</h2>
          <p class="text-gray-500 mt-1">Principais indicadores de desempenho</p>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <!-- Vendas Totais -->
          <div class="flex flex-col items-center">
            <div class="size-14 rounded-full bg-green-50 flex items-center justify-center mb-4">
              <CurrencyEur class="size-7 text-green-500" />
            </div>
            <span class="text-2xl font-extrabold text-gray-900">€{{ kpis.receitaHoje?.toFixed(2) || '0.00' }}</span>
            <span class="text-sm text-gray-500 mb-3">Vendas Hoje</span>
            <div class="px-3 py-1 rounded-full bg-green-50 flex items-center gap-1">
              <span class="text-xs font-bold text-green-600">Atualizado</span>
            </div>
          </div>

          <!-- Transações -->
          <div class="flex flex-col items-center">
            <div class="size-14 rounded-full bg-blue-50 flex items-center justify-center mb-4">
              <CartOutline class="size-7 text-blue-500" />
            </div>
            <span class="text-2xl font-extrabold text-gray-900">{{ kpis.vendasHoje || 0 }}</span>
            <span class="text-sm text-gray-500 mb-3">Transações Hoje</span>
            <div class="px-3 py-1 rounded-full bg-blue-50 flex items-center gap-1">
              <span class="text-xs font-bold text-blue-600">Em tempo real</span>
            </div>
          </div>

          <!-- Ticket Médio -->
          <div class="flex flex-col items-center">
            <div class="size-14 rounded-full bg-purple-50 flex items-center justify-center mb-4">
              <CurrencyEur class="size-7 text-purple-500" />
            </div>
            <span class="text-2xl font-extrabold text-gray-900">€{{ (kpis.receitaHoje / (kpis.vendasHoje || 1)).toFixed(2) }}</span>
            <span class="text-sm text-gray-500 mb-3">Ticket Médio</span>
            <div class="px-3 py-1 rounded-full bg-purple-50 flex items-center gap-1">
              <span class="text-xs font-bold text-purple-600">Hoje</span>
            </div>
          </div>

          <!-- Valor de Stock -->
          <div class="flex flex-col items-center">
            <div class="size-14 rounded-full bg-orange-50 flex items-center justify-center mb-4">
              <PackageVariantClosed class="size-7 text-orange-500" />
            </div>
            <span class="text-2xl font-extrabold text-gray-900">€{{ kpis.valorStock?.toFixed(2) || '0.00' }}</span>
            <span class="text-sm text-gray-500 mb-3">Valor de Stock</span>
            <div class="px-3 py-1 rounded-full bg-orange-50 flex items-center gap-1">
              <span class="text-xs font-bold text-orange-600">Estimado</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue';
import { useStatisticsStore } from '../stores/statistics';
import { useShopStore } from '../stores/shop';
import CustomLineChart from '../components/CustomLineChart.vue';
import CustomPieChart from '../components/CustomPieChart.vue';
import CustomBarChart from '../components/CustomBarChart.vue';
import CurrencyEur from 'vue-material-design-icons/CurrencyEur.vue';
import CartOutline from 'vue-material-design-icons/CartOutline.vue';
import PackageVariantClosed from 'vue-material-design-icons/PackageVariantClosed.vue';

const statsStore = useStatisticsStore();
const shopStore = useShopStore();

onMounted(() => {
  statsStore.fetchAll();
});

watch(
  () => shopStore.selectedShopId,
  () => {
    statsStore.fetchAll();
  }
);

// Dados para Evolução de Vendas Mensal
const monthlySalesData = computed(() => [
  {
    title: 'Vendas (€)',
    values: statsStore.monthlySales.map(d => ({ x: d.mes, y: d.totalVendas }))
  },
  {
    title: 'Transações',
    values: statsStore.monthlySales.map(d => ({ x: d.mes, y: d.totalTransacoes }))
  }
]);

// Fallback para quando não há dados
const hasMonthlyData = computed(() => statsStore.monthlySales.length > 0);

// Dados para Vendas por Categoria
const categoryData = computed(() => {
  if (statsStore.categorySales.length === 0) {
      return [
        { name: 'Sem Dados', value: 1 }
      ];
  }
  return statsStore.categorySales.map(c => ({
    name: c.categoria,
    value: c.totalFaturado
  }));
});

const pieChartPalette = ['#3b82f6', '#f97316', '#a855f7', '#eab308', '#ec4899', '#22c55e'];

// Dados para Tráfego por Horas
const hourlyTrafficData = computed(() => [
  {
    entity: 'Clientes',
    data: statsStore.hourlyTraffic.map(d => ({ x: d.hora, y: d.totalClientes }))
  }
]);

const kpis = computed(() => statsStore.kpis);
</script>
