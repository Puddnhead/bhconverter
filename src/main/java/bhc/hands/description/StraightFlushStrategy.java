package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Strategy for converting straight flushes
 *
 * Created by MVW on 4/16/2018.
 */
public class StraightFlushStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        String highCard = hand.getFiveCardHand().substring(1, 4);
        String lowCard = hand.getFiveCardHand().substring(13, 16);
        String highRank = RankMapper.getRank(highCard);
        String lowRank = RankMapper.getRank(lowCard);
        hand.setPokerStarsDescription("a straight flush, " + lowRank + " to " + highRank);
    }
}
