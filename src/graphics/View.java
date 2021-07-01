package graphics;

import javax.swing.*;
import java.awt.*;

import control.Bot;
import control.Config;


public class View extends JFrame {
    public final int COMPRIMENTO = 270 + 30 + 100;
    public final int ALTURA = 270 + 40;


    JPanel gamePainel;
    JPanel infoPainel;
    Bot bot;
    public View(Bot bot) {
        this.bot = bot;

        setBounds(0, 0, COMPRIMENTO, ALTURA);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(Config.nomeJogador);
        getContentPane().setLayout(null);
        this.gamePainel = new GamePanel(this.bot);
        this.gamePainel.setBounds(0, 0, 270, 270);
        getContentPane().add(this.gamePainel);

        this.infoPainel = new InfoPanel(this.bot);
        this.infoPainel.setBounds(270, 0, 100, 270 + 40);
        getContentPane().add(this.infoPainel);

        setVisible(true);
    }
}
