import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const themeDir = new URL('../theme/src', import.meta.url).pathname

export default defineConfig({
  plugins: [react()],
  base: '/vendor/',
  resolve: {
    alias: {
      '@workspace/theme': themeDir,
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5174,
    watch: { usePolling: true },
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET ?? 'http://backend:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: process.env.VITE_API_TARGET ?? 'http://backend:8080',
        changeOrigin: true,
      },
    },
  },
})
