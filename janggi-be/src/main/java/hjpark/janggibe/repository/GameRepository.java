package hjpark.janggibe.repository;

import hjpark.janggibe.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    @Query("SELECT g FROM Game g WHERE (g.redPlayerName = :playerName OR g.bluePlayerName = :playerName) " +
           "AND g.gameStatus = 'IN_PROGRESS'")
    Optional<Game> findActiveGameByPlayerName(@Param("playerName") String playerName);
    
    @Query("SELECT g FROM Game g WHERE g.gameStatus = 'WAITING' ORDER BY g.createdAt ASC")
    List<Game> findWaitingGames();
    
    @Query("SELECT g FROM Game g WHERE (g.redPlayerName = :playerName OR g.bluePlayerName = :playerName) " +
           "ORDER BY g.updatedAt DESC")
    List<Game> findGamesByPlayerName(@Param("playerName") String playerName);
}
