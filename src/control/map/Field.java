package control.map;

import java.util.*;

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

    private static int xSpawn = -1;
    private static int ySpawn = -1;

    public static void init() {
        if (mapa == null) {
            mapa = new HashMap<>();
            posicoesOuro = new HashMap<>();
            posicoesPowerup = new HashMap<>();
        }
    }

    public static void set(int x, int y, Position tipoCasa) {

        // primeiro set de uma casa vazia, logo eh o spawn do jogador
        if (tipoCasa == Position.EMPTY && xSpawn == -1) {
            xSpawn = x;
            ySpawn = y;
        }

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
    public static void setAround(int x, int y, Position tipo) {
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
     * @param dir direcao do drone
     * @param tick tick atual do jogo
     * @return true se houver algum ouro para coletar
     */
    public static boolean hasOuroParaColetar(int x, int y, PlayerInfo.Direction dir, int tick) {
        return hasAlgoParaColetar(x, y, dir, tick, posicoesOuro);
    }

    /**
     * Verifica se tem algum powerup para coletar
     * Ele tem um powerup para coletar se o tempo de respawn do ouro é
     * menor que o tempo para chegar lá
     * @param x posicao x do drone
     * @param y posicao y do drone
     * @param dir direcao do drone
     * @param tick tick atual do jogo
     * @return true se houver algum powerup para coletar
     */
    public static boolean hasPowerupParaColetar(int x, int y, PlayerInfo.Direction dir, int tick) {
        return hasAlgoParaColetar(x, y, dir, tick, posicoesPowerup);
    }

    private static boolean hasAlgoParaColetar(int x, int y, PlayerInfo.Direction dir, int tick, HashMap<String, Integer> posicoes) {
        int xDest, yDest, tickDest, distanciaDest, ticksParaNascer;
        String[] temp;
        for (Map.Entry<String, Integer> entry: posicoes.entrySet()) {
            temp = entry.getKey().split("-");
            xDest = Integer.parseInt(temp[0]);
            yDest = Integer.parseInt(temp[1]);
            tickDest = entry.getValue();
            ticksParaNascer = (Config.tempoSpawn / Config.timerRapido) - (tick - tickDest);

            bufferPath = aStar(x, y, dir, xDest, yDest);
            if (bufferPath == null) { return false; }
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
     * Retorna o ponto medio dos ouros, ou o spawn se nao tiver nenhum ouro
     */
    public static int[] pontoMedioOuro() {
        if (!hasOuro()) {
            return new int[] {xSpawn, ySpawn};
        }
        int somax = 0;
        int somay = 0;
        String [] c;
        for (String s: posicoesOuro.keySet()) {
            c = s.split("-");
            somax += Integer.parseInt(c[0]);
            somay += Integer.parseInt(c[1]);
        }
        return new int[] {somax / posicoesOuro.size(), somay / posicoesOuro.size()};
    }

    /**
     * Encontra o melhor bloco ainda não explorado a partir do ponto focal fornecido
     */
    public static int[] melhorBlocoUsandoPontoFocal(int xDrone, int yDrone, PlayerInfo.Direction dirDrone, int xPonto, int yPonto) {
        // TODO
        return null;
    }

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

    public static int manhattan(int x, int y, int x1, int y1) {
        return (Math.abs(x - x1) + Math.abs(y - y1));
    }

    /**
     * Algoritmo A* para encontrar o melhor caminho entre os dois pontos.
     * Evita caminhos perigosos (com possível burado ou flash)
     * @param xDrone posicao x do drone
     * @param yDrone posicao y do drone
     * @param dirDrone direcao do drone
     * @param xDest posicao x do destino
     * @param yDest posicao y do destino
     * @return Um objeto Path contendo o caminho até o ponto, ou null caso não haja nenhum caminho até lá
     */
    public static Path aStar(int xDrone, int yDrone, PlayerInfo.Direction dirDrone,  int xDest, int yDest) {

        Node.init();
        Node nodeInicial = Node.getNode(xDrone, yDrone, dirDrone);
        nodeInicial.distanciaFinal = manhattan(xDrone, yDrone, xDest, yDest);
        nodeInicial.ticksPercorridos = 0;
        nodeInicial.anterior = null;

        // priority queue contendo os node que ainda nao precisam ser verificados
        PriorityQueue<Node> openSet = new PriorityQueue<>(100, new CompararNode());
        openSet.add(nodeInicial);

        Node node;
        int novoTick;
        while (!openSet.isEmpty()) {
            node = openSet.poll();
            if (node.x == xDest && node.y == yDest) {
                return node.gerarCaminho();
            }
            Node[] vizinhos = node.gerarVizinhos(node);
            if (vizinhos == null) {continue;}

            for (Node viz: vizinhos) {
                novoTick = node.ticksPercorridos + 1;
                if (novoTick < viz.ticksPercorridos) {
                    viz.anterior = node;
                    viz.ticksPercorridos = node.ticksPercorridos + 1;
                    viz.distanciaFinal = manhattan(viz.x, viz.y, xDest, yDest);
                    if (!openSet.contains(viz)) {
                        openSet.add(viz);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Classe para a implementacao do A*
     */
    private static class Node {

        private static HashMap<String, Node> hashMap;

        public static Node getNode(int x, int y, PlayerInfo.Direction dir) {
            String s = x + "/" + y + "/" + dir;
            if (hashMap.containsKey(s)) { return hashMap.get(s); }
            Node n = new Node(x, y, dir);
            hashMap.put(s, n);
            return n;
        }

        public static void init() {hashMap = new HashMap<>();}

        public int distanciaFinal;
        public int ticksPercorridos = 1000000;
        public Node anterior;

        public int x;
        public int y;
        public PlayerInfo.Direction dir;
        public Node(int x, int y, PlayerInfo.Direction dir) {
            this.x = x; this.y = y; this.dir = dir;
        }

        public Node[] gerarVizinhos(Node n) {
            switch (n.dir) {
                case north, south -> {
                    return new Node[]{
                            Node.getNode(x, y + 1, dir), Node.getNode(x, y - 1, dir),
                            Node.getNode(x, y, PlayerInfo.Direction.east),
                            Node.getNode(x, y, PlayerInfo.Direction.west)
                    };
                }
                case east, west -> {
                    return new Node[]{
                            Node.getNode(x + 1, y, dir), Node.getNode(x - 1, y, dir),
                            Node.getNode(x, y, PlayerInfo.Direction.north),
                            Node.getNode(x, y, PlayerInfo.Direction.south)
                    };
                }
            }
            return null;
        }

        public Path gerarCaminho() {
            Node atual = this;
            Node anterior = this.anterior;

            ArrayList<Action> acoes = new ArrayList<>();
            while (anterior != null) {
                if (atual.dir == anterior.dir) {
                    switch (atual.dir) {
                        case north -> {
                            if (atual.y > anterior.y) { acoes.add(0, Action.TRAS); } else { acoes.add(0, Action.FRENTE); }
                        }
                        case south -> {
                            if (atual.y < anterior.y) { acoes.add(0, Action.TRAS); } else { acoes.add(0, Action.FRENTE); }
                        }
                        case west -> {
                            if (atual.x > anterior.x) { acoes.add(0, Action.TRAS); } else { acoes.add(0, Action.FRENTE); }
                        }
                        case east -> {
                            if (atual.x < anterior.x) { acoes.add(0, Action.TRAS); } else { acoes.add(0, Action.FRENTE); }
                        }
                    }
                } else {
                    switch (atual.dir) {
                        case north -> {
                            if (anterior.dir == PlayerInfo.Direction.east) {acoes.add(0, Action.ESQUERDA);} else { acoes.add(0, Action.DIREITA);}
                        }
                        case south -> {
                            if (anterior.dir == PlayerInfo.Direction.west) {acoes.add(0, Action.ESQUERDA);} else { acoes.add(0, Action.DIREITA);}
                        }
                        case east -> {
                            if (anterior.dir == PlayerInfo.Direction.south) {acoes.add(0, Action.ESQUERDA);} else { acoes.add(0, Action.DIREITA);}
                        }
                        case west -> {
                            if (anterior.dir == PlayerInfo.Direction.north) {acoes.add(0, Action.ESQUERDA);} else { acoes.add(0, Action.DIREITA);}
                        }
                    }
                }
                atual = anterior;
                anterior = atual.anterior;
            }
            Path ret = new Path();
            ret.acoes = acoes.toArray(ret.acoes);
            ret.tamanho = ret.acoes.length;
            return ret;
        }
    }

    private static class CompararNode implements Comparator<Node> {
        public int compare(Node n1, Node n2) { return n2.distanciaFinal - n1.distanciaFinal; }
    }
}
