package bhc.domain;

import java.util.Map;

/**
 * Pojo for holding some information we need to keep track of while processing a hand
 *
 * Created by MVW on 4/17/2018.
 */
public class HandContext {
    private Map<String, String> playerMap;
    private double currentBet;

    public HandContext(Map<String, String> playerMap, double currentBet) {
        this.playerMap = playerMap;
        this.currentBet = currentBet;
    }

    public Map<String, String> getPlayerMap() {
        return playerMap;
    }

    public double getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(double currentBet) {
        this.currentBet = currentBet;
    }
}
