import { defineConfig, loadEnv } from 'vite'
import tailwindcss from '@tailwindcss/vite'
import vue from '@vitejs/plugin-vue'
import basicSsl from '@vitejs/plugin-basic-ssl'
import { fileURLToPath, URL } from 'node:url'

const repoRoot = fileURLToPath(new URL('../', import.meta.url))

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, repoRoot, '')

  const localApiPort = env.TAKI_LOCAL_API_PORT || '8080'
  const globalApiPort = env.TAKI_GLOBAL_API_PORT || '8081'
  const localApiTarget = env.VITE_LOCAL_API_TARGET || `http://localhost:${localApiPort}`
  const globalApiTarget = env.VITE_GLOBAL_API_TARGET || `http://localhost:${globalApiPort}`

  return {
    envDir: repoRoot,
    plugins: [
      vue(),
      tailwindcss(),
      basicSsl(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      host: true,
      proxy: {
        '/api/local': {
          target: localApiTarget,
          changeOrigin: true,
          secure: false,
          withCredentials: true,
        },
        '/api/global': {
          target: globalApiTarget,
          changeOrigin: true,
          secure: false,
          withCredentials: true,
        },
      },
    },
    assetsInclude: ['**/*.svg', '**/*.csv'],
    define: {
      'import.meta.env.APP_MODE': JSON.stringify((env.APP_MODE || 'local').toLowerCase())
    }
  }
})
