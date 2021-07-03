package graphics;

import javax.swing.*;
import java.awt.*;

import control.drone.Bot;
import control.drone.AI;
import control.map.Field;

public class InfoPanel extends JPanel {
    public final int COMPRIMENTO = 100;
    public final int ALTURA = 270 + 40;

    JLabel posX;
    JLabel posY;
    JLabel energia;
    JLabel pontuacao;

    JLabel stateText;
    JLabel ping;
    JLabel acaoAtual;
    JLabel qtdSafe;

    JLabel isInimigo;
    JLabel isBuraco;
    JLabel isFlash;
    JLabel isPowerup;
    JLabel isTesouro;
    JLabel isParede;
    JLabel isAcerto;
    JLabel isDano;
    JLabel isInimigoFrente;

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

        ping = new JLabel();
        ping.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(ping);


        acaoAtual = new JLabel();
        acaoAtual.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(acaoAtual);


        qtdSafe = new JLabel();
        qtdSafe.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(qtdSafe);

        isInimigo = new JLabel();
        isInimigo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isInimigo);

        isBuraco = new JLabel();
        isBuraco.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isBuraco);

        isFlash = new JLabel();
        isFlash.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isFlash);

        isPowerup = new JLabel();
        isPowerup.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isPowerup);

        isTesouro = new JLabel();
        isTesouro.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isTesouro);

        isParede = new JLabel();
        isParede.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isParede);

        isAcerto = new JLabel();
        isAcerto.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isAcerto);

        isDano = new JLabel();
        isDano.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isDano);

        isInimigoFrente = new JLabel();
        isInimigoFrente.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(isInimigoFrente);

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        posX.setText("X: " + this.bot.getX());
        posY.setText("Y: " + this.bot.getY());
        energia.setText("E: " + this.bot.getEnergy());
        stateText.setText("S: " + this.ai.estadoAtual);
        ping.setText("ms: " + this.bot.ping);
        acaoAtual.setText("acao: " + this.ai.acaoAtual);
        qtdSafe.setText("safes: " + Field.getSizeSafe());

        isInimigo.setText("isInimigo: " + this.bot.ultimaObservacao.isInimigo);
        isBuraco.setText("isBuraco: " + this.bot.ultimaObservacao.isBuraco);
        isFlash.setText("isFlash: " + this.bot.ultimaObservacao.isFlash);
        isPowerup.setText("isPowerup: " + this.bot.ultimaObservacao.isPowerup);
        isInimigo.setText("isInimigo: " + this.bot.ultimaObservacao.isTesouro);
        isParede.setText("isParede: " + this.bot.ultimaObservacao.isParede);
        isAcerto.setText("isAcerto: " + this.bot.ultimaObservacao.isAcerto);
        isDano.setText("isDano: " + this.bot.ultimaObservacao.isDano);
        isInimigoFrente.setText("isFrente: " + this.bot.ultimaObservacao.isInimigoFrente);

    }


}
