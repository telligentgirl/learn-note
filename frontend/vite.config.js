import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  // 开发时：浏览器访问 /api/user/login，由 Vite 转发到 Java 的 /user/login。
  // 浏览器只请求前端自己的地址，因此不需要在 Java 中临时放开跨域。
  server: {
    proxy: {
      '/ai': { target: 'http://127.0.0.1:8000', changeOrigin: true },
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
