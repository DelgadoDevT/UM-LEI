<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-7xl mx-auto space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Relatórios</h1>
        <p class="text-sm text-gray-600 mt-1">Análise e exportação de dados</p>
      </div>

      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Filtros</h2>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <Label for="reportType">Tipo de Relatório</Label>
            <Select id="reportType" name="reportType" v-model="filters.type">
              <option value="sales">Vendas</option>
              <option value="inventory">Inventário</option>
            </Select>
          </div>

          <div>
            <Label for="startDate">Data Início</Label>
            <Input
                id="startDate"
                name="startDate"
                v-model="filters.startDate"
                type="date"
                :class="errors.startDate ? 'border-red-300' : ''"
            />
            <p v-if="errors.startDate" class="text-xs text-red-600 mt-1">{{ errors.startDate }}</p>
          </div>

          <div>
            <Label for="endDate">Data Fim</Label>
            <Input
                id="endDate"
                name="endDate"
                v-model="filters.endDate"
                type="date"
                :class="errors.endDate ? 'border-red-300' : ''"
            />
            <p v-if="errors.endDate" class="text-xs text-red-600 mt-1">{{ errors.endDate }}</p>
          </div>

          <div class="flex items-end gap-2">
            <Button
                @click="generateReport"
                class="!bg-green-600 hover:!bg-green-700 text-white flex items-center justify-center gap-2 w-full md:w-auto"
            >
              <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="shrink-0"
              >
                <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <line x1="10" y1="9" x2="8" y2="9"/>
              </svg>
              <span>Gerar Relatório</span>
            </Button>
          </div>
        </div>
      </div>

      <div class="bg-blue-100/50 rounded-lg border border-blue-200 p-6 flex flex-col md:flex-row items-center gap-6">
        <div class="size-12 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
          <FileQuestion class="size-6 text-blue-600" />
        </div>
        <div class="flex-1 space-y-1">
          <h3 class="text-lg font-semibold text-blue-900">Ajuda com Relatórios</h3>
          <p class="text-sm text-blue-600">
            Precisa de ajuda para interpretar os dados? Consulte o nosso guia de relatórios ou contacte o suporte técnico para assistência.
          </p>
          <div class="pt-2">
            <Button @click="() => router.push('/app/help')" variant="outline" class="bg-white text-black border-gray-300 hover:bg-gray-50">
              Guia de Ajuda
            </Button>
          </div>
        </div>
      </div>

      <div v-if="reportGenerated" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="bg-white rounded-lg border border-gray-200 p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-600">Total de Vendas</p>
                <p class="text-2xl font-bold text-gray-900 mt-1">{{ reportData.totalSales }}</p>
              </div>
              <div class="size-12 rounded-full bg-blue-100 flex items-center justify-center">
                <ShoppingCart class="size-6 text-blue-600" />
              </div>
            </div>
          </div>

          <div class="bg-white rounded-lg border border-gray-200 p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-600">Receita Total</p>
                <p class="text-2xl font-bold text-gray-900 mt-1">€{{ reportData.totalRevenue.toFixed(2) }}</p>
              </div>
              <div class="size-12 rounded-full bg-green-100 flex items-center justify-center">
                <Banknote class="size-6 text-green-600" />
              </div>
            </div>
          </div>

          <div class="bg-white rounded-lg border border-gray-200 p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-600">Ticket Médio</p>
                <p class="text-2xl font-bold text-gray-900 mt-1">€{{ reportData.avgTicket.toFixed(2) }}</p>
              </div>
              <div class="size-12 rounded-full bg-purple-100 flex items-center justify-center">
                <BarChart3 class="size-6 text-purple-600" />
              </div>
            </div>
          </div>
        </div>

        <div v-if="filters.type === 'sales'" class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900">Relatório de Vendas</h2>
            <div class="flex gap-2">
              <Button variant="outline" size="sm" class="text-gray-900 border-gray-300" @click="exportReport('excel')">
                <Download class="size-4 mr-2" />
                Excel
              </Button>
              <Button variant="outline" size="sm" class="text-gray-900 border-gray-300" @click="exportReport('pdf')">
                <Download class="size-4 mr-2" />
                PDF
              </Button>
            </div>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-gray-50 border-b border-gray-200">
              <tr>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">ID</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Data/Hora</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Itens</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Total</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Pagamento</th>
                <th class="text-left py-3 px-4 text-sm font-medium text-gray-700">Operador</th>
              </tr>
              </thead>
              <tbody>
              <tr
                  v-for="sale in pagedSales"
                  :key="sale.id"
                  class="border-b border-gray-100 hover:bg-gray-50"
              >
                <td class="py-3 px-4 text-sm text-gray-900">{{ sale.id }}</td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ formatDateTime(sale.date) }}</td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ sale.items.length }}</td>
                <td class="py-3 px-4 text-sm font-medium text-green-600">€{{ sale.total.toFixed(2) }}</td>
                <td class="py-3 px-4 text-sm">
                    <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-700">
                      {{ sale.paymentMethod }}
                    </span>
                </td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ sale.cashier }}</td>
              </tr>
              </tbody>
            </table>
          </div>
          <Pagination
            :total-items="filteredSales.length"
            :current-page="salesReportPage"
            :page-size="salesReportPageSize"
            @update:current-page="salesReportPage = $event"
            @update:page-size="salesReportPageSize = $event"
          />
        </div>

        <div v-if="filters.type === 'inventory'" class="bg-white rounded-lg border border-gray-200 p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900">Relatório de Inventário</h2>
            <div class="flex gap-2">
              <Button variant="outline" size="sm" class="text-gray-900 border-gray-300" @click="exportReport('excel')">
                <Download class="size-4 mr-2" />
                Excel
              </Button>
              <Button variant="outline" size="sm" class="text-gray-900 border-gray-300" @click="exportReport('pdf')">
                <Download class="size-4 mr-2" />
                PDF
              </Button>
            </div>
          </div>

          <div class="space-y-4">
            <div v-if="lowStockProducts.length > 0">
              <h3 class="text-sm font-semibold text-red-700 mb-2 flex items-center gap-2">
                <AlertCircle class="size-4" />
                Produtos com Stock Baixo ({{ lowStockProducts.length }})
              </h3>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
                <div
                    v-for="product in pagedLowStock"
                    :key="product.id"
                    class="flex items-center justify-between p-3 bg-red-50 rounded-lg border border-red-200"
                >
                  <div>
                    <p class="text-sm font-medium text-gray-900">{{ product.name }}</p>
                    <p class="text-xs text-gray-600">Stock: {{ product.stock }} | Mín: {{ product.minStock }}</p>
                  </div>
                  <div class="text-2xl">{{ product.image }}</div>
                </div>
              </div>
              <Pagination
                class="mt-3 rounded-lg border border-gray-100"
                :total-items="lowStockProducts.length"
                :current-page="lowStockPage"
                :page-size="lowStockPageSize"
                :page-size-options="[6, 12, 24, 48]"
                @update:current-page="lowStockPage = $event"
                @update:page-size="lowStockPageSize = $event"
              />
            </div>

            <div>
              <h3 class="text-sm font-semibold text-gray-700 mb-2">Stock por Categoria</h3>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <div
                    v-for="cat in stockByCategory"
                    :key="cat.category"
                    class="p-4 bg-gray-50 rounded-lg border border-gray-200"
                >
                  <p class="text-xs text-gray-600">{{ cat.category }}</p>
                  <p class="text-xl font-bold text-gray-900 mt-1">{{ cat.total }}</p>
                  <p class="text-xs text-gray-500">unidades</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p class="text-sm text-blue-800">
            <span class="font-semibold">Período:</span> {{ formatDate(filters.startDate) }} a {{ formatDate(filters.endDate) }}
          </p>
        </div>
      </div>
    </div>

    <Dialog :open="showConfirmDialog" @update:open="showConfirmDialog = $event">
      <div class="p-6">
        <DialogHeader>
          <DialogTitle>Confirmar Geração de Relatório</DialogTitle>
          <DialogDescription>
            Confirme os dados para a geração do relatório.
          </DialogDescription>
        </DialogHeader>

        <div class="py-4 space-y-4">
          <div class="bg-gray-50 p-4 rounded-lg border border-gray-200 space-y-2">
            <p class="text-sm text-gray-700">
              <span class="font-semibold">Tipo:</span>
              {{ filters.type === 'sales' ? 'Vendas' : 'Inventário' }}
            </p>
            <p class="text-sm text-gray-700">
              <span class="font-semibold">Início:</span> {{ formatDate(filters.startDate) }}
            </p>
            <p class="text-sm text-gray-700">
              <span class="font-semibold">Fim:</span> {{ formatDate(filters.endDate) }}
            </p>
          </div>

          <div class="flex flex-col gap-2">
            <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Opções de Exportação</p>
            <div class="grid grid-cols-2 gap-2">
              <Button variant="outline" class="text-gray-900 border-gray-300" @click="exportReport('pdf')">
                <Download class="size-4 mr-2 text-gray-900" />
                Exportar PDF
              </Button>
              <Button variant="outline" class="text-gray-900 border-gray-300" @click="exportReport('excel')">
                <Download class="size-4 mr-2 text-gray-900" />
                Exportar Excel
              </Button>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" class="text-gray-900 border-gray-300" @click="showConfirmDialog = false">
            Cancelar
          </Button>
          <Button @click="confirmAndGenerateReport" class="bg-green-600 hover:bg-green-700 text-white">
            Confirmar e Ver
          </Button>
        </DialogFooter>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useProductsStore } from '../stores/products';
