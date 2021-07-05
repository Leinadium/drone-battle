package control.drone;

import INF1771_GameClient.Dto.PlayerInfo;
import control.enums.Action;
import control.enums.Position;
import control.enums.State;
import control.interfaces.IBot;
import control.map.Field;
import control.map.Path;

import java.util.Arrays;
import java.util.Scanner;

public class AI {

    IBot bot;
    public State estadoAtual;

    public Action acaoAtual;
    int tickFugir = 0;
    int ticksAtacando = 0;
    Scanner scanner = new Scanner(System.in);

    // para exploracao
    private State estadoAnterior;
    public Path pathAtual;


    public AI(Bot bot) {
        this.bot = bot;
        estadoAnterior = null;
    }


    public Action pensarRoubado() {
        estadoAtual = calcularEstado();
        atualizarMapa();

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
        atualizarMapa();

        estadoAnterior = estadoAtual;
        estadoAtual = calcularEstado();

        switch (estadoAtual) {
            case FUGIR -> doFugir();
            case ATACAR -> doAtacar();
            case COLETAR -> doColetar();
            case EXPLORAR -> doExplorar();
            case RECARREGAR -> doRecarregar();
        }

        if (estadoAtual != State.ATACAR) {ticksAtacando = 0;}

        return acaoAtual;
    }

