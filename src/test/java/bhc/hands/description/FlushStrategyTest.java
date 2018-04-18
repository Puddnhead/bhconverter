package bhc.hands.description;

import bhc.domain.Hand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Flush strategy unit test
 *
 * Created by MVW on 4/16/2018.
 */
public class FlushStrategyTest {

    private FlushStrategy strategy = new FlushStrategy();

    @Test
    public void testFlushStrategy() throws Exception {
        Hand hand = new Hand("[Qs Js 8s 5s 2s]", "(Flush)");
        strategy.convertBovadaDescription(hand);
        assertEquals("a flush, Queen high", hand.getPokerStarsDescription());
    }
}