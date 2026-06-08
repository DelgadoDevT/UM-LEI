<template>
  <div v-if="totalItems > 0" class="flex flex-wrap items-center justify-between gap-3 py-3 px-4 bg-gray-50 border-t border-gray-100">
    <div class="flex items-center gap-2 text-sm text-gray-600">
      <span>Mostrar</span>
      <select
        :value="pageSize"
        @change="onPageSizeChange($event.target.value)"
        class="border border-gray-300 rounded-md px-2 py-1 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-green-500"
      >
        <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
      </select>
      <span>por página · {{ rangeLabel }} de {{ totalItems }}</span>
    </div>
    <div class="flex items-center gap-1">
      <button
        type="button"
        :disabled="currentPage === 1"
        @click="goTo(1)"
        class="px-2 py-1 text-sm rounded-md border border-gray-300 bg-white hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
      >«</button>
      <button
        type="button"
        :disabled="currentPage === 1"
        @click="goTo(currentPage - 1)"
        class="px-2 py-1 text-sm rounded-md border border-gray-300 bg-white hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
      >‹</button>
      <button
        v-for="p in visiblePages"
        :key="p"
        type="button"
        :class="[
          'px-3 py-1 text-sm rounded-md border',
          p === currentPage
            ? 'bg-green-600 border-green-600 text-white font-semibold'
            : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-100'
        ]"
        @click="goTo(p)"
      >{{ p }}</button>
      <button
        type="button"
        :disabled="currentPage === totalPages"
        @click="goTo(currentPage + 1)"
        class="px-2 py-1 text-sm rounded-md border border-gray-300 bg-white hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
      >›</button>
      <button
        type="button"
        :disabled="currentPage === totalPages"
        @click="goTo(totalPages)"
        class="px-2 py-1 text-sm rounded-md border border-gray-300 bg-white hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
      >»</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  totalItems: { type: Number, required: true },
  currentPage: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  pageSizeOptions: { type: Array, default: () => [10, 20, 50, 100] }
});

const emit = defineEmits(['update:currentPage', 'update:pageSize']);

const totalPages = computed(() => Math.max(1, Math.ceil(props.totalItems / props.pageSize)));

const rangeLabel = computed(() => {
  if (props.totalItems === 0) return '0';
  const start = (props.currentPage - 1) * props.pageSize + 1;
  const end = Math.min(props.currentPage * props.pageSize, props.totalItems);
  return `${start}–${end}`;
});

const visiblePages = computed(() => {
  const total = totalPages.value;
  const cur = props.currentPage;
  const window = 5;
  if (total <= window) return Array.from({ length: total }, (_, i) => i + 1);
  let start = Math.max(1, cur - 2);
  let end = Math.min(total, start + window - 1);
  start = Math.max(1, end - window + 1);
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

const goTo = (p) => {
  if (p < 1 || p > totalPages.value || p === props.currentPage) return;
  emit('update:currentPage', p);
};

const onPageSizeChange = (val) => {
  const n = Number(val);
  emit('update:pageSize', n);
  emit('update:currentPage', 1);
};
</script>
