package bhc.converter;

import bhc.domain.HandContext;
import bhc.util.SystemUtils;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some methods for transforming actions
 *
 * Created by MVW on 4/15/2018.
 */
public class ActionConverter {
    // Small Blind : Small Blind $0.05
    // Big Blind : Big blind $0.10
    // UTG+1  [ME] : Posts chip $0.10
    private static final Pattern postingActionPattern = Pattern.compile("(.*) : (.*) (\\$?\\d+(\\.\\d\\d)?)");
    private static final Pattern handActionPattern = Pattern.compile("(.*) : (\\S+)");
    private static final Pattern uncalledPortionReturnPattern = Pattern.compile(".* : Return uncalled portion of bet (\\$?.*)$");
    private static final Pattern allinActionPattern = Pattern.compile(".* : All-in(\\(timeout\\))? \\$?(\\d+(\\.\\d\\d)?)");
    private static final Pattern allinRaiseActionPattern =
            Pattern.compile(".* : All-in\\(raise(-timeout)?\\) \\$?(\\d+(\\.\\d\\d)?) to \\$?(\\d+(\\.\\d\\d)?)");
    private static final Pattern betActionPattern = Pattern.compile(".* : Bets \\$(.*)$");
    private static final Pattern raisesActionPattern = Pattern.compile(".* : Raises .* to \\$?(.*)$");

    // Posting actions
    private static final String BOVADA_SMALL_BLIND_ACTION = "Small Blind";
    private static final String BOVADA_SMALL_BLIND_ACTION2 = "Small blind";
    private static final String BOVADA_BIG_BLIND_ACTION = "Big blind";
    private static final String BOVADA_POSTING_ACTION = "Posts chip";
    private static final String BOVADA_DEAD_CHIP_ACTION = "Posts dead chip";
    private static final String BOVADA_ANTE_CHIP_ACTION = "Ante chip";

    private static final String POKERSTARS_SMALL_BLIND_ACTION = "posts small blind";
    private static final String POKERSTARS_BIG_BLIND_ACTION = "posts big blind";
    private static final String POKERSTARS_ANTE_ACTION = "posts the ante";

    // Hand actions
    private static final String BOVADA_CALL_ACTION = "Calls";
    private static final String BOVADA_CALL_ACTION2 = "Call";
    private static final String BOVADA_CALL_TIMEOUT_ACTION = "Call(timeout)";
    private static final String BOVADA_CHECK_ACTION = "Checks";
    private static final String BOVADA_CHECK_TIMEOUT_ACTION = "Checks(timeout)";
    private static final String BOVADA_CHECK_DISCONNECT_ACTION = "Checks(disconnect)";
    private static final String BOVADA_RAISE_ACTION = "Raises";
    private static final String BOVADA_RAISE_TIMEOUT_ACTION = "Raises(timeout)";
    private static final String BOVADA_FOLD_ACTION = "Folds";
    private static final String BOVADA_FOLD_BLIND_DISCONNECTED_ACTION = "Fold(Blind";
    private static final String BOVADA_FOLD_TIMEOUT_ACTION = "Folds(timeout)";
    private static final String BOVADA_FOLD_AUTH_DISCONNECT_ACTION = "Folds(auth-disconnect)";
    private static final String BOVADA_FOLD_AUTH_ACTION = "Folds(auth)";
    private static final String BOVADA_BET_ACTION = "Bets";
    private static final String BOVADA_RETURN_ACTION = "Return";
    private static final String BOVADA_DOES_NOT_SHOW_ACTION = "Does";
    private static final String BOVADA_ALL_IN_RAISE_ACTION = "All-in(raise)";
    private static final String BOVADA_ALL_IN_RAISE_TIMEOUT_ACTION = "All-in(raise-timeout)";
    private static final String BOVADA_ALL_IN_ACTION = "All-in";
    private static final String BOVADA_ALL_IN_TIMEOUT_ACTION = "All-in(timeout)";
    private static final String BOVADA_SEAT_STAND_ACTION = "Seat";
    private static final String BOVADA_TABLE_ACTION = "Table";
    private static final String BOVADA_SIT_OUT_ACTION = "Sit";
    private static final String BOVADA_REJOIN_ACTION = "Re-join";

    private static final String POKERSTARS_CALL_ACTION = "calls";
    private static final String POKERSTARS_BET_ACTION = "bets";
    private static final String POKERSTARS_RAISE_ACTION = "raises";

    public static String convertPostingAction(String action, Map<String, String> playerMap) {
        Matcher postingActionMatcher = postingActionPattern.matcher(action);
        if (postingActionMatcher.find()) {
            String playerName = postingActionMatcher.group(1);
            String bovadaAction = postingActionMatcher.group(2);

            String transformedName = playerMap.get(playerName);
            // escape and [ '+' for UTG+1 etc
            playerName = playerName.replace("+", "\\+").replace("[", "\\[");
            String pokerstarsAction = "";
            switch (bovadaAction) {
                case BOVADA_SMALL_BLIND_ACTION:
                case BOVADA_SMALL_BLIND_ACTION2:
                    pokerstarsAction = POKERSTARS_SMALL_BLIND_ACTION;
                    break;
                case BOVADA_BIG_BLIND_ACTION:
                    pokerstarsAction = POKERSTARS_BIG_BLIND_ACTION;
                    break;
                case BOVADA_POSTING_ACTION:
                    pokerstarsAction = POKERSTARS_BIG_BLIND_ACTION;
                    break;
                case BOVADA_DEAD_CHIP_ACTION:
                    pokerstarsAction = POKERSTARS_BIG_BLIND_ACTION;
                    break;
                case BOVADA_ANTE_CHIP_ACTION:
                    pokerstarsAction = POKERSTARS_ANTE_ACTION;
                    break;
                default:
                    SystemUtils.exitProgramWithError("Unrecognized posting action: " + bovadaAction, Optional.empty());
            }

            return action.replaceFirst(playerName + " ", transformedName).replace(bovadaAction, pokerstarsAction).trim();
        }

        return action;
    }

