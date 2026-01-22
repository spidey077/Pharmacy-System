import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import javax.swing.*;
// SQL Server Database Manager
class DatabaseManager {
    // Update the connection URL to use integrated security (Windows Authentication)
    private static final String DB_URL = "jdbc:sqlserver://DESKTOP-C4VLF6H\\SQLEXPRESS:1433;databaseName=PharmacyDB;integratedSecurity=true";
    static {
        // Load the JDBC driver and set the path to sqljdbc_auth.dll for integrated security
        try {
            // IMPORTANT: Set this path to the folder containing sqljdbc_auth.dll of the appropriate architecture (x86 or x64)
            // For example: "C:\\sqljdbc_10.2\\enu\\auth\\x64"
            System.setProperty("java.library.path", "C:\\Users\\Muhammad Nouman\\Documents\\VSC\\sqljdbc_12.10\\enu\\auth\\x64");
            // To reset paths for java.library.path property dynamically (not always necessary)
            // This requires reflection hack for reloading - omitted for simplicity

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Failed to load SQL Server JDBC driver: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        // No username or password needed when using integratedSecurity=true
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Create Medicines table
            stmt.executeUpdate("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='medicines' AND xtype='U') " +
                "CREATE TABLE medicines (" +
                "id INT IDENTITY(1,1) PRIMARY KEY," +
                "name NVARCHAR(255) NOT NULL," +
                "use_description NVARCHAR(255)," +
                "dosage NVARCHAR(255)," +
                "price DECIMAL(10,2)," +
                "stock INT," +
                "is_child BIT," +
                "CONSTRAINT UC_Medicine UNIQUE(name, is_child))");
            // Create Customers table
            stmt.executeUpdate("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='customers' AND xtype='U') " +
                "CREATE TABLE customers (" +
                "id INT IDENTITY(1,1) PRIMARY KEY," +
                "name NVARCHAR(255) NOT NULL," +
                "contact_info NVARCHAR(255)," +
                "dob DATE," +
                "address NVARCHAR(MAX)," +
                "allergies NVARCHAR(MAX)," +
                "current_meds NVARCHAR(MAX)," +
                "gender NVARCHAR(50)," +
                "email NVARCHAR(255))");
            // Create Orders table
            stmt.executeUpdate("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='orders' AND xtype='U') " +
                "CREATE TABLE orders (" +
                "id INT IDENTITY(1,1) PRIMARY KEY," +
                "order_id NVARCHAR(50) UNIQUE," +
                "customer_id INT," +
                "order_date DATE," +
                "order_type NVARCHAR(50)," +
                "FOREIGN KEY (customer_id) REFERENCES customers(id))");

            // Create Order Items table
            stmt.executeUpdate("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='order_items' AND xtype='U') " +
                "CREATE TABLE order_items (" +
                "id INT IDENTITY(1,1) PRIMARY KEY," +
                "order_id INT," +
                "medicine_id INT," +
                "quantity INT," +
                "FOREIGN KEY (order_id) REFERENCES orders(id)," +
                "FOREIGN KEY (medicine_id) REFERENCES medicines(id))");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Database initialization failed: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// Interface for inventory management
interface InventoryManager {
    void viewInventory();
    Medicine findMedicine(String name, boolean isChild);
}
class Person {
    protected String name;
    protected String contactInfo;
    
    public Person(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
    }
    
    public void displayInfo() {
        System.out.println("Name: " + name);
        System.out.println("Contact: " + contactInfo);
    }
    
    public String getName() { return name; }
    public String getContactInfo() { return contactInfo; }
}
// Customer class with enhanced GUI display
class Customer extends Person {
    private LocalDate dob;
    private String address;
    private String allergies;
    private String currentMeds;
    private String gender;
    private String email;
    
    public Customer(String name, String contactInfo, LocalDate dob, String address, 
                   String allergies, String currentMeds, String gender, String email) {
        super(name, contactInfo);
        this.dob = dob;
        this.address = address;
        this.allergies = allergies;
        this.currentMeds = currentMeds;
        this.gender = gender;
        this.email = email;
    }
    
    @Override
    public void displayInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Customer Information:\n");
        info.append("Name: ").append(name).append("\n");
        info.append("Age: ").append(Period.between(dob, LocalDate.now()).getYears()).append("\n");
        info.append("Gender: ").append(gender).append("\n");
        info.append("Contact: ").append(contactInfo).append("\n");
        info.append("Email: ").append(email).append("\n");
        info.append("Address: ").append(address).append("\n");
        info.append("Allergies: ").append(allergies).append("\n");
        info.append("Current Medications: ").append(currentMeds).append("\n");
        
        JOptionPane.showMessageDialog(null, 
            info.toString(),
            "Customer Details",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public LocalDate getDob() { return dob; }
    public String getAddress() { return address; }
    public String getAllergies() { return allergies; }
    public String getCurrentMeds() { return currentMeds; }
    public String getGender() { return gender; }
    public String getEmail() { return email; }
    
    public boolean isChild() {
        return Period.between(dob, LocalDate.now()).getYears() < 18;
    }
}

// Order class with GUI display
class Order {
    private PharmacySystemGUI pharmacySystem;
    private List<OrderItem> items;
    private Customer customer;
    private String orderType;
    private LocalDate orderDate;
    private String orderId;
    
    public Order(PharmacySystemGUI pharmacySystem, Customer customer, String orderType) {
        this.pharmacySystem = pharmacySystem;
        this.customer = customer;
        this.orderType = orderType;
        this.items = new ArrayList<>();
        this.orderDate = LocalDate.now();
        this.orderId = generateOrderId();
    }
    
    private String generateOrderId() {
        return "ORD-" + orderDate.getYear() + "-" + 
               String.format("%02d", orderDate.getMonthValue()) + "-" +
               String.format("%04d", (int)(Math.random() * 10000));
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void displayOrder() {
        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append("===== ORDER SUMMARY =====\n");
        orderDetails.append("Order ID: ").append(orderId).append("\n");
        orderDetails.append("Order Date: ").append(orderDate).append("\n");
        orderDetails.append("Order Type: ").append(orderType).append("\n\n");
        
        orderDetails.append("Customer Information:\n");
        orderDetails.append("Name: ").append(customer.name).append("\n");
        orderDetails.append("Contact: ").append(customer.contactInfo).append("\n\n");
        
        orderDetails.append("Items Ordered:\n");
        
        double total = 0;
        for (OrderItem item : items) {
            orderDetails.append("- ").append(item.getMedicine().getName())
                       .append(" (Qty: ").append(item.getQuantity())
                       .append(", Dosage: ").append(item.getMedicine().getDosage())
                       .append(") - PKR ").append(String.format("%.2f", item.getTotalPrice()))
                       .append("\n");
            total += item.getTotalPrice();
        }
        
        orderDetails.append("\nTOTAL: PKR ").append(String.format("%.2f", total)).append("\n");
        orderDetails.append("\nThank you for choosing ").append(PharmacySystemGUI.PHARMACY_NAME).append("!");
        
        JTextArea textArea = new JTextArea(orderDetails.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(null, scrollPane, "Order Summary", JOptionPane.INFORMATION_MESSAGE);
    }
}

// OrderItem class
class OrderItem {
    private Medicine medicine;
    private int quantity;
    
    public OrderItem(Medicine medicine, int quantity) {
        this.medicine = medicine;
        this.quantity = quantity;
        medicine.reduceStock(quantity);
    }
    
    public double getTotalPrice() {
        return medicine.getPrice() * quantity;
    }
    
    public Medicine getMedicine() {
        return medicine;
    }
    
    public int getQuantity() {
        return quantity;
    }
}

// Medicine class
class Medicine {
    private String name;
    private String use;
    private String dosage;
    private double price;
    private int stock;
    
    public Medicine(String name, String use, String dosage, double price, int stock) {
        this.name = name;
        this.use = use;
        this.dosage = dosage;
        this.price = price;
        this.stock = stock;
    }
    
    public String getName() { return name; }
    public String getUse() { return use; }
    public String getDosage() { return dosage; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    
    public void reduceStock(int quantity) {
        if (quantity <= stock) {
            stock -= quantity;
        }
    }
    
    public boolean hasStock(int quantity) {
        return stock >= quantity;
    }
}

// Main GUI System for Customers
class PharmacySystemGUI extends JFrame implements InventoryManager {
    public static final String PHARMACY_NAME = "MediCare Pharmacy";
    public static final String PHARMACY_SLOGAN = "Your Health, Our Priority";
    public static final String PHARMACY_LOCATION = "123 Health Street, Karachi";
    public static final String PHARMACY_CONTACT = "021-1234567";
    
    private Map<String, Medicine> adultMedicines;
    private Map<String, Medicine> childMedicines;
    
    private JPanel mainPanel;
    private JButton placeOrderBtn;
    private JButton viewMedicinesBtn;
    private JButton exitBtn;
    
    public PharmacySystemGUI() {
        super(PHARMACY_NAME + " - Customer Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        initializeSystem();
        createMainPanel();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                    null, 
                    "Are you sure you want to exit?", 
                    "Exit Confirmation", 
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
    
    private void initializeSystem() {
        this.adultMedicines = new HashMap<>();
        this.childMedicines = new HashMap<>();
        
        // Initialize adult medicines
        addMedicine(adultMedicines, new Medicine("Paracetamol (Panadol)", "Pain & fever", "500-1000mg every 4-6h (max 4g/day)", 50, 100));
        addMedicine(adultMedicines, new Medicine("Ibuprofen (Brufen)", "Pain, inflammation", "200-400mg every 6h (max 1200mg/day)", 100, 80));
        addMedicine(adultMedicines, new Medicine("Aspirin (Disprin)", "Pain, fever, heart protection", "325-650mg (low-dose: 75mg for heart)", 50, 75));
        addMedicine(adultMedicines, new Medicine("Omeprazole (Omee)", "Acid reflux, GERD", "20-40mg once daily", 150, 70));
        addMedicine(adultMedicines, new Medicine("Loratadine (Claritin)", "Allergies", "10mg once daily", 200, 65));
        addMedicine(adultMedicines, new Medicine("Simvastatin (Zocor)", "High cholesterol", "10-40mg at bedtime", 500, 40));
        addMedicine(adultMedicines, new Medicine("Metformin (Glucophage)", "Type 2 diabetes", "500-1000mg twice daily", 100, 60));
        addMedicine(adultMedicines, new Medicine("Salbutamol (Ventolin inhaler)", "Asthma", "1-2 puffs every 4-6h", 500, 40));
        addMedicine(adultMedicines, new Medicine("Dextromethorphan (Tixylix)", "Cough", "10-20mg every 4h (max 120mg/day)", 150, 50));
        addMedicine(adultMedicines, new Medicine("Hydrocortisone cream (Dermovate)", "Skin rashes, itching", "Apply 1-2 times daily", 200, 60));
        
        // Initialize child medicines
        addMedicine(childMedicines, new Medicine("Paracetamol (Calpol)", "Pain & fever", "10-15mg/kg every 4-6h (max 5 doses/day)", 100, 90));
        addMedicine(childMedicines, new Medicine("Ibuprofen (Brufen syrup)", "Pain, fever", "5-10mg/kg every 6-8h (max 40mg/kg/day)", 150, 80));
        addMedicine(childMedicines, new Medicine("Amoxicillin (Moxlin)", "Bacterial infections", "20-40mg/kg/day divided every 8-12h", 200, 70));
        addMedicine(childMedicines, new Medicine("Cetirizine (Zyrtec drops)", "Allergies", "2-5 years: 2.5mg; 6+ years: 5-10mg daily", 200, 65));
        addMedicine(childMedicines, new Medicine("Salbutamol (Ventolin syrup)", "Asthma", "0.1mg/kg/dose", 150, 55));
        addMedicine(childMedicines, new Medicine("ORS (Pedialyte, Rehidrat)", "Dehydration", "As needed (follow instructions)", 50, 100));
        addMedicine(childMedicines, new Medicine("Azithromycin (Zithromax)", "Bacterial infections", "10mg/kg once daily for 3-5 days", 300, 60));
        addMedicine(childMedicines, new Medicine("Multivitamin (Polybion syrup)", "Vitamin deficiency", "As per doctor's advice", 200, 75));
        addMedicine(childMedicines, new Medicine("Gripe Water (Woodward's)", "Colic, gas", "5-10ml as needed", 100, 90));
        addMedicine(childMedicines, new Medicine("Teething Gel (Dentinox)", "Teething pain", "Apply to gums as needed", 200, 70));
    }
    
    private void addMedicine(Map<String, Medicine> inventory, Medicine medicine) {
        inventory.put(medicine.getName(), medicine);
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header with pharmacy info
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(PHARMACY_NAME);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel sloganLabel = new JLabel(PHARMACY_SLOGAN);
        sloganLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(sloganLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));
        
        placeOrderBtn = new JButton("Place New Order");
        placeOrderBtn.addActionListener(e -> placeOrder());
        
        viewMedicinesBtn = new JButton("Browse Medicines");
        viewMedicinesBtn.addActionListener(e -> showInventory());
        
        exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> exitSystem());
        
        // Style buttons
        styleButton(placeOrderBtn, new Color(70, 130, 180));
        styleButton(viewMedicinesBtn, new Color(60, 179, 113));
        styleButton(exitBtn, new Color(220, 20, 60));
        
        buttonPanel.add(placeOrderBtn);
        buttonPanel.add(viewMedicinesBtn);
        buttonPanel.add(exitBtn);
        
        // Footer with contact info
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel locationLabel = new JLabel("Location: " + PHARMACY_LOCATION);
        locationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel contactLabel = new JLabel("Contact: " + PHARMACY_CONTACT);
        contactLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        footerPanel.add(locationLabel);
        footerPanel.add(contactLabel);
        
        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void placeOrder() {
        // First ask if this is for a child or adult
        int patientType = JOptionPane.showOptionDialog(this,
            "Is this order for a child or adult?",
            "Patient Type",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new Object[]{"Child", "Adult", "Cancel"},
            "Adult");
        
        if (patientType == JOptionPane.CLOSED_OPTION || patientType == 2) {
            return; // User cancelled
        }
        
        boolean isChild = (patientType == 0);
        
        // Collect customer information
        Customer customer = collectCustomerInformation();
        if (customer == null) return; // User cancelled
        
        // Create order
        Order order = new Order(this, customer, "Customer Order");
        boolean addingMore = true;
        
        while (addingMore) {
            Medicine medicine = selectMedicine(isChild);
            if (medicine == null) {
                addingMore = false;
                continue;
            }
            
            int quantity = selectQuantity(medicine);
            if (quantity == 0) continue; // User cancelled
            
            if (medicine.hasStock(quantity)) {
                order.addItem(new OrderItem(medicine, quantity));
                JOptionPane.showMessageDialog(this,
                    "Added to order:\n" + medicine.getName() + 
                    "\nQuantity: " + quantity + 
                    "\nDosage: " + medicine.getDosage() + 
                    "\nPrice: PKR " + String.format("%.2f", medicine.getPrice() * quantity),
                    "Item Added",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Not enough stock. Only " + medicine.getStock() + " available.",
                    "Stock Warning",
                    JOptionPane.WARNING_MESSAGE);
                suggestAlternative(medicine, isChild, order);
            }
            
            int response = JOptionPane.showConfirmDialog(this,
                "Would you like to add another medicine?",
                "Add More Items",
                JOptionPane.YES_NO_OPTION);
            addingMore = (response == JOptionPane.YES_OPTION);
        }
        
        if (!order.getItems().isEmpty()) {
            order.displayOrder();
        } else {
            JOptionPane.showMessageDialog(this,
                "No items were added to the order.",
                "Empty Order",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private Customer collectCustomerInformation() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        // Required fields
        JTextField nameField = new JTextField();
        JTextField dobField = new JTextField();
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();
        
        // Optional fields
        JTextField allergiesField = new JTextField("None");
        JTextField currentMedsField = new JTextField("None");
        
        panel.add(new JLabel("Full Name*:"));
        panel.add(nameField);
        panel.add(new JLabel("Date of Birth (YYYY-MM-DD)*:"));
        panel.add(dobField);
        panel.add(new JLabel("Gender*:"));
        panel.add(genderCombo);
        panel.add(new JLabel("Phone Number*:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email*:"));
        panel.add(emailField);
        panel.add(new JLabel("Address*:"));
        panel.add(addressField);
        panel.add(new JLabel("Known Allergies:"));
        panel.add(allergiesField);
        panel.add(new JLabel("Current Medications:"));
        panel.add(currentMedsField);
        
        while (true) {
            int result = JOptionPane.showConfirmDialog(
                null, panel, "Customer Information", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result != JOptionPane.OK_OPTION) {
                return null; // User cancelled
            }
            
            // Validate inputs
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Name cannot be empty");
                }
                
                LocalDate dob = LocalDate.parse(dobField.getText());
                if (dob.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException("Date of birth cannot be in the future");
                }
                
                String phone = phoneField.getText().replaceAll("[^0-9]", "");
                if (!phone.matches("\\d{11}")) {
                    throw new IllegalArgumentException("Phone number must be exactly 11 digits");
                }
                
                String email = emailField.getText().trim();
                if (!email.matches(".+@.+\\..+")) {
                    throw new IllegalArgumentException("Please enter a valid email address");
                }
                
                String address = addressField.getText().trim();
                if (address.isEmpty()) {
                    throw new IllegalArgumentException("Address cannot be empty");
                }
                
                String allergies = allergiesField.getText().trim();
                if (allergies.isEmpty()) allergies = "None";
                
                String currentMeds = currentMedsField.getText().trim();
                if (currentMeds.isEmpty()) currentMeds = "None";
                
                String contactInfo = phone + " | " + email;
                
                return new Customer(name, contactInfo, dob, address, 
                                   allergies, currentMeds, 
                                   genderCombo.getSelectedItem().toString(), email);
                
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD format (e.g., 1990-05-15)",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private Medicine selectMedicine(boolean forChild) {
        Map<String, Medicine> inventory = forChild ? childMedicines : adultMedicines;
        
        // Create a sorted list of medicine names
        List<String> medicineNames = new ArrayList<>(inventory.keySet());
        Collections.sort(medicineNames);
        
        JComboBox<String> medicineCombo = new JComboBox<>(medicineNames.toArray(new String[0]));
        medicineCombo.setEditable(true);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Select Medicine:"), BorderLayout.NORTH);
        panel.add(medicineCombo, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Select Medicine", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String selectedName = (String) medicineCombo.getSelectedItem();
            return inventory.get(selectedName);
        }
        
        return null;
    }
    
    private int selectQuantity(Medicine medicine) {
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, medicine.getStock(), 1));
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Medicine:"));
        panel.add(new JLabel(medicine.getName()));
        panel.add(new JLabel("Available Stock:"));
        panel.add(new JLabel(String.valueOf(medicine.getStock())));
        panel.add(new JLabel("Enter Quantity:"));
        panel.add(quantitySpinner);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Select Quantity", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            return (int) quantitySpinner.getValue();
        }
        
        return 0;
    }
    
    private void suggestAlternative(Medicine originalMed, boolean forChild, Order order) {
        int response = JOptionPane.showConfirmDialog(this,
            "Would you like to see alternatives for " + originalMed.getName() + "?",
            "Alternative Medicines",
            JOptionPane.YES_NO_OPTION);
        
        if (response != JOptionPane.YES_OPTION) {
            return;
        }
        
        Map<String, Medicine> inventory = forChild ? childMedicines : adultMedicines;
        List<Medicine> alternatives = new ArrayList<>();
        
        for (Medicine med : inventory.values()) {
            if (med.getUse().equals(originalMed.getUse()) && !med.getName().equals(originalMed.getName())) {
                alternatives.add(med);
            }
        }
        
        if (!alternatives.isEmpty()) {
            JPanel panel = new JPanel(new BorderLayout());
            JComboBox<Medicine> altCombo = new JComboBox<>(alternatives.toArray(new Medicine[0]));
            altCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                             boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Medicine) {
                        Medicine m = (Medicine) value;
                        setText(m.getName() + " (Stock: " + m.getStock() + ")");
                    }
                    return this;
                }
            });
            
            panel.add(new JLabel("Select an alternative:"), BorderLayout.NORTH);
            panel.add(altCombo, BorderLayout.CENTER);
            
            int selection = JOptionPane.showConfirmDialog(
                this, panel, "Alternative Medicines", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (selection == JOptionPane.OK_OPTION) {
                Medicine selectedAlt = (Medicine) altCombo.getSelectedItem();
                int quantity = selectQuantity(selectedAlt);
                if (quantity > 0) {
                    order.addItem(new OrderItem(selectedAlt, quantity));
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "No alternatives available for " + originalMed.getName(),
                "No Alternatives",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showInventory() {
        JTabbedPane inventoryTabs = new JTabbedPane();
        
        // Adult medicines tab
        JPanel adultPanel = createInventoryPanel(adultMedicines, "Adult Medicines");
        inventoryTabs.addTab("Adult Medicines", adultPanel);
        
        // Child medicines tab
        JPanel childPanel = createInventoryPanel(childMedicines, "Child Medicines");
        inventoryTabs.addTab("Child Medicines", childPanel);
        
        JOptionPane.showMessageDialog(this, 
            inventoryTabs, 
            "Available Medicines", 
            JOptionPane.PLAIN_MESSAGE);
    }
    
    private JPanel createInventoryPanel(Map<String, Medicine> inventory, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table data
        String[] columnNames = {"Medicine Name", "Use", "Dosage", "Price (PKR)", "Stock"};
        Object[][] data = new Object[inventory.size()][5];
        
        int i = 0;
        for (Medicine med : inventory.values()) {
            data[i][0] = med.getName();
            data[i][1] = med.getUse();
            data[i][2] = med.getDosage();
            data[i][3] = med.getPrice();
            data[i][4] = med.getStock();
            i++;
        }
        
        // Create table
        JTable table = new JTable(data, columnNames);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void exitSystem() {
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to exit?", 
            "Exit Confirmation", 
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    // Interface implementations
    @Override
    public void viewInventory() {
        showInventory();
    }
    
    @Override
    public Medicine findMedicine(String name, boolean isChild) {
        Map<String, Medicine> inventory = isChild ? childMedicines : adultMedicines;
        return inventory.get(name);
    }
}

public class PharmacyCustomerSystem {
    public static void main(String[] args) {
        // Test database connection before starting UI
        try (Connection testConn = DatabaseManager.getConnection()) {
            System.out.println("Successfully connected to database");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Failed to connect to database: " + e.getMessage() + 
                "\nPlease check your database connection and credentials.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                PharmacySystemGUI system = new PharmacySystemGUI();
                system.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to initialize application: " + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
