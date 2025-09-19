<template>
  <div class="janggi-board-container">
    <div class="game-info">
      <div class="current-turn">
        <span v-if="gameStore.isGameActive">
          현재 턴: {{ getCurrentTurnText() }}
        </span>
        <span v-else-if="gameStore.gameStatus === 'WAITING'">
          게임 대기 중... ({{ getPlayerCount() }}/2)
        </span>
        <span v-else-if="gameStore.gameStatus === 'FINISHED'">
          게임 종료
        </span>
      </div>
      
      <!-- 대기 중일 때 플레이어 정보 표시 -->
      <div v-if="gameStore.gameStatus === 'WAITING'" class="waiting-info">
        <div class="player-list">
          <div class="player-item red-player">
            <span class="player-color">🔴</span>
            <span class="player-name">{{ gameStore.currentGame?.redPlayerName || '대기 중...' }}</span>
          </div>
          <div class="player-item blue-player">
            <span class="player-color">🔵</span>
            <span class="player-name">{{ gameStore.currentGame?.bluePlayerName || '대기 중...' }}</span>
          </div>
        </div>
        <div class="waiting-message">
          <span v-if="getPlayerCount() === 1">다른 플레이어를 기다리고 있습니다...</span>
          <span v-else-if="getPlayerCount() === 2">게임을 시작할 수 있습니다!</span>
        </div>
      </div>
      
      <div class="game-controls">
        <button 
          v-if="gameStore.gameStatus === 'WAITING'"
          @click="startGame"
          :disabled="gameStore.isLoading || getPlayerCount() < 2"
          class="btn-primary"
        >
          {{ getPlayerCount() < 2 ? '플레이어 대기 중...' : '게임 시작' }}
        </button>
        <button 
          @click="exitGame"
          class="btn-secondary"
        >
          게임 나가기
        </button>
      </div>
    </div>

    <div class="board-wrapper">
      <div class="janggi-board">
        <!-- 일반적인 칸 방식으로 말 배치 -->
        <div class="board-grid">
          <div 
            v-for="row in 10" 
            :key="row" 
            class="board-row"
          >
            <div 
              v-for="col in 9" 
              :key="col" 
              class="board-cell"
              :class="getCellClass(row - 1, col - 1)"
              @click="handleCellClick(row - 1, col - 1)"
            >
              <div v-if="getPiece(row - 1, col - 1)" class="piece" :class="getPieceColorClass(row - 1, col - 1)">
                {{ getPieceText(row - 1, col - 1) }}
              </div>
              <div v-if="isSelected(row - 1, col - 1)" class="selected-indicator"></div>
              <div v-if="isValidMove(row - 1, col - 1)" class="valid-move-indicator"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="gameStore.error" class="error-message">
      {{ gameStore.error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useGameStore } from '@/stores/gameStore';
import { useAlert } from '@/composables/useAlert';
import type { Piece, PieceColor } from '@/types/game';

const gameStore = useGameStore();
const { success, error, warning, confirm } = useAlert();

// 말 선택 및 이동 관련 상태
const selectedPiece = ref<{row: number, col: number} | null>(null);
const validMoves = ref<Array<{row: number, col: number}>>([]);
const isMyTurn = computed(() => {
  if (!gameStore.currentGame) return false;
  const playerName = localStorage.getItem('playerName');
  if (!playerName) return false;
  
  const myColor = gameStore.getMyColor();
  if (!myColor) return false;
  
  return gameStore.currentGame.currentTurn === myColor;
});

function getPiece(row: number, col: number): Piece | null {
  return gameStore.gameBoard?.board[row]?.[col] || null;
}

function getPieceText(row: number, col: number): string {
  const piece = getPiece(row, col);
  if (!piece) return '';

  const pieceTexts = {
    KING: { RED: '왕', BLUE: '王' },
    GUARD: { RED: '사', BLUE: '士' },
    ELEPHANT: { RED: '상', BLUE: '象' },
    HORSE: { RED: '마', BLUE: '馬' },
    CHARIOT: { RED: '차', BLUE: '車' },
    CANNON: { RED: '포', BLUE: '包' },
    SOLDIER: { RED: '졸', BLUE: '卒' }
  };

  return pieceTexts[piece.type]?.[piece.color] || '';
}

function getPieceColorClass(row: number, col: number): string {
  const piece = getPiece(row, col);
  if (!piece) return '';
  
  return piece.color === 'RED' ? 'piece-red' : 'piece-blue';
}

// 말 이동 가능 범위 계산
function calculateValidMoves(row: number, col: number): Array<{row: number, col: number}> {
  const piece = getPiece(row, col);
  if (!piece) return [];
  
  const moves: Array<{row: number, col: number}> = [];
  
  switch (piece.type) {
    case 'SOLDIER': // 졸/병
      if (piece.color === 'RED') {
        // 한나라 졸: 앞으로만 이동 가능
        if (row < 9) moves.push({row: row + 1, col});
        // 강을 건넌 후에는 좌우로도 이동 가능
        if (row >= 4) {
          if (col > 0) moves.push({row, col: col - 1});
          if (col < 8) moves.push({row, col: col + 1});
        }
      } else {
        // 초나라 병: 앞으로만 이동 가능
        if (row > 0) moves.push({row: row - 1, col});
        // 강을 건넌 후에는 좌우로도 이동 가능
        if (row <= 5) {
          if (col > 0) moves.push({row, col: col - 1});
          if (col < 8) moves.push({row, col: col + 1});
        }
      }
      break;
      
    case 'KING': // 왕
      // 궁성 내에서만 이동 가능 (3x3 영역)
      const palaceRows = piece.color === 'RED' ? [0, 1, 2] : [7, 8, 9];
      const palaceCols = [3, 4, 5];
      
      for (let dr = -1; dr <= 1; dr++) {
        for (let dc = -1; dc <= 1; dc++) {
          if (dr === 0 && dc === 0) continue;
          const newRow = row + dr;
          const newCol = col + dc;
          
          if (palaceRows.includes(newRow) && palaceCols.includes(newCol)) {
            moves.push({row: newRow, col: newCol});
          }
        }
      }
      break;
      
    case 'GUARD': // 사
      // 궁성 내에서만 대각선으로 이동 가능
      const guardPalaceRows = piece.color === 'RED' ? [0, 1, 2] : [7, 8, 9];
      const guardPalaceCols = [3, 4, 5];
      
      const diagonalMoves = [
        {dr: -1, dc: -1}, {dr: -1, dc: 1},
        {dr: 1, dc: -1}, {dr: 1, dc: 1}
      ];
      
      diagonalMoves.forEach(({dr, dc}) => {
        const newRow = row + dr;
        const newCol = col + dc;
        
        if (guardPalaceRows.includes(newRow) && guardPalaceCols.includes(newCol)) {
          moves.push({row: newRow, col: newCol});
        }
      });
      break;
      
    case 'ELEPHANT': // 상
      // 대각선으로 2칸씩 이동 (궁성 내에서만)
      const elephantPalaceRows = piece.color === 'RED' ? [0, 1, 2] : [7, 8, 9];
      const elephantPalaceCols = [3, 4, 5];
      
      const elephantMoves = [
        {dr: -2, dc: -2}, {dr: -2, dc: 2},
        {dr: 2, dc: -2}, {dr: 2, dc: 2}
      ];
      
      elephantMoves.forEach(({dr, dc}) => {
        const newRow = row + dr;
        const newCol = col + dc;
        
        if (elephantPalaceRows.includes(newRow) && elephantPalaceCols.includes(newCol)) {
          // 중간에 말이 있는지 확인
          const midRow = row + dr / 2;
          const midCol = col + dc / 2;
          if (!getPiece(midRow, midCol)) {
            moves.push({row: newRow, col: newCol});
          }
        }
      });
      break;
      
    case 'HORSE': // 마
      // 일자로 1칸, 대각선으로 1칸 이동
      const horseMoves = [
        {dr: -2, dc: -1}, {dr: -2, dc: 1},
        {dr: -1, dc: -2}, {dr: -1, dc: 2},
        {dr: 1, dc: -2}, {dr: 1, dc: 2},
        {dr: 2, dc: -1}, {dr: 2, dc: 1}
      ];
      
      horseMoves.forEach(({dr, dc}) => {
        const newRow = row + dr;
        const newCol = col + dc;
        
        if (newRow >= 0 && newRow <= 9 && newCol >= 0 && newCol <= 8) {
          // 중간에 말이 있는지 확인
          const midRow = row + (dr > 0 ? 1 : -1) * (Math.abs(dr) > Math.abs(dc) ? 1 : 0);
          const midCol = col + (dc > 0 ? 1 : -1) * (Math.abs(dc) > Math.abs(dr) ? 1 : 0);
          
          if (!getPiece(midRow, midCol)) {
            moves.push({row: newRow, col: newCol});
          }
        }
      });
      break;
      
    case 'CHARIOT': // 차
      // 상하좌우로 직선 이동
      const chariotDirections = [
        {dr: -1, dc: 0}, {dr: 1, dc: 0},
        {dr: 0, dc: -1}, {dr: 0, dc: 1}
      ];
      
      chariotDirections.forEach(({dr, dc}) => {
        for (let i = 1; i <= 9; i++) {
          const newRow = row + dr * i;
          const newCol = col + dc * i;
          
          if (newRow < 0 || newRow > 9 || newCol < 0 || newCol > 8) break;
          
          const targetPiece = getPiece(newRow, newCol);
          if (targetPiece) {
            // 상대방 말이면 잡을 수 있음
            if (targetPiece.color !== piece.color) {
              moves.push({row: newRow, col: newCol});
            }
            break;
          } else {
            moves.push({row: newRow, col: newCol});
          }
        }
      });
      break;
      
    case 'CANNON': // 포
      // 상하좌우로 직선 이동 (다른 말을 뛰어넘어야 함)
      const cannonDirections = [
        {dr: -1, dc: 0}, {dr: 1, dc: 0},
        {dr: 0, dc: -1}, {dr: 0, dc: 1}
      ];
      
      cannonDirections.forEach(({dr, dc}) => {
        let hasJumped = false;
        
        for (let i = 1; i <= 9; i++) {
          const newRow = row + dr * i;
          const newCol = col + dc * i;
          
          if (newRow < 0 || newRow > 9 || newCol < 0 || newCol > 8) break;
          
          const targetPiece = getPiece(newRow, newCol);
          
          if (!hasJumped) {
            if (targetPiece) {
              hasJumped = true;
            } else {
              moves.push({row: newRow, col: newCol});
            }
          } else {
            if (targetPiece) {
              if (targetPiece.color !== piece.color) {
                moves.push({row: newRow, col: newCol});
              }
              break;
            }
          }
        }
      });
      break;
  }
  
  return moves;
}

function getCurrentTurnText(): string {
  if (!gameStore.currentGame) return '';
  return gameStore.currentGame.currentTurn === 'RED' ? '한나라 (빨강)' : '초나라 (파랑)';
}

// 말 선택 처리
function selectPiece(row: number, col: number): void {
  if (!isMyTurn.value) return;
  
  const piece = getPiece(row, col);
  if (!piece) return;
  
  const myColor = gameStore.getMyColor();
  if (!myColor || piece.color !== myColor) return;
  
  selectedPiece.value = {row, col};
  validMoves.value = calculateValidMoves(row, col);
}

// 말 이동 처리
async function movePiece(toRow: number, toCol: number): Promise<void> {
  if (!selectedPiece.value) return;
  
  const isValidMove = validMoves.value.some((move: {row: number, col: number}) => 
    move.row === toRow && move.col === toCol
  );
  
  if (!isValidMove) return;
  
  try {
    await gameStore.makeMove({
      fromRow: selectedPiece.value.row,
      fromCol: selectedPiece.value.col,
      toRow: toRow,
      toCol: toCol,
      playerName: localStorage.getItem('playerName') || ''
    });
    
    // 선택 해제
    selectedPiece.value = null;
    validMoves.value = [];
    
    success('이동 완료', '말을 이동했습니다!');
  } catch (err) {
    console.error('말 이동 실패:', err);
    error('이동 실패', '말 이동에 실패했습니다.');
  }
}

// 칸 클릭 처리
function handleCellClick(row: number, col: number): void {
  if (!isMyTurn.value) return;
  
  const piece = getPiece(row, col);
  
  if (selectedPiece.value) {
    // 이미 말이 선택된 상태
    if (selectedPiece.value.row === row && selectedPiece.value.col === col) {
      // 같은 말을 다시 클릭하면 선택 해제
      selectedPiece.value = null;
      validMoves.value = [];
    } else if (piece && piece.color === gameStore.getMyColor()) {
      // 다른 내 말을 클릭하면 선택 변경
      selectPiece(row, col);
    } else {
      // 이동 시도
      movePiece(row, col);
    }
  } else {
    // 말이 선택되지 않은 상태
    if (piece && piece.color === gameStore.getMyColor()) {
      selectPiece(row, col);
    }
  }
}

// 이동 가능한 위치인지 확인
function isValidMove(row: number, col: number): boolean {
  return validMoves.value.some((move: {row: number, col: number}) => move.row === row && move.col === col);
}

function getPlayerCount(): number {
  if (!gameStore.currentGame) return 0;
  let count = 0;
  // 게임을 만든 사람(redPlayerName)은 항상 있음
  if (gameStore.currentGame.redPlayerName) count++;
  // 두 번째 플레이어(bluePlayerName)가 참가했는지 확인
  if (gameStore.currentGame.bluePlayerName) count++;
  return count;
}

function getCellClass(row: number, col: number): string {
  const classes: string[] = [];
  
  // 경계 체크 (10x9 보드)
  if (row < 0 || row >= 10 || col < 0 || col >= 9) {
    return classes.join(' ');
  }
  
  // 궁성 표시
  if (isInPalace(row, col, 'RED' as PieceColor) || isInPalace(row, col, 'BLUE' as PieceColor)) {
    classes.push('palace');
  }
  
  // 강 표시
  if (row === 4 || row === 5) {
    classes.push('river');
  }
  
  return classes.join(' ');
}

function isSelected(row: number, col: number): boolean {
  if (!selectedPiece.value) return false;
  
  return selectedPiece.value.row === row && 
         selectedPiece.value.col === col;
}

// 이 함수는 위에서 이미 정의됨

// 이 함수는 위에서 이미 정의됨

function isInPalace(row: number, col: number, color: PieceColor): boolean {
  if (color === 'RED') {
    return row >= 0 && row <= 2 && col >= 3 && col <= 5;
  } else {
    return row >= 7 && row <= 9 && col >= 3 && col <= 5;
  }
}

async function makeMove(toRow: number, toCol: number): Promise<void> {
  if (!gameStore.selectedPiece) return;

  try {
    const playerName = localStorage.getItem('playerName');
    if (!playerName) {
      console.error('플레이어 이름이 없습니다.');
      return;
    }

    await gameStore.makeMove({
      playerName,
      fromRow: gameStore.selectedPiece.row,
      fromCol: gameStore.selectedPiece.col,
      toRow,
      toCol
    });
  } catch (err) {
    console.error('이동 실패:', error);
  }
}

async function startGame(): Promise<void> {
  try {
    if (getPlayerCount() < 2) {
      await warning(
        '게임 시작 불가',
        '게임을 시작하려면 2명의 플레이어가 필요합니다.'
      );
      return;
    }
    
    // 게임 상태를 IN_PROGRESS로 변경하는 API 호출
    // 현재는 게임이 이미 시작된 상태이므로 별도 처리 불필요
    await success(
      '게임 시작!',
      '게임이 시작되었습니다. 즐거운 대국 되세요! 🎮'
    );
  } catch (err) {
    console.error('게임 시작 실패:', error);
    await error(
      '게임 시작 실패',
      '게임을 시작하는 중 오류가 발생했습니다.'
    );
  }
}

async function exitGame(): Promise<void> {
  try {
    const playerName = localStorage.getItem('playerName');
    if (!playerName) {
      await error(
        '플레이어 정보 없음',
        '플레이어 이름이 없습니다. 홈으로 돌아갑니다.'
      );
      window.location.href = '/';
      return;
    }
    
    // 게임 나가기 확인 - 더 예쁜 알럿
    const confirmed = await confirm(
      '게임 나가기',
      '정말로 게임에서 나가시겠습니까?\n\n나가면 다른 플레이어가 기다리게 됩니다.'
    );
    
    if (!confirmed) {
      return;
    }
    
    if (gameStore.currentGame?.id) {
      // 백엔드에 게임 나가기 요청
      await fetch(`http://localhost:8080/api/game/${gameStore.currentGame.id}/leave`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ playerName })
      });
    }
    
    // 성공 알럿 없이 바로 홈으로 이동
    gameStore.resetGame();
    window.location.href = '/';
  } catch (err) {
    console.error('게임 나가기 실패:', error);
    await error(
      '게임 나가기 실패',
      '게임에서 나가는 중 오류가 발생했습니다. 홈으로 돌아갑니다.'
    );
    // 오류가 발생해도 홈으로 돌아가기
    gameStore.resetGame();
    window.location.href = '/';
  }
}

