package control.interfaces;

import INF1771_GameClient.Dto.PlayerInfo;
import control.enums.Action;
import control.enums.Observation;

import java.util.ArrayList;

public interface IBot {
    int getX();
    int getY();
    // long getScore();
    int getEnergy();
    int getTick();
    PlayerInfo.Direction getDir();
    ArrayList<Observation> getUltimaObservacao();
}
