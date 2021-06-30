package control.enums;

public enum Event {
    CAIR(-1000),
    MORRER(-10),
    MATAR(1000),
    ATIRAR(-10),
    PEGAR(-5),
    ;

    private final int custo;

    private Event(int custo) {
        this.custo = custo;
    }

    public int getCusto() {
        return this.custo;
    }
}
