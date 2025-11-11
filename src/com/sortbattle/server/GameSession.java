package com.sortbattle.server;

import com.sortbattle.common.GameConfig;
import com.sortbattle.common.Message;
import com.sortbattle.common.MessageType;
import com.sortbattle.common.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    // Biến cờ để đảm bảo logic rematch chỉ chạy một lần
    private final AtomicBoolean rematchLogicExecuted = new AtomicBoolean(false);

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
        String path = "src/resources/vi-DauCu-filtered.dic";
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
        payload.put("timeLeft",timeLeft);
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
        int p1ScoreChange, p2ScoreChange;
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

        Player finalP1 = server.updatePlayerScore(p1.getUsername(), p1ScoreChange, score1 > score2);
        Player finalP2 = server.updatePlayerScore(p2.getUsername(), p2ScoreChange, score2 > score1);
        
        // Lưu lịch sử trận đấu
        int duration = config.getTimeLimitSeconds() - timeLeft;
        server.saveMatchHistory(p1.getUsername(), p2.getUsername(), winner, score1, score2, config.getDataType().toString(), config.getSortOrder().toString(), config.getItemCount(), config.getTimeLimitSeconds(), duration);

        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", reason);
        payload.put("scores", scores);
        payload.put("winner", winner);
        payload.put("finalPlayer1", finalP1);
        payload.put("finalPlayer2", finalP2);

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

        if (rematchResponses.size() == 2 && rematchLogicExecuted.compareAndSet(false, true)) {
            boolean p1WantsRematch = rematchResponses.getOrDefault(player1Handler.getPlayer().getUsername(), false);
            boolean p2WantsRematch = rematchResponses.getOrDefault(player2Handler.getPlayer().getUsername(), false);

            if (p1WantsRematch && p2WantsRematch) {
                resetForNewGame();
            player1Handler.sendMessage(new Message(MessageType.REMATCH_ACCEPTED, null));
            player2Handler.sendMessage(new Message(MessageType.REMATCH_ACCEPTED, null));
            
            // ========== PHẦN SỬA LỖI QUAN TRỌNG ==========
            // Server phải yêu cầu người chơi bị thách đấu (player2)
            // cấu hình cho ván đấu mới.
            System.out.println("Both players agreed to rematch. Requesting new config from " + player2Handler.getPlayer().getUsername());
            player2Handler.sendMessage(new Message(MessageType.REQUEST_GAME_CONFIG, null));
            } else {
                if (!p1WantsRematch && !p2WantsRematch) {
                    player1Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED_SILENT, null));
                    player2Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED_SILENT, null));
                } else if (p1WantsRematch) { // p2 từ chối
                    player1Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED, null));
                    player2Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED_SILENT, null));
                } else { // p1 từ chối
                    player2Handler.sendMessage(new Message(MessageType.REMATCH_REJECTED, null));
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

    // ========== PHẦN SỬA LỖI ==========
    // BỎ HẲN TIMER VÀ GỌI BROADCAST NGAY LẬP TỨC

    // Lấy thông tin mới nhất từ database
    server.refreshPlayerInfo(player1Handler.getPlayer().getUsername());
    server.refreshPlayerInfo(player2Handler.getPlayer().getUsername());
    
    // Broadcast danh sách người chơi với điểm và trạng thái mới
    server.broadcastPlayerList();
    
    System.out.println("✓ Broadcasted player list immediately after session termination.");
    // ========== HẾT PHẦN SỬA LỖI ==========
}
    
    public synchronized void handlePlayerDisconnect(ClientHandler disconnectedHandler) {
        if (gameEnded) return;

        ClientHandler remainingHandler = (disconnectedHandler == player1Handler) ? player2Handler : player1Handler;
        
        Player disconnectedPlayer = disconnectedHandler.getPlayer();
        Player remainingPlayer = remainingHandler.getPlayer();
        
        //Người thoát: trừ 25đ

        server.updatePlayerScore(disconnectedPlayer.getUsername(), -25, false);
        disconnectedPlayer.setTotalScore(Math.max(0, disconnectedPlayer.getTotalScore() - 25));
        
        // Người còn lại: +50 điểm
        server.updatePlayerScore(remainingPlayer.getUsername(), 50, true);
        remainingPlayer.setTotalScore(remainingPlayer.getTotalScore() + 50);

        // Lưu lịch sử trận đấu
        int duration = config != null ? (config.getTimeLimitSeconds() - timeLeft) : 0;
        server.saveMatchHistory(
            player1Handler.getPlayer().getUsername(),
            player2Handler.getPlayer().getUsername(),
            remainingPlayer.getUsername(), // Người còn lại = thắng
            0, 0, // Điểm trong trận = 0 (vì chưa kết thúc)
            config != null ? config.getDataType().toString() : "UNKNOWN",
            config != null ? config.getSortOrder().toString() : "UNKNOWN",
            config != null ? config.getItemCount() : 0,
            config != null ? config.getTimeLimitSeconds() : 0,
            duration
        );
        
        // ========== SỬA: GỬI THÔNG BÁO CHO NGƯỜI CÒN LẠI (KHÔNG ASK REMATCH) ==========
        remainingHandler.sendMessage(new com.sortbattle.common.Message(
            com.sortbattle.common.MessageType.OPPONENT_DISCONNECTED, 
            disconnectedPlayer.getUsername()
        ));
        
        // ========== KẾT THÚC SESSION NGAY, KHÔNG GỌI endGame() ==========
        gameEnded = true;
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        terminateSession();
        }
}