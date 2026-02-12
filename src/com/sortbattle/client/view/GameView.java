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
import com.sortbattle.client.model.ClientModel;
import com.sortbattle.common.GameConfig;
import com.sortbattle.common.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.*;

public class GameView extends JFrame {
    private final ClientController controller;
    private final JLabel timerLabel;
    private final JLabel player1ScoreLabel;
    private final JLabel player2ScoreLabel;
    private final JLabel hintLabel;
    private final JLabel sortOrderLabel;
    private final JPanel itemsPanel;
    private final Map<String, JButton> itemButtons = new HashMap<>();
    private JDialog waitingDialog;
    private JPanel player1Panel;
    private JPanel player2Panel;

    public GameView(ClientController controller) {
        this.controller = controller;

        setTitle("🎮 SORT BATTLE - TRẬN ĐẤU");
        setSize(900, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // ========== PANEL CHÍNH ==========
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ========== HEADER - TIMER VÀ THÔNG TIN ==========
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        
        // Timer với viền nổi bật
        JPanel timerContainer = new JPanel();
        timerContainer.setBackground(new Color(70, 130, 180));
        timerContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(25, 25, 112), 3),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        timerLabel = new JLabel("Thời gian: 90s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        timerLabel.setForeground(Color.WHITE);
        timerContainer.add(timerLabel);
        
        // Yêu cầu sắp xếp
        sortOrderLabel = new JLabel("Sắp xếp: TĂNG DẦN", SwingConstants.CENTER);
        sortOrderLabel.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 18));
        sortOrderLabel.setForeground(new Color(220, 20, 60));
        sortOrderLabel.setBorder(new EmptyBorder(8, 0, 5, 0));
        sortOrderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(timerContainer);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(sortOrderLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ========== SCORE PANEL - ĐIỂM SỐ 2 NGƯỜI CHƠI ==========
        JPanel scorePanel = new JPanel(new GridLayout(1, 2, 20, 0));
        scorePanel.setOpaque(false);
        scorePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Player 1 (Bạn)
        player1Panel = createPlayerPanel(true);
        player1ScoreLabel = new JLabel("Player1: 0", SwingConstants.CENTER);
        player1ScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        player1ScoreLabel.setForeground(Color.WHITE);
        player1Panel.add(player1ScoreLabel);
        
        // Player 2 (Đối thủ)
        player2Panel = createPlayerPanel(false);
        player2ScoreLabel = new JLabel("Player2: 0", SwingConstants.CENTER);
        player2ScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        player2ScoreLabel.setForeground(Color.WHITE);
        player2Panel.add(player2ScoreLabel);
        
        scorePanel.add(player1Panel);
        scorePanel.add(player2Panel);

        // ========== HINT PANEL - GỢI Ý ==========
        JPanel hintPanel = new JPanel();
        hintPanel.setBackground(new Color(255, 250, 205));
        hintPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        hintLabel = new JLabel("Gợi ý: ...", SwingConstants.CENTER);
        hintLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hintLabel.setForeground(new Color(184, 134, 11));
        hintPanel.add(hintLabel);

        // ========== CENTER PANEL - KẾT HỢP SCORE + HINT + ITEMS ==========
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setOpaque(false);
        
        JPanel topSection = new JPanel(new BorderLayout(10, 10));
        topSection.setOpaque(false);
        topSection.add(scorePanel, BorderLayout.NORTH);
        topSection.add(hintPanel, BorderLayout.SOUTH);
        
        centerContainer.add(topSection, BorderLayout.NORTH);
        
        // Items Panel
        itemsPanel = new JPanel();
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(176, 196, 222), 2),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // ========== BOTTOM PANEL - NÚT THOÁT ==========
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton exitButton = new JButton("Thoát trận");
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        exitButton.setBackground(new Color(220, 20, 60));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setBorderPainted(false);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitButton.setBorder(new EmptyBorder(12, 40, 12, 40));
        
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exitButton.setBackground(new Color(255, 69, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exitButton.setBackground(new Color(220, 20, 60));
            }
        });
        
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn thoát? Bạn sẽ bị trừ điểm.",
                "Xác nhận thoát", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                controller.exitGame();
            }
        });
        
        bottomPanel.add(exitButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);

        // Auto-adjust grid columns
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                adjustGridColumns();
            }
        });
    }
    
    // ========== TẠO PANEL ĐIỂM SỐ CHO TỪNG NGƯỜI CHƠI ==========
    private JPanel createPlayerPanel(boolean isPlayer1) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Gradient background
        if (isPlayer1) {
            panel.setBackground(new Color(34, 139, 34)); // Green
        } else {
            panel.setBackground(new Color(220, 20, 60)); // Red
        }
        
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isPlayer1 ? new Color(0, 100, 0) : new Color(139, 0, 0), 3),
            new EmptyBorder(15, 10, 15, 10)
        ));
        
        // Label "Bạn" hoặc "Đối thủ"
        JLabel roleLabel = new JLabel(isPlayer1 ? "BẠN" : "ĐỐI THỦ", SwingConstants.CENTER);
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(roleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        return panel;
    }
    
    // public void initializeGame(ClientModel model) {
    //     GameConfig config = model.getCurrentGameConfig();
        
    //     // Cập nhật thông tin
    //     timerLabel.setText("⏱ Thời gian: " + config.getTimeLimitSeconds() + "s");
    //     player1ScoreLabel.setText(model.getCurrentPlayer().getUsername() + ": 0");
    //     player2ScoreLabel.setText(model.getOpponent().getUsername() + ": 0");
        
    //     String orderText = config.getSortOrder() == GameConfig.SortOrder.ASCENDING ? "TĂNG DẦN ▲" : "GIẢM DẦN ▼";
    //     sortOrderLabel.setText("🎯 Yêu cầu: Sắp xếp " + orderText);
    //     hintLabel.setVisible(config.isHintsEnabled());
        
    //     // Tạo các nút item với style đẹp
    //     itemsPanel.removeAll();
    //     itemButtons.clear();
        
    //     for (String item : model.getGameItems()) {
    //         JButton itemButton = new JButton(item);
    //         styleItemButton(itemButton);
    //         itemButton.addActionListener(e -> controller.onItemClick(item));
    //         itemsPanel.add(itemButton);
    //         itemButtons.put(item, itemButton);
    //     }

    //     adjustGridColumns();
    //     itemsPanel.revalidate();
    //     itemsPanel.repaint();
    // }

    // ========== STYLE CHO NÚT ITEM ==========
    // private void styleItemButton(JButton button) {
    //     button.setFont(new Font("Segoe UI", Font.BOLD, 18));
    //     button.setBackground(new Color(100, 149, 237));
    //     button.setForeground(Color.WHITE);
    //     button.setFocusPainted(false);
    //     button.setBorderPainted(false);
    //     button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    //     button.setMargin(new Insets(12, 15, 12, 15));
    //     button.setBorder(BorderFactory.createCompoundBorder(
    //         BorderFactory.createLineBorder(new Color(65, 105, 225), 2),
    //         new EmptyBorder(8, 12, 8, 12)
    //     ));
        
    //     // Hover effect
    //     button.addMouseListener(new java.awt.event.MouseAdapter() {
    //         public void mouseEntered(java.awt.event.MouseEvent evt) {
    //             if (button.isEnabled()) {
    //                 button.setBackground(new Color(65, 105, 225));
    //             }
    //         }
    //         public void mouseExited(java.awt.event.MouseEvent evt) {
    //             if (button.isEnabled()) {
    //                 button.setBackground(new Color(100, 149, 237));
    //             }
    //         }
    //     });
    // }

    // ...existing code...

