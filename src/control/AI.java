package control;

import INF1771_GameClient.Dto.PlayerInfo;
import control.enums.Action;
import control.enums.Observation;
import control.enums.Position;
import control.enums.State;
import control.interfaces.IBot;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class AI {

    IBot bot;
    public State estadoAtual;
    ArrayList<Action> acoesEstado;      // lista de acoes a serem tomadas para aquele estado
    int tickFugir;
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
        estadoAtual = calcularPesos();

        String s = scanner.nextLine();
        if (s.equals("w")) { return Action.FRENTE; }
        if (s.equals("d")) { return Action.DIREITA; }
        if (s.equals("a")) { return Action.ESQUERDA; }
        if (s.equals("s")) { return Action.TRAS; }
        if (s.equals("q")) { return Action.PEGAR; }
        if (s.equals(" ")) { return Action.ATIRAR; }
        return Action.PEGAR;
    }

    public Action pensar() {
        State novoEstado = calcularPesos();
        // TODO: calcular fila de acoes

        // o codigo a seguir eh temporario
        int x = random.nextInt(6);
        Action[] acoes = {
                Action.ATIRAR, Action.FRENTE, Action.TRAS,
                Action.DIREITA, Action.ESQUERDA, Action.PEGAR};

        return acoes[x];
    }

    public void atualizarMapa() {
        PlayerInfo.Direction dir = this.bot.getDir();
        int x = this.bot.getX();
        int y = this.bot.getY();
        boolean ehVazio = true;
        boolean ehPerigo = false;
        boolean ehParede = false;
        for (Observation o: this.bot.getUltimaObservacao()) {
            switch (o) {
                case FLASH, BURACO -> {
                    Field.setAround(x, y, dir, Position.DANGER);
                    ehPerigo = true;
                }
                case PAREDE -> {
                    Field.setFront(x, y, dir, Position.PAREDE);
                    ehParede = true;
                }
                case POWERUP -> {
                    Field.set(x, y, Position.POWERUP);
                    Field.setPowerup(x, y, this.bot.getTick());
                    ehVazio = false;
                }
                case TESOURO -> {
                    Field.set(x, y, Position.OURO);
                    Field.setOuro(x, y, this.bot.getTick());
                    ehVazio = false;
                }
            }
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
     * Calcula os pesos de cada estado a partir das observações atuais do Bot.
     * @return estado do bot a ser considerado
     */
    private State calcularPesos() {
        calcularFugir();
        calcularRecarregar();
        calcularColetar();
        calcularAtacar();
        calcularExplorar();

        if (pesoFugir >= pesoRecarregar &&
                pesoFugir >= pesoColetar &&
                pesoFugir >= pesoAtacar &&
                pesoFugir >= pesoExplorar
        ) { return State.FUGIR; }

        if (pesoRecarregar >= pesoFugir &&
                pesoRecarregar >= pesoColetar &&
                pesoRecarregar >= pesoAtacar &&
                pesoRecarregar >= pesoExplorar
        ) { return State.RECARREGAR; }

        if (pesoColetar >= pesoRecarregar &&
                pesoColetar >= pesoFugir &&
                pesoColetar >= pesoAtacar &&
                pesoColetar >= pesoExplorar
        ) { return State.COLETAR; }

        if (pesoAtacar >= pesoRecarregar &&
                pesoAtacar >= pesoColetar &&
                pesoAtacar >= pesoFugir &&
                pesoAtacar >= pesoExplorar
        ) { return State.ATACAR; }

        if (pesoExplorar >= pesoRecarregar &&
                pesoExplorar >= pesoColetar &&
                pesoExplorar >= pesoAtacar &&
                pesoExplorar >= pesoFugir
        ) { return State.EXPLORAR; }

        // se nao caiu nenhum deles, escolhe um aleatorio
        System.out.println("ESCOLHENDO UM ALEATORIO!");
        State[] escolhas = {State.EXPLORAR, State.ATACAR, State.COLETAR, State.FUGIR, State.COLETAR};
        int x = random.nextInt(5);
        return escolhas[x];
    }


    /**
     * Calcula o peso de explorar
     */
    private void calcularExplorar() {
        this.pesoExplorar =  1 - this.pesoColetar;
    }

    /**
     * Calcula o peso de atacar
     */
    private void calcularAtacar() {
        if (
                this.bot.getUltimaObservacao().contains(Observation.INIMIGOFRENTE) ||
                this.bot.getUltimaObservacao().contains(Observation.INIMIGO)
        ) {
            this.pesoAtacar =  1 - this.pesoFugir;
        }
        else { this.pesoAtacar = 0; }

    }

    /**
     * Calcula o peso de fugir
     */
    private void calcularFugir() {
        int tickAtual = this.bot.getTick();
        if (estadoAtual == State.FUGIR && tickAtual - tickFugir < 10) {
            this.pesoFugir = 1;
            return;
        }

        if (
                !this.bot.getUltimaObservacao().contains(Observation.INIMIGOFRENTE)
                        && !this.bot.getUltimaObservacao().contains(Observation.INIMIGO)
        ) {
            pesoFugir = 0;
            return;
        }

        // usando uma formula diferente, para quando nao se sabe a energia inimiga
        float energiaAtual = this.bot.getEnergy();
        if (energiaAtual < 30) {
            this.pesoFugir = 1;
        }
        else if (energiaAtual > 70) {
            this.pesoFugir = 0;
        }
        else {
            this.pesoFugir =  (1 - (energiaAtual - 30) / 40);
        }
    }

    /**
     * Calcula o peso de recarregar
     */
    private void calcularRecarregar() {
        float energiaAtual = this.bot.getEnergy();
        if (energiaAtual < 10) {
            this.pesoRecarregar = 1;
        } else if (energiaAtual < 90) {
            this.pesoRecarregar = (1 - (energiaAtual - 10) / 80);
        } else {
            this.pesoRecarregar =  0;
        }
    }

    /**
     * Calcula o peso de coletar
     */
    private void calcularColetar() {
        float[] distancias = Field.getDistanceGold(bot.getX(), bot.getY(), bot.getTick());
        if (distancias == null) {
            pesoColetar = 0;
            return;
        }

        int i = 0;
        float soma = 0;
        for (float d: distancias) {
            soma += (2 / Math.PI * 0.5 * Math.atan(d) + 0.5);
            i++;
        }
        pesoColetar = soma/i;
    }
}
