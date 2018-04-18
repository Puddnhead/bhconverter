package bhc.converter;

import bhc.domain.PokerGame;
import bhc.util.FileService;
import bhc.util.GlobalParsingUtil;
import bhc.util.SystemUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base class for the game converters
 *
 * Created by MVW on 4/18/2018.
 */
public abstract class GameConverter {

    protected File inputFile;
    protected File outputDirectory;
    protected String gameType;
    protected PokerGame pokerGame;

    public GameConverter(File inputFile, File outputDirectory, PokerGame pokerGame) {
        this.inputFile = inputFile;
        this.outputDirectory = outputDirectory;
        this.pokerGame = pokerGame;
    }

    /**
     * Convert a ring game and write to a directory
     */
    public void convertGame() {
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

    List<String> readEntireHand(BufferedReader reader) {
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

    abstract void transformNextHand(String inputLine, BufferedReader reader, FileWriter writer);
}
