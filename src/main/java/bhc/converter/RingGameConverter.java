package bhc.converter;

import bhc.hands.HandWriter;
import bhc.domain.PokerGame;
import bhc.hands.HandParsingUtil;
import bhc.util.FileService;
import bhc.util.GlobalParsingUtil;
import bhc.util.SystemUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Logic for converting ring games
 *
 * Created by MVW on 4/15/2018.
 */
public class RingGameConverter {

    private File inputFile;
    private File outputDirectory;
    private String gameType;
    private PokerGame pokerGame;

    public RingGameConverter(File inputFile, File outputDirectory, PokerGame pokerGame) {
        this.inputFile = inputFile;
        this.outputDirectory = outputDirectory;
        this.pokerGame = pokerGame;
    }

    /**
     * Convert a ring game and write to a directory
     */
    public void convertRingGame() {
        File outputFile = FileService.createOutputFile(inputFile, outputDirectory);
        gameType = GlobalParsingUtil.parseGameType(inputFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             FileWriter writer = new FileWriter(outputFile)
        ) {
            String inputLine = reader.readLine();
            while (inputLine != null) {
                transformNextHand(inputLine, reader, writer);
                inputLine = reader.readLine();
            }
            writer.append("\n");
        } catch (IOException e) {
            SystemUtils.exitProgramWithError("Error processing file " + inputFile.getName(), Optional.of(e));
        }
    }

    private void transformNextHand(String firstLine, BufferedReader reader, FileWriter writer) {
        HandWriter handWriter = new HandWriter(this, writer, pokerGame);
        handWriter.transformFirstLine(firstLine, writer);

        List<String> entireHand = readEntireHand(reader);
        handWriter.writeSecondLine(entireHand, writer);
        Map<String, String> seatMap = HandParsingUtil.generateSeatMap(entireHand);
        handWriter.setSeatMap(seatMap);
        handWriter.writeSeats(entireHand);

        HandParsingUtil.modifyHeroEntry(seatMap);
        handWriter.writePostingActions(entireHand);
        handWriter.writeHoleCards(entireHand);
        handWriter.writeHandAction(entireHand);
        handWriter.writeShowdownAndSummary(entireHand);
    }

    private List<String> readEntireHand(BufferedReader reader) {
        List<String> entireHand = new ArrayList<>();

        try {
            String currentLine = reader.readLine();
            while (currentLine != null && !currentLine.equals("")) {
                entireHand.add(currentLine);
                currentLine = reader.readLine();
            }
            // skip the second blank line
            reader.readLine();
        } catch (IOException ioe) {
            SystemUtils.exitProgramWithError("Error transforming hand", Optional.of(ioe));
        }

        return entireHand;
    }

    public String getGameType() {
        return gameType;
    }
}
