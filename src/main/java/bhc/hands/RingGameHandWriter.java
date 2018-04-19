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
 * Methods for writing a hand
 *
 * Created by MVW on 4/15/2018.
 */
public class RingGameHandWriter extends HandWriter {

    private static final Pattern lastWordPattern = Pattern.compile("(\\S+)$");
    private static final Pattern bovadaDatePattern = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d)");
    private static final Pattern setDealerPattern = Pattern.compile("Set dealer \\[(\\d)]");
    public static final Pattern seatNamePattern = Pattern.compile("^Seat \\d: (.*) \\(\\$\\w+(\\.*\\d\\d)? in chips\\)$");
    private static final Pattern cardDealtPattern = Pattern.compile(".* Card dealt to a spot (\\[.*])");
    private static final Pattern handResultPattern = Pattern.compile("(.*) : Hand result (.*)$");
    private static final Pattern totalWonPattern = Pattern.compile("Total Pot\\(\\$?(.*)\\)$");
    private static final Pattern seatFoldedPattern = Pattern.compile("Seat\\+(\\d): (.*) Folded .*$");
    private static final Pattern seatMuckedPattern = Pattern.compile("Seat\\+(\\d): (.*) \\[Mucked] .*$");
    private static final Pattern seatWonPattern = Pattern.compile("Seat\\+(\\d): (.*) (\\$[\\d]+(\\.\\d\\d)?) .*$");
    private static final Pattern seatLostPattern = Pattern.compile("Seat\\+(\\d): (.*) lost .*$");
    private static final Pattern doesNotShowPattern = Pattern.compile("(.*) : Does not show .*");

    private static final String HOLE_CARDS = "*** HOLE CARDS ***";
    private static final String SHOWDOWN = "Showdown";
    private static final String SUMMARY = "*** SUMMARY ***";
    private static final String HAND_RESULT = "Hand result";
    private static final String DOESNT_SHOW_HAND = "Does not show";

    private static final String BOVADA_BUTTON = "Dealer";
    private static final String BOVADA_SMALL_BLIND = "Small Blind";
    private static final String BOVADA_BIG_BLIND = "Big Blind";

    private static final String POKERSTARS_BUTTON = "(button)";
    private static final String POKERSTARS_SMALL_BLIND = "(small blind)";
    private static final String POKERSTARS_BIG_BLIND = "(big blind)";

    private Map<String, String> playerMap;
    private List<String> uncalledPortionOfBet = new ArrayList<>();

    public RingGameHandWriter(GameConverter gameConverter, FileWriter fileWriter, PokerGame pokerGame) {
     super(gameConverter, fileWriter, pokerGame);
    }

    public void transformFirstLine(String firstLine, FileWriter writer) {
        try {
            String transformed = firstLine.replace("Bovada Hand", "PokerStars Hand");

            transformed = transformed.replaceFirst(" TBL#\\d+ HOLDEM No Limit", ": Hold'em No Limit " +
                    pokerGame.getBlindsRegex());

            // convert 02:00 timestamps to 2:00
            Matcher timespaceMatcher = lastWordPattern.matcher(transformed);
            while (timespaceMatcher.find()) {
                String timestamp = timespaceMatcher.group();
                if (timestamp.charAt(0) == '0') {
                    transformed = transformed.replace(timestamp, timestamp.substring(1));
                }
            }

            // convert 2000-01-01 dates to 2000/01/01
            Matcher dateMatcher = bovadaDatePattern.matcher(transformed);
            while (dateMatcher.find()) {
                String date = dateMatcher.group();
                String transformedDate = date.replaceAll("-", "/");
                transformed = transformed.replace(date, transformedDate);
            }

            writer.append(transformed).append("\n");
        } catch (IOException e) {
            SystemUtils.exitProgramWithError("Error writing file", Optional.of(e));
        }
    }

    public void writeSecondLine(List<String> entireHand, FileWriter writer) {
        String dealerSeat = findDealerSeat(entireHand);
        try {
            writer.append("Table '#")
                    .append(pokerGame.getTableNumber())
                    .append("' ")
                    .append(gameConverter.getGameType())
                    .append("Seat ")
                    .append(dealerSeat)
                    .append(" is the button\n");
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing second line", Optional.of(ioe));
        }
    }

    public void writeSeats(List<String> entireHand) {
        int numSeats = 0;

        try {
            for (String line: entireHand) {
                Matcher seatNameMatcher = seatNamePattern.matcher(line);
                if (seatNameMatcher.matches()) {
                    numSeats++;
                    String bovadaSeatName = seatNameMatcher.group(1);
                    String generatedName = playerMap.get(bovadaSeatName);
                    String transformedSeatLine = line.replace(bovadaSeatName, generatedName);
                    fileWriter.append(transformedSeatLine).append("\n");
                } else {
                    break;
                }
            }

            // remove seat lines
            removeLinesFromHand(numSeats, entireHand);
            // remove Set Dealer line if present
            if (entireHand.get(0).contains("Set dealer")) {
                entireHand.remove(0);
            }
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing seats", Optional.of(ioe));
        }
    }

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
            SystemUtils.exitProgramWithError("Error writing posting action", Optional.of(ioe));
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
            SystemUtils.exitProgramWithError("Error writing hole cards", Optional.of(ioe));
        }
    }

    public void writeHandAction(List<String> entireHand) {
        HandContext handContext = new HandContext(playerMap, pokerGame.getFixedLargeBlind());

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
                if (transformedAction.equals("Table enter user") || transformedAction.contains("Seat stand") ||
                        transformedAction.contains("Table leave user") || transformedAction.contains("Seat sit out") ||
                        transformedAction.contains("Table deposit")) {
                    // do nothing
                } else if (transformedAction.contains("Uncalled bet")) {
                    uncalledPortionOfBet.add(transformedAction);
                } else {
                    fileWriter.append(transformedAction).append("\n");
                }
            }
            removeLinesFromHand(numActions, entireHand);
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing hand actions", Optional.of(ioe));
        }
    }

    public void writeShowdownAndSummary(List<String> entireHand) {
        Map<String, Hand> showedDownHandsMap = writeShowdown(entireHand);
        writeUncalledPortionReturns();
        double totalWon = writeWinners(entireHand);
        writeSummary(entireHand, showedDownHandsMap, totalWon);
    }

    private String findDealerSeat(List<String> entireHand) {
        String dealerSeat = null;
        String smallestBlindSeatNumber;

        for (String line: entireHand) {
            Matcher setDealerMatcher = setDealerPattern.matcher(line);
            if (setDealerMatcher.find()) {
                dealerSeat = "#" + setDealerMatcher.group(1);
                break;
            }
        }

        // if no dealer, button is the first seat before the smallest blind
        if (dealerSeat == null) {
            smallestBlindSeatNumber = findSmallestBlindSeatNumber(entireHand);

            // if the smallest blind is the lowest seat number, return the highest seat number
            if (smallestBlindSeatNumber != null && entireHand.get(0).contains(smallestBlindSeatNumber)) {
                int highestSeatIndex = -1;
                for (String line: entireHand) {
                    if (line.contains("Seat")) {
                        highestSeatIndex++;
                    } else {
                        break;
                    }
                }
                String highestSeat = entireHand.get(highestSeatIndex);
                // ex: Seat 4: blah blah blah <-- looking for '4'
                dealerSeat = "#" + highestSeat.charAt(5);
                // else return the seat immediately before the smallest blind
            } else {
                int previousSeatIndex = -1;
                for (String line: entireHand) {
                    if (line.contains(smallestBlindSeatNumber)) {
                        break;
                    }
                    previousSeatIndex++;
                }
                String previousSeat = entireHand.get(previousSeatIndex);
                // ex: Seat 2: blah blah blah
                dealerSeat = "#" + previousSeat.charAt(5);
            }
        }
        return dealerSeat;
    }

    private String findSmallestBlindSeatNumber(List<String> entireHand) {
        // first look for small blind
        for (String line: entireHand) {
            if (line.contains("Small Blind")) {
                // ex: Seat 5: Small Blind ($26.12 in chips)
                return line.substring(0, 6);
            }
        }

        // then look for big blind
        for (String line: entireHand) {
            if (line.contains("Big Blind")) {
                // ex: Seat 2: Big Blind ($1.93 in chips)
                return line.substring(0, 6);
            }
        }

        return null;
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
            SystemUtils.exitProgramWithError("Error writing showdown", Optional.of(ioe));
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
            SystemUtils.exitProgramWithError("Error writing uncalled portion of bets", Optional.of(ioe));
        }
    }

    /**
     * Write the winners
     *
     * @param entireHand the hand
     * @return the total amount won (used to calculate rake
     */
    private double writeWinners(List<String> entireHand) {
        double totalWinnings = 0;

        // print winners
        try {
            while (!entireHand.get(0).equals(SUMMARY)) {
                Matcher doesNotShowMatcher = doesNotShowPattern.matcher(entireHand.get(0));
                if (doesNotShowMatcher.find()) {
                    String bovadaPlayerName = doesNotShowMatcher.group(1);
                    String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
                    fileWriter.append(pokerstarsPlayerName).append(": doesn't show hand\n");
                }

                Matcher handResultMatcher = handResultPattern.matcher(entireHand.get(0));
                if (handResultMatcher.find()) {
                    String bovadaWinner = handResultMatcher.group(1);
                    String pokerStarsWinner = playerMap.get(bovadaWinner);
                    String winningAmount = handResultMatcher.group(2);
                    fileWriter.append(pokerStarsWinner).append(" collected ").append(winningAmount).append("from pot\n");
                    if (winningAmount.charAt(0) == '$') {
                        winningAmount = winningAmount.substring(1);
                        totalWinnings += Double.parseDouble(winningAmount);
                    }
                }
                entireHand.remove(0);
            }
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Could not print winners", Optional.of(ioe));
        }

        return totalWinnings;
    }

    private void writeSummary(List<String> entireHand, Map<String, Hand> handsMap, double totalWon) {
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
                Matcher lostMatcher = seatLostPattern.matcher(line);
                if (foldedMatcher.matches()) {
                    printMuckedHand(foldedMatcher, handsMap, line);
                } else if (muckedMatcher.find()) {
                    printMuckedHand(muckedMatcher, handsMap, line);
                } else if (wonMatcher.find()) {
                    String seatNumber = wonMatcher.group(1);
                    String bovadaPlayerName = wonMatcher.group(2);
                    String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
                    Hand hand = handsMap.get(bovadaPlayerName);
                    String wonAmount = wonMatcher.group(3);
                    fileWriter.append("Seat ").append(seatNumber).append(": ").append(pokerstarsPlayerName).append(" ");
                    printPositionIfApplicable(line);
                    if (hand != null) {
                        fileWriter.append("showed ").append(hand.getTwoCardHand()).append(" and ");
                    }
                    fileWriter.append("won (").append(wonAmount).append(")");
                    if (hand != null) {
                        fileWriter.append(" with ").append(hand.getPokerStarsDescription());
                    }
                    fileWriter.append("\n");
                } else if (lostMatcher.find()) {
                    String seatNumber = lostMatcher.group(1);
                    String bovadaPlayerName = lostMatcher.group(2);
                    String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
                    Hand hand = handsMap.get(bovadaPlayerName);
                    fileWriter.append("Seat ").append(seatNumber).append(": ").append(pokerstarsPlayerName).append(" ");
                    printPositionIfApplicable(line);
                    fileWriter.append("showed ").append(hand.getTwoCardHand())
                            .append(" and lost with ").append(hand.getPokerStarsDescription()).append("\n");
                }
            }
            fileWriter.append("\n\n\n");
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing summary", Optional.of(ioe));
        }
    }

    private void printMuckedHand(Matcher matcher, Map<String, Hand> handsMap, String line) throws IOException {
        String seatNumber = matcher.group(1);
        String bovadaPlayerName = matcher.group(2);
        String pokerstarsPlayerName = playerMap.get(bovadaPlayerName);
        Hand hand = handsMap.get(bovadaPlayerName);
        fileWriter.append("Seat ").append(seatNumber).append(": ").append(pokerstarsPlayerName).append(" ");
        printPositionIfApplicable(line);
        fileWriter.append("mucked");
        if (hand != null) {
            fileWriter.append(" ").append(hand.getTwoCardHand());
        }
        fileWriter.append("\n");
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

    private void writeTotalWon(List<String> entireHand, double totalWon) {
        try {
            fileWriter.append(SUMMARY).append("\n");
            entireHand.remove(0);
            Matcher totalWonMatcher = totalWonPattern.matcher(entireHand.get(0));
            if (totalWonMatcher.find()) {
                String potSize = totalWonMatcher.group(1);
                double potSizeNumber = Double.parseDouble(potSize);
                double rake = Math.round((potSizeNumber - totalWon) * 100.0) / 100.0;
                fileWriter.append("Total pot $").append(potSize);
                if (rake > 0) {
                    fileWriter.append(" | Rake $").append(String.format("%.2f", rake));
                }
                fileWriter.append("\n");
            } else {
                SystemUtils.exitProgramWithError("Error writing total won", Optional.empty());
            }
            entireHand.remove(0);
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing summary", Optional.of(ioe));
        }
    }

    private void writeBoard(List<String> entireHand) {
        try {
            if (entireHand.get(0).contains("Board")) {
                String board = entireHand.get(0);
                board = board.substring(0, board.length() - 1).trim() + "]";
                fileWriter.append(board).append("\n");
                entireHand.remove(0);
            }
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing board", Optional.of(ioe));
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

    private String getHeroName() {
        for (String playerName: playerMap.keySet()) {
            if (playerMap.get(playerName).equals("Hero")) {
                return playerName;
            }
        }

        SystemUtils.exitProgramWithError("Error updating hero name", Optional.empty());
        return null;
    }

    private void removeLinesFromHand(int linesToRemove, List<String> hand) {
        for (int i = 0; i < linesToRemove; i++) {
            hand.remove(0);
        }
    }

    private void updateHeroName(Map<String, Hand> handMap) {
        String heroName = getHeroName();
        Hand heroHand = handMap.get(heroName);
        if (heroHand != null) {
            handMap.remove(heroName);
        }
        playerMap.remove(heroName);
        heroName = heroName.substring(0, heroName.length() - 6);
        playerMap.put(heroName, "Hero");
        if (heroHand != null) {
            handMap.put(heroName, heroHand);
        }
    }

    public void setPlayerMap(Map<String, String> playerMap) {
        this.playerMap = playerMap;
    }
}
