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
import java.util.ArrayList;
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
        connectToServer(); // ← THÊM DÒNG NÀY
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

            // Trong handleServerMessage, thay case LEADERBOARD_RESPONSE:
            // Trong handleServerMessage
            case LEADERBOARD_RESPONSE:
                List<String> leaderboardData = (List<String>) message.getPayload();
                System.out.println("Leaderboard data size: " + leaderboardData.size());
                List<Player> players = new ArrayList<>();
                for (String line : leaderboardData) {
                    System.out.println("Parsing leaderboard line: " + line);
                    try {
                        // Format thực tế: "#1 - player1: 97 điểm | 6/12 thắng (50.0%)"
                        String[] parts = line.split(" - ", 2); // Split chỉ 1 lần để có 2 phần
                        if (parts.length == 2) {
                            String rank = parts[0].trim(); // "#1"
                            String info = parts[1].trim(); // "player1: 97 điểm | 6/12 thắng (50.0%)"

                            // Parse info: "player1: 97 điểm | 6/12 thắng (50.0%)"
                            String[] infoParts = info.split("\\|"); // Split theo "|"
                            if (infoParts.length >= 2) {
                                String usernameAndScore = infoParts[0].trim(); // "player1: 97 điểm"
                                String winsAndRate = infoParts[1].trim(); // "6/12 thắng (50.0%)"

                                // Tách username và score từ "player1: 97 điểm"
                                int colonIndex = usernameAndScore.indexOf(": ");
                                if (colonIndex != -1) {
                                    String username = usernameAndScore.substring(0, colonIndex).trim();
                                    String scoreStr = usernameAndScore.substring(colonIndex + 2).replace(" điểm", "")
                                            .trim();
                                    int totalScore = Integer.parseInt(scoreStr);

                                    // Tách wins/total từ "6/12 thắng (50.0%)"
                                    String[] winParts = winsAndRate.split(" ")[0].split("/"); // "6/12" → ["6", "12"]
                                    if (winParts.length == 2) {
                                        int wins = Integer.parseInt(winParts[0]);
                                        int total = Integer.parseInt(winParts[1]);
                                        int losses = total - wins;

                                        Player p = new Player(username);
                                        p.setTotalScore(totalScore);
                                        p.setGamesWon(wins);
                                        p.setGamesPlayed(total);
                                        players.add(p);
                                    } else {
                                        System.err.println("Invalid wins/total in line: " + line);
                                    }
                                } else {
                                    System.err.println("Invalid username/score in line: " + line);
                                }
                            } else {
                                System.err.println("Invalid info parts in line: " + line);
                            }
                        } else {
                            System.err.println("Invalid main parts in line: " + line);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Parse error (number) in line: " + line + " - " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Unexpected parse error in line: " + line + " - " + e.getMessage());
                    }
                }
                if (lobbyView != null) {
                    if (players.isEmpty()) {
                        JOptionPane.showMessageDialog(lobbyView, "Chưa có dữ liệu bảng xếp hạng", "Bảng xếp hạng",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        lobbyView.showLeaderboardDialog(players);
                    }
                }
                break;

            case HISTORY_RESPONSE:
                List<String> historyData = (List<String>) message.getPayload();
                List<String[]> historyRows = new ArrayList<>();
                String currentUsername = model.getCurrentPlayer().getUsername(); // Lấy tên người chơi hiện tại

                for (String line : historyData) {
                    try {
                        String[] row = line.split(" \\| ");
                        if (row.length >= 5) {
                            for (int i = 0; i < row.length; i++) row[i] = row[i].trim();

                            String opponent = row[0]; // "player2 vs player1"
                            String type = row[1];     // "NUMBER" or "WORD"
                            String score = row[2];    // "2-2"
                            String winnerInfo = row[3]; // "Winner: Draw" hoặc "Winner: player1"
                            String time = row[4]; 

                            // Map kết quả Win/Lose/Draw dựa vào current player
                            String result = "Draw"; // mặc định
                            if (!winnerInfo.toLowerCase().contains("draw")) {
                                String winnerName = winnerInfo.substring("Winner: ".length()).trim();
                                if (winnerName.equals(currentUsername)) {
                                    result = "Win";
                                } else {
                                    result = "Lose";
                                }
                            }

                            String[] formattedRow = { opponent, result, score, time, type };
                            historyRows.add(formattedRow);
                        }
                    } catch (Exception e) {
                        System.err.println("Parse error in history line: " + line + " - " + e.getMessage());
                    }
                }

                if (lobbyView != null) {
                    if (historyRows.isEmpty()) {
                        JOptionPane.showMessageDialog(lobbyView, "Bạn chưa có lịch sử trận đấu", "Lịch sử",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        lobbyView.showMatchHistoryDialog(historyRows);
                    }
                }
                break;


            case PLAYER_LIST_UPDATE:
                model.setOnlinePlayers((List<Player>) message.getPayload());
                if (model.getCurrentPlayer() != null) {
                    for (Player p : model.getOnlinePlayers()) {
                        if (p.getUsername().equals(model.getCurrentPlayer().getUsername())) {
                            // Cập nhật điểm từ danh sách mới
                            model.getCurrentPlayer().setTotalScore(p.getTotalScore());
                            model.getCurrentPlayer().setGamesPlayed(p.getGamesPlayed());
                            model.getCurrentPlayer().setGamesWon(p.getGamesWon());

                            System.out.println("✓ Updated current player score: " + p.getTotalScore());
                            break;
                        }
                    }
                }
                // Cập nhật luôn nếu lobbyView đã tồn tại
                if (lobbyView != null && lobbyView.isVisible()) {
                    lobbyView.updatePlayerList(model.getOnlinePlayers(), model.getCurrentPlayer().getUsername());
                    if (model.getCurrentPlayer() != null) {
                        lobbyView.updateWelcomeMessage(model.getCurrentPlayer().getUsername());
                    }
                }
                // CẬP NHẬT THÔNG TIN BẢN THÂN (nếu có trong danh sách)

                // Nếu chưa có lobbyView, danh sách đã lưu trong model
                // sẽ được hiển thị khi tạo lobbyView ở LOGIN_SUCCESS
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
                if (gameView != null) {
                    gameView.dispose();
                    gameView = null;
                }
                handleGameStart((Map<String, Object>) message.getPayload());
                break;

            case GAME_STATE_UPDATE:
                handleGameStateUpdate((Map<String, Object>) message.getPayload());
                break;

            case GAME_OVER:
                handleGameOver((Map<String, Object>) message.getPayload());
                break;

            case OPPONENT_DISCONNECTED:
                SwingUtilities.invokeLater(() -> {
                    // hiển thị thông báo xong về lobby
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
                // "Đối thủ đồng ý chơi lại!",
                // "Rematch",
                // JOptionPane.INFORMATION_MESSAGE);
                // if (gameView != null) {
                // gameView.setVisible(false);
                // // gameView.dispose();
                // }
                // if (lobbyView != null) {
                // lobbyView.setVisible(false);

                // }
                break;
            case REQUEST_GAME_CONFIG:
                // Người bị thách đấu chọn cấu hình mới
                if (gameView != null && gameView.isVisible()) {
                    final GameView oldGameView = gameView;

                    SwingUtilities.invokeLater(() -> {
                        oldGameView.showConfigDialog((config) -> {
                            // gửi config lên server
                            sendMessage(new Message(MessageType.GAME_CONFIG_SUBMIT, config));
                            oldGameView.forceDispose();
                        });
                        // gameView.showConfigDialog((config) -> {
                        // // Gửi config lên server
                        // sendMessage(new Message(MessageType.GAME_CONFIG_SUBMIT, config));

                        // // Đóng gameView cũ, đợi GAME_START
                        // gameView.dispose();
                        // gameView = null;
                        // });
                    });
                    gameView = null; // Đặt gameView về null để chờ GAME_START mới
                } else {
                    System.err.println("Cannot request game config: gameView is null or not visible");
                }
                break;
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
                }
                if (lobbyView != null) {
                    lobbyView.setVisible(true);
                }
                // Đợi 500ms để nhận PLAYER_LIST_UPDATE từ server, rồi cập nhật UI
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
                break;
            case REMATCH_REJECTED_SILENT:
                // Không hiển thị thông báo, tự động về lobby
                if (gameView != null) {
                    gameView.hideWaitingDialog();
                    gameView.dispose();
                }
                if (lobbyView != null) {
                    lobbyView.setVisible(true);
                }
                // Đợi 500ms để nhận PLAYER_LIST_UPDATE từ server, rồi cập nhật UI
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
        if (gameView != null) {
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
                // gameView.dispose();
                // if (lobbyView != null) {
                // lobbyView.setVisible(true);
                // }
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
        // gửi thông báo thoát trận lên server
        if (model.getCurrentPlayer() != null) {
            sendMessage(new Message(MessageType.EXIT_GAME, model.getCurrentPlayer().getUsername()));
        }
        // đóng gameview và mở lobby
        if (gameView != null) {
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
        // socket.close();
        // }
        // } catch (IOException e) {
        // System.err.println("✗ Error during exit: " + e.getMessage());
        // }
        // System.exit(0);
    }

    /**
     * HIỂN THỊ BẢNG XẾP HẠNG
     */
    // private void showLeaderboard(List<String> leaderboard) {
    // Component parent = lobbyView != null ? lobbyView : gameView;

    // if (leaderboard.isEmpty()) {
    // JOptionPane.showMessageDialog(parent,
    // "Chưa có dữ liệu bảng xếp hạng",
    // "Bảng xếp hạng",
    // JOptionPane.INFORMATION_MESSAGE);
    // return;
    // }

    // String text = "=== TOP 10 PLAYERS ===\n\n" + String.join("\n", leaderboard);
    // JTextArea textArea = new JTextArea(text);
    // textArea.setEditable(false);
    // textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    // JScrollPane scrollPane = new JScrollPane(textArea);
    // scrollPane.setPreferredSize(new Dimension(500, 300));
    // JOptionPane.showMessageDialog(parent, scrollPane, "Bảng xếp hạng",
    // JOptionPane.INFORMATION_MESSAGE);
    // }

    /**
     * HIỂN THỊ LỊCH SỬ
     */
    // private void showHistory(List<String> history) {
    // Component parent = lobbyView != null ? lobbyView : gameView;

    // if (history.isEmpty()) {
    // JOptionPane.showMessageDialog(parent,
    // "Bạn chưa có lịch sử trận đấu",
    // "Lịch sử",
    // JOptionPane.INFORMATION_MESSAGE);
    // return;
    // }

    // String text = "=== LỊCH SỬ TRẬN ĐẤU ===\n\n" + String.join("\n\n", history);
    // JTextArea textArea = new JTextArea(text);
    // textArea.setEditable(false);
    // textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
    // JScrollPane scrollPane = new JScrollPane(textArea);
    // scrollPane.setPreferredSize(new Dimension(600, 350));
    // JOptionPane.showMessageDialog(parent, scrollPane, "Lịch sử trận đấu",
    // JOptionPane.INFORMATION_MESSAGE);
    // }

    /**
     * GETTER MODEL
     */
    public ClientModel getModel() {
        return model;
    }
}