private void adjustGridColumns() {
    if (itemButtons.isEmpty()) return;

    // Lấy kích thước thực tế của container chứa items
    int containerWidth = itemsPanel.getWidth();
    int containerHeight = itemsPanel.getParent().getHeight(); // Chiều cao của scrollpane
    
    if (containerWidth <= 0) {
        containerWidth = 850; // fallback width
    }
    if (containerHeight <= 0) {
        containerHeight = 400; // fallback height
    }

    // Kích thước cố định cho mỗi button
    int buttonWidth = 100;  // Chiều rộng cố định
    int buttonHeight = 50;  // Chiều cao cố định
    int hgap = 8;
    int vgap = 8;

    // Tính số cột tối đa dựa trên chiều rộng container
    int maxColumns = Math.max(1, (containerWidth - 20) / (buttonWidth + hgap));
    
    // Tính số hàng cần thiết
    int totalItems = itemButtons.size();
    int rows = (int) Math.ceil((double) totalItems / maxColumns);
    
    // Kiểm tra nếu số hàng quá nhiều, giảm số cột để cân đối
    int maxRows = Math.max(1, (containerHeight - 20) / (buttonHeight + vgap));
    
    int columns;
    if (rows > maxRows) {
        // Cần nhiều cột hơn để vừa màn hình theo chiều dọc
        columns = (int) Math.ceil((double) totalItems / maxRows);
        columns = Math.min(columns, maxColumns); // Không vượt quá maxColumns
        rows = (int) Math.ceil((double) totalItems / columns);
    } else {
        columns = maxColumns;
    }
    
    // Cập nhật layout chỉ khi cần thiết
    LayoutManager current = itemsPanel.getLayout();
    boolean needSet = true;
    
    if (current instanceof GridLayout) {
        GridLayout gl = (GridLayout) current;
        if (gl.getColumns() == columns && gl.getHgap() == hgap && gl.getVgap() == vgap) {
            needSet = false;
        }
    }
    
    if (needSet) {
        itemsPanel.setLayout(new GridLayout(rows, columns, hgap, vgap));
        
        // Đặt kích thước cố định cho tất cả buttons
        for (JButton button : itemButtons.values()) {
            button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
            button.setMinimumSize(new Dimension(buttonWidth, buttonHeight));
            button.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        }
        
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }
}

