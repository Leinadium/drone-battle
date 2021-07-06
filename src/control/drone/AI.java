package control.drone;

import INF1771_GameClient.Dto.PlayerInfo;
import control.enums.Action;
import control.enums.Position;
import control.enums.State;
import control.interfaces.IBot;
import control.map.Field;
import control.map.Path;

public class AI {

    IBot bot;
    public State estadoAtual;

    public Action acaoAtual;
    int tickFugir = 0;
    int ticksAtacando = 0;
    boolean mapaMudou;
    int xBuffer;
    int yBuffer;
    boolean indoParaPowerup;

    // para exploracao
    private State estadoAnterior;
    public Path pathAtual;


    public AI(IBot bot) {
        this.bot = bot;
        estadoAnterior = null;
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
            case FUGIR -> fazerFugir();
            case ATACAR -> fazerAtaque();
            case COLETAR -> fazerColetar();
            case EXPLORAR -> fazerExplorar();
            case RECARREGAR -> fazerRecarregar();
        }

        if (estadoAtual != State.ATACAR) {ticksAtacando = 0;}
        this.xBuffer = bot.getX();
        this.yBuffer = bot.getY();

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

        mapaMudou = false;
        indoParaPowerup = false;

        // quando uma observacao de flash eh perdida, pode ocorrer de andar em um deles
        // logo, isso garante que o flash seja atualizado mesmo se eu andar nele
        // como o servidor de vez em qdo trava, > 1 pode ter uns falsos positivos
        if (Math.abs(x - xBuffer) + Math.abs(y - yBuffer) > 3) {
            Field.setForce(xBuffer, yBuffer, Position.PERIGO);
            mapaMudou = true;
        }

        // se o drone tomou tiro, coloca a posicao como insegura para ele fugir dali
        if (o.isDano) {
            Field.setPosicaoInsegura(x, y);
            Field.setPosicaoInsegura(x - 1, y);
            Field.setPosicaoInsegura(x - 2, y);

            Field.setPosicaoInsegura(x + 1, y);
            Field.setPosicaoInsegura(x + 2, y);

            Field.setPosicaoInsegura(x, y - 1);
            Field.setPosicaoInsegura(x, y - 2);

            Field.setPosicaoInsegura(x, y + 1);
            Field.setPosicaoInsegura(x, y + 2);
        }

        if (o.isFlash || o.isBuraco) {
            Field.setAoRedor(x, y, Position.PERIGO);
            ehPerigo = true;
            mapaMudou = true;
        }
        if (o.isParede) {
            if (acaoAtual == Action.FRENTE) Field.setFrente(x, y, dir, Position.PAREDE);
            else Field.setAtras(x, y, dir, Position.PAREDE);
            ehParede = true;
            mapaMudou = true;
        }
        if (o.isPowerup) {
            Field.removerSeguro(x, y);
            Field.set(x, y, Position.POWERUP);
            Field.setPowerup(x, y);
            ehVazio = false;
        }
        if (o.isTesouro) {
            Field.removerSeguro(x, y);
            Field.set(x, y, Position.OURO);
            Field.setOuro(x, y);
            ehVazio = false;
        }

        if (!ehPerigo) {
            Field.setAoRedor(x, y, Position.SEGURO);
            if (!ehParede) {
                if (acaoAtual == Action.FRENTE) Field.setFrente(x, y, dir, Position.SEGURO);
                else Field.setAtras(x, y, dir, Position.SEGURO);
            }
        }
        if (ehVazio) {
            Field.removerSeguro(x, y);
            Field.set(x, y, Position.VAZIO);
            Field.deveriaTerOuroOuPowerupAqui(x, y);
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

        // se tiver um ouro coleta imediatamente. Sempre vale a pena
        if (o.isTesouro) {
            return State.COLETAR;
        }

        // se tiver um powerup, pega
        if (o.isPowerup && e <= 70) {
            return State.RECARREGAR;
        }

        // ele tem que fugir 5 vezes
        if (tickFugir > 0) {
            tickFugir -= 1;
           return State.FUGIR;
        }

        // verificando atacar
        if (o.isInimigoFrente && ticksAtacando < 10 && e > 30
                && !Field.temParedeNaFrente(bot.getX(), bot.getY(), bot.getDir(), o.distanciaInimigoFrente)) {
            return State.ATACAR;
        }

        // verificando fugir
        if ((o.isDano && !o.isInimigoFrente)
                || ((o.isInimigoFrente || o.isInimigo) && e <= 30)) {
            tickFugir = 5;
            return State.FUGIR;
        }

        // verificando recuperar
        if (e <= 80) {
            return State.RECARREGAR;
        }

        // verificando coletar
        if (Field.temOuro() && Field.temOuroParaColetar(bot.getX(), bot.getY(), bot.getDir())) {
            return State.COLETAR;
        }

        // verificando exploracao
        return State.EXPLORAR;
    }

    /**
     * Calcula as ações para o estado ATAQUE
     * Neste caso, atira uma vez
     */
    private void fazerAtaque() {
        ticksAtacando += 1;
        acaoAtual = Action.ATIRAR;
    }

