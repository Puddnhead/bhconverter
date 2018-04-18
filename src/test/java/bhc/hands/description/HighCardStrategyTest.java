package bhc.hands.description;

import bhc.domain.Hand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for high card strategy
 *
 * Created by MVW on 4/16/2018.
 */
public class HighCardStrategyTest {

    private HighCardStrategy highCardStrategy = new HighCardStrategy();

    @Test
    public void testHighCardStrategy() throws Exception {
        Hand hand = new Hand("[Kh Js Th 4s 3d]", "(High Card)");
        highCardStrategy.convertBovadaDescription(hand);
        assertEquals("high card King", hand.getPokerStarsDescription());
    }
}