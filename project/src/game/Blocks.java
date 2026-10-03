package game;

import java.awt.Color;
import java.awt.Graphics2D;

public class Blocks {

    public int[][] shape;
    public Color color;

    public int xGrid;
    public int yGrid;

    public double xPix;
    public double yPix;
    public double speed = 0.25;

    public Blocks(int type)
    {
        this.xGrid = 8;
        this.yGrid = 2;
        setShape(type);
    }

    public void setShape(int m) {
        switch(m) {
            case 1: // I-piece
                shape = new int[][] {{0,0,0,0}, {1,1,1,1}, {0,0,0,0}, {0,0,0,0}};
                color = Color.BLUE;
                break;
            case 2: // 2x2 square piece
                shape = new int[][] {{1,1}, {1,1}};
                color = Color.RED;
                break;
            case 3: // shorter T piece
                shape = new int[][] {{0,1,0}, {1,1,1}, {0,0,0}};
                color = Color.GREEN;
                break;
            case 4: // L piece
                shape = new int[][] {{0,0,1}, {1,1,1}, {0,0,0}};
                color = Color.MAGENTA;
                break;
            case 5: // 3x3 square piece
                shape = new int[][] {{1,1,1}, {1,1,1}, {1,1,1}};
                color = new Color(255, 105, 180);
                break;
            case 6: // plus piece
                shape = new int[][] {{0,1,0}, {1,1,1}, {0,1,0}};
                color = Color.ORANGE;
                break;
            case 7: // longer T piece
                shape = new int[][] {{1,1,1}, {0,1,0},{0,1,0}};
                color = Color.YELLOW;
                break;
            case 8: // stair piece
                shape = new int[][] {{0,0,1}, {0,1,0},{1,0,0}};
                color = Color.CYAN;
                break;
            case 9: // alternate L piece
                shape = new int[][] {{1,0,0}, {1,1,1}, {0,0,0}};
                color = new Color(128, 0, 128);
                break;
            case 10: // bracket piece
                shape = new int[][] {{1,0,0}, {1,0,0}, {1,1,1}};
                color = new Color(152, 251, 152);
                break;
            default:
                shape = new int[][]{{1}};
                color = Color.WHITE;
        }
    }

    public void rotate() {
        int l = shape.length;
        int[][]rotated = new int[l][l];
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < l; j++) {
                rotated[j][l-1-i] = shape[i][j];
            }
        }
        shape = rotated;
    }

    public void animate(int size) {
        double tX = xGrid * size;
        double tY = yGrid * size;

        xPix += (tX - xPix) * speed;
        yPix += (tY - yPix) * speed;
    }

    public void draw(Graphics2D gD, Color overrideColor) {
        gD.setColor(overrideColor != null ? overrideColor : color);

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    int screenX = (xGrid + col) * 32;
                    int screenY = (yGrid + row) * 32;

                    gD.fillRect(screenX, screenY, 32, 32);
                    gD.setColor(Color.BLACK);
                    gD.drawRect(screenX, screenY, 32, 32);

                    gD.setColor(overrideColor != null ? overrideColor : color);
                }
            }
        }
    }

}
