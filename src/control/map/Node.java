package control.map;

import INF1771_GameClient.Dto.PlayerInfo;
import control.enums.Action;
import control.enums.Position;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe para a implementacao do A*
 */
class Node {

    private static HashMap<String, Node> hashMap;

    public static Node getNode(int x, int y, PlayerInfo.Direction dir) {
        return getNode(x, y, dir, false);
    }
    public static Node getNode(int x, int y, PlayerInfo.Direction dir, boolean ehAtras) {
        String s = x + "/" + y + "/" + dir + "/" + ehAtras;
        if (hashMap.containsKey(s)) { return hashMap.get(s); }
        Node n = new Node(x, y, dir, ehAtras);
        hashMap.put(s, n);
        return n;
    }

    public static void init() {hashMap = new HashMap<>();}

    public int distanciaFinal;
    public int ticksPercorridos = 1000000;
    public Node anterior;

    public int x;
    public int y;
    public boolean ehAtras;
    public boolean ehSafe;

    public PlayerInfo.Direction dir;
    public Node(int x, int y, PlayerInfo.Direction dir) {
        this.x = x; this.y = y; this.dir = dir; ehAtras = false;
        this.ehSafe = Field.get(x, y) == Position.SAFE;
    }
    public Node(int x, int y, PlayerInfo.Direction dir, boolean ehAtras) {
        this.x = x; this.y = y; this.dir = dir; this.ehAtras = ehAtras;
    }

    /**
     * Gera os vizinhos para o algoritmo do A*
     * Retira Nodes invalidos
     * @param n Node original
     * @return array de nodes. Pode ter nodes nulos
     */
    public Node[] gerarVizinhos(Node n) {
        Node[] ret;
        switch (n.dir) {
            case north -> ret = new Node[] {
                    Node.getNode(x, y - 1, dir), Node.getNode(x, y + 1, dir, true),
                    Node.getNode(x, y, PlayerInfo.Direction.east),
                    Node.getNode(x, y, PlayerInfo.Direction.west),
            };
            case south -> ret = new Node[] {
                    Node.getNode(x, y + 1, dir), Node.getNode(x, y - 1, dir, true),
                    Node.getNode(x, y, PlayerInfo.Direction.east),
                    Node.getNode(x, y, PlayerInfo.Direction.west)
            };
            case east -> ret = new Node[] {
                    Node.getNode(x + 1, y, dir), Node.getNode(x - 1, y, dir, true),
                    Node.getNode(x, y, PlayerInfo.Direction.north),
                    Node.getNode(x, y, PlayerInfo.Direction.south)
            };
            case west -> ret = new Node[] {
                    Node.getNode(x - 1, y, dir), Node.getNode(x + 1, y, dir, true),
                    Node.getNode(x, y, PlayerInfo.Direction.north),
                    Node.getNode(x, y, PlayerInfo.Direction.south)
            };
            default -> ret = null;
        }
        int x, y;
        for (int i = 0; i < 4; i ++) {
            x = ret[i].x;
            y = ret[i].y;
            Position tipo = Field.get(x, y);
            if (tipo == Position.DANGER || tipo == Position.PAREDE || tipo == Position.UNKNOWN) {
                ret[i] = null;
            }
        }
        return ret;
    }

    public Path gerarCaminho() {
        Node atual = this;
        Node anterior = this.anterior;

        ArrayList<Action> acoes = new ArrayList<>();
        while (anterior != null) {
            if (atual.dir == anterior.dir) {
                switch (atual.dir) {
                    case north -> {
                        if (atual.y > anterior.y) { acoes.add(0, Action.BACK); } else { acoes.add(0, Action.FRONT); }
                    }
                    case south -> {
                        if (atual.y < anterior.y) { acoes.add(0, Action.BACK); } else { acoes.add(0, Action.FRONT); }
                    }
                    case west -> {
                        if (atual.x > anterior.x) { acoes.add(0, Action.BACK); } else { acoes.add(0, Action.FRONT); }
                    }
                    case east -> {
                        if (atual.x < anterior.x) { acoes.add(0, Action.BACK); } else { acoes.add(0, Action.FRONT); }
                    }
                }
            } else {
                switch (atual.dir) {
                    case north -> {
                        if (anterior.dir == PlayerInfo.Direction.east) {acoes.add(0, Action.LEFT);} else { acoes.add(0, Action.RIGHT);}
                    }
                    case south -> {
                        if (anterior.dir == PlayerInfo.Direction.west) {acoes.add(0, Action.LEFT);} else { acoes.add(0, Action.RIGHT);}
                    }
                    case east -> {
                        if (anterior.dir == PlayerInfo.Direction.south) {acoes.add(0, Action.LEFT);} else { acoes.add(0, Action.RIGHT);}
                    }
                    case west -> {
                        if (anterior.dir == PlayerInfo.Direction.north) {acoes.add(0, Action.LEFT);} else { acoes.add(0, Action.RIGHT);}
                    }
                }
            }
            atual = anterior;
            anterior = atual.anterior;
        }

        if (acoes.size() == 0) { return null; }

        Path ret = new Path();
        ret.xDest = this.x;
        ret.yDest = this.y;

        ret.acoes = new Action[acoes.size()];
        for (int i = 0; i < acoes.size(); i ++) { ret.acoes[i] = acoes.get(i); }
        ret.tamanho = ret.acoes.length;
        return ret;
    }
}