package com.showtime.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/** Module 5: ticket / confirmation screen. */
public class TicketDialog extends JDialog {

    static { Theme.install(); }

    private static final Color OK       = new Color(0x3DBE6B);
    private static final Color OK_DARK  = new Color(0x12301D);
    private static final Color BAD      = Theme.ACCENT;
    private static final Color BAD_DARK = new Color(0x3A1217);
    private static final Color WAIT      = new Color(0xE0A93B);
    private static final Color WAIT_DARK = new Color(0x33290F);

    public TicketDialog(Window owner, Map<String, Object> b) {
        super(owner, "Ticket #" + Json.str(b, "id"), ModalityType.APPLICATION_MODAL);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        String status = Json.str(b, "status");
        boolean confirmed = "CONFIRMED".equals(status);
        boolean failed = "CANCELLED".equals(status) || "EXPIRED".equals(status) || "FAILED".equals(status);
        Color tone = confirmed ? OK : failed ? BAD : WAIT;
        Color toneDark = confirmed ? OK_DARK : failed ? BAD_DARK : WAIT_DARK;

        // ---- Header ----
        JLabel t = new JLabel(confirmed ? "Booking Confirmed" : "Booking " + status);
        t.setFont(Theme.font(Font.BOLD, 22f));
        t.setForeground(Color.WHITE);
        JLabel sub = Theme.muted(confirmed ? "Show this ticket at the theatre" : "Ticket #" + Json.str(b, "id"), 13f);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        t.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        texts.add(t);
        texts.add(Box.createVerticalStrut(3));
        texts.add(sub);

        StatusHeader head = new StatusHeader(tone, toneDark);
        head.setLayout(new BorderLayout(16, 0));
        head.setBorder(new EmptyBorder(20, 24, 20, 24));
        head.add(new Badge(tone, confirmed), BorderLayout.WEST);
        head.add(texts, BorderLayout.CENTER);

        // ---- Ticket card ----
        String payId = Json.str(b, "razorpayPaymentId");
        String payStatus = Json.str(b, "paymentStatus");
        String[][] details = {
                {"Booking ID", "#" + Json.str(b, "id")},
                {"Movie", Json.str(b, "movieTitle")},
                {"Theatre", Json.str(b, "theatreName")},
                {"Show time", Fmt.dateTime(Json.str(b, "showTime"))},
                {"Seats", Json.str(b, "seats")},
        };
        String[][] payment = {
                {"Amount", Fmt.rupees(Json.num(b, "amount"))},
                {"Payment status", payStatus.isEmpty() ? "-" : payStatus},
                {"Payment ID", payId.isEmpty() ? "-" : payId},
        };

        Theme.RoundPanel card = new Theme.RoundPanel(Theme.PANEL, 18);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.add(rows(details, false));
        card.add(Box.createVerticalStrut(12));
        card.add(new Perforation());
        card.add(Box.createVerticalStrut(12));
        card.add(rows(payment, true));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 24, 8, 24));
        body.add(card, BorderLayout.CENTER);

        // ---- Footer ----
        JButton close = Theme.primary("Close");
        close.addActionListener(e -> dispose());
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(8, 24, 18, 24));
        foot.add(close);

        add(head, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(foot, BorderLayout.SOUTH);
        getRootPane().setDefaultButton((JButton) close);
        pack();
        setMinimumSize(new Dimension(460, 0));
        setLocationRelativeTo(owner);
    }

    private static JPanel rows(String[][] data, boolean paymentBlock) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        for (int i = 0; i < data.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            c.insets = new Insets(5, 0, 5, 24);
            JLabel k = Theme.muted(data[i][0], 13f);
            p.add(k, c);

            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(5, 0, 5, 0);
            JLabel v = new JLabel(data[i][1]);
            boolean amount = paymentBlock && i == 0;
            v.setFont(Theme.font(Font.BOLD, amount ? 17f : 14f));
            v.setForeground(amount ? Theme.ACCENT_HI : Theme.TEXT);
            p.add(v, c);
            c.fill = GridBagConstraints.NONE;
        }
        return p;
    }

    // ====================================================================
    //  Styled components
    // ====================================================================

    /** Header tinted by booking status, with a colored line at the bottom. */
    private static class StatusHeader extends JPanel {
        private final Color tone, dark;

        StatusHeader(Color tone, Color dark) {
            this.tone = tone;
            this.dark = dark;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, dark, getWidth(), getHeight(), Theme.BG));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(tone);
            g2.fillRect(0, getHeight() - 3, getWidth(), 3);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Round status badge with a check mark (confirmed) or a cross (anything else). */
    private static class Badge extends JComponent {
        private final Color tone;
        private final boolean ok;

        Badge(Color tone, boolean ok) {
            this.tone = tone;
            this.ok = ok;
            setPreferredSize(new Dimension(48, 48));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.smooth(g2);
            int s = Math.min(getWidth(), getHeight()) - 2;
            g2.setColor(new Color(tone.getRed(), tone.getGreen(), tone.getBlue(), 45));
            g2.fillOval(1, 1, s, s);
            g2.setColor(tone);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(1, 1, s, s);
            g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (ok) {
                g2.drawPolyline(new int[]{14, 21, 34}, new int[]{25, 32, 17}, 3);
            } else {
                g2.drawLine(17, 17, 31, 31);
                g2.drawLine(31, 17, 17, 31);
            }
            g2.dispose();
        }
    }

    /** Dashed "tear here" line between ticket details and payment details. */
    private static class Perforation extends JComponent {
        Perforation() {
            setPreferredSize(new Dimension(100, 2));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Theme.OUTLINE);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{6f, 5f}, 0f));
            g2.drawLine(0, 1, getWidth(), 1);
            g2.dispose();
        }
    }
}