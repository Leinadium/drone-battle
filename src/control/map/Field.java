package control.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import INF1771_GameClient.Dto.PlayerInfo;
import control.Config;
import control.enums.Action;
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

    private static Path bufferPath;

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
     * Verifica se tem algum ouro para coletar
     * Ele tem um ouro para coletar se o tempo de respawn do ouro é
     * menor que o tempo para chegar lá
     * @param x posicao x do drone
     * @param y posicao y do drone
     * @param tick tick atual do jogo
     * @return true se houver algum ouro para coletar
     */
    public static boolean hasOuroParaColetar(int x, int y, int tick) {
        return hasAlgoParaColetar(x, y, tick, posicoesOuro);
    }

    /**
     * Verifica se tem algum powerup para coletar
     * Ele tem um powerup para coletar se o tempo de respawn do ouro é
     * menor que o tempo para chegar lá
     * @param x posicao x do drone
     * @param y posicao y do drone
     * @param tick tick atual do jogo
     * @return true se houver algum powerup para coletar
     */
    public static boolean hasPowerupParaColetar(int x, int y, int tick) {
        return hasAlgoParaColetar(x, y, tick, posicoesPowerup);
    }

    private static boolean hasAlgoParaColetar(int x, int y, int tick, HashMap<String, Integer> posicoes) {
        int xDest, yDest, tickDest, distanciaDest, ticksParaNascer;
        String[] temp;
        for (Map.Entry<String, Integer> entry: posicoes.entrySet()) {
            temp = entry.getKey().split("-");
            xDest = Integer.parseInt(temp[0]);
            yDest = Integer.parseInt(temp[1]);
            tickDest = entry.getValue();
            ticksParaNascer = (Config.tempoSpawn / Config.timerRapido) - (tick - tickDest);

            bufferPath = aStar(x, y, xDest, yDest);
            distanciaDest = bufferPath.tamanho;
            if (ticksParaNascer - distanciaDest <= 0) { return true; }
        }
        return false;
    }

    /**
     * Retorna o ultimo Path calculado pelo aStar.
     * Se o ultimo comando foi hasOuroParaColetar ou hasPowerupParaColetar,
     *      o path retornado será exatamente esse
     */
    public static Path getBufferPath() { return bufferPath; }

    /**
     * Verifica se tem algum ouro ja descoberto
     * @return true se houver algum ouro
     */
    public static boolean hasOuro() { return posicoesOuro.size() > 0; }

    /**
     * Verifica se tem algum powerup ja descoberto
     * @return true se houver algum power
     */
    public static boolean hasPowerup() { return posicoesPowerup.size() > 0; }

    /**
     * Retorna uma lista com as posicoes x,y da area 3x3 na frente do drone.
     * Utilizado no calculo da fuga do drone
     */
    public static int[][] coords3x3Front(int x, int y, PlayerInfo.Direction dir) {
        int [][] ret;

        switch (dir) {
            case north -> ret = new int[][] {
                    {x, y - 1},{x, y - 2},{x, y - 3},
                    {x - 1, y - 1},{x - 1, y - 2},{x - 1, y - 3},
                    {x + 1,  y - 1},{x + 1, y - 2},{x + 1, y - 3},
            };
            case east -> ret = new int[][] {
                    {x + 1, y},{x + 2, y},{x + 3, y},
                    {x + 1, y - 1},{x + 2, y - 1},{x + 3, y - 1},
                    {x + 1,  y - 1},{x + 2, y - 1},{x + 3, y - 1},
            };
            case south -> ret = new int[][] {
                    {x, y + 1},{x, y + 2},{x, y + 3},
                    {x - 1, y + 1},{x - 1, y + 2},{x - 1, y + 3},
                    {x + 1,  y + 1},{x + 1, y + 2},{x + 1, y + 3},
            };
            case west -> ret = new int[][] {
                    {x - 1, y},{x - 2, y},{x - 3, y},
                    {x - 1, y - 1},{x - 2, y - 1},{x - 3, y - 1},
                    {x - 1,  y - 1},{x - 2, y - 1},{x - 3, y - 1},
            };
            default -> ret = null;
        }
        return ret;
    }

    /**
     * Retorna uma lista contendo as posicões dos quadrados das arestas
     * de um quadrado 5x5
     * Utilizado no calculo da fuga de um drone
     */
    public static int[][] coords5x5Around(int x, int y) {
        return new int[][] {
                {x-2, y-2},{x-1, y-2},{x, y-2},{x+1, y-2},
                {x+2, y-2},{x+2, y-1},{x+2, y},{x+2, y+1},
                {x+2, y+2},{x+1, y+2},{x, y+2},{x-1, y+2},
                {x-2, y+2},{x-2, y+1},{x-2, y},{x-2, y-1},
        };
    }

    /**
     * Retorna uma lista contendo as posições dos quadrados nas areas 5x2 nos lados
     * do drone
     * Utilizado no calculo da fuga de um drone
     */
    public static int[][] coords5x2Sides(int x, int y, PlayerInfo.Direction dir) {
        int[][] ret;
        switch (dir) {
            case north, south -> ret = new int[][] {
                    {x-2,y-2},{x-2,y-1},{x-2,y},{x-2,y+1},{x-2,y+2},
                    {x-1,y-2},{x-1,y-1},{x-1,y},{x-1,y+1},{x-1,y+2},
                    {x+1,y-2},{x+1,y-1},{x+1,y},{x+1,y+1},{x+1,y+2},
                    {x+2,y-2},{x+2,y-1},{x+2,y},{x+2,y+1},{x+2,y+2},
            };
            case east, west -> ret = new int[][] {
                    {x-2,y-2},{x-1,y-2},{x,y-2},{x+1,y-2},{x+2,y-2},
                    {x-2,y-1},{x-1,y-1},{x,y-1},{x+1,y-1},{x+2,y-1},
                    {x-2,y+1},{x-1,y+1},{x,y+1},{x+1,y+1},{x+2,y+1},
                    {x-2,y+2},{x-1,y+2},{x,y+2},{x+1,y+2},{x+2,y+2},
            };
            default -> ret = null;
        }
        return ret;
    }

    /**
     * Classe para retorno do algoritmo de pathfinder
     */
    public static class Path {
        public Action[] acoes;
        public int tamanho;
    }

    /**
     * Algoritmo A* para encontrar o melhor caminho entre os dois pontos.
     * Evita caminhos perigosos (com possível burado ou flash)
     * @param xDrone posicao x do drone
     * @param yDrone posicao y do drone
     * @param xDest posicao x do destino
     * @param yDest posicao y do destino
     * @return Um objeto Path contendo o caminho até o ponto, ou null caso não haja nenhum caminho até lá
     */
    public static Path aStar(int xDrone, int yDrone, int xDest, int yDest) {
        // TODO
        return new Path();
    }
}
