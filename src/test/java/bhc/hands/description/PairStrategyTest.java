package bhc.hands.description;

import bhc.domain.Hand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for pair strategy
 *
 * Created by MVW on 4/16/2018.
 */
public class PairStrategyTest {

    private PairStrategy pairStrategy = new PairStrategy();

    @Test
    public void testPairStrategy() throws Exception {
        Hand hand = new Hand("[Jh Js Th 4s 3d]", "(One pair)");
        pairStrategy.convertBovadaDescription(hand);
        assertEquals("a pair of Jacks", hand.getPokerStarsDescription());
    }
}