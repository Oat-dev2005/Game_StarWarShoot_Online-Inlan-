import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class DrawImage extends JFrame implements ActionListener {
    private Socket socket; // เก็บ socket ที่เชื่อมต่อ
    private PrintWriter out; // สำหรับส่งข้อความไปยังเซิร์ฟเวอร์
    private BufferedReader in; // สำหรับอ่านข้อความจากเซิร์ฟเวอร์

    MyBackground bg = new MyBackground();
    static JLayeredPane panel1 = new JLayeredPane();
    JButton button1 = new JButton("START");
    JButton button2 = new JButton("Exit");

    public DrawImage(Socket socket, PrintWriter out) {
        this.socket = socket; // เก็บ socket
        this.out = out; // เก็บ PrintWriter

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // เปิด BufferedReader
        } catch (Exception e) {
            e.printStackTrace();
        }

        setSize(1366, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel1.setBounds(0, 0, 1366, 768);
        bg.setBounds(0, 0, 1366, 768);

        button1.setBounds(750, 750, 120, 50);
        button1.setBackground(Color.WHITE);
        button1.setFont(new Font("Times new Roman", Font.PLAIN, 18));
        button1.addActionListener(this);

        button2.setBounds(950, 750, 120, 50);
        button2.setBackground(Color.WHITE);
        button2.setFont(new Font("Times new Roman", Font.PLAIN, 18));
        button2.addActionListener(this);

        panel1.add(bg, Integer.valueOf(0)); // set layer(0)
        panel1.add(button1, Integer.valueOf(1)); // set layer(1)
        panel1.add(button2, Integer.valueOf(1));

        // เริ่มเล่นเสียง
        playBackgroundMusic();

        // Thread เพื่อตรวจสอบสถานะเซิร์ฟเวอร์และรับคำสั่งจากเซิร์ฟเวอร์
        new Thread(() -> {
            try {
                while (true) {
                    if (socket.isClosed() || !socket.isConnected()) {
                        showServerClosedMessage(); // แสดงข้อความว่าทางเซิร์ฟเวอร์ถูกปิด
                        break; // ออกจาก loop
                    }
                    String serverMessage = in.readLine(); // รับข้อความจากเซิร์ฟเวอร์
                    if (serverMessage != null) {
                        if (serverMessage.equals("START_GAME")) {
                            startGame(); // เริ่มเกมเมื่อได้รับคำสั่ง
                        }
                    }
                    Thread.sleep(100); // รอ 0.1 วินาทีก่อนการตรวจสอบครั้งถัดไป
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        setLayout(null);
        add(panel1);
        setVisible(true); // แสดงหน้าต่าง
    }

    private void playBackgroundMusic() {
        try {
            File wavFile = new File(System.getProperty("user.dir") + File.separator + "Star_war_audio.wav");
            AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showServerClosedMessage() {
        JOptionPane.showMessageDialog(this, "Server has been closed.", "Server Closed", JOptionPane.ERROR_MESSAGE);
        dispose(); // ปิดหน้าต่าง
        System.exit(0); // ปิดโปรแกรม
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button1) {
            out.println("READY"); // ส่งข้อความ "READY" ไปยังเซิร์ฟเวอร์
            button1.setEnabled(false); // ปิดปุ่ม START หลังจากกดแล้ว
        } else if (e.getSource() == button2) {
            try {
                out.println("EXIT"); // ส่งข้อความ "EXIT" ไปยังเซิร์ฟเวอร์
                socket.close(); // ปิดการเชื่อมต่อ
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setVisible(false); // ปิดหน้าต่าง
            dispose(); // ทำลายหน้าต่าง
            System.exit(0); // ปิดโปรแกรม
        }
    }

    private void startGame() {
        new mygame.MyGameFrame(); // เริ่มหน้าเกม
        setVisible(false); // ซ่อนหน้าต่างนี้
    }
}

class MyBackground extends JPanel {
    Image bg = Toolkit.getDefaultToolkit().createImage(
            System.getProperty("user.dir") + File.separator + "SpaceWar.jpg");
    Image cusor = Toolkit.getDefaultToolkit().createImage(
            System.getProperty("user.dir") + File.separator + "ship_cusor.png")
            .getScaledInstance(100, 100, Image.SCALE_SMOOTH); // width และ height เพื่อปรับขนาดของรูปภาพ

    int sx = 0;
    int sy = 0;

    public MyBackground() {
        setSize(171, 176);
        setLocation(0, 0);
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                sx = e.getX();
                sy = e.getY();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {}
        });
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(bg, 0, 0, this);
        g.drawImage(cusor, sx - 50, sy - 50, this); // กำหนด sx และ sy เพื่อให้รูปภาพอยู๋ตรงกับ cursor 
        Font game_name = new Font("Times new Roman", Font.BOLD, 72);
        g.setColor(new Color(255, 255, 255));
        g.setFont(game_name);
        g.drawString("MAY FORCE BE WITH YOU", 450, 280);
    }
}
