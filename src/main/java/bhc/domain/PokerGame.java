package bhc.domain;

/**
 * Pojo for storing some info about a poker game
 *
 * Created by MVW on 4/18/2018.
 */
public class PokerGame {

    private boolean isTournament;

    private double fixedSmallBlind;

    private double fixedLargeBlind;

    private String entryFee;

    private String tableNumber;

    private String tournamentNumber;

    /**
     * Cash game constructor
     */
    public PokerGame(double fixedSmallBlind, double fixedLargeBlind, String tableNumber) {
        this.isTournament = false;
        this.fixedSmallBlind = fixedSmallBlind;
        this.fixedLargeBlind = fixedLargeBlind;
        this.tableNumber = tableNumber;
        this.tournamentNumber = null;
        this.entryFee = null;
    }

    /**
     * Tournament game constructor
     */
    public PokerGame(String entryFee, String tournamentNumber) {
        this.isTournament = true;
        this.entryFee = entryFee;
        this.tournamentNumber = tournamentNumber;
        this.fixedLargeBlind = 0;
        this.fixedLargeBlind = 0;
        this.tableNumber = null;
    }

    public boolean isTournament() {
        return isTournament;
    }

    public double getFixedSmallBlind() {
        return fixedSmallBlind;
    }

    public double getFixedLargeBlind() {
        return fixedLargeBlind;
    }

    public String getBlinds() {
        if (fixedSmallBlind <= 0 && fixedLargeBlind <= 0) {
            return null;
        }

        return String.format("($%.2f/$%.2f)", fixedSmallBlind, fixedLargeBlind);
    }

    public String getEntryFee() {
        return entryFee;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getTournamentNumber() {
        return tournamentNumber;
    }
}
