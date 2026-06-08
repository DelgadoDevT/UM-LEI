<template>
  <div class="min-h-screen bg-green-100 flex items-center justify-center p-4">
    <div class="max-w-md w-full">
      <div class="bg-white rounded-xl shadow-lg border border-gray-200 p-8">

        <div class="text-center mb-8">
          <img
              src="../assets/taki_logo.png"
              alt="Taki Logo"
              class="h-24 mx-auto mb-4"
          />
          <h1 class="text-3xl font-bold text-gray-900">Taki</h1>
          <p class="text-gray-600 mt-2">Sistema de Gestão - Ponto de Venda</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-6">
          <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg text-sm">
            {{ error }}
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700">Utilizador</label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </span>
              <input
                  type="text"
                  v-model="username"
                  placeholder="Introduza o nome de utilizador"
                  class="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm pl-10 focus:outline-none focus:ring-2 focus:ring-green-500"
                  required
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700">Palavra-passe</label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              </span>
              <input
                  type="password"
                  v-model="password"
                  placeholder="Introduza a palavra-passe"
                  class="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm pl-10 focus:outline-none focus:ring-2 focus:ring-green-500"
                  required
              />
            </div>
          </div>

          <button
              type="submit"
              class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-lg transition-colors disabled:opacity-50"
              :disabled="loading"
          >
            {{ loading ? 'A entrar...' : 'Iniciar Sessão' }}
          </button>
        </form>
      </div>
      <p class="text-center text-xs text-gray-400 mt-8">
        © 2026 Taki - Sistema de Gestão Interno. Todos os direitos reservados.
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();

const username = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);

const handleLogin = async () => {
  error.value = '';
  loading.value = true;

  try {
    const success = await authStore.login(username.value.trim(), password.value);

    if (success) {
      window.location.replace(authStore.defaultLandingPath);
    } else {
      error.value = 'Utilizador ou palavra-passe incorretos.';
    }
  } catch (err) {
    console.error('Erro de Login:', err);
    error.value = err.body?.message || err.message || 'Utilizador ou palavra-passe incorretos.';
  } finally {
    loading.value = false;
  }
};
</script>