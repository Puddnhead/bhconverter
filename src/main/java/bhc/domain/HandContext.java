package bhc.domain;

import java.util.Map;

/**
 * Pojo for holding some information we need to keep track of while processing a hand
 *
 * Created by MVW on 4/17/2018.
 */
public class HandContext {
    private Map<String, String> seatMap;
    private double currentBet;

    public HandContext(Map<String, String> seatMap, double currentBet) {
        this.seatMap = seatMap;
        this.currentBet = currentBet;
    }

    public Map<String, String> getSeatMap() {
        return seatMap;
    }

    public double getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(double currentBet) {
        this.currentBet = currentBet;
    }
}
