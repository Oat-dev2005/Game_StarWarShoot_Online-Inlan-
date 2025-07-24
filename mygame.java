import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class mygame {
    public static void main(String[] args) {
        MyGameFrame game = new MyGameFrame();
        game.setVisible(true);
    }

    static class MyGameFrame extends JFrame {
        MyGamePanel panel = new MyGamePanel();

        public MyGameFrame() {
            setTitle("My Game");
            setSize(1800, 1000); // ขนาดของเฟรม
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            add(panel);
            setVisible(true);
        }
    }

    static class MyGamePanel extends JPanel {
        Image background = Toolkit.getDefaultToolkit().createImage(
                System.getProperty("user.dir") + File.separator + "starwar.jpg"
        );

        Image ghost = Toolkit.getDefaultToolkit().createImage(
                System.getProperty("user.dir") + File.separator + "fighter.png"
        );

        Image sight = Toolkit.getDefaultToolkit().createImage(
                System.getProperty("user.dir") + File.separator + "sight.gif"
        );

        int numOfGhosts = 20;
        int[] x = new int[numOfGhosts];
        int[] y = new int[numOfGhosts];
        int[] size = new int[numOfGhosts];
        int[] direction = new int[numOfGhosts];  // ทิศทางของยาน (0: ซ้าย, 1: ขวา, 2: ขึ้น, 3: ลง)
        boolean[] isAlive = new boolean[numOfGhosts]; // ติดตามสถานะของผี
        int ghostsRemaining; // จำนวนผีที่เหลือ
        int hp = 100; // จำนวน HP เริ่มต้น
        boolean gameOver = false; // ติดตามสถานะเกมว่าเป็นเกมจบหรือไม่

        int sightX = 0;
        int sightY = 0;

        public MyGamePanel() {
            setSize(1800, 1000);
            setLocation(0, 0);

            Random random = new Random();

            for (int i = 0; i < numOfGhosts; i++) {
                x[i] = random.nextInt(1600); // กำหนดตำแหน่งเริ่มต้นของยาน
                y[i] = random.nextInt(900);
                direction[i] = random.nextInt(4); // สุ่มทิศทางเริ่มต้น
                size[i] = random.nextInt(100) + 50; // ขนาดของยาน
                isAlive[i] = true; // ตั้งค่าให้ผีมีชีวิตอยู่
            }

            ghostsRemaining = numOfGhosts; // เริ่มต้นจำนวนผีที่เหลือ
            for (int i = 0; i < numOfGhosts; i++) {
                new GhostMover(i).start(); // เริ่ม thread สำหรับแต่ละยาน
            }

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    sightX = e.getX();
                    sightY = e.getY();
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    shoot(e);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(background, 0, 0, this);

            for (int i = 0; i < numOfGhosts; i++) {
                if (isAlive[i]) { // เช็คว่าผียังมีชีวิตอยู่หรือไม่
                    g.drawImage(ghost, x[i], y[i], x[i] + size[i], y[i] + size[i], 0, 0, 100, 100, this);
                }
            }

            g.drawImage(sight, sightX - 50, sightY - 50, this); // วาดเลเซอร์
            
            // แสดง HP บนหน้าจอ
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("HP: " + hp, getWidth() - 150, 50);

            if (ghostsRemaining == 0) { // ถ้าผีหมด
                showWinScreen(g); // แสดงหน้าจอชนะ
            } else if (gameOver) { // ถ้าเกมจบ
                showGameOverScreen(g); // แสดงหน้าจอเกมจบ
            }
        }

        private void showWinScreen(Graphics g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 100));
            g.drawString("Win!", getWidth() / 2 - 100, getHeight() / 2 - 50); // แสดงข้อความ Win
            JButton exitButton = new JButton("Exit");
            exitButton.setBounds(getWidth() / 2 - 50, getHeight() / 2 + 20, 100, 40);
            exitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0); // ออกจากเกม
                }
            });
            this.add(exitButton);
            exitButton.setVisible(true);
            exitButton.setFocusable(false);
        }

        private void showGameOverScreen(Graphics g) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 100));
            g.drawString("Game Over!", getWidth() / 2 - 200, getHeight() / 2 - 50); // แสดงข้อความ Game Over
            JButton exitButton = new JButton("Exit");
            exitButton.setBounds(getWidth() / 2 - 50, getHeight() / 2 + 20, 100, 40);
            exitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0); // ออกจากเกม
                }
            });
            this.add(exitButton);
            exitButton.setVisible(true);
            exitButton.setFocusable(false);
        }

        private void shoot(MouseEvent e) {
            boolean hit = false; // ตรวจสอบว่าผีถูกยิงหรือไม่
            for (int i = 0; i < numOfGhosts; i++) {
                if (isAlive[i]) { // ตรวจสอบว่าผียังมีชีวิตอยู่
                    int hitboxSize = size[i] + 30; // ขยาย hitbox
                    if (e.getX() >= x[i] - 15 && e.getX() <= x[i] + hitboxSize && e.getY() >= y[i] - 15 && e.getY() <= y[i] + hitboxSize) {
                        isAlive[i] = false; // ทำให้ผีหาย
                        ghostsRemaining--; // ลดจำนวนผีที่เหลือ
                        hit = true; // ยืนยันว่ามีการยิงโดน
                        break; // ออกจากลูปเมื่อยิงได้
                    }
                }
            }
            if (!hit) { // ถ้ายิงไม่โดน
                hp = Math.max(0, hp - 30); // ลด HP ลง 30, ป้องกันไม่ให้ติดลบ
            }

            // เช็คว่า HP หมดหรือไม่
            if (hp == 0) {
                gameOver = true; // ตั้งค่าให้เกมจบ
            }

            repaint(); // รีเฟรชหน้าจอหลังจากยิง
        }

        // คลาสย่อยเพื่อควบคุมการเคลื่อนไหวของยานแต่ละตัว
        class GhostMover extends Thread {
            int ghostIndex;

            public GhostMover(int index) {
                this.ghostIndex = index;
            }

            @Override
            public void run() {
                Random random = new Random();
                while (true) {
                    try {
                        // เปลี่ยนทิศทางหลังจากเคลื่อนที่ไปได้ช่วงหนึ่ง
                        if (random.nextInt(100) < 5) {  // โอกาสเปลี่ยนทิศทาง 5%
                            direction[ghostIndex] = random.nextInt(4);  // สุ่มทิศทางใหม่
                        }

                        // เคลื่อนที่ตามทิศทางที่กำหนด
                        switch (direction[ghostIndex]) {
                            case 0: // ซ้าย
                                x[ghostIndex] -= 5;
                                if (x[ghostIndex] < 0) x[ghostIndex] = 0;  // ป้องกันไม่ให้เกินขอบจอ
                                break;
                            case 1: // ขวา
                                x[ghostIndex] += 5;
                                if (x[ghostIndex] > getWidth() - size[ghostIndex]) {
                                    x[ghostIndex] = getWidth() - size[ghostIndex];  // ป้องกันไม่ให้เกินขอบจอ
                                }
                                break;
                            case 2: // ขึ้น
                                y[ghostIndex] -= 5;
                                if (y[ghostIndex] < 0) y[ghostIndex] = 0;  // ป้องกันไม่ให้เกินขอบจอ
                                break;
                            case 3: // ลง
                                y[ghostIndex] += 5;
                                if (y[ghostIndex] > getHeight() - size[ghostIndex]) {
                                    y[ghostIndex] = getHeight() - size[ghostIndex];  // ป้องกันไม่ให้เกินขอบจอ
                                }
                                break;
                        }

                        // รีเฟรชหน้าจอ
                        repaint();

                        // หน่วงเวลาการเคลื่อนไหวเพื่อให้เห็นการเคลื่อนไหว
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void setVisible(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setVisible'");
    }
}
