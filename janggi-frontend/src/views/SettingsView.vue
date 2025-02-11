<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal settings-modal">
      <h2>설정</h2>

      <label>
        <input type="checkbox" v-model="darkMode" @change="toggleDarkMode">
        다크모드
      </label>

      <button @click="logout">로그아웃</button>
      <button @click="deleteAccount">게임 탈퇴</button>

      <button @click="close">확인</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits } from 'vue';
import { useRouter } from 'vue-router';

const emit = defineEmits(['close']);
const router = useRouter();
const darkMode = ref(false);

const toggleDarkMode = () => {
  document.body.classList.toggle('dark-mode', darkMode.value);
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
  z-index: 10;
}

.modal {
  background: white;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  position: relative;
}

.settings-modal {
  z-index: 20;
}
</style>
