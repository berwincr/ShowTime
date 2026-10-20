package com.showtime.client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Module 2 (movie search + theatres/showtimes) and Module 5 (booking history). */
public class MainFrame extends JFrame {

    static { Theme.install(); }

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.ENGLISH);

    // ---- movies tab
    private final DefaultListModel<Map<String, Object>> movieModel = new DefaultListModel<>();
    private final JList<Map<String, Object>> movieList = new JList<>(movieModel);
    private final JTextField searchField = new JTextField(14);
    private final JLabel statusLabel = Theme.muted(" ", 12f);
    private final JLabel posterLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel titleLabel = new JLabel("Select a movie");
    private final JLabel metaLabel = Theme.muted(" ", 13f);
    private final JTextArea overviewArea = new JTextArea(4, 30);
    private final JComboBox<LocalDate> dateBox = new JComboBox<>();
    private final DefaultTableModel showModel = readOnly("Theatre", "Area", "Time", "Price", "Seats left");
    private final JTable showTable = new JTable(showModel);
    private final JLabel showStatus = Theme.muted(" ", 12f);
    private List<Map<String, Object>> shows = new ArrayList<>();
    private final Map<String, ImageIcon> posters = new HashMap<>();

    // ---- bookings tab
    private final DefaultTableModel upcomingModel = readOnly("ID", "Movie", "Theatre", "Show time", "Seats", "Amount", "Status");
    private final DefaultTableModel pastModel = readOnly("ID", "Movie", "Theatre", "Show time", "Seats", "Amount", "Status");
    private final JTable upcomingTable = new JTable(upcomingModel);
    private final JTable pastTable = new JTable(pastModel);
    private List<Map<String, Object>> upcoming = new ArrayList<>();
    private List<Map<String, Object>> past = new ArrayList<>();

    // ---- navigation
    private final Theme.Btn moviesTab = new Theme.Btn("Movies", Theme.Btn.Kind.SEGMENT);
    private final Theme.Btn bookingsTab = new Theme.Btn("My Bookings", Theme.Btn.Kind.SEGMENT);
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);

    public MainFrame() {
        super("ShowTime - Movie Ticket Booking");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        cardPanel.setOpaque(false);
        cardPanel.add(moviesTab(), "movies");
        cardPanel.add(bookingsTab(), "bookings");

        Theme.RoundPanel segment = new Theme.RoundPanel(Theme.FIELD, 999);
        segment.setLayout(new GridLayout(1, 2, 4, 0));
        segment.setBorder(new EmptyBorder(4, 4, 4, 4));
        segment.add(moviesTab);
        segment.add(bookingsTab);
        moviesTab.addActionListener(e -> showTab(true));
        bookingsTab.addActionListener(e -> showTab(false));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(14, 16, 4, 16));
        nav.add(segment);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(nav, BorderLayout.NORTH);
        center.add(cardPanel, BorderLayout.CENTER);

        add(header(), BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        showTabQuiet(true);
        setSize(1100, 740);
        setLocationRelativeTo(null);
        loadMovies("");
    }

    private void showTab(boolean movies) {
        showTabQuiet(movies);
        if (!movies) loadBookings();
    }

    private void showTabQuiet(boolean movies) {
        cards.show(cardPanel, movies ? "movies" : "bookings");
        moviesTab.setActive(movies);
        bookingsTab.setActive(!movies);
    }

    private static DefaultTableModel readOnly(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // ================================================================ header
    private JPanel header() {
        JPanel p = new Theme.Header();
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel logo = new JLabel("<html>Show<font color='#E5484D'>Time</font></html>");
        logo.setFont(Theme.font(Font.BOLD, 26f));
        logo.setForeground(Color.WHITE);

        JLabel hello = new JLabel("Hello, " + Api.userName);
        hello.setFont(Theme.font(Font.PLAIN, 14f));
        hello.setForeground(Theme.TEXT);

        JButton logout = Theme.ghost("Log out");
        logout.addActionListener(e -> logout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(hello);
        right.add(logout);

        p.add(logo, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void logout() {
        Ui.async(() -> Api.post("/auth/logout", null), r -> afterLogout(), e -> afterLogout());
    }

    private void afterLogout() {
        Api.clearSession();
        new LoginFrame().setVisible(true);
        dispose();
    }

    // ================================================================ movies tab
    private JComponent moviesTab() {
        // left: search + list
        JButton searchButton = Theme.primary("Search");
        searchButton.addActionListener(e -> loadMovies(searchField.getText().trim()));
        searchField.addActionListener(e -> loadMovies(searchField.getText().trim()));
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(Theme.field(searchField), BorderLayout.CENTER);
        searchRow.add(searchButton, BorderLayout.EAST);

        movieList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieList.setFixedCellHeight(58);
        movieList.setBackground(Theme.PANEL);
        movieList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) value;
                String sub = Json.str(m, "language");
                String genres = Json.str(m, "genres");
                if (!genres.isEmpty()) sub += (sub.isEmpty() ? "" : "  |  ") + genres;
                String text = "<html><b>" + escape(Json.str(m, "title")) + "</b><br>"
                        + "<font color='#9AA0AC' size='3'>" + escape(sub) + "</font></html>";
                super.getListCellRendererComponent(list, text, index, isSelected, false);
                setOpaque(true);
                setFont(Theme.BASE);
                setBackground(isSelected ? Theme.SELECT : Theme.PANEL);
                setForeground(Theme.TEXT);
                setBorder(new CompoundBorder(
                        new MatteBorder(0, 3, 0, 0, isSelected ? Theme.ACCENT : Theme.PANEL),
                        new EmptyBorder(4, 10, 4, 8)));
                return this;
            }
        });
        movieList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showMovie(movieList.getSelectedValue());
        });

        Theme.Card left = new Theme.Card(null);
        left.body.add(searchRow, BorderLayout.NORTH);
        left.body.add(Theme.scroll(movieList), BorderLayout.CENTER);
        left.body.add(statusLabel, BorderLayout.SOUTH);
        left.setPreferredSize(new Dimension(320, 100));

        // right top: poster + details
        posterLabel.setPreferredSize(new Dimension(170, 255));
        posterLabel.setOpaque(true);
        posterLabel.setBackground(Theme.FIELD);
        posterLabel.setForeground(Theme.MUTED);
        posterLabel.setBorder(BorderFactory.createLineBorder(Theme.OUTLINE));

        titleLabel.setFont(Theme.font(Font.BOLD, 24f));
        titleLabel.setForeground(Color.WHITE);

        overviewArea.setEditable(false);
        overviewArea.setLineWrap(true);
        overviewArea.setWrapStyleWord(true);
        overviewArea.setFont(Theme.font(Font.PLAIN, 14f));
        overviewArea.setBackground(Theme.PANEL);
        overviewArea.setForeground(new Color(0xD0D3DA));
        overviewArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        titleBox.add(titleLabel);
        titleBox.add(metaLabel);

        JPanel info = new JPanel(new BorderLayout(0, 8));
        info.setOpaque(false);
        info.add(titleBox, BorderLayout.NORTH);
        info.add(Theme.scroll(overviewArea), BorderLayout.CENTER);

        Theme.Card details = new Theme.Card(null);
        details.body.setLayout(new BorderLayout(16, 0));
        details.body.add(posterLabel, BorderLayout.WEST);
        details.body.add(info, BorderLayout.CENTER);

        // right bottom: date + shows table
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) dateBox.addItem(today.plusDays(i));
        Theme.combo(dateBox);
        dateBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                LocalDate d = (LocalDate) value;
                String text = d.equals(LocalDate.now()) ? "Today, " + d.format(DAY)
                        : d.equals(LocalDate.now().plusDays(1)) ? "Tomorrow, " + d.format(DAY) : d.format(DAY);
                super.getListCellRendererComponent(list, text, index, isSelected, false);
                setOpaque(true);
                setBackground(isSelected ? Theme.ACCENT : Theme.FIELD);
                setForeground(Theme.TEXT);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
        dateBox.addActionListener(e -> loadShows());

        JButton select = Theme.primary("Select Seats");
        select.addActionListener(e -> openSeats());
        Theme.table(showTable);
        showTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        showTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSeats();
            }
        });

        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dateRow.setOpaque(false);
        dateRow.add(Theme.muted("Date", 13f));
        dateRow.add(dateBox);
        dateRow.add(showStatus);

        JPanel selectRow = new JPanel(new BorderLayout());
        selectRow.setOpaque(false);
        selectRow.add(Theme.muted("Double-click a row to pick seats", 12f), BorderLayout.WEST);
        selectRow.add(select, BorderLayout.EAST);

        Theme.Card showsCard = new Theme.Card("Theatres and showtimes");
        showsCard.body.add(dateRow, BorderLayout.NORTH);
        showsCard.body.add(Theme.scroll(showTable), BorderLayout.CENTER);
        showsCard.body.add(selectRow, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.add(details, BorderLayout.NORTH);
        right.add(showsCard, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(12, 0));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 16, 16, 16));
        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        return root;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void loadMovies(String query) {
        statusLabel.setText("Loading movies...");
        String path = "/movies" + (query.isEmpty() ? "" : "?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        Ui.async(() -> Json.list(Api.get(path)), list -> {
            movieModel.clear();
            for (Object o : list) movieModel.addElement(Json.map(o));
            statusLabel.setText(list.size() + " movie(s)" + (query.isEmpty() ? " now playing" : " found"));
            if (!list.isEmpty()) {
                movieList.setSelectedIndex(0);
            } else {
                showMovie(null);
            }
        }, e -> {
            statusLabel.setText("Could not load movies");
            Ui.error(this, e);
        });
    }

    private void showMovie(Map<String, Object> m) {
        if (m == null) {
            titleLabel.setText("No movie selected");
            metaLabel.setText(" ");
            overviewArea.setText("");
            posterLabel.setIcon(null);
            posterLabel.setText("");
            showModel.setRowCount(0);
            shows = new ArrayList<>();
            return;
        }
        titleLabel.setText(Json.str(m, "title"));
        List<String> meta = new ArrayList<>();
        if (!Json.str(m, "language").isEmpty()) meta.add(Json.str(m, "language"));
        if (Json.num(m, "runtimeMinutes") > 0) meta.add(Json.num(m, "runtimeMinutes") + " min");
        Object rating = m.get("rating");
        if (rating instanceof Number n && n.doubleValue() > 0) meta.add(String.format("Rating %.1f/10", n.doubleValue()));
        if (!Json.str(m, "releaseDate").isEmpty()) meta.add("Released " + Json.str(m, "releaseDate"));
        metaLabel.setText(String.join("  |  ", meta));
        overviewArea.setText(Json.str(m, "overview"));
        overviewArea.setCaretPosition(0);
        loadPoster(Json.str(m, "posterUrl"));
        loadShows();
    }

    private void loadPoster(String url) {
        posterLabel.setIcon(null);
        if (url.isEmpty()) {
            posterLabel.setText("No poster");
            return;
        }
        if (posters.containsKey(url)) {
            posterLabel.setText("");
            posterLabel.setIcon(posters.get(url));
            return;
        }
        posterLabel.setText("Loading poster...");
        Ui.async(() -> ImageIO.read(URI.create(url).toURL()), img -> {
            if (img == null) {
                posterLabel.setText("No poster");
                return;
            }
            ImageIcon icon = new ImageIcon(img.getScaledInstance(170, 255, Image.SCALE_SMOOTH));
            posters.put(url, icon);
            Map<String, Object> current = movieList.getSelectedValue();
            if (current != null && url.equals(Json.str(current, "posterUrl"))) {
                posterLabel.setText("");
                posterLabel.setIcon(icon);
            }
        }, e -> posterLabel.setText("No poster"));
    }

    private void loadShows() {
        Map<String, Object> m = movieList.getSelectedValue();
        LocalDate date = (LocalDate) dateBox.getSelectedItem();
        if (m == null || date == null) return;
        long movieId = Json.num(m, "id");
        showStatus.setText("Loading showtimes...");
        showModel.setRowCount(0);
        Ui.async(() -> Json.list(Api.get("/movies/" + movieId + "/shows?date=" + date)), list -> {
            Map<String, Object> now = movieList.getSelectedValue();
            if (now == null || Json.num(now, "id") != movieId || !date.equals(dateBox.getSelectedItem())) return; // stale
            shows = new ArrayList<>();
            showModel.setRowCount(0);
            for (Object o : list) {
                Map<String, Object> s = Json.map(o);
                shows.add(s);
                showModel.addRow(new Object[]{Json.str(s, "theatreName"), Json.str(s, "area"),
                        Fmt.time(Json.str(s, "showTime")), Fmt.rupees(Json.num(s, "price")),
                        Json.num(s, "availableSeats") + " / " + Json.num(s, "totalSeats")});
            }
            showStatus.setText(list.isEmpty() ? "No shows left on this date" : list.size() + " show(s)");
        }, e -> {
            showStatus.setText("Could not load showtimes");
            Ui.error(this, e);
        });
    }

    private void openSeats() {
        int row = showTable.getSelectedRow();
        if (row < 0 || row >= shows.size()) {
            Ui.info(this, "Please select a showtime first.");
            return;
        }
        Map<String, Object> show = shows.get(row);
        new SeatDialog(this, show, () -> {
            loadShows();
            loadBookings();
        }).setVisible(true);
        loadShows();     // refresh seat counts after the dialog closes
    }

    // ================================================================ bookings tab
    private JComponent bookingsTab() {
        for (JTable t : new JTable[]{upcomingTable, pastTable}) {
            Theme.table(t);
            t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        upcomingTable.addMouseListener(doubleClick(upcomingTable, true));
        pastTable.addMouseListener(doubleClick(pastTable, false));

        Theme.Card up = new Theme.Card("Upcoming (confirmed)");
        up.body.add(Theme.scroll(upcomingTable), BorderLayout.CENTER);
        Theme.Card pa = new Theme.Card("Past and other bookings");
        pa.body.add(Theme.scroll(pastTable), BorderLayout.CENTER);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 12));
        tables.setOpaque(false);
        tables.add(up);
        tables.add(pa);

        JButton refresh = Theme.ghost("Refresh");
        refresh.addActionListener(e -> loadBookings());
        JButton view = Theme.primary("View ticket");
        view.addActionListener(e -> {
            int r = upcomingTable.getSelectedRow();
            if (r >= 0) { showTicket(upcoming.get(r)); return; }
            r = pastTable.getSelectedRow();
            if (r >= 0) { showTicket(past.get(r)); return; }
            Ui.info(this, "Select a booking first (or double-click a row).");
        });
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.add(refresh);
        bar.add(view);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 16, 16, 16));
        root.add(bar, BorderLayout.NORTH);
        root.add(tables, BorderLayout.CENTER);
        return root;
    }

    private MouseAdapter doubleClick(JTable table, boolean isUpcoming) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (e.getClickCount() == 2 && r >= 0) showTicket((isUpcoming ? upcoming : past).get(r));
            }
        };
    }

    private void showTicket(Map<String, Object> booking) {
        new TicketDialog(this, booking).setVisible(true);
    }

    private void loadBookings() {
        Ui.async(() -> Json.list(Api.get("/bookings/my")), list -> {
            upcoming = new ArrayList<>();
            past = new ArrayList<>();
            upcomingModel.setRowCount(0);
            pastModel.setRowCount(0);
            for (Object o : list) {
                Map<String, Object> b = Json.map(o);
                boolean isUpcoming = Boolean.TRUE.equals(b.get("upcoming"));
                Object[] row = {"#" + Json.str(b, "id"), Json.str(b, "movieTitle"), Json.str(b, "theatreName"),
                        Fmt.dateTime(Json.str(b, "showTime")), Json.str(b, "seats"),
                        Fmt.rupees(Json.num(b, "amount")), Json.str(b, "status")};
                if (isUpcoming) {
                    upcoming.add(b);
                    upcomingModel.addRow(row);
                } else {
                    past.add(b);
                    pastModel.addRow(row);
                }
            }
        }, e -> Ui.error(this, e));
    }
}