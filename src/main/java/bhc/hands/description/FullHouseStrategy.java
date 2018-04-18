package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Strategy for converting flushes
 *
 * Created by MVW on 4/16/2018.
 */
public class FullHouseStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        String card1 = hand.getFiveCardHand().substring(1, 4);
        String card2 = hand.getFiveCardHand().substring(10, 13);
        String rank1 = RankMapper.getPluralRank(card1);
        String rank2 = RankMapper.getPluralRank(card2);
        hand.setPokerStarsDescription("a full house, " + rank1 + " full of " + rank2);
    }
}
