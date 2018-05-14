package bhc.hands;

import bhc.converter.ActionConverter;
import bhc.converter.GameConverter;
import bhc.domain.Hand;
import bhc.domain.HandContext;
import bhc.domain.PokerGame;
import bhc.util.SystemUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for hand writer
 *
 * Created by MVW on 4/18/2018.
 */
public abstract class HandWriter {

    public static final Pattern seatNamePattern = Pattern.compile("Seat (\\d+): (.*) \\(\\$?[\\d,]+(\\.*\\d\\d)? in chips\\)$");
    private static final Pattern cardDealtPattern = Pattern.compile(".* Card dealt to a spot (\\[.*])");
    private static final Pattern handResultPattern = Pattern.compile("(.*) : Hand [rR]esult \\$?(\\d+(\\.\\d\\d)?)");
    private static final Pattern handResultSidePotPattern = Pattern.compile("(.*) : Hand [rR]esult-Side [Pp]ot \\$?(\\d+(\\.\\d\\d)?)");
    static final Pattern totalWonPattern = Pattern.compile("Total Pot\\(\\$?(.*)\\)$");
    private static final Pattern seatFoldedPattern = Pattern.compile("Seat\\+(\\d+): (.*) Folded .*$");
    private static final Pattern seatMuckedPattern = Pattern.compile("Seat\\+(\\d+): (.*) \\[Mucked] .*$");
    private static final Pattern seatWonPattern = Pattern.compile("Seat\\+(\\d+): (.*) (\\$?\\d+(\\.\\d\\d)?) ");
    private static final Pattern seatLostPattern = Pattern.compile("Seat\\+(\\d+): (.*) los[et] .*$");
    private static final Pattern doesNotShowSummaryPattern = Pattern.compile("Seat\\+(\\d+): (.*) (\\$?[\\d]+(\\.\\d\\d)?) \\[Does not show].*$");
    private static final Pattern doesNotShowPattern = Pattern.compile("(.*) : Does not show .*");
    private static final Pattern rankingPattern = Pattern.compile("(.*) : Ranking (\\d+)");
    private static final Pattern prizeCashPattern = Pattern.compile("(.*) : Prize Cash \\[(.*)]");
    private static final Pattern smallBlindSeatPatter = Pattern.compile("Seat (\\d+): Small Blind .*");
    private static final Pattern bigBlindSeatPatter = Pattern.compile("Seat (\\d+): Big Blind .*");

    static final String SUMMARY = "*** SUMMARY ***";

    private static final String BOVADA_BUTTON = "Dealer";
    private static final String BOVADA_SMALL_BLIND = "Small Blind";
    private static final String BOVADA_BIG_BLIND = "Big Blind";

    private static final String POKERSTARS_BUTTON = "(button)";
    private static final String POKERSTARS_SMALL_BLIND = "(small blind)";
    private static final String POKERSTARS_BIG_BLIND = "(big blind)";


    private static final String HOLE_CARDS = "*** HOLE CARDS ***";
    private static final String SHOWDOWN = "Showdown";
    private static final String HAND_RESULT = "Hand result";
    private static final String DOESNT_SHOW_HAND = "Does not show";

    GameConverter gameConverter;
    protected PokerGame pokerGame;
    FileWriter fileWriter;

    Map<String, String> playerMap;
    Map<String, String> holeCardsMap;
    List<String> uncalledPortionOfBet = new ArrayList<>();

    HandWriter(GameConverter gameConverter, FileWriter fileWriter, PokerGame pokerGame) {
        this.gameConverter = gameConverter;
        this.pokerGame = pokerGame;
        this.fileWriter = fileWriter;
    }

    public abstract void transformFirstLine(String firstLine, FileWriter writer);

    public abstract void writeSecondLine(List<String> entireHand, FileWriter writer);

    public abstract void writeSeats(List<String> entireHand, Optional<Map<String, String>> seatMap);

