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
 * Methods for writing a hand
 *
 * Created by MVW on 4/15/2018.
 */
public class RingGameHandWriter extends HandWriter {

    private static final Pattern lastWordPattern = Pattern.compile("(\\S+)$");
    private static final Pattern bovadaDatePattern = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d)");

    public RingGameHandWriter(GameConverter gameConverter, FileWriter fileWriter, PokerGame pokerGame) {
     super(gameConverter, fileWriter, pokerGame);
    }

    @Override
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

    @Override
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

    @Override
    public void writeSeats(List<String> entireHand, Optional<Map<String, String>> seatMap) {
        int numSeats = 0;

        try {
            for (String line: entireHand) {
                Matcher seatNameMatcher = seatNamePattern.matcher(line);
                if (seatNameMatcher.matches()) {
                    numSeats++;
                    String bovadaSeatName = seatNameMatcher.group(2);
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

    @Override
    public void writeHandAction(List<String> entireHand) {
        HandContext handContext = new HandContext(playerMap, pokerGame.getFixedLargeBlind(), true);
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
}
