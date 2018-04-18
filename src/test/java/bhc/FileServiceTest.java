package bhc;

import bhc.util.FileService;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Tests for file finder util
 *
 * Created by MVW on 4/15/2018.
 */
public class FileServiceTest {

    @Test
    public void testCorrectTooFewArgumentsThrowsException() throws Exception {
        String[] args = {"Bhc", "\\temp"};
        assertFalse(FileService.hasCorrectUsage(args));

        String[] args2 = {"Bhc", "\\temp", "\\temp"};
        assertTrue(FileService.hasCorrectUsage(args2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInputFilesThrowsExceptionForMissingDirectory() throws Exception {
        String[] args = {"Bhc",  "\\thisaint\\no\\damn\\directory", "thisiswhatever"};
        FileService.getInputFiles(args);
    }

    @Test
    @Ignore
    public void testGetInputFilesWorksForDirectoryWithFiles() throws Exception {
        String[] args = {"Bhc", "\\temp", "\\temp"};
        File[] inputFIles = FileService.getInputFiles(args);
        assertTrue(inputFIles.length > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetOutputDirectoryThrowsExceptionForMissingDirectory() throws Exception {
        String[] args = {"Bhc", "whatever", "\\thisaint\\no\\damn\\direcotry"};
        FileService.getOutputDirectory(args);
    }

    @Test
    @Ignore
    public void testGetOutputDirectoryWorksForExistingDirectory() throws Exception {
        String[] args = {"Bhc", "whatever", "\\temp"};
        File outputDirectory = FileService.getOutputDirectory(args);
        assertTrue(outputDirectory.exists());
    }
}