    public void writePostingActions(List<String> entireHand) {
        try {
            int numActions = 0;
            for (String line : entireHand) {
                if (line.equals(HOLE_CARDS)) {
                    break;
                }

                numActions++;
                String transformedAction = ActionConverter.convertPostingAction(line, playerMap);
                if (transformedAction.equals("Table enter user") || transformedAction.contains("Seat stand") ||
                        transformedAction.contains("Table leave user") || transformedAction.contains("Seat sit out")) {
                    // do nothing
                } else {
                    fileWriter.append(transformedAction).append("\n");

                }
            }

            // remove the posting actions and *** HOLE CARDS*** line from the hand
            removeLinesFromHand(numActions + 1, entireHand);
        } catch (IOException ioe) {
            SystemUtils.logError("Error writing posting action", Optional.of(ioe));
        }
    }

    public void writeHoleCards(List<String> entireHand) {
        try {
            int numDeals = 0;
            fileWriter.append(HOLE_CARDS).append("\n");
            String heroName = getHeroName();

            for (String line: entireHand) {
                Matcher cardDealtMatcher = cardDealtPattern.matcher(line);
                if (cardDealtMatcher.find()) {
                    numDeals++;
                    if (heroName != null && line.contains(heroName)) {
                        String heroHand = cardDealtMatcher.group(1);
                        fileWriter.append("Dealt to Hero ").append(heroHand).append("\n");
                    }
                } else {
                    break;
                }
            }

            //Remove cards dealt lines from entire hand
            removeLinesFromHand(numDeals, entireHand);

        } catch (IOException ioe) {
            SystemUtils.logError("Error writing hole cards", Optional.of(ioe));
        }
    }

    public abstract HandContext writeHandAction(List<String> entireHand);

    void writeHandAction(List<String> entireHand, HandContext handContext) {
        try {
            int numActions = 0;
            for (String line : entireHand) {
                if (line.contains(HAND_RESULT) || line.contains(SHOWDOWN) || line.contains(DOESNT_SHOW_HAND)) {
                    break;
                }
                numActions++;

                if (line.contains("***")) {
                    fileWriter.append(line).append("\n");
                    handContext.setCurrentBet(0);
                    continue;
                }

                String transformedAction = ActionConverter.convertHandAction(line, handContext);
                if (transformedAction.contains("Table enter user") || transformedAction.contains("Seat stand") ||
                        transformedAction.contains("Table leave user") || transformedAction.contains("Seat sit out") ||
                        transformedAction.contains("Table deposit") || transformedAction.contains("Sit out") ||
                        transformedAction.contains("Re-join")) {
                    // do nothing
                } else if (transformedAction.contains("Uncalled bet")) {
                    uncalledPortionOfBet.add(transformedAction);
                } else {
                    fileWriter.append(transformedAction).append("\n");
                }
            }
            removeLinesFromHand(numActions, entireHand);
        } catch (IOException ioe) {
            SystemUtils.logError("Error writing hand actions", Optional.of(ioe));
        }
    }

    public void writeShowdownAndSummary(List<String> entireHand, Optional<Map<String, String>> seatMap, HandContext handContext) {
        Map<String, Hand> showedDownHandsMap = writeShowdown(entireHand);
        writeUncalledPortionReturns();
        double totalWon = writeWinners(entireHand, handContext);
        writeSummary(entireHand, showedDownHandsMap, totalWon, seatMap);
    }

    /**
     * Write the showed down hands
     *
     * @param entireHand the hand
     * @return a map of bovada player name to showed-down or mucked hand
     */
    private Map<String, Hand> writeShowdown(List<String> entireHand) {
        Map<String, Hand> showedDownHandsMap = HandParsingUtil.findShowedDownHands(entireHand);
        Map<String, Hand> muckedHandsMap = HandParsingUtil.findMuckedHands(entireHand);

        try {
            if (!showedDownHandsMap.isEmpty()) {
                fileWriter.append("*** SHOW DOWN ***\n");

                int numHands = 0;
                for (String line: entireHand) {
                    if (line.contains("Showdown")) {
                        numHands++;
                        writeShowedDownHand(line, showedDownHandsMap);
                    } else if (line.contains("Mucks")) {
                        numHands++;
                        writeMuckedHands(line, muckedHandsMap);
                    } else if (line.contains("Hand result")){
                        break;
                    }
                }
                removeLinesFromHand(numHands, entireHand);
            }
        } catch (IOException ioe) {
            SystemUtils.logError("Error writing showdown", Optional.of(ioe));
        }

        showedDownHandsMap.putAll(muckedHandsMap);
        return showedDownHandsMap;
    }

