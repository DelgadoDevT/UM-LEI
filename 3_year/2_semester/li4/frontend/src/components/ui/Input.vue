<template>
  <input
      v-bind="$attrs"
      :type="type"
      :disabled="disabled"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      :class="inputClasses"
  />
</template>

<script setup>
import { computed } from 'vue';
import { cn } from '../../utils/cn';

// Unificamos todos os props num único bloco
const props = defineProps({
  modelValue: [String, Number],
  type: {
    type: String,
    default: 'text'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  class: {
    type: String,
    default: ''
  }
});

defineEmits(['update:modelValue']);

const inputClasses = computed(() => {
  return cn(
      'flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
      props.class
  );
});
</script>