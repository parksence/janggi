package hjpark.janggibe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameBoard {
    public static final int BOARD_ROWS = 10;
    public static final int BOARD_COLS = 9;
    
    @Builder.Default
    private Piece[][] board = new Piece[BOARD_ROWS][BOARD_COLS];
    
    @Builder.Default
    private List<Piece> capturedPieces = new ArrayList<>();
    
    @PostConstruct
    public void initializeBoard() {
        // 초기 보드 설정
        board = new Piece[BOARD_ROWS][BOARD_COLS];
        
        // 한나라 (상단) 말 배치
        // 차, 마, 상, 사, 왕, 사, 상, 마, 차
        board[0][0] = Piece.builder().type(Piece.PieceType.CHARIOT).color(Piece.PieceColor.RED).row(0).col(0).build();
        board[0][1] = Piece.builder().type(Piece.PieceType.HORSE).color(Piece.PieceColor.RED).row(0).col(1).build();
        board[0][2] = Piece.builder().type(Piece.PieceType.ELEPHANT).color(Piece.PieceColor.RED).row(0).col(2).build();
        board[0][3] = Piece.builder().type(Piece.PieceType.GUARD).color(Piece.PieceColor.RED).row(0).col(3).build();
        board[0][5] = Piece.builder().type(Piece.PieceType.GUARD).color(Piece.PieceColor.RED).row(0).col(5).build();
        board[0][6] = Piece.builder().type(Piece.PieceType.ELEPHANT).color(Piece.PieceColor.RED).row(0).col(6).build();
        board[0][7] = Piece.builder().type(Piece.PieceType.HORSE).color(Piece.PieceColor.RED).row(0).col(7).build();
        board[0][8] = Piece.builder().type(Piece.PieceType.CHARIOT).color(Piece.PieceColor.RED).row(0).col(8).build();
        
        // 왕은 사보다 한 칸 앞에 배치
        board[1][4] = Piece.builder().type(Piece.PieceType.KING).color(Piece.PieceColor.RED).row(1).col(4).build();
        
        // 포 배치
        board[2][1] = Piece.builder().type(Piece.PieceType.CANNON).color(Piece.PieceColor.RED).row(2).col(1).build();
        board[2][7] = Piece.builder().type(Piece.PieceType.CANNON).color(Piece.PieceColor.RED).row(2).col(7).build();
        
        // 졸 배치
        board[3][0] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.RED).row(3).col(0).build();
        board[3][2] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.RED).row(3).col(2).build();
        board[3][4] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.RED).row(3).col(4).build();
        board[3][6] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.RED).row(3).col(6).build();
        board[3][8] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.RED).row(3).col(8).build();
        
        // 초나라 (하단) 말 배치
        // 차, 마, 상, 사, 왕, 사, 상, 마, 차
        board[9][0] = Piece.builder().type(Piece.PieceType.CHARIOT).color(Piece.PieceColor.BLUE).row(9).col(0).build();
        board[9][1] = Piece.builder().type(Piece.PieceType.HORSE).color(Piece.PieceColor.BLUE).row(9).col(1).build();
        board[9][2] = Piece.builder().type(Piece.PieceType.ELEPHANT).color(Piece.PieceColor.BLUE).row(9).col(2).build();
        board[9][3] = Piece.builder().type(Piece.PieceType.GUARD).color(Piece.PieceColor.BLUE).row(9).col(3).build();
        board[9][5] = Piece.builder().type(Piece.PieceType.GUARD).color(Piece.PieceColor.BLUE).row(9).col(5).build();
        board[9][6] = Piece.builder().type(Piece.PieceType.ELEPHANT).color(Piece.PieceColor.BLUE).row(9).col(6).build();
        board[9][7] = Piece.builder().type(Piece.PieceType.HORSE).color(Piece.PieceColor.BLUE).row(9).col(7).build();
        board[9][8] = Piece.builder().type(Piece.PieceType.CHARIOT).color(Piece.PieceColor.BLUE).row(9).col(8).build();
        
        // 왕은 사보다 한 칸 앞에 배치
        board[8][4] = Piece.builder().type(Piece.PieceType.KING).color(Piece.PieceColor.BLUE).row(8).col(4).build();
        
        // 포 배치
        board[7][1] = Piece.builder().type(Piece.PieceType.CANNON).color(Piece.PieceColor.BLUE).row(7).col(1).build();
        board[7][7] = Piece.builder().type(Piece.PieceType.CANNON).color(Piece.PieceColor.BLUE).row(7).col(7).build();
        
        // 병 배치
        board[6][0] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.BLUE).row(6).col(0).build();
        board[6][2] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.BLUE).row(6).col(2).build();
        board[6][4] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.BLUE).row(6).col(4).build();
        board[6][6] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.BLUE).row(6).col(6).build();
        board[6][8] = Piece.builder().type(Piece.PieceType.SOLDIER).color(Piece.PieceColor.BLUE).row(6).col(8).build();
    }
    
    public Piece getPiece(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }
    
    public void setPiece(int row, int col, Piece piece) {
        if (isValidPosition(row, col)) {
            board[row][col] = piece;
            if (piece != null) {
                piece.setRow(row);
                piece.setCol(col);
            }
        }
    }
    
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_ROWS && col >= 0 && col < BOARD_COLS;
    }
    
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = getPiece(fromRow, fromCol);
        if (piece != null) {
            Piece capturedPiece = getPiece(toRow, toCol);
            if (capturedPiece != null) {
                capturedPiece.setCaptured(true);
                capturedPieces.add(capturedPiece);
            }
            
            setPiece(toRow, toCol, piece);
            setPiece(fromRow, fromCol, null);
        }
    }
    
    public GameBoard copy() {
        GameBoard newBoard = GameBoard.builder().build();
        for (int i = 0; i < BOARD_ROWS; i++) {
            for (int j = 0; j < BOARD_COLS; j++) {
                Piece piece = this.board[i][j];
                if (piece != null) {
                    newBoard.board[i][j] = piece.copy();
                }
            }
        }
        newBoard.capturedPieces = new ArrayList<>(this.capturedPieces);
        return newBoard;
    }
}
