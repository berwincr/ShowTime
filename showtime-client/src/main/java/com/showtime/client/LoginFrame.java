package com.showtime.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Module 1: registration and login. */
public class LoginFrame extends JFrame {

    // ---- Palette -------------------------------------------------------
    private static final Color BG          = new Color(0x14161C);
    private static final Color FIELD       = new Color(0x252932);
    private static final Color FIELD_HOVER = new Color(0x2F3440);
    private static final Color OUTLINE     = new Color(0x3A3F4C);
    private static final Color TEXT        = new Color(0xF2F3F5);
    private static final Color MUTED       = new Color(0x9AA0AC);
    private static final Color ACCENT      = new Color(0xE5484D);
    private static final Color ACCENT_HI   = new Color(0xF06469);
    private static final Color ACCENT_LO   = new Color(0xC93A3F);
    private static final Color DISABLED    = new Color(0x4A4F5C);

    private static final Font BASE = new Font("SansSerif", Font.PLAIN, 14);

    private final JTextField loginEmail = new JTextField(20);
    private final JPasswordField loginPassword = new JPasswordField(20);
    private final AccentButton loginButton = new AccentButton("Log in");

    private final JTextField regName = new JTextField(20);
    private final JTextField regEmail = new JTextField(20);
    private final JPasswordField regPassword = new JPasswordField(20);
    private final AccentButton registerButton = new AccentButton("Create account");

    private final AccentButton loginTab = new AccentButton("Log in");
    private final AccentButton registerTab = new AccentButton("Register");
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);

    public LoginFrame() {
        super("ShowTime - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        // ---- Header ----
        JPanel header = new GradientPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(26, 28, 24, 28));

        JLabel logo = new JLabel("<html>Show<font color='#E5484D'>Time</font></html>");
        logo.setFont(BASE.deriveFont(Font.BOLD, 34f));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tag = new JLabel("Book movie tickets across theatres");
        tag.setFont(BASE.deriveFont(13f));
        tag.setForeground(MUTED);
        tag.setAlignmentX(LEFT_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(4));
        header.add(tag);

        // ---- Segmented tab switcher ----
        RoundPanel segment = new RoundPanel(FIELD, 999);
        segment.setLayout(new GridLayout(1, 2, 4, 0));
        segment.setBorder(new EmptyBorder(4, 4, 4, 4));
        segment.add(loginTab);
        segment.add(registerTab);
        loginTab.setSegment(true);
        registerTab.setSegment(true);
        loginTab.addActionListener(e -> showTab(true));
        registerTab.addActionListener(e -> showTab(false));

        // ---- Forms ----
        cardPanel.setOpaque(false);
        cardPanel.add(form(new String[]{"Email", "Password"},
                new JTextComponent[]{loginEmail, loginPassword}, loginButton), "login");
        cardPanel.add(form(new String[]{"Name", "Email", "Password (min 6)"},
                new JTextComponent[]{regName, regEmail, regPassword}, registerButton), "register");

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(22, 28, 24, 28));
        body.add(segment, BorderLayout.NORTH);
        body.add(cardPanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
        showTab(true);

        pack();
        setMinimumSize(new Dimension(440, 520));
        setLocationRelativeTo(null);
    }

    private void showTab(boolean login) {
        cards.show(cardPanel, login ? "login" : "register");
        loginTab.setActive(login);
        registerTab.setActive(!login);
        getRootPane().setDefaultButton(login ? loginButton : registerButton);
    }

    private JPanel form(String[] labels, JTextComponent[] fields, JButton button) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setFont(BASE.deriveFont(Font.BOLD, 12f));
            l.setForeground(MUTED);
            c.gridy = row++;
            c.insets = new Insets(i == 0 ? 14 : 12, 2, 5, 2);
            p.add(l, c);

            c.gridy = row++;
            c.insets = new Insets(0, 0, 0, 0);
            p.add(new FieldBox(fields[i]), c);
        }

        c.gridy = row++;
        c.insets = new Insets(26, 0, 0, 0);
        button.setPreferredSize(new Dimension(100, 44));
        p.add(button, c);

        c.gridy = row;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        p.add(Box.createGlue(), c);
        return p;
    }

    // ====================================================================
    //  Logic (unchanged)
    // ====================================================================

    private void login() {
        String email = loginEmail.getText().trim();
        String password = new String(loginPassword.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            Ui.info(this, "Please enter your email and password.");
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);
        submit(loginButton, "/auth/login", body);
    }

    private void register() {
        String name = regName.getText().trim();
        String email = regEmail.getText().trim();
        String password = new String(regPassword.getPassword());
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Ui.info(this, "Please fill in all the fields.");
            return;
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            Ui.info(this, "Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            Ui.info(this, "Password must be at least 6 characters.");
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        submit(registerButton, "/auth/register", body);
    }

    private void submit(JButton button, String path, Map<String, Object> body) {
        button.setEnabled(false);
        Ui.async(() -> Json.map(Api.post(path, body)), session -> {
            Api.token = Json.str(session, "token");
            Api.userId = Json.num(session, "userId");
            Api.userName = Json.str(session, "name");
            new MainFrame().setVisible(true);
            dispose();
        }, e -> {
            button.setEnabled(true);
            Ui.error(this, e);
        });
    }

    // ====================================================================
    //  Styled components
    // ====================================================================

    private static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Header with a subtle diagonal gradient and an accent line at the bottom. */
    private static class GradientPanel extends JPanel {
        GradientPanel() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, new Color(0x3A1217), getWidth(), getHeight(), BG));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(ACCENT);
            g2.fillRect(0, getHeight() - 3, getWidth(), 3);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Panel with a rounded, filled background. */
    private static class RoundPanel extends JPanel {
        private final Color fill;
        private final int arc;

        RoundPanel(Color fill, int arc) {
            this.fill = fill;
            this.arc = arc;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Wraps a text field in a rounded box with hover and focus highlighting. */
    private static class FieldBox extends JPanel {
        private boolean focused;
        private boolean hover;

        FieldBox(JTextComponent field) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(2, 12, 2, 12));

            field.setOpaque(false);
            field.setBorder(new EmptyBorder(9, 0, 9, 0));
            field.setFont(BASE);
            field.setForeground(TEXT);
            field.setCaretColor(ACCENT);
            field.setSelectionColor(new Color(0xE5484D, false));
            field.setSelectedTextColor(Color.WHITE);
            add(field, BorderLayout.CENTER);

            field.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true; repaint(); }
                @Override public void focusLost(FocusEvent e) { focused = false; repaint(); }
            });
            MouseAdapter m = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            };
            field.addMouseListener(m);
            addMouseListener(m);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(hover && !focused ? FIELD_HOVER : FIELD);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(focused ? ACCENT : OUTLINE);
            g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded button with hover/pressed/disabled states. Also used for the tab switcher. */
    private static class AccentButton extends JButton {
        private boolean segment;
        private boolean active = true;

        AccentButton(String text) {
            super(text);
            setFont(BASE.deriveFont(Font.BOLD, 14f));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
        }

        void setSegment(boolean segment) {
            this.segment = segment;
            setActive(false);
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : MUTED);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            Color fill = null;
            if (!isEnabled()) {
                fill = DISABLED;
            } else if (active) {
                fill = getModel().isPressed() ? ACCENT_LO
                     : getModel().isRollover() ? ACCENT_HI : ACCENT;
            } else if (getModel().isRollover()) {
                fill = FIELD_HOVER;
            }
            if (fill != null) {
                int arc = segment ? 999 : 12;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}