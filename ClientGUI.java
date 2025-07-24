import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

// คลาสหลักของ client GUI
public class ClientGUI extends JFrame {
    private Socket socket; // Socket สำหรับเชื่อมต่อกับเซิร์ฟเวอร์
    private PrintWriter out; // สำหรับส่งข้อความไปยังเซิร์ฟเวอร์
    private JTextField ipField, nameField; // ช่องกรอกข้อมูล IP ของเซิร์ฟเวอร์และชื่อผู้ใช้
    private JPanel inputPanel; // พาแนลสำหรับป้อนข้อมูล
    private JButton connectButton; // ปุ่มสำหรับเชื่อมต่อ

    // คอนสตรัคเตอร์สำหรับตั้งค่า GUI
    public ClientGUI() {
        inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        ipField = new JTextField(15);
        nameField = new JTextField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Server IP:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(ipField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Your Name:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(nameField, gbc);

        connectButton = new JButton("Connect");
        connectButton.setPreferredSize(new Dimension(100, 30));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(connectButton, gbc);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.CENTER);

        setTitle("Client GUI");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
    }

    // เมธอดสำหรับเชื่อมต่อกับเซิร์ฟเวอร์
    private void connect() {
        String serverIp = ipField.getText().trim();
        String name = nameField.getText().trim();
        
        if (serverIp.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Server IP and your Name.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        try {
            socket = new Socket(serverIp, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(name); // ส่งชื่อผู้ใช้ไปยังเซิร์ฟเวอร์
            inputPanel.setVisible(false); // ซ่อน inputPanel เมื่อต่อเชื่อมสำเร็จ
            new DrawImage(socket, out); // เปิดหน้า DrawImage พร้อมส่ง PrintWriter
            setVisible(false); // ซ่อน ClientGUI
            listenForMessages(); // เริ่มฟังข้อความจากเซิร์ฟเวอร์
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // เมธอดสำหรับฟังข้อความจากเซิร์ฟเวอร์
    private void listenForMessages() {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("START_GAME")) {
                        startGame();
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Server connection lost.", "Connection Error", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        }).start();
    }

    // เมธอดสำหรับเริ่มเกม
    private void startGame() {
        // ไม่ต้องทำอะไรที่นี่ เพราะการเริ่มเกมอยู่ใน DrawImage
    }

    // เมธอด main สำหรับเริ่มโปรแกรมและแสดง GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI clientGUI = new ClientGUI();
            clientGUI.setVisible(true);
        });
    }
}

