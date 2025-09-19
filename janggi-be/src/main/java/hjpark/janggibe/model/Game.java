package hjpark.janggibe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_title")
    private String roomTitle;
    
    @Column(name = "red_player_name")
    private String redPlayerName;
    
    @Column(name = "blue_player_name")
    private String bluePlayerName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_turn")
    private Piece.PieceColor currentTurn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_status")
    private GameStatus gameStatus;
    
    @Column(name = "winner_name")
    private String winnerName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "board_state", columnDefinition = "TEXT")
    private String boardState; // JSON 형태로 보드 상태 저장
    
    @Column(name = "move_history", columnDefinition = "TEXT")
    private String moveHistory; // JSON 형태로 이동 기록 저장
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum GameStatus {
        WAITING,    // 대기 중
        IN_PROGRESS, // 진행 중
        FINISHED,   // 종료
        ABANDONED   // 포기
    }
}
