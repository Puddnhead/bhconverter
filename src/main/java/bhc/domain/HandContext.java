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
    private boolean isCashGame;
    private boolean currentBetIsBigBlind;

    public HandContext(Map<String, String> playerMap, double bigBlind, boolean isCashGame) {
        this.playerMap = playerMap;
        this.currentBet = bigBlind;
        this.currentBetIsBigBlind = true;
        this.isCashGame = isCashGame;
    }

    public Map<String, String> getPlayerMap() {
        return playerMap;
    }

    public double getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(double currentBet) {
        this.currentBet = currentBet;
        this.currentBetIsBigBlind = false;
    }

    public boolean isCashGame() {
        return isCashGame;
    }

    public boolean currentBetEqualsBigBlind() {
        return currentBetIsBigBlind;
    }
}
