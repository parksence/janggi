<template>
  <div class="home-container">
    <h1>장기 게임</h1>

    <div class="player-name-section">
      <label for="playerName">플레이어 이름:</label>
      <div class="name-display">
        <span class="player-name">{{ playerName }}</span>
        <button @click="generateNewName" class="regenerate-btn">새 이름</button>
      </div>
    </div>

    <div v-if="games.length > 0" class="game-list">
      <div v-for="game in games" :key="game.id" class="game-card">
        <div class="game-info">
          <h3 class="room-title">{{ game.roomTitle || '무제 게임' }}</h3>
          <div class="host-info">
            <span class="host-name">{{ game.redPlayerName }}</span>
            <span class="waiting-text">님이 대국을 기다리고 있습니다.</span>
          </div>
        </div>
        <button 
          @click="joinGame(game.id)" 
          class="join-btn"
          :disabled="joiningGameId === game.id"
        >
          <span v-if="joiningGameId === game.id" class="spinner"></span>
          {{ joiningGameId === game.id ? '참여 중...' : '참여하기' }}
        </button>
      </div>
    </div>
    <div v-else class="empty-state">
      <p>대기 중인 게임이 없습니다.</p>
    </div>

    <div class="button-group">
      <button class="primary-btn" @click="showCreateGameModal = true">새 게임 만들기</button>
      <button 
        class="secondary-btn" 
        @click="refreshGames"
        :disabled="isRefreshing"
      >
        <span v-if="isRefreshing" class="spinner"></span>
        {{ isRefreshing ? '새로고침 중...' : '새로고침' }}
      </button>
    </div>

    <!-- 새 게임 만들기 모달 -->
    <div v-if="showCreateGameModal" class="modal-overlay" @click="showCreateGameModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>새 게임 만들기</h3>
          <p class="modal-subtitle">게임 방의 제목을 입력해주세요</p>
        </div>
        <div class="form-group">
          <input 
            id="roomTitle"
            v-model="roomTitle" 
            type="text" 
            placeholder="방 제목을 입력하세요"
            class="room-title-input"
            @keyup.enter="createGame"
          />
        </div>
        <div class="modal-buttons">
          <button 
            @click="createGame" 
            class="confirm-btn"
            :disabled="isCreatingGame"
          >
            <span v-if="isCreatingGame" class="spinner"></span>
            {{ isCreatingGame ? '게임 생성 중...' : '게임 생성' }}
          </button>
          <button 
            @click="showCreateGameModal = false" 
            class="cancel-btn"
            :disabled="isCreatingGame"
          >
            취소
          </button>
        </div>
      </div>
    </div>

    <!-- 설정 팝업 -->
    <SettingsView v-if="showSettings" @close="showSettings = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAlert } from '@/composables/useAlert';
import { useGameStore } from '@/stores/gameStore';
import SettingsView from './SettingsView.vue';

const router = useRouter();
const { success, error, warning } = useAlert();
const gameStore = useGameStore();
const games = ref<{ id: number; redPlayerName: string; roomTitle?: string }[]>([]);
const playerName = ref('');
const roomTitle = ref('');
const showSettings = ref(false);
const showCreateGameModal = ref(false);

const fetchGames = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/game/waiting');
    games.value = await response.json();
  } catch (error) {
    console.error("게임 목록을 불러오지 못했습니다.");
  }
};

// 플레이어 이름 자동 생성
const generatePlayerName = () => {
  const adjectives = ['빠른', '똑똑한', '용감한', '재치있는', '신중한', '활발한', '차분한', '열정적인'];
  const nouns = ['호랑이', '독수리', '사자', '늑대', '여우', '곰', '표범', '수달'];
  const adjective = adjectives[Math.floor(Math.random() * adjectives.length)];
  const noun = nouns[Math.floor(Math.random() * nouns.length)];
  const number = Math.floor(Math.random() * 999) + 1;
  return `${adjective}${noun}${number}`;
};

const generateNewName = () => {
  playerName.value = generatePlayerName();
};

const createGame = async () => {
  if (isCreatingGame.value) return; // 이미 게임 생성 중이면 무시
  
  if (!roomTitle.value.trim()) {
    await warning(
      '방 제목 필요',
      '방 제목을 입력해주세요.'
    );
    return;
  }

  isCreatingGame.value = true;
  try {
    // 게임 스토어를 사용하여 게임 생성
    await gameStore.createGame(playerName.value, roomTitle.value);
    
    // 플레이어 이름을 localStorage에 저장
    localStorage.setItem('playerName', playerName.value);
    
    // 모달 닫기
    showCreateGameModal.value = false;
    roomTitle.value = '';
    
    // 바로 게임 화면으로 이동
    router.push('/game');
  } catch (error) {
    console.error("게임 생성 중 오류가 발생했습니다.");
    await error(
      '게임 생성 오류',
      '게임 생성 중 오류가 발생했습니다. 다시 시도해주세요.'
    );
  } finally {
    isCreatingGame.value = false;
  }
};

