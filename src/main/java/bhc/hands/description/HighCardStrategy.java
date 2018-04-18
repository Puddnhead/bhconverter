package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Strategy for converting high card descriptions
 *
 * Created by MVW on 4/16/2018.
 */
public class HighCardStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        String highCard, fiveCardHand = hand.getFiveCardHand();
        if (fiveCardHand != null) {
            highCard = hand.getFiveCardHand().substring(1, 4);
        }  else {
            highCard = hand.getTwoCardHand().substring(1, 4);
        }
        String rank = RankMapper.getRank(highCard);
        hand.setPokerStarsDescription("high card " + rank);
    }
}
