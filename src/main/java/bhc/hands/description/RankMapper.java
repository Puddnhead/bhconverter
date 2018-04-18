package bhc.hands.description;

/**
 * Spits out a word given a card rank
 *
 * Created by MVW on 4/16/2018.
 */
public class RankMapper {

    public static String getRank(String card) {
        String rank = "";

        switch (card.charAt(0)) {
            case 'A':
                rank = "Ace";
                break;
            case '2':
                rank = "Deuce";
                break;
            case '3':
                rank = "Three";
                break;
            case '4':
                rank = "Four";
                break;
            case '5':
                rank = "Five";
                break;
            case '6':
                rank = "Six";
                break;
            case '7':
                rank = "Seven";
                break;
            case '8':
                rank = "Eight";
                break;
            case '9':
                rank = "Nine";
                break;
            case 'T':
                rank = "Ten";
                break;
            case 'J':
                rank = "Jack";
                break;
            case 'Q':
                rank = "Queen";
                break;
            case 'K':
                rank = "King";
                break;
        }

        return rank;
    }

    public static String getPluralRank(String card) {
        String rank = getRank(card);
        if (rank.equals("Six")) {
            rank += "es";
        } else {
            rank += "s";
        }

        return rank;
    }
}
