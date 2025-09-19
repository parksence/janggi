export interface Piece {
  type: PieceType;
  color: PieceColor;
  row: number;
  col: number;
  isCaptured: boolean;
}

export enum PieceType {
  KING = 'KING',
  GUARD = 'GUARD',
  ELEPHANT = 'ELEPHANT',
  HORSE = 'HORSE',
  CHARIOT = 'CHARIOT',
  CANNON = 'CANNON',
  SOLDIER = 'SOLDIER'
}

export enum PieceColor {
  RED = 'RED',
  BLUE = 'BLUE'
}

export interface Game {
  id: number;
  roomTitle?: string;
  redPlayerName: string;
  bluePlayerName?: string;
  currentTurn: PieceColor;
  gameStatus: GameStatus;
  winnerName?: string;
  createdAt: string;
  updatedAt: string;
  boardState: string;
  moveHistory?: string;
}

export enum GameStatus {
  WAITING = 'WAITING',
  IN_PROGRESS = 'IN_PROGRESS',
  FINISHED = 'FINISHED',
  ABANDONED = 'ABANDONED'
}

export interface Move {
  id: number;
  gameId: number;
  playerName: string;
  fromRow: number;
  fromCol: number;
  toRow: number;
  toCol: number;
  pieceType: PieceType;
  pieceColor: PieceColor;
  capturedPieceType?: string;
  moveNumber: number;
  createdAt: string;
}

export interface GameBoard {
  board: (Piece | null)[][];
  capturedPieces: Piece[];
}

export interface MoveRequest {
  playerName: string;
  fromRow: number;
  fromCol: number;
  toRow: number;
  toCol: number;
}
