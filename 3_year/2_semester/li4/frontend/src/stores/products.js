import { defineStore } from 'pinia';
import { ref } from 'vue';
import { localApiClient, globalApiClient } from '../services/apiClients';

export const useProductsStore = defineStore('products', () => {
  const isGlobal = ['global', 'central'].includes(import.meta.env.APP_MODE);
  const client = isGlobal ? globalApiClient : localApiClient;

  const products = ref([]);

  const fetchProducts = async () => {
    try {
      const shopStore = (await import('./shop')).useShopStore();
      const shopId = shopStore.selectedShopId === 'all' || shopStore.selectedShopId == null
        ? 0
        : Number(shopStore.selectedShopId);

      const url = isGlobal ? `/produtos?idLoja=${shopId}` : `/produtos?idLoja=${shopId}`;
      const promotionsStore = (await import('./promotions')).usePromotionsStore();
      const [data] = await Promise.all([
        client.get(url),
        promotionsStore.fetchPromotions()
      ]);

      const getVatMultiplier = (taxaIva) => {
        if (typeof taxaIva !== 'string') return 1.23; // Default to 23%
        if (taxaIva.includes('23')) return 1.23;
        if (taxaIva.includes('13')) return 1.13;
        if (taxaIva.includes('6')) return 1.06;
        return 1.23; // Default fallback
      };

      const activePromotions = promotionsStore.getActivePromotions || [];
      const bestDiscountFor = (productId, categoryName) => {
        let best = 0;
        for (const promo of activePromotions) {
          const aplicaProduto = Array.isArray(promo.produtos) && promo.produtos.includes(productId);
          const aplicaCategoria = Array.isArray(promo.categorias) && categoryName
            && promo.categorias.some(c => String(c).toLowerCase() === String(categoryName).toLowerCase());
          if (aplicaProduto || aplicaCategoria) {
            const d = Number(promo.desconto ?? promo.discount ?? 0);
            if (d > best) best = d;
          }
        }
        return best;
      };

      products.value = data.map(p => {
        const vatMultiplier = getVatMultiplier(p.taxaIva);
        const precoBase = Number(p.precoVenda ?? p.preco ?? 0);
        const price = precoBase * vatMultiplier;
        const id = p.idProduto ?? p.id;
        const category = p.categoriaDesignacao || p.categoria?.nome || 'Sem categoria';
        const discountPercentage = bestDiscountFor(id, category);
        const hasPromotion = discountPercentage > 0;
        return {
          id,
          name: p.nome,
          barcode: p.codigoBarras,
          category,
          price, // Price with VAT, sem desconto
          basePrice: precoBase,
          taxaIva: p.taxaIva,
          stock: Number(p.stockAtual ?? 0),
          minStock: Number(p.stockMinimo ?? 0),
          lowStockLojas: p.lowStockLojas ?? [],
          supplier: p.supplier || p.fornecedorNome || 'Sem fornecedor',
          supplierId: p.supplierId || p.idFornecedor || null,
          status: p.estado,
          hasPromotion,
          discountPercentage,
          discountedPrice: hasPromotion ? price * (1 - discountPercentage / 100) : price
        };
      });
    } catch (error) {
      console.error('Error fetching products:', error);
      throw error;
    }
  };

  const addProduct = async (product) => {
    try {
      await client.post('/produtos', product);
      await fetchProducts();
    } catch (error) {
      console.error('Error adding product:', error);
      throw error;
    }
  };

  const updateProduct = async (id, updatedProduct) => {
    try {
      const result = await client.patch(`/produtos/${id}`, updatedProduct);
      const index = products.value.findIndex(p => p.id === id);
      if (index !== -1) {
        products.value[index] = {
          ...products.value[index],
          name: result.nome ?? products.value[index].name,
          barcode: result.codigoBarras ?? products.value[index].barcode,
          price: result.precoVenda ?? products.value[index].price,
          status: result.estado ?? products.value[index].status,
          stock: result.stockAtual ?? products.value[index].stock,
          minStock: result.stockMinimo ?? products.value[index].minStock
        };
      }
    } catch (error) {
      console.error('Error updating product:', error);
      throw error;
    }
  };

  const deleteProduct = async (id) => {
    try {
      await client.patch(`/produtos/${id}/inativar`);
      products.value = products.value.filter(p => p.id !== id);
    } catch (error) {
      console.error('Error deleting product:', error);
      throw error;
    }
  };

  const getProductByBarcode = (barcode) => {
    return products.value.find(p => p.barcode === barcode);
  };

  const getLowStockProducts = () => {
    return products.value.filter(p => {
      const stock = Number(p.stock ?? 0);
      const min = Number(p.minStock ?? 0);
      return min > 0 && stock <= min;
    });
  };

  const registerMovement = async (payload) => {
    try {
      await client.post('/inventario/movimentos', payload);
      
      // Encontrar o produto correspondente ao idInventario ou idProduto
      // Aqui assumimos que o payload tem idProduto para facilitar a atualização local
      const product = products.value.find(p => p.id === payload.idProduto);
      if (product) {
        const qty = Number(payload.quantidade);
        if (payload.tipo === 'ENTRADA') product.stock += qty;
        else if (payload.tipo === 'SAIDA' || payload.tipo === 'QUEBRA') product.stock -= qty;
      }
    } catch (error) {
      console.error('Error registering movement:', error);
      throw error;
    }
  };

  const $reset = () => {
    products.value = [];
  };

  return {
    products,
    $reset,
    fetchProducts,
    addProduct,
    updateProduct,
    deleteProduct,
    registerMovement,
    getProductByBarcode,
    getLowStockProducts
  };
});