    /**
     * Atualiza o mapa a partir das observações atuais
     */
    private void atualizarMapa() {
        PlayerInfo.Direction dir = this.bot.getDir();
        int x = this.bot.getX();
        int y = this.bot.getY();
        boolean ehVazio = true;
        boolean ehPerigo = false;
        boolean ehParede = false;
        Observation o = this.bot.getUltimaObservacao();

        if (o.isFlash || o.isBuraco) {
            Field.setAround(x, y, Position.DANGER);
            ehPerigo = true;
        }
        if (o.isParede) {
            if (acaoAtual == Action.FRENTE) Field.setFront(x, y, dir, Position.PAREDE);
            else Field.setBack(x, y, dir, Position.PAREDE);
            ehParede = true;
        }
        if (o.isPowerup) {
            Field.removeSafe(x, y);
            Field.set(x, y, Position.POWERUP);
            Field.setPowerup(x, y);
            ehVazio = false;
        }
        if (o.isTesouro) {
            Field.removeSafe(x, y);
            Field.set(x, y, Position.OURO);
            Field.setOuro(x, y);
            ehVazio = false;
        }

        if (!ehPerigo) {
            Field.setAround(x, y, Position.SAFE);
            if (!ehParede) {
                if (acaoAtual == Action.FRENTE) Field.setFront(x, y, dir, Position.SAFE);
                else Field.setBack(x, y, dir, Position.SAFE);
            }
        }
        if (ehVazio) {
            Field.removeSafe(x, y);
            Field.set(x, y, Position.EMPTY);
            Field.shouldThereBeGoldOrPowerupHere(x, y);
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

        // excecao a regra, se tiver um ouro coleta imediatamente. Sempre vale a pena
        if (o.isTesouro) {
            return State.COLETAR;
        }

        // outra excecao, se tiver um powerup e tiver perdido um pouco de vida, pega
        if (o.isPowerup && e <= 70) {
            return State.RECARREGAR;
        }

        // outra excecao, ele tem que fugir 5 vezes
        if (tickFugir > 0) {
            tickFugir -= 1;
            return State.FUGIR;
        }

        // verificando atacar
        if (o.isInimigoFrente && e > 30 && ticksAtacando < 10
                && !Field.hasParedeInFront(bot.getX(), bot.getY(), bot.getDir(), o.distanciaInimigoFrente)) {
            return State.ATACAR;
        }

        // verificando fugir
        if ((o.isDano && !(o.isInimigo || o.isInimigoFrente))
                || ((o.isInimigoFrente || o.isInimigo) && e <= 30 )) {
            tickFugir = 5;
            return State.FUGIR;
        }

        // verificando recuperar
        if (e <= 50) {
            return State.RECARREGAR;
        }

        // verificando coletar
        if (Field.hasOuro() && Field.hasOuroParaColetar(bot.getX(), bot.getY(), bot.getDir(), bot.getTick())) {
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
        ticksAtacando += 1;
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
        Path caminho = null, temp;

        Observation o = bot.getUltimaObservacao();
        PlayerInfo.Direction dir = bot.getDir();
        int [][] area;

        if (estadoAnterior == State.FUGIR && tickFugir > 0 && pathAtual.tamanho > 1) {
            pathAtual.removerPrimeiraAcao();
            acaoAtual = pathAtual.acoes[0];
            return;
        }

        // segundo caso
        if (o.isInimigo) {
            // area = Field.coords5x5Around(bot.getX(), bot.getY());
            // simplesmente olha para o lado
            acaoAtual = Action.ESQUERDA;
            return;

        }
        // primeiro e terceiro caso
        else {
            area = Field.coords5x2Sides(bot.getX(), bot.getY(), dir);
        }

        // limpando coords impossiveis
        for (int[] coord: area) {
            if ((Field.get(coord[0], coord[1]) == Position.PAREDE)
                    || (Field.get(coord[0], coord[1])) == Position.DANGER) {
                continue;
            }
            temp = Field.aStar(bot.getX(), bot.getY(), bot.getDir(), coord[0], coord[1]);
            if (temp == null) { continue; }
            if (caminho == null || caminho.tamanho > temp.tamanho) {
                caminho = temp;
            }
        }

        // passando a primeira acao
        if (caminho == null) {
            doAtacar();     // se defende se nao tem como fugir
        } else {
            pathAtual = caminho;
            acaoAtual = pathAtual.acoes[0];
        }
    }

    /**
     * Calcula as ações para o estado COLETAR
     * Vai no caminho mais rapido para um OURO
     */
    private void doColetar() {
        if (this.bot.getUltimaObservacao().isTesouro) {
            acaoAtual = Action.PEGAR;
            return;
        }

        if (estadoAnterior == State.COLETAR && !Field.mapaMudou && pathAtual.tamanho > 1) {
            pathAtual.removerPrimeiraAcao();
            acaoAtual = pathAtual.acoes[0];
            // System.out.println(Arrays.toString(pathAtual.acoes));
            return;
        }

        // como foi rodado o hasOuroParaColetar, é possível pegar o path do buffer
        pathAtual = Field.getBufferPath();
        System.out.println(Arrays.toString(pathAtual.acoes));

        if (pathAtual == null ) {
            acaoAtual = Action.NONE;
            System.out.println("=========== FAZENDO NADA? =========");
        } else {
            acaoAtual =  pathAtual.acoes[0];
        }
    }

    /**
     * Calcula as ações para o estado RECARREGAR
     * Caso tenha um powerup para coletar, siga até ele.
     * Se não, se tiver algum powerup, explora em volta dele
     * Se não tiver powerup, explora
     */
    private void doRecarregar() {
        if (this.bot.getUltimaObservacao().isPowerup) {
            acaoAtual = Action.PEGAR;
            return;
        }

        // cache
        if (estadoAnterior == State.RECARREGAR && !Field.mapaMudou && pathAtual.tamanho > 1) {
            pathAtual.removerPrimeiraAcao();
            acaoAtual = pathAtual.acoes[0];
            return;
        }

        // procurando algum powerup pronto
        if (Field.hasPowerupParaColetar(bot.getX(), bot.getY(), bot.getDir(), bot.getTick())) {
            // pega o path que tem no buffer dele
            pathAtual = Field.getBufferPath();
            acaoAtual = pathAtual.acoes[0];
            return;
        }
        // procurando algum powerup para explorar em volta
        if (Field.hasPowerup()) {

            // TODO: pegar as coordenadas do lugar em vez de fazer o path inteiro
            Field.powerupMaisProximo(bot.getX(), bot.getY(), bot.getDir());
            int xx, yy;
            int[] dest;
            xx = Field.getBufferPath().xDest;       // crashou
            yy = Field.getBufferPath().yDest;
            dest = Field.melhorBlocoUsandoPontoFocal(bot.getX(), bot.getY(),bot.getDir(), xx, yy);
            pathAtual = Field.aStar(bot.getX(), bot.getY(), bot.getDir(), dest[0], dest[1]);
            if (pathAtual != null) acaoAtual = pathAtual.acoes[0];
        }
        // se nao tiver nenhum powerup
        else {
            doExplorar();
        }
    }

    /**
     * Calcula as ações para o estado EXPLORAR
     * Explicado no README.md
     */
    private void doExplorar() {

        if (estadoAnterior == State.EXPLORAR && !Field.mapaMudou && pathAtual.tamanho > 1) {
            // retirando a primeira ação do path e pegando a proxima
            pathAtual.removerPrimeiraAcao();
            acaoAtual = pathAtual.acoes[0];

            // System.out.println(Arrays.toString(pathAtual.acoes));

            return;
        }

        int[] pontoFocal = Field.pontoMedioOuro();
        int[] destino = Field.melhorBlocoUsandoPontoFocal(bot.getX(), bot.getY(), bot.getDir(), pontoFocal[0], pontoFocal[1]);

        pathAtual = Field.aStar(bot.getX(), bot.getY(), bot.getDir(), destino[0], destino[1]);

        if (pathAtual == null) {
            acaoAtual = Action.NONE;
        }
        else {
            // System.out.println(Arrays.toString(pathAtual.acoes));
            acaoAtual = pathAtual.acoes[0];
        }
    }
}
