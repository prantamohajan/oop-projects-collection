import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Hospital Medical Equipment Management System - Graphical User Interface.
 *
 * Pure Swing (no external library), so it runs with any JDK 8 or newer.
 * The GUI only talks to the model classes (Hospital, MedicalEquipment,
 * MaintenanceScheduler ...) and never changes their data directly.
 */
public class HospitalGUI extends JFrame {

    // ------------------------------------------------------------------ theme
    static final Color NAVY      = new Color(0x0B3C5D);
    static final Color BLUE      = new Color(0x1D6FA5);
    static final Color TEAL      = new Color(0x0EA5A4);
    static final Color BG        = new Color(0xF1F5F9);
    static final Color CARD      = Color.WHITE;
    static final Color TEXT      = new Color(0x0F172A);
    static final Color MUTED     = new Color(0x64748B);
    static final Color BORDER    = new Color(0xE2E8F0);
    static final Color FIELD_BORDER = new Color(0xCBD5E1);
    static final Color ZEBRA     = new Color(0xF8FAFC);
    static final Color HOVER     = new Color(0xEFF6FF);
    static final Color SELECTED  = new Color(0xDBEAFE);
    static final Color GREEN     = new Color(0x16A34A);
    static final Color GREEN_BG  = new Color(0xDCFCE7);
    static final Color AMBER     = new Color(0xD97706);
    static final Color AMBER_BG  = new Color(0xFEF3C7);
    static final Color RED       = new Color(0xDC2626);
    static final Color RED_BG    = new Color(0xFEE2E2);
    static final Color PURPLE    = new Color(0x7C3AED);
    static final Color SLATE     = new Color(0x475569);

    private static String fontFamily;

