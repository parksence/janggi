package hjpark.janggibe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "moves")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "game_id")
    private Long gameId;
    
    @Column(name = "player_name")
    private String playerName;
    
    @Column(name = "from_row")
    private int fromRow;
    
    @Column(name = "from_col")
    private int fromCol;
    
    @Column(name = "to_row")
    private int toRow;
    
    @Column(name = "to_col")
    private int toCol;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "piece_type")
    private Piece.PieceType pieceType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "piece_color")
    private Piece.PieceColor pieceColor;
    
    @Column(name = "captured_piece_type")
    private String capturedPieceType; // 잡힌 말의 타입
    
    @Column(name = "move_number")
    private int moveNumber;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
