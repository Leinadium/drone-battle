package control.drone;

import INF1771_GameClient.Dto.PlayerInfo;
import INF1771_GameClient.Dto.ScoreBoard;
import INF1771_GameClient.Dto.ShotInfo;
import INF1771_GameClient.Socket.HandleClient;
import control.Config;
import control.enums.Action;
import control.interfaces.IBot;
import control.map.Field;
import control.server.ConnectionListener;
import control.server.GameListener;
import control.server.Log;
import control.server.Sender;
import control.enums.State;

import graphics.View;

import javax.swing.*;
import java.util.*;

public class Bot implements Runnable, IBot {

    public static int tickAtual;
    public static int ping = 0;

    public HandleClient client;                             // client para conexao
    public Map<Long, PlayerInfo> listaJogadores;            // lista dos jogadores
    public List<ShotInfo> listaDisparos;                    // lista de disparos
    public List<ScoreBoard> listaScores;                    // lista de scores
    public Log log;                                         // log do jogo
    public Sender sender;                                   // envio de mensagens
    public long time;                                       // tempo decorrido
    public Observation ultimaObservacao;         // observacao do bot

    public AI ai;

    public int x;
    public int y;
    public int tick;
    public PlayerInfo.Direction dir;
    public PlayerInfo.State state;
    public long score;
    public int energy;
    public int thinkingTime;

    public JFrame tela;

    private void config() {
        this.client = new HandleClient();
        Field.init();
        this.log = new Log();
        this.sender = new Sender(this.client);

        this.ai = new AI(this);

        this.listaJogadores = new HashMap<>();
        this.listaDisparos = new ArrayList<>();
        this.listaScores = new ArrayList<>();
        this.ultimaObservacao = new Observation();

        this.time = 0;

        // cria um listener de conexao, e adiciona no client
        this.client.addChangeStatusListener(new ConnectionListener(this.client));

        // cria um listener de commando, passando o bot para atualizar minhas variaveis
        this.client.addCommandListener(new GameListener(this));

        // inicia a conexao
        this.client.connect(Config.url);

        tela = new View(this);

        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Cria um novo bot, rodando as configuracoes padrao
     */
    public Bot() {
        config();
    }

    public void dormir(int ms) {
        Field.doTick(ms);
        if (ms < 0) { return; }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void limparObservacaoes() {
        this.ultimaObservacao.reset();
    }

    public void exibirScore() {
        System.out.println("==== SCOREBOARD ====");
        System.out.printf("Time: %d\n", time);
        System.out.printf("Status: %s\n", state.toString());
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
    // public long getScore() { return this.score; }
    public int getEnergy() { return this.energy; }
    public int getTick() { return this.tick; }
    public PlayerInfo.Direction getDir() { return this.dir; }
    public Observation getUltimaObservacao() { return this.ultimaObservacao; }
    /* FIM DAS IMPLEMENTACOES DO IBOT */

    /**
     * Roda o bot
     */
    public void run() {
        int timer = 0;
        long tempoExec;
        boolean emPartida = false;

        dormir(Config.timerDefault);

        while (true) {
            switch (state) {
                case game -> {
                    // caso seja o inicio da partida
                    if (!emPartida) {
                        this.tick = 0;
                        emPartida = true;
                        this.sender.pedirStatus();
                        this.sender.pedirObservacao();
                        dormir(Config.timerRapido);
                    }
                    this.tick += 1;
                    tempoExec = System.currentTimeMillis();

                    // faz a acao
                    Action acao = this.ai.pensar();
                    this.sender.enviarAction(acao);

                    // atualiza as coisas
                    this.tela.repaint();

                    // pede as proximas coisas
                    limparObservacaoes();

                    this.sender.pedirObservacao();
                    this.sender.pedirStatus();
                    this.sender.pedirStatusGame();

                    this.thinkingTime = (int) (System.currentTimeMillis() - tempoExec);
                    Bot.tickAtual = (int) System.currentTimeMillis();

                    dormir(Config.timerRapido);
                }
                case dead, ready, gameover -> {
                    emPartida = false;
                    Field.init();
                    if (timer == 5) {
                        this.sender.pedirScoreboard();
                        this.sender.pedirStatus();
                        exibirScore();
                        timer = 0;
                    }
                    timer += 1;

                    this.sender.pedirStatusGame();
                    dormir(Config.timerDefault);
                }
            }
            this.log.printLast();
        }
    }
}
