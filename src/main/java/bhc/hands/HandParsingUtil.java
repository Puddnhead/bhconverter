package bhc.hands;

import bhc.domain.Hand;
import bhc.util.RandomNameGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some generic parsing methods
 *
 * Created by MVW on 4/15/2018.
 */
public class HandParsingUtil {

    private static final Pattern heroPattern = Pattern.compile("Seat (\\d+): (.* \\[ME])");
    private static final Pattern otherPlayerPattern = Pattern.compile("Seat (\\d+): (.*) \\(\\$?[\\d,]+(\\.*\\d\\d)? in chips\\)$");
    static final Pattern showdownPattern = Pattern.compile("^(.*) : Showdown (\\[.*]) (\\(.*\\))$");
    static final Pattern preflopShowdown = Pattern.compile("^(.*) : Showdown\\(.*$");
    static final Pattern muckPattern = Pattern.compile("^(.*) : Mucks (\\[.*]) (\\(.*\\))$");
    private static final Pattern summaryPattern = Pattern.compile("Seat\\+\\d+: .*");
    private static final Pattern cardDealtPattern = Pattern.compile("^(.*) : Card dealt to a spot (\\[.*])");

    private static final HandDescriptionConverter handDescriptionConverter = new HandDescriptionConverter();
    private static final RandomNameGenerator randomNameGenerator = new RandomNameGenerator();

    /**
     * Randomizes the names of players so for instance "Small Blind" won't end up having statistics in Poker tracker
     *
     * @param entireHand the hand
     * @return a map of bovada player name to randomized pokerstars name
     */
    public static Map<String, String> generatePlayerMap(List<String> entireHand) {
        Map<String, String> playerMap = new HashMap<>();

        for (String line: entireHand) {
            Matcher heroMatcher = heroPattern.matcher(line);
            if (heroMatcher.find()) {
                String player = heroMatcher.group(2);
                playerMap.put(player, "Hero");
                continue;
            }

            Matcher otherPlayerMatcher = otherPlayerPattern.matcher(line);
            if (otherPlayerMatcher.find()) {
                String player = otherPlayerMatcher.group(2);
                playerMap.put(player, randomNameGenerator.randomName());
            }
        }

        return playerMap;
    }

    /**
     * Maps the arbitrary bovada seat numbers to number 1-9 that won't confuse PokerTracker
     *
     * @param entireHand the hand
     * @return a map of bovada seat numbers to pokerstars seat numbers
     */
    public static Map<String, String> generateSeatMap(List<String> entireHand) {
        Map<String, String> seatMap = new HashMap<>();
        int currentSeat = 1;

        for (String line: entireHand) {
            Matcher heroMatcher = heroPattern.matcher(line);
            if (heroMatcher.find()) {
                String seat = heroMatcher.group(1);
                seatMap.put(seat, currentSeat + "");
                currentSeat++;
                continue;
            }

            Matcher otherPlayerMatcher = otherPlayerPattern.matcher(line);
            if (otherPlayerMatcher.find()) {
                String seat = otherPlayerMatcher.group(1);
                seatMap.put(seat, currentSeat + "");
                currentSeat++;
            }
        }
        return seatMap;
    }

    /**
     * Generate a map of bovada player name to dealt hand
     *
     * @param entireHand
     * @return
     */
    public static Map<String, String> generateHoldCardsMap(List<String> entireHand) {
        Map<String, String> holeCardsMap = new HashMap<>();

        for (String line: entireHand) {
            Matcher cardDealtMatcher = cardDealtPattern.matcher(line);
            if (cardDealtMatcher.find()) {
                String playerName = cardDealtMatcher.group(1);
                if (playerName.contains("[ME]")) {
                    playerName = playerName.substring(0, playerName.length() - 5);
                }
                String twoCardHand = cardDealtMatcher.group(2);
                holeCardsMap.put(playerName, twoCardHand);
            }
        }

        return holeCardsMap;
    }

