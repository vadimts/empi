package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.command.itunes.SyncPlaylist;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.ITunesException;
import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.util.ITunes.Script;
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
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ITunesTest {

    private static final SLogger log = new SLogger();

    private static final String TEST_PLAYLIST = "test-playlist-";
    private static final Map<Locale, Map<Tag, String>> tags = new HashMap<>();
    private static Path dirTestMp3s;
    private static List<Path> testMp3s;

    static {
        HashMap<Tag, String> de = new HashMap<>();
        de.put(NAME, "Rehbraune Augen hat mein Schatz");
        de.put(ARTIST, "Berühmte Tiroler Band");
        de.put(ALBUM, "Das größte Musik Album");
        tags.put(Locale.GERMAN, de);

        HashMap<Tag, String> en = new HashMap<>();
        en.put(NAME, "Great, new, track");
        en.put(ARTIST, "Famous hip-hop artist");
        en.put(ALBUM, "Music album with a ridiculously long name like in the 90's");
        tags.put(Locale.ENGLISH, en);

        HashMap<Tag, String> jp = new HashMap<>();
        jp.put(NAME, "歌");
        jp.put(ARTIST, "楽士");
        jp.put(ALBUM, "新譜");
        tags.put(Locale.JAPANESE, jp);

        HashMap<Tag, String> ru = new HashMap<>();
        ru.put(NAME, "Эх, дубинушка, ухнем!");
        ru.put(ARTIST, "Надежда Дедкина и Серебряный Квадрат");
        ru.put(ALBUM, "Русский народный трэшнячок");
        tags.put(new Locale("ru", "RU"), ru);
    }

    private String playlistName;
    private long totalTrackCount;
    private ArrayList<String> addedTrackIds = new ArrayList<>();
    private List<Path> createdFiles;

    @BeforeClass
    public static void beforeClass() throws IOException {
        dirTestMp3s = Paths.get(ITunesTest.class.getResource("/mp3").getPath());
    }

    @Before
    public void before() throws Exception {

        // Store the total count of iTunes tracks.
        totalTrackCount = getItunesTracksCount();
        addedTrackIds = new ArrayList<>();

        // Create the test playlist.
        playlistName = TEST_PLAYLIST + new Date().getTime();
        Script.getOrCreatePlaylist(playlistName);

        assertTrue("Test MP3s not found", Files.isDirectory(dirTestMp3s));

        // Update file names of test MP3s so that they get newly added to iTunes.
        renameTestMp3s("01s", playlistName);
        testMp3s = Files.walk(dirTestMp3s, 1).filter(Mp3File::isMp3File).collect(Collectors.toList());
    }

    @After
    public void after() throws Exception {

        // Delete newly added tracks from iTunes, if any.
        if (!addedTrackIds.isEmpty()) {
            log.debug("Deleting " + addedTrackIds.size() + " iTunes tracks added by the test: " + addedTrackIds.stream().collect(joining(", ")));
            for (String addedTrackId : addedTrackIds) {
                log.debug("Deleted? " + Script.deleteTrackFromLibrary(addedTrackId));
            }
        }
        assertEquals(totalTrackCount, getItunesTracksCount());

        // Delete test playlist.
        Script.exec("delete playlist \"" + playlistName + "\"");

        // Rename test MP3s back to original file names.
        renameTestMp3s(playlistName, "01s");
    }

    @Test
    public void analyse() throws Exception {
        createdFiles = new ArrayList<>();
        List<Path> existingTracks = new ArrayList<>();
        List<Path> missingTracks = new ArrayList<>();
        List<Path> outsideTracks = new ArrayList<>();
        List<Path> newTracks = new ArrayList<>();
        String dPlaylist = "playlist";
        String dOutside = "outside";
        String dExisting = "existingTracks";
        String dMissing = "missingTracks";
        String dNew = "newTracks";
        Path dirSyncRoot = dirTestMp3s.resolve("sync");
        Path dirPlaylist = dirSyncRoot.resolve(dPlaylist);
        Path dirOutside = dirSyncRoot.resolve(dOutside);
        Path dirExisting = dirPlaylist.resolve(dExisting);
        Path dirMissing = dirPlaylist.resolve(dMissing);
        Path dirNew = dirPlaylist.resolve(dNew);

        // Copy test MP3s to album placeholder dirs and add tracks.
        Files.walk(dirOutside).forEach(x -> outsideTracks.addAll(createTestFilesAndAddTracks(x)));
        Files.walk(dirExisting).forEach(x -> existingTracks.addAll(createTestFilesAndAddTracks(x)));
        Files.walk(dirMissing).forEach(x -> missingTracks.addAll(createTestFilesAndAddTracks(x)));
        Files.walk(dirNew).forEach(x -> newTracks.addAll(createTestFiles(x)));

        assertEquals(totalTrackCount + outsideTracks.size() + existingTracks.size() + missingTracks.size(), getItunesTracksCount());

        // Physically delete files from the directory "missingTracks".
        missingTracks.forEach(x -> {
            try {
                Files.deleteIfExists(x);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(60000);

        SyncPlaylist syncCommand = new SyncPlaylist();
        SyncPlaylist.Result result = syncCommand.analyse(dirPlaylist.toString(), playlistName);
        assertEquals(outsideTracks.size(), result.getOutsideTracks().size());
        assertEquals(existingTracks.size(), result.getExistingTracks().size());
        assertEquals(missingTracks.size(), result.getMissingTracks().size());
        assertFalse(result.getMissingTracks().isEmpty());
        assertFalse(result.getMissingTracks().get(0).getLocation().startsWith(ITunes.Script.ERROR_PREFIX));
        assertEquals(newTracks.size(), result.getNewTracks().size());

        // Delete test MP3s.
        for (Path file : createdFiles) {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void cleanLocation() throws CommandException {
        assertEquals("/mnt/alpaca/music/cd/Laid Back/_..Keep Smiling + Hole In the Sky/01 Elevator Boy.mp3",
            ITunes.cleanLocation("file:///mnt/alpaca/music/cd/Laid%20Back/_..Keep%20Smiling%20+%20Hole%20In%20the%20Sky/01%20Elevator%20Boy.mp3"));
    }

    @Test
    public void getOrCreatePlaylist() throws ITunesException {
        String result = Script.execLine("set a to playlist \"" + playlistName + "\"");
        assertTrue(result.matches("user playlist id \\d+ of source id \\d+"));
    }

    @Test
    public void getPlaylist() {
        try {
            Script.getPlaylist(playlistName + "that doesn't exist");
            fail();
        } catch (ITunesException e) {
            assertTrue(e.getMessage().startsWith("iTunes got an error:"));
        }
    }

    @Test
    public void playlistAddDeleteTrack() throws Exception {

        // Tags to test.
        String tagNames = Stream.of(NAME, ARTIST, ALBUM).map(Tag::toString).collect(joining(",", "{", "}"));

        // Iterate through locales to add tracks with multilingual tags.
        for (Locale locale : tags.keySet()) {

            // Expected MP3 tags.
            String expectedTags = Stream.of(tags.get(locale).get(NAME), tags.get(locale).get(ARTIST), tags.get(locale).get(ALBUM)).map(String::toString).collect(joining(", "));

            // Add an MP3 to the playlist.
            String trackPathName = "/mp3/" + playlistName + "-tags-" + locale.getLanguage() + ".mp3";
            Path trackPath = Paths.get(ITunesTest.class.getResource(trackPathName).getPath());
            String addedTrackId = Script.addTrack(trackPath, playlistName);
            addedTrackIds.add(addedTrackId);

            // Get actual track's MP3 tags from iTunes.
            String actualTags = Script.execLine("set s to " + tagNames + " of last track of playlist \"" + playlistName + "\"");

            // Assert tags.
            assertEquals(expectedTags, actualTags);
        }

        // Check that track count has correctly increased.
        assertEquals(totalTrackCount + tags.size(), getItunesTracksCount());
    }

    protected List<Path> createTestFiles(Path dir) {
        List<Path> files = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            files.addAll(testMp3s.parallelStream().map(x -> copyFile(x, dir)).filter(Objects::nonNull).collect(Collectors.toList()));
            createdFiles.addAll(files);
            log.debug("Created " + files.size() + " test MP3s in '" + dir + "'");
        }
        return files;
    }

    protected List<Path> createTestFilesAndAddTracks(Path dir) {
        List<Path> addedTracks = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            List<Path> createdFiles = createTestFiles(dir);
            if (!createdFiles.isEmpty()) {
                createdFiles.forEach(x -> {
                    try {
                        addedTrackIds.add(Script.addTrack(x, playlistName));
                        addedTracks.add(x);
                    } catch (ITunesException e) {
                        e.printStackTrace();
                    }
                });
                log.debug("Added " + addedTracks.size() + " tracks from '" + dir + "'");
            }
        }
        return addedTracks;
    }

    private Path copyFile(Path file, Path dir) {
        try {
            return Files.copy(file, dir.resolve(file.getFileName()));
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    private long getItunesTracksCount() throws ITunesException {
        return Long.valueOf(Script.execLine("get count of tracks of playlist 1"));
    }

    private void renameTestMp3s(String from, String to) throws IOException {
        Files.walk(dirTestMp3s, 1).forEach(x -> {
            try {
                if (Files.isRegularFile(x)) {
                    Files.move(x, Paths.get(x.toString().replace(from, to)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
