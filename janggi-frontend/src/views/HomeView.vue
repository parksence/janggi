<template>
  <div class="home-container">
    <h1>대기실</h1>
    <div v-if="games.length > 0">
      <ul>
        <li v-for="game in games" :key="game.id">
          {{ game.host }} 님이 대국을 기다리고 있습니다.
        </li>
      </ul>
    </div>
    <div v-else>
      <p>대국 중인 사람이 없습니다.</p>
    </div>

    <button @click="showUserInfo = true">내 정보</button>
    <button @click="requestGame">대국 신청</button>

    <!-- 내 정보 팝업 -->
    <UserInfoView v-if="showUserInfo" @close="showUserInfo = false" @openSettings="showSettings = true" />

    <!-- 설정 팝업 (내 정보보다 위에 있음) -->
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
}
</style>
