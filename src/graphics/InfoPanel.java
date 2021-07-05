package graphics;

import javax.swing.*;
import java.awt.*;

import control.drone.Bot;
import control.drone.AI;

public class InfoPanel extends JPanel {
    JLabel posX;
    JLabel posY;
    JLabel energia;
    JLabel pontuacao;

    JLabel stateText;
    JLabel processTime;
    JLabel acaoAtual;
    JLabel ping;

    Bot bot;
    AI ai;

    public InfoPanel(Bot bot) {
        this.bot = bot;
        this.ai = bot.ai;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        posX = new JLabel();
        posX.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(posX);

        posY = new JLabel();
        posY.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(posY);

        energia = new JLabel();
        energia.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(energia);

        pontuacao = new JLabel();
        pontuacao.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(pontuacao);

        stateText = new JLabel();
        stateText.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(stateText);

        acaoAtual = new JLabel();
        acaoAtual.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(acaoAtual);

        processTime = new JLabel();
        processTime.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(processTime);

        ping = new JLabel();
        ping.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(ping);

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        posX.setText("X: " + this.bot.getX());
        posY.setText("Y: " + this.bot.getY());
        energia.setText("E: " + this.bot.getEnergy());
        pontuacao.setText("P: " + this.bot.score);
        stateText.setText("S: " + this.ai.estadoAtual);
        processTime.setText("tt: " + this.bot.thinkingTime);
        acaoAtual.setText("acao: " + this.ai.acaoAtual);
        ping.setText("ping: " + Bot.ping);

    }


}
