package control;

import java.awt.*;

public class Config {

    public static final String nomeJogador = "Leinadium";
    public static final String url = "atari.icad.puc-rio.br";
    public static final int timerLento = 1000;
    public static final int timerNormal = 200;
    public static final int timerMinimo = 200;
    public static final Color corDefault = new Color(23, 179, 132);
    public static final int tempoSpawn = 15000 + 3 * timerNormal;

    public static Color convertFromString(String s) {
        String[] p = s.split("(,)|(])");

        int A = Integer.parseInt(p[0].substring(p[0].indexOf('=') + 1));
        int R = Integer.parseInt(p[1].substring(p[1].indexOf('=') + 1));
        int G = Integer.parseInt(p[2].substring(p[2].indexOf('=') + 1));
        int B = Integer.parseInt(p[3].substring(p[3].indexOf('=') + 1));

        return new Color(R, G, B, A);
    }

    /**
     * Calcula quantos ticks para renascer o objeto
     * @param tickNow tick atual do jogo
     * @param tickOld tick da ultima coleta do objeto
     * @return a quantidade de ticks para nascer
     */
    public static int ticksToBorn(int tickNow, int tickOld) {
        return Config.tempoSpawn / Config.timerNormal - (tickNow - tickOld);
    }

    private Config() {}


}
