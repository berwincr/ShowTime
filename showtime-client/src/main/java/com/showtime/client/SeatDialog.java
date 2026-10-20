package com.showtime.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Module 3: seat layout (available / selected / booked), total price, booking and payment. */
public class SeatDialog extends JDialog {

    static { Theme.install(); }

    private static final Color SEAT_BOOKED = new Color(0x22252D);

    private final Frame owner;
    private final Map<String, Object> show;
    private final Runnable onBooked;
    private final long showId;
    private final long price;

    private final JPanel grid = new JPanel();
    private final JLabel summary = new JLabel(" ");
    private final Theme.Btn payButton = Theme.primary("Pay with Razorpay");
    private final Set<String> selected = new TreeSet<>(
            Comparator.comparing((String s) -> s.charAt(0)).thenComparingInt(s -> Integer.parseInt(s.substring(1))));
    private final Map<String, SeatButton> buttons = new LinkedHashMap<>();
    private final Set<String> booked = new HashSet<>();

    public SeatDialog(Frame owner, Map<String, Object> show, Runnable onBooked) {
        super(owner, "Select seats", true);
        this.owner = owner;
        this.show = show;
        this.onBooked = onBooked;
        this.showId = Json.num(show, "showId");
        this.price = Json.num(show, "price");
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        // ---- Header ----
        JPanel top = new Theme.Header();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(18, 24, 16, 24));
        JLabel movie = new JLabel(Json.str(show, "movieTitle"));
        movie.setFont(Theme.font(Font.BOLD, 22f));
        movie.setForeground(Color.WHITE);
        movie.setAlignmentX(LEFT_ALIGNMENT);
        JLabel where = Theme.muted(Json.str(show, "theatreName") + "  |  " + Json.str(show, "showDate")
                + "  " + Fmt.time(Json.str(show, "showTime")) + "  |  " + Fmt.rupees(price) + " per seat", 13f);
        where.setAlignmentX(LEFT_ALIGNMENT);
        top.add(movie);
        top.add(Box.createVerticalStrut(4));
        top.add(where);

