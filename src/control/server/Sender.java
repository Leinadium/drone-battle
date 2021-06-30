package control.server;

import java.awt.Color;


import INF1771_GameClient.Socket.HandleClient;
import control.enums.Action;

public class Sender {

    HandleClient client;

    /**
     * Classe para enviar os comandos
     * @param client
     */
    public Sender(HandleClient client) {
        this.client = client;
    }

    /**
     * Envia a mensagem do comando para o client
     * @param s Mensagem a ser enviada
     */
    private void enviarMensagem(String s) {
        if (s.trim().length() > 0) {
            this.client.sendSay(s);
        }
    }

    /**
     * Envia a decisao tomada para o client
     * @param action Ação tomada
     */
    public void enviarAction(Action action) {
        switch (action) {
            case DIREITA -> client.sendTurnRight();
            case ESQUERDA -> client.sendTurnLeft();
            case FRENTE -> client.sendForward();
            case TRAS -> client.sendBackward();
            case PEGAR -> client.sendGetItem();
            case ATIRAR -> client.sendShoot();
        }
    }

    /**
     * Pede uma atualizacao da scoreboard
     */
    public void pedirScoreboard() { this.client.sendRequestScoreboard(); }

    /**
     * Pede para atualizar o status do jogador
     */
    public void pedirStatus() {
        this.client.sendRequestUserStatus();
    }

    /**
     * Pede para atualizar o status do jogo
     */
    public void pedirStatusGame() { this.client.sendRequestGameStatus(); }

    /**
     * Pede para fazer uma observação a sua volta
     */
    public void pedirObservacao() {
        this.client.sendRequestObservation();
    }

    /**
     * Pede para trocar o nome
     */
    public void pedirNovoNome(String name) {
        this.client.sendName(name);
    }

    /**
     * Pede para trocar de cor
     */
    public void pedirCor(Color cor) {
        this.client.sendColor(cor);
    }


}
