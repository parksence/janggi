<template>
  <div class="game-view">
    <div class="game-header">
      <div class="game-info" v-if="gameStore.currentGame">
        <div class="players-info">
          <div class="player red-player">
            <span class="player-name">{{ gameStore.currentGame.redPlayerName || '대기 중' }}</span>
            <span class="player-side">한나라</span>
          </div>
          <div class="vs">VS</div>
          <div class="player blue-player">
            <span class="player-name">{{ gameStore.currentGame.bluePlayerName || '대기 중' }}</span>
            <span class="player-side">초나라</span>
          </div>
        </div>
        
        <div class="turn-info" v-if="gameStore.gameStatus === 'IN_PROGRESS'">
          <div class="current-turn">
            <span class="turn-label">현재 턴:</span>
            <span class="turn-player" :class="gameStore.currentGame.currentTurn === 'RED' ? 'red' : 'blue'">
              {{ gameStore.currentGame.currentTurn === 'RED' ? '한나라' : '초나라' }}
            </span>
          </div>
          <div class="turn-timer">
            <span class="timer-label">남은 시간:</span>
            <span class="timer-value">{{ formatTimer(turnTimeLeft) }}</span>
          </div>
        </div>
        
        <div class="game-status" v-else>
          <span v-if="gameStore.isLoading" class="loading">로딩 중...</span>
          <span v-else-if="gameStore.gameStatus === 'WAITING'" class="waiting">게임 대기 중</span>
          <span v-else-if="gameStore.gameStatus === 'FINISHED'" class="finished">게임 종료</span>
        </div>
      </div>
    </div>

    <!-- 카운트다운 오버레이 -->
    <div v-if="showCountdown" class="countdown-overlay">
      <div class="countdown-container">
        <div class="countdown-number">{{ countdownNumber }}</div>
        <div class="countdown-text">게임 시작!</div>
      </div>
    </div>

    <div class="game-content">
      <div class="main-game-area">
        <JanggiBoard />
      </div>
      
      <div class="game-sidebar">
        <div class="game-list">
          <h3>대기 중인 게임</h3>
          <div v-if="gameStore.waitingGames.length === 0" class="no-games">
            대기 중인 게임이 없습니다.
          </div>
          <div v-else class="game-list-items">
            <div 
              v-for="game in gameStore.waitingGames" 
              :key="game.id"
              class="game-item"
              @click="joinGame(game.id)"
            >
              <div class="game-info">
                <div class="game-title">{{ game.roomTitle || '무제 게임' }}</div>
                <div class="game-details">
                  <span class="host-name">{{ game.redPlayerName }} 님</span>
                  <span class="created-time">{{ formatTime(game.createdAt) }}</span>
                </div>
              </div>
              <button class="join-btn">참여하기</button>
            </div>
          </div>
        </div>

        <div class="move-history">
          <h3>이동 기록</h3>
          <div v-if="gameStore.moveHistory.length === 0" class="no-moves">
            이동 기록이 없습니다.
          </div>
          <div v-else class="move-list">
            <div 
              v-for="move in gameStore.moveHistory" 
              :key="move.id"
              class="move-item"
            >
              <span class="move-number">{{ move.moveNumber }}.</span>
              <span class="move-details">
                {{ getPieceText(move.pieceType) }} 
                ({{ move.fromRow + 1 }}, {{ move.fromCol + 1 }}) → 
                ({{ move.toRow + 1 }}, {{ move.toCol + 1 }})
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useGameStore } from '@/stores/gameStore';
import JanggiBoard from '@/components/game/JanggiBoard.vue';
import type { PieceType } from '@/types/game';

const gameStore = useGameStore();

// 카운트다운 관련 변수
const showCountdown = ref(false);
const countdownNumber = ref(3);
let countdownInterval: number | null = null;

// 턴 타이머 관련 변수
const turnTimeLeft = ref(0);
const TURN_TIME = 30; // 30초
let turnTimer: number | null = null;

const pieceTexts = {
  KING: '왕',
  GUARD: '사',
  ELEPHANT: '상',
  HORSE: '마',
  CHARIOT: '차',
  CANNON: '포',
  SOLDIER: '졸'
};

function getPieceText(pieceType: PieceType): string {
  return pieceTexts[pieceType] || pieceType;
}

function getPlayerCount(): number {
  if (!gameStore.currentGame) return 0;
  let count = 0;
  if (gameStore.currentGame.redPlayerName) count++;
  if (gameStore.currentGame.bluePlayerName) count++;
  return count;
}

function getCurrentPlayerName(): string {
  const playerName = localStorage.getItem('playerName');
  return playerName || '게스트';
}

// 턴 타이머 시작
function startTurnTimer(): void {
  turnTimeLeft.value = TURN_TIME;
  
  turnTimer = setInterval(() => {
    turnTimeLeft.value--;
    
    if (turnTimeLeft.value <= 0) {
      clearInterval(turnTimer!);
      turnTimer = null;
      // 시간 초과 처리
      handleTimeOut();
    }
  }, 1000);
}

