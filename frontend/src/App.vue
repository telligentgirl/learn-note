<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <!-- 路由决定这里显示哪个页面，目前显示登录页。 -->
  <RouterView />
</template>

<style>
body { margin: 0; background: #f5f7fb; }
* { box-sizing: border-box; }
button, input, select, textarea { font: inherit; }
:focus-visible { outline: 3px solid #a5baff; outline-offset: 3px; }
</style>
