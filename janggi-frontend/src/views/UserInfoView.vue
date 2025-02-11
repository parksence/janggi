<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal user-modal">
      <h2>내 정보</h2>
      <div class="user-info">
        <p>닉네임: {{ user.nickname }}</p>
        <p>전적: {{ user.wins }}승 {{ user.losses }}패</p>
      </div>

      <div class="button-group">
        <button class="secondary-btn" @click="openSettings">설정</button>
        <button class="primary-btn" @click="close">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineEmits } from 'vue';

const emit = defineEmits(['close', 'openSettings']);

const user = ref({ nickname: '', wins: 0, losses: 0 });

const fetchUserInfo = async () => {
  try {
    const response = await fetch('/api/user-info');
    user.value = await response.json();
  } catch (error) {
    console.error("유저 정보를 불러오지 못했습니다.");
  }
};

const close = () => {
  emit('close');
};

const openSettings = () => {
  emit('openSettings');
};

onMounted(fetchUserInfo);
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(10px);
}

.modal {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  max-width: 400px;
  width: 100%;
  text-align: center;
  animation: fadeIn 0.3s ease;
  color: #333;
}

.button-group {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.user-info {
  margin: 20px 0;
}

/* 다크모드 스타일 */
:global(.dark-mode) .modal {
  background: #1e1e1e;
  color: #ffffff;
}

:global(.dark-mode) .button-group button {
  background-color: #333;
  color: #ffffff;
  border: 1px solid #444;
}

:global(.dark-mode) .button-group button:hover {
  background-color: #444;
}
</style>
