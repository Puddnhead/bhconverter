package bhc.hands;

/**
 * Enum representing a hand value
 *
 * Created by MVW on 4/16/2018.
 */
public enum HandValue {

    HIGH_CARD("(High Card)"),
    PAIR("(One pair)"),
    TWO_PAIR("(Two pair)"),
    THREE_OF_A_KIND("(Three of a kind)"),
    STRAIGHT("(Straight)"),
    FLUSH("(Flush)"),
    FULL_HOUSE("(Full House)"),
    FOUR_OF_A_KIND("(Four of a kind)"),
    STRAIGHT_FLUSH("(Straight Flush)");

    private String bovadaValue;

    HandValue(String bovadaValue) {
        this.bovadaValue = bovadaValue;
    }

    public String getBovadaValue() {
        return bovadaValue;
    }

    public static HandValue fromBovadaValue(String bovadaValue) {
        for (HandValue handValue: HandValue.values()) {
            if (handValue.getBovadaValue().equals(bovadaValue)) {
                return handValue;
            }
        }

        return null;
    }
}