    public static String convertHandAction(String action, HandContext handContext) {
        String transformedAction = action;
        Matcher handActionMatcher = handActionPattern.matcher(action);
        if (handActionMatcher.find()) {
            String playerName = handActionMatcher.group(1);
            String bovadaAction = handActionMatcher.group(2);

            String transformedName = handContext.getPlayerMap().get(playerName);
            transformedAction = transformedAction.replace(playerName + " ", transformedName);

            // sloppy but default to Raise because the grouping is a bit off
            switch (bovadaAction) {
                case BOVADA_CALL_ACTION:
                case BOVADA_CALL_ACTION2:
                case BOVADA_CALL_TIMEOUT_ACTION:
                    transformedAction = transformedAction.replace(bovadaAction, POKERSTARS_CALL_ACTION);
                    break;
                case BOVADA_CHECK_ACTION:
                case BOVADA_CHECK_TIMEOUT_ACTION:
                case BOVADA_CHECK_DISCONNECT_ACTION:
                    transformedAction = transformedName + ": checks";
                    break;
                case BOVADA_FOLD_ACTION:
                case BOVADA_FOLD_TIMEOUT_ACTION:
                case BOVADA_FOLD_BLIND_DISCONNECTED_ACTION:
                case BOVADA_FOLD_AUTH_DISCONNECT_ACTION:
                case BOVADA_FOLD_AUTH_ACTION:
                    transformedAction = transformedName + ": folds";
                    break;
                case BOVADA_RAISE_ACTION:
                case BOVADA_RAISE_TIMEOUT_ACTION:
                    Matcher raiseMatcher = raisesActionPattern.matcher(action);
                    if (raiseMatcher.find()) {
                        double currentBet = Double.parseDouble(raiseMatcher.group(1));
                        handContext.setCurrentBet(currentBet);
                    }
                    transformedAction = transformedAction.replace(bovadaAction, POKERSTARS_RAISE_ACTION);
                    break;
                case BOVADA_BET_ACTION:
                    Matcher betMatcher = betActionPattern.matcher(action);
                    if (betMatcher.find()) {
                        double currentBet = Double.parseDouble(betMatcher.group(1));
                        handContext.setCurrentBet(currentBet);
                    }
                    transformedAction = transformedAction.replace(BOVADA_BET_ACTION, POKERSTARS_BET_ACTION);
                    break;
                case BOVADA_RETURN_ACTION:
                    Matcher uncalledPortionMatcher = uncalledPortionReturnPattern.matcher(action);
                    if (uncalledPortionMatcher.find()) {
                        String amount = uncalledPortionMatcher.group(1).trim();
                        transformedAction = "Uncalled bet (" + amount + ") returned to " + transformedName;
                    }
                    break;
                case BOVADA_DOES_NOT_SHOW_ACTION:
                    transformedAction = transformedName + ": doesn't show hand";
                    break;
                case BOVADA_ALL_IN_ACTION:
                case BOVADA_ALL_IN_TIMEOUT_ACTION:
                    Matcher allinMatcher = allinActionPattern.matcher(action);
                    if (allinMatcher.find()) {
                        String allinValue = allinMatcher.group(2);
                        double allinDouble = Double.parseDouble(allinValue);
                        String allinAction = "calls ";
                        if (allinDouble > handContext.getCurrentBet()) {
                            allinAction = "bets ";
                            handContext.setCurrentBet(allinDouble);
                        }
                        String dollarSign = handContext.isCashGame() ? "$" : "";
                        transformedAction = transformedName + ": " + allinAction + dollarSign + allinValue + " and is all-in";
                    }
                    break;
                case BOVADA_ALL_IN_RAISE_ACTION:
                case BOVADA_ALL_IN_RAISE_TIMEOUT_ACTION:
                    Matcher allinRaiseMatcher = allinRaiseActionPattern.matcher(action);
                    if (allinRaiseMatcher.find()) {
                        String raiseValue = allinRaiseMatcher.group(2);
                        String allinValue = allinRaiseMatcher.group(4);
                        String dollarSign = handContext.isCashGame() ? "$" : "";
                        if (raiseValue.equals(allinValue) && handContext.currentBetEqualsBigBlind()) {
                            transformedAction = transformedName + ": bets " + dollarSign + raiseValue + " and is all-in";
                        } else {
                            transformedAction = transformedName + ": raises " + dollarSign + raiseValue + " to " +
                                    dollarSign + allinValue + " and is all-in";
                        }
                        double allinDouble = Double.parseDouble(allinValue);
                        handContext.setCurrentBet(allinDouble);
                    }
                    break;
                case BOVADA_SEAT_STAND_ACTION:
                case BOVADA_TABLE_ACTION:
                case BOVADA_SIT_OUT_ACTION:
                case BOVADA_REJOIN_ACTION:
                    // just ignore these lines - don't care about people sitting down or standing up
                    break;
                default:
                    SystemUtils.exitProgramWithError("Unrecognized hand action: " + bovadaAction, Optional.empty());
            }
        }

        return transformedAction;
    }
}
