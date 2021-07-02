package control.drone;


public class Observation {
    public boolean isInimigo = false;
    public boolean isBuraco = false;
    public boolean isFlash = false;
    public boolean isPowerup = false;
    public boolean isTesouro = false;
    public boolean isParede = false;
    public boolean isAcerto = false;
    public boolean isDano = false;
    public boolean isInimigoFrente = false;
    public int distanciaInimigoFrente = -1;

    public void print() {
        String s = "";
        if (isInimigoFrente) {s += "ENEMY|";}
        if (isBuraco) {s += "BURACO|";}
        if (isFlash) {s += "FLASH|";}
        if (isPowerup) {s += "POWERUP|";}
        if (isParede) {s += "PAREDE|";}
        if (isAcerto) {s += "ACERTO|";}
        if (isDano) {s += "DANO|";}
        if (isInimigo) {s += "ACERTO|";}
        System.out.println(s);
    }
}
