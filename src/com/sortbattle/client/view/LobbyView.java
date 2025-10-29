/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sortbattle.client.view;

/**
 *
 * @author admin
 */

import com.sortbattle.client.controller.ClientController;
import com.sortbattle.common.GameConfig;
import com.sortbattle.common.Player;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LobbyView extends JFrame {
    private final ClientController controller;
    private final JLabel welcomeLabel;
    private final JList<Player> playerList;
    private final DefaultListModel<Player> playerListModel;
    private final JButton challengeButton;

    public LobbyView(ClientController controller) {
        this.controller = controller;

        setTitle("Sảnh chờ - Sort Battle Online");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel phía trên (Welcome)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomeLabel = new JLabel("Chào mừng!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Panel trung tâm (Danh sách người chơi)
        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.setFont(new Font("Arial", Font.PLAIN, 14));
        // Custom renderer để hiển thị màu sắc theo trạng thái
        playerList.setCellRenderer(new PlayerListRenderer());
        JScrollPane scrollPane = new JScrollPane(playerList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel phía dưới (Nút thách đấu)
        JPanel bottomPanel = new JPanel();
        challengeButton = new JButton("Thách đấu");
        challengeButton.setFont(new Font("Arial", Font.BOLD, 14));
        challengeButton.addActionListener(e -> {
            Player selectedPlayer = playerList.getSelectedValue();
            if (selectedPlayer != null) {
                if (selectedPlayer.getStatus() == Player.PlayerStatus.IDLE) {
                    controller.challengePlayer(selectedPlayer.getUsername());
                } else {
                    JOptionPane.showMessageDialog(this, "Người chơi này đang bận!", "Không thể thách đấu", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người chơi để thách đấu.", "Chưa chọn đối thủ", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomPanel.add(challengeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        
        JButton leaderboardButton = new JButton("🏆 Bảng xếp hạng");
leaderboardButton.setFont(new Font("Arial", Font.BOLD, 12));
leaderboardButton.addActionListener(e -> controller.requestLeaderboard());
bottomPanel.add(leaderboardButton);

JButton historyButton = new JButton("📜 Lịch sử");
historyButton.setFont(new Font("Arial", Font.BOLD, 12));
historyButton.addActionListener(e -> controller.requestHistory());
bottomPanel.add(historyButton);
        
        add(mainPanel);
    }
    
    public void updateWelcomeMessage(String username) {
        welcomeLabel.setText("Chào mừng, " + username + "!");
    }
    
    public void updatePlayerList(List<Player> players, String currentPlayerUsername) {
    playerListModel.clear();
    for (Player player : players) {
        if (!player.getUsername().equals(currentPlayerUsername)) {
            playerListModel.addElement(player);
        }
    }
}
    
    public void showGameConfigDialog(String opponentName) {
        // Tạo dialog
        JDialog configDialog = new JDialog(this, "Cấu hình trận đấu với " + opponentName, true);
        configDialog.setSize(400, 350);
        configDialog.setLocationRelativeTo(this);
        configDialog.setLayout(new GridLayout(6, 2, 10, 10));
        configDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        GameConfig config = new GameConfig(); // Lấy config mặc định

        // 1. Loại nội dung
        configDialog.add(new JLabel("Loại nội dung:"));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Từ", "Số"});
        typeCombo.addActionListener(e -> config.setDataType("Số".equals(typeCombo.getSelectedItem()) ? GameConfig.DataType.NUMBER : GameConfig.DataType.WORD));
        configDialog.add(typeCombo);

        // 2. Số lượng/Khoảng số
        configDialog.add(new JLabel("Số lượng/Khoảng:"));
        JComboBox<Integer> countCombo = new JComboBox<>(new Integer[]{10, 20, 30, 40, 50, 60, 80, 100});
        countCombo.setSelectedItem(30); // Default for words
        typeCombo.addActionListener(e -> {
             if ("Số".equals(typeCombo.getSelectedItem())) {
                 countCombo.setModel(new DefaultComboBoxModel<>(new Integer[]{20, 40, 60, 80, 100}));
                 countCombo.setSelectedItem(60);
             } else {
                 countCombo.setModel(new DefaultComboBoxModel<>(new Integer[]{10, 20, 30, 40, 50}));
                 countCombo.setSelectedItem(30);
             }
        });
        configDialog.add(countCombo);

        // 3. Thời gian
        configDialog.add(new JLabel("Thời gian (giây):"));
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(90, 30, 300, 5));
        configDialog.add(timeSpinner);

        // 4. Gợi ý
        configDialog.add(new JLabel("Khung gợi ý:"));
        JCheckBox hintCheckbox = new JCheckBox("Bật", true);
        configDialog.add(hintCheckbox);
        
        configDialog.add(new JLabel()); // Ô trống
        
        // 5. Nút Bắt đầu
        JButton startButton = new JButton("Bắt đầu");
        startButton.addActionListener(e -> {
            config.setItemCount((Integer) countCombo.getSelectedItem());
            config.setTimeLimitSeconds((Integer) timeSpinner.getValue());
            config.setHintsEnabled(hintCheckbox.isSelected());
            controller.submitGameConfig(config);
            configDialog.dispose();
        });
        configDialog.add(startButton);

        configDialog.setVisible(true);
    }
    
    // Custom renderer class
    private static class PlayerListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Player) {
                Player player = (Player) value;
                setText(player.toString());
                if (player.getStatus() == Player.PlayerStatus.BUSY) {
                    setForeground(Color.GRAY);
                } else {
                    setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }
}
