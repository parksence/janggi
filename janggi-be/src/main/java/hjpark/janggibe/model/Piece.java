package hjpark.janggibe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Piece {
    private PieceType type;
    private PieceColor color;
    private int row;
    private int col;
    private boolean isCaptured;
    
    public enum PieceType {
        KING("왕"),           // 왕
        GUARD("사"),          // 사
        ELEPHANT("상"),       // 상
        HORSE("마"),          // 마
        CHARIOT("차"),        // 차
        CANNON("포"),         // 포
        SOLDIER("졸");        // 졸/병
        
        private final String koreanName;
        
        PieceType(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() {
            return koreanName;
        }
    }
    
    public enum PieceColor {
        RED("한"),    // 한나라 (상단)
        BLUE("초");   // 초나라 (하단)
        
        private final String koreanName;
        
        PieceColor(String koreanName) {
            this.koreanName = koreanName;
        }
        
        public String getKoreanName() {
            return koreanName;
        }
    }
    
    public Piece copy() {
        return Piece.builder()
                .type(this.type)
                .color(this.color)
                .row(this.row)
                .col(this.col)
                .isCaptured(this.isCaptured)
                .build();
    }
}
