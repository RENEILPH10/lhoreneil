import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;

public class BankingApp {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private HashMap<String, User> users;
    private Admin admin;
    private User currentUser; 

    public BankingApp() {
        users = DataStore.loadUsers();
        if (!users.containsKey("demo")) {
            users.put("demo", new User("demo", "123"));
        }
        admin = new Admin("admin", "admin123"); 

        frame = new JFrame("Central Bank Davao");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panels
        mainPanel.add(startPanel(), "START");
        mainPanel.add(loginPanel(), "LOGIN");
        mainPanel.add(adminPanel(), "ADMIN");
        mainPanel.add(userPanel(), "USER");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Start Panel
    private JPanel startPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("CENTRAL BANK DAVAO", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(title, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        JButton adminBtn = styledButton("ADMIN");
        JButton clientBtn = styledButton("CLIENT");

        adminBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        clientBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        btnPanel.add(adminBtn);
        btnPanel.add(clientBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    JTextField usernameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();

    // Login Panel
    private JPanel loginPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        JButton loginBtn = styledButton("Login");
        JButton registerBtn = styledButton("Register");
        JButton backBtn = styledButton("Back");

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);

        panel.add(btnPanel);

        // Actions
        loginBtn.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (user.equals(admin.getUsername()) && pass.equals(admin.getPassword())) {
                cardLayout.show(mainPanel, "ADMIN");
            } else if (users.containsKey(user) && users.get(user).getPassword().equals(pass)) {
                currentUser = users.get(user);
                cardLayout.show(mainPanel, "USER");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid login!");
            }
        });

        registerBtn.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();
            if (!user.isEmpty() && !pass.isEmpty()) {
                if (!users.containsKey(user)) {
                    users.put(user, new User(user, pass));
                    DataStore.saveUsers(users);
                    JOptionPane.showMessageDialog(frame, "User registered!");
                } else {
                    JOptionPane.showMessageDialog(frame, "User already exists!");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter username and password!");
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "START"));

        return panel;
    }

    // Admin Dashboard
    private JPanel adminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Admin Dashboard", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(header, BorderLayout.NORTH);

        JTextArea output = new JTextArea();
        output.setFont(new Font("Monospaced", Font.PLAIN, 14));
        output.setEditable(false);
        JScrollPane scroll = new JScrollPane(output);
        scroll.setPreferredSize(new Dimension(650, 150));
        panel.add(scroll, BorderLayout.CENTER);

        // Button Grid
        JPanel btnPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        JButton viewUsersBtn = styledButton("View Users");
        JButton saveBtn = styledButton("Save Data");
        JButton reportBtn = styledButton("Report");
        JButton addUserBtn = styledButton("Add User");
        JButton deactivateBtn = styledButton("Deactivate User");
        JButton logoutBtn = styledButton("Logout");

        btnPanel.add(viewUsersBtn);
        btnPanel.add(addUserBtn);
        btnPanel.add(deactivateBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(reportBtn);
        btnPanel.add(logoutBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        // Actions
        viewUsersBtn.addActionListener(e -> output.setText(admin.listUsers(users)));

        addUserBtn.addActionListener(e -> {
            String newname = JOptionPane.showInputDialog("Enter username:");
            String newpass = JOptionPane.showInputDialog("Enter password:");
            if (newname != null && newpass != null && !newname.isEmpty() && !newpass.isEmpty()) {
                if (!users.containsKey(newname)) {
                    users.put(newname, new User(newname, newpass));
                    DataStore.saveUsers(users);
                    JOptionPane.showMessageDialog(frame, "User added!");
                } else {
                    JOptionPane.showMessageDialog(frame, "User already exists!");
                }
            }
        });

        deactivateBtn.addActionListener(e -> {
            String target = JOptionPane.showInputDialog("Enter username to deactivate:");
            if (target != null && users.containsKey(target)) {
                users.remove(target);
                DataStore.saveUsers(users);
                JOptionPane.showMessageDialog(frame, "User deactivated!");
            } else {
                JOptionPane.showMessageDialog(frame, "User not found!");
            }
        });

        saveBtn.addActionListener(e -> {
            DataStore.saveUsers(users);
            JOptionPane.showMessageDialog(frame, "Data saved!");
        });

        reportBtn.addActionListener(e -> generateReport("admin_report.txt", admin.listUsers(users)));

        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "START"));

