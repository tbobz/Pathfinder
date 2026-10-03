package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyHandler extends KeyAdapter {  //extends key adapter or implements key listener

    public boolean upPress, downPress, leftPress, rightPress, rotatePress, lockPress, helpPress;

    private GamePanel g;

    public KeyHandler(GamePanel g) {
        this.g = g;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_H) {
            if (!helpPress) {
                g.showHelp = !g.showHelp;
                g.repaint();
            }
            helpPress = true;
        }
        if (!g.showHelp) {
            if ((key == KeyEvent.VK_LEFT) || (key == KeyEvent.VK_A)) {
                leftPress = true;
            }
            if ((key == KeyEvent.VK_RIGHT) || (key == KeyEvent.VK_D)) {
                rightPress = true;
            }
            if ((key == KeyEvent.VK_UP) || (key == KeyEvent.VK_W)) {
                upPress = true;
            }
            if ((key == KeyEvent.VK_DOWN) || (key == KeyEvent.VK_S)) {
                downPress = true;
            }
            if ((key == KeyEvent.VK_R) || (key == KeyEvent.VK_SPACE)) {
                rotatePress = true;
            }
            if (key == KeyEvent.VK_ENTER) {
                lockPress = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_H) {
            helpPress = false;
        }
    }

    public void clearInputs() {
        lockPress = false;
        leftPress = false;
        rightPress = false;
        upPress = false;
        downPress = false;
        rotatePress = false;
        helpPress = false;
    }
}
