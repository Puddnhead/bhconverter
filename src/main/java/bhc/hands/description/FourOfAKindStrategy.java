package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Strategy for converting four of a kind hands
 *
 * Created by MVW on 4/16/2018.
 */
public class FourOfAKindStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        String card = hand.getFiveCardHand().substring(1, 4);
        String rank = RankMapper.getPluralRank(card);
        hand.setPokerStarsDescription("four of a kind, " + rank);
    }
}