    /**
     * For some reason after listing seats the bovada print out has a second whitespace character for hero
     *
     * @param playerMap the generated player map
     */
    public static void modifyHeroEntry(Map<String, String> playerMap) {
        String heroKey = null;

        for (String key: playerMap.keySet()) {
            if (key.contains("[ME]")) {
                heroKey = key;
                break;
            }
        }

        playerMap.remove(heroKey);
        playerMap.put(heroKey.replace("[ME]", " [ME]"), "Hero");
    }

    /**
     * Parses the Bovada showdown and summary and returns a a map of the Bovada player name to the 2-card hand
     *
     * @param entireHand the hand containing showdowns and summaries
     * @return a map of the Bovada player name to the 2-card hand
     */
    static Map<String, Hand> findShowedDownHands(List<String> entireHand) {
        Map<String, Hand> playerHandMap = new HashMap<>();

        for (String line: entireHand) {
            Matcher showdownMatcher = showdownPattern.matcher(line);
            Matcher preflopShowdownMatcher = preflopShowdown.matcher(line);
            if (showdownMatcher.find()) {
                String playerName = showdownMatcher.group(1);
                String fiveCardHand = showdownMatcher.group(2);
                String bovadaDescription = showdownMatcher.group(3);
                Hand hand = new Hand(fiveCardHand, bovadaDescription);
                String twoCardHand = findTwoCardHand(playerName, entireHand);
                hand.setTwoCardHand(twoCardHand);
                handDescriptionConverter.convert(hand);
                playerHandMap.put(playerName, hand);
            } else if (preflopShowdownMatcher.find()) {
                String playerName = preflopShowdownMatcher.group(1);
                String bovadaDescription = "(High Card)";
                Hand hand = new Hand(null, bovadaDescription);
                String twoCardHand = findTwoCardHand(playerName, entireHand);
                hand.setTwoCardHand(twoCardHand);
                handDescriptionConverter.convert(hand);
                playerHandMap.put(playerName, hand);
            }
        }
        return playerHandMap;
    }

    static Map<String, Hand> findMuckedHands(List<String> entireHand) {
        Map<String, Hand> playerHandMap = new HashMap<>();

        for (String line: entireHand) {
            Matcher muckedMatcher = muckPattern.matcher(line);
            if (muckedMatcher.find()) {
                String playerName = muckedMatcher.group(1);
                String twoCardHand = muckedMatcher.group(2);
                String bovadaDescription = muckedMatcher.group(3);
                Hand hand = new Hand(null, bovadaDescription);
                hand.setTwoCardHand(twoCardHand);
                playerHandMap.put(playerName, hand);
            }
        }

        return playerHandMap;
    }

    private static String findTwoCardHand(String playerName, List<String> entireHand) {
        String hand = "[]";

        // first strip [ME] if its part of the player name
        String normalizedPlayerName = playerName;
        if (normalizedPlayerName.contains("[ME]")) {
            normalizedPlayerName = normalizedPlayerName.substring(0, normalizedPlayerName.length() - 5).trim();
        }
        // so UTG won't match UTG+1
        normalizedPlayerName += " ";

        for (String line: entireHand) {
            Matcher summaryMatcher = summaryPattern.matcher(line);
            if (summaryMatcher.matches() && line.contains(normalizedPlayerName)) {
                if (line.charAt(line.length() - 24) == '[') {
                    // example:
                    // Seat+1: Dealer $2.09  with One pair [Qs Jh-Qs Qd As Jh Th]
                    hand = "[" + line.substring(line.length() - 23, line.length() - 18) + "]";
                } else if (line.charAt(line.length() - 10) == '[') {
                    // example:
                    // Seat+213: UTG+3 1760  with High Card [Ac Kc ]
                    hand = "[" + line.substring(line.length() - 9, line.length() - 4) + "]";
                } else {
                    //example:
                    // Seat+3: Big Blind $0.15  with High Card [4c 2d]
                    hand = line.substring(line.length() - 9, line.length() - 2);
                }
                break;
            }
        }

        return hand;
    }
}
