/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sortbattle.server;

import com.sortbattle.common.Message;
import com.sortbattle.common.MessageType;
import static com.sortbattle.common.MessageType.CHALLENGE_REQUEST;
import static com.sortbattle.common.MessageType.CHALLENGE_RESPONSE;
import static com.sortbattle.common.MessageType.GAME_CONFIG_SUBMIT;
import static com.sortbattle.common.MessageType.LOGIN_REQUEST;
import static com.sortbattle.common.MessageType.LOGOUT_REQUEST;
import static com.sortbattle.common.MessageType.PLAYER_CLICK;
import static com.sortbattle.common.MessageType.REMATCH_RESPONSE;
import com.sortbattle.common.Player;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Player player;
    private GameSession gameSession;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
                        out.flush(); // Flush header

            in = new ObjectInputStream(socket.getInputStream());

            // Vòng lặp xử lý các thông điệp từ client
            while (true) {
                Message clientMessage = (Message) in.readObject();
                handleMessage(clientMessage);
            }
        } catch (EOFException | SocketException e) {
            // Client ngắt kết nối đột ngột
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in ClientHandler for " + (player != null ? player.getUsername() : "unknown") + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Message message) {
        System.out.println("Received from " + (player != null ? player.getUsername() : "new client") + ": " + message);
        switch (message.getType()) {
            case LOGIN_REQUEST:
                java.util.Map<String, String> loginCreds = (java.util.Map<String, String>) message.getPayload();
                handleLogin(loginCreds.get("username"), loginCreds.get("password"));
                break;

            case REGISTER_REQUEST:
                java.util.Map<String, String> registerCreds = (java.util.Map<String, String>) message.getPayload();
                handleRegister(registerCreds.get("username"), registerCreds.get("password"));
                break;

            case LEADERBOARD_REQUEST:
                handleLeaderboardRequest();
                break;

            case HISTORY_REQUEST:
                String historyUsername = (String) message.getPayload();
                handleHistoryRequest(historyUsername);
                break;
            case CHALLENGE_REQUEST:
                handleChallengeRequest((String) message.getPayload());
                break;
            case CHALLENGE_RESPONSE:
                handleChallengeResponse((Map<String, Object>) message.getPayload());
                break;
            case GAME_CONFIG_SUBMIT:
                if (gameSession != null) {
                    gameSession.configureAndStartGame(message);
                }
                break;
            case PLAYER_CLICK:
                 if (gameSession != null) {
                    gameSession.handlePlayerClick(this, message.getPayload());
                }
                break;
            case EXIT_GAME:
                if (gameSession != null) {
                    gameSession.handlePlayerDisconnect(this);
                }
                break;
            case REMATCH_RESPONSE:
                if(gameSession != null){
                    gameSession.handleRematchResponse(this, (boolean) message.getPayload());
                }
                break;
            case LOGOUT_REQUEST:
                cleanup();
                break;
            default:
                System.out.println("Unknown message type: " + message.getType());
        }
    }
    
    // private void handleLogin(String username) {
    //     // Kiểm tra xem tài khoản đã online chưa
    //     if (server.getClientHandler(username) != null) {
    //         // Gửi thông điệp ép logout cho client cũ
    //         server.getClientHandler(username).sendMessage(new Message(MessageType.REMOTE_LOGOUT, "Tài khoản của bạn đã được đăng nhập từ nơi khác."));
    //         server.getClientHandler(username).cleanup(); // Dọn dẹp kết nối cũ
    //     }
        
    //     Player authenticatedPlayer = server.authenticatePlayer(username);
    //     if (authenticatedPlayer != null) {
    //         this.player = authenticatedPlayer;
    //         sendMessage(new Message(MessageType.LOGIN_SUCCESS, this.player));
    //         server.addOnlineClient(this);
    //     } else {
    //         sendMessage(new Message(MessageType.LOGIN_FAILURE, "Tên đăng nhập không tồn tại."));
    //     }
    // }
    private void handleLogin(String username, String password) {
        if (server.getClientHandler(username) != null) {
            sendMessage(new Message(MessageType.LOGIN_FAILURE, "Tài khoản đã đăng nhập từ nơi khác!"));
            return;
        }
        
        Player authenticatedPlayer = server.authenticatePlayer(username, password);
        if (authenticatedPlayer != null) {
            player = authenticatedPlayer;
            player.setStatus(Player.PlayerStatus.IDLE);
            server.addOnlineClient(this);

            sendMessage(new Message(MessageType.LOGIN_SUCCESS, player));
            System.out.println("Player logged in: " + username);
            List<Player> onlinePlayers = server.getOnlinePlayers();
            sendMessage(new Message(MessageType.PLAYER_LIST_UPDATE, onlinePlayers));
        } else {
            sendMessage(new Message(MessageType.LOGIN_FAILURE, "Sai tên đăng nhập hoặc mật khẩu!"));
        }
    }

    private void handleRegister(String username, String password) {
        boolean success = server.registerPlayer(username, password);
        if (success) {
            sendMessage(new Message(MessageType.REGISTER_SUCCESS, "Đăng ký thành công!"));
            System.out.println("✓ New player registered: " + username);
        } else {
            sendMessage(new Message(MessageType.REGISTER_FAILURE, "Username đã tồn tại!"));
        }
    }

    private void handleLeaderboardRequest() {
        List<String> leaderboard = server.getLeaderboard(10);
        sendMessage(new Message(MessageType.LEADERBOARD_RESPONSE, leaderboard));
    }

    private void handleHistoryRequest(String username) {
        List<String> history = server.getMatchHistory(username, 10);
        sendMessage(new Message(MessageType.HISTORY_RESPONSE, history));
    }
        private void handleChallengeRequest(String opponentUsername) {
            ClientHandler opponentHandler = server.getClientHandler(opponentUsername);
            if (opponentHandler != null && opponentHandler.getPlayer().getStatus() == Player.PlayerStatus.IDLE) {
                // Gửi lời mời tới đối thủ
                Message challengeMessage = new Message(MessageType.INCOMING_CHALLENGE, this.player.getUsername());
                opponentHandler.sendMessage(challengeMessage);
            } else {
                // Gửi lại thông báo cho người mời là đối thủ không sẵn sàng
                sendMessage(new Message(MessageType.CHALLENGE_REJECTED, "Người chơi " + opponentUsername + " không sẵn sàng hoặc không online."));
            }
        }

    private void handleChallengeResponse(Map<String, Object> response) {
        String challengerUsername = (String) response.get("challenger");
        boolean accepted = (boolean) response.get("accepted");
        ClientHandler challengerHandler = server.getClientHandler(challengerUsername);

        if (challengerHandler != null) {
            if (accepted) {
                // Tạo session game và thông báo cho người mời để cấu hình game
                server.createGameSession(challengerHandler, this);
                challengerHandler.sendMessage(new Message(MessageType.CHALLENGE_ACCEPTED, this.player.getUsername()));
            } else {
                challengerHandler.sendMessage(new Message(MessageType.CHALLENGE_REJECTED, this.player.getUsername() + " đã từ chối lời mời."));
            }
        }
    }

    public void sendPlayerList(List<Player> players) {
        sendMessage(new Message(MessageType.PLAYER_LIST_UPDATE, players));
    }

    public void sendMessage(Message message) {
        try {
        if (socket.isConnected() && !socket.isClosed()) {
            synchronized (out) { // Đồng bộ hóa để tránh xung đột ghi
                out.writeObject(message);
                out.flush();
                // out.reset(); // Reset để tránh cache object cũ
            }
        }
    } catch (IOException e) {
        System.err.println("Failed to send message to " + (player != null ? player.getUsername() : "unknown") + ": " + e.getMessage());
        cleanup();
    }
    }
    
    private void cleanup() {
        if (gameSession != null) {
            gameSession.handlePlayerDisconnect(this);
        }
        server.removeOnlineClient(player != null ? player.getUsername() : null);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    // Getters and Setters
    public Player getPlayer() {
        return player;
    }

    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }
}
