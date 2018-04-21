package bhc;

import bhc.hands.HandWriter;
import bhc.hands.RingGameHandWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Base class for these converter tests
 *
 * Created by MVW on 4/19/2018.
 */
public class ConverterTestBase {

    private final Pattern tournamentTablePattern = Pattern.compile("Table '#\\d+ 9'");
    protected Map<String, String> playerNameMap = new HashMap<>();

    protected void assertEquality(BufferedReader controlReader, BufferedReader testReader) throws IOException {
        String controlLine = controlReader.readLine();
        String testLine = testReader.readLine();

        while (controlLine != null) {
            // don't care if we skip a seat sit down or table deposit, also returning big blind uncalled portion
            if ((controlLine.equals("Seat sit down") && !testLine.equals("Seat sit down")) ||
                    (controlLine.contains("Table deposit") && !testLine.contains("Table deposit")) ||
                    (controlLine.contains("Table leave user") && !testLine.contains("Table leave user")) ||
                    (controlLine.contains("Set dealer") && !testLine.contains("Set dealer")) ||
                    (controlLine.contains("Sit out") && !testLine.contains("Sit out")) ||
                    (controlLine.contains("Re-join") && !testLine.contains("Re-join")) ||
                    (controlLine.contains("Stand") && !testLine.contains("Stand"))) {
                controlLine = controlReader.readLine();
                continue;
            }
            assertEqualsExceptSeatNames(controlLine, testLine);
            controlLine = controlReader.readLine();
            testLine = testReader.readLine();
        }
    }

    private void assertEqualsExceptSeatNames(String controlLine, String testLine) {
        String transformedTestLine = testLine;
        String transformedControlLine = convertTournamentTableName(controlLine);

        Matcher seatNameMatcher = HandWriter.seatNamePattern.matcher(transformedControlLine);
        if (seatNameMatcher.find()) {
            String controlPlayerName = seatNameMatcher.group(2);

            seatNameMatcher = HandWriter.seatNamePattern.matcher(testLine);
            assertTrue(seatNameMatcher.find());
            String testPlayerName = seatNameMatcher.group(2);
            playerNameMap.put(testPlayerName, controlPlayerName);

            transformedTestLine = testLine.replace(testPlayerName, controlPlayerName);
        }

        for (String testPlayerName: playerNameMap.keySet()) {
            if (transformedTestLine.contains(testPlayerName)) {
                transformedTestLine = transformedTestLine.replace(testPlayerName, playerNameMap.get(testPlayerName));
                break;
            }
        }

        if (!transformedControlLine.equals(transformedTestLine)) {
            System.out.println("WARNING: Comparison mismatch");
            System.out.println("\tExpected:\t" + controlLine);
            System.out.println("\tActual:\t\t" + testLine);
        }
    }

    String convertTournamentTableName(String controlLine) {
        Matcher tournamentTableMatcher = tournamentTablePattern.matcher(controlLine);
        if (tournamentTableMatcher.find()) {
            return controlLine.replace(" 9", "");
        }
        return controlLine;
    }
}
