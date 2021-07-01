package graphics;


import control.Bot;
import control.Field;
import control.enums.Position;

import javax.swing.*;
import java.awt.*;

class GamePanel extends JPanel {
    public final int TAM = 30;
    public final int CENTRO_X = 270 / 2;
    public final int CENTRO_Y = 270 / 2;
    Bot bot;

    public GamePanel(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // desenhando o campo :
        // os quadrados serao pintados a partir da coordenada do drone
        // indo de x-4 até x+4, e y-4 até y+4
        int xDrone = this.bot.x;
        int yDrone = this.bot.y;
        int x, y;
        Position pos;

        for (int i = 0; i < 81; i ++) {
            x = xDrone + (i % 9) - 4;
            y = yDrone + (i / 9) - 4;
            pos = Field.get(x, y);
            switch (pos) {
                case DANGER -> g.setColor(Color.RED);
                case PAREDE -> g.setColor(Color.BLACK);
                case UNKNOWN -> g.setColor(Color.GRAY);
                case SAFE -> g.setColor(Color.GREEN);
                case OURO -> g.setColor(Color.YELLOW);
                case POWERUP -> g.setColor(Color.CYAN);
                case EMPTY -> g.setColor(Color.WHITE);
            }
            g.fillRect((i % 9) * TAM, (i / 9) * TAM, TAM, TAM);
            g.setColor(Color.BLACK);
            g.drawRect((i % 9) * TAM, (i / 9) * TAM, TAM, TAM);
        }
        // pintando o meu bot

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.BLACK);
        switch (this.bot.dir) {
            case north -> g2d.drawLine(CENTRO_X, CENTRO_Y, CENTRO_X, CENTRO_Y - TAM/2);
            case east -> g2d.drawLine(CENTRO_X, CENTRO_Y, CENTRO_X + TAM/2, CENTRO_Y);
            case south -> g2d.drawLine(CENTRO_X, CENTRO_Y, CENTRO_X, CENTRO_Y  + TAM/2);
            case west -> g2d.drawLine(CENTRO_X, CENTRO_Y, CENTRO_X - TAM/2, CENTRO_Y);
        }

    }
}