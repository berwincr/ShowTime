package com.showtime.client;

import java.awt.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;

/** Formatting + browser helpers. */
public final class Fmt {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private Fmt() {}

    public static String dateTime(String iso) {
        try {
            return LocalDateTime.parse(iso).format(DATE_TIME);
        } catch (RuntimeException e) {
            return iso;
        }
    }

    public static String time(String iso) {
        try {
            return LocalTime.parse(iso).format(TIME);
        } catch (RuntimeException e) {
            return iso;
        }
    }

    public static String rupees(long amount) {
        return "Rs. " + amount;
    }

    /** Opens the URL in the default browser; if that is not possible, shows the link so it can be copied. */
    public static void openBrowser(Component parent, String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception ignored) {
            // fall through to the manual option
        }
        JTextField field = new JTextField(url, 40);
        field.setEditable(false);
        JOptionPane.showMessageDialog(parent,
                new Object[]{"Could not open the browser automatically. Copy this link into your browser:", field},
                "Open payment page", JOptionPane.INFORMATION_MESSAGE);
    }
}
