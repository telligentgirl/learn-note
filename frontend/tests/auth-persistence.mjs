import assert from 'node:assert/strict'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import persistedstate from 'pinia-plugin-persistedstate'
import { useAuthStore } from '../src/stores/auth.js'
const memory = new Map()
globalThis.localStorage = { getItem: key => memory.get(key) ?? null, setItem: (key, value) => memory.set(key, value) }
globalThis.window = { localStorage: globalThis.localStorage }
function freshApp() {
  const pinia = createPinia().use(persistedstate)
  createApp({}).use(pinia)
  return useAuthStore(pinia)
}
const first = freshApp()
first.token = 'test-token'
first.user = { id: 1, username: 'test-user' }
first.$persist()
const restored = freshApp()
assert.equal(restored.token, 'test-token')
assert.equal(restored.user.username, 'test-user')
restored.clear()
restored.$persist()
assert.equal(freshApp().token, '')
console.log('PASS: 登录状态恢复、退出清理持久化状态')
