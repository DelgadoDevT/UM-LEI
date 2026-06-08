<template>
  <label :for="htmlFor" class="text-sm font-medium text-gray-700">
    <slot />
  </label>
</template>

<script setup>
defineProps({
  htmlFor: {
    type: String,
    default: undefined
  }
});
</script>