import { useSalesStore } from '../stores/sales';
import { useShopStore } from '../stores/shop';
import { localApiClient, globalApiClient } from '../services/apiClients';
import { toast } from 'vue-sonner';
import { format, parseISO, isWithinInterval } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import {
  Download,
  ShoppingCart,
  Banknote,
  BarChart3,
  AlertCircle,
  FileQuestion
} from 'lucide-vue-next';

// Imports de UI
import Button from '../components/ui/Button.vue';
import Input from '../components/ui/Input.vue';
import Select from '../components/ui/Select.vue';
import Label from '../components/ui/Label.vue';

// Imports de Dialog
import Dialog from '../components/ui/Dialog.vue';
import DialogHeader from '../components/ui/DialogHeader.vue';
import DialogTitle from '../components/ui/DialogTitle.vue';
import DialogDescription from '../components/ui/DialogDescription.vue';
import DialogFooter from '../components/ui/DialogFooter.vue';
import Pagination from '../components/ui/Pagination.vue';

import { useRouter } from 'vue-router';

const router = useRouter();
const productsStore = useProductsStore();
const salesStore = useSalesStore();
const shopStore = useShopStore();
const isGlobalMode = ['global', 'central'].includes(import.meta.env.APP_MODE);

const filters = ref({
  type: 'sales',
  startDate: format(new Date(new Date().setDate(new Date().getDate() - 7)), 'yyyy-MM-dd'),
  endDate: format(new Date(), 'yyyy-MM-dd')
});

