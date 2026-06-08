<template>
  <button
    :type="type"
    :class="buttonClasses"
    :disabled="disabled"
    @click="$emit('click', $event)"
  >
    <slot />
  </button>
</template>

<script setup>
import { computed } from 'vue';
import { cn } from '../../utils/cn';

const props = defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'outline', 'ghost', 'destructive'].includes(value)
  },
  type: {
    type: String,
    default: 'button'
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'sm', 'lg'].includes(value)
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

defineEmits(['click']);

const buttonClasses = computed(() => {
  const baseClasses = 'inline-flex items-center justify-center rounded-md font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none';
  
  const variantClasses = {
    default: 'bg-gray-900 text-white hover:bg-gray-800',
    outline: 'border border-gray-300 bg-white hover:bg-gray-50',
    ghost: 'hover:bg-gray-100',
    destructive: 'bg-red-600 text-white hover:bg-red-700'
  };
  
  const sizeClasses = {
    default: 'h-10 px-4 py-2',
    sm: 'h-8 px-3 text-sm',
    lg: 'h-12 px-6'
  };
  
  return cn(
    baseClasses,
    variantClasses[props.variant],
    sizeClasses[props.size],
    props.class
  );
});
</script>
