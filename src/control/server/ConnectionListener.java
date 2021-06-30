package control.server;

import INF1771_GameClient.Socket.*;
import control.Config;

public class ConnectionListener implements CommandListener{
    HandleClient client;
    public ConnectionListener(HandleClient client) {
        this.client = client;
    }

    public void receiveCommand(String[] cmd) {
        if (this.client.connected) {
            System.out.println("Conectado!");
            this.client.sendName(Config.nomeJogador);       // envia meu nome
            this.client.sendColor(Config.corDefault);       // envia minha cor
            this.client.sendRequestGameStatus();            // pega o status
            this.client.sendRequestUserStatus();            // me atualiza
            this.client.sendRequestObservation();           // da uma observacao
        }
        else {
            System.out.println("Desconectado!");
        }
    }
}
