<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (token) {
    localStorage.setItem('token', token);
    router.replace('/');
  }
});
</script>

<style>
/* 구글 폰트 적용 */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600&display=swap');

html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: 'Inter', sans-serif;
  background-color: #f9f9f9;
}

/* 공통 버튼 스타일 */
button {
  font-family: 'Inter', sans-serif;
  font-size: 16px;
  padding: 10px 16px;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.3s;
}

/* 다크모드 스타일 */
.dark-mode {
  background-color: #121212;
  color: #ffffff;
}
</style>
