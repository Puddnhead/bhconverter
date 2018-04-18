package bhc.hands.description;

import bhc.domain.Hand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Four of a kind unit test
 *
 * Created by MVW on 4/16/2018.
 */
public class FourOfAKindStrategyTest {

    private FourOfAKindStrategy strategy = new FourOfAKindStrategy();

    @Test
    public void testFourOfAKindStrategy() throws Exception {
        Hand hand = new Hand("[9h 9s 9c 9d Ad]", "(Four of a kind)");
        strategy.convertBovadaDescription(hand);
        assertEquals("four of a kind, Nines", hand.getPokerStarsDescription());
    }
}