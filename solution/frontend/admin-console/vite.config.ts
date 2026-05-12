import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const themeDir = new URL('../theme/src', import.meta.url).pathname

export default defineConfig({
  plugins: [react()],
  base: '/admin/',
  resolve: {
    alias: {
      '@workspace/theme': themeDir,
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5175,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
