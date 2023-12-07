import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FallingFruitGame extends JFrame {

    private static final int BASKET_WIDTH = 80;
    private static final int BASKET_HEIGHT = 60;
    private static final int OBJECT_WIDTH = 50;
    private static final int OBJECT_HEIGHT = 50;
    private static final double INITIAL_NUM_COINS = 1;

    private boolean gameStarted = false;
    private boolean stageOverPopupShown = false;

    private Canvas canvas;
    private Timer countdownTimer;

    private Random rand;
    private int barrierNum = 2;
    private double coinNum = 1.5;
    private int barrierSpeed = 2;
    private int coinSpeed = 2;

    private List<Basket> baskets = new ArrayList<>();
    private List<Rectangle> barriers = new ArrayList<>();
    //private List<List<Coin>> coinsList = new ArrayList<>();
    private List<Rectangle> coins = new ArrayList<>();
    private List<Integer> scores = new ArrayList<>();
    private List<Integer> lives = new ArrayList<>();

    private int stage = 0;
    private int winner;

    private static final int TIME_LIMIT_PER_STAGE = 30000; // Time limit in milliseconds (40 seconds)
    private long remainingTime;
    private long stageStartTime; // Variable to store the start time of the current stage
    private long elapsedTime = 0; // Variable to store the elapsed time in the current stage

    private JLabel timerLabel; // Label to display the countdown timer

    public FallingFruitGame() {
        setTitle("Falling Objects Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        rand = new Random();

        initializeLives();
        initializeBaskets();
        initializeBarriers(barrierNum);
        initializeCoins(INITIAL_NUM_COINS);
        initializeScores();


        // Example loading the ocean background image

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(800, 600)); // Set canvas size
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e.getKeyCode());
            }
        });

        add(canvas, BorderLayout.CENTER);

        timerLabel = new JLabel("Time: " + (TIME_LIMIT_PER_STAGE / 1000) + "s", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(Color.BLACK);
        add(timerLabel, BorderLayout.NORTH);

        setLocationRelativeTo(null);
        setVisible(true);
        
    }

    private void initializeBaskets() {
        ImageIcon basketImage1 = new ImageIcon("img/Basket/player1.png");
        ImageIcon basketImage2 = new ImageIcon("img/Basket/player2.png");
    
        baskets.add(new Basket(getWidth() / 4 - BASKET_WIDTH / 2, getHeight() - 50 - BASKET_HEIGHT, BASKET_WIDTH, BASKET_HEIGHT, 0, basketImage1));
        baskets.add(new Basket(getWidth() * 3 / 4 - BASKET_WIDTH / 2, getHeight() - 50 - BASKET_HEIGHT, BASKET_WIDTH, BASKET_HEIGHT, 0, basketImage2));
    }
    
    private void initializeBarriers(int barrierNum) {
        barriers.clear();
        ImageIcon barrierImage1 = new ImageIcon("img/Barrier/bomb.png"); // Adjust the image path
        ImageIcon barrierImage2 = new ImageIcon("img/Barrier/poison.png"); // Adjust the image path
        for (int i = 0; i < barrierNum; i++) {
            int x = rand.nextInt(getWidth() - OBJECT_WIDTH);
            int y = -rand.nextInt(300);
            barriers.add(new Barrier(x, y, OBJECT_WIDTH, OBJECT_HEIGHT, barrierImage1));
        
            // Generate a different position for the second type of barrier
            int x2 = rand.nextInt(getWidth() - OBJECT_WIDTH);
            int y2 = -rand.nextInt(300);
            barriers.add(new Barrier(x2, y2, OBJECT_WIDTH, OBJECT_HEIGHT, barrierImage2));
        }
        
    }

    private void initializeCoins(double coinNum) {
        coins.clear(); // Clear the previous coins
        ImageIcon coinImage1 = new ImageIcon("img/Fruits/kiwi.png"); // Adjust the image path
        ImageIcon coinImage2 = new ImageIcon("img/Fruits/banana.png");
        ImageIcon coinImage3 = new ImageIcon("img/Fruits/strawberry.png"); // Adjust the image path
        ImageIcon coinImage4 = new ImageIcon("img/Fruits/watermelon.png");
        ImageIcon coinImage5 = new ImageIcon("img/Fruits/apple.png"); // Adjust the image path\

        for (int i = 0; i < coinNum; i++) {
            int x = rand.nextInt(getWidth() - OBJECT_WIDTH);
            int y = -rand.nextInt(300);
            coins.add(new Coin(x, y, OBJECT_WIDTH, OBJECT_HEIGHT, coinImage1));
    
            // Generate a different position for the second type of coin
            int x2 = rand.nextInt(getWidth() - OBJECT_WIDTH);
            int y2 = -rand.nextInt(300);
            coins.add(new Coin(x2, y2, OBJECT_WIDTH, OBJECT_HEIGHT, coinImage2));

             // Generate a different position for the second type of coin
            int x3 = rand.nextInt(getWidth() - OBJECT_WIDTH);
            int y3 = -rand.nextInt(300);
            coins.add(new Coin(x2, y2, OBJECT_WIDTH, OBJECT_HEIGHT, coinImage3));

             // Generate a different position for the second type of coin
            int x4 = rand.nextInt(getWidth() - OBJECT_WIDTH);
            int y4 = -rand.nextInt(300);
            coins.add(new Coin(x2, y2, OBJECT_WIDTH, OBJECT_HEIGHT, coinImage4));

             // Generate a different position for the second type of coin
            int x5 = rand.nextInt(getWidth() - OBJECT_WIDTH);
            int y5 = -rand.nextInt(300);
            coins.add(new Coin(x2, y2, OBJECT_WIDTH, OBJECT_HEIGHT, coinImage5));
        }
    }
     

    private void initializeScores() {
        scores.add(0);
        scores.add(0);
    }

    private void initializeLives() {
        lives.add(5);
        lives.add(5);
    }

    private void startGame() {
        removeGameStartButton();
        
        gameStarted = true;
        startGameLoop();
        setNewStage(0);
        startCountdownTimer();

        canvas.requestFocusInWindow(); // Request focus for the Canvas
    }
    

    private void startGameLoop() {
        new Thread(() -> {
            while (gameStarted) {
                update();
                canvas.repaint(); // Trigger repaint
                try {
                    Thread.sleep(16); // Adjust the sleep duration as needed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    

    private void setNewStage(int newStage) {
        stage = newStage;

        switch (stage) {
            case 0:
                setNewStage(1);
                startNewStage();
            case 1:
                initializeLives();
                barrierNum = 3;
                barrierSpeed = 3;
                break;
            case 2:
                barrierNum = 4;
                barrierSpeed += 0.5;
                break;
            case 3:
                barrierNum = 5;
                barrierSpeed += 0.5;
                break;
            case 4:
                barrierNum = 6;
                barrierSpeed += 0.5;
                break;
            case 5:
                barrierNum = 7;
                barrierSpeed += 0.5;
                break;
            case 100:
                barrierNum = 100;
            default:
                break;
        }
    }

    private void startNewStage() {
        elapsedTime = 0;
    
        initializeBarriers(barrierNum);
        initializeCoins(coinNum);

        barriers.iterator();
        coins.iterator();
    
        stageStartTime = System.currentTimeMillis(); // Set the start time for the current stage
    
        // Start the countdown timer for the new stage
        startCountdownTimer();
    }
    

    private void startCountdownTimer() {
        Timer countdownTimer = new Timer(1000, e -> {
            elapsedTime = System.currentTimeMillis() - stageStartTime;
            long remainingTime = Math.max(0, TIME_LIMIT_PER_STAGE - elapsedTime) / 1000;
            timerLabel.setText("Time: " + remainingTime + "s");
            
        });
        countdownTimer.start();

        if ( elapsedTime > TIME_LIMIT_PER_STAGE ){
            stopCountdownTimer();
            endStage();
        }
    }

    private void stopCountdownTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }
    
    private void endStage() {
        showStageOverPopup(stage);
    }

    private void showGameStartPage(){
        drawGameStart(getGraphics());
        add(canvas, BorderLayout.CENTER);
        //drawGameStartButton();
        //drawHowToButton();
        //drawButtons();
    }

    private void howTo(int stage) {
        JOptionPane.showMessageDialog(
                    this, "How To Play\n" +
                            "1. Player 1 controls: A (left) and D (right)\n" +
                            "2. Player 2 controls: Left arrow (left) and Right arrow (right)\n" +
                            "3. Players earn points by getting Coins\n" +
                            "4. Colliding with barriers will cause lives to decrease\n" +
                            "5. In each stage, the goal is to reach the stage number * 100 of the score", "How to Play", JOptionPane.INFORMATION_MESSAGE);
        startGame();
    }

    private void showStageOverPopup(int stage) {
        JOptionPane.showMessageDialog(this, "Stage " + stage + " Over", "Stage Complete", JOptionPane.INFORMATION_MESSAGE);
        stage ++;
        showNewStagePopup(stage);
    }

    private void showNewStagePopup(int stage) {
        JOptionPane.showMessageDialog(this, "Stage " + stage + " Start!", "Stage Complete", JOptionPane.INFORMATION_MESSAGE);
        // Start the new stage
        setNewStage(stage);
        startNewStage();
    }
    

    private void showGameOverPopup() {


        if (stage != 5) {
            if (lives.get(0)>lives.get(1)){
            winner = 1;
            } else if (lives.get(0) < lives.get(1)){
                winner = 2;
            }
        } else {
            if (scores.get(0)>scores.get(1)){
                winner = 1;
            } else { winner = 2; }
        }

        JOptionPane.showMessageDialog(this, 
        "Game Over!\n"
        + "The Winner is...\n" + "Player " + winner + "\n"
        + "The Final score is...\n" 
        + "Player 1 :" + scores.get(0) + "\n"
        + "Player 2 :" + scores.get(1) + "\n", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }  

    private void update() {
        for (int player = 0; player < baskets.size(); player++) {
            Basket basket = baskets.get(player);
    
            // Update basket position
            basket.x += basket.speed;
    
            // Ensure the basket stays within the frame
            if (basket.x < 0) {
                basket.x = 0;
            } else if (basket.x > getWidth() - BASKET_WIDTH) {
                basket.x = getWidth() - BASKET_WIDTH;
            }
    
            // Update coin positions
            for (Rectangle coin : coins) {
                coin.setLocation((int) coin.getX(), (int) coin.getY() + coinSpeed);
    
                // Check for collisions with the basket
                if (coin.intersects(basket)) {
                    scores.set(player, scores.get(player) + 10);
    
                    // Reset the coin position
                    int x = rand.nextInt(getWidth() - OBJECT_WIDTH);
                    int y = -rand.nextInt(300);
                    coin.setLocation(x, y);
                }
    
                // Check if coin has reached the bottom, reset its position
                if (coin.getY() > getHeight()) {
                    int x = rand.nextInt(getWidth() - OBJECT_WIDTH);
                    int y = -rand.nextInt(300);
                    coin.setLocation(x, y);
                }
            }
    
            // Update barrier positions
            for (Rectangle barrier : barriers) {
                barrier.setLocation((int) barrier.getX(), (int) barrier.getY() + barrierSpeed);
    
                // Check for collisions with the basket
                if (barrier.intersects(basket)) {
                    lives.set(player, lives.get(player) - 1);
    
                    // Reset the barrier position
                    int x = rand.nextInt(getWidth() - OBJECT_WIDTH);
                    int y = -rand.nextInt(300);
                    barrier.setLocation(x, y);
                }
    
                // Check if barrier has reached the bottom, reset its position
                if (barrier.getY() > getHeight()) {
                    int x = rand.nextInt(getWidth() - OBJECT_WIDTH);
                    int y = -rand.nextInt(300);
                    barrier.setLocation(x, y);
                }
            }
        }
    
        // Check game conditions
        checkGameConditions();
    }
    
    
    
    
    
    
    
    
    private void checkGameConditions() {
        if (lives.get(0) == 0 || lives.get(1) == 0 ){
            stopCountdownTimer();
            showGameOverPopup();
        } else {
            if (gameStarted) {
                if (elapsedTime > TIME_LIMIT_PER_STAGE && !stageOverPopupShown) {
                    stopCountdownTimer();
                    showStageOverPopup(stage);
                    stageOverPopupShown = true;
                    elapsedTime = 0; // Set the flag to true to avoid showing the popup multiple times
                }
            }
        }
    }
    

    private void render(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        drawOcean(g);
        drawPlayers(g);
    
        // Draw barriers and coins
        List<List<? extends Shape>> objectsList = new ArrayList<>();
        objectsList.add(barriers);
        objectsList.add(coins);  // Add all coin lists
        drawObjects(g, objectsList);
    
        drawPlayerInfo(g);
    }
    
    

    private void drawGameStart(Graphics g) {
        drawOcean(g);
        ImageIcon gameTitleIcon = new ImageIcon("img/Falling Fruits.png");
        Image gameTitle = gameTitleIcon.getImage();
    
        g.drawImage(gameTitle, 160, 100, 520, 180, this);
        drawGameStartButton();
    }

    private void removeGameStartButton() {
        removeButton("GAME START");
    }

    private void removeButton(String buttonText) {
        Component[] components = getContentPane().getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getText().equals(buttonText)) {
                    remove(component);
                }
            }
        }
    }
    

    private void drawGameStartButton() {
        int centerX = getX()/2 + 150;
        JButton gameStartButton = createImageButton("img/GAME START.png", centerX, 300, 150, 50);
    
        // Add action listeners to the buttons
        gameStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle the Game Start button click
                howTo(stage);
                 // Pass the graphics context to the startGameLoop method
            }
        });
    
        // Add buttons directly to the frame
        add(gameStartButton);
    }

    
    private JButton createImageButton(String imagePath, int x, int y, int width, int height) {
        ImageIcon buttonIcon = new ImageIcon(imagePath);
        JButton button = new JButton(buttonIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBounds(x, y, width, height);
        return button;
    }

    private void drawOcean(Graphics g) {
        // Load your ocean image
        ImageIcon oceanImageIcon = new ImageIcon("img/pixel-cityscape-8-bit-pixel-art-game-landscape-vector.jpg");
        Image oceanImage = oceanImageIcon.getImage();

        // Draw the ocean image as the background
        g.drawImage(oceanImage, 0, 0, getWidth(), getHeight(), this);
    }

    private void drawPlayers(Graphics g) {
        for (Basket basket : baskets) {
            g.drawImage(basket.getBasketImage().getImage(), (int) basket.getX(), (int) basket.getY(), BASKET_WIDTH, BASKET_HEIGHT, null);
        }
    }    

    private void drawObjects(Graphics g, List<List<? extends Shape>> objectsList) {
        Graphics2D g2d = (Graphics2D) g;
    
        for (List<? extends Shape> objects : objectsList) {
            for (Shape object : objects) {
                if (object instanceof Barrier) {
                    Barrier barrier = (Barrier) object;
                    drawImageOrBox(g2d, barrier.getBarrierImage(), (int) barrier.getX(), (int) barrier.getY());
                } else if (object instanceof Coin) {
                    Coin coin = (Coin) object;
                    drawImageOrBox(g2d, coin.getCoinImage(), (int) coin.getX(), (int) coin.getY());
                }
                // Add any other object types you may have in the future
            }
        }
    }    
    
    private void drawImageOrBox(Graphics2D g2d, ImageIcon imageIcon, int x, int y) {
        if (imageIcon != null) {
            g2d.drawImage(imageIcon.getImage(), x, y, OBJECT_WIDTH, OBJECT_HEIGHT, null);
        } else {
            // Draw a colored box if the image cannot be imported
            g2d.setColor(Color.YELLOW);  // You can change the color if needed
            g2d.fillRect(x, y, OBJECT_WIDTH, OBJECT_HEIGHT);
        }
    }
    
       
    private void drawPlayerInfo (Graphics g) {
        Font playerInfoFont = new Font("Arial", Font.PLAIN, 16);
        g.setFont(playerInfoFont);
        g.setColor(Color.BLACK);

        g.drawString("----------\n   PLAYER 1   \n----------\n", 20, 20);
        g.drawString("Score: " + scores.get(0), 20, 60);
        g.drawString("Lives: " + lives.get(0), 20, 80);

        g.drawString("----------\n   PLAYER 2   \n----------\n", 580, 20);
        g.drawString("Score: " + scores.get(1), 580, 60);
        g.drawString("Lives: " + lives.get(1), 580, 80);

        g.drawString("Stage: " + stage, 340, 20);
        g.drawString("Time Left: " + (30000 - elapsedTime) / 1000, 320, 40);
    }

    private void handleKeyPress(int keyCode) {
        if (!gameStarted) {
            if (keyCode == KeyEvent.VK_SPACE) {
                startGame();
            }
        } else {
            // Handle other key presses during the game
            switch (keyCode) {
                case KeyEvent.VK_A:
                    baskets.get(0).speed = -5; // Player 1 moves left (A key)
                    break;
                case KeyEvent.VK_D:
                    baskets.get(0).speed = 5; // Player 1 moves right (D key)
                    break;
                case KeyEvent.VK_LEFT:
                    baskets.get(1).speed = -5; // Player 2 moves left (Left arrow key)
                    break;
                case KeyEvent.VK_RIGHT:
                    baskets.get(1).speed = 5; // Player 2 moves right (Right arrow key)
                    break;
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;
            }
        }
    }
    

    private void handleKeyRelease(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
                baskets.get(0).speed = 0;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
                baskets.get(1).speed = 0;
                break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FallingFruitGame game = new FallingFruitGame();
            game.startGameLoop(); // Start the game loop after initializing the game
        });
    }

    private static class Basket extends Rectangle {
        private ImageIcon basketImage;
        int speed;
    
        public Basket(int x, int y, int width, int height, int speed, ImageIcon basketImage) {
            super(x, y, width, height);
            this.speed = speed;
            this.basketImage = basketImage;
        }
    
        public ImageIcon getBasketImage() {
            return basketImage;
        }
    }

    public class Barrier extends Rectangle {
        private ImageIcon barrierImage;
    
        public Barrier(int x, int y, int width, int height, ImageIcon barrierImage) {
            super(x, y, width, height);
            this.barrierImage = barrierImage;
        }
    
        public ImageIcon getBarrierImage() {
            return barrierImage;
        }
    }
    public class Coin extends Rectangle {
        private ImageIcon coinImage;
    
        public Coin(int x, int y, int width, int height, ImageIcon coinImage) {
            super(x, y, width, height);
            this.coinImage = coinImage;
        }
    
        public ImageIcon getCoinImage() {
            return coinImage;
        }
    }
    

    private class Canvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!gameStarted) {
                showGameStartPage();
            } else {
                render(g);
            }
        }
    }
}