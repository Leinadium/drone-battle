package control.drone;

import INF1771_GameClient.Dto.PlayerInfo;
import control.enums.Action;
import control.enums.Position;
import control.enums.State;
import control.interfaces.IBot;
import control.map.Field;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class AI {

    IBot bot;
    public State estadoAtual;
    public Action acaoAtual;
    int tickFugir;                      // ticks que representam quanto tempo ele esta fugindo
    Scanner scanner = new Scanner(System.in);

    public float pesoExplorar;
    public float pesoAtacar;
    public float pesoFugir;
    public float pesoRecarregar;
    public float pesoColetar;

    Random random = new Random();

    public AI(Bot bot) {
        this.bot = bot;
    }


    public Action pensarRoubado() {
        estadoAtual = calcularEstado();

        String s = scanner.nextLine();
        if (s.equals("w")) { return Action.FRENTE; }
        if (s.equals("d")) { return Action.DIREITA; }
        if (s.equals("a")) { return Action.ESQUERDA; }
        if (s.equals("s")) { return Action.TRAS; }
        if (s.equals("q")) { return Action.PEGAR; }
        if (s.equals(" ")) { return Action.ATIRAR; }
        return Action.PEGAR;
    }

    /**
     * Cérebro da AI. Calcula o estado e retorna a ação a ser executada
     * @return Ação a ser feita
     */
    public Action pensar() {
        estadoAtual = calcularEstado();
        switch (estadoAtual) {
            case FUGIR -> doFugir();
            case ATACAR -> doAtacar();
            case COLETAR -> doColetar();
            case EXPLORAR -> doExplorar();
            case RECARREGAR -> doRecarregar();
        }
        return acaoAtual;
    }

    /**
     * Atualiza o mapa a partir das observações atuais
     */
    public void atualizarMapa() {
        PlayerInfo.Direction dir = this.bot.getDir();
        int x = this.bot.getX();
        int y = this.bot.getY();
        boolean ehVazio = true;
        boolean ehPerigo = false;
        boolean ehParede = false;
        Observation o = this.bot.getUltimaObservacao();

        if (o.isFlash || o.isBuraco) {
            Field.setAround(x, y, dir, Position.DANGER);
            ehPerigo = true;
        }
        if (o.isParede) {
            Field.setFront(x, y, dir, Position.PAREDE);
            ehParede = true;
        }
        if (o.isPowerup) {
            Field.set(x, y, Position.POWERUP);
            Field.setPowerup(x, y, this.bot.getTick());
            ehVazio = false;
        }
        if (o.isTesouro) {
            Field.set(x, y, Position.OURO);
            Field.setOuro(x, y, this.bot.getTick());
            ehVazio = false;
        }

        if (!ehPerigo) {
            Field.setAround(x, y, dir, Position.SAFE);
            if (!ehParede) {
                Field.setFront(x, y, dir, Position.SAFE);
            }
        }
        if (ehVazio) {
            Field.set(x, y, Position.EMPTY);
        }
    }

    /**
     * Calcula qual o estado do drone.
     * A descrição completa está no README
     * @return O novo estado do drone
     */
    private State calcularEstado() {
        Observation o = bot.getUltimaObservacao();
        int e = bot.getEnergy();
        int t = bot.getTick();

        // excecao a regra, se tiver um ouro coleta imediatamente. Sempre vale a pena
        if (o.isTesouro) {
            return State.COLETAR;
        }

        // outra excecao, ele tem que fugir 5 vezes
        if (tickFugir > 0) {
            tickFugir -= 1;
            return State.FUGIR;
        }

        // verificando atacar
        if (o.isInimigoFrente && e > 30) {
            return State.ATACAR;
        }

        // verificando fugir
        if ((o.isDano && !(o.isInimigo || o.isInimigoFrente))
                || ((o.isInimigoFrente || o.isInimigo) && e <= 30 )) {
            tickFugir = 1;
            return State.FUGIR;
        }

        // verificando recuperar
        if (e <= 30) {
            return State.RECARREGAR;
        }

        // verificando coletar
        if (Field.hasOuro() && Field.hasOuroParaColetar(bot.getX(), bot.getY(), bot.getTick())) {
            return State.COLETAR;
        }

        // verificando exploracao
        return State.EXPLORAR;
    }

    /**
     * Calcula as ações para o estado ATACAR
     * Neste caso, atira uma vez
     */
    private void doAtacar() {
        acaoAtual = Action.ATIRAR;
    }

    /**
     * Calcula as ações para o estado FUGIR
     * Define o tickFugir para 5 (ou seja, ele vai executar pelo menos 5 acoes)
     *
     * Caso tome dano sem ter inimigo perto, fugir para algum bloco exceto
     *      tras, esquerda ou direita
     * Caso tenha um inimigo a sua volta, fugir do quadrado 3x3 que ele esta
     * Caso tenha um inimigo a sua frente, fugir da linha de visão dele
     */
    private void doFugir() {
        tickFugir = 5;
        Field.Path caminho = null, temp;

        Observation o = bot.getUltimaObservacao();
        PlayerInfo.Direction dir = bot.getDir();
        int [][] area;

        // primeiro caso
        if (o.isDano && !(o.isInimigo || o.isInimigoFrente)) {
            area = Field.coords3x3Front(bot.getX(), bot.getY(), dir);
        }
        // segundo caso
        else if (o.isInimigo) {
            area = Field.coords5x5Around(bot.getX(), bot.getY());
        }
        // terceiro caso
        else {
            area = Field.coords5x2Sides(bot.getX(), bot.getY(), dir);
        }

        // limpando coords impossiveis
        for (int[] coord: area) {
            if ((Field.get(coord[0], coord[1]) == Position.PAREDE)
                    || (Field.get(coord[0], coord[1])) == Position.DANGER) {
                continue;
            }
            temp = Field.aStar(bot.getX(), bot.getY(), coord[0], coord[1]);
            if ((caminho == null && temp != null) || caminho.tamanho > temp.tamanho) {
                caminho = temp;
            }
        }

        // passando a primeira acao
        if (caminho == null) {
            doExplorar();
        } else {
            acaoAtual = caminho.acoes[0];
        }
    }

    /**
     * Calcula as ações para o estado COLETAR
     * Vai no caminho mais rapido para um OURO
     */
    private void doColetar() {
        // como foi rodado o hasOuroParaColetar, é possível pegar o path do buffer
        acaoAtual = Field.getBufferPath().acoes[0];
    }

    /**
     * Calcula as ações para o estado RECARREGAR
     * Caso tenha um powerup para coletar, siga até ele.
     * Se não, se tiver algum powerup, explora em volta dele
     * Se não tiver powerup, explora
     */
    private void doRecarregar() {
        if (Field.hasPowerupParaColetar(bot.getX(), bot.getY(), bot.getTick())) {
            // pega o path que tem no buffer dele
            acaoAtual = Field.getBufferPath().acoes[0];
            return;
        }
        if (Field.hasPowerup()) {
            /* TODO */
        }
        else {
            doExplorar();
        }
    }

    /**
     * Calcula as ações para o estado EXPLORAR
     * Explicado no README.md
     */
    private void doExplorar() {

    }
}
