package eu.tsvetkov.x_empi.script;

import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.util.SLogger;
import eu.tsvetkov.empi.util.Util;
import eu.tsvetkov.x_empi.error.itunes.ITunesException;
import eu.tsvetkov.x_empi.error.itunes.PlaylistNotFoundException;
import eu.tsvetkov.x_empi.util.ITunes;
import eu.tsvetkov.x_empi.util.ITunesTest;
import org.junit.*;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.tsvetkov.empi.util.Util.isNotEmpty;
import static eu.tsvetkov.x_empi.util.ITunes.Tag.*;
import static eu.tsvetkov.x_empi.util.ITunesTest.TEST_PLAYLIST;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class BaseScriptTest<T extends BaseScript> {

    static final Map<Locale, Map<ITunes.Tag, String>> tags = newTestTags();
    private static final SLogger log = new SLogger();
    @Rule
    public TestMode testMode = new TestMode();
    List<Integer> addedTrackIds;
    List<Path> createdFiles;
    Path dirTestMp3s;
    int originalPlaylistCount;
    long originalTrackCount;
    String playlistName;
    T script;
    List<Path> testMp3s;
    boolean useSystemLibrary;
    private boolean testPlaylists;
    private boolean testTracks;

    @Before
    public void before() throws Exception {
        script = Util.newGenericTypeInstance(this);

        // If test works with playlists
        if (testPlaylists) {
            playlistName = TEST_PLAYLIST + new Date().getTime();
            originalPlaylistCount = getPlaylistCount();
        }

        // If test works with tracks
        if (testTracks) {
            if (dirTestMp3s == null) {
                dirTestMp3s = Paths.get(ITunesTest.class.getResource("/mp3").toURI());
                assertTrue("Test MP3s not found", Files.isDirectory(dirTestMp3s));
            }

            addedTrackIds = new ArrayList<>();
            createdFiles = new ArrayList<>();
            originalTrackCount = getTrackCount();
            // Update file names ofNumber test MP3s so that they get newly added to iTunes.
            renameTestMp3s("01s", playlistName);
            testMp3s = Files.walk(dirTestMp3s, 1).filter(Mp3File::isMp3File).collect(Collectors.toList());
            script.getOrCreatePlaylist(playlistName);
        }
    }

    @After
    public void after() throws Exception {
        // If test works with tracks
        if (testTracks) {
            // Delete test tracks from iTunes.
            for (Integer trackId : addedTrackIds) {
                script.deleteTrackFromLibrary(trackId);
            }
            assertEquals(originalTrackCount, getTrackCount());
            // Delete created test MP3 files.
            for (Path file : createdFiles) {
                Files.deleteIfExists(file);
            }
            // Rename test MP3 files back to original file names.
            renameTestMp3s(playlistName, "01s");
        }

        // If test works with playlists
        if (testPlaylists) {
            // Delete test playlist.
            script.deletePlaylist(playlistName);
            assertEquals(originalPlaylistCount, getPlaylistCount());
        }
    }

    @BeforeClass
    public static void beforeClass() throws Exception {

    }

    @Test
    @TestTracks
    public void addTracksToPlaylist() throws Exception {
        addTestTracks(playlistName);
        // Check that track count has correctly increased.
        assertEquals(originalTrackCount + tags.size(), getTrackCount());
    }

    @Test
    @TestTracks
    public void deleteTrackFromLibrary() throws Exception {
        addTestTracks(playlistName);
        List<ITunes.Track> tracks = script.getPlaylistTracks(playlistName);
        int firstTrackId = tracks.get(0).getId();
        script.deleteTrackFromLibrary(firstTrackId);
        assertEquals(tracks.size() - 1, script.getPlaylistTracks(playlistName).size());
        for (Integer trackId : addedTrackIds) {
            if (trackId.equals(firstTrackId)) {
                addedTrackIds.remove(trackId);
                break;
            }
        }
//        addedTrackIds = addedTrackIds.stream().filter(x -> x != firstTrackId).collect(toList());
    }

    @Test
    public void getNonExistingPlaylist() throws Exception {
        try {
            script.getPlaylist("playlist that doesn't exist");
            fail();
        } catch (ITunesException e) {
            assertTrue(e instanceof PlaylistNotFoundException);
        }
    }

    @Test
    @TestPlaylists
    public void getOrCreatePlaylist() throws Exception {
        assertEquals(playlistName, script.getOrCreatePlaylist(playlistName));
        assertEquals(originalPlaylistCount + 1, getPlaylistCount());
    }

    @Test
    @TestTracks
    public void getPlaylistTracks() throws Exception {
        addTestTracks(playlistName);
        List<ITunes.Track> tracks = script.getPlaylistTracks(playlistName);
        tracks.forEach(System.out::println);
    }

    @Test
    @TestTracks
    public void sync() throws Exception {
//        List<Path> existingTracks = new ArrayList<>();
//        List<Path> missingTracks = new ArrayList<>();
//        List<Path> outsideTracks = new ArrayList<>();
//        List<Path> newTracks = new ArrayList<>();
//        String dPlaylist = "playlist";
//        String dOutside = "outside";
//        String dExisting = "existingTracks";
//        String dMissing = "missingTracks";
//        String dNew = "newTracks";
//        Path dirSyncRoot = dirTestMp3s.resolve("sync");
//        Path dirPlaylist = dirSyncRoot.resolve(dPlaylist);
//        Path dirOutside = dirSyncRoot.resolve(dOutside);
//        Path dirExisting = dirPlaylist.resolve(dExisting);
//        Path dirMissing = dirPlaylist.resolve(dMissing);
//        Path dirNew = dirPlaylist.resolve(dNew);
//
//        // Copy test MP3s to album placeholder dirs and add tracks to iTunes library.
//        Files.walk(dirOutside).forEach(x -> outsideTracks.addAll(createTestFilesAndAddTracks(x)));
//        Files.walk(dirExisting).forEach(x -> existingTracks.addAll(createTestFilesAndAddTracks(x)));
//        Files.walk(dirMissing).forEach(x -> missingTracks.addAll(createTestFilesAndAddTracks(x)));
//        Files.walk(dirNew).forEach(x -> newTracks.addAll(createTestFiles(x)));
//
//        assertEquals(originalTrackCount + outsideTracks.size() + existingTracks.size() + missingTracks.size(), getTrackCount());
//
//        Thread.sleep(10000);
//
//        // Physically delete files from the directory "missingTracks".
//        for (Path track : missingTracks) {
//            Files.deleteIfExists(track);
//        }
//
//        Thread.sleep(30000);
//
//        SyncPlaylist syncCommand = new SyncPlaylist();
//        useSystemLibrary = true;
//        syncCommand.setUseSystemLibrary(useSystemLibrary);
//        syncCommand.setLibraryXmlPath(script.getLibraryXmlPath());
//        SyncPlaylist.Result result = syncCommand.analyse(dirPlaylist.toString(), playlistName);
//        assertEquals(outsideTracks.size(), result.getOutsideTracks().size());
//        assertEquals(existingTracks.size(), result.getExistingTracks().size());
//        assertEquals(missingTracks.size(), result.getMissingTracks().size());
//        assertFalse(result.getMissingTracks().isEmpty());
//        assertFalse(result.getMissingTracks().get(0).getPath().startsWith(BaseScript.ERROR_PREFIX));
//        assertEquals(newTracks.size(), result.getNewTrackPaths().size());
//
//        syncCommand.sync(dirPlaylist.toString(), playlistName);
//
//        syncCommand.getSuccess().getNewTracks().forEach(x -> addedTrackIds.add(x.getId()));
//
//        List<ITunes.Track> tracks = script.getPlaylistTracks(playlistName);
//        assertEquals(existingTracks.size() + newTracks.size(), tracks.size());
//        assertEquals(originalTrackCount + existingTracks.size() + newTracks.size(), getTrackCount());
    }

    private static HashMap<Locale, Map<ITunes.Tag, String>> newTestTags() {
        HashMap<Locale, Map<ITunes.Tag, String>> tags = new HashMap<>();

        HashMap<ITunes.Tag, String> de = new HashMap<>();
        de.put(NAME, "Rehbraune Augen hat mein Schatz");
        de.put(ARTIST, "Berühmte Tiroler Band");
        de.put(ALBUM, "Das größte Musik Album");
        tags.put(Locale.GERMAN, de);

        HashMap<ITunes.Tag, String> en = new HashMap<>();
        en.put(NAME, "Great, new, track");
        en.put(ARTIST, "Famous hip-hop artist");
        en.put(ALBUM, "Music album with ridiculously long name like in the 90's");
        tags.put(Locale.ENGLISH, en);

        HashMap<ITunes.Tag, String> jp = new HashMap<>();
        jp.put(NAME, "歌");
        jp.put(ARTIST, "楽士");
        jp.put(ALBUM, "新譜");
        tags.put(Locale.JAPANESE, jp);

        HashMap<ITunes.Tag, String> ru = new HashMap<>();
        ru.put(NAME, "Эх, дубинушка, ухнем!");
        ru.put(ARTIST, "Надежда Дедкина и Серебряный Квадрат");
        ru.put(ALBUM, "Русский народный трэшнячок");
        tags.put(new Locale("ru", "RU"), ru);
        return tags;
    }

    private Path copyFile(Path file, Path dir) {
        try {
            return Files.copy(file, dir.resolve(file.getFileName()));
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    private int getPlaylistCount() throws ScriptException {
        log.debug("Getting playlists count");
        return Integer.parseInt(script.exec(getPlaylistCountScript()));
    }

    private long getTrackCount() throws ScriptException {
        log.debug("Getting tracks count");
        return Integer.parseInt(script.exec(getTracksCountScript()));
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


    public class TestMode implements MethodRule {
        @Override
        public Statement apply(Statement base, FrameworkMethod junitMethod, Object target) {
            Method method = junitMethod.getMethod();
            BaseScriptTest test = (BaseScriptTest) target;
            test.testPlaylists = method.isAnnotationPresent(TestPlaylists.class) || method.isAnnotationPresent(TestTracks.class);
            test.testTracks = method.isAnnotationPresent(TestTracks.class);
            return base;
        }
    }

    void addTestTracks(String playlistName) throws ITunesException, ScriptException {
        // Iterate through locales to add tracks with multilingual tags.
        for (Locale locale : tags.keySet()) {

            // Expected MP3 tags.
            String expectedTags = Stream.of(tags.get(locale).get(NAME), tags.get(locale).get(ARTIST), tags.get(locale).get(ALBUM)).map(String::toString).collect(joining(","));

            // Add an MP3 to the playlist.
            String trackPathName = "/mp3/" + playlistName + "-tags-" + locale.getLanguage() + ".mp3";
            String trackPathString = getClass().getResource(trackPathName).getPath();
            // Fix paths on Windows if needed, i.e. "/C:/etc/empi/target/..." -> "C:/etc/empi/target/..."
            trackPathString = trackPathString.replaceFirst("^/(.:/)", "$1");
            ITunes.Track newTrack = script.addTrack(trackPathString, playlistName);
            addedTrackIds.add(newTrack.getId());

            // Get actual track's MP3 tags from iTunes.
            String actualTags = script.exec(getLastTrackTags(playlistName));

            // Assert tags.
            assertEquals(expectedTags, actualTags);
        }
    }

    List<Path> createTestFiles(Path dir) {
        List<Path> files = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            files.addAll(testMp3s.parallelStream().map(x -> copyFile(x, dir)).filter(Objects::nonNull).collect(Collectors.toList()));
            createdFiles.addAll(files);
            log.debug("Created " + files.size() + " test MP3s in '" + dir + "'");
        }
        return files;
    }

    List<Path> createTestFilesAndAddTracks(Path dir) {
        List<Path> addedTracks = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            List<Path> createdFiles = createTestFiles(dir);
            if (isNotEmpty(createdFiles)) {
                createdFiles.forEach(x -> {
                    try {
                        addedTrackIds.add(script.addTrack(x.toString(), playlistName).getId());
                        addedTracks.add(x);
                    } catch (ITunesException e) {
                        log.error(e);
                    }
                });
                log.debug("Added " + addedTracks.size() + " tracks from '" + dir + "'");
            }
        }
        return addedTracks;
    }

    abstract String getTracksCountScript();

    abstract String getPlaylistCountScript();

    abstract ITunesScript getLastTrackTags(String playlistName);
}
