package bhc;

import bhc.converter.RingGameConverter;
import bhc.domain.PokerGame;
import bhc.util.GameTypeUtil;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Test for zone games
 *
 * Created by MVW on 7/21/2018.
 */
public class ZoneGameConverterTest extends ConverterTestBase {
    @Test
    public void testConvertZoneGame() throws IOException {
        String inputFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\HH20180718-235251 - 1593141 - ZONE - $0.10-$0.25 - HOLDEMZonePoker - NL - ZonePoker No.1286.txt";
        File inputFile = new File(inputFileName);
        PokerGame pokerGame = GameTypeUtil.getGameType(inputFile);
        File outputDirectory = new File("C:\\workspaces\\bhconverter\\src\\test\\resources\\");
        RingGameConverter ringGameConverter = new RingGameConverter(inputFile, outputDirectory, pokerGame);
        ringGameConverter.convertGame();

        String testOutputFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\BovadaHH20180718-235251 - 1593141 - ZONE - $0.10-$0.25 - HOLDEMZonePoker - NL - ZonePoker No.1286.txt";
        String controlFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\BovadaHandHistory_Holdem_NL_Zone(GENERATED).txt";

        try (
                BufferedReader controlReader = new BufferedReader(new FileReader(controlFileName));
                BufferedReader testReader = new BufferedReader(new FileReader(testOutputFileName));
        ) {
            assertEquality(controlReader, testReader);
        }
    }
}
