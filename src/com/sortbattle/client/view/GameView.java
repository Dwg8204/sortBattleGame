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
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameView extends JFrame {
    private final ClientController controller;
    private final JLabel timerLabel;
    private final JLabel player1ScoreLabel;
    private final JLabel player2ScoreLabel;
    private final JLabel hintLabel;
    private final JLabel sortOrderLabel;
    private final JPanel itemsPanel;
    private final Map<String, JButton> itemButtons = new HashMap<>();

    public GameView(ClientController controller) {
        this.controller = controller;

        setTitle("Sort Battle - Trong trận");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Ngăn đóng cửa sổ bằng nút X
        setLocationRelativeTo(null);
        
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel Header (Timer và thông tin sắp xếp)
        JPanel headerPanel = new JPanel(new BorderLayout());
        timerLabel = new JLabel("Thời gian: 90s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(timerLabel, BorderLayout.CENTER);
        
        sortOrderLabel = new JLabel("Sắp xếp: TĂNG DẦN", SwingConstants.CENTER);
        sortOrderLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        headerPanel.add(sortOrderLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel Điểm số
        JPanel scorePanel = new JPanel(new GridLayout(1, 2, 20, 0));
        scorePanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        player1ScoreLabel = new JLabel("Player1: 0", SwingConstants.CENTER);
        player1ScoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        player1ScoreLabel.setBorder(BorderFactory.createTitledBorder(null, "Bạn", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.BLUE));

        player2ScoreLabel = new JLabel("Player2: 0", SwingConstants.CENTER);
        player2ScoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        player2ScoreLabel.setBorder(BorderFactory.createTitledBorder(null, "Đối thủ", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.RED));
        scorePanel.add(player1ScoreLabel);
        scorePanel.add(player2ScoreLabel);
        
        // Panel chứa điểm số và gợi ý
        JPanel statusPanel = new JPanel(new BorderLayout(10,10));
        statusPanel.add(scorePanel, BorderLayout.CENTER);
        hintLabel = new JLabel("Gợi ý: ...", SwingConstants.CENTER);
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        statusPanel.add(hintLabel, BorderLayout.SOUTH);
        
        // Panel dưới cùng
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusPanel, BorderLayout.CENTER);
        JButton exitButton = new JButton("Thoát trận");
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn thoát? Bạn sẽ bị trừ điểm.",
                "Xác nhận thoát", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                controller.exitGame();
            }
        });
        bottomPanel.add(exitButton, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Panel chứa các item - Sử dụng layout động
        itemsPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);

        // Khi thay đổi kích thước cửa sổ, điều chỉnh số cột trong grid để tự wrap các nút
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                adjustGridColumns();
            }
        });
    }
    
    public void initializeGame(ClientModel model) {
        GameConfig config = model.getCurrentGameConfig();
        
        // Cập nhật thông tin ban đầu
        timerLabel.setText("Thời gian: " + config.getTimeLimitSeconds() + "s");
        player1ScoreLabel.setText(model.getCurrentPlayer().getUsername() + ": 0");
        player2ScoreLabel.setText(model.getOpponent().getUsername() + ": 0");
        sortOrderLabel.setText("Yêu cầu: Sắp xếp " + (config.getSortOrder() == GameConfig.SortOrder.ASCENDING ? "TĂNG DẦN" : "GIẢM DẦN"));
        hintLabel.setVisible(config.isHintsEnabled());
        
        // Tạo các nút item
        itemsPanel.removeAll();
        itemButtons.clear();
        for (String item : model.getGameItems()) {
            JButton itemButton = new JButton(item);
            itemButton.setFont(new Font("Arial", Font.BOLD, 16));
            itemButton.setMargin(new Insets(10, 10, 10, 10));
            itemButton.addActionListener(e -> controller.onItemClick(item));
            itemsPanel.add(itemButton);
            itemButtons.put(item, itemButton);
        }

        // Gán layout dạng Grid với số cột tính toán tự động
        adjustGridColumns();

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    // Tính số cột dựa trên chiều rộng hiện tại và kích thước nút để wrap tự động
    private void adjustGridColumns() {
        if (itemButtons.isEmpty()) return;

        int containerWidth = itemsPanel.getParent() != null ? itemsPanel.getParent().getWidth() : this.getWidth();
        if (containerWidth <= 0) containerWidth = 700; // fallback

        // Ước lượng chiều rộng nút: lấy preferred width của một nút mẫu
        JButton sample = itemButtons.values().iterator().next();
        int btnWidth = sample.getPreferredSize().width;
        int hgap = 5;

        int columns = Math.max(1, containerWidth / (btnWidth + hgap));
        
        // Đặt layout mới chỉ khi khác để tránh revalidate thừa
        LayoutManager current = itemsPanel.getLayout();
        boolean needSet = true;
        if (current instanceof GridLayout) {
            GridLayout gl = (GridLayout) current;
            if (gl.getColumns() == columns) needSet = false;
        }
        if (needSet) {
            itemsPanel.setLayout(new GridLayout(0, columns, hgap, 5));
            itemsPanel.revalidate();
            itemsPanel.repaint();
        }
    }
    
    public void updateState(Map<String, Object> payload) {
        // Bỏ SwingUtilities.invokeLater ở đây vì đã được gọi từ ClientController
        
        // Cập nhật thời gian
        Object timeObj = payload.get("timeLeft");
        if (timeObj instanceof Number) {
            int timeLeft = ((Number) timeObj).intValue();
            timerLabel.setText("Thời gian: " + timeLeft + "s");
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
        
        // Cập nhật nút đã được click (nếu có)
        Object clickedObj = payload.get("clickedItem");
        if (clickedObj instanceof String) {
            String clickedItem = (String) clickedObj;
            if (clickedItem != null && itemButtons.containsKey(clickedItem)) {
                JButton button = itemButtons.get(clickedItem);
                button.setEnabled(false);
                button.setBackground(Color.LIGHT_GRAY);
            }
        }

        // Cập nhật gợi ý (an toàn với null)
        Object nextHintObj = payload.get("nextHint");
        if (nextHintObj instanceof String) {
            hintLabel.setText("Gợi ý: " + nextHintObj);
        } else {
            hintLabel.setText("Gợi ý: ...");
        }
    }
    
    public void showEndGameDialog(Map<String, Object> payload) {
        String reason = (String) payload.get("reason");
        String winner = (String) payload.get("winner");
        Player p1 = (Player) payload.get("finalPlayer1");
        Player p2 = (Player) payload.get("finalPlayer2");
        Map<String, Integer> scores = (Map<String, Integer>) payload.get("scores");

        StringBuilder message = new StringBuilder("<html><h2>Trận đấu kết thúc!</h2>");
        message.append("<p>Lý do: ").append(reason).append("</p>");
        message.append("<h3>Kết quả:</h3>");
        message.append("<p><b>").append(p1.getUsername()).append(":</b> ").append(scores.get(p1.getUsername())).append(" điểm (Tổng: ").append(p1.getTotalScore()).append(")</p>");
        message.append("<p><b>").append(p2.getUsername()).append(":</b> ").append(scores.get(p2.getUsername())).append(" điểm (Tổng: ").append(p2.getTotalScore()).append(")</p>");

        if (winner != null) {
            message.append("<h3>=> ").append(winner).append(" chiến thắng!</h3>");
        } else {
            message.append("<h3>=> Kết quả HÒA!</h3>");
        }
        message.append("</html>");

        JOptionPane.showMessageDialog(this, message, "Kết quả trận đấu", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showWaitingForRematch() {
        // Vô hiệu hóa cửa sổ game và hiển thị thông báo chờ
        for(JButton button : itemButtons.values()) {
            button.setEnabled(false);
        }
        JOptionPane.showMessageDialog(this, "Đã gửi yêu cầu chơi lại. Vui lòng chờ đối thủ...", "Chờ", JOptionPane.INFORMATION_MESSAGE);
    }
}