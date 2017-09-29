package eu.tsvetkov.empi.itunes.script;

import eu.tsvetkov.empi.command.itunes.PlaylistSync;
import eu.tsvetkov.empi.command.itunes.SyncPlaylist;
import eu.tsvetkov.empi.error.itunes.ITunesException;
import eu.tsvetkov.empi.error.itunes.PlaylistNotFoundException;
import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.util.*;
import org.junit.*;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.tsvetkov.empi.util.ITunes.Tag.ALBUM;
import static eu.tsvetkov.empi.util.ITunes.Tag.ARTIST;
import static eu.tsvetkov.empi.util.ITunes.Tag.NAME;
import static eu.tsvetkov.empi.util.ITunesTest.TEST_PLAYLIST;
import static eu.tsvetkov.empi.util.Util.*;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class BaseScriptTest<T extends BaseScript> {

    private static final SLogger log = new SLogger();

    static final Map<Locale, Map<ITunes.Tag, String>> tags = newTestTags();

    @Rule
    public TestMode testMode = new TestMode();

    Path dirTestMp3s;
    Path dirTestResources;
    List<Path> testMp3s;
    T script;
    String playlistName;
    int originalPlaylistCount;
    long originalTrackCount;
    List<Integer> addedTrackIds;
    List<Path> createdFiles;
    boolean useSystemLibrary;
    private boolean testPlaylists;
    private boolean testTracks;
    private TestMp3Dir syncMP3s;

    @BeforeClass
    public static void beforeClass() throws Exception {

    }

    @Before
    public void before() throws Exception {
        script = Util.newGenericTypeInstance(this);

        // If test works with playlists
        if(testPlaylists) {
            playlistName = TEST_PLAYLIST + new Date().getTime();
            originalPlaylistCount = getPlaylistCount();
        }

        // If test works with tracks
        if(testTracks) {
            if(dirTestMp3s == null) {
                dirTestResources = Paths.get(ITunesTest.class.getResource("/").toURI());
                dirTestMp3s = Paths.get(ITunesTest.class.getResource("/mp3").toURI());
                assertTrue("Test MP3s not found", Files.isDirectory(dirTestMp3s));
            }

            addedTrackIds = new ArrayList<>();
            createdFiles = new ArrayList<>();
            originalTrackCount = getTrackCount();
            // Update file names of test MP3s so that they get newly added to iTunes.
            renameTestMp3s("01s", playlistName);
            testMp3s = Files.walk(dirTestMp3s, 1).filter(Mp3File::isMp3File).collect(Collectors.toList());
            script.getOrCreatePlaylist(playlistName);
        }
    }

    @After
    public void after() throws Exception {
        // If test works with tracks
        if(testTracks) {
            // Delete test tracks from iTunes.
            for (Integer trackId : addedTrackIds) {
                script.deleteTrackFromLibrary(trackId);
            }

            Thread.sleep(10000);

            assertEquals(originalTrackCount, getTrackCount());
            // Delete created test MP3 files.
            for (Path file : createdFiles) {
                Files.deleteIfExists(file);
            }
            // Rename test MP3 files back to original file names.
            renameTestMp3s(playlistName, "01s");
        }

        // If test works with playlists
        if(testPlaylists) {
            // Delete test playlist.
            script.deletePlaylist(playlistName);
            assertEquals(originalPlaylistCount, getPlaylistCount());
        }

        if(syncMP3s != null) {
            syncMP3s.delete();
        }
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
    public void addTracksToPlaylist() throws Exception {
        addTestTracks(playlistName);
        // Check that track count has correctly increased.
        assertEquals(originalTrackCount + tags.size(), getTrackCount());
    }

    @Test
    @TestTracks
    public void sync() throws Exception {
        useSystemLibrary = false;
        List<Path> existingTracks = new ArrayList<>();
        List<Path> missingTracks = new ArrayList<>();
        List<Path> outsideTracks = new ArrayList<>();
        List<Path> newTracks = new ArrayList<>();
        syncMP3s = new TestMp3Dir(dirTestResources.resolve("sync"));

        // Copy test MP3s to album placeholder dirs and add tracks to iTunes library.
        Files.walk(syncMP3s.pOutside).forEach(x -> outsideTracks.addAll(createTestFilesAndAddTracks(x)));
        Files.walk(syncMP3s.pExisting).forEach(x -> existingTracks.addAll(createTestFilesAndAddTracks(x)));
        Files.walk(syncMP3s.pMissing).forEach(x -> missingTracks.addAll(createTestFilesAndAddTracks(x)));
        Files.walk(syncMP3s.pNew).forEach(x -> newTracks.addAll(createTestFiles(x)));

        Thread.sleep(10000);

        assertEquals(originalTrackCount + outsideTracks.size() + existingTracks.size() + missingTracks.size(), getTrackCount());

        Thread.sleep(10000);

        // Physically delete files from the directory "missingTracks".
        for (Path track : missingTracks) {
            Files.deleteIfExists(track);
        }

        Thread.sleep(10000);

        SyncPlaylist syncCommand = new SyncPlaylist();
        syncCommand.setUseSystemLibrary(useSystemLibrary);
        syncCommand.setLibraryXmlPath(script.getLibraryXmlPath());
        PlaylistSync playlistSync = syncCommand.analyse(syncMP3s.pPlaylist.toString(), playlistName);
        assertEquals(outsideTracks.size(), playlistSync.getMisplacedTracks().sizeBefore());
        assertEquals(existingTracks.size(), playlistSync.getUnmodifiedTracks().sizeBefore());
        assertEquals(missingTracks.size(), playlistSync.getMissingTracks().sizeBefore());
        assertFalse(playlistSync.getMissingTracks().getBefore().isEmpty());
        if(useSystemLibrary) {
            assertFalse(playlistSync.getMissingTracks().getBefore().get(0).getPath().startsWith(BaseScript.ERROR_PREFIX));
        }
        assertEquals(newTracks.size(), playlistSync.getNewTracks().getBefore().size());

        syncCommand.sync(syncMP3s.pPlaylist.toString(), playlistName);

        syncCommand.getSync().getNewTracks().getSuccess().forEach(x -> addedTrackIds.add(x.getId()));

        List<Track> tracks = script.getPlaylistTracks(playlistName);
        assertEquals(existingTracks.size() + newTracks.size(), tracks.size());
        assertEquals(originalTrackCount + existingTracks.size() + newTracks.size(), getTrackCount());
    }

    @Test
    @TestTracks
    public void deleteTrackFromLibrary() throws Exception {
        addTestTracks(playlistName);
        List<Track> tracks = script.getPlaylistTracks(playlistName);
        int firstTrackId = tracks.get(0).getId();
        script.deleteTrackFromLibrary(firstTrackId);
        assertEquals(tracks.size() - 1, script.getPlaylistTracks(playlistName).size());
        for (Integer trackId : addedTrackIds) {
            if(trackId.equals(firstTrackId)) {
                addedTrackIds.remove(trackId);
                break;
            }
        }
//        addedTrackIds = addedTrackIds.stream().filter(x -> x != firstTrackId).collect(toList());
    }

    @Test
    @TestTracks
    public void getPlaylistTracks() throws Exception {
        addTestTracks(playlistName);
        List<Track> tracks = script.getPlaylistTracks(playlistName);
        tracks.forEach(System.out::println);
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




    private void addTestTracks(String playlistName) throws ITunesException, ScriptException {
        // Iterate through locales to add tracks with multilingual tags.
        for (Locale locale : tags.keySet()) {

            // Expected MP3 tags.
            String expectedTags = Stream.of(tags.get(locale).get(NAME), tags.get(locale).get(ARTIST), tags.get(locale).get(ALBUM)).map(String::toString).collect(joining(","));

            // Add an MP3 to the playlist.
            String trackPathName = "/mp3/" + playlistName + "-tags-" + locale.getLanguage() + ".mp3";
            String trackPathString = getClass().getResource(trackPathName).getPath();
            // Fix paths on Windows if needed, i.e. "/C:/etc/empi/target/..." -> "C:/etc/empi/target/..."
            trackPathString = trackPathString.replaceFirst("^/(.:/)", "$1");
            Track newTrack = script.addTrack(trackPathString, playlistName);
            addedTrackIds.add(newTrack.getId());

            // Get actual track's MP3 tags from iTunes.
            String actualTags = script.exec(getLastTrackTags(playlistName));

            // Assert tags.
            assertEquals(expectedTags, actualTags);
        }
    }

    private Path copyFile(Path file, Path dir) {
        try {
            return Files.copy(file, dir.resolve(file.getFileName()));
        } catch (IOException e) {
            System.out.println(e);
            return null;
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

    private long getTrackCount() throws ScriptException {
        log.debug("Getting tracks count");
        return Integer.parseInt(script.exec(getTracksCountScript()));
    }

    private int getPlaylistCount() throws ScriptException {
        log.debug("Getting playlists count");
        return Integer.parseInt(script.exec(getPlaylistCountScript()));
    }

    abstract String getTracksCountScript();

    abstract String getPlaylistCountScript();

    abstract ITunesScript getLastTrackTags(String playlistName);

    private class TestMode implements MethodRule {

        @Override
        public Statement apply(Statement base, FrameworkMethod junitMethod, Object target) {
            Method method = junitMethod.getMethod();
            BaseScriptTest test = (BaseScriptTest) target;
            test.testPlaylists = method.isAnnotationPresent(TestPlaylists.class) || method.isAnnotationPresent(TestTracks.class);
            test.testTracks = method.isAnnotationPresent(TestTracks.class);
            return base;
        }
    }

    private static class TestMp3Dir {
        static String dPlaylist = "playlistDir";
        static String dOutside = "outsidePlaylistDir";
        static String dExisting = "existingTracks";
        static String dMissing = "missingTracks";
        static String dNew = "newTracks";
        static String albumDirsString = joinLines(
            "13 - Мусоргский",
            "1989 - Magnetic Mirror Master Mix (with The Upsetters)",
            "Freestylers - FSUK2 (320 from lossless) (1998)",
            "Lime_Dubs-Jade_and_Matt_U-LIME004-VINYL-2011-sweet",
            "VA - Sehnlicher.Baikal Lounge 2008",
            " Vol.1",
            "  CD 1",
            "  CD 2",
            " Vol.2",
            "  CD 1",
            "  CD 2",
            "t e l e p a t h テレパシー能力者 - 肉体からの離脱 2014",
            "Игорь Вдовин - Кракатук 2006"
        );

        Path pPlaylist;
        Path pOutside;
        Path pExisting;
        Path pMissing;
        Path pNew;
        private Path rootDir;
        private ListIterator<String> albumDirs = Util.split(albumDirsString, '\n').listIterator();
        Pattern prefixRE = Pattern.compile("^([ ]+).*");

        TestMp3Dir(Path rootDir) throws IOException {
            this.rootDir = rootDir;
            delete();
            create();
        }

        void delete() throws IOException {
            if(Files.exists(rootDir)) {
                Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            }
        }

        TestMp3Dir create() throws IOException {
            Files.createDirectories(rootDir);
            pOutside  = Files.createDirectories(rootDir.resolve(dOutside));
            pPlaylist = Files.createDirectories(rootDir.resolve(dPlaylist));
            pExisting = Files.createDirectories(pPlaylist.resolve(dExisting));
            pMissing  = Files.createDirectories(pPlaylist.resolve(dMissing));
            pNew      = Files.createDirectories(pPlaylist.resolve(dNew));
            List<Path> playlistDirs = Arrays.asList(pOutside, pExisting, pMissing, pNew);
            int playlistDirsCount = playlistDirs.size();

            int i = 0;
            while (albumDirs.hasNext()) {
                Path playlistDir = playlistDirs.get(i < playlistDirsCount ? i++ : new Random().nextInt(playlistDirsCount));
                String albumDir = albumDirs.next();
                createAlbumDir(playlistDir, albumDir);
            }

            return this;
        }

        private void createAlbumDir(Path parentDir, String albumDirName) throws IOException {
            Path albumDir = Files.createDirectories(parentDir.resolve(albumDirName.trim()));
            Matcher matcher = prefixRE.matcher(albumDirName);
            String prefix = (matcher.matches() ? matcher.group(1) : "");
            while (albumDirs.hasNext()) {
                albumDirName = albumDirs.next();
                matcher = prefixRE.matcher(albumDirName);
                if(matcher.matches() && matcher.group(1).length() > prefix.length()) {
                    createAlbumDir(albumDir, albumDirName);
                }
                else {
                    albumDirs.previous();
                    return;
                }
            }
        }
    }
}
