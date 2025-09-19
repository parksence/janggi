package hjpark.janggibe.repository;

import hjpark.janggibe.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoveRepository extends JpaRepository<Move, Long> {
    
    List<Move> findByGameIdOrderByMoveNumberAsc(Long gameId);
    
    @Query("SELECT m FROM Move m WHERE m.gameId = :gameId ORDER BY m.moveNumber DESC")
    List<Move> findLatestMovesByGameId(@Param("gameId") Long gameId, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Move m WHERE m.gameId = :gameId")
    int countMovesByGameId(@Param("gameId") Long gameId);
}
