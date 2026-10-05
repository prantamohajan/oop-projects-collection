import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class RiderAssignmentGUI extends JFrame {
    private DeliveryManager manager;

    // GUI Tables & Models
    private JTable riderTable;
    private JTable orderTable;
    private DefaultTableModel riderTableModel;
    private DefaultTableModel orderTableModel;

    // Rider Form Controls
    private JTextField tfRiderName, tfRiderId, tfRiderPhone, tfExtraAttr;
    private JComboBox<String> cbVehicleType;
    private JLabel lblExtra;

    // Order Form Controls
    private JTextField tfOrderId, tfCustomer, tfRestaurant, tfAddress, tfDistance;
    private JRadioButton rbAny, rbBike, rbCycle;
    private ButtonGroup bgPreference;

    // Modern Color Palette
    private final Color PRIMARY_BLUE = new Color(24, 119, 242);
    private final Color SUCCESS_GREEN = new Color(16, 185, 129);
    private final Color ACTION_ORANGE = new Color(245, 158, 11);
    private final Color BG_LIGHT = new Color(243, 244, 246);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(31, 41, 55);

    public RiderAssignmentGUI() {
        manager = new DeliveryManager();
        initUI();
        seedInitialData();
        refreshTables();
    }

    private void initUI() {
        setTitle("Food Delivery Rider Assignment System - Dispatch Console");
        setSize(1150, 720);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_LIGHT);
        setLayout(new BorderLayout(15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("Smart Food Delivery Dispatch & Rider Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Premier University OOP Lab Project");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(224, 242, 254));
        headerPanel.add(lblSub, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(0, 15, 15, 15));

        // Left Container: Forms
        JPanel formsContainer = new JPanel(new GridLayout(2, 1, 14, 14));
        formsContainer.setOpaque(false);

        // Form 1: Rider Registration
        JPanel addRiderCard = createCardPanel("Register New Delivery Partner");
        addRiderCard.setLayout(new GridLayout(6, 2, 8, 8));

        tfRiderId = createStyledTextField();
        tfRiderName = createStyledTextField();
        tfRiderPhone = createStyledTextField();
        tfExtraAttr = createStyledTextField();
        cbVehicleType = new JComboBox<>(new String[] { "Bike", "Cycle" });
        cbVehicleType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblExtra = createStyledLabel("Fuel Level (%):");

        cbVehicleType.addActionListener(e -> {
            if ("Bike".equals(cbVehicleType.getSelectedItem())) {
                lblExtra.setText("Fuel Level (%):");
            } else {
                lblExtra.setText("Max Load (kg):");
            }
        });

        addRiderCard.add(createStyledLabel("Rider ID:"));
        addRiderCard.add(tfRiderId);
        addRiderCard.add(createStyledLabel("Rider Name:"));
        addRiderCard.add(tfRiderName);
        addRiderCard.add(createStyledLabel("Phone Number:"));
        addRiderCard.add(tfRiderPhone);
        addRiderCard.add(createStyledLabel("Vehicle Category:"));
        addRiderCard.add(cbVehicleType);
        addRiderCard.add(lblExtra);
        addRiderCard.add(tfExtraAttr);

        JButton btnAddRider = createStyledButton("Register Rider", SUCCESS_GREEN);
        btnAddRider.addActionListener(e -> registerRiderAction());
        addRiderCard.add(new JLabel(""));
        addRiderCard.add(btnAddRider);

        formsContainer.add(addRiderCard);

        // Form 2: Order Management & Assignment
        JPanel createOrderCard = createCardPanel("Create & Dispatch Order");
        createOrderCard.setLayout(new GridLayout(8, 2, 6, 6));

        tfOrderId = createStyledTextField();
        tfCustomer = createStyledTextField();
        tfRestaurant = createStyledTextField();
        tfAddress = createStyledTextField();
        tfDistance = createStyledTextField();

        rbAny = new JRadioButton("Any", true);
        rbBike = new JRadioButton("Bike");
        rbCycle = new JRadioButton("Cycle");
        rbAny.setOpaque(false);
        rbBike.setOpaque(false);
        rbCycle.setOpaque(false);

        bgPreference = new ButtonGroup();
        bgPreference.add(rbAny);
        bgPreference.add(rbBike);
        bgPreference.add(rbCycle);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(rbAny);
        radioPanel.add(rbBike);
        radioPanel.add(rbCycle);

        createOrderCard.add(createStyledLabel("Order Reference ID:"));
        createOrderCard.add(tfOrderId);
        createOrderCard.add(createStyledLabel("Customer Name:"));
        createOrderCard.add(tfCustomer);
        createOrderCard.add(createStyledLabel("Pickup Restaurant:"));
        createOrderCard.add(tfRestaurant);
        createOrderCard.add(createStyledLabel("Delivery Address:"));
        createOrderCard.add(tfAddress);
        createOrderCard.add(createStyledLabel("Trip Distance (km):"));
        createOrderCard.add(tfDistance);
        createOrderCard.add(createStyledLabel("Vehicle Preference:"));
        createOrderCard.add(radioPanel);

        JButton btnAssignOrder = createStyledButton("Assign Order", ACTION_ORANGE);
        btnAssignOrder.addActionListener(e -> createOrderAction());

        JButton btnCompleteOrder = createStyledButton("Complete Delivery", PRIMARY_BLUE);
        btnCompleteOrder.addActionListener(e -> completeDeliveryAction());

        createOrderCard.add(btnAssignOrder);
        createOrderCard.add(btnCompleteOrder);

        formsContainer.add(createOrderCard);
        mainPanel.add(formsContainer);

        // Right Container: Tables
        JPanel tablesContainer = new JPanel(new GridLayout(2, 1, 14, 14));
        tablesContainer.setOpaque(false);

        // Rider Status Table
        riderTableModel = new DefaultTableModel(new String[] { "ID", "Name", "Vehicle", "Status", "Details" }, 0);
        riderTable = createStyledTable(riderTableModel);
        riderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        riderTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        riderTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        riderTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        riderTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        riderTable.getColumnModel().getColumn(4).setPreferredWidth(320);

        JScrollPane riderScroll = new JScrollPane(riderTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        riderScroll.setBorder(
                BorderFactory.createTitledBorder(new LineBorder(new Color(209, 213, 219), 1), "Live Rider Fleet Status",
                        TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), PRIMARY_BLUE));
        riderScroll.getViewport().setBackground(Color.WHITE);
        tablesContainer.add(riderScroll);

        // Live Order Table
        orderTableModel = new DefaultTableModel(
                new String[] { "Order ID", "Customer", "Restaurant", "Status", "Action" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = createStyledTable(orderTableModel);
        setupTrashColumn();
        JScrollPane orderScroll = new JScrollPane(orderTable);
        orderScroll.setBorder(
                BorderFactory.createTitledBorder(new LineBorder(new Color(209, 213, 219), 1), "Active Orders Log",
                        TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), PRIMARY_BLUE));
        orderScroll.getViewport().setBackground(Color.WHITE);
        tablesContainer.add(orderScroll);

        mainPanel.add(tablesContainer);
        add(mainPanel, BorderLayout.CENTER);
    }

    // Helper UI Stylers
    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createTitledBorder(new EmptyBorder(4, 8, 8, 8), title, TitledBorder.LEFT,
                        TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), PRIMARY_BLUE)));
        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(209, 213, 219), 1, true),
                new EmptyBorder(3, 6, 3, 6)));
        return tf;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(243, 244, 246));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(new Color(243, 244, 246));
        return table;
    }

    private void setupTrashColumn() {
        orderTable.getColumnModel().getColumn(4).setMaxWidth(70);
        orderTable.getColumnModel().getColumn(4).setMinWidth(70);

        Icon trashIcon = new TrashIcon(16, new Color(220, 38, 38));
        DefaultTableCellRenderer trashRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, "", isSelected, false, row, column);
                String status = (String) table.getModel().getValueAt(row, 3);
                setIcon("Delivered".equalsIgnoreCase(status) ? trashIcon : null);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        };
        orderTable.getColumnModel().getColumn(4).setCellRenderer(trashRenderer);

        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = orderTable.rowAtPoint(e.getPoint());
                int col = orderTable.columnAtPoint(e.getPoint());
                if (row < 0 || col != 4) {
                    return;
                }

                String status = (String) orderTableModel.getValueAt(row, 3);
                if (!"Delivered".equalsIgnoreCase(status)) {
                    return;
                }

                String orderId = (String) orderTableModel.getValueAt(row, 0);
                int choice = JOptionPane.showConfirmDialog(
                        RiderAssignmentGUI.this,
                        "Delete delivered order #" + orderId + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    manager.removeOrder(orderId);
                    refreshTables();
                }
            }
        });
    }

    // ৪ জন রাইডার ইনিশিয়াল সেট করা হলো (২ জন বাইক, ২ জন সাইকেল)
    private void seedInitialData() {
        manager.addRider(new BikeRider("Alice", "R1", "01711111111", 85.0, 42.5));
        manager.addRider(new BikeRider("Bob", "R2", "01722222222", 70.0, 35.0));
        manager.addRider(new CycleRider("Joshef", "R3", "01733333333", 12.0));
        manager.addRider(new CycleRider("Kelvin", "R4", "01744444444", 10.0));
    }

    private void processPendingOrders() {
        for (Order o : manager.getOrderList()) {
            if ("Pending".equalsIgnoreCase(o.getStatus())) {
                try {
                    // পেন্ডিং অর্ডারগুলোতে ৫ কিমি ধরে ফ্রি রাইডার দেওয়া হবে
                    manager.assignRider(o, 4.0, "Any");
                } catch (NoRiderAvailableException e) {
                    break;
                }
            }
        }
    }

    private void clearOrderForm() {
        tfOrderId.setText("");
        tfCustomer.setText("");
        tfRestaurant.setText("");
        tfAddress.setText("");
        tfDistance.setText("");
        rbAny.setSelected(true);
    }

    private void clearRiderForm() {
        tfRiderId.setText("");
        tfRiderName.setText("");
        tfRiderPhone.setText("");
        tfExtraAttr.setText("");
    }

    private void registerRiderAction() {
        try {
            String id = tfRiderId.getText().trim();
            String name = tfRiderName.getText().trim();
            String phone = tfRiderPhone.getText().trim();
            String vehicle = (String) cbVehicleType.getSelectedItem();
            String extraStr = tfExtraAttr.getText().trim();

            if (id.isEmpty() || name.isEmpty() || extraStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all rider details!", "Missing Input",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            double extra = Double.parseDouble(extraStr);

            if ("Bike".equals(vehicle)) {
                double fuel = extra;
                double estimatedDist = fuel * 0.5;
                manager.addRider(new BikeRider(name, id, phone, fuel, estimatedDist));
            } else {
                manager.addRider(new CycleRider(name, id, phone, extra));
            }

            clearRiderForm();
            processPendingOrders();
            refreshTables();
            JOptionPane.showMessageDialog(this, "Rider registered successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Fuel/MaxLoad must be numeric!", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createOrderAction() {
        try {
            String orderId = tfOrderId.getText().trim();
            String customer = tfCustomer.getText().trim();
            String restaurant = tfRestaurant.getText().trim();
            String address = tfAddress.getText().trim();
            String distStr = tfDistance.getText().trim();

            if (orderId.isEmpty() || customer.isEmpty() || distStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Order ID, Customer, and Distance!", "Missing Input",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            double distance = Double.parseDouble(distStr);
            String preference = rbBike.isSelected() ? "Bike" : (rbCycle.isSelected() ? "Cycle" : "Any");

            Order newOrder = new Order(orderId, customer, restaurant, address);
            manager.addOrder(newOrder);

            // নতুন স্মার্ট লজিক কল করা হচ্ছে
            Rider assignedRider = manager.assignRider(newOrder, distance, preference);
            double time = assignedRider.calculateDeliveryTime(distance);

            clearOrderForm();
            refreshTables();

            JOptionPane.showMessageDialog(this,
                    "Order assigned to: " + assignedRider.getName() + " (" + assignedRider.getVehicleType() + ")" +
                            "\nTrip Distance: " + distance + " km" +
                            "\nEstimated Delivery Time: " + String.format("%.2f", time) + " hours",
                    "Dispatch Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (NoRiderAvailableException ex) {
            clearOrderForm();
            refreshTables();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dispatch Notice", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Trip distance must be a valid number!", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeDeliveryAction() {
        int selectedRow = riderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an active rider from the fleet table first.",
                    "Selection Needed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String riderId = (String) riderTableModel.getValueAt(selectedRow, 0);
        boolean success = manager.completeOrder(riderId);

        if (success) {
            processPendingOrders();
            refreshTables();
            JOptionPane.showMessageDialog(this, "Delivery completed! Rider is available now.");
        } else {
            JOptionPane.showMessageDialog(this, "The selected rider has no active delivery task.", "Notice",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshTables() {
        riderTableModel.setRowCount(0);
        for (Rider r : manager.getRiderMap().values()) {
            riderTableModel.addRow(new Object[] {
                    r.getId(),
                    r.getName(),
                    r.getVehicleType(),
                    r.isAvailable() ? "Available" : "On Delivery",
                    r.getRoleDescription()
            });
        }

        orderTableModel.setRowCount(0);
        for (Order o : manager.getOrderList()) {
            orderTableModel.addRow(new Object[] {
                    o.getOrderId(),
                    o.getCustomerName(),
                    o.getRestaurant(),
                    o.getStatus(),
                    ""
            });
        }
    }

    private static class TrashIcon implements Icon {
        private final int size;
        private final Color color;

        TrashIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawLine(x + 2, y + 4, x + size - 2, y + 4);
            g2.drawLine(x + size / 2 - 3, y + 4, x + size / 2 - 3, y + 2);
            g2.drawLine(x + size / 2 - 3, y + 2, x + size / 2 + 3, y + 2);
            g2.drawLine(x + size / 2 + 3, y + 2, x + size / 2 + 3, y + 4);

            g2.drawPolygon(
                    new int[] { x + 4, x + size - 4, x + size - 5, x + 5 },
                    new int[] { y + 6, y + 6, y + size - 2, y + size - 2 },
                    4);

            g2.drawLine(x + 7, y + 8, x + 7, y + size - 4);
            g2.drawLine(x + size - 7, y + 8, x + size - 7, y + size - 4);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RiderAssignmentGUI().setVisible(true));
    }
}