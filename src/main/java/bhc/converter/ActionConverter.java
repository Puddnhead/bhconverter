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
    private static final Pattern postingActionPattern = Pattern.compile("(.*) : (.*) (\\$.*)");
    private static final Pattern handActionPattern = Pattern.compile("(.*) : (\\S+)");
    private static final Pattern uncalledPortionReturnPattern = Pattern.compile(".* : Return uncalled portion of bet (\\$.*)$");
    private static final Pattern betActionPattern = Pattern.compile(".* : Bets \\$(.*)$");
    private static final Pattern allinActionPattern = Pattern.compile(".* : All-in \\$(.*)$");
    private static final Pattern allinRaiseActionPattern = Pattern.compile(".* : All-in\\(raise\\) .* to \\$(.*)$");
    private static final Pattern raisesActionPattern = Pattern.compile(".* : Raises .* to \\$(.*)$");

    // Posting actions
    private static final String BOVADA_SMALL_BLIND_ACTION = "Small Blind";
    private static final String BOVADA_BIG_BLIND_ACTION = "Big blind";
    private static final String BOVADA_POSTING_ACTION = "Posts chip";
    private static final String BOVADA_DEAD_CHIP_ACTION = "Posts dead chip";

    private static final String POKERSTARS_SMALL_BLIND_ACTION = "posts small blind";
    private static final String POKERSTARS_BIG_BLIND_ACTION = "posts big blind";

    // Hand actions
    private static final String BOVADA_CALL_ACTION = "Calls";
    private static final String BOVADA_CHECK_ACTION = "Checks";
    private static final String BOVADA_RAISE_ACTION = "Raises";
    private static final String BOVADA_FOLD_ACTION = "Folds";
    private static final String BOVADA_BET_ACTION = "Bets";
    private static final String BOVADA_RETURN_ACTION = "Return";
    private static final String BOVADA_DOES_NOT_SHOW_ACTION = "Does";
    private static final String BOVADA_ALL_IN_RAISE_ACTION = "All-in(raise)";
    private static final String BOVADA_ALL_IN_ACTION = "All-in";
    private static final String BOVADA_SEAT_STAND_ACTION = "Seat";
    private static final String BOVADA_TABLE_ACTION = "Table";

    private static final String POKERSTARS_CALL_ACTION = "calls";
    private static final String POKERSTARS_CHECK_ACTION = "checks";
    private static final String POKERSTARS_BET_ACTION = "bets";

    public static String convertPostingAction(String action, Map<String, String> seatMap) {
        Matcher postingActionMatcher = postingActionPattern.matcher(action);
        if (postingActionMatcher.find()) {
            String playerName = postingActionMatcher.group(1);
            String bovadaAction = postingActionMatcher.group(2);

            String transformedName = seatMap.get(playerName);
            // escape and [ '+' for UTG+1 etc
            playerName = playerName.replace("+", "\\+").replace("[", "\\[");
            String pokerstarsAction = "";
            switch (bovadaAction) {
                case BOVADA_SMALL_BLIND_ACTION:
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

            String transformedName = handContext.getSeatMap().get(playerName);
            transformedAction = transformedAction.replace(playerName + " ", transformedName);

            // sloppy but default to Raise because the grouping is a bit off
            switch (bovadaAction) {
                case BOVADA_CALL_ACTION:
                    transformedAction = transformedAction.replace(BOVADA_CALL_ACTION, POKERSTARS_CALL_ACTION);
                    break;
                case BOVADA_CHECK_ACTION:
                    transformedAction = transformedAction.replace(BOVADA_CHECK_ACTION, POKERSTARS_CHECK_ACTION);
                    break;
                case BOVADA_FOLD_ACTION:
                    transformedAction = transformedName + ": folds";
                    break;
                case BOVADA_RAISE_ACTION:
                    Matcher raiseMatcher = raisesActionPattern.matcher(action);
                    if (raiseMatcher.find()) {
                        String raiseValue = raiseMatcher.group(1);
                        double currentBet = Double.parseDouble(raiseValue);
                        double raiseInterval = Double.parseDouble(raiseValue) - handContext.getCurrentBet();
                        raiseInterval = Math.round(raiseInterval * 100.0) / 100.0;
                        handContext.setCurrentBet(currentBet);
                        transformedAction = transformedName + ": raises $" + String.format("%.2f", raiseInterval) + " to $" + raiseValue;
                    }
                    break;
                case BOVADA_BET_ACTION:
                    transformedAction = transformedAction.replace(BOVADA_BET_ACTION, POKERSTARS_BET_ACTION);
                    Matcher betMatcher = betActionPattern.matcher(action);
                    if (betMatcher.find()) {
                        String betValue = betMatcher.group(1);
                        double currentBet = Double.parseDouble(betValue);
                        handContext.setCurrentBet(currentBet);
                    }
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
                    Matcher allinMatcher = allinActionPattern.matcher(action);
                    if (allinMatcher.find()) {
                        String allinValue = allinMatcher.group(1);
                        double allinBet = Double.parseDouble(allinValue);
                        double currentBet = handContext.getCurrentBet();

                        if (currentBet == 0) {
                            transformedAction = transformedName + ": bets $" + String.format("%.2f", allinBet) + " and is all-in";
                            handContext.setCurrentBet(allinBet);
                        } else {
                            transformedAction = transformedName + ": calls $" + String.format("%.2f", allinBet) + " and is all-in";
                        }
                    }
                    break;
                case BOVADA_ALL_IN_RAISE_ACTION:
                    Matcher allinRaiseMatcher = allinRaiseActionPattern.matcher(action);
                    if (allinRaiseMatcher.find()) {
                        String allinValue = allinRaiseMatcher.group(1);
                        double allinBet = Double.parseDouble(allinValue);
                        double currentBet = handContext.getCurrentBet();

                        double raise = allinBet - currentBet;
                        raise = Math.round(raise * 100.0) / 100.0;
                        transformedAction = transformedName + ": raises $" + String.format("%.2f", raise)
                                + " to $" + String.format("%.2f", allinBet) + " and is all-in";
                        handContext.setCurrentBet(allinBet);
                    }
                    break;
                case BOVADA_SEAT_STAND_ACTION:
                case BOVADA_TABLE_ACTION:
                    // just ignore these lines - don't care about people sitting down or standing up
                    break;
                default:
                    SystemUtils.exitProgramWithError("Unrecognized hand action: " + bovadaAction, Optional.empty());
            }
        }

        return transformedAction;
    }
}
