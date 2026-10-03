package game;

import javax.swing.JFrame;

public class GameWindow extends JFrame {
    public GameWindow(GamePanel panel) {
        this.setTitle("Pathfinder");
        this.setSize(900, 900);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.add(panel);
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        panel.addKeyListener(new KeyHandler(panel));

        this.setLocationRelativeTo(null);

        this.setVisible(true);
    }
}
