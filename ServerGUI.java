import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerGUI extends JFrame {

    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private JTextArea textArea;
    private JTextField ipField; // ฟิลด์สำหรับการป้อนที่อยู่ IP
    private JButton startButton, stopButton;
    private boolean running = false;

    public ServerGUI() {
        clients = new ArrayList<>();
        textArea = new JTextArea(15, 30);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        // สร้างฟิลด์สำหรับป้อนที่อยู่ IP
        ipField = new JTextField(15); // ปล่อยให้ฟิลด์นี้ว่างสำหรับ IP

        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Server IP:")); // เพิ่มป้ายกำกับสำหรับที่อยู่ IP
        panel.add(ipField);
        panel.add(startButton);
        panel.add(stopButton);

        add(scrollPane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        setTitle("Server GUI");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void startServer() {
        running = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        textArea.append("Server starting...\n");

        new Thread(() -> {
            try {
                // ดึงที่อยู่ IP ของเครื่องที่รันโปรแกรม
                InetAddress localhost = InetAddress.getLocalHost();
                String ipAddress = localhost.getHostAddress(); // รับที่อยู่ IP
                ipField.setText(ipAddress); // อัปเดตฟิลด์ด้วยที่อยู่ IP

                serverSocket = new ServerSocket(12345, 50, localhost); // สร้าง ServerSocket ด้วยที่อยู่ IP ที่ดึงมา
                textArea.append("Server started at " + ipAddress + " on port 12345...\n");
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).start();
                }
            } catch (IOException e) {
                textArea.append("Error starting server: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private void stopServer() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            textArea.append("Error stopping server: " + e.getMessage() + "\n");
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        textArea.append("Server stopped.\n");
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private String clientName;
        private boolean isReady = false;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                clientName = in.readLine(); // รับชื่อผู้ใช้
                synchronized (clients) {
                    clients.add(this);
                    updateClientStatus();
                }

                while (clientSocket.isConnected()) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    if (message.equals("READY")) {
                        synchronized (clients) {
                            isReady = true;
                            updateClientStatus();
                            // ตรวจสอบว่าทุกคนพร้อมหรือยัง
                            if (allClientsReady()) {
                                notifyClientsToStartGame(); // แจ้งให้ทุกคนเริ่มเกม
                            }
                        }
                    }
                }
            } catch (IOException e) {
                textArea.append("Error with client connection: " + e.getMessage() + "\n");
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    textArea.append("Could not close socket.\n");
                }
                synchronized (clients) {
                    if (clientName != null) {
                        clients.remove(this);
                        updateClientStatus();
                    }
                }
            }
        }

        private void updateClientStatus() {
            textArea.setText("Connected Clients:\n");
            for (ClientHandler client : clients) {
                textArea.append(client.clientName + (client.isReady ? " (Ready)" : " (Not Ready)") + "\n");
            }
        }

        private boolean allClientsReady() {
            for (ClientHandler client : clients) {
                if (!client.isReady) {
                    return false;
                }
            }
            return true;
        }

        private void notifyClientsToStartGame() {
            textArea.append("All clients are ready! Starting the game...\n");
            for (ClientHandler client : clients) {
                client.sendStartGame();
            }
            startGame(); // เรียกใช้เมธอดสำหรับเริ่มเกม
        }

        private void sendStartGame() {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("START_GAME"); // ส่งคำสั่งเริ่มเกมไปยังไคลเอนต์
            } catch (IOException e) {
                textArea.append("Error sending start game message to client.\n");
            }
        }

        private void startGame() {
            // TODO: เพิ่ม logic สำหรับเข้าสู่หน้าเล่นเกมที่นี่
            textArea.append("Game is starting for all players!\n");
            // คุณสามารถเปิดหน้าเล่นเกมที่นี่ได้
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerGUI serverGUI = new ServerGUI();
            serverGUI.setVisible(true);
        });
    }
}