onMounted(async () => {
  // 컴포넌트 마운트 시 필요한 초기화 작업
  console.log('JanggiBoard mounted');
  console.log('Current game:', gameStore.currentGame);
  console.log('Game board:', gameStore.gameBoard);
  console.log('Game status:', gameStore.gameStatus);
  
  // 현재 게임이 있지만 게임 보드가 없는 경우 로드 시도
  if (gameStore.currentGame && !gameStore.gameBoard) {
    console.log('Loading game board for current game...');
    await gameStore.loadGameBoard();
  }
});
</script>

<style scoped>
.janggi-board-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.game-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 600px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}


.current-turn {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.waiting-info {
  margin: 15px 0;
  padding: 15px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
  border: 1px solid #ddd;
}

.player-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
}

.player-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.5);
}

.player-color {
  font-size: 16px;
}

.player-name {
  font-weight: 500;
  color: #333;
}

.waiting-message {
  text-align: center;
  font-size: 14px;
  color: #666;
  font-style: italic;
}


.game-controls {
  display: flex;
  gap: 10px;
}

.btn-primary, .btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #4CAF50;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #45a049;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f44336;
  color: white;
}

.btn-secondary:hover {
  background: #da190b;
}

.board-wrapper {
  background: #8B4513;
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.janggi-board {
  display: grid;
  grid-template-rows: repeat(10, 1fr);
  gap: 0;
  background: linear-gradient(135deg, #F4E4BC 0%, #E6D3A3 50%, #D4C4A8 100%);
  border: 6px solid #8B4513;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 2px 4px rgba(255, 255, 255, 0.2),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1);
  width: 540px;
  height: 600px;
  position: relative;
  background-image: 
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.1) 1px, transparent 1px),
    radial-gradient(circle at 80% 80%, rgba(0, 0, 0, 0.05) 1px, transparent 1px);
  background-size: 30px 30px, 25px 25px;
}

