package graphics;


import INF1771_GameClient.Dto.PlayerInfo;
import control.drone.Bot;
import control.enums.Action;
import control.map.Field;
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
        try {
            // desenhando o campo :
            // os quadrados serao pintados a partir da coordenada do drone
            // indo de x-4 até x+4, e y-4 até y+4
            int xDrone = this.bot.x;
            int yDrone = this.bot.y;
            int x, y;
            Position pos;

            for (int i = 0; i < 81; i++) {
                x = xDrone + (i % 9) - 4;
                y = yDrone + (i / 9) - 4;
                pos = Field.get(x, y);
                switch (pos) {
                    case PERIGO -> g.setColor(Color.RED);
                    case PAREDE -> g.setColor(Color.BLACK);
                    case DESCONHECIDO -> g.setColor(Color.GRAY);
                    case SEGURO -> g.setColor(Color.GREEN);
                    case OURO -> g.setColor(Color.YELLOW);
                    case POWERUP -> g.setColor(Color.CYAN);
                    case VAZIO -> g.setColor(Color.WHITE);
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
                case north -> g2d.drawLine(CENTRO_X, CENTRO_Y, CENTRO_X, CENTRO_Y - TAM / 2);
                case east -> g2d.drawLine(CENTRO_X, CENTRO_Y, CENTRO_X + TAM / 2, CENTRO_Y);
                case south -> g2d.drawLine(CENTRO_X, CENTRO_Y, CENTRO_X, CENTRO_Y + TAM / 2);
                case west -> g2d.drawLine(CENTRO_X, CENTRO_Y, CENTRO_X - TAM / 2, CENTRO_Y);
            }

            // pintando o spawn
            x = Field.xSpawn - xDrone + 4;
            y = Field.ySpawn - yDrone + 4;
            g2d.setColor(Color.blue);
            g2d.drawRect(x * TAM, y * TAM, TAM, TAM);

            if (this.bot.ai.pathAtual != null) {
                // pintando path
                int bx = xDrone;
                int by = yDrone;
                PlayerInfo.Direction bdir = this.bot.dir;
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(4));
                for (Action a: this.bot.ai.pathAtual.acoes) {
                    switch (a) {
                        case ESQUERDA -> {
                            switch (bdir) {
                                case north -> bdir = PlayerInfo.Direction.west;
                                case south -> bdir = PlayerInfo.Direction.east;
                                case east -> bdir = PlayerInfo.Direction.north;
                                case west -> bdir = PlayerInfo.Direction.south;
                            }
                        }
                        case DIREITA -> {
                            switch (bdir) {
                                case north -> bdir = PlayerInfo.Direction.east;
                                case south -> bdir = PlayerInfo.Direction.west;
                                case east -> bdir = PlayerInfo.Direction.south;
                                case west -> bdir = PlayerInfo.Direction.north;
                            }
                        }
                        case FRENTE -> {
                            switch (bdir) {
                                case north -> {
                                    g2d.drawLine((bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2,
                                            (bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4 - 1)*TAM + TAM/2);
                                    by -= 1;
                                }
                                case south -> {
                                    g2d.drawLine((bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2,
                                            (bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4 + 1)*TAM + TAM/2);
                                    by += 1;
                                }
                                case east -> {
                                    g2d.drawLine((bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2,
                                            (bx - xDrone + 4 + 1)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2);
                                    bx += 1;
                                }
                                case west -> {
                                    g2d.drawLine((bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2,
                                            (bx - xDrone + 4 - 1)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2);
                                    bx -= 1;
                                }
                            }
                        }
                        case TRAS -> {
                            switch (bdir) {
                                case south -> {
                                    g2d.drawLine((bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2,
                                            (bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4 - 1)*TAM + TAM/2);
                                    by -= 1;
                                }
                                case north -> {
                                    g2d.drawLine((bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2,
                                            (bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4 + 1)*TAM + TAM/2);
                                    by += 1;
                                }
                                case west -> {
                                    g2d.drawLine((bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2,
                                            (bx - xDrone + 4 + 1)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2);
                                    bx += 1;
                                }
                                case east -> {
                                    g2d.drawLine((bx - xDrone + 4)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2,
                                            (bx - xDrone + 4 - 1)*TAM + TAM / 2, (by - yDrone + 4)*TAM + TAM/2);
                                    bx -= 1;
                                }
                            }
                        }
                    }
                }
                // pintando o ultimo quadrado
                g2d.setColor(Color.MAGENTA);
                g2d.fillRect((bx - xDrone + 4)*TAM + 5, (by - yDrone + 4)*TAM + 5,TAM - 10,TAM - 10);
            }
        }
        catch (NullPointerException ignored) {}
    }
}