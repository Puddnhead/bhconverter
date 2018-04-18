package bhc.hands.description;

import bhc.domain.Hand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for full house strategy
 *
 * Created by MVW on 4/16/2018.
 */
public class FullHouseStrategyTest {

    private FullHouseStrategy strategy = new FullHouseStrategy();

    @Test
    public void testFullHouseStrategy() throws Exception {
        Hand hand = new Hand("[Th Ts Tc As Ad]", "(Two pair)");
        strategy.convertBovadaDescription(hand);
        assertEquals("a full house, Tens full of Aces", hand.getPokerStarsDescription());
    }
}