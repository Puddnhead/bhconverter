package bhc.converter;

/**
 * Hand action enum
 * 
 * Created by MVW on 4/29/2018.
 */
public enum HandAction {

    // Posting actions
    BOVADA_SMALL_BLIND_ACTION("Small Blind"),
    BOVADA_SMALL_BLIND_ACTION2("Small blind"),
    BOVADA_BIG_BLIND_ACTION("Big blind"),
    BOVADA_POSTING_ACTION("Posts chip"),
    BOVADA_DEAD_CHIP_ACTION("Posts dead chip"),
    BOVADA_ANTE_CHIP_ACTION("Ante chip"),

    POKERSTARS_SMALL_BLIND_ACTION("posts small blind"),
    POKERSTARS_BIG_BLIND_ACTION("posts big blind"),
    POKERSTARS_ANTE_ACTION("posts the ante"),

    // Hand actions
    BOVADA_CALL_ACTION("Calls"),
    BOVADA_CALL_ACTION2("Call"),
    BOVADA_CALL_TIMEOUT_ACTION("Call(timeout)"),
    BOVADA_CHECK_ACTION("Checks"),
    BOVADA_CHECK_TIMEOUT_ACTION("Checks(timeout)"),
    BOVADA_CHECK_DISCONNECT_ACTION("Checks(disconnect)"),
    BOVADA_RAISE_ACTION("Raises"),
    BOVADA_RAISE_TIMEOUT_ACTION("Raises(timeout)"),
    BOVADA_FOLD_ACTION("Folds"),
    BOVADA_FOLD_BLIND_DISCONNECTED_ACTION("Fold(Blind"),
    BOVADA_FOLD_TIMEOUT_ACTION("Folds(timeout)"),
    BOVADA_FOLD_AUTH_DISCONNECT_ACTION("Folds(auth-disconnect)"),
    BOVADA_FOLD_DISCONNECT_ACTION("Folds(disconnect)"),
    BOVADA_FOLD_AUTH_ACTION("Folds(auth)"),
    BOVADA_BET_ACTION("Bets"),
    BOVADA_RETURN_ACTION("Return"),
    BOVADA_DOES_NOT_SHOW_ACTION("Does"),
    BOVADA_ALL_IN_RAISE_ACTION("All-in(raise)"),
    BOVADA_ALL_IN_RAISE_TIMEOUT_ACTION("All-in(raise-timeout)"),
    BOVADA_ALL_IN_ACTION("All-in"),
    BOVADA_ALL_IN_TIMEOUT_ACTION("All-in(timeout)"),
    BOVADA_SEAT_STAND_ACTION("Seat"),
    BOVADA_TABLE_ACTION("Table"),
    BOVADA_SIT_OUT_ACTION("Sit"),
    BOVADA_REJOIN_ACTION("Re-join"),
    BOVADA_LEAVE_ACTION("Leave(Auto)"),

    POKERSTARS_CALL_ACTION("calls"),
    POKERSTARS_BET_ACTION("bets"),
    POKERSTARS_RAISE_ACTION("raises");

    HandAction(String action) {
        this.action = action;
    }

    private String action;
    
    public String getAction() {
        return action;
    }

    public static HandAction fromString(String action) {
        for (HandAction handAction: HandAction.values()) {
            if (handAction.getAction().equals(action)) {
                return handAction;
            }
        }
        return null;
    }
}
