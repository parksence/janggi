package hjpark.janggibe.service;

import hjpark.janggibe.model.*;
import hjpark.janggibe.repository.GameRepository;
import hjpark.janggibe.repository.MoveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final GameLogicService gameLogicService;
    
    public Game createGame(String playerName, String roomTitle) {
        Game game = Game.builder()
                .roomTitle(roomTitle)
                .redPlayerName(playerName)
                .currentTurn(Piece.PieceColor.RED)
                .gameStatus(Game.GameStatus.WAITING)
                .build();
        
        // 초기 보드 상태 설정
        GameBoard initialBoard = new GameBoard();
        initialBoard.initializeBoard(); // 수동으로 초기화 호출
        game.setBoardState(gameLogicService.serializeBoard(initialBoard));
        
        return gameRepository.save(game);
    }
    
    public Game joinGame(Long gameId, String playerName) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));
        
        if (game.getGameStatus() != Game.GameStatus.WAITING) {
            throw new RuntimeException("이미 시작된 게임입니다.");
        }
        
        // redPlayerName이 null인 경우 체크
        if (game.getRedPlayerName() == null) {
            throw new RuntimeException("게임 호스트 정보가 없습니다.");
        }
        
        if (game.getRedPlayerName().equals(playerName)) {
            throw new RuntimeException("자신이 만든 게임에는 참여할 수 없습니다.");
        }
        
        // 이미 bluePlayerName이 있는 경우 체크
        if (game.getBluePlayerName() != null) {
            throw new RuntimeException("이미 다른 플레이어가 참여했습니다.");
        }
        
        game.setBluePlayerName(playerName);
        game.setGameStatus(Game.GameStatus.IN_PROGRESS);
        
        // 게임 시작 시 보드 상태가 없으면 초기화
        if (game.getBoardState() == null || game.getBoardState().isEmpty()) {
            GameBoard initialBoard = new GameBoard();
            initialBoard.initializeBoard();
            game.setBoardState(gameLogicService.serializeBoard(initialBoard));
        }
        
        return gameRepository.save(game);
    }
    
    public Game makeMove(Long gameId, String playerName, int fromRow, int fromCol, int toRow, int toCol) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));
        
        if (game.getGameStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new RuntimeException("진행 중인 게임이 아닙니다.");
        }
        
        // 현재 턴 확인
        Piece.PieceColor playerColor = getPlayerColor(game, playerName);
        if (playerColor != game.getCurrentTurn()) {
            throw new RuntimeException("당신의 턴이 아닙니다.");
        }
        
        // 보드 상태 복원
        GameBoard board = gameLogicService.deserializeBoard(game.getBoardState());
        
        // 이동 유효성 검사
        if (!gameLogicService.isValidMove(board, fromRow, fromCol, toRow, toCol, playerColor)) {
            throw new RuntimeException("유효하지 않은 이동입니다.");
        }
        
        // 이동 실행
        Piece piece = board.getPiece(fromRow, fromCol);
        Piece capturedPiece = board.getPiece(toRow, toCol);
        
        board.movePiece(fromRow, fromCol, toRow, toCol);
        
        // 이동 기록 저장
        Move move = Move.builder()
                .gameId(gameId)
                .playerName(playerName)
                .fromRow(fromRow)
                .fromCol(fromCol)
                .toRow(toRow)
                .toCol(toCol)
                .pieceType(piece.getType())
                .pieceColor(piece.getColor())
                .capturedPieceType(capturedPiece != null ? capturedPiece.getType().name() : null)
                .moveNumber(moveRepository.countMovesByGameId(gameId) + 1)
                .build();
        
        moveRepository.save(move);
        
        // 게임 상태 업데이트
        game.setBoardState(gameLogicService.serializeBoard(board));
        game.setCurrentTurn(game.getCurrentTurn() == Piece.PieceColor.RED ? 
                           Piece.PieceColor.BLUE : Piece.PieceColor.RED);
        
        // 게임 종료 조건 확인
        if (gameLogicService.isGameOver(board, playerColor)) {
            game.setGameStatus(Game.GameStatus.FINISHED);
            game.setWinnerName(playerName);
        }
        
        return gameRepository.save(game);
    }
    
    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));
    }
    
    public List<Game> getWaitingGames() {
        return gameRepository.findWaitingGames();
    }
    
    public Optional<Game> getActiveGameByPlayerName(String playerName) {
        return gameRepository.findActiveGameByPlayerName(playerName);
    }
    
    public List<Move> getGameHistory(Long gameId) {
        return moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
    }
    
    public void leaveGame(Long gameId, String playerName) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("게임을 찾을 수 없습니다."));
        
        // 플레이어가 게임에 참여했는지 확인
        boolean isRedPlayer = game.getRedPlayerName() != null && game.getRedPlayerName().equals(playerName);
        boolean isBluePlayer = game.getBluePlayerName() != null && game.getBluePlayerName().equals(playerName);
        
        if (!isRedPlayer && !isBluePlayer) {
            throw new RuntimeException("이 게임의 플레이어가 아닙니다.");
        }
        
        if (isRedPlayer) {
            // 호스트가 나가는 경우
            if (game.getBluePlayerName() != null) {
                // 두 번째 플레이어가 있으면 호스트를 두 번째 플레이어로 변경
                game.setRedPlayerName(game.getBluePlayerName());
                game.setBluePlayerName(null);
            } else {
                // 혼자만 있으면 게임 삭제
                gameRepository.delete(game);
                return;
            }
        } else if (isBluePlayer) {
            // 두 번째 플레이어가 나가는 경우
            game.setBluePlayerName(null);
            game.setGameStatus(Game.GameStatus.WAITING);
        }
        
        gameRepository.save(game);
    }
    
    private Piece.PieceColor getPlayerColor(Game game, String playerName) {
        if (game.getRedPlayerName() != null && game.getRedPlayerName().equals(playerName)) {
            return Piece.PieceColor.RED;
        } else if (game.getBluePlayerName() != null && game.getBluePlayerName().equals(playerName)) {
            return Piece.PieceColor.BLUE;
        } else {
            throw new RuntimeException("이 게임의 플레이어가 아닙니다.");
        }
    }
}
