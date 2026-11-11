<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
const router = useRouter()
function logout() { auth.clear(); router.replace('/login') }
</script>
<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand"><span class="brand-icon">知</span><div>学习笔记<small>KNOWLEDGE NOTES</small></div></div>
      <nav aria-label="工作台导航"><RouterLink to="/workspace/notes"><span>▤</span> 笔记管理</RouterLink><RouterLink to="/workspace/chat"><span>✧</span> AI 问答</RouterLink><RouterLink to="/workspace/profile"><span>◎</span> 个人资料</RouterLink></nav>
      <div class="sidebar-bottom">记录 · 理解 · 复习</div>
    </aside>
    <div class="body">
      <header><span>欢迎回来：<strong>{{ auth.user?.nickname || auth.user?.username }}</strong></span><div class="user"><span class="avatar">{{ (auth.user?.nickname || auth.user?.username || '我').slice(0,1) }}</span><button @click="logout">退出登录</button></div></header>
      <!-- 子路由默认展示笔记表格，侧栏和顶部用户栏保持不动。 -->
      <div class="content"><RouterView /></div>
      <footer>学习笔记助手 · 让每一份知识有所积累</footer>
    </div>
  </div>
</template>
<style scoped>
.admin-layout{display:flex;min-height:100vh;font-family:"Microsoft YaHei",system-ui,sans-serif;color:#606266;font-size:14px;background:#f4f4f4}.sidebar{width:208px;flex-shrink:0;background:#232323;color:#ddd;position:fixed;inset:0 auto 0 0;display:flex;flex-direction:column}.brand{height:100px;display:flex;align-items:center;justify-content:center;gap:10px;font-size:21px;font-weight:600;color:white}.brand-icon{font-size:28px;border:2px solid white;border-radius:50%;width:44px;height:44px;display:grid;place-items:center}.brand small{font-size:8px;display:block;letter-spacing:1px;margin-top:5px;color:#aaa}.sidebar nav{display:flex;flex-direction:column;margin-top:12px}.sidebar a{text-decoration:none;color:#ddd;padding:20px 24px;border-left:3px solid transparent;display:flex;gap:12px}.sidebar a:hover{background:#303030}.sidebar a.router-link-active{color:#e7c56a;background:#2d2c28;border-left-color:#e7c56a}.sidebar-bottom{margin-top:auto;padding:24px;font-size:12px;color:#777}.body{margin-left:208px;flex:1;min-width:0}header{height:64px;background:white;padding:0 28px;display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid #eee}.user{display:flex;align-items:center;gap:12px}.avatar{width:32px;height:32px;border-radius:50%;background:#e8f3ff;color:#409eff;display:grid;place-items:center}button{background:none;border:0;color:#909399;cursor:pointer;padding:8px}.content{padding:20px}footer{text-align:center;padding:24px;color:#a0a0a0;font-size:12px}@media(max-width:760px){.sidebar{width:136px}.body{margin-left:136px}.brand{font-size:16px;gap:6px}.brand-icon{display:none}.sidebar a{padding:18px 12px}.content{padding:12px}header{padding:0 14px}.sidebar-bottom{padding:14px;font-size:10px}}@media(max-width:460px){.sidebar{width:104px}.body{margin-left:104px}.brand small{display:none}.sidebar a{font-size:12px}.sidebar a span{display:none}.user .avatar{display:none}}
</style>

