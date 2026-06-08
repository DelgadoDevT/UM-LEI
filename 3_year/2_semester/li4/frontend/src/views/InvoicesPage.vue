<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-7xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Gestão de Faturas</h1>
          <p class="text-sm text-gray-600 mt-1">Consulta e histórico de documentos fiscais</p>
        </div>
        <div class="flex gap-2">
            <Button @click="emitSaft" class="bg-purple-600 text-white border-purple-300">
                <span class="mr-2">📁</span> Emitir SAF-T (Mês Atual)
            </Button>
        </div>
      </div>

      <!-- Filtros Rápidos -->
      <div class="bg-white rounded-lg border border-gray-200 p-4 flex flex-wrap gap-4 items-end">
        <div class="flex-1 min-w-[200px]">
          <Label for="searchInvoice">Pesquisar</Label>
          <Input id="searchInvoice" v-model="searchTerm" placeholder="Nº Fatura, ID ou NIF..." class="mt-1" />
        </div>
        <div>
          <Label for="filterPayment">Método</Label>
          <Select id="filterPayment" v-model="filterPayment" class="mt-1 min-w-[150px]">
            <option value="all">Todos</option>
            <option value="Numerário">Numerário</option>
            <option value="Cartão">Cartão</option>
            <option value="MBWay">MBWay</option>
          </Select>
        </div>
        <div class="flex items-center gap-2">
            <div class="bg-gray-100 px-3 py-2 rounded-lg border border-gray-200">
                <p class="text-[10px] text-gray-500 uppercase font-bold">Total Listado</p>
                <p class="text-lg font-bold text-green-600">€{{ totalFiltered.toFixed(2) }}</p>
            </div>
        </div>
      </div>

      <!-- Lista de Faturas -->
      <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-200">
              <tr>
                <th class="text-left py-3 px-4 text-xs font-bold text-gray-500 uppercase">Nº Fatura</th>
                <th class="text-left py-3 px-4 text-xs font-bold text-gray-500 uppercase">Data</th>
                <th class="text-left py-3 px-4 text-xs font-bold text-gray-500 uppercase">Itens</th>
                <th class="text-left py-3 px-4 text-xs font-bold text-gray-500 uppercase">Total</th>
                <th class="text-left py-3 px-4 text-xs font-bold text-gray-500 uppercase">Método</th>
                <th class="text-right py-3 px-4 text-xs font-bold text-gray-500 uppercase">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-if="filteredInvoices.length === 0">
                <td colspan="6" class="py-12 text-center text-gray-400 italic">Nenhuma fatura encontrada com os filtros atuais.</td>
              </tr>
              <tr v-for="invoice in pagedInvoices" :key="invoice.id ?? invoice.numFatura" class="hover:bg-gray-50 transition-colors">
                <td class="py-3 px-4 font-mono text-sm font-bold text-gray-900">{{ invoice.numFatura }}</td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ formatDateTime(invoice.date) }}</td>
                <td class="py-3 px-4 text-sm text-gray-600">
                   <span class="bg-gray-100 px-2 py-0.5 rounded-full text-xs">{{ invoice.items?.length || 0 }} unid.</span>
                </td>
                <td class="py-3 px-4 text-sm font-bold text-gray-900">€{{ Number(invoice.total || 0).toFixed(2) }}</td>
                <td class="py-3 px-4 text-sm">
                   <span :class="getPaymentBadgeClass(invoice.paymentMethod)">
                     {{ invoice.paymentMethod }}
                   </span>
                </td>
                <td class="py-3 px-4 text-right">
                  <Button variant="outline" size="sm" @click="viewDetails(invoice)" class="text-blue-600 border-blue-200 hover:bg-blue-50">
                    Detalhes
                  </Button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <Pagination
          :total-items="filteredInvoices.length"
          :current-page="invoicesPage"
          :page-size="invoicesPageSize"
          @update:current-page="invoicesPage = $event"
          @update:page-size="invoicesPageSize = $event"
        />
      </div>
    </div>

    <!-- Modal de Detalhes -->
    <Dialog :open="!!selectedInvoice" @update:open="selectedInvoice = null">
      <div class="p-6 max-w-2xl">
        <DialogHeader>
          <DialogTitle class="flex items-center gap-2">
            <span>📄 Fatura</span>
            <span class="font-mono">{{ selectedInvoice?.numFatura }}</span>
          </DialogTitle>
          <DialogDescription>Emitida em {{ formatDateTime(selectedInvoice?.date) }}</DialogDescription>
        </DialogHeader>

        <div class="py-6 space-y-6">
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div class="bg-gray-50 p-3 rounded-lg border border-gray-100">
              <p class="text-gray-500 font-medium">Operador</p>
              <p class="text-gray-900 font-bold">{{ selectedInvoice?.cashier || 'N/A' }}</p>
            </div>
            <div class="bg-gray-50 p-3 rounded-lg border border-gray-100">
              <p class="text-gray-500 font-medium">Método de Pagamento</p>
              <p class="text-gray-900 font-bold">{{ selectedInvoice?.paymentMethod }}</p>
            </div>
          </div>

          <div class="space-y-3">
            <p class="text-sm font-bold text-gray-700 border-b pb-2">Linhas do Documento</p>
            <div class="max-h-[300px] overflow-y-auto space-y-2 pr-2">
              <div v-for="line in selectedInvoice?.items" :key="line.idLinhaVenda || line.id" class="flex justify-between items-center text-sm p-2 bg-white border border-gray-100 rounded-lg">
                <div class="flex-1">
                  <p class="font-medium text-gray-900">{{ line.produto?.nome || line.nome || 'Produto desconhecido' }}</p>
                  <p class="text-xs text-gray-500">{{ line.quantidade }} × €{{ getLinePriceWithVat(line).toFixed(2) }}</p>
                </div>
                <div class="text-right">
                  <p class="font-bold text-gray-900">€{{ getLineTotal(line).toFixed(2) }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-gray-50 p-4 rounded-xl border border-gray-100 space-y-2 text-sm">
            <div class="flex justify-between text-gray-700">
              <span>Subtotal (sem IVA)</span>
              <span class="font-medium">€{{ invoiceBreakdown.subtotal.toFixed(2) }}</span>
            </div>
            <div
              v-for="bucket in invoiceBreakdown.ivaBuckets"
              :key="bucket.taxa"
              class="flex justify-between text-gray-700"
            >
              <span>IVA {{ bucket.taxa }}%</span>
              <span class="font-medium">€{{ bucket.valor.toFixed(2) }}</span>
            </div>
            <div v-if="invoiceBreakdown.ivaBuckets.length === 0" class="flex justify-between text-gray-700">
              <span>IVA</span>
              <span class="font-medium">€{{ invoiceBreakdown.totalIva.toFixed(2) }}</span>
            </div>
          </div>

          <div class="bg-green-50 p-4 rounded-xl border border-green-100 flex justify-between items-center">
             <span class="text-green-800 font-bold text-lg">Total Final</span>
             <span class="text-green-700 font-black text-2xl">€{{ Number(selectedInvoice?.total || 0).toFixed(2) }}</span>
          </div>
        </div>

        <DialogFooter class="flex gap-2">
          <Button variant="outline" @click="selectedInvoice = null" class="flex-1">Fechar</Button>
          <Button class="bg-blue-600 text-white flex-1" @click="printInvoice">
            <span class="mr-2">🖨️</span> Imprimir / PDF
          </Button>
        </DialogFooter>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useSalesStore } from '../stores/sales';
