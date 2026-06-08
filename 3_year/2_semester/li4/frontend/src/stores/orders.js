import { defineStore } from 'pinia';
import { ref } from 'vue';
import { localApiClient, globalApiClient } from '../services/apiClients';
import { useProductsStore } from './products';

export const useOrdersStore = defineStore('orders', () => {
  const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
  const client = isGlobal ? globalApiClient : localApiClient;

  const productsStore = useProductsStore();
  const orders = ref([]);

  const fetchOrders = async () => {
    try {
      const shopStore = (await import('./shop')).useShopStore();
      const shopId = shopStore.selectedShopId === 'all' || shopStore.selectedShopId == null
        ? 0
        : Number(shopStore.selectedShopId);

      const url = isGlobal ? `/encomendas?idLoja=${shopId}` : `/encomendas?idLoja=${shopId}`;
      const data = await client.get(url);
      orders.value = data.map(o => {
        const lines = o.linhas || [];
        
        let productName = 'Vazia';
        if (lines.length > 0) {
            const productNames = lines.slice(0, 2).map(line => {
                const product = productsStore.products.find(p => p.id === line.idProduto);
                return product ? product.name : line.idProduto;
            });
            productName = productNames.join(', ');
            if (lines.length > 2) {
                productName += `, + ${lines.length - 2} mais`;
            }
        }
        
        return {
          id: o.idEncomenda,
          supplier: o.nomeFornecedor || o.idFornecedor,
          supplierId: o.idFornecedor,
          date: o.dataCriacao || o.dataPedido,
          status: o.estado || o.status,
          total: o.total,
          lines: lines,
          productName: productName,
          quantity: lines.reduce((acc, line) => acc + line.quantidade, 0)
        };
      });
    } catch (error) {
      console.error('Error fetching orders:', error);
    }
  };

  const addOrder = async (productId, quantity) => {
    try {
      const product = productsStore.products.find(p => p.id === productId);
      if (!product) return;

      let supplierId = product.supplierId;
      if (!supplierId) {
        const fornecedores = await client.get('/fornecedores');
        const supplier = fornecedores.find(f => f.nome === product.supplier) || fornecedores[0];
        supplierId = supplier?.idFornecedor;
      }
      if (!supplierId) throw new Error('Fornecedor nao encontrado para o produto selecionado.');

      await client.post('/encomendas', {
        idEncomenda: crypto.randomUUID(),
        idFornecedor: supplierId,
        idLoja: String(product.shopId || 1),
        linhas: [{
          idProduto: productId,
          quantidade: Number(quantity),
          precoCusto: Number(product.price || 0.01)
        }]
      });

      await fetchOrders();
    } catch (error) {
      console.error('Error adding order:', error);
      throw error;
    }
  };

  const markAsDelivered = async (id) => {
    try {
      // The API call advances the order state on the backend.
      // Backend expects a body on PATCH, so we send an empty one.
      const updatedOrder = await client.patch(`/encomendas/${id}/estado`, {});

      const index = orders.value.findIndex(o => o.id === id);
      if (index !== -1) {
        // This is more robust than guessing the next state
        orders.value[index].status = updatedOrder.estado;
      }
    } catch (error) {
      console.error('Error advancing order state:', error);
      throw error;
    }
  };

  const $reset = () => {
    orders.value = [];
  };

  return {
    orders,
    $reset,
    fetchOrders,
    addOrder,
    markAsDelivered
  };
});
