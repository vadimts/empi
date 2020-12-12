package eu.tsvetkov.x_empi.util;

import eu.tsvetkov.empi.util.SLogger;
import eu.tsvetkov.x_empi.error.CommandException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ITunesTest {

    public static final String TEST_PLAYLIST = "test-playlist-";
    private static final SLogger log = new SLogger();
    private static Path dirTestMp3s;
    private static List<Path> testMp3s;
    private ArrayList<Integer> addedTrackIds = new ArrayList<>();
    private List<Path> createdFiles;
    private String playlistName;
    private long totalTrackCount;
    private boolean useSystemLibrary;

    @Before
    public void before() throws Exception {

//        // Store the total count ofNumber iTunes tracks.
//        totalTrackCount = getItunesTracksCount();
//        addedTrackIds = new ArrayList<>();
//
//        // Create the test playlist.
//        playlistName = TEST_PLAYLIST + new Date().getTime();
//        Script.getOrCreatePlaylist(playlistName);
//
//        assertTrue("Test MP3s not found", Files.isDirectory(dirTestMp3s));
//
//        // Update file names ofNumber test MP3s so that they get newly added to iTunes.
//        renameTestMp3s("01s", playlistName);
//        testMp3s = Files.walk(dirTestMp3s, 1).filter(BaseMp3File::isMp3File).collect(Collectors.toList());
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        dirTestMp3s = Paths.get(ITunesTest.class.getResource("/mp3").toURI());
    }

    @Test
    public void cleanLocation() throws CommandException {
        assertEquals("/mnt/alpaca/itunes/cd/Laid Back/_..Keep Smiling + Hole In the Sky/01 Elevator Boy.mp3",
            ITunes.cleanLocation("file:///mnt/alpaca/itunes/cd/Laid%20Back/_..Keep%20Smiling%20+%20Hole%20In%20the%20Sky/01%20Elevator%20Boy.mp3"));
    }
}