// ...existing code...

private void styleItemButton(JButton button) {
    button.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Giảm size font
    button.setBackground(new Color(100, 149, 237));
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    // Đặt kích thước cố định
    button.setPreferredSize(new Dimension(100, 50));
    button.setMinimumSize(new Dimension(100, 50));
    button.setMaximumSize(new Dimension(100, 50));
    
    button.setMargin(new Insets(5, 5, 5, 5));
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(65, 105, 225), 2),
        new EmptyBorder(5, 8, 5, 8)
    ));
    
    // Hover effect
    button.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            if (button.isEnabled()) {
                button.setBackground(new Color(65, 105, 225));
            }
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            if (button.isEnabled()) {
                button.setBackground(new Color(100, 149, 237));
            }
        }
    });
}

// ...existing code...

public void initializeGame(ClientModel model) {
    GameConfig config = model.getCurrentGameConfig();
    
    // Cập nhật thông tin
    timerLabel.setText("⏱ Thời gian: " + config.getTimeLimitSeconds() + "s");
    player1ScoreLabel.setText(model.getCurrentPlayer().getUsername() + ": 0");
    player2ScoreLabel.setText(model.getOpponent().getUsername() + ": 0");
    
    String orderText = config.getSortOrder() == GameConfig.SortOrder.ASCENDING ? "TĂNG DẦN ▲" : "GIẢM DẦN ▼";
    sortOrderLabel.setText("🎯 Yêu cầu: Sắp xếp " + orderText);
    hintLabel.setVisible(config.isHintsEnabled());
    
    // Tạo các nút item với style đẹp
    itemsPanel.removeAll();
    itemButtons.clear();
    
    for (String item : model.getGameItems()) {
        JButton itemButton = new JButton(item);
        styleItemButton(itemButton);
        itemButton.addActionListener(e -> controller.onItemClick(item));
        itemsPanel.add(itemButton);
        itemButtons.put(item, itemButton);
    }

    // Đợi panel được render xong mới adjust
    SwingUtilities.invokeLater(() -> {
        adjustGridColumns();
        itemsPanel.revalidate();
        itemsPanel.repaint();
    });
}

