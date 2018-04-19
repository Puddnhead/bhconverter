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
 * Test the ring game conversion
 *
 * Created by MVW on 4/15/2018.
 */
public class MTTConverterTest extends ConverterTestBase {

    @Test
    public void testConvertRingGame() throws IOException {
        String inputFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\HH20180406-214500 - 1836669 - MTT.txt";
        File inputFile = new File(inputFileName);
        PokerGame pokerGame = GameTypeUtil.getGameType(inputFile);
        File outputDirectory = new File("C:\\workspaces\\bhconverter\\src\\test\\resources\\");
        RingGameConverter ringGameConverter = new RingGameConverter(inputFile, outputDirectory, pokerGame);
        ringGameConverter.convertGame();

        String testOutputFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\BovadaHH20180406-214500 - 1836669 - MTT.txt";
        String controlFileName = "C:\\workspaces\\bhconverter\\src\\test\\resources\\BovadaHandHistory_Holdem_NL_MTT.txt";

        try (
            BufferedReader controlReader = new BufferedReader(new FileReader(controlFileName));
            BufferedReader testReader = new BufferedReader(new FileReader(testOutputFileName));
        ) {
            assertEquality(controlReader, testReader);
        }
    }
}
