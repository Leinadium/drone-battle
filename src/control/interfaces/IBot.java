package control.interfaces;

import INF1771_GameClient.Dto.PlayerInfo;
import control.drone.Observation;

public interface IBot {
    int getX();
    int getY();
    // long getScore();
    int getEnergy();
    int getTick();
    PlayerInfo.Direction getDir();
    Observation getUltimaObservacao();
}
