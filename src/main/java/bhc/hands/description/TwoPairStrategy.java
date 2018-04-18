package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Strategy for converting two pair hands
 *
 * Created by MVW on 4/16/2018.
 */
public class TwoPairStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        String pair1 = hand.getFiveCardHand().substring(1, 4);
        String pair2 = hand.getFiveCardHand().substring(7, 10);
        String rank1 = RankMapper.getPluralRank(pair1);
        String rank2 = RankMapper.getPluralRank(pair2);

        hand.setPokerStarsDescription("two pair, " + rank1 + " and " + rank2);
    }
}
