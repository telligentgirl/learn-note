<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { javaRequest } from '../api/request'
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
const router = useRouter()
const passwordError = ref(''), changingPassword = ref(false)
async function changePassword(event) {
  if (changingPassword.value || busy.value) return
  const form = new FormData(event.currentTarget)
  const old_pwd = form.get('old_pwd'), new_pwd = form.get('new_pwd'), re_pwd = form.get('re_pwd')
  passwordError.value = ''
  if (!/^\S{5,16}$/.test(new_pwd)) {
    passwordError.value = '新密码须为 5～16 位，且不能包含空白。'
    return
  }
  if (new_pwd !== re_pwd) { passwordError.value = '两次输入的新密码不一致。'; return }
  changingPassword.value = true
  try {
    await javaRequest('/user/updatePwd', { method: 'PATCH', body: { old_pwd, new_pwd, re_pwd } })
    auth.clear()
    await router.replace({ path: '/login', query: { passwordChanged: '1' } })
  } catch (e) { passwordError.value = e.message }
  finally { changingPassword.value = false }
}
const profile = ref(null), busy = ref(false), error = ref(''), saved = ref(false)
async function load() {
  const user = await javaRequest('/user/userInfo')
  auth.user = user
  profile.value = { id: user.id, username: user.username, nickname: user.nickname ?? '', email: user.email ?? '' }
}
async function run(action) {
  if (busy.value) return
  busy.value = true; error.value = ''; saved.value = false
  try { await action() } catch (e) { error.value = e.message } finally { busy.value = false }
}
onMounted(() => run(load))
async function save() {
  const { id, nickname, email } = profile.value
  if (!/^\S{1,10}$/.test(nickname)) throw new Error('昵称须为 1～10 个非空白字符。')
  await javaRequest('/user/update', { method: 'PUT', body: { id, nickname, email } })
  await load()
  saved.value = true
}
</script>
<template>
  <main class="profile">
    <h2>个人资料</h2>
    <p v-if="error" role="alert">{{ error }}</p>
    <p v-if="saved" role="status">资料已保存。</p>
    <form v-if="profile" @submit.prevent="run(save)">
      <fieldset :disabled="busy || changingPassword">
        <label>用户名<input :value="profile.username" readonly /></label>
        <label>昵称<input v-model.trim="profile.nickname" maxlength="10" required /></label>
        <label>邮箱<input v-model.trim="profile.email" type="email" required /></label>
        <button>保存资料</button>
      </fieldset>
    </form>
    <p v-else-if="busy" role="status">正在获取用户资料…</p>
    <button v-else @click="run(load)">重新加载</button>
    <section v-if="profile" class="password-section">
      <h2>修改密码</h2>
      <form @submit.prevent="changePassword">
        <fieldset :disabled="changingPassword || busy">
          <label>当前密码<input name="old_pwd" type="password" autocomplete="current-password" required /></label>
          <label>新密码<input name="new_pwd" type="password" autocomplete="new-password" required /></label>
          <label>确认新密码<input name="re_pwd" type="password" autocomplete="new-password" required /></label>
          <p v-if="passwordError" role="alert">{{ passwordError }}</p>
          <button>{{ changingPassword ? '正在修改…' : '修改密码' }}</button>
        </fieldset>
      </form>
    </section>
  </main>
</template>
<style scoped>
.profile { background: white; padding: 24px; border: 1px solid #ebeef5; }
.password-section { border-top: 1px solid #ebeef5; margin-top: 28px; padding-top: 12px; }
fieldset { border: 0; padding: 0; max-width: 420px; margin: 0; }
label { display: block; margin: 16px 0; }
input { display: block; width: 100%; margin-top: 8px; padding: 10px; border: 1px solid #dcdfe6; border-radius: 4px; }
input[readonly] { background: #f4f4f5; }
button { background: #409eff; color: white; border: 0; border-radius: 4px; padding: 10px 18px; cursor: pointer; }
[role='alert'] { color: #b42318; }
</style>
