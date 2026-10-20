package com.showtime.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Shared look for the ShowTime client: palette, fonts and styled components. */
public final class Theme {
    private Theme() {}

    public static final Color BG          = new Color(0x14161C);
    public static final Color PANEL       = new Color(0x1B1E26);
    public static final Color ROW_ALT     = new Color(0x20232C);
    public static final Color FIELD       = new Color(0x252932);
    public static final Color FIELD_HOVER = new Color(0x2F3440);
    public static final Color OUTLINE     = new Color(0x3A3F4C);
    public static final Color TEXT        = new Color(0xF2F3F5);
    public static final Color MUTED       = new Color(0x9AA0AC);
    public static final Color ACCENT      = new Color(0xE5484D);
    public static final Color ACCENT_HI   = new Color(0xF06469);
    public static final Color ACCENT_LO   = new Color(0xC93A3F);
    public static final Color SELECT      = new Color(0x3A1F24);
    public static final Color DISABLED    = new Color(0x4A4F5C);

    public static final Font BASE = new Font("SansSerif", Font.PLAIN, 14);

    public static Font font(int style, float size) { return BASE.deriveFont(style, size); }

    static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Call once before creating windows. Also darkens JOptionPane dialogs used by Ui.info / Ui.error. */
    public static void install() {
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", BASE);
        UIManager.put("OptionPane.buttonFont", BASE);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", BASE);
        UIManager.put("ToolTip.background", FIELD);
        UIManager.put("ToolTip.foreground", TEXT);
    }

    // ------------------------------------------------------------ factories

    public static Btn primary(String text) { return new Btn(text, Btn.Kind.PRIMARY); }
    public static Btn ghost(String text)   { return new Btn(text, Btn.Kind.GHOST); }

    public static JLabel muted(String text, float size) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.PLAIN, size));
        l.setForeground(MUTED);
        return l;
    }

    /** Wraps a text field in a rounded box with hover and focus highlighting. */
    public static JComponent field(JTextComponent f) { return new FieldBox(f); }

    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(new LineBorder(OUTLINE, 1, true));
        s.getViewport().setBackground(PANEL);
        s.setBackground(PANEL);
        s.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        s.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        s.getHorizontalScrollBar().setUI(new SlimScrollBarUI());
        s.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return s;
    }

    public static void table(JTable t) {
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setFont(BASE);
        t.setBackground(PANEL);
        t.setForeground(TEXT);
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(Color.WHITE);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                                     boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tb, v, false, false, r, c);
                setOpaque(true);
                setBackground(FIELD);
                setForeground(MUTED);
                setFont(font(Font.BOLD, 12f));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                                     boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tb, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setBackground(sel ? SELECT : (r % 2 == 0 ? PANEL : ROW_ALT));
                setForeground(sel ? Color.WHITE : TEXT);
                return this;
            }
        });
    }

    public static void combo(JComboBox<?> box) {
        box.setFont(BASE);
        box.setBackground(FIELD);
        box.setForeground(TEXT);
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ------------------------------------------------------------ components

    /** Header with a diagonal gradient and an accent line at the bottom. */
    public static class Header extends JPanel {
        public Header() { setOpaque(false); }

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
    public static class RoundPanel extends JPanel {
        private final Color fill;
        private final int arc;

        public RoundPanel(Color fill, int arc) {
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

    /** Rounded card with an optional title. Add content to {@link #body}. */
    public static class Card extends RoundPanel {
        public final JPanel body = new JPanel(new BorderLayout(0, 8));

        public Card(String title) {
            super(PANEL, 18);
            setLayout(new BorderLayout(0, 10));
            setBorder(new EmptyBorder(14, 14, 14, 14));
            body.setOpaque(false);
            if (title != null) {
                JLabel l = new JLabel(title);
                l.setFont(font(Font.BOLD, 14f));
                l.setForeground(TEXT);
                add(l, BorderLayout.NORTH);
            }
            add(body, BorderLayout.CENTER);
        }
    }

    /** Rounded button with hover / pressed / disabled states. */
    public static class Btn extends JButton {
        public enum Kind { PRIMARY, GHOST, SEGMENT }

        private final Kind kind;
        private boolean active = true;

        public Btn(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setFont(font(Font.BOLD, 13f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 18, 9, 18));
            setForeground(kind == Kind.GHOST ? TEXT : Color.WHITE);
            if (kind == Kind.SEGMENT) setActive(false);
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : MUTED);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            ButtonModel m = getModel();
            int arc = kind == Kind.SEGMENT ? 999 : 12;
            Color fill = null;
            if (!isEnabled()) {
                fill = DISABLED;
            } else if (kind == Kind.GHOST) {
                fill = m.isPressed() ? OUTLINE : m.isRollover() ? FIELD_HOVER : FIELD;
            } else if (kind == Kind.SEGMENT && !active) {
                fill = m.isRollover() ? FIELD_HOVER : null;
            } else {
                fill = m.isPressed() ? ACCENT_LO : m.isRollover() ? ACCENT_HI : ACCENT;
            }
            if (fill != null) {
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            }
            if (kind == Kind.GHOST && isEnabled()) {
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class FieldBox extends JPanel {
        private boolean focused, hover;

        FieldBox(JTextComponent field) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(2, 12, 2, 12));
            field.setOpaque(false);
            field.setBorder(new EmptyBorder(9, 0, 9, 0));
            field.setFont(BASE);
            field.setForeground(TEXT);
            field.setCaretColor(ACCENT);
            field.setSelectionColor(new Color(0xE5484D));
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

    private static class SlimScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = OUTLINE;
            trackColor = PANEL;
        }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(isThumbRollover() ? MUTED : OUTLINE);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }
}