package bhc.hands.description;

import bhc.domain.Hand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for straight strategy
 *
 * Created by MVW on 4/16/2018.
 */
public class StraightStrategyTest {

    private StraightStrategy straightStrategy = new StraightStrategy();

    @Test
    public void testStraightStrategy() throws Exception {
        Hand hand = new Hand("[9h 8s 7h 6s 5d]", "(Straight)");
        straightStrategy.convertBovadaDescription(hand);
        assertEquals("a straight, Five to Nine", hand.getPokerStarsDescription());
    }
}