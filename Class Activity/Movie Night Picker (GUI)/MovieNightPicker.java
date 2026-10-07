import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class MovieNightPicker {

          // ===================== COLOR PALETTE =====================
          private static final Color BG_DARK = new Color(13, 17, 23);
          private static final Color CARD_BG = new Color(22, 27, 34);
          private static final Color FIELD_BG = new Color(30, 41, 59);
          private static final Color BORDER_COLOR = new Color(48, 54, 61);
          private static final Color ACCENT_RED = new Color(235, 59, 90);
          private static final Color ACCENT_GOLD = new Color(254, 211, 48);
          private static final Color ACCENT_CYAN = new Color(34, 166, 179);
          private static final Color TEXT_WHITE = new Color(240, 246, 252);
          private static final Color TEXT_MUTED = new Color(160, 170, 182);
          private static final Color SEAT_AVAILABLE = new Color(38, 46, 58);
          private static final Color SEAT_SELECTED = new Color(46, 204, 113);
          private static final Color SEAT_BOOKED = new Color(72, 84, 96);

          // ড্রপডাউন আর "2. Select Movie Title" এর মাঝের ফাঁকা জায়গা (পপআপ খুললে যেন
          // মুভি লিস্ট না ঢাকে)
          // বেশি/কম লাগলে এই সংখ্যাটা বদলাও
          private static final int POPUP_GAP = 130;

          // ===================== DATA =====================
          private static final Map<String, List<Movie>> categoryMap = new LinkedHashMap<>();
          private static final Map<String, Set<String>> bookedSeatsPerShowtime = new HashMap<>();
          private static final Set<String> currentlySelectedSeats = new TreeSet<>();
          private static final Map<String, JButton> seatButtons = new HashMap<>();

          private static DefaultListModel<Movie> movieListModel;
          private static JList<Movie> movieJList;
          private static JLabel priceBreakdownLabel;
          private static JLabel totalRevenueLabel;
          private static JLabel totalOrdersCountLabel;
          private static JTextArea receiptArea;
          private static JRadioButton btnMorning, btnEvening, btnNight;
          private static JCheckBox chkPopcorn, chkDrinks, chkNachos;
          private static DefaultTableModel historyTableModel;

          private static int totalOrderCount = 0;
          private static double totalRevenue = 0.0;
          private static final DecimalFormat df = new DecimalFormat("#,##0.00");

          static class Movie {
                    String title;
                    String duration;
                    double baseTicketPrice;

                    public Movie(String title, String duration, double baseTicketPrice) {
                              this.title = title;
                              this.duration = duration;
                              this.baseTicketPrice = baseTicketPrice;
                    }

                    @Override
                    public String toString() {
                              return String.format("%s (%s) — $%s", title, duration, df.format(baseTicketPrice));
                    }
          }

          // ===================== MAIN =====================
          public static void main(String[] args) {
                    // Windows Look&Feel কাস্টম ডার্ক কালারের সাথে গণ্ডগোল করে, তাই cross-platform
                    // (Metal) ব্যবহার করা হয়েছে
                    try {
                              UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Exception ignored) {
                    }
                    UIManager.put("Button.disabledText", new Color(130, 140, 150));

                    initSampleData();
                    SwingUtilities.invokeLater(MovieNightPicker::createAndShowGui);
          }

          private static void initSampleData() {
                    categoryMap.put("Action / Thriller", Arrays.asList(
                                        new Movie("Extraction: Zero Point", "2h 10m", 12.0),
                                        new Movie("Cyber Runner 2099", "1h 58m", 11.5),
                                        new Movie("Night Velocity", "2h 05m", 10.0)));
                    categoryMap.put("Sci-Fi & Space", Arrays.asList(
                                        new Movie("Space Quest: Beyond Earth", "2h 45m", 14.0),
                                        new Movie("Quantum Singularity", "2h 15m", 13.0),
                                        new Movie("Neon Galactic", "1h 50m", 11.0)));
                    categoryMap.put("Animation & Comedy", Arrays.asList(
                                        new Movie("The Laughing Carnival", "1h 35m", 9.5),
                                        new Movie("Pixel Adventures", "1h 40m", 10.0),
                                        new Movie("Super Pet Squad", "1h 30m", 9.0)));
                    categoryMap.put("Horror & Mystery", Arrays.asList(
                                        new Movie("The Midnight Manor", "1h 48m", 11.0),
                                        new Movie("Echoes in the Dark", "2h 00m", 12.0)));

                    bookedSeatsPerShowtime.put("11:30 AM (Matinee)", new HashSet<>(Arrays.asList("A1", "A2")));
                    bookedSeatsPerShowtime.put("05:00 PM (Prime)", new HashSet<>(Arrays.asList("B2", "B3", "C4")));
                    bookedSeatsPerShowtime.put("08:30 PM (Night)", new HashSet<>(Arrays.asList("A3", "C1")));
          }

          // ===================== MAIN WINDOW =====================
          private static void createAndShowGui() {
                    JFrame frame = new JFrame("CineFlix Multiplex — Smart Ticket Terminal & Order Telemetry");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(1240, 780);
                    frame.setMinimumSize(new Dimension(1080, 680));
                    frame.getContentPane().setBackground(BG_DARK);
                    frame.setLayout(new BorderLayout());

                    frame.add(createTopBanner(), BorderLayout.NORTH);
                    frame.add(createBottomActionBar(), BorderLayout.SOUTH);

                    JTabbedPane tabbedPane = new JTabbedPane();
                    tabbedPane.setUI(new DarkTabbedPaneUI());
                    tabbedPane.setOpaque(true);
                    tabbedPane.setBackground(BG_DARK);
                    tabbedPane.setForeground(TEXT_WHITE);
                    tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

                    tabbedPane.addTab("Ticket Booking Console", createBookingInterface());
                    tabbedPane.addTab("Live Orders & Sales Telemetry", createHistoryInterface());

                    frame.add(tabbedPane, BorderLayout.CENTER);

                    refreshSeatGridDisplay();
                    updateSummaryCalculations();

                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
          }

          private static JPanel createTopBanner() {
                    JPanel header = new JPanel(new BorderLayout());
                    header.setBackground(CARD_BG);
                    header.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                                        new EmptyBorder(12, 24, 12, 24)));

                    JLabel title = new JLabel("CINEFLIX MULTIPLEX ENTERTAINMENT SYSTEM");
                    title.setFont(new Font("SansSerif", Font.BOLD, 18));
                    title.setForeground(TEXT_WHITE);

                    JPanel metrics = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
                    metrics.setOpaque(false);

                    totalOrdersCountLabel = new JLabel("Orders: 0");
                    totalOrdersCountLabel.setForeground(ACCENT_CYAN);
                    totalOrdersCountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

                    totalRevenueLabel = new JLabel("Revenue: $0.00");
                    totalRevenueLabel.setForeground(ACCENT_GOLD);
                    totalRevenueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

                    JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
                    sep.setPreferredSize(new Dimension(2, 18));
                    sep.setForeground(BORDER_COLOR);

                    metrics.add(totalOrdersCountLabel);
                    metrics.add(sep);
                    metrics.add(totalRevenueLabel);

                    header.add(title, BorderLayout.WEST);
                    header.add(metrics, BorderLayout.EAST);
                    return header;
          }

          // ===================== BOOKING TAB =====================
          private static JPanel createBookingInterface() {
                    JPanel container = new JPanel(new GridLayout(1, 2, 16, 0));
                    container.setBackground(BG_DARK);
                    container.setBorder(new EmptyBorder(14, 16, 14, 16));

                    // ---------- LEFT CARD ----------
                    JPanel leftCard = new JPanel(new GridBagLayout());
                    leftCard.setBackground(CARD_BG);
                    leftCard.setBorder(BorderFactory.createCompoundBorder(
                                        new LineBorder(BORDER_COLOR, 1, true),
                                        new EmptyBorder(18, 20, 18, 20)));

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.weightx = 1.0;
                    gbc.weighty = 0;
                    gbc.gridx = 0;
                    gbc.anchor = GridBagConstraints.NORTHWEST;

                    // 1. Category dropdown
                    gbc.gridy = 0;
                    gbc.insets = new Insets(0, 0, 6, 0);
                    leftCard.add(createHeaderLabel("1.  Filter Genre / Category"), gbc);

                    JComboBox<String> categoryCombo = createStyledCombo(categoryMap.keySet().toArray(new String[0]));
                    gbc.gridy = 1;
                    // নিচে POPUP_GAP পরিমাণ ফাঁকা — ড্রপডাউন খুললে এই জায়গার মধ্যেই পপআপ থাকবে,
                    // মুভি লিস্ট ঢাকবে না
                    gbc.insets = new Insets(0, 0, POPUP_GAP, 0);
                    leftCard.add(categoryCombo, gbc);

                    // 2. Movie list
                    gbc.gridy = 2;
                    gbc.insets = new Insets(0, 0, 6, 0);
                    leftCard.add(createHeaderLabel("2.  Select Movie Title"), gbc);

                    movieListModel = new DefaultListModel<>();
                    movieJList = new JList<>(movieListModel);
                    movieJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    movieJList.setBackground(BG_DARK);
                    movieJList.setForeground(TEXT_WHITE);
                    movieJList.setFixedCellHeight(34);
                    movieJList.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    movieJList.setCellRenderer(new DefaultListCellRenderer() {
                              @Override
                              public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
                                        JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index,
                                                            isSelected, false);
                                        l.setBorder(new EmptyBorder(0, 12, 0, 12));
                                        l.setBackground(isSelected ? ACCENT_RED : BG_DARK);
                                        l.setForeground(isSelected ? Color.WHITE : TEXT_WHITE);
                                        return l;
                              }
                    });

                    JScrollPane movieScroll = new JScrollPane(movieJList);
                    movieScroll.setPreferredSize(new Dimension(300, 3 * 34 + 4));
                    movieScroll.setMinimumSize(new Dimension(300, 3 * 34 + 4));
                    movieScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
                    movieScroll.getViewport().setBackground(BG_DARK);

                    gbc.gridy = 3;
                    gbc.insets = new Insets(0, 0, 20, 0);
                    leftCard.add(movieScroll, gbc);

                    // 3. Showtime
                    gbc.gridy = 4;
                    gbc.insets = new Insets(0, 0, 6, 0);
                    leftCard.add(createHeaderLabel("3.  Choose Showtime"), gbc);

                    JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 2));
                    timePanel.setBackground(CARD_BG);
                    btnMorning = new JRadioButton("11:30 AM (Matinee)", true);
                    btnEvening = new JRadioButton("05:00 PM (Prime)");
                    btnNight = new JRadioButton("08:30 PM (Night)");
                    styleRadioButton(btnMorning);
                    styleRadioButton(btnEvening);
                    styleRadioButton(btnNight);

                    ButtonGroup timeGroup = new ButtonGroup();
                    timeGroup.add(btnMorning);
                    timeGroup.add(btnEvening);
                    timeGroup.add(btnNight);
                    timePanel.add(btnMorning);
                    timePanel.add(btnEvening);
                    timePanel.add(btnNight);

                    gbc.gridy = 5;
                    gbc.insets = new Insets(0, 0, 20, 0);
                    leftCard.add(timePanel, gbc);

                    // 4. Concessions
                    gbc.gridy = 6;
                    gbc.insets = new Insets(0, 0, 6, 0);
                    leftCard.add(createHeaderLabel("4.  Concessions & Refreshments"), gbc);

                    JPanel foodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 2));
                    foodPanel.setBackground(CARD_BG);
                    chkPopcorn = new JCheckBox("Popcorn Combo (+$4.50)");
                    chkDrinks = new JCheckBox("Jumbo Cola (+$2.50)");
                    chkNachos = new JCheckBox("Cheese Nachos (+$3.50)");
                    styleCheckBox(chkPopcorn);
                    styleCheckBox(chkDrinks);
                    styleCheckBox(chkNachos);
                    foodPanel.add(chkPopcorn);
                    foodPanel.add(chkDrinks);
                    foodPanel.add(chkNachos);

                    gbc.gridy = 7;
                    gbc.insets = new Insets(0, 0, 0, 0);
                    leftCard.add(foodPanel, gbc);

                    // বাকি ফাঁকা জায়গা নিচে ঠেলে দেওয়ার জন্য filler
                    gbc.gridy = 8;
                    gbc.weighty = 1.0;
                    gbc.fill = GridBagConstraints.BOTH;
                    JPanel filler = new JPanel();
                    filler.setOpaque(false);
                    leftCard.add(filler, gbc);

                    // ---------- RIGHT CARD ----------
                    JPanel rightCard = new JPanel(new BorderLayout(0, 12));
                    rightCard.setBackground(CARD_BG);
                    rightCard.setBorder(BorderFactory.createCompoundBorder(
                                        new LineBorder(BORDER_COLOR, 1, true),
                                        new EmptyBorder(18, 20, 18, 20)));

                    JPanel screenPanel = new JPanel(new BorderLayout(0, 12));
                    screenPanel.setBackground(CARD_BG);

                    JLabel screenLabel = new JLabel("— FRONT CINEMA SCREEN —", SwingConstants.CENTER);
                    screenLabel.setOpaque(true);
                    screenLabel.setBackground(FIELD_BG);
                    screenLabel.setForeground(ACCENT_GOLD);
                    screenLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                    screenLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
                    screenPanel.add(screenLabel, BorderLayout.NORTH);

                    JPanel seatMatrix = new JPanel(new GridLayout(3, 5, 10, 10));
                    seatMatrix.setBackground(CARD_BG);
                    seatMatrix.setBorder(new EmptyBorder(6, 10, 6, 10));

                    String[] rows = { "A", "B", "C" };
                    for (String r : rows) {
                              for (int col = 1; col <= 5; col++) {
                                        String seatId = r + col;
                                        JButton seatBtn = new JButton(seatId);
                                        seatBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
                                        seatBtn.setPreferredSize(new Dimension(64, 44));
                                        seatBtn.setOpaque(true);
                                        seatBtn.setContentAreaFilled(true);
                                        seatBtn.setFocusPainted(false);
                                        seatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                        seatBtn.setBorder(new LineBorder(BORDER_COLOR, 1, true));
                                        seatBtn.addActionListener(e -> toggleSeatSelection(seatId, seatBtn));
                                        seatButtons.put(seatId, seatBtn);
                                        seatMatrix.add(seatBtn);
                              }
                    }
                    screenPanel.add(seatMatrix, BorderLayout.CENTER);

                    // Legend (কোন রঙের মানে কী)
                    JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
                    legend.setBackground(CARD_BG);
                    legend.add(createLegendItem(SEAT_AVAILABLE, "Available"));
                    legend.add(createLegendItem(SEAT_SELECTED, "Selected"));
                    legend.add(createLegendItem(SEAT_BOOKED, "Booked"));
                    screenPanel.add(legend, BorderLayout.SOUTH);

                    rightCard.add(screenPanel, BorderLayout.NORTH);

                    receiptArea = new JTextArea();
                    receiptArea.setEditable(false);
                    receiptArea.setBackground(BG_DARK);
                    receiptArea.setForeground(SEAT_SELECTED);
                    receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    receiptArea.setMargin(new Insets(10, 12, 10, 12));
                    receiptArea.setText("Ready for booking. Select movie, showtime, and click seats above.\n");
                    JScrollPane receiptScroll = new JScrollPane(receiptArea);
                    receiptScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
                    receiptScroll.getViewport().setBackground(BG_DARK);
                    rightCard.add(receiptScroll, BorderLayout.CENTER);

                    container.add(leftCard);
                    container.add(rightCard);

                    // ---------- Listeners ----------
                    categoryCombo.addActionListener(e -> {
                              populateMovieList((String) categoryCombo.getSelectedItem());
                              updateSummaryCalculations();
                    });

                    movieJList.addListSelectionListener(e -> {
                              if (!e.getValueIsAdjusting())
                                        updateSummaryCalculations();
                    });

                    btnMorning.addActionListener(e -> onShowtimeSwitched());
                    btnEvening.addActionListener(e -> onShowtimeSwitched());
                    btnNight.addActionListener(e -> onShowtimeSwitched());

                    chkPopcorn.addActionListener(e -> updateSummaryCalculations());
                    chkDrinks.addActionListener(e -> updateSummaryCalculations());
                    chkNachos.addActionListener(e -> updateSummaryCalculations());

                    populateMovieList((String) categoryCombo.getSelectedItem());

                    return container;
          }

          // ===================== HISTORY TAB =====================
          private static JPanel createHistoryInterface() {
                    JPanel historyPanel = new JPanel(new BorderLayout(0, 10));
                    historyPanel.setBackground(BG_DARK);
                    historyPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

                    String[] cols = { "Order ID", "Timestamp", "Movie Title", "Showtime", "Seats", "Total Paid" };
                    historyTableModel = new DefaultTableModel(cols, 0) {
                              @Override
                              public boolean isCellEditable(int row, int col) {
                                        return false;
                              }
                    };

                    JTable historyTable = new JTable(historyTableModel);
                    historyTable.setBackground(CARD_BG);
                    historyTable.setForeground(TEXT_WHITE);
                    historyTable.setGridColor(BORDER_COLOR);
                    historyTable.setRowHeight(30);
                    historyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    historyTable.setSelectionBackground(ACCENT_RED);
                    historyTable.setSelectionForeground(Color.WHITE);

                    JTableHeader header = historyTable.getTableHeader();
                    header.setBackground(FIELD_BG);
                    header.setForeground(TEXT_WHITE);
                    header.setFont(new Font("SansSerif", Font.BOLD, 12));

                    DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
                    centerRender.setHorizontalAlignment(JLabel.CENTER);
                    for (int i = 0; i < historyTable.getColumnCount(); i++) {
                              historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRender);
                    }

                    JScrollPane scroll = new JScrollPane(historyTable);
                    scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
                    scroll.getViewport().setBackground(CARD_BG);
                    historyPanel.add(scroll, BorderLayout.CENTER);
                    return historyPanel;
          }

          private static JPanel createBottomActionBar() {
                    JPanel bottomBar = new JPanel(new BorderLayout());
                    bottomBar.setBackground(CARD_BG);
                    bottomBar.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                                        new EmptyBorder(12, 24, 12, 24)));

                    priceBreakdownLabel = new JLabel("Seats Chosen: 0  |  Total Estimated: $0.00");
                    priceBreakdownLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
                    priceBreakdownLabel.setForeground(ACCENT_GOLD);
                    bottomBar.add(priceBreakdownLabel, BorderLayout.WEST);

                    JButton confirmBtn = new JButton("Confirm & Generate Digital Ticket");
                    confirmBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
                    confirmBtn.setBackground(ACCENT_RED);
                    confirmBtn.setForeground(Color.WHITE);
                    confirmBtn.setOpaque(true);
                    confirmBtn.setContentAreaFilled(true);
                    confirmBtn.setFocusPainted(false);
                    confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    confirmBtn.setBorder(new EmptyBorder(11, 22, 11, 22));
                    confirmBtn.addActionListener(e -> executeBooking());
                    bottomBar.add(confirmBtn, BorderLayout.EAST);

                    return bottomBar;
          }

          // ===================== LOGIC =====================
          private static void populateMovieList(String category) {
                    if (movieListModel == null)
                              return;
                    movieListModel.clear();
                    List<Movie> list = categoryMap.get(category);
                    if (list != null && !list.isEmpty()) {
                              for (Movie m : list)
                                        movieListModel.addElement(m);
                              if (movieJList != null) {
                                        movieJList.setSelectedIndex(0);
                              }
                    }
          }

          private static String getSelectedShowtime() {
                    if (btnMorning != null && btnMorning.isSelected())
                              return btnMorning.getText();
                    if (btnEvening != null && btnEvening.isSelected())
                              return btnEvening.getText();
                    if (btnNight != null && btnNight.isSelected())
                              return btnNight.getText();
                    return "11:30 AM (Matinee)";
          }

          private static void onShowtimeSwitched() {
                    currentlySelectedSeats.clear();
                    refreshSeatGridDisplay();
                    updateSummaryCalculations();
          }

          private static void toggleSeatSelection(String seatId, JButton btn) {
                    Set<String> booked = bookedSeatsPerShowtime.computeIfAbsent(getSelectedShowtime(),
                                        k -> new HashSet<>());
                    if (booked.contains(seatId))
                              return;

                    if (currentlySelectedSeats.contains(seatId)) {
                              currentlySelectedSeats.remove(seatId);
                              btn.setBackground(SEAT_AVAILABLE);
                              btn.setForeground(TEXT_WHITE);
                    } else {
                              currentlySelectedSeats.add(seatId);
                              btn.setBackground(SEAT_SELECTED);
                              btn.setForeground(Color.WHITE);
                    }
                    updateSummaryCalculations();
          }

          private static void refreshSeatGridDisplay() {
                    if (seatButtons.isEmpty())
                              return;
                    Set<String> booked = bookedSeatsPerShowtime.computeIfAbsent(getSelectedShowtime(),
                                        k -> new HashSet<>());
                    for (Map.Entry<String, JButton> entry : seatButtons.entrySet()) {
                              String seatId = entry.getKey();
                              JButton btn = entry.getValue();

                              if (booked.contains(seatId)) {
                                        btn.setBackground(SEAT_BOOKED);
                                        btn.setForeground(new Color(130, 140, 150));
                                        btn.setEnabled(false);
                                        btn.setText(seatId + " ✖");
                              } else if (currentlySelectedSeats.contains(seatId)) {
                                        btn.setBackground(SEAT_SELECTED);
                                        btn.setForeground(Color.WHITE);
                                        btn.setEnabled(true);
                                        btn.setText(seatId);
                              } else {
                                        btn.setBackground(SEAT_AVAILABLE);
                                        btn.setForeground(TEXT_WHITE);
                                        btn.setEnabled(true);
                                        btn.setText(seatId);
                              }
                    }
          }

          private static double calculateTotal() {
                    if (movieJList == null)
                              return 0.0;
                    Movie movie = movieJList.getSelectedValue();
                    if (movie == null || currentlySelectedSeats.isEmpty())
                              return 0.0;

                    double ticketsCost = movie.baseTicketPrice * currentlySelectedSeats.size();
                    double snacksCost = 0.0;
                    if (chkPopcorn != null && chkPopcorn.isSelected())
                              snacksCost += 4.50;
                    if (chkDrinks != null && chkDrinks.isSelected())
                              snacksCost += 2.50;
                    if (chkNachos != null && chkNachos.isSelected())
                              snacksCost += 3.50;

                    return ticketsCost + snacksCost;
          }

          private static void updateSummaryCalculations() {
                    if (priceBreakdownLabel == null)
                              return;
                    double total = calculateTotal();
                    priceBreakdownLabel.setText(String.format("Seats Chosen: %d  |  Estimated Total: $%s",
                                        currentlySelectedSeats.size(), df.format(total)));
          }

          private static void executeBooking() {
                    if (movieJList == null)
                              return;
                    Movie movie = movieJList.getSelectedValue();
                    if (movie == null) {
                              JOptionPane.showMessageDialog(null, "Please choose a movie title!", "No Movie Selected",
                                                  JOptionPane.WARNING_MESSAGE);
                              return;
                    }
                    if (currentlySelectedSeats.isEmpty()) {
                              JOptionPane.showMessageDialog(null,
                                                  "Please pick at least one available seat from the grid!",
                                                  "Seat Selection Empty", JOptionPane.WARNING_MESSAGE);
                              return;
                    }

                    String showtime = getSelectedShowtime();
                    double total = calculateTotal();
                    String orderId = "CNX-" + (1000 + new Random().nextInt(9000));
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    Set<String> booked = bookedSeatsPerShowtime.computeIfAbsent(showtime, k -> new HashSet<>());
                    booked.addAll(currentlySelectedSeats);

                    totalOrderCount++;
                    totalRevenue += total;
                    if (totalOrdersCountLabel != null)
                              totalOrdersCountLabel.setText("Orders: " + totalOrderCount);
                    if (totalRevenueLabel != null)
                              totalRevenueLabel.setText("Revenue: $" + df.format(totalRevenue));

                    if (historyTableModel != null) {
                              historyTableModel.addRow(new Object[] {
                                                  orderId, timestamp, movie.title, showtime,
                                                  currentlySelectedSeats.toString(), "$" + df.format(total)
                              });
                    }

                    StringBuilder receipt = new StringBuilder();
                    receipt.append("===================================================\n");
                    receipt.append("          CINEFLIX MULTIPLEX DIGITAL TICKET        \n");
                    receipt.append("===================================================\n");
                    receipt.append("Booking ID : ").append(orderId).append("\n");
                    receipt.append("Timestamp  : ").append(timestamp).append("\n");
                    receipt.append("Movie Title: ").append(movie.title).append("\n");
                    receipt.append("Showtime   : ").append(showtime).append("\n");
                    receipt.append("Seats (Locked): ").append(currentlySelectedSeats).append(" (")
                                        .append(currentlySelectedSeats.size()).append(" Seat/s)\n");

                    List<String> snacks = new ArrayList<>();
                    if (chkPopcorn != null && chkPopcorn.isSelected())
                              snacks.add("Popcorn");
                    if (chkDrinks != null && chkDrinks.isSelected())
                              snacks.add("Cola");
                    if (chkNachos != null && chkNachos.isSelected())
                              snacks.add("Nachos");
                    receipt.append("Concessions: ").append(snacks.isEmpty() ? "None" : String.join(", ", snacks))
                                        .append("\n");
                    receipt.append("---------------------------------------------------\n");
                    receipt.append("TOTAL PAID : $").append(df.format(total)).append(" (PAID IN FULL)\n");
                    receipt.append("Status     : CONFIRMED (Seats marked unavailable)\n");
                    receipt.append("===================================================");

                    if (receiptArea != null) {
                              receiptArea.setText(receipt.toString());
                    }

                    currentlySelectedSeats.clear();
                    if (chkPopcorn != null)
                              chkPopcorn.setSelected(false);
                    if (chkDrinks != null)
                              chkDrinks.setSelected(false);
                    if (chkNachos != null)
                              chkNachos.setSelected(false);
                    refreshSeatGridDisplay();
                    updateSummaryCalculations();

                    JOptionPane.showMessageDialog(null,
                                        "Booking Successful! Ticket ID: " + orderId
                                                            + "\nSeats have been permanently locked.",
                                        "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
          }

          // ===================== UI HELPERS =====================
          private static JLabel createHeaderLabel(String text) {
                    JLabel l = new JLabel(text);
                    l.setFont(new Font("SansSerif", Font.BOLD, 13));
                    l.setForeground(ACCENT_CYAN);
                    return l;
          }

          private static void styleRadioButton(JRadioButton rb) {
                    rb.setOpaque(false);
                    rb.setForeground(TEXT_WHITE);
                    rb.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    rb.setFocusPainted(false);
                    rb.setIcon(new ToggleIcon(true));
                    rb.setIconTextGap(8);
                    rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
          }

          private static void styleCheckBox(JCheckBox cb) {
                    cb.setOpaque(false);
                    cb.setForeground(TEXT_WHITE);
                    cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    cb.setFocusPainted(false);
                    cb.setIcon(new ToggleIcon(false));
                    cb.setIconTextGap(8);
                    cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
          }

          private static JLabel createLegendItem(Color color, String text) {
                    JLabel l = new JLabel(text, new SquareIcon(color), SwingConstants.LEFT);
                    l.setForeground(TEXT_MUTED);
                    l.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    l.setIconTextGap(6);
                    return l;
          }

          /**
           * ড্রপডাউন: সম্পূর্ণ ডার্ক থিম।
           * আগের সমস্যা: ফোকাস থাকলে Swing উপরের অংশে সাদাটে সিলেকশন কালার বসিয়ে দিত।
           * এখন উপরের অংশ (ব্যাকগ্রাউন্ড + লেখা) আমরা নিজেরাই আঁকছি, তাই সবসময় গাঢ়
           * ব্যাকগ্রাউন্ডে সাদা লেখা।
           */
          private static JComboBox<String> createStyledCombo(String[] items) {
                    JComboBox<String> combo = new JComboBox<>(items);
                    combo.setUI(new BasicComboBoxUI() {
                              @Override
                              protected JButton createArrowButton() {
                                        BasicArrowButton b = new BasicArrowButton(SwingConstants.SOUTH,
                                                            FIELD_BG, FIELD_BG, ACCENT_GOLD, FIELD_BG);
                                        b.setBorder(new EmptyBorder(0, 6, 0, 6));
                                        return b;
                              }

                              @Override
                              protected ComboPopup createPopup() {
                                        BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                                        popup.setBorder(new LineBorder(ACCENT_CYAN, 2));
                                        popup.getList().setBackground(FIELD_BG);
                                        popup.getList().setForeground(TEXT_WHITE);
                                        popup.getList().setSelectionBackground(ACCENT_CYAN);
                                        popup.getList().setSelectionForeground(Color.WHITE);
                                        return popup;
                              }

                              // উপরের দেখানো অংশের ব্যাকগ্রাউন্ড — সবসময় গাঢ়
                              @Override
                              public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                                        g.setColor(FIELD_BG);
                                        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                              }

                              // উপরের দেখানো অংশের লেখা — সবসময় সাদা
                              @Override
                              public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                                        Object item = comboBox.getSelectedItem();
                                        if (item == null)
                                                  return;
                                        Graphics2D g2 = (Graphics2D) g.create();
                                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                        g2.setFont(comboBox.getFont());
                                        g2.setColor(TEXT_WHITE);
                                        FontMetrics fm = g2.getFontMetrics();
                                        int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
                                        g2.drawString(item.toString(), bounds.x + 14, textY);
                                        g2.dispose();
                              }
                    });

                    // পপআপের আইটেমের রেন্ডারার
                    combo.setRenderer(new DefaultListCellRenderer() {
                              @Override
                              public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
                                        JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index,
                                                            isSelected, false);
                                        l.setBorder(new EmptyBorder(6, 12, 6, 12));
                                        l.setBackground(isSelected ? ACCENT_CYAN : FIELD_BG);
                                        l.setForeground(Color.WHITE);
                                        return l;
                              }
                    });

                    combo.setBackground(FIELD_BG);
                    combo.setForeground(TEXT_WHITE);
                    combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    combo.setBorder(new LineBorder(BORDER_COLOR, 1));
                    combo.setPreferredSize(new Dimension(300, 38));
                    combo.setMaximumRowCount(6);
                    combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    return combo;
          }

          // ===================== CUSTOM ICONS =====================
          /** রেডিও বাটন (গোল) ও চেকবক্স (চৌকো) এর কাস্টম আইকন */
          static class ToggleIcon implements Icon {
                    private final boolean round;
                    private static final int SIZE = 16;

                    ToggleIcon(boolean round) {
                              this.round = round;
                    }

                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                              boolean selected = ((AbstractButton) c).isSelected();
                              Graphics2D g2 = (Graphics2D) g.create();
                              g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                              g2.setColor(selected ? ACCENT_RED : BG_DARK);
                              if (round)
                                        g2.fillOval(x, y, SIZE, SIZE);
                              else
                                        g2.fillRoundRect(x, y, SIZE, SIZE, 5, 5);

                              g2.setColor(selected ? ACCENT_RED : TEXT_MUTED);
                              g2.setStroke(new BasicStroke(1.5f));
                              if (round)
                                        g2.drawOval(x, y, SIZE, SIZE);
                              else
                                        g2.drawRoundRect(x, y, SIZE, SIZE, 5, 5);

                              g2.setColor(Color.WHITE);
                              if (selected) {
                                        if (round) {
                                                  g2.fillOval(x + 5, y + 5, 7, 7);
                                        } else {
                                                  g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                                                                      BasicStroke.JOIN_ROUND));
                                                  g2.drawLine(x + 4, y + 8, x + 7, y + 12);
                                                  g2.drawLine(x + 7, y + 12, x + 13, y + 4);
                                        }
                              }
                              g2.dispose();
                    }

                    @Override
                    public int getIconWidth() {
                              return SIZE + 2;
                    }

                    @Override
                    public int getIconHeight() {
                              return SIZE + 2;
                    }
          }

          /** লেজেন্ডের ছোট রঙিন বক্স */
          static class SquareIcon implements Icon {
                    private final Color color;

                    SquareIcon(Color color) {
                              this.color = color;
                    }

                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                              g.setColor(color);
                              g.fillRoundRect(x, y, 14, 14, 4, 4);
                              g.setColor(BORDER_COLOR);
                              g.drawRoundRect(x, y, 14, 14, 4, 4);
                    }

                    @Override
                    public int getIconWidth() {
                              return 15;
                    }

                    @Override
                    public int getIconHeight() {
                              return 15;
                    }
          }

          // ===================== CUSTOM TAB STYLE =====================
          /** ডার্ক থিমের ট্যাব: সিলেক্টেড ট্যাব লাল, লেখা সবসময় স্পষ্ট */
          static class DarkTabbedPaneUI extends BasicTabbedPaneUI {
                    @Override
                    protected void installDefaults() {
                              super.installDefaults();
                              tabInsets = new Insets(9, 22, 9, 22);
                              selectedTabPadInsets = new Insets(0, 0, 0, 0);
                              tabAreaInsets = new Insets(6, 8, 0, 0);
                              contentBorderInsets = new Insets(0, 0, 0, 0);
                    }

                    @Override
                    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                        int x, int y, int w, int h, boolean isSelected) {
                              g.setColor(isSelected ? ACCENT_RED : CARD_BG);
                              g.fillRect(x, y, w, h);
                    }

                    @Override
                    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                        int x, int y, int w, int h, boolean isSelected) {
                              g.setColor(BORDER_COLOR);
                              g.drawRect(x, y, w - 1, h);
                    }

                    @Override
                    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                        int tabIndex, Rectangle iconRect, Rectangle textRect,
                                        boolean isSelected) {
                              // ডটেড ফোকাস লাইন বাদ
                    }

                    @Override
                    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                              // ডিফল্ট সাদা বর্ডার বাদ
                    }
          }
}