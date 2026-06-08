<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Overlay -->
        <div 
          class="fixed inset-0 bg-black/50 backdrop-blur-sm"
          @click="handleClose"
        ></div>
        
        <!-- Dialog Content -->
        <div 
          class="relative bg-white rounded-lg shadow-lg max-w-lg w-full mx-4 max-h-[90vh] overflow-y-auto z-50"
          @click.stop
        >
          <slot />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:open']);

const handleClose = () => {
  emit('update:open', false);
};
</script>

<style scoped>
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}
</style>