const errors = ref({});
const reportGenerated = ref(false);
const showConfirmDialog = ref(false);

const reportData = ref({
  totalSales: 0,
  totalRevenue: 0,
  avgTicket: 0
});

const validateDates = () => {
  const newErrors = {};

  if (!filters.value.startDate) {
    newErrors.startDate = 'Data início é obrigatória';
  }

  if (!filters.value.endDate) {
    newErrors.endDate = 'Data fim é obrigatória';
  }

  if (filters.value.startDate && filters.value.endDate) {
    const start = parseISO(filters.value.startDate);
    const end = parseISO(filters.value.endDate);

    if (start > end) {
      newErrors.endDate = 'Data fim deve ser posterior à data início';
    }
  }

  errors.value = newErrors;
  return Object.keys(newErrors).length === 0;
};

const filteredSales = computed(() => {
  if (!filters.value.startDate || !filters.value.endDate) {
    return salesStore.sales;
  }

  const start = parseISO(filters.value.startDate);
  const end = parseISO(filters.value.endDate);
  end.setHours(23, 59, 59, 999);

  return salesStore.sales.filter(sale => {
    const saleDate = new Date(sale.date);
    return isWithinInterval(saleDate, { start, end });
  });
});

const lowStockProducts = computed(() => productsStore.getLowStockProducts());

const salesReportPage = ref(1);
const salesReportPageSize = ref(20);
const lowStockPage = ref(1);
const lowStockPageSize = ref(12);

watch(() => filters.value.startDate, () => { salesReportPage.value = 1; });
watch(() => filters.value.endDate, () => { salesReportPage.value = 1; });

const pagedSales = computed(() => {
  const start = (salesReportPage.value - 1) * salesReportPageSize.value;
  return filteredSales.value.slice(start, start + salesReportPageSize.value);
});

