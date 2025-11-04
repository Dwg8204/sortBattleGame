package com.sortbattle.server;

import com.sortbattle.common.GameConfig;
import com.sortbattle.common.Message;
import com.sortbattle.common.MessageType;
import com.sortbattle.common.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.text.Collator;
public class GameSession {
    private final String gameId;
    private final ClientHandler player1Handler;
    private final ClientHandler player2Handler;
    private final GameServer server;  // ← TÊN BIẾN LÀ server

    private GameConfig config;
    private List<String> shuffledItems;
    private List<String> sortedItems;
    private final Set<String> correctlyClickedItems = new HashSet<>();
    private final Map<String, Integer> scores = new HashMap<>();
    private Timer gameTimer;
    private int timeLeft;
    private int nextItemIndex = 0;
    
    private boolean gameEnded = false;
    private final Map<String, Boolean> rematchResponses = new HashMap<>();

    // Tải từ điển một lần duy nhất
    private static final List<String> WORD_DICTIONARY = loadWordDictionary();

    public GameSession(String gameId, ClientHandler player1Handler, ClientHandler player2Handler, GameServer server) {
        this.gameId = gameId;
        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;
        this.server = server;  // ← ĐÚNG LÀ server
    }
    
    private static List<String> loadWordDictionary() {
        List<String> words = new ArrayList<>();
        String path = "src/resources/vi-DauCu.dic";
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            reader.readLine(); // Skip first line (word count)
            while ((line = reader.readLine()) != null) {
                String word = line.split("/")[0].toLowerCase().trim();
                if (word.matches("^[a-záàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ]+$")) {
                     words.add(word);
                }
            }
            System.out.println("✓ Loaded " + words.size() + " words from dictionary.");
        } catch (IOException e) {
            System.err.println("✗ Could not load word dictionary from " + path + ": " + e.getMessage());
        }
        return words;
    }
    
    public void configureAndStartGame(Message configMessage) {
        this.config = (GameConfig) configMessage.getPayload();
        
        // Server quyết định ngẫu nhiên thứ tự sắp xếp
        this.config.setSortOrder(new Random().nextBoolean() ? GameConfig.SortOrder.ASCENDING : GameConfig.SortOrder.DESCENDING);

        // Chuẩn bị dữ liệu game
        prepareGameData();

        // Khởi tạo điểm số
        scores.put(player1Handler.getPlayer().getUsername(), 0);
        scores.put(player2Handler.getPlayer().getUsername(), 0);

        // Gửi thông điệp bắt đầu game cho cả 2 người chơi
        Map<String, Object> gameStartPayload = createGameStartPayload();
        Message gameStartMessage = new Message(MessageType.GAME_START, gameStartPayload);
        player1Handler.sendMessage(gameStartMessage);
        player2Handler.sendMessage(gameStartMessage);
        
        // Bắt đầu đếm ngược
        startGameTimer();
    }

    private void prepareGameData() {
        if (config.getDataType() == GameConfig.DataType.NUMBER) {
            int maxNumber = config.getItemCount();
            List<Integer> numbers = IntStream.rangeClosed(1, maxNumber).boxed().collect(Collectors.toList());
            Collections.shuffle(numbers);
            shuffledItems = numbers.stream().map(String::valueOf).collect(Collectors.toList());
        } else { // WORD
            if (WORD_DICTIONARY.isEmpty()) {
                // Quay về dữ liệu số nếu không tải được từ điển
                config.setDataType(GameConfig.DataType.NUMBER);
                config.setItemCount(60);
                prepareGameData();
                return;
            }
            Collections.shuffle(WORD_DICTIONARY);
            shuffledItems = WORD_DICTIONARY.stream().limit(config.getItemCount()).collect(Collectors.toList());
        }

        // Tạo danh sách đã được sắp xếp để kiểm tra
        sortedItems = new ArrayList<>(shuffledItems);
                
        if (config.getDataType() == GameConfig.DataType.NUMBER) {
            // Sắp xếp số
            if (config.getSortOrder() == GameConfig.SortOrder.ASCENDING) {
                sortedItems.sort((s1, s2) -> Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)));
            } else { // DESCENDING
                sortedItems.sort((s1, s2) -> Integer.compare(Integer.parseInt(s2), Integer.parseInt(s1)));
            }
        } else { // WORD
            // Sắp xếp từ tiếng Việt - SỬ DỤNG COLLATOR
            Collator vietnameseCollator = Collator.getInstance(new Locale("vi", "VN"));
            vietnameseCollator.setStrength(Collator.PRIMARY); // Bỏ qua dấu thanh khi so sánh
            
            if (config.getSortOrder() == GameConfig.SortOrder.ASCENDING) {
                // Tăng dần: a → z
                sortedItems.sort(vietnameseCollator);
            } else { // DESCENDING
                // Giảm dần: z → a
                sortedItems.sort(vietnameseCollator.reversed());
            }
        }
        
    }
    
    private Map<String, Object> createGameStartPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("config", config);
        payload.put("items", shuffledItems);
        payload.put("opponent", player1Handler.getPlayer());
        payload.put("opponent2", player2Handler.getPlayer());
        payload.put("scores", new HashMap<>(scores));
        payload.put("timeLeft", config.getTimeLimitSeconds());
        payload.put("nextHint", config.isHintsEnabled() && nextItemIndex < sortedItems.size() ? sortedItems.get(nextItemIndex) : null);
        return payload;
    }

    private void startGameTimer() {
        timeLeft = config.getTimeLimitSeconds();
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (GameSession.this) {
                    if (gameEnded) {
                        cancel();
                        return;
                    }
                    
                    timeLeft--;
                    broadcastTimeUpdate();
                    
                    if (timeLeft <= 0) {
                        endGame("Hết thời gian!");
                    }
                }
            }
        }, 1000, 1000);
    }
    
    private void broadcastTimeUpdate() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("scores", new HashMap<>(scores));
        payload.put("timeLeft", Integer.valueOf(timeLeft));
        payload.put("nextHint", config.isHintsEnabled() && nextItemIndex < sortedItems.size() ? sortedItems.get(nextItemIndex) : null);
        
        Message updateMessage = new Message(MessageType.GAME_STATE_UPDATE, payload);
        player1Handler.sendMessage(updateMessage);
        player2Handler.sendMessage(updateMessage);
    }
    
    public synchronized void handlePlayerClick(ClientHandler handler, Object clickedItemObj) {
        if (gameEnded) return;

        String clickedItem = (String) clickedItemObj;
        String correctItem = sortedItems.get(nextItemIndex);

        if (clickedItem.equals(correctItem) && !correctlyClickedItems.contains(clickedItem)) {
            nextItemIndex++;
            correctlyClickedItems.add(clickedItem);
            
            String username = handler.getPlayer().getUsername();
            scores.put(username, scores.get(username) + 1);

            broadcastGameStateUpdate(clickedItem, username);

            if (nextItemIndex == sortedItems.size()) {
                endGame("Đã sắp xếp xong!");
            }
        }
    }
    
    private void broadcastGameStateUpdate(String clickedItem, String scoredPlayer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("scores", new HashMap<>(scores));
        payload.put("timeLeft", timeLeft);
        payload.put("clickedItem", clickedItem);
        payload.put("scoredPlayer", scoredPlayer);
        payload.put("nextHint", config.isHintsEnabled() && nextItemIndex < sortedItems.size() ? sortedItems.get(nextItemIndex) : null);
        
        Message updateMessage = new Message(MessageType.GAME_STATE_UPDATE, payload);
        player1Handler.sendMessage(updateMessage);
        player2Handler.sendMessage(updateMessage);
    }

    private synchronized void endGame(String reason) {
        if (gameEnded) return;
        gameEnded = true;
        
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        // Xác định người thắng cuộc
        Player p1 = player1Handler.getPlayer();
        Player p2 = player2Handler.getPlayer();
        int score1 = scores.get(p1.getUsername());
        int score2 = scores.get(p2.getUsername());

        String winner;
        int p1ScoreChange = 0;
        int p2ScoreChange = 0;
        if (score1 > score2) {
            winner = p1.getUsername();
            p1ScoreChange = 50;
            p2ScoreChange = -25;
        } else if (score2 > score1) {
            winner = p2.getUsername();
            p2ScoreChange = 50;
            p1ScoreChange = -25;
        } else { // Hòa
            winner = null;
            p1ScoreChange = 20;
            p2ScoreChange = 20;
        }
        // Cập nhật điểm local (để hiển thị trên UI)

        p1.addScore(p1ScoreChange);
        p2.addScore(p2ScoreChange);
        // Cập nhật điểm vào database
        server.updatePlayerScore(p1.getUsername(), p1ScoreChange, score1 > score2);  // ← SỬA: gameServer → server
        server.updatePlayerScore(p2.getUsername(), p2ScoreChange, score2 > score1);  // ← SỬA: gameServer → server
        
        // Lưu lịch sử trận đấu
        int duration = config.getTimeLimitSeconds() - timeLeft;
        server.saveMatchHistory(  // ← SỬA: gameServer → server
            p1.getUsername(),
            p2.getUsername(),
            winner,
            score1, score2,
            config.getDataType().toString(),  // ← ĐÃ ĐÚNG
            config.getSortOrder().toString(),
            config.getItemCount(),
            config.getTimeLimitSeconds(),
            duration
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", reason);
        payload.put("scores", scores);
        payload.put("winner", winner);
        payload.put("finalPlayer1", p1);
        payload.put("finalPlayer2", p2);

        Message endMessage = new Message(MessageType.GAME_OVER, payload);
        player1Handler.sendMessage(endMessage);
        player2Handler.sendMessage(endMessage);

        // Sau 2 giây, hỏi có muốn chơi lại không
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                askForRematch();
            }
        }, 2000);
    }

    private void askForRematch() {
        Message rematchRequest = new Message(MessageType.REQUEST_REMATCH, null);
        player1Handler.sendMessage(rematchRequest);
        player2Handler.sendMessage(rematchRequest);
    }

    public synchronized void handleRematchResponse(ClientHandler handler, boolean wantsRematch) {
        rematchResponses.put(handler.getPlayer().getUsername(), wantsRematch);

        if (rematchResponses.size() == 2) {
            boolean p1WantsRematch = rematchResponses.getOrDefault(player1Handler.getPlayer().getUsername(), false);
            boolean p2WantsRematch = rematchResponses.getOrDefault(player2Handler.getPlayer().getUsername(), false);

            if (p1WantsRematch && p2WantsRematch) {
                resetForNewGame();
                player2Handler.sendMessage(new Message(MessageType.REMATCH_ACCEPTED, player1Handler.getPlayer().getUsername()));
                player1Handler.sendMessage(new Message(MessageType.REMATCH_ACCEPTED, player2Handler.getPlayer().getUsername()));
            } else if(!p1WantsRematch && !p2WantsRematch) {
                player1Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED_SILENT, null));
                player2Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED_SILENT, null));
                terminateSession();
            }
             else {
                if(p1WantsRematch && !p2WantsRematch) {
                    player1Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED, player2Handler.getPlayer().getUsername()));
                    player2Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED_SILENT, null));
                } else if(!p1WantsRematch && p2WantsRematch) {
                    player2Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED, player1Handler.getPlayer().getUsername()));
                    player1Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED_SILENT, null));
                }
                terminateSession();
            }
        }
    }

    private void resetForNewGame() {
        this.shuffledItems = null;
        this.sortedItems = null;
        this.correctlyClickedItems.clear();
        this.scores.clear();
        this.gameTimer = null;
        this.nextItemIndex = 0;
        this.gameEnded = false;
        this.rematchResponses.clear();
    }

    private void terminateSession() {
        player1Handler.getPlayer().setStatus(Player.PlayerStatus.IDLE);
        player2Handler.getPlayer().setStatus(Player.PlayerStatus.IDLE);
        
        player1Handler.setGameSession(null);
        player2Handler.setGameSession(null);
        
        server.removeGameSession(this.gameId);
        // ========== DELAY 500MS ĐỂ CLIENT VỀ LOBBY TRƯỚC ==========
    new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
            // Lấy thông tin mới nhất từ database
            server.refreshPlayerInfo(player1Handler.getPlayer().getUsername());
            server.refreshPlayerInfo(player2Handler.getPlayer().getUsername());
            
            // Broadcast danh sách người chơi với điểm mới
            server.broadcastPlayerList();
            
            System.out.println("✓ Broadcasted player list with updated scores");
        }
    }, 500);  
    }
    
    public synchronized void handlePlayerDisconnect(ClientHandler disconnectedHandler) {
        if (gameEnded) return;

        ClientHandler remainingHandler = (disconnectedHandler == player1Handler) ? player2Handler : player1Handler;
        
        Player disconnectedPlayer = disconnectedHandler.getPlayer();
        Player remainingPlayer = remainingHandler.getPlayer();
        
        disconnectedPlayer.subtractScore(25);
        remainingPlayer.addScore(50);

        remainingHandler.sendMessage(new Message(MessageType.OPPONENT_DISCONNECTED, disconnectedPlayer.getUsername()));

        endGame("Đối thủ đã thoát.");
        terminateSession();
    }
}