    /**
     * Calcula as ações para o estado FUGIR
     * Define o tickFugir para 5 (ou seja, ele vai executar pelo menos 5 acoes)
     *
     * Caso tome dano sem ter inimigo perto, fugir da linha de visão dele
     * Caso tenha um inimigo a sua volta, virar
     * Caso tenha um inimigo a sua frente, fugir da linha de visão dele
     */
    private void fazerFugir() {
        Path caminho = null, temp;

        Observation o = bot.getUltimaObservacao();
        PlayerInfo.Direction dir = bot.getDir();
        int [][] area;

        if (this.pathAtual != null && estadoAnterior == State.FUGIR && tickFugir > 0 && pathAtual.tamanho > 1) {
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
                    || (Field.get(coord[0], coord[1])) == Position.PERIGO) {
                continue;
            }
            temp = Field.aEstrela(bot.getX(), bot.getY(), bot.getDir(), coord[0], coord[1]);
            if (temp == null) { continue; }
            if (caminho == null || caminho.tamanho > temp.tamanho) {
                caminho = temp;
            }
        }

        // passando a primeira acao
        if (caminho == null) {
            fazerAtaque();     // se defende se nao tem como fugir
        } else {
            pathAtual = caminho;
            acaoAtual = pathAtual.acoes[0];
        }
    }

    /**
     * Calcula as ações para o estado COLETAR
     * Vai no caminho mais rapido para um OURO
     */
    private void fazerColetar() {
        if (this.bot.getUltimaObservacao().isTesouro) {
            acaoAtual = Action.PEGAR;
            return;
        }

        // buffer
        if (this.pathAtual != null &&  estadoAnterior == State.COLETAR && !this.mapaMudou && pathAtual.tamanho > 1) {
            pathAtual.removerPrimeiraAcao();
            acaoAtual = pathAtual.acoes[0];
            return;
        }

        // como foi rodado o hasOuroParaColetar, é possível pegar o path do buffer
        pathAtual = Field.getBufferPath();
        //  System.out.println(Arrays.toString(pathAtual.acoes));

        if (pathAtual == null ) {
            acaoAtual = Action.NADA;
        } else {
            acaoAtual =  pathAtual.acoes[0];
        }
    }

    /**
     * Calcula as ações para o estado RECARREGAR
     * Caso tenha um powerup para coletar, siga até ele.
     * Se não, se tiver algum powerup, explora em volta dele
     * Se não tiver powerup, explora
     *
     * PROBLEMA CONHECIDO: se aparecer um powerup enquanto ele explora, ele continuará explorando
     * até acabar aquele quadrado, e só depois irá para o powerup
     */
    private void fazerRecarregar() {
        if (this.bot.getUltimaObservacao().isPowerup) {
            acaoAtual = Action.PEGAR;
            return;
        }

        if (Field.temPowerupParaColetar(bot.getX(), bot.getY(), bot.getDir()) && !indoParaPowerup) {
            this.mapaMudou = true;      // forca a recarregar o buffer
        }

        // cache
        if (this.pathAtual != null && estadoAnterior == State.RECARREGAR && !this.mapaMudou && pathAtual.tamanho > 1) {
            pathAtual.removerPrimeiraAcao();
            acaoAtual = pathAtual.acoes[0];
            return;
        }

        // procurando algum powerup pronto
        if (Field.temPowerupParaColetar(bot.getX(), bot.getY(), bot.getDir())) {
            // pega o path que tem no buffer dele
            indoParaPowerup = true;
            pathAtual = Field.getBufferPath();
            acaoAtual = pathAtual.acoes[0];
            return;
        }
        // procurando algum powerup para explorar em volta
        if (Field.temPowerup()) {
            int []dest = Field.powerupMaisPerto(bot.getX(), bot.getY(), bot.getDir());
            if (dest != null) {
                int xx = dest[0];
                int yy = dest[1];
                dest = Field.melhorBlocoUsandoPontoFocal(bot.getX(), bot.getY(),bot.getDir(), xx, yy);
                pathAtual = Field.aEstrela(bot.getX(), bot.getY(), bot.getDir(), dest[0], dest[1]);
                if (pathAtual != null) acaoAtual = pathAtual.acoes[0];
                indoParaPowerup = false;
            } else {
                // algo bugou?
                System.out.println("RECARREGAR BUG?");
                fazerExplorar();
            }
        }
        // caso tudo de errado, ou nao tenha powerups
        else {
            fazerExplorar();
        }
    }

    /**
     * Calcula as ações para o estado EXPLORAR
     */
    private void fazerExplorar() {

        if (this.pathAtual != null && estadoAnterior == State.EXPLORAR && !this.mapaMudou && pathAtual.tamanho > 1) {
            // retirando a primeira ação do path e pegando a proxima
            pathAtual.removerPrimeiraAcao();
            acaoAtual = pathAtual.acoes[0];

            // System.out.println(Arrays.toString(pathAtual.acoes));

            return;
        }

        int[] pontoFocal = Field.pontoMedioOuro();
        int[] destino = Field.melhorBlocoUsandoPontoFocal(bot.getX(), bot.getY(), bot.getDir(), pontoFocal[0], pontoFocal[1]);

        pathAtual = Field.aEstrela(bot.getX(), bot.getY(), bot.getDir(), destino[0], destino[1]);

        if (pathAtual == null) {
            acaoAtual = Action.NADA;
        }
        else {
            // System.out.println(Arrays.toString(pathAtual.acoes));
            acaoAtual = pathAtual.acoes[0];
        }
    }
}
