<template>
  <select
      v-bind="$attrs"
      :value="modelValue"
      @change="$emit('update:modelValue', $event.target.value)"
      :class="selectClasses"
  >
    <slot />
  </select>
</template>

<script setup>
import { computed } from 'vue';
import { cn } from '../../utils/cn';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  class: {
    type: String,
    default: ''
  }
});

defineEmits(['update:modelValue']);

const selectClasses = computed(() => {
  return cn(
    'flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 ring-offset-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
    props.class
  );
});
</script>
