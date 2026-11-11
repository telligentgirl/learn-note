import { ref } from 'vue'
import { defineStore } from 'pinia'

// persist 插件将 Token 和用户资料保存到 localStorage，刷新后自动恢复；不保存密码。
export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref(null)

  function clear() {
    token.value = ''
    user.value = null
  }

  return { token, user, clear }
}, { persist: { key: 'notes-auth', pick: ['token', 'user'] } })