import { useShopStore } from '../stores/shop';
import { toast } from 'vue-sonner';
import { format, isValid } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { localApiClient, globalApiClient } from '../services/apiClients';

const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
const client = isGlobal ? globalApiClient : localApiClient;

import Button from '../components/ui/Button.vue';
import Input from '../components/ui/Input.vue';
import Select from '../components/ui/Select.vue';
import Label from '../components/ui/Label.vue';
import Dialog from '../components/ui/Dialog.vue';
import DialogHeader from '../components/ui/DialogHeader.vue';
import DialogTitle from '../components/ui/DialogTitle.vue';
import DialogDescription from '../components/ui/DialogDescription.vue';
import DialogFooter from '../components/ui/DialogFooter.vue';
import Pagination from '../components/ui/Pagination.vue';

const salesStore = useSalesStore();
const shopStore = useShopStore();
const searchTerm = ref('');
const filterPayment = ref('all');
const selectedInvoice = ref(null);
const invoicesPage = ref(1);
const invoicesPageSize = ref(20);
const normalizeSearchValue = (value) => String(value ?? '').toLowerCase();

const filteredInvoices = computed(() => {
    const term = searchTerm.value.trim().toLowerCase();
    return salesStore.sales.filter((inv) => {
        const numFatura = normalizeSearchValue(inv.numFatura);
        const id = normalizeSearchValue(inv.id);
        const matchesSearch = !term || numFatura.includes(term) || id.includes(term);
        
        const matchesPayment = filterPayment.value === 'all' || inv.paymentMethod === filterPayment.value;
        
        return matchesSearch && matchesPayment;
    });
});

const totalFiltered = computed(() => {
    return filteredInvoices.value.reduce((sum, inv) => sum + Number(inv.total || 0), 0);
});

watch([searchTerm, filterPayment], () => {
    invoicesPage.value = 1;
});

const pagedInvoices = computed(() => {
    const start = (invoicesPage.value - 1) * invoicesPageSize.value;
    return filteredInvoices.value.slice(start, start + invoicesPageSize.value);
});