        return panel;
    }

    // User Dashboard
    private JPanel userPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("User Dashboard", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(header, BorderLayout.NORTH);

        JTextArea history = new JTextArea();
        history.setFont(new Font("Monospaced", Font.PLAIN, 14));
        history.setEditable(false);
        JScrollPane scroll = new JScrollPane(history);
        scroll.setPreferredSize(new Dimension(650, 150));
        panel.add(scroll, BorderLayout.CENTER);

        // Button Grid
        JPanel btnPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        JButton depositBtn = styledButton("Deposit");
        JButton withdrawBtn = styledButton("Withdraw");
        JButton transferBtn = styledButton("Transfer");
        JButton reportBtn = styledButton("Report");
        JButton logoutBtn = styledButton("Logout");

        btnPanel.add(depositBtn);
        btnPanel.add(withdrawBtn);
        btnPanel.add(transferBtn);
        btnPanel.add(reportBtn);
        btnPanel.add(logoutBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        // Actions
        depositBtn.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog("Enter amount to deposit:");
            if (amt != null) {
                try {
                    double val = Double.parseDouble(amt);
                    currentUser.deposit(val);
                    updateHistory(history);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input!");
                }
            }
        });

        withdrawBtn.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog("Enter amount to withdraw:");
            if (amt != null) {
                try {
                    double val = Double.parseDouble(amt);
                    currentUser.withdraw(val);
                    updateHistory(history);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input!");
                }
            }
        });

        transferBtn.addActionListener(e -> {
            String target = JOptionPane.showInputDialog("Enter recipient username:");
            String amt = JOptionPane.showInputDialog("Enter amount to transfer:");
            if (target != null && amt != null && users.containsKey(target)) {
                try {
                    double val = Double.parseDouble(amt);
                    currentUser.transfer(users.get(target), val);
                    updateHistory(history);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input!");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Recipient not found!");
            }
        });

        reportBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            for (Transaction t : currentUser.getHistory()) {
                sb.append(t.toString()).append("\n");
            }
            generateReport(currentUser.getUsername() + "_report.txt", sb.toString());
        });

        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "START"));

        return panel;
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 45));
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        return btn;
    }

    private void updateHistory(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        for (Transaction t : currentUser.getHistory()) {
            sb.append(t.toString()).append("\n");
        }
        area.setText(sb.toString());
    }

    private void generateReport(String fileName, String content) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
            JOptionPane.showMessageDialog(frame, "Report saved as " + fileName);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving report!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankingApp::new);
    }
}


class User {
    private String username;
    private String password;
    private double balance;
    private boolean active;
    private LinkedList<Transaction> history;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance = 0.0;
        this.active = true;
        this.history = new LinkedList<>();
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public void setPassword(String newPass) { this.password = newPass; }
    public double getBalance() { return balance; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }
    public LinkedList<Transaction> getHistory() { return history; }

    public void deposit(double amount) {
        balance += amount;
        history.add(new Transaction("Deposit", amount, balance));
    }

    public void withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
            history.add(new Transaction("Withdraw", amount, balance));
        } else {
            history.add(new Transaction("Failed Withdraw", amount, balance));
        }
    }

    public void transfer(User receiver, double amount) {
        if (amount <= balance && receiver.isActive()) {
            balance -= amount;
            receiver.balance += amount;
            history.add(new Transaction("Transfer to " + receiver.getUsername(), amount, balance));
            receiver.history.add(new Transaction("Transfer from " + username, amount, receiver.getBalance()));
        } else {
            history.add(new Transaction("Failed Transfer to " + receiver.getUsername(), amount, balance));
        }
    }
}

class Admin {
    private String username;
    private String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public String listUsers(HashMap<String, User> users) {
        StringBuilder sb = new StringBuilder();
        for (User u : users.values()) {
            sb.append("User: ").append(u.getUsername())
              .append(" | Balance: ").append(u.getBalance())
              .append(" | Status: ").append(u.isActive() ? "Active" : "Inactive")
              .append("\n");
        }
        return sb.toString();
    }
}

class Transaction {
    private String type;
    private double amount;
    private double balanceAfter;
    private String timestamp;

    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString() {
        return timestamp + " | " + type + " | Amount: " + amount + " | Balance: " + balanceAfter;
    }
}

class DataStore {
    private static final String FILE_NAME = "users.txt";

    public static void saveUsers(HashMap<String, User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (User u : users.values()) {
                pw.println(u.getUsername() + "," + u.getPassword() + "," + u.getBalance() + "," + u.isActive());
                for (Transaction t : u.getHistory()) {
                    pw.println("HISTORY:" + t.toString());
                }
                pw.println("END");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, User> loadUsers() {
        HashMap<String, User> users = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            User currentUser = null;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("HISTORY:") && !line.equals("END")) {
                    String[] parts = line.split(",");
                    currentUser = new User(parts[0], parts[1]);
                    currentUser.deposit(Double.parseDouble(parts[2])); 
                    if (parts.length > 3 && parts[3].equals("false")) {
                        currentUser.deactivate();
                    }
                    users.put(parts[0], currentUser);
                }
            }
        } catch (IOException e) {
            System.out.println("No existing database found, starting fresh...");
        }
        return users;
    }
}
