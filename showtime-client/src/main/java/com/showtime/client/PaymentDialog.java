package com.showtime.client;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Module 4: waits while the customer pays in the browser (Razorpay Checkout).
 * Every 2 seconds it asks the backend for the booking status. The backend only marks the booking
 * CONFIRMED after verifying the Razorpay signature, so this client never decides if a payment worked.
 */
public class PaymentDialog extends JDialog {

    static { Theme.install(); }

    private final long bookingId;
    private final String payUrl;
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "payment-poller");
        t.setDaemon(true);
        return t;
    });

    private String result;                 // CONFIRMED, CANCELLED, EXPIRED, ...
    private Map<String, Object> booking;   // last booking details from the server

    public PaymentDialog(Window owner, long bookingId, String payUrl) {
        super(owner, "Complete your payment", ModalityType.APPLICATION_MODAL);
        this.bookingId = bookingId;
        this.payUrl = payUrl;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        // ---- Header strip ----
        JLabel heading = new JLabel("Waiting for payment...");
        heading.setFont(Theme.font(Font.BOLD, 22f));
        heading.setForeground(Color.WHITE);
        JLabel sub = Theme.muted("Complete the payment in your browser", 13f);

        JPanel header = new Theme.Header();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 24, 18, 24));
        heading.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        header.add(heading);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // ---- Instructions ----
        JLabel info = new JLabel("<html><body style='width:380px'>"
                + "The Razorpay payment page has opened in your browser. "
                + "Complete the payment there and this window will update automatically."
                + "</body></html>");
        info.setFont(Theme.font(Font.PLAIN, 14f));
        info.setForeground(Theme.TEXT);

        // ---- Test-mode hint box ----
        JLabel hint = new JLabel("<html><body style='width:360px'>"
                + "<b>Test mode</b><br>"
                + "UPI id <b>success@razorpay</b>, or card 4111 1111 1111 1111.<br>"
                + "Your seats are held for a few minutes."
                + "</body></html>");
        hint.setFont(Theme.font(Font.PLAIN, 12f));
        hint.setForeground(Theme.MUTED);

        Theme.RoundPanel hintBox = new Theme.RoundPanel(Theme.FIELD, 14);
        hintBox.setLayout(new BorderLayout());
        hintBox.setBorder(new CompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                new EmptyBorder(12, 16, 12, 16)));
        hintBox.add(hint, BorderLayout.CENTER);

        // ---- Progress bar ----
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBorderPainted(false);
        bar.setForeground(Theme.ACCENT);
        bar.setBackground(Theme.FIELD);
        bar.setPreferredSize(new Dimension(100, 6));

        // ---- Buttons ----
        JButton reopen = Theme.ghost("Open payment page again");
        reopen.addActionListener(e -> Fmt.openBrowser(this, payUrl));
        JButton cancel = Theme.primary("Cancel booking");
        cancel.addActionListener(e -> cancelBooking());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(reopen);
        buttons.add(cancel);

        // ---- Assemble ----
        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(info, BorderLayout.NORTH);
        top.add(hintBox, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 16));
        south.setOpaque(false);
        south.add(bar, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(22, 24, 22, 24));
        center.add(top, BorderLayout.NORTH);
        center.add(south, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelBooking();
            }
        });

        pack();
        setMinimumSize(new Dimension(500, 0));
        setLocationRelativeTo(owner);
        poller.scheduleWithFixedDelay(this::poll, 2, 2, TimeUnit.SECONDS);
    }

    public String getResult() { return result; }

    public Map<String, Object> getBooking() { return booking; }

    private void poll() {
        try {
            Map<String, Object> b = Json.map(Api.get("/bookings/" + bookingId));
            String status = Json.str(b, "status");
            if (!"PENDING".equals(status)) {
                SwingUtilities.invokeLater(() -> finish(status, b));
            }
        } catch (Exception ignored) {
            // keep polling; a temporary network error is not a payment failure
        }
    }

    private void cancelBooking() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Cancel this booking and release the seats?", "Cancel booking", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        Ui.async(() -> Json.map(Api.post("/bookings/" + bookingId + "/cancel", null)),
                b -> finish(Json.str(b, "status"), b),
                e -> Ui.error(this, e));
    }

    private void finish(String status, Map<String, Object> b) {
        if (result != null) {
            return;
        }
        result = status;
        booking = b;
        poller.shutdownNow();
        dispose();
    }
}