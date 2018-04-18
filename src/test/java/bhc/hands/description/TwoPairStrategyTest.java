package bhc.hands.description;

import bhc.domain.Hand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for two pair strategy
 *
 * Created by MVW on 4/16/2018.
 */
public class TwoPairStrategyTest {

    TwoPairStrategy twoPairStrategy = new TwoPairStrategy();

    @Test
    public void testTwoPairStrategy() throws Exception {
        Hand hand = new Hand("[6h 6s 2h 2s 3d]", "(Two pair)");
        twoPairStrategy.convertBovadaDescription(hand);
        assertEquals("two pair Sixes and Deuces", hand.getPokerStarsDescription());
    }
}