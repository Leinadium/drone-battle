package control;

import java.util.HashMap;
import java.util.Objects;

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

    public final int comprimento = 59;
    public final int altura = 10;

    public static void init() {
        if (mapa == null) {
            mapa = new HashMap<>();
            posicoesOuro = new HashMap<>();
        }
    }

    public static void set(int x, int y, Position tipoCasa) {
        String s = x + "-" + y;
        mapa.put(s, tipoCasa);
    }

    public static Position get(int x, int y) {
        String s = x + "-" + y;
        Position ret = mapa.get(s);
        // retorna a resposta, ou UNKNOWN se for nulo
        return Objects.requireNonNullElse(ret, Position.UNKNOWN);
    }

    public static void setOuro(int x, int y, int time) {
        String s = x + "-" + y;
        posicoesOuro.put(s, time);
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
        res[0] = get(x, y + 1);
        res[1] = get(x + 1, y);
        res[2] = get(x, y - 1);
        res[3] = get(x - 1, y);
        return res;
    }
}
