import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Game, GameBoard, Move, MoveRequest, Piece, PieceColor } from '@/types/game';
import { GameStatus } from '@/types/game';
import axios from '@/plugins/axios';

export const useGameStore = defineStore('game', () => {
  const currentGame = ref<Game | null>(null);
  const gameBoard = ref<GameBoard | null>(null);
  const selectedPiece = ref<{ row: number; col: number } | null>(null);
  const validMoves = ref<{ row: number; col: number }[]>([]);
  const moveHistory = ref<Move[]>([]);
  const waitingGames = ref<Game[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  const isMyTurn = computed(() => {
    if (!currentGame.value) return false;
    const myColor = getMyColor();
    return currentGame.value.currentTurn === myColor;
  });

  const gameStatus = computed(() => {
    return currentGame.value?.gameStatus || GameStatus.WAITING;
  });

  const isGameActive = computed(() => {
    return gameStatus.value === GameStatus.IN_PROGRESS;
  });

  function getMyColor(): PieceColor | null {
    if (!currentGame.value) return null;
    
    const playerName = localStorage.getItem('playerName');
    if (!playerName) return null;
    
    // 현재 플레이어가 빨간색인지 파란색인지 확인
    if (currentGame.value.redPlayerName === playerName) {
      return 'RED' as PieceColor;
    } else if (currentGame.value.bluePlayerName === playerName) {
      return 'BLUE' as PieceColor;
    }
    
    return null;
  }

  async function createGame(playerName: string, roomTitle: string): Promise<void> {
    try {
      isLoading.value = true;
      error.value = null;
      const response = await axios.post('/api/game/create', { playerName, roomTitle });
      currentGame.value = response.data;
      await loadGameBoard();
    } catch (err: any) {
      error.value = err.response?.data?.message || '게임 생성에 실패했습니다.';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function joinGame(gameId: number, playerName: string): Promise<void> {
    try {
      isLoading.value = true;
      error.value = null;
      const response = await axios.post(`/api/game/${gameId}/join`, { playerName });
      currentGame.value = response.data;
      await loadGameBoard();
    } catch (err: any) {
      error.value = err.response?.data?.message || '게임 참여에 실패했습니다.';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function loadGame(gameId: number): Promise<void> {
    try {
      isLoading.value = true;
      error.value = null;
      const response = await axios.get(`/api/game/${gameId}`);
      currentGame.value = response.data;
      await loadGameBoard();
      await loadMoveHistory(gameId);
    } catch (err: any) {
      error.value = err.response?.data?.message || '게임 로드에 실패했습니다.';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function loadGameBoard(): Promise<void> {
    if (!currentGame.value?.boardState) return;
    
    try {
      gameBoard.value = JSON.parse(currentGame.value.boardState);
    } catch (err) {
      console.error('보드 상태 파싱 실패:', err);
      error.value = '보드 상태를 불러올 수 없습니다.';
    }
  }

  async function loadMoveHistory(gameId: number): Promise<void> {
    try {
      const response = await axios.get(`/api/game/${gameId}/history`);
      moveHistory.value = response.data;
    } catch (err) {
      console.error('이동 기록 로드 실패:', err);
    }
  }

  async function loadWaitingGames(): Promise<void> {
    try {
      const response = await axios.get('/api/game/waiting');
      waitingGames.value = response.data;
    } catch (err) {
      console.error('대기 중인 게임 로드 실패:', err);
    }
  }

  async function loadCurrentGame(): Promise<void> {
    try {
      const playerName = localStorage.getItem('playerName');
      if (!playerName) return;
      
      isLoading.value = true;
      error.value = null;
      const response = await axios.get(`/api/game/active/${playerName}`);
      if (response.data) {
        currentGame.value = response.data;
        await loadGameBoard();
        await loadMoveHistory(currentGame.value.id);
      }
    } catch (err: any) {
      // 활성 게임이 없는 경우는 에러가 아님
      if (err.response?.status !== 404) {
        error.value = err.response?.data?.message || '현재 게임을 불러오는데 실패했습니다.';
      }
    } finally {
      isLoading.value = false;
    }
  }

  async function makeMove(moveRequest: MoveRequest): Promise<void> {
    if (!currentGame.value) return;

    try {
      isLoading.value = true;
      error.value = null;
      const response = await axios.post(`/api/game/${currentGame.value.id}/move`, moveRequest);
      currentGame.value = response.data;
      await loadGameBoard();
      await loadMoveHistory(currentGame.value.id);
      
      // 선택된 말과 유효한 이동 초기화
      selectedPiece.value = null;
      validMoves.value = [];
    } catch (err: any) {
      error.value = err.response?.data?.message || '이동에 실패했습니다.';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  function selectPiece(row: number, col: number): void {
    if (!gameBoard.value || !isMyTurn.value) return;

    const piece = gameBoard.value.board[row][col];
    if (!piece || piece.color !== getMyColor()) return;

    selectedPiece.value = { row, col };
    // TODO: 서버에서 유효한 이동 목록을 가져와야 함
    // 임시로 빈 배열로 설정
    validMoves.value = [];
  }

  function deselectPiece(): void {
    selectedPiece.value = null;
    validMoves.value = [];
  }

  function isSelected(row: number, col: number): boolean {
    return selectedPiece.value?.row === row && selectedPiece.value?.col === col;
  }

  function isValidMove(row: number, col: number): boolean {
    return validMoves.value.some(move => move.row === row && move.col === col);
  }

  function canMoveTo(row: number, col: number): boolean {
    if (!selectedPiece.value || !isMyTurn.value) return false;
    
    const piece = gameBoard.value?.board[selectedPiece.value.row][selectedPiece.value.col];
    if (!piece) return false;

    const targetPiece = gameBoard.value?.board[row][col];
    if (targetPiece && targetPiece.color === piece.color) return false;

    // TODO: 실제 이동 규칙 검증 로직 구현
    return true;
  }

  function setCurrentGame(game: Game): void {
    currentGame.value = game;
  }

  function resetGame(): void {
    currentGame.value = null;
    gameBoard.value = null;
    selectedPiece.value = null;
    validMoves.value = [];
    moveHistory.value = [];
    error.value = null;
  }

  return {
    // State
    currentGame,
    gameBoard,
    selectedPiece,
    validMoves,
    moveHistory,
    waitingGames,
    isLoading,
    error,
    
    // Computed
    isMyTurn,
    gameStatus,
    isGameActive,
    
    // Actions
    createGame,
    joinGame,
    loadGame,
    loadGameBoard,
    loadMoveHistory,
    loadWaitingGames,
    loadCurrentGame,
    setCurrentGame,
    makeMove,
    selectPiece,
    deselectPiece,
    isSelected,
    isValidMove,
    canMoveTo,
    getMyColor,
    resetGame
  };
});
