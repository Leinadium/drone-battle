package control;

import INF1771_GameClient.Dto.PlayerInfo;
import INF1771_GameClient.Dto.ScoreBoard;
import INF1771_GameClient.Dto.ShotInfo;
import INF1771_GameClient.Socket.HandleClient;
import control.enums.Action;
import control.enums.Observation;
import control.interfaces.IBot;
import control.server.ConnectionListener;
import control.server.GameListener;
import control.server.Sender;

import java.util.*;

public class Bot implements Runnable, IBot {
    public HandleClient client;                             // client para conexao
    public Map<Long, PlayerInfo> listaJogadores;            // lista dos jogadores
    public List<ShotInfo> listaDisparos;                    // lista de disparos
    public List<ScoreBoard> listaScores;                    // lista de scores
    public Log log;                                         // log do jogo
    public Sender sender;                                   // envio de mensagens
    public long time;                                       // tempo decorrido
    public ArrayList<Observation> ultimaObservacao;         // observacao do bot
    public ArrayList<Action> filaAcoes;                     // acoes a serem tomadas

    public AI ai;

    public int x;
    public int y;
    public PlayerInfo.Direction dir;
    public PlayerInfo.State state;
    public long score;
    public int energy;

    private void config() {
        this.client = new HandleClient();
        Field.init();
        this.log = new Log();
        this.sender = new Sender(this.client);

        this.ai = new AI(this);

        this.listaJogadores = new HashMap<>();
        this.listaDisparos = new ArrayList<>();
        this.listaScores = new ArrayList<>();
        this.ultimaObservacao = new ArrayList<>();
        this.filaAcoes = new ArrayList<>();

        this.time = 0;

        // cria um listener de conexao, e adiciona no client
        this.client.addChangeStatusListener(new ConnectionListener(this.client));

        // cria um listener de commando, passando o bot para atualizar minhas variaveis
        this.client.addCommandListener(new GameListener(this));

        // inicia a conexao
        this.client.connect(Config.url);

        this.sender.pedirNovoNome(Config.nomeJogador);
        this.sender.pedirCor(Config.corDefault);

        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Cria um novo bot, rodando as configuracoes padrao
     */
    public Bot() {
        config();
    }

    /**
     * Cria um novo bot, rodando as configuracoes padrao mas trocando o nome
     * @param nome Nome do bot
     */
    public Bot(String nome) {
        config();
        this.sender.pedirNovoNome(nome);
    }

    public void dormir(int ms) {
        if (ms < 0) { return; }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void exibirScore() {
        System.out.println("==== SCOREBOARD ====");
        System.out.printf("Time: %d", time);
        System.out.printf("Status: %s", state.toString());
        for (ScoreBoard sb: this.listaScores) {
            String s = String.format("%s (%s): score=%d, energy=%d",
                    sb.name,
                    sb.connected ? "connected" : "not connected",
                    sb.score,
                    sb.energy);
            System.out.println(s);
        }
    }

    /* IMPLEMENTACOES DE IBOT */
    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public long getScore() { return this.score; }
    public long getEnergy() { return this.energy; }
    public long getTime() { return this.time; }
    public PlayerInfo.Direction getDir() { return this.dir; }
    public ArrayList<Observation> getUltimaObservacao() { return this.ultimaObservacao; }
    public void setFilaAcoes(ArrayList<Action> filaAcoes) { this.filaAcoes = filaAcoes; }
    /* FIM DAS IMPLEMENTACOES DO IBOT */

    /**
     * Roda o bot
     */
    public void run() {
        int timer = 0;

        dormir(Config.timerRapido);

        while (true) {
            this.sender.pedirStatusGame();

            switch (state) {
                // dento de um jogo
                case game -> {
                    this.time += 1;
                    this.sender.pedirObservacao();
                    // this.AI.pensar()
                    dormir(Config.timerRapido);
                }
                case dead, ready, gameover -> {
                    timer += 1;
                    if (timer == 5) {
                        this.sender.pedirScoreboard();
                        exibirScore();
                        timer = 0;
                    }
                    dormir(Config.timerDefault);
                }
            }
            this.log.printLast();
        }
    }
}