        // ---- Seat area ----
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(18, 24, 12, 24));
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(16, 24, 0, 24));
        center.add(new ScreenPanel(), BorderLayout.NORTH);
        center.add(grid, BorderLayout.CENTER);
        center.add(legend(), BorderLayout.SOUTH);

        // ---- Footer ----
        JButton close = Theme.ghost("Close");
        close.addActionListener(e -> dispose());
        payButton.addActionListener(e -> pay());
        summary.setFont(Theme.font(Font.PLAIN, 14f));
        summary.setForeground(Theme.TEXT);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.add(close);
        btns.add(payButton);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(Theme.PANEL);
        foot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.OUTLINE),
                new EmptyBorder(14, 24, 14, 24)));
        foot.add(summary, BorderLayout.CENTER);
        foot.add(btns, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(foot, BorderLayout.SOUTH);

        updateSummary();
        loadSeats();
        setSize(700, 640);
        setLocationRelativeTo(owner);
    }

    private JPanel legend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 4));
        p.setOpaque(false);
        p.add(swatch(Theme.FIELD, Theme.OUTLINE, "Available"));
        p.add(swatch(Theme.ACCENT, Theme.ACCENT, "Selected"));
        p.add(swatch(SEAT_BOOKED, SEAT_BOOKED, "Booked"));
        return p;
    }

    private JLabel swatch(Color fill, Color line, String text) {
        JLabel l = Theme.muted(text, 12f);
        l.setIconTextGap(8);
        l.setIcon(new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.smooth(g2);
                g2.setColor(fill);
                g2.fillRoundRect(x, y, 16, 16, 6, 6);
                g2.setColor(line);
                g2.drawRoundRect(x, y, 16, 16, 6, 6);
                g2.dispose();
            }
            public int getIconWidth() { return 17; }
            public int getIconHeight() { return 17; }
        });
        return l;
    }

    private void loadSeats() {
        Ui.async(() -> Json.map(Api.get("/shows/" + showId + "/seats")), map -> {
            int rows = (int) Json.num(map, "rows");
            int cols = (int) Json.num(map, "cols");
            booked.clear();
            for (Object o : Json.list(map.get("bookedSeats"))) {
                booked.add(String.valueOf(o));
            }
            selected.removeIf(booked::contains);
            buildGrid(rows, cols);
            updateSummary();
        }, e -> Ui.error(this, e));
    }

    private void buildGrid(int rows, int cols) {
        grid.removeAll();
        buttons.clear();
        grid.setLayout(new GridLayout(rows, cols + 1, 6, 6));
        for (int r = 0; r < rows; r++) {
            char row = (char) ('A' + r);
            JLabel rowLabel = new JLabel(String.valueOf(row), SwingConstants.CENTER);
            rowLabel.setFont(Theme.font(Font.BOLD, 12f));
            rowLabel.setForeground(Theme.MUTED);
            grid.add(rowLabel);
            for (int c = 1; c <= cols; c++) {
                String label = row + String.valueOf(c);
                SeatButton b = new SeatButton(String.valueOf(c));
                b.setToolTipText(label);
                if (booked.contains(label)) {
                    b.setState(SeatButton.State.BOOKED);
                } else {
                    b.setState(selected.contains(label) ? SeatButton.State.SELECTED : SeatButton.State.AVAILABLE);
                    b.addActionListener(e -> toggle(label, b));
                }
                buttons.put(label, b);
                grid.add(b);
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    private void toggle(String label, SeatButton b) {
        if (selected.contains(label)) {
            selected.remove(label);
            b.setState(SeatButton.State.AVAILABLE);
        } else {
            if (selected.size() >= 6) {
                Ui.info(this, "You can book up to 6 seats at a time.");
                return;
            }
            selected.add(label);
            b.setState(SeatButton.State.SELECTED);
        }
        updateSummary();
    }

    private void updateSummary() {
        if (selected.isEmpty()) {
            summary.setText("<html><font color='#9AA0AC'>No seats selected</font></html>");
            payButton.setEnabled(false);
        } else {
            summary.setText("<html><b>" + String.join(", ", selected) + "</b><br>"
                    + "<font color='#9AA0AC'>" + selected.size() + " x " + Fmt.rupees(price) + " = </font>"
                    + "<b><font color='#F06469'>" + Fmt.rupees(price * selected.size()) + "</font></b></html>");
            payButton.setEnabled(true);
        }
    }

    /** Creates a PENDING booking (seats are held), creates a Razorpay order, opens the browser and waits. */
    private void pay() {
        payButton.setEnabled(false);
        List<String> seats = new ArrayList<>(selected);

        Ui.async(() -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("showId", showId);
            body.put("seats", seats);
            Map<String, Object> booking = Json.map(Api.post("/bookings", body));
            long bookingId = Json.num(booking, "id");
            try {
                Map<String, Object> orderBody = new LinkedHashMap<>();
                orderBody.put("bookingId", bookingId);
                Map<String, Object> order = Json.map(Api.post("/payments/order", orderBody));
                return new Object[]{bookingId, order};
            } catch (Api.ApiException e) {
                try {
                    Api.post("/bookings/" + bookingId + "/cancel", null);   // release the held seats
                } catch (Api.ApiException ignored) {
                    // nothing more we can do
                }
                throw e;
            }
        }, result -> {
            long bookingId = (Long) result[0];
            Map<String, Object> order = Json.map(result[1]);
            String payUrl = Json.str(order, "payUrl");
            Fmt.openBrowser(this, payUrl);

            PaymentDialog dialog = new PaymentDialog(this, bookingId, payUrl);
            dialog.setVisible(true);          // blocks until the booking is no longer PENDING

            String status = dialog.getResult();
            if ("CONFIRMED".equals(status)) {
                dispose();
                onBooked.run();
                new TicketDialog(owner, dialog.getBooking()).setVisible(true);
            } else {
                String why = "EXPIRED".equals(status)
                        ? "The payment was not completed in time and the seats were released."
                        : "The booking was cancelled and the seats were released.";
                Ui.info(this, why);
                selected.clear();
                updateSummary();
                loadSeats();
            }
        }, e -> {
            Ui.error(this, e);
            selected.removeIf(booked::contains);
            updateSummary();
            loadSeats();            // refresh: someone else may have taken a seat
        });
    }

    // ====================================================================
    //  Styled components
    // ====================================================================

    /** A curved, glowing "screen" bar shown above the seats. */
    private static class ScreenPanel extends JPanel {
        ScreenPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(100, 54));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.smooth(g2);
            int w = getWidth();
            int inset = 40;

            // soft glow beneath the curve
            g2.setPaint(new GradientPaint(0, 12, new Color(0xE5484D & 0xFFFFFF | 0x55000000, true),
                    0, 50, new Color(0xE5484D & 0xFFFFFF, true)));
            g2.fill(new QuadCurve2D.Float(inset, 14, w / 2f, -2, w - inset, 14));
            g2.fillRect(inset, 14, w - 2 * inset, 28);

            // the screen line itself
            g2.setColor(Theme.ACCENT);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new QuadCurve2D.Float(inset, 14, w / 2f, -2, w - inset, 14));

            g2.setFont(Theme.font(Font.BOLD, 11f));
            g2.setColor(Theme.MUTED);
            String t = "SCREEN";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, (w - fm.stringWidth(t)) / 2, 40);
            g2.dispose();
        }
    }

    /** Rounded seat with available / selected / booked states. */
    private static class SeatButton extends JButton {
        enum State { AVAILABLE, SELECTED, BOOKED }

        private State state = State.AVAILABLE;
        private boolean hover;

        SeatButton(String text) {
            super(text);
            setFont(Theme.font(Font.BOLD, 12f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new Insets(2, 2, 2, 2));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setState(State s) {
            this.state = s;
            if (s == State.BOOKED) {
                setEnabled(false);
                setText("X");
                setCursor(Cursor.getDefaultCursor());
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.smooth(g2);
            int w = getWidth(), h = getHeight();
            Color text;
            switch (state) {
                case SELECTED -> {
                    g2.setColor(hover ? Theme.ACCENT_HI : Theme.ACCENT);
                    g2.fillRoundRect(0, 0, w, h, 12, 12);
                    text = Color.WHITE;
                }
                case BOOKED -> {
                    g2.setColor(SEAT_BOOKED);
                    g2.fillRoundRect(0, 0, w, h, 12, 12);
                    text = Theme.DISABLED;
                }
                default -> {
                    g2.setColor(hover ? Theme.FIELD_HOVER : Theme.FIELD);
                    g2.fillRoundRect(0, 0, w, h, 12, 12);
                    g2.setColor(hover ? Theme.ACCENT : Theme.OUTLINE);
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                    text = Theme.TEXT;
                }
            }
            g2.setFont(getFont());
            g2.setColor(text);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}