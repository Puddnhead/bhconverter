package bhc.hands.description;

import bhc.domain.Hand;

/**
 * Represents a strategy for converting a bovada hand description to a pokerstars hand description
 *
 * Created by MVW on 4/16/2018.
 */
public interface HandDescriptionStrategy {

    void convertBovadaDescription(Hand hand);
}
