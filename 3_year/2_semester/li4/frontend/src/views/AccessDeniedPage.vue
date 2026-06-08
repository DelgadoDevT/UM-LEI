<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center p-6">
    <div class="max-w-lg w-full bg-white rounded-xl border border-red-200 shadow-sm p-8 text-center space-y-5">
      <div class="mx-auto size-14 rounded-full bg-red-100 flex items-center justify-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="size-7 text-red-600" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2M13 17H11V15H13V17M13 13H11V7H13V13Z"/>
        </svg>
      </div>
      <div class="space-y-2">
        <h1 class="text-2xl font-bold text-gray-900">Acesso negado</h1>
        <p class="text-sm text-gray-600">
          O seu perfil não tem permissão para abrir esta funcionalidade. Esta tentativa foi registada para auditoria.
        </p>
      </div>
      <div class="flex justify-center gap-3">
        <button
          class="px-4 py-2 rounded-md border border-gray-300 text-gray-700 hover:bg-gray-100 text-sm font-medium"
          @click="goBack"
        >
          Voltar
        </button>
        <button
          class="px-4 py-2 rounded-md bg-green-600 hover:bg-green-700 text-white text-sm font-medium"
          @click="goDashboard"
        >
          Ir para Dashboard
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goBack = () => router.back();
const goDashboard = () => router.push('/app/dashboard');
</script>
