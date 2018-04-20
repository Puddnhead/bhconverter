package bhc.hands;

import bhc.converter.GameConverter;
import bhc.domain.HandContext;
import bhc.domain.PokerGame;
import bhc.util.SystemUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hand writer for tourneys
 *
 * Created by MVW on 4/19/2018.
 */
public class MTTHandWriter extends HandWriter {

    private static final Pattern firstLinePattern =
            Pattern.compile("Bovada Hand (#\\d+): HOLDEM Tournament #\\d+ TBL(#\\d+), .* (\\(\\d+/\\d+\\)) - (\\d{4})-(\\d\\d)-(\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d)$");

    private int currentBigBlind;

    public MTTHandWriter(GameConverter gameConverter, FileWriter fileWriter, PokerGame pokerGame) {
        super(gameConverter, fileWriter, pokerGame);
    }

    @Override
    public void transformFirstLine(String firstLine, FileWriter writer) {
        //Bovada Hand #3661260797: HOLDEM Tournament #21391319 TBL#18, Normal- Level 3 (30/60) - 2018-04-06 22:06:14
        //PokerStars Hand #3661260797: Tournament #21391319, $20+$2 - Hold'em No Limit (30/60) - 2018/04/06 22:06:14
        try {
            Matcher firstLineMatcher = firstLinePattern.matcher(firstLine);
            if (firstLineMatcher.find()) {
                String handNumber = firstLineMatcher.group(1);
                String blinds = firstLineMatcher.group(3);
                currentBigBlind = Integer.parseInt(blinds.substring(0, blinds.length()-1).split("/")[1]);
                String year = firstLineMatcher.group(4);
                String month = firstLineMatcher.group(5);
                String day = firstLineMatcher.group(6);
                String hour = firstLineMatcher.group(7);
                String minute = firstLineMatcher.group(8);
                String second = firstLineMatcher.group(9);

                writer.append("PokerStars Hand ").append(handNumber)
                        .append(": Tournament #").append(pokerGame.getTournamentNumber())
                        .append(", ").append(pokerGame.getEntryFee())
                        .append(" - Hold'em No Limit ")
                        .append(blinds)
                        .append(" - ")
                        .append(year).append("/").append(month).append("/").append(day)
                        .append(" ").append(hour).append(":").append(minute).append(":").append(second)
                        .append("\n");

                // Write the start of the second line
                String tableNumber = firstLineMatcher.group(2);
                writer.append("Table '").append(tableNumber).append("' ");
            } else {
                SystemUtils.exitProgramWithError("Error writing first line", Optional.empty());
            }
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing first line", Optional.of(ioe));
        }
    }

    @Override
    public void writeSecondLine(List<String> entireHand, FileWriter writer) {
        String dealerSeat = findDealerSeat(entireHand);
        try {
            writer.append("Seat ")
                    .append(dealerSeat)
                    .append(" is the button\n");
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing second line", Optional.of(ioe));
        }
    }

    @Override
    public void writeSeats(List<String> entireHand, Optional<Map<String, String>> optionalSeatMap) {
        Map<String, String> seatMap = optionalSeatMap.get();
        int numSeats = 0;

        try {
            for (String line: entireHand) {
                Matcher seatNameMatcher = seatNamePattern.matcher(line);
                if (seatNameMatcher.find()) {
                    numSeats++;
                    String bovadaSeatNumber = seatNameMatcher.group(1);
                    String bovadaSeatName = seatNameMatcher.group(2);
                    String generatedSeatNumber = seatMap.get(bovadaSeatNumber);
                    String generatedName = playerMap.get(bovadaSeatName);
                    String transformedSeatLine = line.replace(bovadaSeatName, generatedName);
                    transformedSeatLine = transformedSeatLine.replaceFirst(bovadaSeatNumber, generatedSeatNumber);
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

    @Override
    public void writeHandAction(List<String> entireHand) {
        HandContext handContext = new HandContext(playerMap, currentBigBlind, false);
        writeHandAction(entireHand, handContext);
    }

    @Override
    public void writeTotalWon(List<String> entireHand, double totalWon) {
        try {
            fileWriter.append(SUMMARY).append("\n");
            entireHand.remove(0);
            Matcher totalWonMatcher = totalWonPattern.matcher(entireHand.get(0));
            if (totalWonMatcher.find()) {
                String potSize = totalWonMatcher.group(1);
                fileWriter.append("Total pot ").append(potSize).append(" | Rake 0\n");
            } else {
                SystemUtils.exitProgramWithError("Error writing total won", Optional.empty());
            }
            entireHand.remove(0);
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error writing summary", Optional.of(ioe));
        }
    }
}
