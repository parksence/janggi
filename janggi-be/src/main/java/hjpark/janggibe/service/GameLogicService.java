package hjpark.janggibe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hjpark.janggibe.model.GameBoard;
import hjpark.janggibe.model.Piece;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameLogicService {
    
    private final ObjectMapper objectMapper;
    
    public String serializeBoard(GameBoard board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            log.error("보드 직렬화 실패", e);
            throw new RuntimeException("보드 상태 저장 실패");
        }
    }
    
    public GameBoard deserializeBoard(String boardState) {
        try {
            return objectMapper.readValue(boardState, GameBoard.class);
        } catch (JsonProcessingException e) {
            log.error("보드 역직렬화 실패", e);
            throw new RuntimeException("보드 상태 복원 실패");
        }
    }
    
    public boolean isValidMove(GameBoard board, int fromRow, int fromCol, int toRow, int toCol, Piece.PieceColor playerColor) {
        Piece piece = board.getPiece(fromRow, fromCol);
        
        if (piece == null || piece.getColor() != playerColor) {
            return false;
        }
        
        Piece targetPiece = board.getPiece(toRow, toCol);
        if (targetPiece != null && targetPiece.getColor() == playerColor) {
            return false; // 같은 색 말을 잡을 수 없음
        }
        
        return isValidMoveForPiece(board, piece, fromRow, fromCol, toRow, toCol);
    }
    
    private boolean isValidMoveForPiece(GameBoard board, Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        switch (piece.getType()) {
            case KING:
                return isValidKingMove(board, fromRow, fromCol, toRow, toCol, piece.getColor());
            case GUARD:
                return isValidGuardMove(board, fromRow, fromCol, toRow, toCol, piece.getColor());
            case ELEPHANT:
                return isValidElephantMove(board, fromRow, fromCol, toRow, toCol, piece.getColor());
            case HORSE:
                return isValidHorseMove(board, fromRow, fromCol, toRow, toCol);
            case CHARIOT:
                return isValidChariotMove(board, fromRow, fromCol, toRow, toCol);
            case CANNON:
                return isValidCannonMove(board, fromRow, fromCol, toRow, toCol);
            case SOLDIER:
                return isValidSoldierMove(board, fromRow, fromCol, toRow, toCol, piece.getColor());
            default:
                return false;
        }
    }
    
    private boolean isValidKingMove(GameBoard board, int fromRow, int fromCol, int toRow, int toCol, Piece.PieceColor color) {
        // 왕은 궁성 내에서만 이동 가능
        if (!isInPalace(toRow, toCol, color)) {
            return false;
        }
        
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        // 대각선 또는 직선으로 한 칸만 이동 가능
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1) || 
               (rowDiff == 1 && colDiff == 1);
    }
    
    private boolean isValidGuardMove(GameBoard board, int fromRow, int fromCol, int toRow, int toCol, Piece.PieceColor color) {
        // 사는 궁성 내에서만 이동 가능
        if (!isInPalace(toRow, toCol, color)) {
            return false;
        }
        
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        // 대각선으로만 이동 가능
        return rowDiff == 1 && colDiff == 1;
    }
    
    private boolean isValidElephantMove(GameBoard board, int fromRow, int fromCol, int toRow, int toCol, Piece.PieceColor color) {
        // 상은 강을 건너갈 수 없음
        if (color == Piece.PieceColor.RED && toRow > 4) {
            return false;
        }
        if (color == Piece.PieceColor.BLUE && toRow < 5) {
            return false;
        }
        
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        // 대각선으로 두 칸씩 이동
        if (rowDiff == 2 && colDiff == 2) {
            // 중간에 말이 있으면 이동 불가
            int midRow = (fromRow + toRow) / 2;
            int midCol = (fromCol + toCol) / 2;
            return board.getPiece(midRow, midCol) == null;
        }
        
        return false;
    }
    
    private boolean isValidHorseMove(GameBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        // 말의 이동 패턴: 2-1 또는 1-2
        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
            // 막힌 방향 확인
            int blockRow = fromRow;
            int blockCol = fromCol;
            
            if (rowDiff == 2) {
                blockRow = fromRow + (toRow - fromRow) / 2;
            } else {
                blockCol = fromCol + (toCol - fromCol) / 2;
            }
            
            return board.getPiece(blockRow, blockCol) == null;
        }
        
        return false;
    }
    
    private boolean isValidChariotMove(GameBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        // 차는 직선으로만 이동
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }
        
        // 경로에 다른 말이 있는지 확인
        return isPathClear(board, fromRow, fromCol, toRow, toCol);
    }
    
    private boolean isValidCannonMove(GameBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        // 포는 직선으로만 이동
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }
        
        // 목표 지점에 말이 있는지 확인
        Piece targetPiece = board.getPiece(toRow, toCol);
        if (targetPiece == null) {
            // 목표 지점이 비어있으면 차와 같은 이동
            return isPathClear(board, fromRow, fromCol, toRow, toCol);
        } else {
            // 목표 지점에 말이 있으면 포를 넘어서 이동
            return isPathClearWithOneJump(board, fromRow, fromCol, toRow, toCol);
        }
    }
    
    private boolean isValidSoldierMove(GameBoard board, int fromRow, int fromCol, int toRow, int toCol, Piece.PieceColor color) {
        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);
        
        // 졸/병은 한 칸씩만 이동
        if (Math.abs(rowDiff) > 1 || colDiff > 1) {
            return false;
        }
        
        // 강을 건넌 후에는 좌우로도 이동 가능
        boolean crossedRiver = (color == Piece.PieceColor.RED && fromRow > 4) || 
                              (color == Piece.PieceColor.BLUE && fromRow < 5);
        
        if (crossedRiver) {
            // 강을 건넌 후: 앞, 좌, 우로 이동 가능
            return (rowDiff == 0 && colDiff == 1) || 
                   (color == Piece.PieceColor.RED && rowDiff == 1) ||
                   (color == Piece.PieceColor.BLUE && rowDiff == -1);
        } else {
            // 강을 건너기 전: 앞으로만 이동 가능
            return colDiff == 0 && 
                   ((color == Piece.PieceColor.RED && rowDiff == 1) ||
                    (color == Piece.PieceColor.BLUE && rowDiff == -1));
        }
    }
    
    private boolean isInPalace(int row, int col, Piece.PieceColor color) {
        if (color == Piece.PieceColor.RED) {
            // 한나라 궁성 (0-2행, 3-5열)
            return row >= 0 && row <= 2 && col >= 3 && col <= 5;
        } else {
            // 초나라 궁성 (7-9행, 3-5열)
            return row >= 7 && row <= 9 && col >= 3 && col <= 5;
        }
    }
    
    private boolean isPathClear(GameBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        
        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;
        
        while (currentRow != toRow || currentCol != toCol) {
            if (board.getPiece(currentRow, currentCol) != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return true;
    }
    
    private boolean isPathClearWithOneJump(GameBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        
        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;
        boolean foundJump = false;
        
        while (currentRow != toRow || currentCol != toCol) {
            Piece piece = board.getPiece(currentRow, currentCol);
            if (piece != null) {
                if (foundJump) {
                    return false; // 두 번째 말을 만남
                }
                foundJump = true;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return foundJump; // 정확히 하나의 말을 넘어야 함
    }
    
    public boolean isGameOver(GameBoard board, Piece.PieceColor lastMoveColor) {
        // 왕이 잡혔는지 확인
        boolean redKingExists = false;
        boolean blueKingExists = false;
        
        for (int i = 0; i < GameBoard.BOARD_ROWS; i++) {
            for (int j = 0; j < GameBoard.BOARD_COLS; j++) {
                Piece piece = board.getPiece(i, j);
                if (piece != null && piece.getType() == Piece.PieceType.KING) {
                    if (piece.getColor() == Piece.PieceColor.RED) {
                        redKingExists = true;
                    } else {
                        blueKingExists = true;
                    }
                }
            }
        }
        
        return !redKingExists || !blueKingExists;
    }
    
    public List<int[]> getValidMoves(GameBoard board, int row, int col) {
        List<int[]> validMoves = new ArrayList<>();
        Piece piece = board.getPiece(row, col);
        
        if (piece == null) {
            return validMoves;
        }
        
        for (int i = 0; i < GameBoard.BOARD_ROWS; i++) {
            for (int j = 0; j < GameBoard.BOARD_COLS; j++) {
                if (isValidMove(board, row, col, i, j, piece.getColor())) {
                    validMoves.add(new int[]{i, j});
                }
            }
        }
        
        return validMoves;
    }
}
