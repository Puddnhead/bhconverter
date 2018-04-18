package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Strategy for converting flushes
 *
 * Created by MVW on 4/16/2018.
 */
public class FlushStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        String highCard = hand.getFiveCardHand().substring(1, 4);
        String highRank = RankMapper.getRank(highCard);
        hand.setPokerStarsDescription("a flush, " + highRank + " high");
    }
}
