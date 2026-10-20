package com.showtime.client;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/** Small Swing helpers: colours, background work, dialogs. */
public final class Ui {

    public static final Color BRAND = new Color(0xD3, 0x2F, 0x2F);
    public static final Color DARK = new Color(0x1F, 0x24, 0x30);
    public static final Color AVAILABLE = new Color(0xE8, 0xF5, 0xE9);
    public static final Color SELECTED = new Color(0xFF, 0xB3, 0x00);
    public static final Color BOOKED = new Color(0x9E, 0x9E, 0x9E);

    private Ui() {}

    public interface Work<T> {
        T run() throws Exception;
    }

    /** Runs {@code work} on a background thread, then calls ok/fail on the Swing thread. */
    public static <T> void async(Work<T> work, Consumer<T> ok, Consumer<Exception> fail) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.run();
            }

            @Override
            protected void done() {
                try {
                    ok.accept(get());
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable c = e.getCause();
                    fail.accept(c instanceof Exception ex ? ex : new Exception(c));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    public static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "ShowTime", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "ShowTime", JOptionPane.INFORMATION_MESSAGE);
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(BRAND);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return b;
    }

    public static JLabel title(String text, float size) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, size));
        return l;
    }
}