const joinGame = async (gameId: number) => {
  if (joiningGameId.value === gameId) return; // 이미 참여 중인 게임이면 무시
  
  if (!playerName.value.trim()) {
    await warning(
      '플레이어 이름 필요',
      '플레이어 이름을 입력해주세요.'
    );
    return;
  }

  joiningGameId.value = gameId;
  try {
    const response = await fetch(`http://localhost:8080/api/game/${gameId}/join`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ playerName: playerName.value })
    });

    if (response.ok) {
      // 플레이어 이름을 localStorage에 저장
      localStorage.setItem('playerName', playerName.value);
      
      // 게임 정보를 gameStore에 저장
      const gameData = await response.json();
      gameStore.setCurrentGame(gameData);
      
      // 성공 알럿 없이 바로 게임 화면으로 이동
      router.push('/game');
    } else {
      await error(
        '게임 참여 실패',
        '게임 참여에 실패했습니다. 다시 시도해주세요.'
      );
    }
  } catch (err) {
    console.error("게임 참여 중 오류가 발생했습니다.");
    await error(
      '게임 참여 오류',
      '게임 참여 중 오류가 발생했습니다. 다시 시도해주세요.'
    );
  } finally {
    joiningGameId.value = null;
  }
};

const isRefreshing = ref(false);
const isCreatingGame = ref(false);
const joiningGameId = ref<number | null>(null);

const refreshGames = async () => {
  if (isRefreshing.value) return; // 이미 새로고침 중이면 무시
  
  isRefreshing.value = true;
  try {
    await fetchGames();
    // 새로고침 완료 시 즉시 스피너 종료
    isRefreshing.value = false;
  } catch (error) {
    isRefreshing.value = false;
    await error(
      '새로고침 실패',
      '게임 목록을 불러오는데 실패했습니다.'
    );
  }
};

onMounted(() => {
  // 플레이어 이름 자동 생성
  playerName.value = generatePlayerName();
  fetchGames();
});
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  text-align: center;
  padding: 20px;
}


h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 20px;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.player-name-section {
  margin-bottom: 20px;
}

.player-name-section label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: white;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.name-input {
  padding: 8px 12px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  width: 200px;
}

.name-input:focus {
  outline: none;
  border-color: #007bff;
}

.game-list {
  margin-bottom: 20px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.game-card {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.game-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  border-color: #d0d0d0;
}

.room-title {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.6rem;
  font-weight: bold;
  text-align: left;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 8px;
}

.host-info {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.host-name {
  font-weight: bold;
  color: #e74c3c;
  font-size: 1rem;
}

.waiting-text {
  color: #666;
  font-size: 0.9rem;
}

.join-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.join-btn:hover {
  background: #218838;
}

.empty-state {
  margin-bottom: 20px;
  color: white;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 12px;
  max-width: 600px;
  margin: 0 auto;
}

/* 버튼 공통 스타일 */
.primary-btn, .secondary-btn {
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  min-width: 150px;
}

.primary-btn {
  background: #007bff;
  color: white;
}

.primary-btn:hover {
  background: #0056b3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.secondary-btn {
  background: #28a745;
  color: white;
}

.secondary-btn:hover {
  background: #218838;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
}

/* 플레이어 이름 표시 */
.name-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 8px;
}

.player-name {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 16px;
}

.regenerate-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.regenerate-btn:hover {
  background: #5a6268;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 0;
  border-radius: 24px;
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  min-width: 450px;
  max-width: 520px;
  overflow: hidden;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.modal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 35px 35px 25px 35px;
  text-align: center;
  position: relative;
}

.modal-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  pointer-events: none;
}

.modal-header h3 {
  margin: 0 0 12px 0;
  color: white;
  font-size: 1.8rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.modal-subtitle {
  margin: 0;
  color: rgba(255, 255, 255, 0.95);
  font-size: 1rem;
  font-weight: 400;
  position: relative;
  z-index: 1;
}

.form-group {
  padding: 35px;
  margin: 0;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
}

.room-title-input {
  width: 100%;
  padding: 18px 24px;
  border: 2px solid #e8ecf7;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 500;
  box-sizing: border-box;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.room-title-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 
    0 0 0 4px rgba(102, 126, 234, 0.15),
    0 8px 20px rgba(102, 126, 234, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transform: translateY(-1px);
}

.room-title-input::placeholder {
  color: #a0a8c0;
  font-weight: 400;
}

.modal-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding: 0 35px 35px 35px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
}

.confirm-btn, .cancel-btn {
  padding: 14px 28px;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 120px;
  position: relative;
  overflow: hidden;
}

.confirm-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 
    0 6px 20px rgba(102, 126, 234, 0.3),
    0 2px 8px rgba(0, 0, 0, 0.1);
}

.confirm-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 25px rgba(102, 126, 234, 0.4),
    0 4px 12px rgba(0, 0, 0, 0.15);
}

.confirm-btn:hover:not(:disabled)::before {
  left: 100%;
}

.confirm-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.cancel-btn {
  background: white;
  color: #6c757d;
  border: 2px solid #e8ecf7;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.cancel-btn:hover:not(:disabled) {
  background: #f8f9ff;
  border-color: #d1d9f0;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 스피너 애니메이션 */
.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid #ffffff;
  border-radius: 50%;
  border-top-color: transparent;
  animation: spin 1s ease-in-out infinite;
  margin-right: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 버튼 비활성화 상태 */
.secondary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
