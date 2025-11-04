// File: D:\admin\Nam4\LTM\ltmproject\src\com\sortbattle\client\view\LoginView.java

package com.sortbattle.client.view;

import com.sortbattle.client.controller.ClientController;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private final ClientController controller;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginView(ClientController controller) {
        this.controller = controller;

        setTitle("Sort Battle - Đăng nhập");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("SORT BATTLE GAME");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(51, 102, 255));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Đăng nhập để bắt đầu");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(18);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(18);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(passwordField, gbc);

        // Login Button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton loginButton = new JButton("ĐĂNG NHẬP");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(51, 153, 102));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(200, 35));
        loginButton.addActionListener(e -> login());
        panel.add(loginButton, gbc);

        // Register Button
        gbc.gridy = 5;
        JButton registerButton = new JButton("ĐĂNG KÝ TÀI KHOẢN MỚI");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));
        registerButton.setBackground(new Color(255, 153, 51));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(200, 30));
        registerButton.addActionListener((e) -> {
         // Tạo và hiển thị dialog đăng ký
         RegisterDialog dialog = new RegisterDialog(this, controller);
         controller.setActiveDialog(dialog); // Báo cho controller biết dialog nào đang hoạt động
         dialog.setVisible(true);
         controller.setActiveDialog(null); // Dọn dẹp sau khi dialog đóng
      });
      panel.add(registerButton, gbc);

      this.add(panel);
        
        // Enter để login
        passwordField.addActionListener(e -> login());
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập đầy đủ thông tin!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        controller.attemptLogin(username, password);
    }
    
    // private void register() {
    //     String username = usernameField.getText().trim();
    //     String password = new String(passwordField.getPassword()).trim();
        
    //     if (username.isEmpty() || password.isEmpty()) {
    //         JOptionPane.showMessageDialog(this, 
    //             "Vui lòng nhập đầy đủ thông tin!", 
    //             "Lỗi", 
    //             JOptionPane.ERROR_MESSAGE);
    //         return;
    //     }
        
    //     if (username.length() < 3) {
    //         JOptionPane.showMessageDialog(this, 
    //             "Username phải có ít nhất 3 ký tự!", 
    //             "Lỗi", 
    //             JOptionPane.ERROR_MESSAGE);
    //         return;
    //     }
        
    //     if (password.length() < 6) {
    //         JOptionPane.showMessageDialog(this, 
    //             "Mật khẩu phải có ít nhất 6 ký tự!", 
    //             "Lỗi", 
    //             JOptionPane.ERROR_MESSAGE);
    //         return;
    //     }
        
    //     // Confirm password
    //     String confirmPass = JOptionPane.showInputDialog(this, "Xác nhận mật khẩu:");
    //     if (confirmPass == null || !confirmPass.equals(password)) {
    //         JOptionPane.showMessageDialog(this, 
    //             "Mật khẩu xác nhận không khớp!", 
    //             "Lỗi", 
    //             JOptionPane.ERROR_MESSAGE);
    //         return;
    //     }
        
    //     controller.attemptRegister(username, password);
    // }
}