    private void writeUncalledPortionReturns() {
        try {
            for (String uncalledPortionAction : uncalledPortionOfBet) {
                fileWriter.append(uncalledPortionAction).append("\n");
            }
            uncalledPortionOfBet.clear();
        } catch (IOException ioe) {
            SystemUtils.logError("Error writing uncalled portion of bets", Optional.of(ioe));
        }
    }

    /**
     * Write the winners
     *
     * @param entireHand the hand
     * @return the total amount won (used to calculate rake
     */
    private double writeWinners(List<String> entireHand, HandContext handContext) {
        double totalWinnings = 0;

        // print winners
        try {
            String mainPot = "";
            while (!entireHand.get(0).equals(SUMMARY)) {
                String line = entireHand.get(0);
                Matcher doesNotShowMatcher = doesNotShowPattern.matcher(line);
                if (doesNotShowMatcher.find()) {
                    String bovadaPlayerName = doesNotShowMatcher.group(1);
                    String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
                    fileWriter.append(pokerstarsPlayerName).append(": doesn't show hand\n");
                }

                Matcher handResultMatcher = handResultPattern.matcher(line);
                Matcher handResultSidePotMatcher = handResultSidePotPattern.matcher(line);
                if (handResultSidePotMatcher.find()) {
                    String bovadaWinner = handResultSidePotMatcher.group(1);
                    String pokerStarsWinner = playerMap.get(bovadaWinner);
                    String winningAmount = handResultSidePotMatcher.group(2);
                    totalWinnings += Double.parseDouble(winningAmount);
                    winningAmount = handContext.isCashGame() ? "$" + winningAmount : winningAmount;
                    fileWriter.append(pokerStarsWinner).append(" collected ").append(winningAmount).append(" from side pot\n");
                    mainPot = "main ";
                } else if (handResultMatcher.find()) {
                    String bovadaWinner = handResultMatcher.group(1);
                    String pokerStarsWinner = playerMap.get(bovadaWinner);
                    String winningAmount = handResultMatcher.group(2);

                    if (handContext.currentBetEqualsBigBlind()) {
                        String returnedBet = handContext.isCashGame()
                                ? String.format("$%.2f", handContext.getCurrentBet())
                                : ((int)handContext.getCurrentBet()) + "";
                        fileWriter.append("Uncalled bet (").append(returnedBet).append(") returned to ")
                                .append(pokerStarsWinner).append("\n");

                        double winningAmountDouble = Double.parseDouble(winningAmount);
                        winningAmountDouble -= handContext.getCurrentBet();

                        winningAmount = handContext.isCashGame()
                                ? String.format("$%.2f", winningAmountDouble)
                                : ((int)winningAmountDouble) + "";
                        modifyTotalPot(entireHand, winningAmount);
                    } else {
                        winningAmount = handContext.isCashGame() ? "$" + winningAmount : winningAmount;
                    }
                    fileWriter.append(pokerStarsWinner).append(" collected ").append(winningAmount).append(" from ")
                            .append(mainPot).append("pot\n");
                    if (winningAmount.charAt(0) == '$') {
                        winningAmount = winningAmount.substring(1);
                        totalWinnings += Double.parseDouble(winningAmount);
                    }
                }

                if (line.contains("Ranking")) {
                    writeTournamentPlace(line, entireHand);
                }
                entireHand.remove(0);
            }
        } catch (IOException ioe) {
            SystemUtils.logError("Could not print winners", Optional.of(ioe));
        }

        return totalWinnings;
    }

