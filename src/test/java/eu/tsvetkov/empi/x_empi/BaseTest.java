package eu.tsvetkov.empi.x_empi;

import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;
import org.junit.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class BaseTest {

    public static final String ALBUM_NEW = "new album name";
    public static final String ALBUM_OLD = "old album name";
    public static final String ARTIST_OLD = "old artist";
    public static final String TITLE_OLD = "old title";
    public static final String TRACKNO_OLD = "old track no";
    public static final String WORD_DE_ALL_UMLAUTS = "übergrößengeschäft";
    public static final String WORD_DE_ALL_UMLAUTS_TRANSL = "ubergrossengeschaft";

    protected void assertTestPath(String path) throws Exception {
        Path testPath = getTestFilePath(path);
        Assert.assertTrue(Files.exists(testPath));
        // Check that the real path is capitalized.
        Assert.assertEquals(testPath, testPath.toRealPath());
    }

    protected TagMap createTagMap() {
        HashMap<Mp3Tag, String> oldTags = new HashMap<>();
        // Initialize all tags with null.
        EnumSet.allOf(Mp3Tag.class).forEach(t -> oldTags.put(t, null));
        // Put some real tags.
        oldTags.put(Mp3Tag.TRACK_NO, TRACKNO_OLD);
        oldTags.put(Mp3Tag.ARTIST, ARTIST_OLD);
        oldTags.put(Mp3Tag.TITLE, TITLE_OLD);
        oldTags.put(Mp3Tag.ALBUM, ALBUM_OLD);
        return new TagMap(oldTags);
    }

    protected String getTestFileName(String path) throws Exception {
        return getTestFilePath(path).getFileName().toString();
    }

    protected Path getTestFilePath(String path) throws Exception {
        return Paths.get(getClass().getResource("/mp3/" + path).toURI());
    }

}
