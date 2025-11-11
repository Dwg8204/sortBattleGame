package com.sortbattle.client.controller;

import com.sortbattle.client.model.ClientModel;
import com.sortbattle.client.view.GameView;
import com.sortbattle.client.view.LobbyView;
import com.sortbattle.client.view.LoginView;
import com.sortbattle.common.GameConfig;
import com.sortbattle.common.Message;
import com.sortbattle.common.MessageType;
import com.sortbattle.common.Player;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientController {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final ClientModel model;
    private LoginView loginView;
    private LobbyView lobbyView;
    private GameView gameView;
    private JDialog activeDialog; // Để theo dõi dialog đang mở (RegisterDialog)

    public ClientController() {
        this.model = new ClientModel();
        connectToServer();  // ← THÊM DÒNG NÀY
    }
    
    /**
     * KẾT NỐI ĐẾN SERVER
     */
    private void connectToServer() {
        try {
            System.out.println(" Connecting to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            
            // QUAN TRỌNG: Tạo OutputStream TRƯỚC, flush, rồi mới tạo InputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println(" Connected to server successfully");

            // Khởi động thread lắng nghe server
            Thread listenerThread = new Thread(this::listenToServer);
            listenerThread.setDaemon(true);
            listenerThread.start();
            
        } catch (IOException e) {
            System.err.println(" Cannot connect to server: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Không thể kết nối đến server!\n" + 
                "Vui lòng kiểm tra:\n" +
                "1. Server đã chạy chưa?\n" +
                "2. Địa chỉ: " + SERVER_ADDRESS + ":" + SERVER_PORT, 
                "Lỗi kết nối", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * LẮNG NGHE MESSAGE TỪ SERVER
     */
    private void listenToServer() {
        try {
            while (true) {
                Message serverMessage = (Message) in.readObject();
                System.out.println("<<< Received from Server: " + serverMessage.getType());
                SwingUtilities.invokeLater(() -> handleServerMessage(serverMessage));
            }
        } catch (EOFException | SocketException e) {
            System.err.println("✗ Server disconnected");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                    "Mất kết nối với server!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            });
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("✗ Error reading from server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * GỬI MESSAGE ĐẾN SERVER
     */
    public void sendMessage(Message message) {
        try {
            if (out == null) {
                System.err.println("✗ Cannot send message: not connected to server");
                JOptionPane.showMessageDialog(null, 
                    "Chưa kết nối đến server!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            out.writeObject(message);
            out.reset();
            out.flush();
            System.out.println(">>> Sent to Server: " + message.getType());
        } catch (IOException e) {
            System.err.println("✗ Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * HIỂN THỊ MÀN HÌNH ĐĂNG NHẬP
     */
    public void showLoginView() {
        loginView = new LoginView(this);
        loginView.setVisible(true);
    }

    public void setActiveDialog(JDialog dialog) {
        this.activeDialog = dialog;
    }

    /**
     * XỬ LÝ MESSAGE TỪ SERVER
     */
    private void handleServerMessage(Message message) {
        System.out.println("Handling message: " + message.getType());
        // Xác định cửa sổ cha cho các thông báo
        // Ưu tiên dialog đang mở, nếu không có thì dùng loginView
        Component parent = (activeDialog != null && activeDialog.isVisible()) ? activeDialog : loginView;
        switch (message.getType()) {
            case LOGIN_SUCCESS:
                model.setCurrentPlayer((Player) message.getPayload());
                System.out.println("Login successful: " + model.getCurrentPlayer().getUsername());
                if (loginView != null) {
                    loginView.dispose();
                }
                lobbyView = new LobbyView(this);
                lobbyView.updateWelcomeMessage(model.getCurrentPlayer().getUsername());
                if (!model.getOnlinePlayers().isEmpty()) {
                    lobbyView.updatePlayerList(model.getOnlinePlayers(), model.getCurrentPlayer().getUsername());
                }
                lobbyView.setVisible(true);
                break;
                
            case LOGIN_FAILURE:
                String errorMsg = (String) message.getPayload();
                System.out.println("✗ Login failed: " + errorMsg);
                JOptionPane.showMessageDialog(loginView, // Lỗi đăng nhập luôn ở màn hình login
                    "Đăng nhập thất bại: " + errorMsg, 
                    "Lỗi đăng nhập", 
                    JOptionPane.ERROR_MESSAGE);
                break;
                
            case REGISTER_SUCCESS:
                // Nếu đăng ký thành công, đóng dialog đăng ký lại
                if (activeDialog != null) {
                    activeDialog.dispose();
                }
                JOptionPane.showMessageDialog(loginView, // Thông báo thành công trên màn hình login
                    "Đăng ký thành công! Vui lòng đăng nhập.", 
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
                break;
                
            case REGISTER_FAILURE:
                // Hiển thị lỗi ngay trên dialog đăng ký
                JOptionPane.showMessageDialog(parent, 
                    "Đăng ký thất bại: " + message.getPayload(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                break;
                
            case LEADERBOARD_RESPONSE:
            // SỬA ĐỔI: Gọi phương thức mới của LobbyView
            if (lobbyView != null && lobbyView.isVisible()) {
                List<Player> leaderboardData = (List<Player>) message.getPayload();
                lobbyView.showLeaderboardDialog(leaderboardData);
            }
            break;

        case HISTORY_RESPONSE:
            // SỬA ĐỔI: Gọi phương thức mới của LobbyView
            if (lobbyView != null && lobbyView.isVisible()) {
                List<String[]> historyData = (List<String[]>) message.getPayload();
                if (historyData.isEmpty()) {
                    JOptionPane.showMessageDialog(lobbyView, 
                        "Bạn chưa có lịch sử trận đấu", 
                        "Lịch sử", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    lobbyView.showMatchHistoryDialog(historyData);
                }
            }
            break;
                
            case PLAYER_LIST_UPDATE:
                List<Player> receivedPlayers = (List<Player>) message.getPayload();
                model.setOnlinePlayers(receivedPlayers); // Cập nhật danh sách chung

                if (model.getCurrentPlayer() != null) {
                    // Tìm người chơi hiện tại trong danh sách MỚI nhận được
                    for (Player p : receivedPlayers) {
                        if (p.getUsername().equals(model.getCurrentPlayer().getUsername())) {
                            
                            // === PHẦN SỬA LỖI QUAN TRỌNG ===
                            // THAY THẾ HOÀN TOÀN đối tượng player cũ bằng đối tượng mới từ danh sách.
                            // Điều này đảm bảo chúng ta đang sử dụng phiên bản mới nhất mà stream cung cấp.
                            model.setCurrentPlayer(p); 
                            // ==============================
                            
                            System.out.println("✓ Updated current player object. New score: " + p.getTotalScore());
                            break;
                        }
                    }
                }

                // Cập nhật giao diện (logic này vẫn giữ nguyên)
                if (lobbyView != null && lobbyView.isVisible()) {
                    lobbyView.updatePlayerList(model.getOnlinePlayers(), model.getCurrentPlayer().getUsername());
                    if (model.getCurrentPlayer() != null) {
                        lobbyView.updateWelcomeMessage(model.getCurrentPlayer().getUsername());
                    }
                }
                break;
                
            case INCOMING_CHALLENGE:
                handleIncomingChallenge((String) message.getPayload());
                break;
                
            case CHALLENGE_ACCEPTED:
                lobbyView.showGameConfigDialog((String) message.getPayload());
                break;
                
            case CHALLENGE_REJECTED:
                JOptionPane.showMessageDialog(lobbyView, 
                    message.getPayload().toString(), 
                    "Thách đấu bị từ chối", 
                    JOptionPane.INFORMATION_MESSAGE);
                break;
                
            case GAME_START:
            if (gameView !=null) {
                gameView.dispose();
                gameView = null;
            }
                handleGameStart((Map<String, Object>) message.getPayload());
                break;
                
            case GAME_STATE_UPDATE:
                handleGameStateUpdate((Map<String,Object>) message.getPayload());
                break;
                
            case GAME_OVER:
                handleGameOver((Map<String,Object>) message.getPayload());
                break;
                
            case OPPONENT_DISCONNECTED:
    SwingUtilities.invokeLater(() -> {
        // Hiển thị thông báo xong về lobby
        JOptionPane.showMessageDialog(gameView, 
            "Đối thủ " + message.getPayload() + " đã thoát!\n" + "Trận đấu kết thúc.", 
            "Trận đấu kết thúc", 
            JOptionPane.WARNING_MESSAGE);
        
        if (gameView != null) {
            gameView.dispose();
            gameView = null;
        }

        if (lobbyView != null) {
            lobbyView.setVisible(true);
            
            // ========== PHẦN SỬA LỖI QUAN TRỌNG ==========
            // Ngay sau khi hiển thị lại Lobby, hãy buộc nó cập nhật
            // danh sách người chơi và thông điệp chào mừng từ model.
            // Model đã được cập nhật bởi tin nhắn PLAYER_LIST_UPDATE trước đó.
            if (model.getCurrentPlayer() != null) {
                lobbyView.updatePlayerList(model.getOnlinePlayers(), model.getCurrentPlayer().getUsername());
                lobbyView.updateWelcomeMessage(model.getCurrentPlayer().getUsername());
            }
            // BỎ Timer không cần thiết ở đây nữa.
            // ===========================================
        }
    });
    break;
                
            case REMOTE_LOGOUT:
                JOptionPane.showMessageDialog(null, 
                    message.getPayload().toString(), 
                    "Thông báo", 
                    JOptionPane.WARNING_MESSAGE);
                System.exit(0);
                break;
                
            case REQUEST_REMATCH:
                handleRematchRequest();
                break;
                
            case REMATCH_ACCEPTED:
                if (gameView != null) {
                        gameView.hideWaitingDialog();
                        // gameView.setVisible(false);
                        // gameView.dispose();
                    }
                // JOptionPane.showMessageDialog(gameView, 
                //     "Đối thủ đồng ý chơi lại!", 
                //     "Rematch", 
                //     JOptionPane.INFORMATION_MESSAGE);
                // if (gameView != null) {
                //     gameView.setVisible(false);
                //     // gameView.dispose();
                // }
                // if (lobbyView != null) {
                //     lobbyView.setVisible(false);
                    
                // }
                break;
            case REQUEST_GAME_CONFIG:
                // Người bị thách đấu chọn cấu hình mới
                if (gameView != null && gameView.isVisible()) {
                    final GameView oldGameView = gameView;

                    SwingUtilities.invokeLater(() -> {
                        oldGameView.showConfigDialog((config) -> {
                            //gửi config lên server
                            sendMessage(new Message(MessageType.GAME_CONFIG_SUBMIT, config));
                            oldGameView.forceDispose();
                        });
                        // gameView.showConfigDialog((config) -> {
                        //     // Gửi config lên server
                        //     sendMessage(new Message(MessageType.GAME_CONFIG_SUBMIT, config));
                            
                        //     // Đóng gameView cũ, đợi GAME_START
                        //     gameView.dispose();
                        //     gameView = null;
                        // });
                    });
                    gameView = null; // Đặt gameView về null để chờ GAME_START mới
                }
                else{
                    System.err.println("Cannot request game config: gameView is null or not visible");
                }
                break;
            // Sửa trong file ClientController.java

case REMATCH_REJECTED:
    if (gameView != null) {
        gameView.hideWaitingDialog();
    }
    JOptionPane.showMessageDialog(lobbyView,
        "Đối thủ từ chối chơi lại.",
        "Rematch",
        JOptionPane.INFORMATION_MESSAGE);
    
    if (gameView != null) {
        gameView.dispose();
        gameView = null;
    }
    if (lobbyView != null) {
        lobbyView.setVisible(true);
        // Buộc cập nhật giao diện sảnh chờ với dữ liệu mới nhất từ model
        if (model.getCurrentPlayer() != null) {
            lobbyView.updatePlayerList(model.getOnlinePlayers(), model.getCurrentPlayer().getUsername());
            lobbyView.updateWelcomeMessage(model.getCurrentPlayer().getUsername());
        }
    }
    break;
            // Sửa trong file ClientController.java

case REMATCH_REJECTED_SILENT:
    // Không hiển thị thông báo, tự động về lobby
    if (gameView != null) {
        gameView.hideWaitingDialog();
        gameView.dispose();
        gameView = null;
    }
    if (lobbyView != null) {
        lobbyView.setVisible(true);
        // Buộc cập nhật giao diện sảnh chờ với dữ liệu mới nhất từ model
        if (model.getCurrentPlayer() != null) {
            lobbyView.updatePlayerList(model.getOnlinePlayers(), model.getCurrentPlayer().getUsername());
            lobbyView.updateWelcomeMessage(model.getCurrentPlayer().getUsername());
        }
    }
    break;
                            
            default:
                System.out.println("⚠ Unknown message type: " + message.getType());
        }
    }
    
    private void handleIncomingChallenge(String challenger) {
        int response = JOptionPane.showConfirmDialog(lobbyView,
                "Người chơi " + challenger + " muốn thách đấu bạn. Bạn có đồng ý?",
                "Lời mời thách đấu",
                JOptionPane.YES_NO_OPTION);

        boolean accepted = (response == JOptionPane.YES_OPTION);
        Map<String, Object> payload = new HashMap<>();
        payload.put("challenger", challenger);
        payload.put("accepted", accepted);
        sendMessage(new Message(MessageType.CHALLENGE_RESPONSE, payload));
    }

    private void handleGameStart(Map<String, Object> payload) {
        GameConfig config = (GameConfig) payload.get("config");
        List<String> items = (List<String>) payload.get("items");
        
        Player opponent;
        if (((Player) payload.get("opponent")).getUsername().equals(model.getCurrentPlayer().getUsername())) {
             opponent = (Player) payload.get("opponent2");
        } else {
             opponent = (Player) payload.get("opponent");
        }

        model.setCurrentGame(config, items, opponent);
        
        if (lobbyView != null) {
            lobbyView.setVisible(false);
        }
        gameView = new GameView(this);
        gameView.initializeGame(model);
        gameView.updateState(payload);
        gameView.setVisible(true);
    }
    
    private void handleGameStateUpdate(Map<String, Object> payload) {
        if (gameView != null && gameView.isVisible()) {
            gameView.updateState(payload);
        }
    }
    
    private void handleGameOver(Map<String, Object> payload) {
        if(gameView != null) {
            gameView.showEndGameDialog(payload);
        }
    }

    private void handleRematchRequest() {
        if (gameView != null && gameView.isVisible()) {
            int response = JOptionPane.showConfirmDialog(gameView, 
                "Bạn có muốn chơi lại không?", 
                "Chơi lại", 
                JOptionPane.YES_NO_OPTION);
            boolean wantsRematch = (response == JOptionPane.YES_OPTION);
            sendMessage(new Message(MessageType.REMATCH_RESPONSE, wantsRematch));

            if (!wantsRematch) {
                //  gameView.dispose();
                //  if (lobbyView != null) {
                //      lobbyView.setVisible(true);
                //  }
                gameView.showWaitingForRematch();
            } else {
                gameView.showWaitingForRematch();
            }
        }
    }

    /**
     * THỬ ĐĂNG NHẬP
     */
    public void attemptLogin(String username, String password) {
        System.out.println(">>> Attempting login: " + username);
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);
        sendMessage(new Message(MessageType.LOGIN_REQUEST, credentials));
    }

    /**
     * THỬ ĐĂNG KÝ
     */
    public void attemptRegister(String username, String password) {
        System.out.println(">>> Attempting register: " + username);
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);
        sendMessage(new Message(MessageType.REGISTER_REQUEST, credentials));
    }

    /**
     * YÊU CẦU BẢNG XẾP HẠNG
     */
    public void requestLeaderboard() {
        sendMessage(new Message(MessageType.LEADERBOARD_REQUEST, null));
    }

    /**
     * YÊU CẦU LỊCH SỬ
     */
    public void requestHistory() {
        if (model.getCurrentPlayer() != null) {
            sendMessage(new Message(MessageType.HISTORY_REQUEST, model.getCurrentPlayer().getUsername()));
        }
    }
    
    /**
     * THÁCH ĐẤU NGƯỜI CHƠI
     */
    public void challengePlayer(String opponentUsername) {
        if (opponentUsername.equals(model.getCurrentPlayer().getUsername())) {
            JOptionPane.showMessageDialog(lobbyView, 
                "Bạn không thể tự thách đấu chính mình.", 
                "Lỗi", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        sendMessage(new Message(MessageType.CHALLENGE_REQUEST, opponentUsername));
    }
    
    /**
     * GỬI CẤU HÌNH GAME
     */
    public void submitGameConfig(GameConfig config) {
        sendMessage(new Message(MessageType.GAME_CONFIG_SUBMIT, config));
    }

    /**
     * CLICK ITEM
     */
    public void onItemClick(String item) {
        sendMessage(new Message(MessageType.PLAYER_CLICK, item));
    }
    
    /**
     * THOÁT GAME
     */
    public void exitGame() {
        // try {
            //gửi thông báo thoát trận lên server
            if (model.getCurrentPlayer() != null) {
                sendMessage(new Message(MessageType.EXIT_GAME, model.getCurrentPlayer().getUsername())); 
            }
            //đóng gameview và mở lobby
            if (gameView!=null) {
                gameView.dispose();
                gameView = null;
            }
            if (lobbyView != null) {
                lobbyView.setVisible(true);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            if (lobbyView != null && model.getCurrentPlayer() != null) {
                                lobbyView.updateWelcomeMessage(model.getCurrentPlayer().getUsername());
                            }
                        });
                    }
                }, 500);
            }
            
            // if (socket != null && !socket.isClosed()) {
            //     socket.close();
            // }
        // } catch (IOException e) {
        //     System.err.println("✗ Error during exit: " + e.getMessage());
        // }
        // System.exit(0);
    }
    
    /**
     * HIỂN THỊ BẢNG XẾP HẠNG
     */
    // private void showLeaderboard(List<String> leaderboard) {
    //     Component parent = lobbyView != null ? lobbyView : gameView;
        
    //     if (leaderboard.isEmpty()) {
    //         JOptionPane.showMessageDialog(parent, 
    //             "Chưa có dữ liệu bảng xếp hạng", 
    //             "Bảng xếp hạng", 
    //             JOptionPane.INFORMATION_MESSAGE);
    //         return;
    //     }
        
    //     String text = "=== TOP 10 PLAYERS ===\n\n" + String.join("\n", leaderboard);
    //     JTextArea textArea = new JTextArea(text);
    //     textArea.setEditable(false);
    //     textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    //     JScrollPane scrollPane = new JScrollPane(textArea);
    //     scrollPane.setPreferredSize(new Dimension(500, 300));
    //     JOptionPane.showMessageDialog(parent, scrollPane, "Bảng xếp hạng", JOptionPane.INFORMATION_MESSAGE);
    // }

    // /**
    //  * HIỂN THỊ LỊCH SỬ
    //  */
    // private void showHistory(List<String> history) {
    //     Component parent = lobbyView != null ? lobbyView : gameView;
        
    //     if (history.isEmpty()) {
    //         JOptionPane.showMessageDialog(parent, 
    //             "Bạn chưa có lịch sử trận đấu", 
    //             "Lịch sử", 
    //             JOptionPane.INFORMATION_MESSAGE);
    //         return;
    //     }
        
    //     String text = "=== LỊCH SỬ TRẬN ĐẤU ===\n\n" + String.join("\n\n", history);
    //     JTextArea textArea = new JTextArea(text);
    //     textArea.setEditable(false);
    //     textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
    //     JScrollPane scrollPane = new JScrollPane(textArea);
    //     scrollPane.setPreferredSize(new Dimension(600, 350));
    //     JOptionPane.showMessageDialog(parent, scrollPane, "Lịch sử trận đấu", JOptionPane.INFORMATION_MESSAGE);
    // }

    /**
     * GETTER MODEL
     */
    public ClientModel getModel() {
        return model;
    }
}