const pagedLowStock = computed(() => {
  const start = (lowStockPage.value - 1) * lowStockPageSize.value;
  return lowStockProducts.value.slice(start, start + lowStockPageSize.value);
});

const stockByCategory = computed(() => {
  const categories = {};

  productsStore.products.forEach(product => {
    if (!categories[product.category]) {
      categories[product.category] = 0;
    }
    categories[product.category] += product.stock;
  });

  return Object.entries(categories).map(([category, total]) => ({
    category,
    total
  }));
});

const generateReport = () => {
  if (!validateDates()) {
    toast.error('Por favor, corrija os erros nos filtros');
    return;
  }

  showConfirmDialog.value = true;
};

const confirmAndGenerateReport = () => {
  const sales = filteredSales.value;
  const totalRevenue = sales.reduce((sum, sale) => sum + sale.total, 0);
  const avgTicket = sales.length > 0 ? totalRevenue / sales.length : 0;

  reportData.value = {
    totalSales: sales.length,
    totalRevenue,
    avgTicket
  };

  reportGenerated.value = true;
  showConfirmDialog.value = false;

  toast.success('Relatório gerado com sucesso');
};

const triggerDownload = (blob, filename) => {
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
};

const parseTaxaIvaPct = (raw) => {
  if (raw === null || raw === undefined) return 0;
  const s = String(raw);
  const m = s.match(/(\d+(?:[.,]\d+)?)/);
  if (!m) return 0;
  let n = Number(m[1].replace(',', '.'));
  if (!Number.isFinite(n)) return 0;
  if (n < 1) n = n * 100;
  return Math.round(n * 100) / 100;
};

const buildSalesDetailedRows = () => {
  const rows = [];
  filteredSales.value.forEach(sale => {
    const linhas = sale.items || [];
    if (linhas.length === 0) {
      const total = Number(sale.total || 0);
      const iva = Number(sale.imposto || 0);
      const subtotal = Number(sale.subtotal || (total - iva));
      rows.push({
        fatura: sale.numFatura || sale.id,
        data: formatDateTime(sale.date),
        operador: sale.cashier || '',
        produto: '',
        quantidade: 0,
        precoBase: 0,
        subtotal,
        taxaIvaPct: 0,
        iva,
        totalLinha: total,
        pagamento: sale.paymentMethod || ''
      });
      return;
    }
    linhas.forEach((line) => {
      const quantidade = Number(line.quantidade || 0);
      const totalLinha = Number(line.totalFinal || line.subtotal || (quantidade * Number(line.preco || 0)));
      const taxaPct = parseTaxaIvaPct(line.produto?.taxaIva ?? line.taxaIva);
      const subtotalLinha = taxaPct > 0 ? totalLinha / (1 + taxaPct / 100) : totalLinha;
      const ivaLinha = totalLinha - subtotalLinha;
      const precoBase = quantidade > 0 ? subtotalLinha / quantidade : 0;
      rows.push({
        fatura: sale.numFatura || sale.id,
        data: formatDateTime(sale.date),
        operador: sale.cashier || '',
        produto: line.produto?.nome || line.nome || line.idProduto || 'Produto',
        quantidade,
        precoBase,
        subtotal: subtotalLinha,
        taxaIvaPct: taxaPct,
        iva: ivaLinha,
        totalLinha,
        pagamento: sale.paymentMethod || ''
      });
    });
  });
  return rows;
};

const buildInventoryDetailedRows = () => {
  return productsStore.products.map((product) => ({
    produto: product.name || product.id,
    categoria: product.category || 'Sem Categoria',
    stockAtual: Number(product.stock ?? 0),
    stockMinimo: Number(product.minStock ?? 0),
    estado: Number(product.stock ?? 0) <= Number(product.minStock ?? 0) ? 'Abaixo do mínimo' : 'OK'
  }));
};

