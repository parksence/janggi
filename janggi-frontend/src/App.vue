<template>
  <div :class="{ 'dark-mode': darkModeStore.isDarkMode }">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useDarkModeStore } from '@/stores/darkModeStore';

const router = useRouter();
const darkModeStore = useDarkModeStore();

onMounted(async () => {
  // OAuth 토큰 처리
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (token) {
    localStorage.setItem('token', token);
    router.replace('/');
  }

  // 로컬스토리지에서 다크모드 상태 확인
  const savedDarkMode = localStorage.getItem('darkMode');
  
  if (savedDarkMode !== null) {
    // 로컬스토리지에 저장된 값이 있으면 그 값을 사용
    darkModeStore.isDarkMode = savedDarkMode === 'true';
    document.documentElement.classList.toggle('dark-mode', darkModeStore.isDarkMode);
  } else {
    // 저장된 값이 없으면 서버에서 값을 가져옴
    await darkModeStore.fetchDarkModeStatus();
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
  transition: background 0.3s, color 0.3s;
}

/* 공통 버튼 스타일 */
button {
  font-family: 'Inter', sans-serif;
  font-size: 16px;
  padding: 10px 16px;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.3s, color 0.3s;
}

/* 다크모드 전역 스타일 */
.dark-mode {
  background-color: #121212;
  color: #ffffff;
}

.dark-mode button {
  background-color: #333;
  color: #ffffff;
  border: 1px solid #444;
}

.dark-mode button:hover {
  background-color: #444;
}

/* 다크 모드가 적용되었을 때 폰트 색상 유지 */
.dark-mode html, .dark-mode body {
  background-color: #121212;
  color: #ffffff;
  transition: background 0.3s ease-in-out, color 0.3s ease-in-out;
}
</style>