// 턴 타이머 정지
function stopTurnTimer(): void {
  if (turnTimer) {
    clearInterval(turnTimer);
    turnTimer = null;
  }
}

// 시간 초과 처리
function handleTimeOut(): void {
  // 시간 초과 시 자동으로 턴 넘기기
  console.log('시간 초과! 턴이 넘어갑니다.');
  // TODO: 서버에 시간 초과 알림
}

// 타이머 시간 포맷팅
function formatTimer(seconds: number): string {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}

// 카운트다운 시작 함수
function startCountdown(): void {
  showCountdown.value = true;
  countdownNumber.value = 3;
  
  countdownInterval = setInterval(() => {
    countdownNumber.value--;
    
    if (countdownNumber.value <= 0) {
      clearInterval(countdownInterval!);
      countdownInterval = null;
      
      // 카운트다운 완료 후 0.5초 뒤에 숨기기
      setTimeout(() => {
        showCountdown.value = false;
      }, 500);
    }
  }, 1000);
}

// 게임 상태 변화 감지
watch(() => gameStore.gameStatus, (newStatus, oldStatus) => {
  // WAITING에서 IN_PROGRESS로 변경될 때 카운트다운 시작
  if (oldStatus === 'WAITING' && newStatus === 'IN_PROGRESS') {
    startCountdown();
  }
});

// 현재 턴 변화 감지
watch(() => gameStore.currentGame?.currentTurn, (newTurn, oldTurn) => {
  if (newTurn && newTurn !== oldTurn && gameStore.gameStatus === 'IN_PROGRESS') {
    // 턴이 바뀔 때마다 타이머 재시작
    stopTurnTimer();
    startTurnTimer();
  }
});

function formatTime(dateString: any): string {
  try {
    // null, undefined, 빈 문자열 체크
    if (!dateString || dateString === null || dateString === undefined) {
      return '시간 정보 없음';
    }
    
    // 배열인 경우 (예: [2025, 9, 18, 17, 35, 23, 522325000])
    if (Array.isArray(dateString)) {
      if (dateString.length >= 6) {
        const [year, month, day, hour, minute, second] = dateString;
        const date = new Date(year, month - 1, day, hour, minute, second);
        if (!isNaN(date.getTime())) {
          return date.toLocaleTimeString('ko-KR', { 
            hour: '2-digit', 
            minute: '2-digit' 
          });
        }
      }
      return '시간 정보 없음';
    }
    
    // 문자열인 경우
    if (typeof dateString === 'string') {
      // 빈 문자열 체크
      if (!dateString.trim()) {
        return '시간 정보 없음';
      }
      
      // ISO 형식 처리
      let normalizedDateString = dateString;
      
      // +0900 형식을 +09:00 형식으로 변환
      normalizedDateString = normalizedDateString.replace(/\+(\d{2})(\d{2})$/, '+$1:$2');
      // -0900 형식을 -09:00 형식으로 변환
      normalizedDateString = normalizedDateString.replace(/-(\d{2})(\d{2})$/, '-$1:$2');
      
      const date = new Date(normalizedDateString);
      
      // 유효한 날짜인지 확인
      if (!isNaN(date.getTime())) {
        return date.toLocaleTimeString('ko-KR', { 
          hour: '2-digit', 
          minute: '2-digit' 
        });
      }
    }
    
    return '시간 정보 없음';
  } catch (error) {
    // 에러 로그 제거하여 콘솔 정리
    return '시간 정보 없음';
  }
}

async function joinGame(gameId: number): Promise<void> {
  try {
    const playerName = localStorage.getItem('playerName');
    if (!playerName) {
      alert('플레이어 이름이 없습니다. 홈으로 돌아가서 이름을 입력해주세요.');
      return;
    }
    await gameStore.joinGame(gameId, playerName);
    // 게임 참여 후 현재 게임 정보 로드
    await gameStore.loadCurrentGame();
  } catch (error) {
    console.error('게임 참여 실패:', error);
  }
}

onMounted(async () => {
  await gameStore.loadWaitingGames();
  
  // 게임이 없고 플레이어 이름이 없으면 홈으로 돌아가기
  const playerName = localStorage.getItem('playerName');
  if (!playerName) {
    alert('플레이어 이름이 없습니다. 홈으로 돌아갑니다.');
    window.location.href = '/';
    return;
  }
  
  // 현재 게임이 없으면 활성 게임을 로드
  if (!gameStore.currentGame) {
    await gameStore.loadCurrentGame();
  } else {
    // 이미 게임이 있으면 게임보드와 이동 기록 로드
    await gameStore.loadGameBoard();
    if (gameStore.currentGame.id) {
      await gameStore.loadMoveHistory(gameStore.currentGame.id);
    }
  }
  
  // 게임이 진행 중이면 턴 타이머 시작
  if (gameStore.gameStatus === 'IN_PROGRESS') {
    startTurnTimer();
  }
});