const escapePdfText = (value) => String(value ?? '').replace(/\\/g, '\\\\').replace(/\(/g, '\\(').replace(/\)/g, '\\)');
const buildSimplePdf = (lines) => {
  const yStart = 780;
  const contentLines = lines.slice(0, 45).map((line, i) => `50 ${yStart - (i * 14)} Td (${escapePdfText(line)}) Tj`).join('\n');
  const stream = `BT /F1 10 Tf\n${contentLines}\nET`;
  const objects = [
    '1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n',
    '2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n',
    '3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n',
    '4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n',
    `5 0 obj << /Length ${stream.length} >> stream\n${stream}\nendstream endobj\n`
  ];
  let pdf = '%PDF-1.4\n';
  const offsets = [];
  objects.forEach(obj => {
    offsets.push(pdf.length);
    pdf += obj;
  });
  const xrefOffset = pdf.length;
  pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
  offsets.forEach(offset => {
    pdf += `${String(offset).padStart(10, '0')} 00000 n \n`;
  });
  pdf += `trailer << /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF`;
  return new Blob([pdf], { type: 'application/pdf' });
};

const exportReport = async (format) => {
  if (!validateDates()) {
    toast.error('Por favor, corrija os erros nos filtros');
    return;
  }

  try {
    const reportType = filters.value.type;
    const startDate = filters.value.startDate;
    const endDate = filters.value.endDate;
    const extension = format === 'excel' ? 'csv' : 'pdf';
    const filename = `Relatorio_${reportType}_${startDate}_ate_${endDate}.${extension}`;

    if (format === 'pdf') {
      const { client, url } = getReportUrlAndClient(reportType, startDate, endDate);
      const response = await client.get(url, { responseType: 'blob' });
      triggerDownload(response, filename);
      toast.success('Relatório PDF gerado com sucesso!');
      return;
    }

    // CSV/Excel generation (kept client-side for simplicity, but optimized)
    if (reportType === 'sales') {
      const rows = buildSalesDetailedRows();
      const header = 'Fatura;Data;Operador;Produto;Quantidade;Preço Base (s/IVA);Subtotal (s/IVA);Taxa IVA (%);IVA;Total c/IVA;Pagamento';
      const csvRows = rows.map(row => [
        row.fatura,
        row.data,
        row.operador,
        row.produto,
        row.quantidade,
        row.precoBase.toFixed(2),
        row.subtotal.toFixed(2),
        row.taxaIvaPct.toFixed(2),
        row.iva.toFixed(2),
        row.totalLinha.toFixed(2),
        row.pagamento
      ].join(';'));
      const totSub = rows.reduce((a, r) => a + r.subtotal, 0);
      const totIva = rows.reduce((a, r) => a + r.iva, 0);
      const totGer = rows.reduce((a, r) => a + r.totalLinha, 0);
      const totalsRow = `;;;;;;${totSub.toFixed(2)};;${totIva.toFixed(2)};${totGer.toFixed(2)};`;
      triggerDownload(new Blob([[header, ...csvRows, totalsRow].join('\n')], { type: 'text/csv;charset=utf-8;' }), filename);
      toast.success('Relatório CSV descarregado com sucesso!');
    } else {
      const rows = buildInventoryDetailedRows();
      const header = 'Produto;Categoria;Stock Atual;Stock Mínimo;Estado';
      const csvRows = rows.map(row => [
        row.produto,
        row.categoria,
        row.stockAtual,
        row.stockMinimo,
        row.estado
      ].join(';'));
      triggerDownload(new Blob([[header, ...csvRows].join('\n')], { type: 'text/csv;charset=utf-8;' }), filename);
      toast.success('Relatório CSV descarregado com sucesso!');
    }
  } catch (error) {
    toast.error('Erro ao gerar o relatório.');
  }
};

const getReportUrlAndClient = (type, start, end) => {
  const client = isGlobalMode ? globalApiClient : localApiClient;
  const baseUrl = '/relatorios';

  if (type === 'sales') {
    return { client, url: `${baseUrl}/vendas/pdf?startDate=${start}&endDate=${end}` };
  } else {
    return { client, url: `${baseUrl}/inventario/pdf` };
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  return format(parseISO(dateString), 'dd/MM/yyyy', { locale: ptBR });
};

const formatDateTime = (date) => {
  return format(new Date(date), 'dd/MM/yyyy HH:mm', { locale: ptBR });
};

onMounted(async () => {
  await Promise.all([
    salesStore.fetchSales(),
    productsStore.fetchProducts()
  ]);
});

watch(
  () => shopStore.selectedShopId,
  async () => {
    await Promise.all([
      salesStore.fetchSales(),
      productsStore.fetchProducts()
    ]);
  }
);
</script>
