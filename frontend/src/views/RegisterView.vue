<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { javaRequest } from '../api/request'
const router = useRouter()
const busy = ref(false), error = ref(''), success = ref(null)
async function register(event) {
  if (busy.value) return
  const form = new FormData(event.currentTarget)
  const username = String(form.get('username')).trim()
  const password = String(form.get('password'))
  error.value = ''
  if (![username, password].every(value => /^\S{5,16}$/.test(value))) {
    error.value = '用户名和密码须为 5～16 位，且不能包含空白。'
    return
  }
  if (password !== form.get('confirm')) { error.value = '两次输入的密码不一致。'; return }
  busy.value = true
  try {
    await javaRequest('/user/register', { method: 'POST', body: new URLSearchParams({ username, password }) })
    success.value.showModal()
  } catch (e) { error.value = e.message }
  finally { busy.value = false }
}
</script>
<template>
  <main class="card">
    <RouterLink to="/login">← 返回登录</RouterLink>
    <h1>创建笔记账号</h1>
    <form @submit.prevent="register">
      <fieldset :disabled="busy">
        <label>用户名<input name="username" autocomplete="username" required /></label>
        <label>密码<input name="password" type="password" autocomplete="new-password" required /></label>
        <label>确认密码<input name="confirm" type="password" autocomplete="new-password" required /></label>
        <p v-if="error" role="alert">{{ error }}</p>
        <button>{{ busy ? '正在注册…' : '注册' }}</button>
      </fieldset>
    </form>
    <dialog ref="success" @cancel.prevent><h2>注册成功</h2><button @click="router.replace('/login')">去登录</button></dialog>
  </main>
</template>
<style scoped>
.card { max-width: 440px; margin: 8vh auto; padding: 32px; background: white; border: 1px solid #dce3ed; border-radius: 16px; font-family: system-ui, sans-serif; color: #243047; }
fieldset { border: 0; padding: 0; margin: 0; }
label { display: block; margin: 16px 0; }
input { display: block; width: 100%; margin-top: 8px; padding: 12px; border: 1px solid #8896ac; border-radius: 6px; }
button { background: #315bc7; color: white; border: 0; border-radius: 6px; padding: 10px 20px; cursor: pointer; }
dialog { border: 0; border-radius: 12px; padding: 24px; } dialog::backdrop { background: #0006; }
[role='alert'] { color: #b42318; }
</style>