onUnmounted(() => {
  // 카운트다운 정리
  if (countdownInterval) {
    clearInterval(countdownInterval);
    countdownInterval = null;
  }
  
  // 턴 타이머 정리
  stopTurnTimer();
  
  gameStore.resetGame();
});
</script>

<style scoped>
.game-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 50%, #CD853F 100%);
  background-image: 
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.1) 1px, transparent 1px),
    radial-gradient(circle at 80% 80%, rgba(0, 0, 0, 0.1) 1px, transparent 1px);
  background-size: 50px 50px, 30px 30px;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.game-header {
  text-align: center;
  margin-bottom: 20px;
  color: white;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.players-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 15px;
}

.player {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 20px;
  border-radius: 12px;
  backdrop-filter: blur(5px);
  min-width: 120px;
}

.player.red-player {
  background: rgba(244, 67, 54, 0.2);
  border: 2px solid rgba(244, 67, 54, 0.3);
}

.player.blue-player {
  background: rgba(33, 150, 243, 0.2);
  border: 2px solid rgba(33, 150, 243, 0.3);
}

.player-name {
  font-size: 1.1rem;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.player-side {
  font-size: 0.9rem;
  opacity: 0.8;
  font-weight: 500;
}

.vs {
  font-size: 1.2rem;
  font-weight: 700;
  color: #FFD700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.turn-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 12px 20px;
  border-radius: 12px;
  backdrop-filter: blur(5px);
}

.current-turn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.turn-label {
  font-size: 0.9rem;
  opacity: 0.8;
}

.turn-player {
  font-size: 1rem;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 8px;
}

.turn-player.red {
  background: rgba(244, 67, 54, 0.3);
  color: #FFCDD2;
}

.turn-player.blue {
  background: rgba(33, 150, 243, 0.3);
  color: #BBDEFB;
}

.turn-timer {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timer-label {
  font-size: 0.9rem;
  opacity: 0.8;
}

.timer-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #FFD700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  font-family: 'Courier New', monospace;
}

.game-info {
  margin: 15px 0;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  backdrop-filter: blur(10px);
}

.game-title {
  font-size: 1.3rem;
  font-weight: bold;
  margin-bottom: 10px;
  color: white;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.player-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.player-count {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
}

.red-player {
  color: #ff6b6b;
  font-weight: bold;
}

.blue-player {
  color: #4ecdc4;
  font-weight: bold;
}

.vs {
  color: white;
  font-weight: bold;
  font-size: 0.9rem;
}

.player-count-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 5px 12px;
  border-radius: 15px;
  font-weight: bold;
  font-size: 0.9rem;
}

.game-status {
  font-size: 1.2rem;
  font-weight: 500;
}

.loading {
  color: #FFD700;
}

.waiting {
  color: #FFA500;
}

.in-progress {
  color: #00FF00;
}

.finished {
  color: #FF6B6B;
}

.game-content {
  display: flex;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  flex: 1;
  align-items: flex-start;
}

.main-game-area {
  flex: 2;
  display: flex;
  justify-content: center;
  align-items: center;
}

.game-sidebar {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  flex-shrink: 0;
}

.game-list, .move-history {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.game-list h3, .move-history h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.2rem;
}

.no-games, .no-moves {
  text-align: center;
  color: #666;
  font-style: italic;
  padding: 20px;
}


.game-list-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.game-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}


.game-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}


.game-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.game-title {
  font-weight: bold;
  color: #333;
  font-size: 1.1rem;
}


.game-details {
  display: flex;
  gap: 10px;
  align-items: center;
}

.host-name {
  font-size: 0.9rem;
  color: #007bff;
  font-weight: 500;
}

.created-time {
  font-size: 0.85rem;
  color: #666;
}


.join-btn {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.3s ease;
}

.join-btn:hover {
  background: #45a049;
}

.move-list {
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.move-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 0.9rem;
}

.dark-mode .move-item {
  background: #424242;
}

.move-number {
  font-weight: 600;
  color: #666;
  min-width: 30px;
}

.dark-mode .move-number {
  color: #aaa;
}

/* 카운트다운 스타일 */
.countdown-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
}

.countdown-container {
  text-align: center;
  color: white;
}

.countdown-number {
  font-size: 8rem;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
  animation: pulse 1s ease-in-out;
}

.countdown-text {
  font-size: 2rem;
  font-weight: 500;
  opacity: 0.9;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.move-details {
  color: #333;
  flex: 1;
}

.dark-mode .move-details {
  color: white;
}

/* 반응형 디자인 */
@media (max-width: 1200px) {
  .game-content {
    flex-direction: column;
    align-items: center;
  }
  
  .game-sidebar {
    width: 100%;
    max-width: 600px;
  }
}

@media (max-width: 768px) {
  .game-view {
    padding: 10px;
  }
  
  .game-header h1 {
    font-size: 2rem;
  }
  
  .game-content {
    gap: 20px;
  }
}
</style>
