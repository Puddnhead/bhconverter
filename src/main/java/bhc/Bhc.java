package bhc;

import bhc.converter.RingGameConverter;
import bhc.domain.PokerGame;
import bhc.util.FileService;
import bhc.util.GameTypeUtil;

import java.io.File;

/**
 * Main bovada hand converter class
 *
 * Created by MVW on 4/15/2018.
 */
public class Bhc {

    public static void main(String[] args) {
        File[] inputFiles;
        File outputDirectory;

        if (!FileService.hasCorrectUsage(args)) {
            FileService.printUsage();
            System.exit(0);
        }

        try {
            inputFiles = FileService.getInputFiles(args);
            outputDirectory = FileService.getOutputDirectory(args);
        } catch (IllegalArgumentException e) {
            return;
        }

        for (File inputFile: inputFiles) {
            PokerGame pokerGame = GameTypeUtil.getGameType(inputFile);
            if (pokerGame == null) {
                System.out.println("WARNING: Did not recognize " + inputFile.getPath() + " as a ring game or MTT");
            } else if (pokerGame.isTournament()) {
                // do nothing
            } else {
                RingGameConverter ringGameConverter = new RingGameConverter(inputFile, outputDirectory, pokerGame);
                ringGameConverter.convertGame();
            }
        }
    }
}
