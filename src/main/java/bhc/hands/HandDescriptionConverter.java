package bhc.hands;

import bhc.domain.Hand;
import bhc.util.SystemUtils;
import bhc.hands.description.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Converts a bovada hand description to a pokerstars hand description
 *
 * Created by MVW on 4/16/2018.
 */
class HandDescriptionConverter {

    private static Map<HandValue, HandDescriptionStrategy> strategyMap;

    static {
        strategyMap = new HashMap<>();
        strategyMap.put(HandValue.HIGH_CARD, new HighCardStrategy());
        strategyMap.put(HandValue.PAIR, new PairStrategy());
        strategyMap.put(HandValue.TWO_PAIR, new TwoPairStrategy());
        strategyMap.put(HandValue.THREE_OF_A_KIND, new ThreeOfAKindStrategy());
        strategyMap.put(HandValue.STRAIGHT, new StraightStrategy());
        strategyMap.put(HandValue.FLUSH, new FlushStrategy());
        strategyMap.put(HandValue.FULL_HOUSE, new FullHouseStrategy());
        strategyMap.put(HandValue.FOUR_OF_A_KIND, new FourOfAKindStrategy());
        strategyMap.put(HandValue.STRAIGHT_FLUSH, new StraightFlushStrategy());
        strategyMap.put(HandValue.ROYAL_FLUSH, new RoyalFlushStrategy());
    }

    void convert(Hand hand) {
        HandValue handValue = HandValue.fromBovadaValue(hand.getBovadaDescription());
        if (handValue == null) {
            SystemUtils.logError("Error mapping bovada hand value", Optional.empty());
        }

        HandDescriptionStrategy strategy = strategyMap.get(handValue);
        if (strategy == null) {
            SystemUtils.logError(
                    "Could not find a strategy for mapping description of " + handValue.name(), Optional.empty());
        }
        strategy.convertBovadaDescription(hand);
    }
}
