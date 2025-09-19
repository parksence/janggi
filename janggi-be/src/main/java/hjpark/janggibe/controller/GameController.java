package hjpark.janggibe.controller;

import hjpark.janggibe.model.Game;
import hjpark.janggibe.model.Move;
import hjpark.janggibe.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Slf4j
public class GameController {
    
    private final GameService gameService;
    
    @PostMapping("/create")
    public ResponseEntity<Game> createGame(@RequestBody Map<String, String> request) {
        String playerName = request.get("playerName");
        String roomTitle = request.get("roomTitle");
        Game game = gameService.createGame(playerName, roomTitle);
        return ResponseEntity.ok(game);
    }
    
    @PostMapping("/{gameId}/join")
    public ResponseEntity<Game> joinGame(@PathVariable Long gameId, @RequestBody Map<String, String> request) {
        String playerName = request.get("playerName");
        Game game = gameService.joinGame(gameId, playerName);
        return ResponseEntity.ok(game);
    }
    
    @PostMapping("/{gameId}/move")
    public ResponseEntity<Game> makeMove(
            @PathVariable Long gameId,
            @RequestBody Map<String, Object> moveRequest) {
        
        String playerName = (String) moveRequest.get("playerName");
        int fromRow = (Integer) moveRequest.get("fromRow");
        int fromCol = (Integer) moveRequest.get("fromCol");
        int toRow = (Integer) moveRequest.get("toRow");
        int toCol = (Integer) moveRequest.get("toCol");
        
        Game game = gameService.makeMove(gameId, playerName, fromRow, fromCol, toRow, toCol);
        return ResponseEntity.ok(game);
    }
    
    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable Long gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(game);
    }
    
    @GetMapping("/waiting")
    public ResponseEntity<List<Game>> getWaitingGames() {
        List<Game> games = gameService.getWaitingGames();
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/active/{playerName}")
    public ResponseEntity<Game> getActiveGame(@PathVariable String playerName) {
        return gameService.getActiveGameByPlayerName(playerName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{gameId}/history")
    public ResponseEntity<List<Move>> getGameHistory(@PathVariable Long gameId) {
        List<Move> moves = gameService.getGameHistory(gameId);
        return ResponseEntity.ok(moves);
    }
    
    @PostMapping("/{gameId}/leave")
    public ResponseEntity<String> leaveGame(@PathVariable Long gameId, @RequestBody Map<String, String> request) {
        String playerName = request.get("playerName");
        gameService.leaveGame(gameId, playerName);
        return ResponseEntity.ok("게임에서 나갔습니다.");
    }
    
}
