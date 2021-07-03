package control.server;

import INF1771_GameClient.Dto.PlayerInfo;
import INF1771_GameClient.Dto.ScoreBoard;
import INF1771_GameClient.Socket.*;
import control.drone.Bot;
import control.Config;
import control.drone.Observation;

import java.awt.*;
import java.util.ArrayList;

public class GameListener implements CommandListener {

    Bot bot;

    public GameListener(Bot bot) {
        this.bot = bot;
    }

    /**
     * Traduz as observacoes
     * @param observations String na forma "obs,obs,obs,obs...obs,"
     */
    private void parseObservations(String observations) {
        if (observations.trim().equals("")) { return; }

        // cria uma lista contendo a string de cada observacao
        String[] lista = observations.trim().split(",");
        String[] temp;

        // adiciona cada observacao de acordo com o nome no enum
        for (String o: lista) {
            // verificando a observaçao de inimigo (enemy#X)
            temp = o.split("#");
            if (temp.length > 1) {
                this.bot.ultimaObservacao.isInimigoFrente = true;
                this.bot.ultimaObservacao.distanciaInimigoFrente = Integer.parseInt(temp[1]);
            }

            if (o.equals("blocked")) { this.bot.ultimaObservacao.isParede = true; }
            if (o.equals("steps")) { this.bot.ultimaObservacao.isInimigo = true; }
            if (o.equals("breeze")) { this.bot.ultimaObservacao.isBuraco = true; }
            if (o.equals("flash")) { this.bot.ultimaObservacao.isFlash = true; }
            if (o.equals("blueLight")) { this.bot.ultimaObservacao.isTesouro = true; }
            if (o.equals("redLight")) { this.bot.ultimaObservacao.isPowerup = true; }
        }

    }


    /**
     * Traduz o status do meu drone
     * @param status array na forma "...", "x", "y", "dir", "state", "score", "energy"
     */
    private void parseStatus(String[] status) {
        if (status.length != 7) { return; }
        this.bot.x = Integer.parseInt(status[1]);
        this.bot.y = Integer.parseInt(status[2]);
        this.bot.dir = PlayerInfo.Direction.valueOf(status[3].toLowerCase());
        this.bot.state = PlayerInfo.State.valueOf(status[4].toLowerCase());
        this.bot.score = Long.parseLong(status[5]);
        this.bot.energy = Integer.parseInt(status[6]);
    }


    /**
     * Traduz um player qualquer
     * @param player array na forma "...", "node", "name", "x", "y", "dir", "state", "color"
     * Neste caso, dir e state não estão como string, mas como indices no enumerador
     */
    private void parsePlayer(String[] player) {
        if (player.length != 7) { return; }
        long node = Long.parseLong(player[1]);
        String name = player[2];
        int x = Integer.parseInt(player[3]);
        int y = Integer.parseInt(player[4]);
        PlayerInfo.Direction dir = PlayerInfo.Direction.values()[Integer.parseInt(player[5])];
        PlayerInfo.State state = PlayerInfo.State.values()[Integer.parseInt(player[6])];
        Color cor = Config.convertFromString(player[7]);

        PlayerInfo p = new PlayerInfo(node, name, x, y, dir, state, cor);
        this.bot.listaJogadores.put(node, p);
    }

    /**
     * Atualiza o status do jogo
     * @param status array na forma "...", "status", "time"
     */
    private void parseGameStatus(String[] status) {
        if (status.length != 3) { return; }
        PlayerInfo.State st = PlayerInfo.State.valueOf(status[1].toLowerCase());
        long time = Long.parseLong(status[2]);

        this.bot.state = st;
        this.bot.time = time;
    }

    /**
     * Atualiza a scoreboard da batalha
     * Cada scoreboard eh uma string com as informacoes separadas por # na forma:
     *      "nome#connected#score#energy(#cor)"
     * onde nome eh uma string que representa o nome do jogador
     * onde connected eh "connected" se estiver conectado
     * onde score e energy eh um inteiro
     * onde cor (opcional) eh uma tupla contendo as cores em rgb na forma ????
     *
     * @param scoreboard array na forma "...", "scoreboard", "scoreboard", ..., "scoreboard"
     */
    private void parseScoreboard(String[] scoreboard) {
        ArrayList<ScoreBoard> s = new ArrayList<>();
        for (int i = 1; i < scoreboard.length; i ++) {
            String[] ss = scoreboard[i].split("#");
            String name = ss[0];
            boolean connected = ss[1].equals("connected");
            int score = Integer.parseInt(ss[2]);
            int energy = Integer.parseInt(ss[3]);
            Color cor;
            if (ss.length == 5) {
                cor = Config.convertFromString(ss[4]);
            } else {
                cor = Color.BLACK;
            }

            ScoreBoard sb = new ScoreBoard(name, connected, score, energy, cor);
            s.add(sb);
        }
        this.bot.listaScores = s;
    }

    /**
     * Traduz uma atualizacao do jogo
     * @param notification uma string qualquer
     */
    private void parseNotification(String notification) {
        this.bot.log.add(notification);
    }

    /**
     * Traduz a entrada de um novo jogador
     * @param newPlayer Nome do novo jogador
     */
    private void parsePlayerNew(String newPlayer) {
        String s = String.format("Player [%s] entrou no jogo!", newPlayer);
        this.bot.log.add(s);
    }

    /**
     * Traduz a saida de um novo jogador
     * @param leftPlayer Nome do jogador saindo
     */
    private void parsePlayerLeft(String leftPlayer) {
        String s = String.format("Player [%s] saiu do jogo!", leftPlayer);
        this.bot.log.add(s);
    }


    /**
     * Traduz uma mudanca de nome
     * @param oldName Nome antigo do jogador
     * @param newName Nome novo do jogador
     */
    private void parseChangeName(String oldName, String newName) {
        String s = String.format("[%s] trocou de nome para [%s]", oldName, newName);
        this.bot.log.add(s);
    }

    /**
     * Traduz um acerto de um disparo em um jogador
     * @param target Jogador atingido
     */
    private void parseHit(String target) {
        String s = String.format("Acertei [%s]", target);
        this.bot.log.add(s);
        this.bot.ultimaObservacao.isAcerto = true;
    }

    /**
     * Traduz um dano recebido devido a um disparo de um jogador
     * @param shooter Jogador que fez o disparo
     */
    private void parseDamage(String shooter) {
        String s = String.format("Fui atingido por [%s]", shooter);
        this.bot.log.add(s);
        this.bot.ultimaObservacao.isDano = true;
    }

    public void receiveCommand(String[] cmd) {
        if (cmd.length == 0) { return; }

        switch (cmd[0]) {
            case "o" -> parseObservations(cmd[1]);
            case "s" -> parseStatus(cmd);
            case "player" -> parsePlayer(cmd);
            case "g" -> parseGameStatus(cmd);
            case "u" -> parseScoreboard(cmd);
            case "notification" -> parseNotification(cmd[1]);
            case "hello" -> parsePlayerNew(cmd[1]);
            case "goodbye" -> parsePlayerLeft(cmd[1]);
            case "changename" -> parseChangeName(cmd[1], cmd[2]);
            case "h" -> parseHit(cmd[1]);
            case "d" -> parseDamage(cmd[1]);
        }

    }
}
