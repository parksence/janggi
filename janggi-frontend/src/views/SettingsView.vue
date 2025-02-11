<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal settings-modal">
      <!-- 닫기 버튼 -->
      <button class="close-btn material-icons" @click="close">close</button>
      <h2>설정</h2>

      <div class="setting-item">
        <span>다크모드</span>
        <label class="toggle">
          <input type="checkbox" v-model="darkMode" @change="toggleDarkMode">
          <span class="slider"></span>
        </label>
      </div>

      <hr />

      <div class="button-group">
        <button class="secondary-btn" @click="logout">로그아웃</button>
        <button class="danger-btn" @click="deleteAccount">게임 탈퇴</button>
        <button class="primary-btn" @click="close">확인</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useDarkModeStore } from '@/stores/darkModeStore';

const emit = defineEmits(['close']);
const router = useRouter();
const darkModeStore = useDarkModeStore();
const darkMode = ref(darkModeStore.isDarkMode);

onMounted(async () => {
  await darkModeStore.fetchDarkModeStatus();
  darkMode.value = darkModeStore.isDarkMode;
});

const toggleDarkMode = async () => {
  await darkModeStore.toggleDarkMode();
  darkMode.value = darkModeStore.isDarkMode;
};

const logout = () => {
  localStorage.removeItem('token');
  router.push('/login');
};

const deleteAccount = async () => {
  if (confirm("정말로 탈퇴하시겠습니까?")) {
    await fetch('/api/user-delete', { method: 'DELETE' });
    localStorage.removeItem('token');
    router.push('/login');
  }
};

const close = () => {
  emit('close');
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

body {
  font-family: 'Inter', sans-serif;
}

/* 팝업 배경 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(8px);
}

/* 팝업 스타일 */
.modal {
  background: white;
  padding: 24px;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  width: 360px;
  text-align: center;
  position: relative;
  animation: fadeIn 0.3s ease-in-out;
}

/* 닫기 버튼 */
.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background-color: rgba(0, 0, 0, 0.1);
  color: #333;
}

/* 다크모드에서의 닫기 버튼 스타일 */
:global(.dark-mode) .close-btn {
  color: #999;
}

:global(.dark-mode) .close-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* 제목 */
h2 {
  margin-bottom: 12px;
  font-size: 20px;
  font-weight: bold;
}

/* 설정 항목 */
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  font-size: 16px;
}

/* 가로선 */
hr {
  border: none;
  height: 1px;
  background: #e0e0e0;
  margin: 10px 0;
}

/* 버튼 그룹 */
.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

/* 버튼 스타일 */
.primary-btn, .secondary-btn, .danger-btn {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  transition: 0.3s;
  cursor: pointer;
  border: none;
  width: 100%;
}

.primary-btn {
  background: #007bff;
  color: white;
}

.primary-btn:hover {
  background: #0056b3;
}

.secondary-btn {
  background: #f8f9fa;
  color: #333;
  border: 1px solid #ccc;
}

.secondary-btn:hover {
  background: #e0e0e0;
}

.danger-btn {
  background: #ff4d4d;
  color: white;
}

.danger-btn:hover {
  background: #cc0000;
}

/* 토글 스위치 */
.toggle {
  position: relative;
  width: 48px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

/* 슬라이더 */
.slider {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: #ddd;
  border-radius: 50px;
  transition: 0.3s ease-in-out;
  cursor: pointer;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
}

.slider::before {
  content: "";
  position: absolute;
  width: 18px;
  height: 18px;
  background: white;
  border-radius: 50%;
  top: 3px;
  left: 4px;
  transition: 0.3s ease-in-out;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

input:checked + .slider {
  background-color: #007bff;
}

input:checked + .slider::before {
  transform: translateX(24px);
}

/* 다크 모드 */
.dark-mode .modal {
  background: #181818;
  color: #f5f5f5;
  border: 1px solid #444;
}

.dark-mode h2 {
  font-weight: 600;
  color: #f5f5f5;
}

.dark-mode .slider {
  background-color: #444;
}

.dark-mode input:checked + .slider {
  background-color: #007bff;
}
</style>
