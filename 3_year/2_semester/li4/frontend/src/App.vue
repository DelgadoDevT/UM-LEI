<template>
  <div id="app">
    <Toaster position="top-right" :duration="3000" />
    <RouterView />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import { Toaster } from 'vue-sonner';
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
