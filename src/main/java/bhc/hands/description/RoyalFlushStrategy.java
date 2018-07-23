package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Royal flush strategy
 *
 * Created by MVW on 7/23/2018.
 */
public class RoyalFlushStrategy implements HandDescriptionStrategy {

    @Override
    public void convertBovadaDescription(Hand hand) {
        hand.setPokerStarsDescription("a royal flush");
    }
}
