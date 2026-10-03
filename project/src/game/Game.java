package game;

public class Game {

    public static void main(String[] args) {

        GamePanel p = new GamePanel();
        new GameWindow(p);
        p.requestFocusInWindow();
        p.startGameThread();
    }
}


