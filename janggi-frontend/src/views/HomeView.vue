<template>
  <div class="home-container">
    <h1>대기실</h1>

    <div v-if="games.length > 0" class="game-list">
      <div v-for="game in games" :key="game.id" class="game-card">
        <p><strong>{{ game.host }}</strong> 님이 대국을 기다리고 있습니다.</p>
      </div>
    </div>
    <div v-else class="empty-state">
      <p>대국 중인 사람이 없습니다.</p>
    </div>

    <div class="button-group">
      <button class="primary-btn" @click="showUserInfo = true">내 정보</button>
      <button class="secondary-btn" @click="requestGame">대국 신청</button>
    </div>

    <!-- 내 정보 팝업 -->
    <UserInfoView v-if="showUserInfo" @close="showUserInfo = false" @openSettings="showSettings = true" />

    <!-- 설정 팝업 (내 정보보다 위에 위치) -->
    <SettingsView v-if="showSettings" @close="showSettings = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import UserInfoView from './UserInfoView.vue';
import SettingsView from './SettingsView.vue';

const games = ref<{ id: number; host: string }[]>([]);
const showUserInfo = ref(false);
const showSettings = ref(false);

const fetchGames = async () => {
  try {
    const response = await fetch('/api/games');
    games.value = await response.json();
  } catch (error) {
    console.error("게임 목록을 불러오지 못했습니다.");
  }
};

const requestGame = () => {
  console.log("대국 신청!");
};

onMounted(fetchGames);
</script>

<style scoped>
.home-container {
  text-align: center;
  padding: 20px;
}

h1 {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* 버튼 공통 스타일 */
.primary-btn, .secondary-btn {
  padding: 12px 18px;
  border-radius: 25px;
  font-size: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
}

.primary-btn {
  background: #007bff;
  color: white;
}

.primary-btn:hover {
  background: #0056b3;
}

.secondary-btn {
  background: transparent;
  color: #007bff;
  border: 2px solid #007bff;
}

.secondary-btn:hover {
  background: #007bff;
  color: white;
}
</style>
