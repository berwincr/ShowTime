package com.showtime.client;

import javax.swing.*;

/** Entry point of the ShowTime desktop client. */
public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // default look and feel is fine
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