/* 보드 그리드 스타일 */
.board-grid {
  display: grid;
  grid-template-rows: repeat(10, 1fr);
  gap: 0;
  width: 100%;
  height: 100%;
}

.board-row {
  display: grid;
  grid-template-columns: repeat(9, 1fr);
  gap: 0;
}

.board-cell {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #FFF8DC 0%, #F5DEB3 100%);
  border: 1px solid #CD853F;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 24px;
  font-weight: bold;
  color: #8B4513;
  box-shadow: 
    inset 0 1px 2px rgba(255, 255, 255, 0.3),
    inset 0 -1px 2px rgba(0, 0, 0, 0.1);
}

.board-cell:hover {
  background: #FFE4B5;
  transform: scale(1.05);
}

.board-cell.palace {
  background: linear-gradient(135deg, #FFE4B5 0%, #F0E68C 50%, #E6D3A3 100%);
  border: 2px solid #B8860B;
  position: relative;
  box-shadow: 
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1),
    0 0 8px rgba(184, 134, 11, 0.2);
}

.board-cell.palace::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px solid #B8860B;
  border-radius: 2px;
  opacity: 0.4;
  background: 
    linear-gradient(45deg, transparent 45%, #B8860B 45%, #B8860B 55%, transparent 55%),
    linear-gradient(-45deg, transparent 45%, #B8860B 45%, #B8860B 55%, transparent 55%);
}

.board-cell.river {
  background: linear-gradient(90deg, #E0F6FF 0%, #B0E0E6 50%, #87CEEB 100%);
  border: 2px solid #4682B4;
  position: relative;
  box-shadow: 
    inset 0 2px 4px rgba(255, 255, 255, 0.3),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1);
}

.board-cell.river::before {
  content: '한';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 18px;
  font-weight: bold;
  color: #2E8B57;
  opacity: 0.7;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

/* 선택된 말 표시 */
.selected-indicator {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 4px solid #FFD700;
  border-radius: 8px;
  box-shadow: 
    0 0 0 2px rgba(255, 215, 0, 0.3),
    inset 0 0 0 2px rgba(255, 215, 0, 0.2);
  animation: pulse 1.5s ease-in-out infinite;
  pointer-events: none;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.05);
  }
}

