import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird class
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  // Scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4; // Speed at which pipes move to the left (simulates the bird moving right)
    int velocityY = 0;  // Speed of the bird moving up/down
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;
    double highScore = 0;

    JPanel scorePanel;
    JLabel scoreLabel;
    JLabel highScoreLabel;
    JPanel gameOverPanel;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialize bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Timer for placing pipes
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        // Game timer
        gameLoop = new Timer(1000 / 60, this); // Time between frames in milliseconds
        gameLoop.start();

        // Initialize score and game over panels
        initializeScorePanel();
        initializeGameOverPanel();
    }

    void initializeScorePanel() {
        scorePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(245, 253, 0, 128)); // Semi-transparent green background
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        scorePanel.setOpaque(false);
        scorePanel.setPreferredSize(new Dimension(boardWidth, 50)); 
        scorePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Black border

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLACK); // Text color in black
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));

        highScoreLabel = new JLabel("High Score: 0");
        highScoreLabel.setForeground(Color.RED); // Text color in red
        highScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));

        setLayout(new BorderLayout());
        add(scorePanel, BorderLayout.NORTH);

        // Position labels using GridBagLayout
        scorePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10); // No spacing between labels
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // Add score label to the left
        gbc.gridx = 0;
        gbc.gridy = 0;
        scorePanel.add(scoreLabel, gbc);

        // Add high score label to the bottom-right
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST; // Align to the right
        scorePanel.add(highScoreLabel, gbc);
    }

    void initializeGameOverPanel() {
        gameOverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black background
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 36)); // Font size for "Game Over"
                FontMetrics fm = g.getFontMetrics();
                String text = "Game Over";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g.drawString(text, (getWidth() - textWidth) / 2, (getHeight() + textHeight / 2) / 2);
            }
        };
        gameOverPanel.setOpaque(false);
        gameOverPanel.setPreferredSize(new Dimension(boardWidth / 2, boardHeight / 6)); // Adjusted dimensions
        gameOverPanel.setVisible(false); // Initially hidden
        add(gameOverPanel, BorderLayout.CENTER);
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // Bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score
        if (!gameOver) {
            scoreLabel.setText("Score: " + (int) score);
        }
        highScoreLabel.setText("High Score: " + (int) highScore);

        // Show Game Over panel if the game is over
        if (gameOver) {
            gameOverPanel.setVisible(true);
        }
    }

    public void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Apply gravity and limit the bird's y position to the top of the canvas

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // 0.5 because there are 2 pipes! so 0.5*2 = 1, 1 for each set of pipes
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }

        // Update high score
        if (score > highScore) {
            highScore = score;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                // Restart game by resetting conditions
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameOverPanel.setVisible(false); // Hide the Game Over panel
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
