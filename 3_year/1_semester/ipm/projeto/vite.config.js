// Vite configuration file
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Export Vite configuration
export default defineConfig({
  plugins: [vue()],        // Enable Vue plugin
  server: {
    port: 3000            // Development server port
  }
})