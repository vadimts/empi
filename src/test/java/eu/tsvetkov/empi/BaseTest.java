package eu.tsvetkov.empi;

import org.junit.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class BaseTest {

    protected void assertTestPath(String path) throws Exception {
        Path testPath = getTestFilePath(path);
        Assert.assertTrue(Files.exists(testPath));
        // Check that the real path is capitalized.
        Assert.assertEquals(testPath, testPath.toRealPath());
    }

    protected Path getTestFilePath(String path) throws Exception {
        return Paths.get(getClass().getResource("/mp3/" + path).toURI());
    }

    protected String getTestFileName(String path) throws Exception {
        return getTestFilePath(path).getFileName().toString();
    }

}
