import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
//import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.imageio.ImageIO; // For reading images
import java.awt.Image;       // For handling images
import java.io.File;
import java.io.IOException;

/**
 * This 2D game was adopted from a scene in the TV Show 1923
 *  Two characters were on top of a tree and defending themselves against hungry lions
 *  game uses keyboard arrow keys to rotate the character clockwise(VK-right) and counter-clockwise(VK-left)
 *  VK-space is used for shooting bullets to lions, which randomly appear on the panel from different angles
 *  @author Kayvan Mivehnejad
 *  @version 1.3
 *  @since 2024
 */


public class ShootLions {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame1 = new GameFrame();
            frame1.setVisible(true);
        });
        //SwingUtilities.invokeLater(GameFrame::new);
    }
}

class GameFrame extends JFrame {
    public GameFrame() {
        this.setTitle("ShootLions");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        GamePanel gamePanel = new GamePanel();
        this.add(gamePanel);

        // Restart Button, which GamePanel's setter method will use this for its own restartButton
        JButton restartButton = new JButton("Restart The Game");
        restartButton.setFocusable(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> gamePanel.restartGame());
        this.add(restartButton, BorderLayout.SOUTH);
        //this.add(restartButton);

        gamePanel.setRestartButton(restartButton);

        this.pack(); // Swing will automatically fit components jButton+jPanel inside of the JFrame
        //this.setSize(800,700);
        this.setLocationRelativeTo(null); //Frame is centered on the screen
        this.setVisible(true);
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
    private int panelWidth = 800;
    private int panelHeight = 600;

    public GamePanel() {
            this.setPreferredSize(new Dimension(panelWidth, panelHeight));
            this.setBackground(Color.GREEN);
            this.setFocusable(true);
            this.addKeyListener(this);

            int pX = panelWidth / 2;
            int pY = panelHeight / 2;

            player = new Player(pX, pY); // Player's position is at the center of the Panel
            lions = new ArrayList<>();
            bullets = new ArrayList<>();

            // Spawn lions at regular intervals
            Timer lionSpawner = new Timer(3000, e -> {
                if (!gameOver) lions.add(new Lion(panelWidth, panelHeight));
            });
            lionSpawner.start();

            timer = new Timer(16, this); // 16 is time for rendering a single frame,so FPS is 1000/16(ms) = 60 FPS
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
        requestFocusInWindow(); // GamePanel constructor is set Focusable, Refocus the panel for key inputs
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            if(gameOver) {
                restartButton.setVisible(true); // Make the button visible when the game is over.
                return;
            }
            // Update game state
            player.shoot(bullets);
            for (Lion lion : lions) lion.run();
            for (Bullet bullet : bullets) bullet.whizz();

            for (Lion lion : lions) {
                if (lion.isHunted(bullets)) {
                    score += 10;
                } else if (lion.huntPlayer(player)) {
                    gameOver = true;
                    restartButton.setVisible(true);
                }
            }

            // Collision detection and removing Collections off-screen objects
            bullets.removeIf(bullet -> !bullet.isBulletOnScreen());
            //lions.removeIf(Lion::isLionOffScreen); //method reference can map predicate directly to the method
            lions.removeIf(lion -> lion.isLionOffScreen()); //similarly, lambda function could be used  as predicate for removeIf
            repaint(); //send a request to paintComponent to redraw gamePanel
        }

    @Override
    public void paintComponent(Graphics g) {
            super.paintComponent(g); //tells java to apply default jFrame paint on g

            int pX = panelWidth / 2; //Using class properties instead of constant numbers e.g., 300
            int pY = panelHeight / 2;

            if (gameOver) {
                g.setColor(Color.RED);
                g.setFont(new Font("Times New Roman", Font.BOLD, 40));
                g.drawString("Game Over", pX-60, pY -50);
                g.drawString("Score: " + score, pX-60, pY);
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

    public void shoot(ArrayList<Bullet> bullets) {
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
    private double angle; //status of bullet is saved as double to take radians for sin,cos of whizz
    private int speed = 10;

    public Bullet(int x, int y, int angle) {
        this.x = x;
        this.y = y;
        this.angle = Math.toRadians(angle); //converts int to double for radians
    }

    public void whizz() {
        x += speed * Math.cos(angle);
        y += speed * Math.sin(angle);
    }

    public boolean isBulletOnScreen() {
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
    private boolean hunted = false;
    private static Image lionImage;
    private int pX;
    private int pY;

    static {
        try {
            lionImage = ImageIO.read(new File("./Lion.png"));
            //lionImage = ImageIO.read(getClass().getResource("/Lion2.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Lion(int panelWidth, int panelHeight) {
        this.pX = panelWidth / 2;
        this.pY = panelHeight / 2;

        if (Math.random() < 0.5) {
            x = (Math.random() < 0.5) ? 0 : panelWidth; // Left or right
            y = (int) (Math.random() * 600);    // Random Y
        } else {
            y = (Math.random() < 0.5) ? 0 : panelHeight; // Top or bottom
            x = (int) (Math.random() * 800);    // Random X
        }
    }

    public void run() {
        if (!hunted) {
            int targetX = pX, targetY = pY; // Player's position
            double angle = Math.atan2(targetY - y, targetX - x); //lion local angle variable is also saved as double to work with Math methods
            x += (int) (speed * Math.cos(angle));
            y += (int) (speed * Math.sin(angle));
        }
    }

    public boolean isHunted(ArrayList<Bullet> bullets) {
        Rectangle bounds = new Rectangle(x - 25, y - 30, 50, 60); //png is 54X60 pixels, and these bounds are estimated for collision detection
        for (Bullet bullet : bullets) {
            if (bounds.intersects(bullet.getBounds())) {
                hunted = true;
                bullets.remove(bullet);
                return true;
            }
        }
        return false;
    }

    public boolean huntPlayer(Player player) {
        return !hunted && new Rectangle(x - 25, y - 30, 50, 60).intersects(player.getBounds());
    }

    public boolean isLionOffScreen() {
        return hunted || (x < 0 || x > 800 || y < 0 || y > 600);
    }

    public void draw(Graphics g) {
        //Graphics2D g2d2 = (Graphics2D) g;

        if (!hunted && lionImage != null) {
            //g.setColor(Color.ORANGE);
            //g.fillRect(x - 25, y - 25, 50, 50);
            // Draw the image centered at (x, y) taking the width and height of the lion png
            g.drawImage(lionImage, x-lionImage.getWidth(null) / 2, y-lionImage.getHeight(null) / 2, null);
        }
    }
}


