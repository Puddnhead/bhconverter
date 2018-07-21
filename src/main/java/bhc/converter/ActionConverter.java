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
    private static final Pattern postingActionPattern = Pattern.compile("(.*) : (.*) (\\$?\\d+(\\.\\d\\d)?)");
    private static final Pattern handActionPattern = Pattern.compile("(.*) : (\\S+)");
    private static final Pattern uncalledPortionReturnPattern = Pattern.compile(".* : Return uncalled portion of bet (\\$?.*)$");
    private static final Pattern allinActionPattern = Pattern.compile(".* : All-in(\\(timeout\\))? \\$?(\\d+(\\.\\d\\d)?)");
    private static final Pattern allinRaiseActionPattern =
            Pattern.compile(".* : All-in\\(raise(-timeout)?\\) \\$?(\\d+(\\.\\d\\d)?) to \\$?(\\d+(\\.\\d\\d)?)");
    private static final Pattern betActionPattern = Pattern.compile(".* : Bets \\$(.*)$");
    private static final Pattern raisesActionPattern = Pattern.compile(".* : Raises .* to \\$?(.*)$");


    // Small Blind : Small Blind $0.05
    // Big Blind : Big blind $0.10
    // UTG+1  [ME] : Posts chip $0.10
    public static String convertPostingAction(String action, Map<String, String> playerMap) {
        String result = action;
        Matcher postingActionMatcher = postingActionPattern.matcher(action);
        if (postingActionMatcher.find()) {
            String playerName = postingActionMatcher.group(1);
            String bovadaActionStr = postingActionMatcher.group(2);

            String transformedName = playerMap.get(playerName);
            // escape and [ '+' for UTG+1 etc
            playerName = playerName.replace("+", "\\+").replace("[", "\\[");
            HandAction bovadaAction = HandAction.fromString(bovadaActionStr);

            switch (bovadaAction) {
                case BOVADA_SMALL_BLIND_ACTION:
                case BOVADA_SMALL_BLIND_ACTION2:
                    result = action.replaceFirst(playerName + " ", transformedName)
                            .replace(bovadaActionStr, HandAction.POKERSTARS_SMALL_BLIND_ACTION.getAction()).trim();
                    break;
                case BOVADA_BIG_BLIND_ACTION:
                case BOVADA_POSTING_ACTION:
                case BOVADA_DEAD_CHIP_ACTION:
                    result = action.replaceFirst(playerName + " ", transformedName)
                            .replace(bovadaActionStr, HandAction.POKERSTARS_BIG_BLIND_ACTION.getAction()).trim();
                    break;
                case BOVADA_ANTE_CHIP_ACTION:
                    result = action.replaceFirst(playerName + " ", transformedName)
                            .replace(bovadaActionStr, HandAction.POKERSTARS_ANTE_ACTION.getAction()).trim();
                    break;
                case BOVADA_ALL_IN_ACTION:
                case BOVADA_ALL_IN_TIMEOUT_ACTION:
                    result = action.replaceFirst(playerName + " ", transformedName)
                            .replace(bovadaActionStr, HandAction.POKERSTARS_BIG_BLIND_ACTION.getAction()).trim()
                            + " and is all-in";
                    break;
                default:
                    SystemUtils.logError("Unrecognized posting action: " + bovadaAction, Optional.empty());
            }
        }

        return result;
    }

    public static String convertHandAction(String action, HandContext handContext) {
        String transformedAction = action;
        Matcher handActionMatcher = handActionPattern.matcher(action);
        if (handActionMatcher.find()) {
            String playerName = handActionMatcher.group(1);
            String bovadaActionStr = handActionMatcher.group(2);

            String transformedName = handContext.getPlayerMap().get(playerName);
            transformedAction = transformedAction.replace(playerName + " ", transformedName);

            HandAction bovadaAction = HandAction.fromString(bovadaActionStr);

            if (bovadaAction == null) {
                SystemUtils.logError("Unrecognized hand action: " + bovadaActionStr, Optional.empty());
                return transformedAction;
            }

            // sloppy but default to Raise because the grouping is a bit off
            switch (bovadaAction) {
                case BOVADA_CALL_ACTION:
                case BOVADA_CALL_ACTION2:
                case BOVADA_CALL_TIMEOUT_ACTION:
                    transformedAction = transformedAction.replace(bovadaActionStr, HandAction.POKERSTARS_CALL_ACTION.getAction());
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
                case BOVADA_FOLD_DISCONNECT_ACTION:
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
                    transformedAction = transformedAction.replace(bovadaActionStr, HandAction.POKERSTARS_RAISE_ACTION.getAction());
                    break;
                case BOVADA_BET_ACTION:
                    Matcher betMatcher = betActionPattern.matcher(action);
                    if (betMatcher.find()) {
                        double currentBet = Double.parseDouble(betMatcher.group(1));
                        handContext.setCurrentBet(currentBet);
                    }
                    transformedAction = transformedAction.replace(bovadaActionStr, HandAction.POKERSTARS_BET_ACTION.getAction());
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
                case BOVADA_LEAVE_ACTION:
                    // just ignore these lines - don't care about people sitting down or standing up
                    break;
            }
        }

        return transformedAction;
    }
}
