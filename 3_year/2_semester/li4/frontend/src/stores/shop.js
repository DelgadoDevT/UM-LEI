import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useShopStore = defineStore('shop', () => {
  const selectedShopId = ref('all');
  let fetchVersion = 0;

  const setShop = async (id) => {
    selectedShopId.value = id;
    const version = ++fetchVersion;

    const [
      { useProductsStore },
      { useSalesStore },
      { useStatisticsStore },
      { useOrdersStore },
      { useEmployeesStore },
      { usePromotionsStore },
      { useSuppliersStore },
    ] = await Promise.all([
      import('./products'),
      import('./sales'),
      import('./statistics'),
      import('./orders'),
      import('./employees'),
      import('./promotions'),
      import('./suppliers'),
    ]);

    if (version !== fetchVersion) return;

    // Reset all stores immediately so stale data is never shown
    useProductsStore().$reset();
    useSalesStore().$reset();
    useStatisticsStore().$reset();
    try { useOrdersStore().$reset(); } catch {}
    try { useEmployeesStore().$reset(); } catch {}
    try { usePromotionsStore().$reset(); } catch {}
    try { useSuppliersStore().$reset(); } catch {}

    if (version !== fetchVersion) return;

    // Fire all fetches without awaiting — each store updates the UI as soon as its data arrives
    useProductsStore().fetchProducts().catch(() => {});
    useSalesStore().fetchSales().catch(() => {});
    useStatisticsStore().fetchAll().catch(() => {});
    useOrdersStore().fetchOrders?.().catch(() => {});
    useEmployeesStore().fetchEmployees?.().catch(() => {});
    usePromotionsStore().fetchPromotions?.().catch(() => {});
    useSuppliersStore().fetchSuppliers?.().catch(() => {});
  };

  const isAllShops = () => selectedShopId.value === 'all';

  return { selectedShopId, setShop, isAllShops };
});