    private void writeSummary(List<String> entireHand, Map<String, Hand> handsMap, double totalWon,
                              Optional<Map<String, String>> seatMap) {
        writeTotalWon(entireHand, totalWon);
        writeBoard(entireHand);
        updateHeroName(handsMap);

        try {
            while (!entireHand.isEmpty()) {
                String line = entireHand.get(0);
                entireHand.remove(0);

                Matcher foldedMatcher = seatFoldedPattern.matcher(line);
                Matcher muckedMatcher = seatMuckedPattern.matcher(line);
                Matcher wonMatcher = seatWonPattern.matcher(line);
                Matcher doesNotShowMatcher = doesNotShowSummaryPattern.matcher(line);
                Matcher lostMatcher = seatLostPattern.matcher(line);
                if (foldedMatcher.matches()) {
                    printMuckedHand(foldedMatcher, line, seatMap);
                } else if (muckedMatcher.find()) {
                    printMuckedHand(muckedMatcher, line, seatMap);
                } else if (doesNotShowMatcher.find()) {
                    String seatNumber = doesNotShowMatcher.group(1);
                    if (seatMap.isPresent()) {
                        seatNumber = seatMap.get().get(seatNumber);
                    }
                    String bovadaPlayerName = doesNotShowMatcher.group(2);
                    String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
                    String wonAmount = doesNotShowMatcher.group(3);
                    String holeCards = holeCardsMap.get(bovadaPlayerName);
                    fileWriter.append("Seat ").append(seatNumber).append(": ").append(pokerstarsPlayerName).append(" ");
                    printPositionIfApplicable(line);
                    fileWriter.append("showed ").append(holeCards)
                            .append(" and won (")
                            .append(wonAmount).append(")")
                            .append("\n");
                } else if (lostMatcher.find()) {
                    String seatNumber = lostMatcher.group(1);
                    if (seatMap.isPresent()) {
                        seatNumber = seatMap.get().get(seatNumber);
                    }
                    String bovadaPlayerName = lostMatcher.group(2);
                    String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
                    Hand hand = handsMap.get(bovadaPlayerName);
                    fileWriter.append("Seat ").append(seatNumber).append(": ").append(pokerstarsPlayerName).append(" ");
                    printPositionIfApplicable(line);
                    fileWriter.append("showed ").append(hand.getTwoCardHand())
                            .append(" and lost with ").append(hand.getPokerStarsDescription()).append("\n");
                } else if (wonMatcher.find()) {
                    String seatNumber = wonMatcher.group(1);
                    if (seatMap.isPresent()) {
                        seatNumber = seatMap.get().get(seatNumber);
                    }
                    String bovadaPlayerName = wonMatcher.group(2);
                    String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
                    Hand hand = handsMap.get(bovadaPlayerName);
                    String holeCards = holeCardsMap.get(bovadaPlayerName);
                    String wonAmount = wonMatcher.group(3);
                    fileWriter.append("Seat ").append(seatNumber).append(": ").append(pokerstarsPlayerName).append(" ");
                    printPositionIfApplicable(line);
                    fileWriter.append("showed ").append(holeCards)
                            .append(" and won (")
                            .append(wonAmount)
                            .append(")");
                    if (hand != null) {
                        fileWriter.append(" with ").append(hand.getPokerStarsDescription());
                    }
                    fileWriter.append("\n");
                }
            }
            fileWriter.append("\n\n\n");
        } catch (IOException ioe) {
            SystemUtils.logError("Error writing summary", Optional.of(ioe));
        }
    }

    private void printMuckedHand(Matcher matcher, String line, Optional<Map<String, String>> seatMap) throws IOException {
        String seatNumber = matcher.group(1);
        if (seatMap.isPresent()) {
            seatNumber = seatMap.get().get(seatNumber);
        }
        String bovadaPlayerName = matcher.group(2);
        String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
        String holeCards = holeCardsMap.get(bovadaPlayerName);
        fileWriter.append("Seat ").append(seatNumber).append(": ").append(pokerstarsPlayerName).append(" ");
        printPositionIfApplicable(line);
        fileWriter.append("mucked")
            .append(" ").append(holeCards);

        fileWriter.append("\n");
    }

