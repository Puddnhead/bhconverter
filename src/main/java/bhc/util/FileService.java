package bhc.util;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Static utility class for parsing file paths
 *
 * Created by MVW on 4/15/2018.
 */
public class FileService {
    public static boolean hasCorrectUsage(String[] args) {
        return args.length == 2;
    }

    /**
     * Returns a file object for the input directory, if it exists
     *
     * @param args program commandline args
     * @return a list of file objects for the input files
     * @throws IllegalArgumentException if the input directory does not exist, is not a directory, or has no files
     */
    public static File[] getInputFiles(String[] args) {
        File inputDirectory = new File(args[0]);
        if (!inputDirectory.exists() || !inputDirectory.isDirectory() || inputDirectory.listFiles().length == 0) {
            printUsage();
            throw new IllegalArgumentException();
        }

        return inputDirectory.listFiles();
    }

    /**
     * Returns a file object representing the output directory
     *
     * @param args the program command line arguments
     * @return a file object representing the output directory
     * @throws IllegalArgumentException if the directory does not exist
     */
    public static File getOutputDirectory(String[] args) {
        File outputDirectory = new File(args[1]);
        if (!outputDirectory.exists() || !outputDirectory.isDirectory()) {
            printUsage();
            throw new IllegalArgumentException();
        }

        return outputDirectory;
    }



    public static File createOutputFile(File inputFile, File outputDirectory) {
        String outputFilePath = outputDirectory + "\\Bovada" + inputFile.getName();
        File outputFile = new File(outputFilePath);

        try {
            if (outputFile.exists()) {
                if (!outputFile.delete()) {
                    SystemUtils.logError("Error deleting file " + outputFile.getName(), Optional.empty());
                }
            }
            if (!outputFile.createNewFile()) {
                SystemUtils.logError("Error creating file " + outputFile.getName(), Optional.empty());
            }
        } catch (IOException ioe) {
            SystemUtils.logError("Could not create output file", Optional.of(ioe));
        }

        return outputFile;
    }

    public static void printUsage() {
        System.out.println("Usage: java -jar bhc.jar <inputDirectory> <outputDirectory>");
    }
}
