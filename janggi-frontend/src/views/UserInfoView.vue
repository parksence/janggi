<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal">
      <h2>내 정보</h2>
      <p>닉네임: {{ user.nickname }}</p>
      <p>전적: {{ user.wins }}승 {{ user.losses }}패</p>

      <button @click="openSettings">설정</button>
      <button @click="close">닫기</button>
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal {
  background: white;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
}
</style>
