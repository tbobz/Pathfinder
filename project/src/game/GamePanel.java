package game;

import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class GamePanel extends JPanel implements Runnable{

    private ArrayList<Integer> bag = new ArrayList<>();
    private ArrayList<int[]> victoryPath = new ArrayList<>();
    private int pathIndex = 0;
    private volatile double charX, charY;
    private final double charSpeed = 4.0;
    private boolean animStart = false;
    public boolean showHelp = false;

    public int[][] mapGrid = new int[20][20];

    public int piecesRemaining;
    public boolean isVictory;
    public boolean isGameOver;
    public boolean heightViolation;

    public int startX, startY;
    public int endX, endY;

    public final int tileSize = 32;


    Thread gameThread;
    int FPS = 60;
    KeyHandler k = new KeyHandler(this);
    Blocks block;

    public GamePanel() {
        this.setPreferredSize(new Dimension(1024, 768));
        this.setBackground(Color.DARK_GRAY);

        this.setPreferredSize(new Dimension(640, 640));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setLayout(new java.awt.BorderLayout());


        this.setFocusable(true);
        this.requestFocusInWindow();

        this.addKeyListener(k);

        resetConfig();

    }

    public void resetConfig() {
        mapGrid = new int[20][20];

        startX = (int) (Math.random() * 3);
        startY = (int) (Math.random() * 19);

        endX = 17 + (int) (Math.random() * 3);
        if (startY <= 10)
            endY = (int) (Math.random() * 9) + 10;
        else if (startY > 10)
            endY = (int) (Math.random() * 10) + 1;

        // 2 = starting point
        mapGrid[startX][startY] = 2;
        // 3 = ending point
        mapGrid[endX][endY] = 3;
        // 6 = character
        mapGrid[startX][startY-1] = 6;

        piecesRemaining = 8;
        isVictory = false;
        isGameOver = false;
        heightViolation = false;

        replenish();
        block = new Blocks(nextBlock());
    }


    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void replenish() {
        for (int i = 1; i <= 10; i++) {
            bag.add(i);
        }
        Collections.shuffle(bag);
    }

    public int nextBlock() {
        if (bag.isEmpty()) {
            replenish();
        }
        return bag.remove(0);
    }

    @Override
    public void run() {
        double drawInterval = (double) 1000000000 / FPS;
        double nextDraw = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();

            java.awt.Toolkit.getDefaultToolkit().sync();

            try {
                double remainingTime = nextDraw - System.nanoTime();
                remainingTime = remainingTime / 1000000;
                if (remainingTime < 0)
                    remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDraw += drawInterval;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean findHeightViolation() {
        for (int col = 0; col < 20; col++) {
            for (int row = 0; row < 20; row++) {
                if (mapGrid[col][row] == 5)
                    return true;
            }

            int numVert = 0;
            for (int row = 0; row < 20; row++) {
                if (mapGrid[col][row] == 1 || mapGrid[col][row] == 2 || mapGrid[col][row] == 3 || mapGrid[col][row] == 4) {
                    numVert++;
                    if (numVert >= 3) {
                        for (int r = 0; r < 20; r++) {
                            if (mapGrid[col][r] == 4)
                                return true;
                        }
                    }
                }
                else
                    numVert = 0;
            }
        }
        return false;
    }

    public boolean inBounds(int[][] b, int targX, int targY) {
        for (int r = 0; r < b.length; r++) {
            for (int c = 0; c < b[0].length; c++) {
                if (b[r][c] == 1) {
                    int mapX = targX + c;
                    int mapY = targY + r;

                    if (mapX < 0 || mapX >= 20 || mapY < 0 || mapY >= 20)
                        return false;
                }
            }
        }
        return true;
    }

    public void update() {

        boolean victoryFinished = isVictory && !animStart;

        if (isGameOver || victoryFinished) {
            if (k.lockPress) { // hitting ENTER allows the player to play another round
                resetConfig();
                k.clearInputs();
            }
            return;
        }

        // calculates the path for the character to take
        if (isVictory && animStart) {
            if (!victoryPath.isEmpty()) {
                int[] targSpot = victoryPath.get(pathIndex);
                double targX = targSpot[0] * 32;
                double targY = targSpot[1] * 32;

                boolean goingUp = targY < charY;
                // character moving animation
                if (goingUp) {
                    if (charY > targY) {
                        charY = Math.max(charY - charSpeed, targY);
                    }
                    else {
                        if (charX < targX) {
                            charX = Math.min(charX + charSpeed, targX);
                        } else if (charX > targX) {
                            charX = Math.max(charX - charSpeed, targX);
                        }
                    }
                }
                else {

                    if (charX < targX) {
                        charX = Math.min(charX + charSpeed, targX);
                    }
                    else if (charX > targX) {
                        charX = Math.max(charX - charSpeed, targX);
                    }
                    else {
                        if (charY < targY) {
                            charY = Math.min(charY + charSpeed, targY);
                        }
                    }
                }

                if (charX == targX && charY == targY) {
                    if (pathIndex < victoryPath.size() - 1)
                        pathIndex++;
                    else {
                        animStart = false; // ends animation
                    }
                }
            }
            return;
        }

        if (isGameOver) {
            return;
        }

        if (block == null)
            return;

        if (k.upPress && inBounds(block.shape, block.xGrid, block.yGrid - 1))
            block.yGrid--;
        if (k.downPress && inBounds(block.shape, block.xGrid, block.yGrid + 1))
            block.yGrid++;
        if (k.leftPress && inBounds(block.shape, block.xGrid - 1, block.yGrid))
            block.xGrid--;
        if (k.rightPress && inBounds(block.shape, block.xGrid + 1, block.yGrid))
            block.xGrid++;
        if (k.rotatePress)
            block.rotate();
        if (k.helpPress)
            showHelp = !showHelp;
        if (k.lockPress) {
            if (isAreaClear()) {
                lockIn();
                piecesRemaining--;

                if (pathExists() && !findHeightViolation()) {
                    isVictory = true;
                    block = null;

                    for (int col = 0; col < 20; col++) {
                        for (int row = 0; row < 20; row++) {
                            if (mapGrid[col][row] == 6) {
                                mapGrid[col][row] = 0;
                            }
                        }
                    }

                    charX = startX * 32;
                    charY = (startY-1) * 32;
                    pathIndex = 0;
                    animStart = true;

                }
                else if (piecesRemaining <= 0 || findHeightViolation()) {
                    isGameOver = true;
                    block = null;
                }
                else {
                    block = new Blocks(nextBlock());
                }
            }
        }
        if (block != null)
            block.animate(32);
        k.clearInputs();

    }

    // checks whether a valid path exists
    public boolean pathExists() {
        victoryPath.clear();

        int currX = startX;
        int currY = startY - 1;

        victoryPath.add(new int[]{currX, currY});

        while (currX < endX) {
            int nextX = currX + 1;
            int nextY = -1;

            for (int r = 0; r < 20; r++) {
                if (mapGrid[nextX][r] == 1 || mapGrid[nextX][r] == 2 || mapGrid[nextX][r] == 3) {
                    nextY = r - 1;
                    break;
                }
            }

            if (nextY == -1 || (currY - nextY) > 2 || (nextY - currY) > 2){
                victoryPath.clear();
                return false;
            }

            // move tracking position
            currY = nextY;
            currX = nextX;
            victoryPath.add(new int[]{currX, currY});
        }
        // victory condition (on top of goal block)
        if (currY == endY - 1)
            return true;
        else {
            victoryPath.clear();
            return false;
        }
    }

    private void lockIn() {
        for (int r = 0; r < block.shape.length; r++) {
            for (int c = 0; c < block.shape[0].length; c++) {
                if (block.shape[r][c] == 1) {
                    int xTarget = block.xGrid + c;
                    int yTarget = block.yGrid + r;

                    // checks that target position is within bounds and not occupied by another block
                    if (xTarget >= 0 && xTarget < 20 && yTarget >= 0 && yTarget < 20) {
                        if (mapGrid[xTarget][yTarget] == 0) {
                            // places block, assigns the target space to 1 (occupied status)
                            mapGrid[xTarget][yTarget] = 1;
                        }
                    }
                }
            }
        }
    }

    private boolean isAreaClear() {
        for (int r = 0; r < block.shape.length; r++) {
            for (int c = 0; c < block.shape[r].length; c++) {
                if (block.shape[r][c] == 1) {
                    int xTarget = block.xGrid + c;
                    int yTarget = block.yGrid + r;

                    if (xTarget < 0 || xTarget >= 20 || yTarget < 0 || yTarget >= 20) {
                        return false;
                    }
                    if (mapGrid[xTarget][yTarget] == 1 || mapGrid[xTarget][yTarget] == 2 || mapGrid[xTarget][yTarget] == 3 || mapGrid[xTarget][yTarget] == 6) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void paintComponent(Graphics a) {
        super.paintComponent(a);
        Graphics2D g2 = (Graphics2D) a;

        int xOffset = (this.getWidth() - 640) / 2;
        int yOffset = (this.getHeight() - 640) / 2;

        for (int col = 0; col < 20; col++) {
            for (int row = 0; row < 20; row++) {
                int xDraw = (col * tileSize) + xOffset;
                int yDraw = (row * tileSize) + yOffset;

                if (mapGrid[col][row] == 1 || mapGrid[col][row] == 4) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillRect(xDraw, yDraw, tileSize, tileSize);
                }
                // sets start point to light blue
                else if (mapGrid[col][row] == 2) {
                    g2.setColor(new Color(120,210,210));
                    g2.fillRect(xDraw, yDraw, tileSize, tileSize);
                }
                // sets end point to yellow
                else if (mapGrid[col][row] == 3) {
                    g2.setColor(new Color(210, 210, 120));
                    g2.fillRect(xDraw, yDraw, tileSize, tileSize);
                }
                // sets character color to white
                else if (mapGrid[col][row] == 6) {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(xDraw, yDraw, tileSize, tileSize);
                }
            }
        }

        // generates grid lines
        g2.setColor(new Color(40,40,40));
        for (int col = 0; col <= 20; col++) {
            int xLine = (col * tileSize) + xOffset;
            g2.drawLine(xLine, yOffset, xLine, yOffset + 640);
        }
        for (int row = 0; row <= 20; row++) {
            int yLine = (row * tileSize) + yOffset;
            g2.drawLine(xOffset, yLine, xOffset + 640, yLine);
        }

        // specifies hud dimensions and constants
        int hudWidth = 200;
        int hudHeight = 50;
        int hudX = xOffset + (640 - hudWidth) / 2;
        int hudY = yOffset - hudHeight - 15;

        // creates hud box
        g2.setColor(Color.BLACK);
        g2.fillRect(hudX, hudY, hudWidth, hudHeight);

        // creates border around hud
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(hudX, hudY, hudWidth, hudHeight);
        g2.setStroke(new java.awt.BasicStroke(1));

        // creates text
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        g2.drawString("Pieces Available", hudX + 15, hudY + 30);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        // accesses number of blocks remaining
        String count = String.valueOf(piecesRemaining);

        // changes color of text when block budget gets low
        if (piecesRemaining <= 4) {
            g2.setColor(new Color(231, 76, 60));
        }
        else {
            g2.setColor(Color.WHITE);
        }

        // places the text in the hud box
        g2.drawString(count, hudX + 160, hudY + 35);

        int hudWidth2 = 320;
        int hudHeight2 = 65;
        int hudX2 = xOffset + (640 - hudWidth2) / 2;
        int hudY2 = yOffset + 640 + 15;

        // creates hud box
        g2.setColor(Color.BLACK);
        g2.fillRect(hudX2, hudY2, hudWidth2, hudHeight2);

        // creates border around hud
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(hudX2, hudY2, hudWidth2, hudHeight2);
        g2.setStroke(new java.awt.BasicStroke(1));

        // creates text
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        java.awt.FontMetrics bm = g2.getFontMetrics();
        String helpText = "Press H for Game Rules / Controls";
        int xText = hudX2 + (hudWidth2 - bm.stringWidth(helpText)) / 2;
        int yText = hudY2 + ((hudHeight2 - bm.getHeight()) / 2) + bm.getAscent();

        g2.drawString(helpText, xText, yText);

        // shows block movement on the screen
        if (block != null) {
            g2.translate(xOffset, yOffset);
            if (isAreaClear()) {
                block.draw(g2, null);
                g2.translate(-xOffset, -yOffset);
            }
            else
                block.draw(g2, new Color(255, 0, 0, 120));
        }


        if (isVictory) {

            // prepares character to move character along path
            g2.setColor(Color.WHITE);
            g2.fillRect((int)charX + xOffset, (int)charY + yOffset, 32, 32);

            // once animation ends, displays victory message, centered
            if (!animStart) {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(xOffset, yOffset, 640, 640);

                g2.setColor(new Color(46, 204, 113));
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 40));
                java.awt.FontMetrics fm = g2.getFontMetrics();

                String msg = "Congratulations! You won!";
                String subMsg = "Press ENTER to Play Again";
                int txtX = xOffset + (640 - fm.stringWidth(msg)) / 2;
                int txtY = yOffset + (640 / 2);
                g2.drawString(msg, txtX, txtY);
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
                g2.drawString(subMsg, txtX, txtY + 50);
            }
        }

        if (isGameOver) {
            // displays defeat message, centered
            g2.setColor(new Color(0,0,0,150));
            g2.fillRect(xOffset, yOffset, 640, 640);

            g2.setColor(new Color(231,76,60));
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 40));
            java.awt.FontMetrics fm2 = g2.getFontMetrics();

            String msg2 = "Game Over!";
            String subMsg2 = "Press ENTER to Play Again";
            int txtX2 = xOffset + (640 - fm2.stringWidth(msg2)) / 2;
            int txtY2 = yOffset + (640 / 2);
            g2.drawString(msg2, txtX2, txtY2);
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
            g2.drawString(subMsg2, txtX2, txtY2 + 50);
        }

        if (showHelp) {
            g2.setColor(new Color(0,0,0,150));
            g2.fillRect(xOffset, yOffset, 640, 640);

            int menuW = 440;
            int menuH = 300;
            int menuX = xOffset + (640 - menuW) / 2;
            int menuY = yOffset + (640 - menuH) / 2;

            g2.setColor(Color.BLACK);
            g2.fillRect(menuX, menuY, menuW, menuH);

            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawRect(menuX, menuY, menuW, menuH);
            g2.setStroke(new java.awt.BasicStroke(1)); // reset stroke config

            // 3. Draw Menu Header Text
            g2.setColor(new Color(52, 152, 219));
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
            g2.drawString("GAME CONTROLS & RULES", menuX + 45, menuY + 50);

            // 4. Draw Rules Bullet Points
            g2.setColor(Color.WHITE);
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));

            int startY = menuY + 100;
            int lineSpacing = 30;

            g2.drawString("• ARROW KEYS / WASD : Move block around grid", menuX + 30, startY);
            g2.drawString("• SPACEBAR / R  : Rotate the block ", menuX + 30, startY + lineSpacing);
            g2.drawString("• ENTER      : Lock block in place", menuX + 30, startY + (lineSpacing * 2));
            g2.drawString("• OBJECTIVE  : Connect start point to end point", menuX + 30, startY + (lineSpacing * 3));
            g2.drawString("• RESTRICTIONS  : Path height cannot exceed 2 blocks", menuX + 30, startY + (lineSpacing * 4));

            // tells user how to get back to playing
            g2.setColor(Color.LIGHT_GRAY);
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 14));
            g2.drawString("Press 'H' again to close this menu and resume", menuX + 70, menuY + 265);
        }
        // removes g2 variable
        g2.dispose();
    }
}