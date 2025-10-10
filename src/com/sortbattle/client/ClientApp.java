/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sortbattle.client;

/**
 *
 * @author admin
 */

import com.sortbattle.client.controller.ClientController;
import javax.swing.SwingUtilities;

public class ClientApp {
    public static void main(String[] args) {
        // Chạy giao diện trên Event Dispatch Thread của Swing
        SwingUtilities.invokeLater(() -> {
            ClientController controller = new ClientController();
            controller.showLoginView();
        });
    }
}

