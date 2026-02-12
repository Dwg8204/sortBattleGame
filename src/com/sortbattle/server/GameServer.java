package com.sortbattle.server;

import com.sortbattle.common.Player;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GameServer {
    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 50;
    
    private final DatabaseManager dbManager;
    private final Map<String, ClientHandler> onlineClients = new ConcurrentHashMap<>();
    private final Map<String, GameSession> activeGames = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_PLAYERS);

    public GameServer() {
        this.dbManager = new DatabaseManager();
        System.out.println("=== SORT BATTLE SERVER ===");
    }

    public void start() {
        System.out.println("Game Server is running on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    threadPool.execute(clientHandler);
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + PORT + ": " + e.getMessage());
        } finally {
            threadPool.shutdown();
            dbManager.close();
        }
    }
    
    /**
     * XÁC THỰC NGƯỜI CHƠI
     */
    public Player authenticatePlayer(String username, String password) {
        return dbManager.authenticatePlayer(username, password);
    }

    /**
     * ĐĂNG KÝ NGƯỜI CHƠI MỚI
     */
    public boolean registerPlayer(String username, String password) {
        return dbManager.registerPlayer(username, password);
    }

    /**
     * CẬP NHẬT ĐIỂM SAU TRẬN ĐẤU
     */
    public synchronized Player updatePlayerScore(String username, int scoreChange, boolean won) {
    // 1. Cập nhật vào DB
    dbManager.updatePlayerScore(username, scoreChange, won);
    
    // 2. Lấy lại thông tin mới nhất từ DB
    Player updatedPlayer = dbManager.getPlayer(username);

    // 3. Cập nhật đối tượng trong bộ nhớ server
    ClientHandler handler = onlineClients.get(username);
    if (handler != null && updatedPlayer != null) {
        Player playerInMemory = handler.getPlayer();
        playerInMemory.setTotalScore(updatedPlayer.getTotalScore());
        playerInMemory.setGamesPlayed(updatedPlayer.getGamesPlayed());
        playerInMemory.setGamesWon(updatedPlayer.getGamesWon());
    }
    
    // 4. Trả về đối tượng đã được cập nhật hoàn toàn
    //    (Bước này vẫn giữ nguyên để endGame hoạt động chính xác)
    return updatedPlayer;
}

    /**
     * LƯU LỊCH SỬ TRẬN ĐẤU
     */
    public void saveMatchHistory(String p1, String p2, String winner, 
                                   int p1Score, int p2Score, String mode, 
                                   String order, int items, int time, int duration) {
        dbManager.saveMatchHistory(p1, p2, winner, p1Score, p2Score, mode, order, items, time, duration);
    }

    /**
     * LẤY BẢNG XẾP HẠNG
     */
    public List<Player> getLeaderboard(int topN) {
    return dbManager.getLeaderboard(topN);
}

    /**
     * LẤY LỊCH SỬ TRẬN ĐẤU
     */
    public List<String[]> getMatchHistory(String username, int limit) {
    return dbManager.getMatchHistory(username, limit);
}

    /**
     * THÊM CLIENT VÀO DANH SÁCH ONLINE
     */
    public synchronized void addOnlineClient(ClientHandler clientHandler) {
        onlineClients.put(clientHandler.getPlayer().getUsername(), clientHandler);
        broadcastPlayerList();
    }
    
    public synchronized List<Player> getOnlinePlayers() {
        return onlineClients.values().stream()
                .map(ClientHandler::getPlayer)
                .collect(Collectors.toList());
    }
    /**
     * XÓA CLIENT KHỎI DANH SÁCH ONLINE
     */
    public synchronized void removeOnlineClient(String username) {
        if (username != null) {
            ClientHandler handler = onlineClients.remove(username);
            
            // Đặt lại trạng thái người chơi thành IDLE
            if (handler != null && handler.getPlayer() != null) {
                handler.getPlayer().setStatus(Player.PlayerStatus.IDLE);
            }
            
            broadcastPlayerList();
            System.out.println("Player " + username + " has disconnected.");
        }
    }

    /**
     * GỬI DANH SÁCH NGƯỜI CHƠI ONLINE TỚI TẤT CẢ CLIENT
     */
    public synchronized void broadcastPlayerList() {
        List<Player> onlinePlayers = onlineClients.values().stream()
                .map(ClientHandler::getPlayer)
                .collect(Collectors.toList());
        
        onlineClients.values().forEach(client -> client.sendPlayerList(onlinePlayers));
    }
    
    /**
     * TÌM CLIENTHANDLER BẰNG USERNAME
     */
    public ClientHandler getClientHandler(String username) {
        return onlineClients.get(username);
    }

    /**
     * TẠO VÀ BẮT ĐẦU TRẬN ĐẤU MỚI
     */
    public synchronized void createGameSession(ClientHandler player1Handler, ClientHandler player2Handler) {
        String gameId = "game-" + player1Handler.getPlayer().getUsername() + "-" + player2Handler.getPlayer().getUsername();
        GameSession newGame = new GameSession(gameId, player1Handler, player2Handler, this);
        activeGames.put(gameId, newGame);
        
        player1Handler.setGameSession(newGame);
        player2Handler.setGameSession(newGame);

        // Chuyển trạng thái người chơi thành BUSY và broadcast
        player1Handler.getPlayer().setStatus(Player.PlayerStatus.BUSY);
        player2Handler.getPlayer().setStatus(Player.PlayerStatus.BUSY);
        broadcastPlayerList();
    }
    
    /**
     * XÓA TRẬN ĐẤU
     */
    public void removeGameSession(String gameId) {
        activeGames.remove(gameId);
    }
    /**
     * CẬP NHẬT LẠI THÔNG TIN PLAYER TỪ DATABASE
     */
    public void refreshPlayerInfo(String username) {
        ClientHandler handler = onlineClients.get(username);
        if (handler != null) {
            Player freshPlayer = dbManager.getPlayer(username);
            if (freshPlayer != null) {
                handler.getPlayer().setTotalScore(freshPlayer.getTotalScore());
                handler.getPlayer().setGamesPlayed(freshPlayer.getGamesPlayed());
                handler.getPlayer().setGamesWon(freshPlayer.getGamesWon());
                System.out.println("✓ Refreshed player info for " + username + 
                                " | Score: " + freshPlayer.getTotalScore());
            }
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}