    private void writeTournamentPlace(String line, List<String> entireHand) {
        try {
            Matcher rankingMatcher = rankingPattern.matcher(line);
            if (rankingMatcher.find()) {
                String bovadaPlayerName = rankingMatcher.group(1);
                String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
                String ranking = rankingMatcher.group(2);
                String suffix;
                switch (ranking.charAt(ranking.length() - 1)) {
                    case '1':
                        suffix = "st";
                        break;
                    case '2':
                        suffix = "nd";
                        break;
                    case '3':
                        suffix = "rd";
                        break;
                    default:
                        suffix = "th";
                        break;
                }

                boolean winner = false;
                fileWriter.append(pokerstarsPlayerName);
                if (ranking.equals("1")) {
                    fileWriter.append(" wins the tournament");
                    winner = true;
                } else {
                    fileWriter.append(" finished the tournament in ")
                            .append(ranking).append(suffix).append(" place");
                }

                printWinnings(bovadaPlayerName, entireHand, winner);
                fileWriter.append("\n");
            }
        } catch (IOException ioe) {
            SystemUtils.logError("Error printing tournament place", Optional.of(ioe));
        }
    }

    private void printWinnings(String bovadaPlayerName, List<String> entireHand, boolean winner) throws IOException {
        for (String line: entireHand) {
            Matcher prizeCashMatcher = prizeCashPattern.matcher(line);
            if (prizeCashMatcher.find()) {
                String playerName = prizeCashMatcher.group(1);
                if (bovadaPlayerName.equals(playerName)) {
                    String winnings = prizeCashMatcher.group(2);
                    if (winner) {
                        fileWriter.append(" and receives ").append(winnings).append(" - congratulations!");
                    } else {
                        fileWriter.append(" and received ").append(winnings);
                    }
                    break;
                }
            }
        }
    }

    private void printPositionIfApplicable(String line) throws IOException {
        if (line.contains(BOVADA_BUTTON)) {
            fileWriter.append(POKERSTARS_BUTTON).append(" ");
        } else if (line.contains(BOVADA_SMALL_BLIND)) {
            fileWriter.append(POKERSTARS_SMALL_BLIND).append(" ");
        } else if (line.contains(BOVADA_BIG_BLIND)) {
            fileWriter.append(POKERSTARS_BIG_BLIND).append(" ");
        }
    }

    abstract void writeTotalWon(List<String> entireHand, double totalWon);

    private void writeBoard(List<String> entireHand) {
        try {
            if (entireHand.get(0).contains("Board")) {
                String board = entireHand.get(0);
                board = board.substring(0, board.length() - 1).trim() + "]";
                fileWriter.append(board).append("\n");
                entireHand.remove(0);
            }
        } catch (IOException ioe) {
            SystemUtils.logError("Error writing board", Optional.of(ioe));
        }
    }

    private void writeShowedDownHand(String line, Map<String, Hand> showedDownHandsMap) throws IOException {
        Matcher showdownMatcher = HandParsingUtil.showdownPattern.matcher(line);
        Matcher preflopShowdownMatcher = HandParsingUtil.preflopShowdown.matcher(line);
        String bovadaPlayerName = null;
        if (showdownMatcher.find()) {
            bovadaPlayerName = showdownMatcher.group(1);
        } else if (preflopShowdownMatcher.find()) {
            bovadaPlayerName = preflopShowdownMatcher.group(1);
        }

        if (bovadaPlayerName != null) {
            String pokerStarsPlayerName = playerMap.get(bovadaPlayerName);
            Hand hand = showedDownHandsMap.get(bovadaPlayerName);
            fileWriter.append(pokerStarsPlayerName)
                    .append(": shows ")
                    .append(hand.getTwoCardHand())
                    .append(" (")
                    .append(hand.getPokerStarsDescription())
                    .append(")\n");

        }
    }

    private void writeMuckedHands(String line, Map<String, Hand> muckedHandsMap) throws IOException {
        Matcher muckMatcher = HandParsingUtil.muckPattern.matcher(line);
        if (muckMatcher.find()) {
            String bovadaPlayerName = muckMatcher.group(1);
            String pokerStarsPlayerName = playerMap.get(bovadaPlayerName);
            Hand hand = muckedHandsMap.get(bovadaPlayerName);
            fileWriter.append(pokerStarsPlayerName)
                    .append(": Mucks ")
                    .append(hand.getTwoCardHand())
                    .append(" ")
                    .append(hand.getBovadaDescription())
                    .append("\n");
        }
    }