const parseTaxaIva = (raw) => {
    if (raw === null || raw === undefined) return null;
    const s = String(raw);
    const m = s.match(/(\d+(?:[.,]\d+)?)/);
    if (!m) return null;
    let n = Number(m[1].replace(',', '.'));
    if (!Number.isFinite(n)) return null;
    if (n < 1) n = n * 100;
    return Math.round(n * 100) / 100;
};

const invoiceBreakdown = computed(() => {
    const inv = selectedInvoice.value;
    const total = Number(inv?.total || 0);
    if (!inv || !Array.isArray(inv.items) || inv.items.length === 0) {
        return { subtotal: 0, totalIva: 0, ivaBuckets: [] };
    }
    let subtotal = 0;
    let totalIva = 0;
    const byTax = {};
    for (const line of inv.items) {
        const lineTotal = getLineTotal(line);
        const taxa = parseTaxaIva(line.produto?.taxaIva ?? line.taxaIva);
        if (taxa !== null && taxa > 0) {
            const lineSub = lineTotal / (1 + taxa / 100);
            const lineIva = lineTotal - lineSub;
            subtotal += lineSub;
            totalIva += lineIva;
            const k = taxa.toFixed(2).replace(/\.00$/, '');
            byTax[k] = (byTax[k] || 0) + lineIva;
        } else {
            const lineSub = Number(line.subtotal || 0);
            const lineIva = Number(line.totalImposto || 0);
            if (lineSub > 0 || lineIva > 0) {
                subtotal += lineSub;
                totalIva += lineIva;
            } else {
                subtotal += lineTotal;
            }
        }
    }
    if (subtotal === 0 && totalIva === 0 && total > 0) {
        const fallbackIva = Number(inv.imposto || 0);
        const fallbackSub = Number(inv.subtotal || 0) || (total - fallbackIva);
        return { subtotal: fallbackSub, totalIva: fallbackIva, ivaBuckets: [] };
    }
    const ivaBuckets = Object.entries(byTax)
        .map(([taxa, valor]) => ({ taxa, valor }))
        .sort((a, b) => Number(a.taxa) - Number(b.taxa));
    return { subtotal, totalIva, ivaBuckets };
});

const viewDetails = (inv) => {
    selectedInvoice.value = inv;
};

const getLineTotal = (line) => {
    const total = Number(line.totalFinal ?? 0);
    if (total > 0) return total;
    const qty = Number(line.quantidade ?? 0);
    const subtotal = Number(line.subtotal ?? 0);
    if (subtotal > 0) return subtotal;
    return qty * Number(line.preco ?? 0);
};

const getLinePriceWithVat = (line) => {
    const qty = Number(line.quantidade ?? 0);
    if (qty <= 0) return 0;
    return getLineTotal(line) / qty;
};

const printInvoice = async () => {
    if (!selectedInvoice.value) return;
    try {
        const numeroFatura = encodeURIComponent(selectedInvoice.value.numFatura);
        const response = await client.get(`/faturas/${numeroFatura}/pdf`, { responseType: 'blob' });
        const url = window.URL.createObjectURL(response);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `Fatura_${selectedInvoice.value.numFatura}.pdf`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        toast.success('PDF gerado com sucesso.');
    } catch (error) {
        toast.error('Erro ao gerar PDF da fatura.');
    }
};

const emitSaft = async () => {
    try {
        const now = new Date();
        const ano = now.getFullYear();
        const mes = now.getMonth() + 1;
        const response = await client.get(`/faturas/saft?ano=${ano}&mes=${mes}`, { responseType: 'blob' });
        
        const url = window.URL.createObjectURL(response);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `SAFT_${ano}_${mes}.xml`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        toast.success('SAF-T emitido com sucesso.', { description: `Período: ${mes}/${ano}` });
    } catch (error) {
        toast.error('Erro ao emitir SAF-T.');
    }
};

const getPaymentBadgeClass = (method) => {
    const base = "inline-flex items-center px-2 py-1 rounded-full text-xs font-bold ";
    if (method === 'Numerário') return base + "bg-orange-100 text-orange-700 border border-orange-200";
    if (method === 'MBWay') return base + "bg-pink-100 text-pink-700 border border-pink-200";
    return base + "bg-blue-100 text-blue-700 border border-blue-200";
};

const formatDateTime = (date) => {
    if (!date) return 'N/A';
    const parsedDate = new Date(date);
    if (!isValid(parsedDate)) return 'N/A';
    return format(parsedDate, 'dd/MM/yyyy HH:mm', { locale: ptBR });
};

onMounted(() => {
    salesStore.fetchSales();
});

watch(
  () => shopStore.selectedShopId,
  () => {
    salesStore.fetchSales();
  }
);
</script>
