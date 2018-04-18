package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Strategy for converting Pair hands
 *
 * Created by MVW on 4/16/2018.
 */
public class PairStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        String pairCard = hand.getFiveCardHand().substring(1, 4);
        String rank = RankMapper.getPluralRank(pairCard);
        hand.setPokerStarsDescription("a pair of " + rank);
    }
}
