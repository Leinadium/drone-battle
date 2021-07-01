package graphics;

import javax.swing.*;
import java.awt.*;

import control.Bot;
import control.AI;
import control.Config;

public class InfoPanel extends JPanel {
    public final int COMPRIMENTO = 100;
    public final int ALTURA = 270 + 40;

    JLabel posX;
    JLabel posY;
    JLabel energia;
    JLabel pontuacao;

    JLabel stateText;
    JLabel pesoExplorar;
    JLabel pesoAtacar;
    JLabel pesoFugir;
    JLabel pesoRecarregar;
    JLabel pesoColetar;

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

        pesoExplorar = new JLabel();
        pesoExplorar.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(pesoExplorar);

        pesoAtacar = new JLabel();
        pesoAtacar.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(pesoAtacar);

        pesoFugir = new JLabel();
        pesoFugir.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(pesoFugir);

        pesoRecarregar = new JLabel();
        pesoRecarregar.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(pesoRecarregar);

        pesoColetar = new JLabel();
        pesoColetar.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(pesoColetar);

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        posX.setText("X: " + this.bot.getX());
        posY.setText("Y: " + this.bot.getY());
        energia.setText("E: " + this.bot.getEnergy());
        stateText.setText("S: " + this.ai.estadoAtual);
        pesoExplorar.setText("Exp: " + this.ai.pesoExplorar);
        pesoFugir.setText("Fug: " + this.ai.pesoFugir);
        pesoAtacar.setText("Ata: " + this.ai.pesoAtacar);
        pesoRecarregar.setText("Rec: " + this.ai.pesoRecarregar);
        pesoColetar.setText("Col: " + this.ai.pesoColetar);
    }


}
