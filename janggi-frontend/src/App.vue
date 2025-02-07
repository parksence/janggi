<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

onMounted(() => {
  // URL에서 토큰 파라미터 확인
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');
  
  if (token) {
    // 토큰을 localStorage에 저장
    localStorage.setItem('token', token);
    // URL에서 token 파라미터 제거
    router.replace('/');
  }
});
</script>

<style>
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: Arial, sans-serif;
}
</style>
