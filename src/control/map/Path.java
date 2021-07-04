package control.map;

import control.enums.Action;

/**
 * Classe para retorno do algoritmo de pathfinder
 */
public class Path {
    public Action[] acoes;
    public int tamanho;
    public int xDest;
    public int yDest;

    public void removerPrimeiraAcao() {
        Action[] novaAcoes = new Action[this.tamanho - 1];
        System.arraycopy(this.acoes, 1, novaAcoes, 0, this.tamanho-1);
        this.acoes = novaAcoes;
        this.tamanho -= 1;
    }
}