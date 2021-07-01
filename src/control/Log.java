package control;

import java.util.ArrayList;
import java.util.List;

public class Log {

    private final List<String> lista;
    private int indiceAtual;

    public Log() {
        lista = new ArrayList<>();
        indiceAtual = 0;
    }

    public void add(String s) {
        lista.add(s);
    }

    /**
     * Exibe as mensagens ate a ultima chamada dessa funcao
     */
    public void printLast() {
        for (int i = indiceAtual; i < lista.size(); i ++) {
            System.out.println(lista.get(i));
        }
        indiceAtual = lista.size();
    }

    public void dump() {
        for (String s: lista) {
            System.out.println(s);
        }
    }
}