/* 이동 가능한 위치 표시 */
.valid-move-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  background: radial-gradient(circle, rgba(0, 255, 0, 0.8) 0%, rgba(0, 255, 0, 0.3) 70%, transparent 100%);
  border-radius: 50%;
  border: 2px solid rgba(0, 255, 0, 0.9);
  box-shadow: 
    0 0 10px rgba(0, 255, 0, 0.5),
    inset 0 0 5px rgba(255, 255, 255, 0.3);
  animation: glow 2s ease-in-out infinite;
  pointer-events: none;
}

@keyframes glow {
  0%, 100% {
    opacity: 0.8;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

/* 말이 있는 칸에 이동 가능 표시가 있을 때 */
.board-cell:has(.piece) .valid-move-indicator {
  background: radial-gradient(circle, rgba(255, 0, 0, 0.8) 0%, rgba(255, 0, 0, 0.3) 70%, transparent 100%);
  border-color: rgba(255, 0, 0, 0.9);
  box-shadow: 
    0 0 10px rgba(255, 0, 0, 0.5),
    inset 0 0 5px rgba(255, 255, 255, 0.3);
}

.intersection-point {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.intersection-point::before {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  background: #8B4513;
  border-radius: 50%;
  box-shadow: 
    0 0 0 2px rgba(255, 255, 255, 0.3),
    0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

/* 말이 있는 꼭지점에서는 점 숨기기 */
.intersection-point:has(.piece)::before {
  display: none;
}

.intersection-point:hover::before {
  transform: scale(1.3);
  background: #A0522D;
  box-shadow: 
    0 0 0 3px rgba(255, 255, 255, 0.5),
    0 4px 8px rgba(0, 0, 0, 0.3);
}

.intersection-point:hover {
  background: rgba(255, 255, 255, 0.05);
}

.intersection-point.palace {
  background: rgba(255, 215, 0, 0.1);
}

.intersection-point.river {
  background: rgba(0, 191, 255, 0.1);
}

/* 기존 보드 스타일 (참고용) */
.board-row {
  display: grid;
  grid-template-columns: repeat(9, 1fr);
  gap: 0;
}

.board-cell {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #FFF8DC 0%, #F5DEB3 100%); /* 밝은 나무 색상 */
  border: 1px solid #CD853F; /* 중간 톤 테두리 */
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 24px; /* 말 크기 조금 더 크게 */
  font-weight: bold;
  color: #8B4513;
  /* 실제 장기판처럼 약간의 입체감 */
  box-shadow: 
    inset 0 1px 2px rgba(255, 255, 255, 0.3),
    inset 0 -1px 2px rgba(0, 0, 0, 0.1);
}

.board-cell:hover {
  background: #FFE4B5;
  transform: scale(1.05);
}

.board-cell.palace {
  background: linear-gradient(135deg, #FFE4B5 0%, #F0E68C 50%, #E6D3A3 100%); /* 궁성 특별 색상 */
  border: 2px solid #B8860B; /* 금색 테두리 */
  position: relative;
  /* 궁성 특별 효과 */
  box-shadow: 
    inset 0 2px 4px rgba(255, 255, 255, 0.4),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1),
    0 0 8px rgba(184, 134, 11, 0.2);
}

.board-cell.palace::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 36px;
  border: 2px solid #B8860B;
  border-radius: 2px;
  opacity: 0.4;
  /* 궁성 내부 대각선 */
  background: 
    linear-gradient(45deg, transparent 45%, #B8860B 45%, #B8860B 55%, transparent 55%),
    linear-gradient(-45deg, transparent 45%, #B8860B 45%, #B8860B 55%, transparent 55%);
}

.board-cell.river {
  background: linear-gradient(90deg, #E0F6FF 0%, #B0E0E6 50%, #87CEEB 100%); /* 강물 색상 */
  border: 2px solid #4682B4;
  position: relative;
  /* 강물 효과 */
  box-shadow: 
    inset 0 2px 4px rgba(255, 255, 255, 0.3),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1);
}

.board-cell.river::before {
  content: '한';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 18px;
  font-weight: bold;
  color: #2E8B57; /* 진한 녹색 */
  opacity: 0.7;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

.piece {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFF8DC 0%, #F5DEB3 30%, #E6D3A3 70%, #D4C4A8 100%);
  border: 3px solid #8B4513;
  box-shadow: 
    0 8px 16px rgba(0, 0, 0, 0.3),
    0 4px 8px rgba(0, 0, 0, 0.2),
    inset 0 3px 6px rgba(255, 255, 255, 0.5),
    inset 0 -3px 6px rgba(0, 0, 0, 0.15);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 20px;
  font-weight: 700;
  color: #8B4513;
  position: relative;
  z-index: 10;
  cursor: pointer;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  /* 고급 나무 질감 */
  background-image: 
    radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.2) 1px, transparent 1px),
    radial-gradient(circle at 70% 70%, rgba(0, 0, 0, 0.1) 1px, transparent 1px);
  background-size: 15px 15px, 20px 20px;
}

.piece:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.4);
}

/* 한나라(빨간색) 말 스타일 */
.piece-red {
  background: linear-gradient(135deg, #FFE4E1 0%, #FFB3BA 30%, #FF8A95 70%, #FF6B7A 100%);
  border: 3px solid #D32F2F;
  color: #B71C1C;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.9);
}

.piece-red:hover {
  background: linear-gradient(135deg, #FFE4E1 0%, #FFB3BA 30%, #FF8A95 70%, #FF6B7A 100%);
  border-color: #C62828;
  box-shadow: 
    0 8px 16px rgba(211, 47, 47, 0.4),
    0 4px 8px rgba(0, 0, 0, 0.2),
    inset 0 3px 6px rgba(255, 255, 255, 0.6),
    inset 0 -3px 6px rgba(0, 0, 0, 0.1);
}

/* 초나라(파란색) 말 스타일 */
.piece-blue {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 30%, #90CAF9 70%, #64B5F6 100%);
  border: 3px solid #1976D2;
  color: #0D47A1;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.9);
}

.piece-blue:hover {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 30%, #90CAF9 70%, #64B5F6 100%);
  border-color: #1565C0;
  box-shadow: 
    0 8px 16px rgba(25, 118, 210, 0.4),
    0 4px 8px rgba(0, 0, 0, 0.2),
    inset 0 3px 6px rgba(255, 255, 255, 0.6),
    inset 0 -3px 6px rgba(0, 0, 0, 0.1);
}

.selected-indicator {
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  border: 3px solid #FFD700; /* 금색 테두리 */
  border-radius: 50%;
  pointer-events: none;
  animation: pulse 1.5s infinite;
  box-shadow: 
    0 0 10px rgba(255, 215, 0, 0.6),
    inset 0 0 10px rgba(255, 215, 0, 0.2);
}

.valid-move-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  background: rgba(76, 175, 80, 0.7);
  border-radius: 50%;
  pointer-events: none;
  animation: fadeInOut 1s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

@keyframes fadeInOut {
  0% { opacity: 0.3; }
  50% { opacity: 0.8; }
  100% { opacity: 0.3; }
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 10px 20px;
  border-radius: 6px;
  border: 1px solid #ffcdd2;
  max-width: 600px;
  text-align: center;
}

</style>