// ...existing code...
    
    public void updateState(Map<String, Object> payload) {
        // Cập nhật thời gian với màu cảnh báo
        Object timeObj = payload.get("timeLeft");
        if (timeObj instanceof Number) {
            int timeLeft = ((Number) timeObj).intValue();
            timerLabel.setText("⏱ Thời gian: " + timeLeft + "s");
            
            // Đổi màu khi còn ít thời gian
            if (timeLeft <= 10) {
                timerLabel.setForeground(new Color(255, 69, 0));
            } else if (timeLeft <= 30) {
                timerLabel.setForeground(new Color(255, 215, 0));
            } else {
                timerLabel.setForeground(Color.WHITE);
            }
        }

        // Cập nhật điểm
        Map<String, Integer> scores = (Map<String, Integer>) payload.get("scores");
        if (scores != null) {
            String p1Text = player1ScoreLabel.getText();
            String p2Text = player2ScoreLabel.getText();
            
            scores.forEach((username, score) -> {
                if (p1Text.startsWith(username)) {
                    player1ScoreLabel.setText(username + ": " + score);
                } else if (p2Text.startsWith(username)) {
                    player2ScoreLabel.setText(username + ": " + score);
                }
            });
        }
        
        // Cập nhật nút đã click
        Object clickedObj = payload.get("clickedItem");
        if (clickedObj instanceof String) {
            String clickedItem = (String) clickedObj;
            if (clickedItem != null && itemButtons.containsKey(clickedItem)) {
                JButton button = itemButtons.get(clickedItem);
                button.setEnabled(false);
                button.setBackground(new Color(169, 169, 169));
                button.setForeground(new Color(105, 105, 105));
                button.setText("✓ " + clickedItem);
            }
        }

        // Cập nhật gợi ý
        Object nextHintObj = payload.get("nextHint");
        if (nextHintObj instanceof String) {
            hintLabel.setText("💡 Gợi ý: " + nextHintObj);
        } else {
            hintLabel.setText("💡 Gợi ý: ...");
        }
    }
    
    public void showEndGameDialog(Map<String, Object> payload) {
        String reason = (String) payload.get("reason");
        String winner = (String) payload.get("winner");
        Player p1 = (Player) payload.get("finalPlayer1");
        Player p2 = (Player) payload.get("finalPlayer2");
        Map<String, Integer> scores = (Map<String, Integer>) payload.get("scores");

        // Tạo dialog đẹp hơn
        JDialog resultDialog = new JDialog(this, "🏆 KẾT QUẢ TRẬN ĐẤU", true);
        resultDialog.setSize(500, 400);
        resultDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Title
        JLabel titleLabel = new JLabel("TRẬN ĐẤU KẾT THÚC!", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // Reason
        JLabel reasonLabel = new JLabel("Lý do: " + reason, JLabel.CENTER);
        reasonLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        reasonLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(reasonLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Scores
        JLabel scoreTitle = new JLabel("KẾT QUẢ:", JLabel.CENTER);
        scoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(scoreTitle);
        panel.add(Box.createVerticalStrut(10));
        
        JLabel p1Score = new JLabel(p1.getUsername() + ": " + scores.get(p1.getUsername()) + " điểm (Tổng: " + p1.getTotalScore() + ")", JLabel.CENTER);
        p1Score.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        p1Score.setForeground(new Color(34, 139, 34));
        p1Score.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(p1Score);
        panel.add(Box.createVerticalStrut(5));
        
        JLabel p2Score = new JLabel(p2.getUsername() + ": " + scores.get(p2.getUsername()) + " điểm (Tổng: " + p2.getTotalScore() + ")", JLabel.CENTER);
        p2Score.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        p2Score.setForeground(new Color(220, 20, 60));
        p2Score.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(p2Score);
        panel.add(Box.createVerticalStrut(20));
        
        // Winner
        String resultText = winner != null ? "🏆 " + winner + " CHIẾN THẮNG!" : "🤝 KẾT QUẢ HÒA!";
        JLabel winnerLabel = new JLabel(resultText, JLabel.CENTER);
        winnerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        winnerLabel.setForeground(winner != null ? new Color(255, 215, 0) : new Color(128, 128, 128));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(winnerLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Close button
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> resultDialog.dispose());
        panel.add(closeButton);
        
        resultDialog.add(new JScrollPane(panel));
        resultDialog.setVisible(true);
    }
    
    public void showWaitingForRematch() {
        if (waitingDialog != null && waitingDialog.isVisible()) {
            waitingDialog.dispose();
        }
        
        waitingDialog = new JDialog(this, "⏳ Chờ đối thủ", false);
        waitingDialog.setLayout(new BorderLayout(10, 10));
        waitingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel messageLabel = new JLabel("<html><center>Đã gửi yêu cầu chơi lại.<br>Vui lòng chờ đối thủ phản hồi...</center></html>", JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(new Color(70, 130, 180));
        contentPanel.add(progressBar, BorderLayout.SOUTH);
        
        waitingDialog.add(contentPanel);
        waitingDialog.pack();
        waitingDialog.setSize(380, 160);
        waitingDialog.setLocationRelativeTo(this);
        waitingDialog.setVisible(true);
    }

    public void showConfigDialog(Consumer<GameConfig> onConfigSelected) {
        JDialog configDialog = new JDialog(this, "⚙ Cấu hình trận đấu mới", true);
        configDialog.setSize(450, 400);
        configDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 248, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        GameConfig config = new GameConfig();

        // 1. Loại nội dung
        JLabel typeLabel = new JLabel("Loại nội dung:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridwidth = 1;
        mainPanel.add(typeLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Từ", "Số"});
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeCombo.addActionListener(e -> config.setDataType(
            "Số".equals(typeCombo.getSelectedItem()) ? GameConfig.DataType.NUMBER : GameConfig.DataType.WORD));
        mainPanel.add(typeCombo, gbc);

        // 2. Số lượng
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel countLabel = new JLabel("Số lượng:");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(countLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<Integer> countCombo = new JComboBox<>(new Integer[]{10, 20, 30, 40, 50});
        countCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countCombo.setSelectedItem(30);
        typeCombo.addActionListener(e -> {
            if ("Số".equals(typeCombo.getSelectedItem())) {
                countCombo.setModel(new DefaultComboBoxModel<>(new Integer[]{20, 40, 60, 80, 100}));
                countCombo.setSelectedItem(60);
            } else {
                countCombo.setModel(new DefaultComboBoxModel<>(new Integer[]{10, 20, 30, 40, 50}));
                countCombo.setSelectedItem(30);
            }
        });
        mainPanel.add(countCombo, gbc);

        // 3. Thời gian
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel timeLabel = new JLabel("Thời gian (giây):");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(timeLabel, gbc);
        
        gbc.gridx = 1;
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(90, 30, 300, 5));
        timeSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(timeSpinner, gbc);

        // 4. Gợi ý
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel hintLabelConfig = new JLabel("Khung gợi ý:");
        hintLabelConfig.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(hintLabelConfig, gbc);
        
        gbc.gridx = 1;
        JCheckBox hintCheckbox = new JCheckBox("Bật", true);
        hintCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hintCheckbox.setOpaque(false);
        mainPanel.add(hintCheckbox, gbc);

        // 5. Nút Bắt đầu
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JButton startButton = new JButton("🎮 BẮT ĐẦU");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startButton.setBackground(new Color(34, 139, 34));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.setBorder(new EmptyBorder(12, 40, 12, 40));
        startButton.addActionListener(e -> {
            config.setItemCount((Integer) countCombo.getSelectedItem());
            config.setTimeLimitSeconds((Integer) timeSpinner.getValue());
            config.setHintsEnabled(hintCheckbox.isSelected());
            configDialog.dispose();
            onConfigSelected.accept(config);
        });
        mainPanel.add(startButton, gbc);

        configDialog.add(mainPanel);
        configDialog.setVisible(true);
    }

    public void hideWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isVisible()) {
            waitingDialog.dispose();
            waitingDialog = null;
        }
    }

    public void forceDispose() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof JDialog && window.getOwner() == this) {
                window.dispose();
            }
        }
        this.dispose();
    }
}