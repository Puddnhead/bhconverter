package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Strategy for converting three of a kind hands
 *
 * Created by MVW on 4/16/2018.
 */
public class ThreeOfAKindStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        String card = hand.getFiveCardHand().substring(1, 4);
        String rank = RankMapper.getPluralRank(card);
        hand.setPokerStarsDescription("three of a kind, " + rank);
    }
}