    private void modifyTotalPot(List<String> entireHand, String winningAmount) {
        int totalPotIndex = -1, seatWonIndex = -1;
        String previousWinning = "", seatWonReplacement = "";
        for (int i = 0; i < entireHand.size(); i++) {
            String line = entireHand.get(i);
            Matcher totalPotMatcher = totalWonPattern.matcher(line);
            Matcher seatWonMatcher = seatWonPattern.matcher(line);
            if (totalPotMatcher.find()) {
                totalPotIndex = i;
            } else if (seatWonMatcher.find()) {
                seatWonIndex = i;
                previousWinning = seatWonMatcher.group(3);
                seatWonReplacement = line.replace(previousWinning, winningAmount);
            }
        }

        if (totalPotIndex < 0 || seatWonIndex < 0) {
            SystemUtils.logError("Error updating total pot and winner on pot folded to big blind", Optional.empty());
        } else {
            entireHand.set(totalPotIndex, "Total Pot(" + winningAmount + ")");
            entireHand.set(seatWonIndex, seatWonReplacement);
        }
    }

    String findDealerSeat(List<String> entireHand) {
        String dealerSeat = "";
        String smallestBlindSeatNumber;

        // button is the first seat before the smallest blind
        smallestBlindSeatNumber = findSmallestBlindBovadaSeatNumber(entireHand);

        // if the smallest blind is the lowest seat number, return the highest seat number
        // hacky but add ':' so line "Seat 57:" won't match "5" and "Seat 71:" won't match "1:"
        int seatIndex = -1;
        if (smallestBlindSeatNumber != null && entireHand.get(0).contains(" " + smallestBlindSeatNumber + ":")) {
            for (String line: entireHand) {
                if (line.contains("Seat")) {
                    seatIndex++;
                } else {
                    break;
                }
            }
            // else return the seat immediately before the smallest blind
        } else {
            for (String line: entireHand) {
                // hacky but add ':' so line "Seat 57:" won't match "Seat 5:"
                if (line.contains(" " + smallestBlindSeatNumber + ":")) {
                    break;
                }
                seatIndex++;
            }
        }
        if (seatIndex < 0) {
            SystemUtils.logError("Error determining dealer seat", Optional.empty());
        } else {
            dealerSeat = "#" + (seatIndex + 1);
        }
        return dealerSeat;
    }

    private String findSmallestBlindBovadaSeatNumber(List<String> entireHand) {
        // first look for small blind
        for (String line: entireHand) {
            Matcher smallBlindMatcher = smallBlindSeatPatter.matcher(line);
            if (smallBlindMatcher.find()) {
                // ex: Seat 5: Small Blind ($26.12 in chips)
                return smallBlindMatcher.group(1);
            }
        }

        // then look for big blind
        for (String line: entireHand) {
            Matcher bigBlindMatcher = bigBlindSeatPatter.matcher(line);
            if (bigBlindMatcher.find()) {
                // ex: Seat 2: Big Blind ($1.93 in chips)
                return bigBlindMatcher.group(1);
            }
        }

        return null;
    }

    String getHeroName() {
        for (String playerName: playerMap.keySet()) {
            if (playerMap.get(playerName).equals("Hero")) {
                return playerName;
            }
        }

        SystemUtils.logError("Error updating hero name", Optional.empty());
        return null;
    }

    void removeLinesFromHand(int linesToRemove, List<String> hand) {
        for (int i = 0; i < linesToRemove; i++) {
            hand.remove(0);
        }
    }

    void updateHeroName(Map<String, Hand> handMap) {
        String heroName = getHeroName();
        Hand heroHand = handMap.get(heroName);
        if (heroHand != null) {
            handMap.remove(heroName);
        }
        playerMap.remove(heroName);
        heroName = heroName.substring(0, heroName.length() - 5).trim();
        playerMap.put(heroName, "Hero");
        if (heroHand != null) {
            handMap.put(heroName, heroHand);
        }
    }

    public void setPlayerMap(Map<String, String> playerMap) {
        this.playerMap = playerMap;
    }

    public void setHoleCardsMap(Map<String, String> holeCardsMap) {
        this.holeCardsMap = holeCardsMap;
    }
}