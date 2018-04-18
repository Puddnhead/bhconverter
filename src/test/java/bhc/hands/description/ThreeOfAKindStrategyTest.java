package bhc.hands.description;

import bhc.domain.Hand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for three of a kind strategy
 *
 * Created by MVW on 4/16/2018.
 */
public class ThreeOfAKindStrategyTest {

    ThreeOfAKindStrategy strategy = new ThreeOfAKindStrategy();

    @Test
    public void testThreeOfAKindStrategy() throws Exception {
        Hand hand = new Hand("[8h 8s 8c 2s 3d]", "(Three of a kind)");
        strategy.convertBovadaDescription(hand);
        assertEquals("three of a kind, Eights", hand.getPokerStarsDescription());
    }
}