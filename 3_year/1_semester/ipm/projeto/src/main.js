// Main Vue app initialization
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Create and mount Vue application with router
createApp(App).use(router).mount('#app')