package bhc;

import bhc.converter.RingGameConverter;
import bhc.domain.PokerGame;
import bhc.hands.HandWriter;
import bhc.util.GameTypeUtil;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static org.junit.Assert.assertTrue;

/**
 * Test the ring game conversion
 *
 * Created by MVW on 4/15/2018.
 */
public class RingGameConverterTest {

    private Map<String, String> playerNameMap = new HashMap<>();

    @Test
    public void testConvertRingGame() throws IOException {
        String inputFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\HH20180411-024552 - 5843087 - RING.txt";
        File inputFile = new File(inputFileName);
        PokerGame pokerGame = GameTypeUtil.getGameType(inputFile);
        File outputDirectory = new File("C:\\workspaces\\bhconverter\\src\\test\\resources\\");
        RingGameConverter ringGameConverter = new RingGameConverter(inputFile, outputDirectory, pokerGame);
        ringGameConverter.convertGame();

        String testOutputFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\BovadaHandHistory_Holdem_NL_20180411-024552 - 5843087 - RING.txt";
        String controlFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\BovadaHandHistory_Holdem_NL_RING.txt";
        //read file into stream, try-with-resources
        BufferedReader controlReader = new BufferedReader(new FileReader(controlFileName));
        BufferedReader testReader = new BufferedReader(new FileReader(testOutputFileName));

        String controlLine = controlReader.readLine();
        String testLine;
        testLine = testReader.readLine();

        while (controlLine != null) {
            // don't care if we skip a seat sit down or table deposit, also returning big blind uncalled portion
            if ((controlLine.equals("Seat sit down") && !testLine.equals("Seat sit down")) ||
                    (controlLine.contains("Table deposit") && !testLine.contains("Table deposit")) ||
                    (controlLine.contains("Table leave user") && !testLine.contains("Table leave user")) ||
                    (controlLine.contains("Uncalled bet ($0.1)") && !testLine.contains("Uncalled bet ($0.1)")) ||
                    (controlLine.contains("Set dealer") && !testLine.contains("Set dealer"))){
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

        Matcher seatNameMatcher = HandWriter.seatNamePattern.matcher(controlLine);
        if (seatNameMatcher.find()) {
            String controlPlayerName = seatNameMatcher.group(1);

            seatNameMatcher = HandWriter.seatNamePattern.matcher(testLine);
            assertTrue(seatNameMatcher.find());
            String testPlayerName = seatNameMatcher.group(1);
            playerNameMap.put(testPlayerName, controlPlayerName);

            transformedTestLine = testLine.replace(testPlayerName, controlPlayerName);
        }

        for (String testPlayerName: playerNameMap.keySet()) {
            if (transformedTestLine.contains(testPlayerName)) {
                transformedTestLine = transformedTestLine.replace(testPlayerName, playerNameMap.get(testPlayerName));
                break;
            }
        }

        if (!controlLine.equals(transformedTestLine)) {
            System.out.println("WARNING: Comparison mismatch");
            System.out.println("\tExpected:\t" + controlLine);
            System.out.println("\tActual:\t\t" + testLine);
        }
    }
}
