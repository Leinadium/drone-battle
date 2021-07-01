package control;

import java.util.HashMap;
import java.util.Objects;
import java.lang.Math.*;

import INF1771_GameClient.Dto.PlayerInfo;
import control.enums.Position;

/**
 * Classe para representar o mapa do jogo.
 * Parte estatica: contem as informacoes do mapa do jogo.
 *  Eh alterada cada vez que o bot chama alguma funcao instanciada.
 *
 * Parte objeto: contem as informacoes de onde o bot esta, e o que tem ao seu redor
 */
public class Field {

    private static HashMap<String, Position> mapa;
    private static HashMap<String, Integer> posicoesOuro;
    private static HashMap<String, Integer> posicoesPowerup;

    public static final int comprimento = 59;
    public static final int altura = 34;

    public static void init() {
        if (mapa == null) {
            mapa = new HashMap<>();
            posicoesOuro = new HashMap<>();
            posicoesPowerup = new HashMap<>();
        }
    }

    public static void set(int x, int y, Position tipoCasa) {
        String s = x + "-" + y;

        if (tipoCasa == Position.DANGER) {
            if (get(x, y) == Position.UNKNOWN) {
                mapa.put(s, tipoCasa);
            } else { return; }
        }

        if (tipoCasa == Position.SAFE) {
            if (get(x, y) == Position.UNKNOWN || get(x, y) == Position.DANGER) {
                mapa.put(s, tipoCasa);
            } else { return; }
        }
        if (tipoCasa == Position.EMPTY) {
            if (get(x, y) == Position.OURO || get(x, y) == Position.POWERUP) {
                return;
            } else { mapa.put(s, tipoCasa); }
        }
        mapa.put(s, tipoCasa);
    }

    public static Position get(int x, int y) {
        if (x < 0 || y < 0 || x >= Field.comprimento || y >= Field.altura) {
            return Position.PAREDE;
        }

        String s = x + "-" + y;
        Position ret = mapa.get(s);
        // retorna a resposta, ou UNKNOWN se for nulo
        return Objects.requireNonNullElse(ret, Position.UNKNOWN);
    }

    public static void setOuro(int x, int y, int time) {
        String s = x + "-" + y;
        posicoesOuro.put(s, time);
    }

    public static void setPowerup(int x, int y, int time) {
        String s = x + "-" + y;
        posicoesPowerup.put(s, time);
    }

    /**
     * Retorna o que tem em volta daquela posicao.
     * @param x Posicao x
     * @param y Posicao Y
     * @return Um array contendo as informacoes na seguinte ordem:
     *  NORTE, LESTE, SUL, OESTE (norte, seguindo sentido horario).
     */
    public static Position[] aroundPos(int x, int y) {
        Position[] res = new Position[4];
        res[0] = get(x, y - 1);
        res[1] = get(x + 1, y);
        res[2] = get(x, y + 1);
        res[3] = get(x - 1, y);
        return res;
    }

    /**
     * Atualiza os blocos na sua frente e lados com aquela informacao
     * @param x Posicao x
     * @param y Posicao y
     * @param dir Direcao do drone
     * @param tipo Informacao do bloco
     */
    public static void setAround(int x, int y, PlayerInfo.Direction dir, Position tipo) {
        set(x + 1, y, tipo);
        set(x - 1, y, tipo);
        set(x , y + 1, tipo);
        set(x, y - 1, tipo);
    }

    /**
     * Atualiza o bloco a sua frente com aquela informacao
     * @param x Posicao x
     * @param y Posicao Y
     * @param dir Direcao do drone
     * @param tipo Informacao do bloco
     */
    public static void setFront(int x, int y, PlayerInfo.Direction dir, Position tipo) {
        switch (dir) {
            case north -> set(x, y - 1, tipo);
            case east -> set(x + 1, y, tipo);
            case south -> set(x, y + 1, tipo);
            case west -> set(x - 1, y, tipo);
        }
    }

    /**
     * Calcula a distancia media de todas as fontes de ouro.
     * A distancia é calculada sobre a distância manhattan menos o tempo que falta
     * para renascer
     * @param x posicao x do drone
     * @param y posicao y do drone
     * @return um float com o valor calculado
     */
    public static float getAverageDistanceGold(int x, int y) {
        if (posicoesOuro.size() == 0) {
            return 0;
        }
        float soma = 0;
        float tempo;
        int x_ouro, y_ouro;
        String[] ss;
        for (String s: posicoesOuro.keySet()) {
            ss = s.split("-");
            x_ouro = Integer.parseInt(ss[0]);
            y_ouro = Integer.parseInt(ss[1]);
            tempo = posicoesOuro.get(s);
            soma += Math.abs(x_ouro - x) + Math.abs(y_ouro - y) - tempo;
        }
        return soma / posicoesOuro.size();
    }

    /**
     * Retorna a distancia do drone ate todos os ouros
     * @param x posicao x do drone
     * @param y posicao y do drone
     * @param tickAtual tick atual do bot
     * @return Um array de floats contendo a distancia-tempoSpawn de cada ouro
     *          ou null, quando nao existe nenhum ouro
     */
    public static float[] getDistanceGold(int x, int y, int tickAtual) {
        if (posicoesOuro.size() == 0) { return null; }

        float[] ret = new float[posicoesOuro.size()];
        String[] ss;
        int x_ouro, y_ouro, tickAntigo, i = 0, tempo;

        for (String s: posicoesOuro.keySet()) {
            ss = s.split("-");
            x_ouro = Integer.parseInt(ss[0]);
            y_ouro = Integer.parseInt(ss[1]);
            tickAntigo = posicoesOuro.get(s);
            tempo = (Config.tempoSpawn / Config.timerRapido) - (tickAtual - tickAntigo);
            ret[i] = Math.abs(x_ouro - x) + Math.abs(y_ouro - y) - tempo;
            i++;
        }
        return ret;
    }
}
