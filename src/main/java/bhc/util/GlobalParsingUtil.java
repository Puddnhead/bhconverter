package bhc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing information from a file that is common to all hands and not a particular hand
 *
 * Created by MVW on 4/15/2018.
 */
public class GlobalParsingUtil {
    private static final Pattern seatNumberPattern = Pattern.compile("Seat (\\d):");

    public static String parseGameType(File inputFile) {
        int maxPlayers = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))) {
            String inputLine = bufferedReader.readLine();
            while (inputLine != null) {
                Matcher seatNumberMatcher = seatNumberPattern.matcher(inputLine);
                if (seatNumberMatcher.find()) {
                    int seatNumber = Integer.parseInt(seatNumberMatcher.group(1));
                    if (seatNumber > maxPlayers) {
                        maxPlayers = seatNumber;
                    }
                }
                inputLine = bufferedReader.readLine();
            }
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error parsing table name for file " + inputFile.getName(), Optional.of(ioe));
        }

        return maxPlayers + "-max ";
    }
}
