<script setup>
// script 是页面的交互逻辑；setup 中的变量可以直接用于下面的 template。
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { javaRequest } from '../api/request'
import { RouterLink, useRouter } from 'vue-router'
const router = useRouter()

const auth = useAuthStore()
// ref 创建响应式变量：变量变化时，Vue 会更新页面。
// 在 JavaScript 中用 .value，在模板中 Vue 会自动解包。
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function login(event) {
  if (loading.value) return
  error.value = ''
  // 提交时读取实际表单值：某些自动填充不会及时触发 v-model 的 input 事件。
  // 只清理账号首尾空白，不修改密码，不记录密码到日志。
  const form = new FormData(event.currentTarget)
  username.value = String(form.get('username') ?? '').trim()
  password.value = String(form.get('password') ?? '')
  for (const [label, value] of [['用户名', username.value], ['密码', password.value]]) {
    if (!value) { error.value = `请填写${label}。`; return }
    if (/\s/.test(value)) { error.value = `${label}含有空白字符，请检查输入。`; return }
    if (value.length < 5 || value.length > 16) {
      error.value = `${label}长度须为 5～16 位，当前为 ${value.length} 位。`
      return
    }
  }
  loading.value = true
  auth.clear()
  try {
    // Java 接收普通表单参数，不是 @RequestBody JSON，所以使用 URLSearchParams。
    const token = await javaRequest('/user/login', {
      method: 'POST',
      body: new URLSearchParams({ username: username.value, password: password.value }),
    })
    if (typeof token !== 'string' || !token) throw new Error('登录接口没有返回有效 Token。')

    // 再调用受保护接口，验证 Token 确实能用。现有 Java 要求原始 Token，不加 Bearer。
    auth.token = token
    const user = await javaRequest('/user/userInfo')
    if (!user?.username) throw new Error('用户信息不完整。')
    auth.token = token
    auth.user = user
    await router.replace('/workspace/notes')
    password.value = '' // 不保留已经提交的密码，也不在页面打印 Token。
  } catch (cause) {
    auth.clear()
    error.value = cause instanceof Error ? cause.message : '登录失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <!-- template 描述页面结构。v-if / v-else 根据登录状态切换内容。 -->
  <main class="card">
    <p class="eyebrow">学习笔记助手</p>
    <p v-if="$route.query.passwordChanged === '1'" role="status">密码已修改，请使用新密码登录。</p>
    <section v-if="auth.user" aria-live="polite">
      <h1>登录成功</h1>
      <!-- 双花括号把变量显示成文字，不会把用户内容当作 HTML 执行。 -->
      <p>欢迎，{{ auth.user.nickname || auth.user.username }}！</p>
      <p>已通过 Java 接口获取你的用户信息。</p>
      <button type="button" @click="auth.clear()">返回登录</button>
    </section>
    <!-- .prevent 阻止表单默认刷新页面，改由 login 函数发送请求。 -->
    <form v-else @submit.prevent="login">
      <h1>登录你的笔记空间</h1>
      <p>使用已有的 Java 后端账号登录。</p>
      <label for="username">用户名</label>
      <!-- v-model 让输入框和变量同步；冒号表示绑定动态属性。 -->
      <input id="username" name="username" v-model="username" autocomplete="username" required :disabled="loading" />
      <label for="password">密码</label>
      <input id="password" name="password" v-model="password" type="password" autocomplete="current-password" required :disabled="loading" />
      <p v-if="error" class="error" role="alert">{{ error }}</p>
      <button type="submit" :disabled="loading">{{ loading ? '正在登录…' : '登录' }}</button>
    </form>
    <p>还没有账号？<RouterLink to="/register">去注册</RouterLink></p>
  </main>
</template>

<style scoped>
/* style 控制外观；scoped 表示这些样式仅作用于当前组件。 */
.card { max-width: 400px; margin: 10vh auto; padding: 32px; border: 1px solid #dce3ed; border-radius: 16px; font-family: system-ui, sans-serif; color: #243047; }
.eyebrow { color: #315bc7; font-weight: 600; }
h1 { font-size: 24px; }
label { display: block; margin-top: 20px; margin-bottom: 8px; }
input { box-sizing: border-box; width: 100%; padding: 12px; border: 1px solid #8896ac; border-radius: 6px; font: inherit; }
button { margin-top: 24px; padding: 12px 24px; border: none; border-radius: 6px; background: #315bc7; color: white; font: inherit; cursor: pointer; }
button:disabled { opacity: 0.6; cursor: wait; }
.error { color: #b42318; }
</style>
