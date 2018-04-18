package bhc.domain;

/**
 * POJO to contain a two-card hand, the best 5-card hand, and a hand value
 *
 * Created by MVW on 4/16/2018.
 */
public class Hand {

    private String twoCardHand;

    private String fiveCardHand;

    private String bovadaDescription;

    private String pokerStarsDescription;

    public Hand(String fiveCardHand, String bovadaDescription) {
        this.fiveCardHand = fiveCardHand;
        this.bovadaDescription = bovadaDescription;
    }

    public String getTwoCardHand() {
        return twoCardHand;
    }

    public void setTwoCardHand(String twoCardHand) {
        this.twoCardHand = twoCardHand;
    }

    public String getFiveCardHand() {
        return fiveCardHand;
    }

    public String getBovadaDescription() {
        return bovadaDescription;
    }

    public String getPokerStarsDescription() {
        return pokerStarsDescription;
    }

    public void setPokerStarsDescription(String pokerStarsDescription) {
        this.pokerStarsDescription = pokerStarsDescription;
    }
}
