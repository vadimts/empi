package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.command.itunes.SyncPlaylist;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.itunes.ITunesException;
import eu.tsvetkov.empi.itunes.script.BaseScript;
import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.util.ITunes.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.tsvetkov.empi.util.ITunes.Tag.*;
import static eu.tsvetkov.empi.util.Util.joinLines;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ITunesTest {

    private static final SLogger log = new SLogger();

    public static final String TEST_PLAYLIST = "test-playlist-";
    private static Path dirTestMp3s;
    private static List<Path> testMp3s;

    private String playlistName;
    private long totalTrackCount;
    private ArrayList<Integer> addedTrackIds = new ArrayList<>();
    private List<Path> createdFiles;
    private boolean useSystemLibrary;

    @BeforeClass
    public static void beforeClass() throws Exception {
        dirTestMp3s = Paths.get(ITunesTest.class.getResource("/mp3").toURI());
    }

    @Before
    public void before() throws Exception {

//        // Store the total count of iTunes tracks.
//        totalTrackCount = getItunesTracksCount();
//        addedTrackIds = new ArrayList<>();
//
//        // Create the test playlist.
//        playlistName = TEST_PLAYLIST + new Date().getTime();
//        Script.getOrCreatePlaylist(playlistName);
//
//        assertTrue("Test MP3s not found", Files.isDirectory(dirTestMp3s));
//
//        // Update file names of test MP3s so that they get newly added to iTunes.
//        renameTestMp3s("01s", playlistName);
//        testMp3s = Files.walk(dirTestMp3s, 1).filter(Mp3File::isMp3File).collect(Collectors.toList());
    }

    @After
    public void after() throws Exception {

        // Delete newly added tracks from iTunes, if any.
        if (!addedTrackIds.isEmpty()) {
            log.debug("Deleting " + addedTrackIds.size() + " iTunes tracks added by the test: " + joinLines(addedTrackIds));
            for (int addedTrackId : addedTrackIds) {
                Script.deleteTrackFromLibrary(addedTrackId);
            }
        }
//        assertEquals(totalTrackCount, getItunesTracksCount());
//
//        // Delete test playlist.
//        Script.SCRIPT.exec("delete playlist \"" + playlistName + "\"");
//
//        // Rename test MP3s back to original file names.
//        renameTestMp3s(playlistName, "01s");
    }

    @Test
    public void cleanLocation() throws CommandException {
        assertEquals("/mnt/alpaca/music/cd/Laid Back/_..Keep Smiling + Hole In the Sky/01 Elevator Boy.mp3",
            ITunes.cleanLocation("file:///mnt/alpaca/music/cd/Laid%20Back/_..Keep%20Smiling%20+%20Hole%20In%20the%20Sky/01%20Elevator%20Boy.mp3"));
    }
}
