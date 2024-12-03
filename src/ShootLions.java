import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
//import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.imageio.ImageIO; // For reading images
import java.awt.Image;       // For handling images
import java.io.File;
import java.io.IOException;

class GameFrame extends JFrame {
    public GameFrame() {
        this.setTitle("ShootLions");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        GamePanel gamePanel = new GamePanel();
        this.add(gamePanel);

        // Restart Button
        JButton restartButton = new JButton("Restart The Game");
        restartButton.setFocusable(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> gamePanel.restartGame());
        this.add(restartButton, BorderLayout.SOUTH);

        gamePanel.setRestartButton(restartButton);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}

public class ShootLions {
    public static void main(String[] args) {
        //GameFrame frame = new GameFrame();
        SwingUtilities.invokeLater(GameFrame::new);
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
        private Timer timer;
        private Player player;
        private ArrayList<Lion> lions;
        private ArrayList<Bullet> bullets;
        private int score = 0;
        private boolean gameOver = false;
        private JButton restartButton;

        public GamePanel() {
            this.setPreferredSize(new Dimension(800, 600));
            this.setBackground(Color.GREEN);
            this.setFocusable(true);
            this.addKeyListener(this);

            player = new Player(400, 300); // Tree position
            lions = new ArrayList<>();
            bullets = new ArrayList<>();

            // Spawn lions at regular intervals
            Timer lionSpawner = new Timer(2000, e -> {
                if (!gameOver) lions.add(new Lion());
            });
            lionSpawner.start();

            timer = new Timer(16, this); // 60 FPS
            timer.start();
        }

    public void setRestartButton(JButton restartButton) {
        this.restartButton = restartButton;
    }

    public void restartGame() {
        score = 0;
        gameOver = false;
        lions.clear();
        bullets.clear();
        restartButton.setVisible(false);
        requestFocusInWindow(); // Refocus the panel for key inputs
    }


    @Override
        public void actionPerformed(ActionEvent e) {
            if(gameOver) {
                restartButton.setVisible(true); // Make the button visible when the game ends
                return;
            }
            // Update game state
            player.update(bullets);
            for (Lion lion : lions) lion.update();
            for (Bullet bullet : bullets) bullet.update();

            for (Lion lion : lions) {
                if (lion.isHit(bullets)) {
                    score += 10;
                } else if (lion.reachesPlayer(player)) {
                    gameOver = true;
                    restartButton.setVisible(true);
                }
            }

            // Collision detection and removing off-screen objects
            bullets.removeIf(bullet -> !bullet.isOnScreen());
            lions.removeIf(Lion::isDefeated );
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (gameOver) {
                g.setColor(Color.RED);
                g.setFont(new Font("Times New Roman", Font.BOLD, 40));
                g.drawString("Game Over", 300, 250);
                g.drawString("Score: " + score, 330, 300);
            } else {
                player.draw(g);
                for (Lion lion : lions) lion.draw(g);
                for (Bullet bullet : bullets) bullet.draw(g);

                // Draw score
                g.setColor(Color.WHITE);
                g.setFont(new Font("Times New Roman", Font.PLAIN, 20));
                g.drawString("Score: " + score, 10, 20);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }

        @Override
        public void keyTyped(KeyEvent e) {}
    }

class Player {
        private int x, y, angle;
        private boolean shooting;
        private int shootCooldown = 0;

        public Player(int x, int y) {
            this.x = x;
            this.y = y;
            this.angle = 0;
        }

        public void update(ArrayList<Bullet> bullets) {
            if (shooting && shootCooldown == 0) {
                bullets.add(new Bullet(x, y, angle));
                shootCooldown = 20; // Cooldown between shots
            }
            if (shootCooldown > 0) shootCooldown--;
        }

        public void draw(Graphics g) {

            Graphics2D g2d = (Graphics2D) g; //casting Graphics2D inside of graphics as g
            g2d.setColor(Color.BLUE);
            // Translate and rotate for proper orientation
            g2d.translate(x, y);
            g2d.rotate(Math.toRadians(angle));
            g2d.fillRect(-20, -10, 40, 20); // Rotating player

            // Draw the triangle (front)
            int[] xPoints = {25, 20, 20}; // Coordinates relative to (x, y)
            int[] yPoints = {0, 10, -10};
            g2d.fillPolygon(xPoints, yPoints, 3);

            g2d.rotate(-Math.toRadians(angle));
            g2d.translate(-x, -y);
        }

        public void shoot() {
            // Add bullet logic here
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) angle -= 10;
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) angle += 10;
            if (e.getKeyCode() == KeyEvent.VK_SPACE) shooting = true;
        }

        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) shooting = false;
        }

        public Rectangle getBounds() {
            return new Rectangle(x - 20, y - 20, 40, 40);
        }
    }

class Bullet {
    private int x, y;
    private double angle;
    private int speed = 10;

    public Bullet(int x, int y, int angle) {
        this.x = x;
        this.y = y;
        this.angle = Math.toRadians(angle);
    }

    public void update() {
        x += speed * Math.cos(angle);
        y += speed * Math.sin(angle);
    }

    public boolean isOnScreen() {
        return x >= 0 && x <= 800 && y >= 0 && y <= 600;
    }

    public Rectangle getBounds() {
        return new Rectangle(x - 5, y - 5, 10, 10);
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x - 5, y - 5, 10, 10); // Draw bullet
    }
}



class Lion {
    private int x, y;
    private int speed = 3;
    private boolean defeated = false;
    private static Image lionImage;

    static {
        try {
            lionImage = ImageIO.read(new File("./Lion.png"));
            //lionImage = ImageIO.read(getClass().getResource("/Lion2.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Lion() {


        if (Math.random() < 0.5) {
            x = (Math.random() < 0.5) ? 0 : 800; // Left or right
            y = (int) (Math.random() * 600);    // Random Y
        } else {
            y = (Math.random() < 0.5) ? 0 : 600; // Top or bottom
            x = (int) (Math.random() * 800);    // Random X
        }
    }

    public void update() {
        if (!defeated) {
            int targetX = 400, targetY = 300; // Player's position
            double angle = Math.atan2(targetY - y, targetX - x);
            x += (int) (speed * Math.cos(angle));
            y += (int) (speed * Math.sin(angle));
        }
    }

    public boolean isHit(ArrayList<Bullet> bullets) {
        Rectangle bounds = new Rectangle(x - 25, y - 25, 50, 50);
        for (Bullet bullet : bullets) {
            if (bounds.intersects(bullet.getBounds())) {
                defeated = true;
                bullets.remove(bullet);
                return true;
            }
        }
        return false;
    }

    public boolean reachesPlayer(Player player) {
        return !defeated && new Rectangle(x - 25, y - 25, 50, 50).intersects(player.getBounds());
    }

    public boolean isDefeated() {
        return defeated || (x < 0 || x > 800 || y < 0 || y > 600);
    }

    public void draw(Graphics g) {
        //Graphics2D g2d2 = (Graphics2D) g;

        if (!defeated && lionImage != null) {
            //g.setColor(Color.ORANGE);
            //g.fillRect(x - 25, y - 25, 50, 50);
            // Draw the image centered at (x, y)
            g.drawImage(lionImage, x-lionImage.getWidth(null) / 2, y-lionImage.getHeight(null) / 2, null);
        }
    }
}


