package control.map;

import java.util.*;

import INF1771_GameClient.Dto.PlayerInfo;
import control.Config;
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
    private static HashMap<String, Boolean> posicoesSafe;
    private static HashMap<String, Integer> posicoesUnsafe;

    public static final int comprimento = 59;
    public static final int altura = 34;

    private static Path bufferPath;

    public static int xSpawn = -1;
    public static int ySpawn = -1;

    /**
     * Inicializa as variaveis e listas internas do mapa
     */
    public static void iniciar() {
        mapa = new HashMap<>();                 // mapa com todas as posicoes conhecidas
        posicoesOuro = new HashMap<>();         // mapa contendo os ouros e quantos ticks desde a ultima visita
        posicoesPowerup = new HashMap<>();      // mapa contendo os powerups e quantos ticks desde a ultima visita
        posicoesSafe = new HashMap<>();         // mapa contendo todas as posicoes seguras conhecidas
        posicoesUnsafe = new HashMap<>();       // mapa contendo todas as posicoes nao seguras e quantos ticks
    }

    /**
     * Atualiza todos os ouros, powerups, e posicoes nao seguras
     * com o tempo em milissegundos fornecido
     * @param ms milisegundos para atualizar as posicoes
     */
    public static void fazerTick(int ms) {
        posicoesOuro.replaceAll((s, v) -> v + ms);
        posicoesPowerup.replaceAll((s, v) -> v + ms);

        // atualizando as posicoes nao seguras
        ArrayList<String> toRemove = new ArrayList<>();
        for (String s: posicoesUnsafe.keySet()) {
            int tick = posicoesUnsafe.get(s);
            if (tick > 5) { toRemove.add(s); }
            else posicoesUnsafe.put(s, tick + 1);
        }
        // removendo as posicoes nao seguras velhas
        for (String s: toRemove) {
            posicoesUnsafe.remove(s);
        }
    }

    /**
     * Atualiza a casa sem verificação de segurança do set.
     * Ou seja, o que foi passado será atualizado (ao contrario do set)
     * @param x posicao x da casa
     * @param y posicao y da casa
     * @param tipoCasa tipo da casa
     */
    public static void setForce(int x, int y, Position tipoCasa) {
        String s = x + "-" + y;
        mapa.put(s, tipoCasa);
        if (tipoCasa == Position.OURO) {
            setOuro(x, y);
        } else if (tipoCasa == Position.POWERUP) {
            setPowerup(x, y);
        }
    }

    /**
     * Atualiza a casa com a posicao fornecida
     * Se o tipo for SAFE ou DANGER, mas ja se sabe o que tem na casa, não é atualizado
     * Se o tipo for EMPTY mas souber que ali tem um ouro ou powerup, nao é atualizado
     * É atualizado o spawn na primeira vez que esse método é chamado
     * @param x posicao x da casa
     * @param y posicao y da casa
     * @param tipoCasa tipo da casa
     */
    public static void set(int x, int y, Position tipoCasa) {

        Position get = get(x, y);
        if (get == tipoCasa) {
            return;
        }

        // primeiro set de uma casa vazia, logo eh o spawn do jogador
        if (tipoCasa == Position.VAZIO && xSpawn == -1) {
            xSpawn = x;
            ySpawn = y;
        }

        String s = x + "-" + y;

        if (tipoCasa == Position.PERIGO) {
            if (get(x, y) == Position.DESCONHECIDO) {
                mapa.put(s, tipoCasa);
            }
            return;
        }
        if (tipoCasa == Position.SEGURO) {
            if (get(x, y) == Position.DESCONHECIDO || get(x, y) == Position.PERIGO) {
                mapa.put(s, tipoCasa);
                setSeguro(x, y);
            }
            return;
        }
        if (tipoCasa == Position.VAZIO) {
            if (!(get(x, y) == Position.OURO || get(x, y) == Position.POWERUP)) {
                mapa.put(s, tipoCasa);
            }
            return;
        }
        mapa.put(s, tipoCasa);
        removerSeguro(x, y);
    }

    /**
     * Retorna o valor da casa
     * @param x posicao x da casa
     * @param y posicao y da casa
     * @return tipo da casa
     */
    public static Position get(int x, int y) {
        if (x < 0 || y < 0 || x >= Field.comprimento || y >= Field.altura) {
            return Position.PAREDE;
        }

        String s = x + "-" + y;
        Position ret = mapa.get(s);
        // retorna a resposta, ou UNKNOWN se for nulo
        return Objects.requireNonNullElse(ret, Position.DESCONHECIDO);
    }

    /**
     * Coloca a posicao nao segura por 5 ticks
     * @param x posicao x da casa
     * @param y posicao y da casa
     */
    public static void setPosicaoInsegura(int x, int y) {
        posicoesUnsafe.put(x + "-" + y, 1);
    }

    /**
     * Retorna se a posicao é atualmente insegura
     */
    private static boolean isInsegura(int x, int y) {
        return posicoesUnsafe.containsKey(x + "-" + y);
    }

    public static void setOuro(int x, int y) {
        String s = x + "-" + y;
        posicoesOuro.put(s, 0);
    }

    /**
     * Verifica se era para ter um ouro ou Powerup na posicao.
     * Se era para ter, esse ouro ou powerup sera reiniciado, ou seja,
     * o tempo de spawn será como se eu tivesse acabado de pegar
     * @param x posicao x do ouro/powerup
     * @param y posicao y do ouro/powerup
     */
    public static void deveriaTerOuroOuPowerupAqui(int x, int y) {
        String s = x + "-" + y;
        boolean hasGold = posicoesOuro.containsKey(s);
        boolean hasPowerup = posicoesPowerup.containsKey(s);

        if (!(hasGold || hasPowerup)) {
            return;     // nao tem nada registrado aqui
        }

        int tempo;
        if (hasGold) { tempo = posicoesOuro.get(s); }
        else { tempo = posicoesPowerup.get(s); }

        // era para ja ter nascido?
        if (tempo >= Config.tempoSpawn) {
            // atualiza os dados
            if (hasGold) { setOuro(x, y); }
            else { setPowerup(x, y); }
        }
    }

    /**
     * Salva o powerup na lista interna
     * @param x posicao x da casa
     * @param y posicao y da casa
     */
    public static void setPowerup(int x, int y) {
        String s = x + "-" + y;
        posicoesPowerup.put(s, 0);
    }

    /**
     * Salva a posicao segura na lista interna
     * @param x posicao x da casa
     * @param y posicao y da casa
     */
    private static void setSeguro(int x, int y) {
        String s = x + "-" + y;
        posicoesSafe.put(s, true);
    }

    /**
     * Remove a casa como uma casa segura
     * @param x posicao x da casa
     * @param y posicao y da casa
     */
    public static void removerSeguro(int x, int y) {
        String s = x + "-" + y;
        posicoesSafe.remove(s);
    }

    /**
     * Atualiza os blocos na sua frente e lados com aquela informacao
     * @param x Posicao x
     * @param y Posicao y
     * @param tipo Informacao do bloco
     */
    public static void setAoRedor(int x, int y, Position tipo) {
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
    public static void setFrente(int x, int y, PlayerInfo.Direction dir, Position tipo) {
        switch (dir) {
            case north -> set(x, y - 1, tipo);
            case east -> set(x + 1, y, tipo);
            case south -> set(x, y + 1, tipo);
            case west -> set(x - 1, y, tipo);
        }
    }

    /**
     * Atualiza o bloco atrás do drone com aquela informacao
     */
    public static void setAtras(int x, int y, PlayerInfo.Direction dir, Position tipo) {
        switch (dir) {
            case north -> set(x, y + 1, tipo);
            case east -> set(x - 1, y, tipo);
            case south -> set(x, y - 1, tipo);
            case west -> set(x + 1, y, tipo);
        }
    }

    /**
     * Verifica se tem algum ouro para coletar
     * Ele tem um ouro para coletar se o tempo de respawn do ouro é
     * menor que o tempo para chegar lá
     * @param x posicao x do drone
     * @param y posicao y do drone
     * @param dir direcao do drone
     * @return true se houver algum ouro para coletar
     */
    public static boolean temOuroParaColetar(int x, int y, PlayerInfo.Direction dir) {
        return temAlgoParaColetar(x, y, dir, posicoesOuro);
    }

    /**
     * Verifica se tem algum powerup para coletar
     * Ele tem um powerup para coletar se o tempo de respawn do ouro é
     * menor que o tempo para chegar lá
     * @param x posicao x do drone
     * @param y posicao y do drone
     * @param dir direcao do drone
     * @return true se houver algum powerup para coletar
     */
    public static boolean temPowerupParaColetar(int x, int y, PlayerInfo.Direction dir) {
        return temAlgoParaColetar(x, y, dir, posicoesPowerup);
    }

    /**
     * Coloca o path para o powerup mais proximo no bufferPath, se houver
     * Se nao, bufferpath sera nulo
     */
    public static int[] powerupMaisPerto(int x, int y, PlayerInfo.Direction dir) {
        Path pathTemp;
        int xTemp, yTemp;
        String[] sTemp;
        bufferPath = null;
        int xRes = -1, yRes = -1;
        for (String s: posicoesPowerup.keySet()) {
            sTemp = s.split("-");
            xTemp = Integer.parseInt(sTemp[0]);
            yTemp = Integer.parseInt(sTemp[1]);

            pathTemp = aEstrela(x, y, dir, xTemp, yTemp);
            if (pathTemp == null) {continue;}

            if (bufferPath == null || pathTemp.tamanho < bufferPath.tamanho) {
                bufferPath = pathTemp;
                xRes = xTemp;
                yRes = yTemp;
            }
        }
        if (xRes != -1) {
            return new int[] {xRes, yRes};
        } else {
            return null;
        }

    }

    /**
     * Verifica se tem algum powerup ou ouro para coletar.
     * Usado pelos metodos hasOuroParaColetar e hasPowerupParaColetar
     * Se tiver, retorna True e coloca o path no buffer
     */
    private static boolean temAlgoParaColetar(int x, int y, PlayerInfo.Direction dir, HashMap<String, Integer> positions) {
        int xDest, yDest, tickDest, distanciaDest, ticksParaNascer;
        String[] temp;

        // vao retornar true, mas tbm vai colocar no buffer o path para o mais proximo
        boolean ret = false;
        Path pathMaisProximo = null;

        for (Map.Entry<String, Integer> entry: positions.entrySet()) {
            temp = entry.getKey().split("-");
            xDest = Integer.parseInt(temp[0]);
            yDest = Integer.parseInt(temp[1]);
            tickDest = entry.getValue();
            ticksParaNascer = (Config.tempoSpawn - tickDest) / Config.timerNormal;
            // System.out.println(entry.getKey() + '/' + ticksParaNascer);

            bufferPath = aEstrela(x, y, dir, xDest, yDest);
            if (bufferPath == null) { continue; }
            distanciaDest = bufferPath.tamanho;
            if (ticksParaNascer - distanciaDest < 0) {
                ret = true;
                if (pathMaisProximo == null || pathMaisProximo.tamanho > bufferPath.tamanho) {
                    pathMaisProximo = bufferPath;
                }
            }
        }
        if (ret) {
            bufferPath = pathMaisProximo;       // coloca no buffer
            return true;
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
    public static boolean temOuro() { return posicoesOuro.size() > 0; }

    /**
     * Retorna o ponto medio dos ouros, ou o spawn se nao tiver nenhum ouro
     */
    public static int[] pontoMedioOuro() {
        if (!temOuro()) {
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
        int[] ret = new int[2];
        int menorDist = 100000;
        int x, y, d, distanciaBlocoPonto, distanciaBlocoDrone;
        Path path;
        String[] temp;
        for (String s: posicoesSafe.keySet()) {
            temp = s.split("-");
            x = Integer.parseInt(temp[0]);
            y = Integer.parseInt(temp[1]);

            // distanciaBlocoPonto = (int) Math.pow(manhattan(x, y, xPonto, yPonto), 2);
            distanciaBlocoPonto = 2 * (int) Math.sqrt((Math.pow(x - xPonto, 2) + Math.pow(y - yPonto, 2)));

            path = aEstrela(xDrone, yDrone, dirDrone, x, y);
            if (path == null) {continue;}
            distanciaBlocoDrone = path.tamanho;

            d = distanciaBlocoDrone  + distanciaBlocoPonto;
            if (d < menorDist) {
                ret[0] = x;
                ret[1] = y;
                menorDist = d;
            }
        }
        return ret;
    }

    /**
     * Verifica se tem algum powerup ja descoberto
     * @return true se houver algum power
     */
    public static boolean temPowerup() { return posicoesPowerup.size() > 0; }

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
     * Verifica se tem alguma parede na minha frente em até q blocos de distancia
     * @param x Posicao x do drone
     * @param y Posicao y do drone
     * @param dir Direção do drone
     * @param q Quantidade de blocos para serem verificados
     * @return true se tiver alguma parede, false se não tiver ou não for conhecido
     */
    public static boolean temParedeNaFrente(int x, int y, PlayerInfo.Direction dir, int q) {
        int[][] coordsParaVerificar = new int[q][2];
        switch (dir) {
            case north -> {
                for (int i = 1; i < q; i ++) {
                    coordsParaVerificar[i-1][0] = x;
                    coordsParaVerificar[i-1][1] = y - i;
                }
            }
            case south -> {
                for (int i = 1; i < q; i ++) {
                    coordsParaVerificar[i-1][0] = x;
                    coordsParaVerificar[i-1][1] = y + i;
                }
            }
            case east -> {
                for (int i = 1; i < q; i ++) {
                    coordsParaVerificar[i-1][0] = x + i;
                    coordsParaVerificar[i-1][1] = y;
                }
            }
            case west -> {
                for (int i = 1; i < q; i ++) {
                    coordsParaVerificar[i-1][0] = x - i;
                    coordsParaVerificar[i-1][1] = y;
                }
            }
        }
        // verificando os blocos
        for (int []pos: coordsParaVerificar) {
            if (get(pos[0], pos[1]) == Position.PAREDE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula a distancia manhattan entre dois pontos
     */
    public static int manhattan(int x, int y, int x1, int y1) {
        return (Math.abs(x - x1) + Math.abs(y - y1));
    }

    /**
     * Algoritmo A* para encontrar o melhor caminho entre os dois pontos.
     * Evita caminhos perigosos (com possível burado ou flash) e paredes
     * Penaliza caminhos em que o drone anda de ré
     * Recompensa caminhos que passam por lugares seguros nao explorados
     * Penaliza caminhos que passam por posicoes nao seguras
     * @param xDrone posicao x do drone
     * @param yDrone posicao y do drone
     * @param dirDrone direcao do drone
     * @param xDest posicao x do destino
     * @param yDest posicao y do destino
     * @return Um objeto Path contendo o caminho até o ponto, ou null caso não haja nenhum caminho até lá
     */
    public static Path aEstrela(int xDrone, int yDrone, PlayerInfo.Direction dirDrone, int xDest, int yDest) {

        Node.init();
        Node nodeInicial = Node.getNode(xDrone, yDrone, dirDrone);
        nodeInicial.distanciaFinal = manhattan(xDrone, yDrone, xDest, yDest);
        nodeInicial.ticksPercorridos = 0;
        nodeInicial.anterior = null;

        // priority queue contendo os node que ainda nao precisam ser verificados
        PriorityQueue<Node> openSet = new PriorityQueue<>(100, new CompararNode());
        openSet.add(nodeInicial);

        Node node;
        int novoTick, custo;
        while (!openSet.isEmpty()) {
            node = openSet.poll();
            if (node.x == xDest && node.y == yDest) {
                return node.gerarCaminho();
            }
            Node[] vizinhos = node.gerarVizinhos(node);

            for (Node viz: vizinhos) {
                if (viz == null) { continue; }

                custo = 1;
                if (viz.ehAtras) custo += 1.5;
                if (viz.ehSafe) custo *= 0.8;
                if (isInsegura(viz.x, viz.y)) custo += 10;


                novoTick = node.ticksPercorridos + custo;
                if (novoTick < viz.ticksPercorridos) {
                    viz.anterior = node;
                    viz.ticksPercorridos = node.ticksPercorridos + custo;
                    viz.distanciaFinal = manhattan(viz.x, viz.y, xDest, yDest);
                    if (!openSet.contains(viz)) {
                        openSet.add(viz);
                    }
                }
            }
        }
        return null;
    }

    private static class CompararNode implements Comparator<Node> {
        public int compare(Node n1, Node n2) {
            return (n1.distanciaFinal + n1.ticksPercorridos) - (n2.distanciaFinal + n2.ticksPercorridos); }
    }
}