    static Font font(int style, float size) {
        if (fontFamily == null) {
            String[] wanted = {"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Roboto",
                    "Noto Sans", "Ubuntu", "DejaVu Sans"};
            java.util.Set<String> have = new java.util.HashSet<>(java.util.Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            fontFamily = Font.SANS_SERIF;
            for (String f : wanted) {
                if (have.contains(f)) {
                    fontFamily = f;
                    break;
                }
            }
        }
        return new Font(fontFamily, style, 1).deriveFont(style, size);
    }

    static void aa(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    static Color blend(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    // ------------------------------------------------------------ model layer
    private Hospital hospital;
    private MaintenanceScheduler scheduler;

    // ------------------------------------------------------------- UI fields
    private JTable equipmentTable;
    private DefaultTableModel tableModel;
    private JTable pendingTable, overdueTable;
    private DefaultTableModel pendingModel, overdueModel;
    private JTextPane logPane;
    private JLabel clockLabel;
    private JLabel countLabel;
    private StatCard totalCard, operationalCard, maintenanceCard, outCard, overdueCard;
    private SearchField searchField;
    private JComboBox<String> typeFilter, statusFilter;
    private JPanel detailsBody;
    private RoundedButton btnStart, btnStop, btnAction, btnSchedule, btnService, btnHistory, btnRemove;
    private RoundedButton tabInventory, tabMaintenance;
    private CardLayout pages;
    private JPanel pagePanel;
    private int hoverRow = -1;
    private int clockTicks = 0;

    public HospitalGUI() {
        setTitle("Hospital Medical Equipment Management System - CSE 1115");
        setIconImage(createAppIcon());
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1280, scr.width - 40), Math.min(820, scr.height - 60));
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initBackend();
        initComponents();
        captureConsole();
        refreshAll();
        log("OK", "System initialized. Connected to " + hospital.getName() + " inventory ("
                + hospital.getEquipmentList().size() + " devices loaded).");
        startClock();
    }

    // ================================================================ BACKEND
    private void initBackend() {
        hospital = new Hospital("Premier Central Hospital");
        scheduler = new MaintenanceScheduler();

        PatientMonitor pm = new PatientMonitor("EQ-PM-01", "ICU Bedside Monitor", "Philips", "IntelliVue MX800");
        Ventilator vn = new Ventilator("EQ-VN-02", "Critical Care Ventilator", "Dräger", "Evita V800");
        InfusionPump ip = new InfusionPump("EQ-IP-03", "Smart Infusion Pump", "Baxter", "Sigma Spectrum");
        ECGMachine ecg = new ECGMachine("EQ-ECG-04", "12-Lead Diagnostic ECG", "GE Healthcare", "MAC 2000", 12);
        PortableUltrasound us = new PortableUltrasound("EQ-US-05", "Point-of-Care Ultrasound", "Mindray", "M9", "Convex");

        hospital.addEquipment(pm);
        hospital.addEquipment(vn);
        hospital.addEquipment(ip);
        hospital.addEquipment(ecg);
        hospital.addEquipment(us);

        // Sample service history so the dashboard looks alive on first start
        seedHistory(pm, -45, "Engr. Rahim", 135);
        seedHistory(vn, -120, "Engr. Kamal", 60);
        seedHistory(ip, -200, "Engr. Sultana", -20);   // overdue on purpose (FR5 demo)
        seedHistory(ecg, -30, "Engr. Rahim", 150);
        us.scheduleMaintenance(daysFromNow(30));
    }

    private void seedHistory(MedicalEquipment eq, int daysAgo, String tech, int nextInDays) {
        eq.addMaintenanceRecord(new MaintenanceRecord("REC-1", eq.getEquipmentId(), daysFromNow(daysAgo),
                tech, "Routine calibration & inspection completed.", true));
        eq.scheduleMaintenance(daysFromNow(nextInDays));
    }

    private static Date daysFromNow(int days) {
        return new Date(System.currentTimeMillis() + days * 86400000L);
    }

    // ===================================================================== UI
    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 18, 8, 18));
        content.add(buildStatsRow(), BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setOpaque(false);
        centre.add(buildTabRow(), BorderLayout.NORTH);

        pages = new CardLayout();
        pagePanel = new JPanel(pages);
        pagePanel.setOpaque(false);
        pagePanel.add(buildInventoryPage(), "inventory");
        pagePanel.add(buildMaintenancePage(), "maintenance");
        centre.add(pagePanel, BorderLayout.CENTER);
        content.add(centre, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, content, buildLogPanel());
        split.setBorder(null);
        split.setDividerSize(6);
        split.setResizeWeight(1.0);
        split.setOpaque(false);
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(BG);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
        split.setBorder(new EmptyBorder(0, 0, 0, 0));
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(split.getHeight() - 160));
        showPage("inventory");
    }

    // ---------------------------------------------------------------- header
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                aa(g2);
                g2.setPaint(new GradientPaint(0, 0, NAVY, getWidth(), 0, BLUE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(getWidth() - 260, -120, 300, 300);
                g2.fillOval(getWidth() - 120, 10, 200, 200);
                g2.setColor(TEAL);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 20, 15, 22));

        JLabel logo = new JLabel(new ImageIcon(createLogo(46)));
        JLabel title = new JLabel("Hospital Medical Equipment Management System");
        title.setFont(font(Font.BOLD, 20f));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Object-Oriented Logistics & Safety Dashboard  |  CSE 1115  |  Premier University, Chittagong");
        sub.setFont(font(Font.PLAIN, 12f));
        sub.setForeground(new Color(0xBFDBFE));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);

        JPanel left = new JPanel(new BorderLayout(14, 0));
        left.setOpaque(false);
        left.add(logo, BorderLayout.WEST);
        left.add(titles, BorderLayout.CENTER);

        JLabel hosp = new JLabel(hospital.getName(), SwingConstants.RIGHT);
        hosp.setFont(font(Font.BOLD, 14f));
        hosp.setForeground(Color.WHITE);
        clockLabel = new JLabel("", SwingConstants.RIGHT);
        clockLabel.setFont(font(Font.PLAIN, 12f));
        clockLabel.setForeground(new Color(0xBFDBFE));
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 2));
        right.setOpaque(false);
        right.add(hosp);
        right.add(clockLabel);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void startClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy   HH:mm:ss");
        clockLabel.setText(sdf.format(new Date()));
        new javax.swing.Timer(1000, e -> {
            clockLabel.setText(sdf.format(new Date()));
            if (++clockTicks % 60 == 0) {      // keep "overdue" flags fresh
                refreshAll();
            }
        }).start();
    }

    // ----------------------------------------------------------------- stats
    private JComponent buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);
        totalCard = new StatCard("Total Devices", BLUE);
        operationalCard = new StatCard("Operational", GREEN);
        maintenanceCard = new StatCard("Under Maintenance", AMBER);
        outCard = new StatCard("Out of Service", RED);
        overdueCard = new StatCard("Maintenance Overdue", PURPLE);
        row.add(totalCard);
        row.add(operationalCard);
        row.add(maintenanceCard);
        row.add(outCard);
        row.add(overdueCard);
        return row;
    }

    private JComponent buildTabRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        tabInventory = new RoundedButton("Equipment Inventory", NAVY, false);
        tabMaintenance = new RoundedButton("Maintenance Schedule", NAVY, true);
        tabInventory.addActionListener(e -> showPage("inventory"));
        tabMaintenance.addActionListener(e -> showPage("maintenance"));
        row.add(tabInventory);
        row.add(tabMaintenance);
        return row;
    }

    private void showPage(String page) {
        boolean inv = "inventory".equals(page);
        pages.show(pagePanel, page);
        tabInventory.setOutline(!inv);
        tabMaintenance.setOutline(inv);
    }

    // ------------------------------------------------------ inventory page
    private JComponent buildInventoryPage() {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);

        // filter bar
        CardPanel filterBar = new CardPanel(new BorderLayout(10, 0));
        filterBar.setBorder(new EmptyBorder(10, 14, 12, 14));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);

        searchField = new SearchField("Search by ID, name, manufacturer, model...");
        searchField.setPreferredSize(new Dimension(300, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshTable(); }
            public void removeUpdate(DocumentEvent e) { refreshTable(); }
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });

        String[] types = new String[EquipmentFactory.TYPES.length + 1];
        types[0] = "All Types";
        System.arraycopy(EquipmentFactory.TYPES, 0, types, 1, EquipmentFactory.TYPES.length);
        typeFilter = styledCombo(types);
        EquipmentStatus[] st = EquipmentStatus.values();
        String[] statuses = new String[st.length + 1];
        statuses[0] = "All Statuses";
        for (int i = 0; i < st.length; i++) statuses[i + 1] = st[i].getLabel();
        statusFilter = styledCombo(statuses);
        typeFilter.addActionListener(e -> refreshTable());
        statusFilter.addActionListener(e -> refreshTable());

        RoundedButton clear = new RoundedButton("Clear", SLATE, true);
        clear.addActionListener(e -> {
            searchField.setText("");
            typeFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
        });
        filters.add(searchField);
        filters.add(typeFilter);
        filters.add(statusFilter);
        filters.add(clear);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightBar.setOpaque(false);
        countLabel = new JLabel("");
        countLabel.setFont(font(Font.PLAIN, 12f));
        countLabel.setForeground(MUTED);
        RoundedButton add = new RoundedButton("+  Add Device", TEAL, false);
        add.addActionListener(e -> handleAddDevice());
        rightBar.add(countLabel);
        rightBar.add(add);

        filterBar.add(filters, BorderLayout.WEST);
        filterBar.add(rightBar, BorderLayout.EAST);
        page.add(filterBar, BorderLayout.NORTH);

        // table
        String[] columns = {"Equipment ID", "Device Name", "Type", "Manufacturer / Model", "Status", "Next Service"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        equipmentTable = new JTable(tableModel);
        styleTable(equipmentTable);
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {125, 170, 135, 160, 165, 175};
        for (int i = 0; i < widths.length; i++) {
            equipmentTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        equipmentTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        equipmentTable.getColumnModel().getColumn(5).setCellRenderer(new DueDateRenderer());
        equipmentTable.getColumnModel().getColumn(0).setCellRenderer(new BoldRenderer());
        equipmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetails();
                updateButtons();
            }
        });
        equipmentTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int r = equipmentTable.rowAtPoint(e.getPoint());
                if (r != hoverRow) {
                    hoverRow = r;
                    equipmentTable.repaint();
                }
            }
        });
        equipmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                equipmentTable.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && equipmentTable.getSelectedRow() >= 0) {
                    handleHistory();
                }
            }
        });

        CardPanel tableCard = new CardPanel(new BorderLayout(0, 8));
        JLabel tt = new JLabel("Live Equipment Inventory");
        tt.setFont(font(Font.BOLD, 14f));
        tt.setForeground(TEXT);
        JLabel hint = new JLabel("Double-click a row to open its service history");
        hint.setFont(font(Font.PLAIN, 11.5f));
        hint.setForeground(MUTED);
        JPanel tHead = new JPanel(new BorderLayout());
        tHead.setOpaque(false);
        tHead.add(tt, BorderLayout.WEST);
        tHead.add(hint, BorderLayout.EAST);
        tableCard.add(tHead, BorderLayout.NORTH);
        tableCard.add(styledScroll(equipmentTable), BorderLayout.CENTER);

        // details + actions
        CardPanel detailsCard = new CardPanel(new BorderLayout(0, 10));
        detailsCard.setPreferredSize(new Dimension(330, 100));
        JLabel dt = new JLabel("Device Details");
        dt.setFont(font(Font.BOLD, 14f));
        dt.setForeground(TEXT);
        detailsCard.add(dt, BorderLayout.NORTH);

        detailsBody = new ScrollPanel();
        detailsBody.setOpaque(false);
        detailsBody.setLayout(new BoxLayout(detailsBody, BoxLayout.Y_AXIS));
        JScrollPane detailScroll = styledScroll(detailsBody);
        detailScroll.setBorder(null);
        detailScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        detailScroll.getViewport().setBackground(CARD);
        detailsCard.add(detailScroll, BorderLayout.CENTER);

        btnStart = new RoundedButton("Start", GREEN, false);
        btnStop = new RoundedButton("Stop", RED, false);
        btnAction = new RoundedButton("Device Action", BLUE, false);
        btnSchedule = new RoundedButton("Schedule", AMBER, false);
        btnService = new RoundedButton("Perform Service", TEAL, false);
        btnHistory = new RoundedButton("History", PURPLE, false);
        btnRemove = new RoundedButton("Remove Device", RED, true);
        btnStart.addActionListener(e -> handleStart());
        btnStop.addActionListener(e -> handleStop());
        btnAction.addActionListener(e -> handleDeviceAction());
        btnSchedule.addActionListener(e -> handleSchedule());
        btnService.addActionListener(e -> handleService());
        btnHistory.addActionListener(e -> handleHistory());
        btnRemove.addActionListener(e -> handleRemove());
        btnStart.setToolTipText("Run startOperation() on the selected device");
        btnStop.setToolTipText("Run stopOperation() on the selected device");
        btnAction.setToolTipText("Device specific control (oxygen, flow rate, vitals, capture...)");
        btnSchedule.setToolTipText("Schedule a maintenance visit");
        btnService.setToolTipText("Record a completed maintenance");
        btnHistory.setToolTipText("View the complete maintenance history");

        CardPanel actionBar = new CardPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionBar.setBorder(new EmptyBorder(10, 14, 13, 14));
        JLabel al = new JLabel("Actions for selected device:");
        al.setFont(font(Font.BOLD, 12f));
        al.setForeground(MUTED);
        al.setBorder(new EmptyBorder(0, 0, 0, 8));
        actionBar.add(al);
        actionBar.add(btnStart);
        actionBar.add(btnStop);
        actionBar.add(btnAction);
        actionBar.add(btnSchedule);
        actionBar.add(btnService);
        actionBar.add(btnHistory);
        actionBar.add(btnRemove);
        page.add(actionBar, BorderLayout.SOUTH);

        JPanel centre = new JPanel(new BorderLayout(12, 0));
        centre.setOpaque(false);
        centre.add(tableCard, BorderLayout.CENTER);
        centre.add(detailsCard, BorderLayout.EAST);
        page.add(centre, BorderLayout.CENTER);
        return page;
    }

    // ---------------------------------------------------- maintenance page
    private JComponent buildMaintenancePage() {
        JPanel page = new JPanel(new GridLayout(2, 1, 0, 12));
        page.setOpaque(false);

        pendingModel = new DefaultTableModel(
                new String[]{"Task ID", "Equipment ID", "Scheduled For", "Due In", "Technician", "Details"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pendingTable = new JTable(pendingModel);
        styleTable(pendingTable);
        pendingTable.getColumnModel().getColumn(5).setPreferredWidth(320);

        overdueModel = new DefaultTableModel(
                new String[]{"Equipment ID", "Device Name", "Type", "Was Due On", "Days Overdue", "Current Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        overdueTable = new JTable(overdueModel);
        styleTable(overdueTable);
        overdueTable.getColumnModel().getColumn(4).setCellRenderer(new OverdueDaysRenderer());

        page.add(titledTableCard("Scheduled Maintenance Tasks (pending)",
                "Created with the Schedule button", pendingTable));
        page.add(titledTableCard("Overdue Equipment",
                "Devices whose next maintenance date has already passed", overdueTable));
        return page;
    }

    private CardPanel titledTableCard(String title, String hint, JTable table) {
        CardPanel card = new CardPanel(new BorderLayout(0, 8));
        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 14f));
        t.setForeground(TEXT);
        JLabel h = new JLabel(hint);
        h.setFont(font(Font.PLAIN, 11.5f));
        h.setForeground(MUTED);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(t, BorderLayout.WEST);
        head.add(h, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);
        card.add(styledScroll(table), BorderLayout.CENTER);
        return card;
    }

    // ------------------------------------------------------------------- log
    private JComponent buildLogPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 18, 14, 18));

        JPanel box = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                aa(g2);
                g2.setColor(new Color(0x0F172A));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        box.setOpaque(false);

        JLabel title = new JLabel("System Telemetry & Audit Log");
        title.setFont(font(Font.BOLD, 12f));
        title.setForeground(new Color(0x94A3B8));
        RoundedButton clear = new RoundedButton("Clear", new Color(0x94A3B8), true);
        clear.setCompact(true);
        clear.addActionListener(e -> logPane.setText(""));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(8, 16, 4, 12));
        head.add(title, BorderLayout.WEST);
        head.add(clear, BorderLayout.EAST);
        box.add(head, BorderLayout.NORTH);

        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(new Color(0x0F172A));
        logPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logPane.setBorder(new EmptyBorder(2, 12, 6, 12));
        JScrollPane sp = new JScrollPane(logPane);
        sp.setBorder(null);
        sp.getViewport().setBackground(new Color(0x0F172A));
        sp.setBackground(new Color(0x0F172A));
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI(true));
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        box.add(sp, BorderLayout.CENTER);
        wrap.add(box, BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(100, 140));
        return wrap;
    }

    /** level: INFO, OK, WARN, ERROR, DEVICE */
    private void log(String level, String message) {
        Runnable r = () -> {
            StyledDocument doc = logPane.getStyledDocument();
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            Color c;
            switch (level) {
                case "OK":     c = new Color(0x4ADE80); break;
                case "WARN":   c = new Color(0xFBBF24); break;
                case "ERROR":  c = new Color(0xF87171); break;
                case "DEVICE": c = new Color(0x67E8F9); break;
                default:       c = new Color(0x93C5FD); break;
            }
            try {
                SimpleAttributeSet gray = new SimpleAttributeSet();
                StyleConstants.setForeground(gray, new Color(0x64748B));
                SimpleAttributeSet tag = new SimpleAttributeSet();
                StyleConstants.setForeground(tag, c);
                StyleConstants.setBold(tag, true);
                SimpleAttributeSet msg = new SimpleAttributeSet();
                StyleConstants.setForeground(msg, new Color(0xE2E8F0));
                doc.insertString(doc.getLength(), "[" + time + "] ", gray);
                doc.insertString(doc.getLength(), String.format("%-6s ", level), tag);
                doc.insertString(doc.getLength(), message + "\n", msg);
                logPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException ignored) {
            }
        };
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }

    /** Shows everything the devices print with System.out inside the GUI log too. */
    private void captureConsole() {
        final PrintStream original = System.out;
        OutputStream os = new OutputStream() {
            private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                original.write(b);
                if (b == '\n') {
                    String line = new String(buf.toByteArray(), StandardCharsets.UTF_8).trim();
                    buf.reset();
                    if (!line.isEmpty()) log("DEVICE", line);
                } else if (b != '\r') {
                    buf.write(b);
                }
            }
        };
        System.setOut(new PrintStream(os, true));
    }

    // ============================================================== TABLES
    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setFont(font(Font.PLAIN, 13f));
        t.setForeground(TEXT);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setBackground(CARD);
        t.setSelectionBackground(SELECTED);
        t.setSelectionForeground(TEXT);
        t.setAutoCreateRowSorter(true);
        t.setDefaultRenderer(Object.class, new ZebraRenderer());
        t.setDefaultRenderer(String.class, new ZebraRenderer());

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, col);
                l.setText(value == null ? "" : value.toString().toUpperCase());
                l.setFont(font(Font.BOLD, 11f));
                l.setForeground(MUTED);
                l.setBackground(new Color(0xF8FAFC));
                l.setOpaque(true);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                        new EmptyBorder(0, 12, 0, 12)));
                return l;
            }
        });
    }

    private JScrollPane styledScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(CARD);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI(false));
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI(false));
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    /** Alternating row colours, hover highlight and padding. */
    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, col);
            l.setBorder(new EmptyBorder(0, 12, 0, 12));
            l.setForeground(TEXT);
            l.setBackground(rowColor(table, sel, row));
            l.setOpaque(true);
            return l;
        }
    }

    private Color rowColor(JTable table, boolean selected, int row) {
        if (selected) return SELECTED;
        if (table == equipmentTable && row == hoverRow) return HOVER;
        return row % 2 == 0 ? CARD : ZEBRA;
    }

    class BoldRenderer extends ZebraRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, sel, focus, row, col);
            l.setFont(font(Font.BOLD, 12.5f));
            l.setForeground(NAVY);
            return l;
        }
    }

    class DueDateRenderer extends ZebraRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, sel, focus, row, col);
            String s = value == null ? "" : value.toString();
            if (s.endsWith("(Overdue)")) {
                l.setForeground(RED);
                l.setFont(font(Font.BOLD, 13f));
            } else if (s.startsWith("Not")) {
                l.setForeground(MUTED);
            }
            return l;
        }
    }

    class OverdueDaysRenderer extends ZebraRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, sel, focus, row, col);
            l.setForeground(RED);
            l.setFont(font(Font.BOLD, 13f));
            return l;
        }
    }

    /** Draws the status as a coloured pill with a dot. */
    class StatusRenderer extends JLabel implements TableCellRenderer {
        private Color fg = GREEN, bg = GREEN_BG, rowBg = CARD;

        StatusRenderer() {
            setOpaque(false);
            setFont(font(Font.BOLD, 11.5f));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            String s = value == null ? "" : value.toString();
            setText(s);
            if (EquipmentStatus.OPERATIONAL.getLabel().equals(s)) { fg = GREEN; bg = GREEN_BG; }
            else if (EquipmentStatus.UNDER_MAINTENANCE.getLabel().equals(s)) { fg = AMBER; bg = AMBER_BG; }
            else { fg = RED; bg = RED_BG; }
            rowBg = rowColor(table, sel, row);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(getText());
            int pw = tw + 34, ph = 24;
            int x = 12, y = (getHeight() - ph) / 2;
            g2.setColor(bg);
            g2.fillRoundRect(x, y, pw, ph, ph, ph);
            g2.setColor(fg);
            g2.fillOval(x + 10, y + ph / 2 - 3, 6, 6);
            g2.drawString(getText(), x + 21, y + (ph + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // ===================================================== DATA -> SCREEN
    private void refreshAll() {
        refreshTable();
    }

    private void refreshTable() {
        if (tableModel == null) return;
        String selectedId = getSelectedId();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String typeSel = (String) typeFilter.getSelectedItem();
        String statusSel = (String) statusFilter.getSelectedItem();

        tableModel.setRowCount(0);
        int shown = 0;
        for (MedicalEquipment eq : hospital.search(searchField.getText())) {
            if (!"All Types".equals(typeSel) && !eq.getTypeName().equals(typeSel)) continue;
            if (!"All Statuses".equals(statusSel) && !eq.checkStatus().getLabel().equals(statusSel)) continue;

            String next = eq.getNextMaintenanceDate() != null ? sdf.format(eq.getNextMaintenanceDate()) : "Not scheduled";
            if (eq.isMaintenanceOverdue()) next += " (Overdue)";

            tableModel.addRow(new Object[]{
                    eq.getEquipmentId(), eq.getName(), eq.getTypeName(),
                    eq.getManufacturer() + " (" + eq.getModel() + ")",
                    eq.checkStatus().getLabel(), next});
            shown++;
        }
        countLabel.setText(shown + " of " + hospital.getEquipmentList().size() + " devices");

        // keep the selection
        if (selectedId != null) {
            for (int i = 0; i < equipmentTable.getRowCount(); i++) {
                if (selectedId.equals(equipmentTable.getValueAt(i, 0))) {
                    equipmentTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }

        // stat cards
        int total = hospital.getEquipmentList().size();
        totalCard.setValue(total);
        operationalCard.setValue(hospital.filterByStatus(EquipmentStatus.OPERATIONAL).size());
        maintenanceCard.setValue(hospital.filterByStatus(EquipmentStatus.UNDER_MAINTENANCE).size());
        outCard.setValue(hospital.filterByStatus(EquipmentStatus.OUT_OF_SERVICE).size());
        overdueCard.setValue(hospital.getOverdueEquipment().size());

        refreshMaintenanceTables(sdf);
        updateDetails();
        updateButtons();
    }

    private void refreshMaintenanceTables(SimpleDateFormat sdf) {
        long now = System.currentTimeMillis();
        pendingModel.setRowCount(0);
        for (MaintenanceRecord r : scheduler.getUpcomingTasks()) {
            long days = (long) Math.ceil((r.getDate().getTime() - now) / 86400000.0);
            String due = days > 0 ? "in " + days + " day(s)" : days == 0 ? "today" : Math.abs(days) + " day(s) late";
            pendingModel.addRow(new Object[]{r.getRecordId(), r.getEquipmentId(), sdf.format(r.getDate()),
                    due, r.getTechnician(), r.getDescription()});
        }
        overdueModel.setRowCount(0);
        for (MedicalEquipment eq : hospital.getOverdueEquipment()) {
            long days = Math.max(1, (now - eq.getNextMaintenanceDate().getTime()) / 86400000L);
            overdueModel.addRow(new Object[]{eq.getEquipmentId(), eq.getName(), eq.getTypeName(),
                    sdf.format(eq.getNextMaintenanceDate()), days + " day(s)", eq.checkStatus().getLabel()});
        }
    }

    private void updateDetails() {
        if (detailsBody == null) return;
        detailsBody.removeAll();
        MedicalEquipment eq = getSelectedEquipmentSilently();
        if (eq == null) {
            JLabel empty = new JLabel("<html><div style='text-align:center;width:230px;color:#64748B'>"
                    + "<br><br>Select a device from the table<br>to see its details and actions.</div></html>",
                    SwingConstants.CENTER);
            empty.setFont(font(Font.PLAIN, 13f));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailsBody.add(empty);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            detailsBody.add(wrapLabel(eq.getName(), font(Font.BOLD, 16f), TEXT));
            detailsBody.add(Box.createVerticalStrut(2));
            detailsBody.add(wrapLabel(eq.getTypeName(), font(Font.PLAIN, 12.5f), MUTED));
            detailsBody.add(Box.createVerticalStrut(10));
            detailsBody.add(new StatusBadge(eq.checkStatus()));
            detailsBody.add(Box.createVerticalStrut(14));

            detailsBody.add(sectionTitle("GENERAL"));
            detailsBody.add(kv("Equipment ID", eq.getEquipmentId()));
            detailsBody.add(kv("Manufacturer", eq.getManufacturer()));
            detailsBody.add(kv("Model", eq.getModel()));
            detailsBody.add(kv("Last Service", eq.getLastMaintenanceDate() == null
                    ? "Factory Tested" : sdf.format(eq.getLastMaintenanceDate())));
            String next = eq.getNextMaintenanceDate() == null ? "Not scheduled"
                    : sdf.format(eq.getNextMaintenanceDate()) + (eq.isMaintenanceOverdue() ? "  (Overdue)" : "");
            detailsBody.add(kv("Next Service", next));
            detailsBody.add(kv("Service Records", String.valueOf(eq.getMaintenanceHistory().size())));

            Map<String, String> specific = eq.getSpecificDetails();
            if (!specific.isEmpty()) {
                detailsBody.add(Box.createVerticalStrut(10));
                detailsBody.add(sectionTitle("DEVICE SPECIFIC"));
                for (Map.Entry<String, String> en : specific.entrySet()) {
                    detailsBody.add(kv(en.getKey(), en.getValue()));
                }
            }
        }
        detailsBody.add(Box.createVerticalGlue());
        detailsBody.revalidate();
        detailsBody.repaint();
    }

    private JLabel wrapLabel(String text, Font f, Color c) {
        JLabel l = new JLabel("<html><div style='width:235px'>" + escape(text) + "</div></html>");
        l.setFont(f);
        l.setForeground(c);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel sectionTitle(String s) {
        JLabel l = new JLabel(s);
        l.setFont(font(Font.BOLD, 10.5f));
        l.setForeground(TEAL);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JComponent kv(String k, String v) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(6, 0, 6, 0)));
        JLabel a = new JLabel(k);
        a.setFont(font(Font.PLAIN, 12f));
        a.setForeground(MUTED);
        JLabel b = new JLabel("<html><div style='text-align:right;width:140px'>" + escape(v) + "</div></html>");
        b.setFont(font(Font.BOLD, 12f));
        b.setForeground(TEXT);
        p.add(a, BorderLayout.WEST);
        p.add(b, BorderLayout.EAST);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 12));
        return p;
    }

    private void updateButtons() {
        if (btnStart == null) return;
        MedicalEquipment eq = getSelectedEquipmentSilently();
        boolean has = eq != null;
        EquipmentStatus s = has ? eq.checkStatus() : null;
        btnStart.setEnabled(has && s == EquipmentStatus.OUT_OF_SERVICE);
        btnStop.setEnabled(has && s == EquipmentStatus.OPERATIONAL);
        btnAction.setEnabled(has && s == EquipmentStatus.OPERATIONAL);
        btnSchedule.setEnabled(has && s != EquipmentStatus.UNDER_MAINTENANCE);
        btnService.setEnabled(has);
        btnHistory.setEnabled(has);
        btnRemove.setEnabled(has);
    }

    // ============================================================ SELECTION
    private String getSelectedId() {
        int row = equipmentTable == null ? -1 : equipmentTable.getSelectedRow();
        return row < 0 ? null : (String) equipmentTable.getValueAt(row, 0);
    }

    private MedicalEquipment getSelectedEquipmentSilently() {
        String id = getSelectedId();
        return id == null ? null : hospital.findById(id);
    }

    private MedicalEquipment requireSelection() {
        MedicalEquipment eq = getSelectedEquipmentSilently();
        if (eq == null) {
            warn("Selection Needed", "Please select a device from the table first.");
        }
        return eq;
    }

    // ============================================================== ACTIONS
    private void handleStart() {
        MedicalEquipment eq = requireSelection();
        if (eq == null) return;
        if (eq.checkStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            warn("Under Maintenance", "This device is under maintenance.\nPerform the service first.");
            return;
        }
        eq.startOperation();            // polymorphic call
        log("OK", "startOperation() invoked on " + eq.getEquipmentId() + " (" + eq.getTypeName() + ")");
        refreshTable();
    }

    private void handleStop() {
        MedicalEquipment eq = requireSelection();
        if (eq == null) return;
        eq.stopOperation();             // polymorphic call
        log("WARN", "stopOperation() invoked on " + eq.getEquipmentId() + " (" + eq.getTypeName() + ")");
        refreshTable();
    }

    private void handleDeviceAction() {
        MedicalEquipment eq = requireSelection();
        if (eq == null) return;
        if (eq.checkStatus() != EquipmentStatus.OPERATIONAL) {
            warn("Device Not Running", "Start the device first - it must be OPERATIONAL.");
            return;
        }

        if (eq instanceof Ventilator) {
            final Ventilator v = (Ventilator) eq;
            JTextField oxygen = styledField(String.valueOf(v.getOxygenLevel()));
            JPanel form = new FormPanel().row("Target oxygen level (%)", oxygen).done();
            if (showDialog("Ventilator Controls", eq.getName(), BLUE, form, "Apply", () -> {
                try {
                    v.adjustOxygenLevel(Double.parseDouble(oxygen.getText().trim()));
                    return null;
                } catch (NumberFormatException ex) {
                    return "Please enter a valid number, e.g. 95";
                } catch (IllegalArgumentException ex) {
                    return ex.getMessage();
                }
            })) {
                log("OK", "[Ventilator] Oxygen level set to " + v.getOxygenLevel() + "%");
            }
        } else if (eq instanceof PatientMonitor) {
            final PatientMonitor m = (PatientMonitor) eq;
            JTextField hr = styledField(String.valueOf(m.getHeartRate()));
            JTextField bp = styledField(m.getBloodPressure());
            JPanel form = new FormPanel().row("Heart rate (bpm)", hr).row("Blood pressure (mmHg)", bp).done();
            if (showDialog("Patient Monitor - Update Vitals", eq.getName(), BLUE, form, "Update", () -> {
                try {
                    m.updateVitals(Integer.parseInt(hr.getText().trim()), bp.getText().trim());
                    m.displayVitals();
                    return null;
                } catch (NumberFormatException ex) {
                    return "Heart rate must be a whole number, e.g. 78";
                } catch (IllegalArgumentException ex) {
                    return ex.getMessage();
                }
            })) {
                log("OK", "[PatientMonitor] Vitals updated: HR " + m.getHeartRate() + " bpm, BP " + m.getBloodPressure());
            }
        } else if (eq instanceof InfusionPump) {
            final InfusionPump p = (InfusionPump) eq;
            JTextField med = styledField(p.getMedicationName());
            JTextField rate = styledField(String.valueOf(p.getFlowRate()));
            JPanel form = new FormPanel().row("Medication", med).row("Flow rate (mL/h)", rate).done();
            if (showDialog("Infusion Pump Settings", eq.getName(), BLUE, form, "Apply", () -> {
                try {
                    double r = Double.parseDouble(rate.getText().trim());
                    p.setMedicationName(med.getText());
                    p.setFlowRate(r);
                    return null;
                } catch (NumberFormatException ex) {
                    return "Flow rate must be a number, e.g. 25.5";
                } catch (IllegalArgumentException ex) {
                    return ex.getMessage();
                }
            })) {
                log("OK", "[InfusionPump] " + p.getMedicationName() + " at " + p.getFlowRate() + " mL/h");
                refreshTable();
            }
        } else if (eq instanceof ECGMachine) {
            ((ECGMachine) eq).recordECG();
            log("OK", "[ECG Machine] Electrocardiogram waveform captured successfully.");
        } else if (eq instanceof PortableUltrasound) {
            ((PortableUltrasound) eq).captureImage();
            log("OK", "[Ultrasound] Acoustic image captured and saved.");
        } else {
            info("Device Action", "This device type has no special action.");
        }
        refreshTable();
    }

    private void handleSchedule() {
        final MedicalEquipment eq = requireSelection();
        if (eq == null) return;

        JTextField tech = styledField("");
        JComboBox<String> task = styledCombo(new String[]{
                "Routine calibration & inspection", "Annual sensor calibration",
                "Safety compliance check", "Software / firmware update", "Battery & power supply test"});
        task.setEditable(true);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        JSpinner date = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd"));
        styleSpinner(date);

        JPanel form = new FormPanel()
                .row("Technician name", tech)
                .row("Maintenance task", task)
                .row("Scheduled date", date).done();
        if (showDialog("Schedule Maintenance", eq.getName() + "  (" + eq.getEquipmentId() + ")", AMBER, form, "Schedule", () -> {
            if (tech.getText().trim().isEmpty()) return "Please enter the technician name.";
            Object t = task.getEditor().getItem();
            if (t == null || t.toString().trim().isEmpty()) return "Please enter a maintenance task.";
            Date d = (Date) date.getValue();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if (d.before(today.getTime())) return "The date cannot be in the past.";
            scheduler.scheduleTask(eq, tech.getText().trim(), t.toString().trim(), d);
            eq.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
            return null;
        })) {
            log("INFO", "[Scheduler] " + eq.getEquipmentId() + " scheduled for service by " + tech.getText().trim());
            refreshTable();
        }
    }

    private void handleService() {
        final MedicalEquipment eq = requireSelection();
        if (eq == null) return;

        JTextField tech = styledField("Certified Technician");
        JTextField notes = styledField("Routine calibration & inspection completed.");
        JPanel form = new FormPanel().row("Technician", tech).row("Service notes", notes).done();
        if (showDialog("Perform Service", eq.getName() + "  (" + eq.getEquipmentId() + ")", TEAL, form, "Complete Service", () -> {
            if (tech.getText().trim().isEmpty()) return "Please enter the technician name.";
            if (notes.getText().trim().isEmpty()) return "Please enter the service notes.";
            eq.performMaintenance(tech.getText().trim(), notes.getText().trim());
            scheduler.completeTasksFor(eq);
            return null;
        })) {
            log("OK", "[Maintenance] Service completed for " + eq.getEquipmentId() + ". Status restored to OPERATIONAL.");
            refreshTable();
        }
    }

    private void handleHistory() {
        MedicalEquipment eq = requireSelection();
        if (eq == null) return;

        List<MaintenanceRecord> records = eq.getMaintenanceHistory();
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        if (records.isEmpty()) {
            JLabel l = new JLabel("<html><div style='width:420px;padding:20px 0'>No service records found.<br>"
                    + "This device is running on factory calibration.</div></html>");
            l.setFont(font(Font.PLAIN, 13f));
            l.setForeground(MUTED);
            body.add(l);
        } else {
            DefaultTableModel m = new DefaultTableModel(new String[]{"Record", "Date", "Technician", "Details"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (MaintenanceRecord r : records) {
                m.addRow(new Object[]{r.getRecordId(), sdf.format(r.getDate()), r.getTechnician(), r.getDescription()});
            }
            JTable t = new JTable(m);
            styleTable(t);
            t.setRowHeight(34);
            t.getColumnModel().getColumn(0).setPreferredWidth(70);
            t.getColumnModel().getColumn(1).setPreferredWidth(130);
            t.getColumnModel().getColumn(2).setPreferredWidth(130);
            t.getColumnModel().getColumn(3).setPreferredWidth(300);
            JScrollPane sp = styledScroll(t);
            sp.setPreferredSize(new Dimension(640, Math.min(260, 40 + records.size() * 34)));
            body.add(sp);
        }
        showDialog("Maintenance History", eq.getName() + "  (" + eq.getEquipmentId() + ")  -  "
                + records.size() + " record(s)", PURPLE, body, "Close", null, false);
    }

    private void handleRemove() {
        MedicalEquipment eq = requireSelection();
        if (eq == null) return;
        if (confirm("Remove Device", "Remove " + eq.getName() + " (" + eq.getEquipmentId()
                + ") from the inventory?\nIts maintenance history will be deleted too.", RED, "Remove")) {
            hospital.removeEquipment(eq.getEquipmentId());
            scheduler.removeTasksFor(eq.getEquipmentId());
            log("WARN", "[Inventory] Removed device " + eq.getEquipmentId() + " (" + eq.getName() + ")");
            equipmentTable.clearSelection();
            refreshTable();
        }
    }

    private void handleAddDevice() {
        JComboBox<String> type = styledCombo(EquipmentFactory.TYPES);
        JTextField id = styledField("");
        id.setEditable(false);
        id.setBackground(new Color(0xF1F5F9));
        JTextField name = styledField("");
        JTextField manufacturer = styledField("");
        JTextField model = styledField("");
        JTextField extra = styledField("");
        JLabel extraLabel = new JLabel("");
        extraLabel.setFont(font(Font.BOLD, 12f));
        extraLabel.setForeground(SLATE);

        Runnable onType = () -> {
            String t = (String) type.getSelectedItem();
            id.setText(hospital.generateId(EquipmentFactory.codeFor(t)));
            String label = EquipmentFactory.extraFieldLabel(t);
            extraLabel.setVisible(label != null);
            extra.setVisible(label != null);
            if (label != null) {
                extraLabel.setText(label);
                extra.setText(EquipmentFactory.extraFieldDefault(t));
            }
        };
        type.addActionListener(e -> onType.run());

        FormPanel fp = new FormPanel()
                .row("Device type", type)
                .row("Equipment ID (auto)", id)
                .row("Device name", name)
                .row("Manufacturer", manufacturer)
                .row("Model", model);
        fp.rowWithLabel(extraLabel, extra);
        JPanel form = fp.done();
        onType.run();

        final MedicalEquipment[] created = new MedicalEquipment[1];
        if (showDialog("Register New Device", "Add a new item to the hospital inventory", TEAL, form, "Add Device", () -> {
            if (name.getText().trim().isEmpty()) return "Device name is required.";
            if (manufacturer.getText().trim().isEmpty()) return "Manufacturer is required.";
            if (model.getText().trim().isEmpty()) return "Model is required.";
            try {
                MedicalEquipment eq = EquipmentFactory.create((String) type.getSelectedItem(), id.getText(),
                        name.getText().trim(), manufacturer.getText().trim(), model.getText().trim(), extra.getText());
                if (!hospital.addEquipment(eq)) return "A device with this ID already exists.";
                created[0] = eq;
                return null;
            } catch (IllegalArgumentException ex) {
                return ex.getMessage();
            }
        })) {
            log("OK", "[Inventory] Registered new device: " + created[0].getEquipmentInfo());
            refreshTable();
            for (int i = 0; i < equipmentTable.getRowCount(); i++) {
                if (created[0].getEquipmentId().equals(equipmentTable.getValueAt(i, 0))) {
                    equipmentTable.setRowSelectionInterval(i, i);
                    equipmentTable.scrollRectToVisible(equipmentTable.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    // =============================================================== DIALOGS
    private boolean showDialog(String title, String subtitle, Color accent, JComponent body,
                               String okText, Supplier<String> onOk) {
        return showDialog(title, subtitle, accent, body, okText, onOk, true);
    }

    private boolean showDialog(String title, String subtitle, Color accent, JComponent body,
                               String okText, Supplier<String> onOk, boolean cancel) {
        StyledDialog d = new StyledDialog(this, title, subtitle, accent, body, okText, cancel, onOk);
        d.setVisible(true);
        return d.confirmed;
    }

    private void info(String title, String msg) {
        showDialog(title, "", BLUE, messageBody(msg), "OK", null, false);
    }

    private void warn(String title, String msg) {
        showDialog(title, "", AMBER, messageBody(msg), "OK", null, false);
    }

    private boolean confirm(String title, String msg, Color accent, String okText) {
        return showDialog(title, "", accent, messageBody(msg), okText, null, true);
    }

    private JComponent messageBody(String msg) {
        JLabel l = new JLabel("<html><div style='width:320px'>" + escape(msg).replace("\n", "<br>") + "</div></html>");
        l.setFont(font(Font.PLAIN, 13.5f));
        l.setForeground(TEXT);
        return l;
    }

    static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ------------------------------------------------------- styled widgets
    private JTextField styledField(String text) {
        JTextField f = new JTextField(text);
        f.setFont(font(Font.PLAIN, 13f));
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setCaretColor(BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(FIELD_BORDER, 10),
                new EmptyBorder(6, 10, 6, 10)));
        f.setPreferredSize(new Dimension(260, 38));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(BLUE, 10), new EmptyBorder(6, 10, 6, 10)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(FIELD_BORDER, 10), new EmptyBorder(6, 10, 6, 10)));
            }
        });
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setUI(new ModernComboUI());
        c.setFont(font(Font.PLAIN, 13f));
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
        c.setBorder(new RoundBorder(FIELD_BORDER, 10));
        c.setPreferredSize(new Dimension(190, 36));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
                l.setBorder(new EmptyBorder(7, 12, 7, 12));
                l.setFont(font(Font.PLAIN, 13f));
                l.setForeground(TEXT);
                l.setBackground(sel && index >= 0 ? SELECTED : Color.WHITE);
                return l;
            }
        });
        return c;
    }

    private void styleSpinner(JSpinner s) {
        s.setFont(font(Font.PLAIN, 13f));
        s.setBorder(new RoundBorder(FIELD_BORDER, 10));
        s.setPreferredSize(new Dimension(260, 38));
        JComponent ed = s.getEditor();
        if (ed instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
            tf.setFont(font(Font.PLAIN, 13f));
            tf.setBorder(new EmptyBorder(4, 8, 4, 8));
            tf.setBackground(Color.WHITE);
        }
    }

    private Image createAppIcon() {
        return createLogo(64);
    }

    private static Image createLogo(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        aa(g);
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, size - 1, size - 1, size / 3, size / 3);
        int arm = size / 5, c = size / 2;
        g.setColor(TEAL);
        g.fillRoundRect(c - arm / 2, size / 6, arm, size - size / 3, arm / 2, arm / 2);
        g.fillRoundRect(size / 6, c - arm / 2, size - size / 3, arm, arm / 2, arm / 2);
        g.dispose();
        return img;
    }

    // ===================================================== CUSTOM COMPONENTS

    /** Flat rounded button with hover / pressed / disabled states. */
    static class RoundedButton extends JButton {
        private final Color base;
        private boolean outline;
        private boolean hover, pressed, compact;

        RoundedButton(String text, Color base, boolean outline) {
            super(text);
            this.base = base;
            this.outline = outline;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(font(Font.BOLD, 12.5f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        void setOutline(boolean o) { this.outline = o; repaint(); }

        void setCompact(boolean c) { this.compact = c; revalidate(); }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int pad = compact ? 12 : 34;
            return new Dimension(fm.stringWidth(getText()) + pad, compact ? 26 : 36);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            int w = getWidth(), h = getHeight(), arc = compact ? 12 : 12;
            Color fill, fg, line = null;
            if (!isEnabled()) {
                fill = outline ? Color.WHITE : new Color(0xE2E8F0);
                fg = new Color(0xA0AEC0);
                if (outline) line = new Color(0xE2E8F0);
            } else if (outline) {
                fill = pressed ? blend(Color.WHITE, base, 0.22f) : hover ? blend(Color.WHITE, base, 0.10f) : new Color(255, 255, 255, 0);
                fg = base;
                line = base;
            } else {
                fill = pressed ? blend(base, Color.BLACK, 0.18f) : hover ? blend(base, Color.WHITE, 0.14f) : base;
                fg = Color.WHITE;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            if (line != null) {
                g2.setColor(line);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            g2.setFont(compact ? font(Font.BOLD, 11f) : getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(fg);
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** White rounded panel with a soft shadow. */
    static class CardPanel extends JPanel {
        CardPanel(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(14, 16, 18, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(new Color(15, 23, 42, 14));
            g2.fill(new RoundRectangle2D.Float(1, 3, getWidth() - 2, getHeight() - 4, 18, 18));
            g2.setColor(CARD);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 3, 18, 18));
            g2.setColor(BORDER);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 3, 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Dashboard number card with a coloured accent. */
    static class StatCard extends CardPanel {
        private final Color accent;
        private final JLabel value = new JLabel("0");

        StatCard(String caption, Color accent) {
            super(new BorderLayout(0, 2));
            this.accent = accent;
            setBorder(new EmptyBorder(8, 22, 12, 14));
            value.setFont(font(Font.BOLD, 28f));
            value.setForeground(accent);
            JLabel cap = new JLabel(caption);
            cap.setFont(font(Font.PLAIN, 12.5f));
            cap.setForeground(MUTED);
            add(value, BorderLayout.CENTER);
            add(cap, BorderLayout.SOUTH);
        }

        void setValue(int v) { value.setText(String.valueOf(v)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(accent);
            g2.fillRoundRect(10, 14, 5, getHeight() - 31, 5, 5);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28));
            g2.fillOval(getWidth() - 56, 14, 38, 38);
            g2.setColor(accent);
            g2.fillOval(getWidth() - 44, 26, 14, 14);
            g2.dispose();
        }
    }

    /** Text field with a grey placeholder text. */
    static class SearchField extends JTextField {
        private final String hint;

        SearchField(String hint) {
            this.hint = hint;
            setFont(font(Font.PLAIN, 13f));
            setForeground(TEXT);
            setCaretColor(BLUE);
            setBorder(BorderFactory.createCompoundBorder(new RoundBorder(FIELD_BORDER, 10), new EmptyBorder(6, 12, 6, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                aa(g2);
                g2.setColor(new Color(0x94A3B8));
                g2.setFont(getFont());
                Insets in = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    /** Rounded 1px border. */
    static class RoundBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int arc;

        RoundBorder(Color color, int arc) {
            this.color = color;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }

    /** Big status pill used in the details panel. */
    static class StatusBadge extends JComponent {
        private final EquipmentStatus status;

        StatusBadge(EquipmentStatus status) {
            this.status = status;
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(200, 30));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            Color fg = status == EquipmentStatus.OPERATIONAL ? GREEN
                    : status == EquipmentStatus.UNDER_MAINTENANCE ? AMBER : RED;
            Color bg = status == EquipmentStatus.OPERATIONAL ? GREEN_BG
                    : status == EquipmentStatus.UNDER_MAINTENANCE ? AMBER_BG : RED_BG;
            g2.setFont(font(Font.BOLD, 12f));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(status.getLabel()) + 38, h = 28;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(fg);
            g2.fillOval(12, h / 2 - 4, 8, 8);
            g2.drawString(status.getLabel(), 26, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Panel that always follows the width of its scroll pane (no horizontal scroll). */
    static class ScrollPanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /** Label + field rows for dialogs. */
    static class FormPanel {
        private final JPanel p = new JPanel(new GridBagLayout());
        private int row = 0;

        FormPanel() {
            p.setOpaque(false);
        }

        FormPanel row(String label, JComponent field) {
            JLabel l = new JLabel(label);
            l.setFont(font(Font.BOLD, 12f));
            l.setForeground(SLATE);
            return rowWithLabel(l, field);
        }

        FormPanel rowWithLabel(JLabel l, JComponent field) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = row;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 12, 16);
            c.ipadx = 0;
            p.add(l, c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 0, 12, 0);
            p.add(field, c);
            row++;
            return this;
        }

        JPanel done() {
            return p;
        }
    }

    /** Rounded modal dialog with a coloured header, OK / Cancel and inline error text. */
    static class StyledDialog extends JDialog {
        boolean confirmed = false;
        private final JLabel error = new JLabel(" ");

        StyledDialog(Frame owner, String title, String subtitle, Color accent, JComponent body,
                     String okText, boolean showCancel, Supplier<String> onOk) {
            super(owner, title, true);
            setUndecorated(false);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(Color.WHITE);

            JPanel head = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(new GradientPaint(0, 0, accent, getWidth(), 0, blend(accent, Color.BLACK, 0.25f)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            head.setBorder(new EmptyBorder(14, 22, 14, 22));
            JLabel t = new JLabel(title);
            t.setFont(font(Font.BOLD, 17f));
            t.setForeground(Color.WHITE);
            head.add(t, BorderLayout.NORTH);
            if (subtitle != null && !subtitle.isEmpty()) {
                JLabel s = new JLabel(subtitle);
                s.setFont(font(Font.PLAIN, 12f));
                s.setForeground(new Color(255, 255, 255, 215));
                head.add(s, BorderLayout.SOUTH);
            }
            root.add(head, BorderLayout.NORTH);

            JPanel mid = new JPanel(new BorderLayout(0, 8));
            mid.setOpaque(false);
            mid.setBorder(new EmptyBorder(20, 24, 8, 24));
            mid.add(body, BorderLayout.CENTER);
            error.setFont(font(Font.BOLD, 12f));
            error.setForeground(RED);
            mid.add(error, BorderLayout.SOUTH);
            root.add(mid, BorderLayout.CENTER);

            RoundedButton ok = new RoundedButton(okText, accent, false);
            ok.addActionListener(e -> {
                if (onOk != null) {
                    String err = onOk.get();
                    if (err != null) {
                        error.setText(err);
                        return;
                    }
                }
                confirmed = true;
                dispose();
            });
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            foot.setOpaque(false);
            foot.setBorder(new EmptyBorder(6, 20, 16, 20));
            if (showCancel) {
                RoundedButton cancel = new RoundedButton("Cancel", SLATE, true);
                cancel.addActionListener(e -> dispose());
                foot.add(cancel);
            }
            foot.add(ok);
            root.add(foot, BorderLayout.SOUTH);

            setContentPane(root);
            getRootPane().setDefaultButton(ok);
            getRootPane().registerKeyboardAction(e -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
            setResizable(false);
            pack();
            setMinimumSize(new Dimension(420, 0));
            setLocationRelativeTo(owner);
        }
    }

    /** Thin rounded scroll bar. */
    static class ModernScrollBarUI extends BasicScrollBarUI {
        private final boolean dark;

        ModernScrollBarUI(boolean dark) {
            this.dark = dark;
        }

        @Override
        protected void configureScrollBarColors() {
            thumbColor = dark ? new Color(0x334155) : new Color(0xCBD5E1);
            trackColor = new Color(0, 0, 0, 0);
        }

        private JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override protected JButton createDecreaseButton(int o) { return zero(); }

        @Override protected JButton createIncreaseButton(int o) { return zero(); }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(dark ? new Color(0x0F172A) : Color.WHITE);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            Color base = thumbColor;
            g2.setColor(isThumbRollover() || isDragging ? blend(base, Color.BLACK, 0.2f) : base);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    /** Flat combo box with a chevron. */
    static class ModernComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    aa(g2);
                    g2.setColor(MUTED);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g2.dispose();
                }
            };
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            return b;
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            super.paintCurrentValue(g, bounds, false);   // no blue focus fill
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(Color.WHITE);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    // ===================================================================== main
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            UIManager.put("ToolTip.font", font(Font.PLAIN, 12f));
            UIManager.put("ToolTip.background", new Color(0x0F172A));
            UIManager.put("ToolTip.foreground", Color.WHITE);
            UIManager.put("ToolTip.border", new EmptyBorder(5, 8, 5, 8));
            new HospitalGUI().setVisible(true);
        });
    }
}
