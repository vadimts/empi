package eu.tsvetkov.empi.empi2;

import eu.tsvetkov.empi.itunes.AppleScript;
import eu.tsvetkov.empi.model.AudioArtwork;
import eu.tsvetkov.empi.model.TrackId;
import eu.tsvetkov.empi.model.TrackList;
import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.ops.MusicOps;
import eu.tsvetkov.empi.ops.ScriptRun;
import eu.tsvetkov.empi.util.FileUtil;
import eu.tsvetkov.empi.util.SLogger;
import eu.tsvetkov.empi.util.Str;
import eu.tsvetkov.empi.util.Util;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.tsvetkov.empi.empi2.MusicOpsTest.MockType.*;
import static eu.tsvetkov.empi.itunes.AppleScript.getPlaylistSize;
import static eu.tsvetkov.empi.mp3.Mp3Tag.*;
import static eu.tsvetkov.empi.ops.MusicOps.findArtworkInPath;
import static eu.tsvetkov.empi.ops.MusicOps.matchArtworkFileName;
import static eu.tsvetkov.empi.util.FileUtil.getAudioFilePaths;
import static eu.tsvetkov.empi.util.Util.*;
import static java.util.Arrays.asList;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.iterate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MusicOpsTest {
    public static final Class<?> BASE_CLASS = MusicOpsTest.class;
    public static final Map<Path, AudioArtwork> artworks = new HashMap<>();
    static final List<String> ALBUM_SUBDIRS = asList("CD01", "CD-2", "Vol. 2", "Instrumentals", "Bonus", "Сторона A", "SONGS", "1", "2", "3");
    static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum";
    static final List<String> WORDS = asList(LOREM_IPSUM.replaceAll("[\\.,]", "").split("\\b "));
    private static final String testMp3DirName = "test-playlist";
    static Path testMp3;
    static String testPlaylist = testMp3DirName + "-" + new Date().getTime();
    private static SLogger log = new SLogger();
    private Path testDir;

    @Before
    public void before() {
        testMp3 = FileUtil.getPath(BASE_CLASS, "/mp3/01s.mp3");
        assert Files.exists(testMp3);
        testDir = FileUtil.getPath(BASE_CLASS, "/mp3").resolve(testMp3DirName);
        if (Files.exists(testDir)) {
            if (!FileUtil.isDirEmpty(testDir)) {
                ScriptRun findExistingTestPlaylists = MusicOps.runItems(AppleScript.getPlaylistsOfTrack(TrackId.of(getAudioFilePaths(testDir).get(0))));
                if (!findExistingTestPlaylists.isError()) {
                    findExistingTestPlaylists.getOutputLines().stream().filter(playlist -> playlist.startsWith(testMp3DirName)).forEach(
                        playlist -> MusicOps.runItems(AppleScript.deletePlaylistAndTracksFromLibrary(playlist))
                    );
                }
            }
            FileUtil.rmDir(testDir);
        }
        // Delete playlist "test-playlist-..." if exists
        String existingTestPlaylist = MusicOps.getPlaylist(playlistName -> playlistName.startsWith(testMp3DirName));
        if (existingTestPlaylist != null) {
            MusicOps.runItems(AppleScript.deletePlaylistAndTracksFromLibrary(existingTestPlaylist));
        }
        FileUtil.mkdir(testDir);
    }

    @After
    public void after() {
    }

    @Test
    public void addSetArtwork() {
//        MusicOps.getPlaylist(testPlaylist);
        MusicOps.addTracksLazyPath("auto", Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Anton Borin - Various"));
    }

    @Test
    public void addTracks() {
        new TestFiles(4, 5, 2).create();
        MusicOps.addTracks(testPlaylist, testDir);
    }

    @Test
    public void addTracksBatch() {
        long time = new Date().getTime();
//        createMp3Dir(testDir, 4, 5, 2, true);
        MusicOps.addTracksBatch(testPlaylist, Paths.get("/Volumes/alpaca/itunes/phone-01-2020"));
        log.log("------------------------- " + (new Date().getTime() - time));
    }

    @Test
    public void addTracksLazyPath() {
        long time = new Date().getTime();
        MusicOps.getPlaylist(testPlaylist);
        MusicOps.addTracksLazyPath(testPlaylist, Paths.get("/Volumes/alpaca/itunes/new/_alsonew"));
        log.log("------------------------- " + (new Date().getTime() - time));

        // "/Volumes/alpaca/itunes/phone-01-2020"
        // mp3list 2126
        // * add 2126 dummy mp3s: SCRIPTS: 2126, SUCCESS 2126, ERROR_FILE_UNREADABLE 0, done in 877944 ms = 14,6324 min
        // * Setting 2126 track locations: 510753 ms = 8,51 min
        // ------------------------- 1388697 ms = 23,14495 min

        // +++++ add dummies in batches
        // "/Volumes/alpaca/itunes/phone-01-2020"
        // mp3list 2126
        // * add 2126 dummy mp3s: SCRIPTS: 3, SUCCESS 2, ERROR_FILE_UNREADABLE 1, done in 195836 ms = 3,26 min
        // * Setting 2126 track locations, APPLESCRIPT "setTrackLocations"  518349ms = 8,64 min
        // ------------------------- 748315 ms = 12,4719167 min
    }

    @Test
    public void addTracksSimple() {
        long tim = new Date().getTime();
        MusicOps.addTracks(testPlaylist, Paths.get("/Volumes/alpaca/itunes/good-to-add/hippo/2raumwohnung - kommt zusammen"));
        log.log("------------------------- " + (new Date().getTime() - tim));
    }

    @Test
    public void addTracksWithLove() {
        MusicOps.addTracksWithLove("good-new", Paths.get("/Volumes/alpaca/itunes/good"), Paths.get("/Users/vadim/Music/lib/love/uniq-alpaca-good.love"));
    }

    @Test
    public void deleteTracks() {
        MusicOps.deleteTracks("test", testDir);
    }

    @Test
    public void exportLove() {
        MusicOps.exportLove("cd", Paths.get("/Users/vadim/Music/lib/"));
    }

    @Test
    public void getAllPlaylists() {
        List<String> playlists = MusicOps.getAllPlaylists();
        assertTrue(playlists.contains("Library"));
        assertTrue(playlists.contains("Music"));
    }

    @Test
    public void getArtwork() {
        Path baseDir = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008");
        List<List<Path>> files = MusicOps.findAudioAndArtworkFilePaths(baseDir);
        List<Path> audioFiles = files.get(0);
        List<Path> pictureFiles = files.get(1);
        log.debug(Str.of("Found ${1} audio, ${2} picture files").with(audioFiles.size(), pictureFiles.size()));

        audioFiles.forEach(path -> {
            Mp3File mp3File = new Mp3File(path);
            AudioArtwork tag1Artwork = mp3File.getTag1Artwork();
            AudioArtwork tag2Artwork = mp3File.getTag2Artwork();
            AudioArtwork parentArtwork = findArtworkInPath(path, baseDir);
            log.debug("  TAG1 " + tag1Artwork);
            log.debug("  TAG2 " + tag2Artwork);
            log.debug("  FILE " + parentArtwork);
        });
    }

    @Test
    public void getArtwork2() {
        Path baseDir = Paths.get("/Volumes/alpaca/itunes/phone-01-2020");
        List<List<Path>> files = FileUtil.getFilePathsLists(baseDir, FileUtil::isAudioFile);
        List<Path> audioFiles = files.get(0);
        audioFiles.forEach(path -> {
            AtomicInteger siblingsDirs = new AtomicInteger();
            try {
                Files.walk(path.getParent()).filter(x -> !x.equals(path.getParent())).forEach(sibl -> {
                    if (Files.isDirectory(sibl)) {
                        siblingsDirs.getAndIncrement();
                    }
                });
                if (siblingsDirs.get() > 0) {
                    log.debug(path);
                    Files.walk(path.getParent()).filter(x -> !x.equals(path.getParent())).forEach(sibl -> {
                        if (Files.isDirectory(sibl)) {
                            log.debug("  " + sibl);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void getArtworkBash() throws IOException {
        TreeMap<String, Integer> map = new TreeMap<>(Files.readAllLines(Paths.get("/Users/vadim/Documents/work/vadim/empi/etc/files.txt")).stream().filter(s -> !s.startsWith("#")).collect(Collectors.toMap(s -> s.split("\t")[0], s -> Integer.valueOf(s.split("\t")[1]))));
    }

    @Test
    public void getArtworkName() {
        List<String> mostFrequentNames = asList(
            "   ? cover-001.jpeg",
            "   ? cover-002.jpeg",
            "   ? picture1.jpeg",
            "   4 1.jpeg",
            "-   4 1992_the_eternal_dance.jpg",
            "-   4 2.jpeg",
            "-   4 Back 2.jpg",
            "-   4 Back.Jpg",
            "-   4 Back.png",
            "-   4 Booklet 01.png",
            "-   4 Booklet 02.png",
            "-   4 Booklet 03.png",
            "-   4 Booklet 04.png",
            "-   4 Booklet 05.png",
            "-   4 Booklet 06.png",
            "-   4 Booklet 07.png",
            "-   4 Booklet 08.png",
            "-   4 Booklet 1.jpg",
            "-   4 Booklet 2.jpg",
            "-   4 Booklet-01.jpg",
            "-   4 Booklet-02.jpg",
            "-   4 Booklet-03.jpg",
            "-   4 CD Matrix.jpg",
            "-   4 CD1 Matrix.png",
            "   4 CD1.png",
            "-   4 CD2 Matrix.png",
            "-   4 CD2.png",
            "   4 COVER.jpg",
            "   4 Cd.jpg",
            "   4 Cover 2.jpg",
            "-   4 Inside.Jpg",
            "-   4 Inside2.jpg",
            "-   4 Inside_3.jpg",
            "-   4 Inside_4.jpg",
            "-   4 Label-01.jpg",
            "-   4 Look Here!.jpg",
            "   4 Pic 1.jpeg",
            "-   4 Pic 2.jpeg",
            "-   4 Picture 6.JPG",
            "-   4 That Side.jpeg",
            "-   4 booklet 16-17.jpg",
            "-   4 booklet 5.jpg",
            "-   4 booklet 6.jpg",
            "   4 cover (Front).jpg",
            "   4 cover front.jpg",
            "   4 cover.Jpg",
            "   4 cover1.jpg",
            "-   4 digipack out.jpg",
            "   4 front0001.JPG",
            "   4 front0003.JPG",
            "   4 img001.jpg",
            "-   4 img009.jpg",
            "-   4 img010.jpg",
            "   4 label.jpg",
            "-   4 obi.jpg",
            "   4 sideA.jpg",
            "   4 side_1.jpg",
            "-   4 side_2.jpg",
            "-   4 vinyl aa.jpeg",
            "   4 Сторона A.jpg",
            "-   4 Сторона B.jpg",
            "-   5 13.jpg",
            "-   5 7.jpg",
            "-   5 8.jpg",
            "-   5 CD1 Matrix.jpg",
            "-   5 CD2 Matrix.jpg",
            "   5 Disc.png",
            "-   5 Inside.jpeg",
            "-   5 Picture 5.JPG",
            "   5 Side 1.jpg",
            "-   5 Side 2.jpg",
            "   5 Side_A.jpeg",
            "   5 a.jpg",
            "-   5 all.jpg",
            "   5 artwork.jpg",
            "-   5 b.jpg",
            "-   5 background.jpg",
            "   5 cover200.jpg",
            "   5 disk.jpg",
            "   5 front0001.JPG",
            "   5 front0002.JPG",
            "-   5 img007.jpg",
            "   5 pic.jpg",
            "-   5 small.jpg",
            "-   5 web.jpg",
            "   5 Cover.jpg",
            "   6 A.jpg",
            "-   6 B.jpg",
            "-   6 Booklet 3.png",
            "-   6 Booklet 4.png",
            "   6 CD.jpeg",
            "   6 CD1.jpg",
            "-   6 CD2.jpg",
            "   6 Cover 3.Jpg",
            "   6 Front.Jpg",
            "   6 Picture .JPG",
            "   6 Picture 1.JPG",
            "-   6 Picture 2.JPG",
            "-   6 Picture 3.JPG",
            "-   6 Picture 4.JPG",
            "-   6 This Side.jpeg",
            "-   6 booklet 12-13.jpg",
            "-   6 booklet 14-15.jpg",
            "-   6 booklet 4.jpg",
            "   6 cover (back).jpg",
            "-   6 cower.jpg",
            "   6 folder.JPG",
            "-   6 logo.jpg",
            "   6 vinyl a.jpeg",
            "   7 !cover_2.jpg",
            "-   7 12.jpg",
            "-   7 Booklet 07.jpg",
            "-   7 Booklet 08.jpg",
            "-   7 Booklet.jpg",
            "-   7 Inside_1.jpg",
            "-   7 Inside_2.jpg",
            "-   7 booklet 02-03.jpg",
            "-   7 booklet 04-05.jpg",
            "-   7 booklet 06-07.jpg",
            "-   7 booklet 08-09.jpg",
            "-   7 booklet 10-11.jpg",
            "-   7 booklet 3.jpg",
            "   7 box.jpg",
            "   7 cd.jpeg",
            "   7 cover (2).jpg",
            "-   8 6.jpg",
            "-   8 Booklet 01.jpg",
            "-   8 Booklet 02.jpg",
            "-   8 Booklet 03.jpg",
            "-   8 Booklet 04.jpg",
            "-   8 Booklet 05.jpg",
            "-   8 Booklet 06.jpg",
            "   8 Digipack.jpg",
            "   8 Front.jpeg",
            "-   8 booklet 1.jpg",
            "   8 front1.jpg",
            "-   8 frontback.jpg",
            "   9 Cover.png",
            "-   9 Rear.jpg",
            "-   9 booklet 2-3.jpg",
            "-   9 booklet 4-5.jpg",
            "-   9 booklet 6-7.jpg",
            "-   9 booklet 8-1.jpg",
            "   9 cd.JPG",
            "   9 cover 3.jpg",
            "   9 cover2.jpg",
            "-   9 photo.jpg",
            "-  10 11.jpg",
            "-  10 5.jpg",
            "-  10 CD Matrix.png",
            "-  10 back.png",
            "-  10 img006.jpg",
            "-  11 09.jpg",
            "-  11 10.jpg",
            "  11 CD-cover-2.jpg",
            "-  11 Vinyl Back.jpg",
            "  11 Vinyl Front.jpg",
            "-  11 booklet 2.jpg",
            "  11 folder.png",
            "  11 front.png",
            "-  11 img004.jpg",
            "-  11 img005.jpg",
            "-  12 08.jpg",
            "  12 Box.png",
            "  12 Disc.jpg",
            "  12 a-side.jpg",
            "-  12 b-side.jpg",
            "  12 disc.jpg",
            "  12 img001.jpg",
            "-  12 img002.jpg",
            "-  12 img003.jpg",
            "-  13 Back.JPG",
            "  13 CD-cover-1.jpg",
            "  13 CD.png",
            "-  13 Inlay.png",
            "-  13 Rear.png",
            "-  13 back.JPG",
            "  14 00-Front.jpg",
            "-  14 4.jpg",
            "-  14 Booklet 1.png",
            "-  14 Booklet 2.png",
            "-  14 Foto.jpg",
            "-  14 back out.png",
            "  14 cover400.jpg",
            "-  14 pm400000.gif",
            "-  15 07.jpg",
            "-  15 Back.jpeg",
            "  15 Front.JPG",
            "-  15 booklet 2-3.png",
            "-  15 booklet 4-5.png",
            "-  15 booklet 6-7.png",
            "-  15 booklet 8-1.png",
            "  15 cd.png",
            "  15 front.JPG",
            "-  16 06.jpg",
            "  16 Cover 1.Jpg",
            "  16 Cover 2.Jpg",
            "-  16 Cover_Back.jpg",
            "  16 Cover_Front.jpg",
            "-  16 back in.png",
            "-  16 dislogo0.gif",
            "-  16 matrix.png",
            "-  16 navi-div.gif",
            "-  16 spacer00.gif",
            "-  17 Label.jpg",
            "  18 Cover_CD.jpg",
            "  19 !cover.jpg",
            "-  19 3.jpg",
            "  19 albumartsmall.jpg",
            "-  19 back.jpeg",
            "-  19 inlay.jpg",
            "-  20 05.jpg",
            "  20 folder.gif",
            "  20 folder.jpeg",
            "-  21 screen_mp3.JPG",
            "-  22 04.jpg",
            "-  22 2.jpg",
            "  22 AlbumArtSmall.jpg",
            "-  22 Inlay.jpg",
            "  22 cover.bmp",
            "-  23 03.jpg",
            "  23 1.jpg",
            "  25 cover.png",
            "-  25 inside.jpg",
            "  27 01.jpg",
            "-  28 02.jpg",
            "-  34 Side B.jpg",
            "  35 Side A.jpg",
            "-  37 Inside.jpg",
            "  37 cover.gif",
            "  40 cover 1.jpg",
            "  40 cover 2.jpg",
            "  47 front.jpeg",
            "  58 Cover.jpeg",
            "  72 cd.jpg",
            "  87 Cover.Jpg",
            "  98 cover.JPG",
            " 150 cover.jpeg",
            " 165 Folder.jpg",
            "- 188 Back.jpg",
            " 188 CD.jpg",
            " 309 Front.jpg",
            "- 366 back.jpg",
            " 428 front.jpg",
            " 733 Cover.jpg",
            " 908 folder.jpg",
            "2201 cover.jpg"
        );
        Map<Boolean, List<String>> ignoredMatchedNames = mostFrequentNames.stream().collect(partitioningBy(s -> s.startsWith("-")));
        List<String> ignoredNames = ignoredMatchedNames.get(Boolean.TRUE).stream().map(s -> s.replaceFirst("-", "").trim().substring(s.trim().indexOf(" ") + 1)).collect(toList());
        List<String> matchedNames = ignoredMatchedNames.get(Boolean.FALSE).stream().map(s -> s.trim().substring(s.trim().indexOf(" ") + 1)).collect(toList());
        assertEquals(mostFrequentNames.size(), matchedNames.size() + ignoredNames.size());
        matchedNames.forEach(name -> assertTrue("Name not matching: " + name, MusicOps.matchArtworkFileName(name) != Integer.MAX_VALUE));
        ignoredNames.forEach(name -> {
            int x = MusicOps.matchArtworkFileName(name);
            assertTrue("Name matching " + x + ": " + name, x == Integer.MAX_VALUE);
        });

        // Special cases
        List<String> specialMatching = asList("Kwassa Kwassa(front).jpg", "front pic.pg");
        specialMatching.forEach(name -> assertTrue("Name not matching: " + name, MusicOps.matchArtworkFileName(name) != Integer.MAX_VALUE));

        List<String> specialIgnored = asList("Kwassa Kwassa(frontman).jpg");
        specialIgnored.forEach(name -> assertTrue("Name matching: " + name, MusicOps.matchArtworkFileName(name) == Integer.MAX_VALUE));

        // Test sort ranks
        assertTrue("rank cover (front).jpg", matchArtworkFileName("cover (front).jpg") < matchArtworkFileName("cover (back).jpg"));
        assertTrue("rank cover-001.jpg", matchArtworkFileName("cover-001.jpg") < matchArtworkFileName("cover-002.jpg"));
        assertTrue("rank cover (1).jpg", matchArtworkFileName("cover (1).jpg") < matchArtworkFileName("cover (2).jpg"));
        assertTrue("rank cover 1.jpg", matchArtworkFileName("cover 1.jpg") < matchArtworkFileName("cover 3.jpg"));
        assertTrue("rank CD-cover-1.jpg", matchArtworkFileName("CD-cover-1.jpg") < matchArtworkFileName("CD-cover-2.jpg"));
        assertTrue("rank !cover_1.jpg", matchArtworkFileName("!cover_1.jpg") < matchArtworkFileName("!cover_2.jpg"));
        assertTrue("rank front.jpg", matchArtworkFileName("front.jpg") < matchArtworkFileName("front in.jpg"));
        assertTrue("rank front.jpg", matchArtworkFileName("front.jpg") < matchArtworkFileName("disc1.jpg"));
        assertTrue("rank front.jpg", matchArtworkFileName("front.jpg") < matchArtworkFileName("cover2.jpg"));
        assertTrue("rank Foto.jpg", matchArtworkFileName("Foto.jpg") < matchArtworkFileName("B.B. King - Swing Low Sweet Chariot - CD.jpg"));
        assertTrue("rank B.B. King - Swing Low Sweet Chariot - Front.jpg", matchArtworkFileName("B.B. King - Swing Low Sweet Chariot - Front.jpg") < matchArtworkFileName("B.B. King - Swing Low Sweet Chariot - CD.jpg"));
        assertTrue("rank AlbumArt_{8F752E05-F14D-4927-AE9C-E798CA79E198}_Large.jpg", matchArtworkFileName("AlbumArt_{8F752E05-F14D-4927-AE9C-E798CA79E198}_Large.jpg") < matchArtworkFileName("R-565762-1159313589.jpg"));
    }

    @Test
    public void getPlaylist() {
        assertEquals("Library", MusicOps.getPlaylist(playlistName -> playlistName.toLowerCase().equals("library")));
        assertEquals("Music", MusicOps.getPlaylist(playlistName -> playlistName.startsWith("Mu")));
    }

    @Test
    public void getPlaylistSizeTest() {
        MusicOps.runItems(getPlaylistSize(testMp3DirName));
    }

    @Test
    public void getTracks() {
        int batchSize = 9;
        int missingTracksCount = 2;

        // Create 15 MP3s in 3 dirs.
        TestFiles testFiles = new TestFiles(3, 5, 1).create();
        MusicOps.getPlaylist(testPlaylist);
        MusicOps.addTracks(testPlaylist, testDir);

        // Delete MP3s to test missing tracks.
        List<Integer> missingTracksIndexes = getRandomIndexes(testFiles.getCount(), missingTracksCount);
        missingTracksIndexes.forEach(i -> {
            try {
                Files.delete(testFiles.file(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        TrackList tracks = MusicOps.getTracks(testPlaylist, batchSize);
        assertEquals(testFiles.getCount(), tracks.getTotalCount());
        assertEquals(testFiles.getCount() - missingTracksCount, tracks.getFound().size());
        assertEquals(missingTracksCount, tracks.getMissing().size());
    }

    public void logPic(Artwork pic, Path trackPath) {
        try {
            log.debug(Str.of("  tag1, type ${1}, mime ${2}, size ${3} x ${4}, linked? ${5}, url ${6}, desc ${7}, ${8} bytes").with(
                pic.getPictureType(),
                pic.getMimeType(),
                pic.getImage().getWidth(),
                pic.getImage().getHeight(),
                pic.isLinked(),
                pic.getImageUrl(),
                pic.getDescription(),
                pic.getBinaryData()
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logTrackArtwork(Path trackPath) {
        log.debug(trackPath);
        Mp3File mp3File = new Mp3File(trackPath);
        ID3v1Tag tag1 = mp3File.getTag1();
        AbstractID3v2Tag tag2 = mp3File.getTag2();
        if (tag1 != null && !tag1.getArtworkList().isEmpty()) {
            tag1.getArtworkList().forEach(pic -> {
                logPic(pic, trackPath);
            });
        } else if (tag2 != null && !tag2.getArtworkList().isEmpty()) {
            tag2.getArtworkList().forEach(pic -> logPic(pic, trackPath));
        } else {
            log.debug("  no pic" + (tag1 == null ? "/tag1" : "") + (tag2 == null ? "/tag2" : ""));
        }
    }

    @Test
    public void matchTrackFileName() {
        List<String> trackFileNames1 = asList(
            "01. Milanese - Mr Good News.mp3",
            "A. Skitty - Sweet Vibrations.mp3",
            "11 Peter Hype - Champion Girls.mp3.mp3",
            "0A.Dillinja - Good Girl.mp3",
            "01-Augustus_Pablo_-_Meditation_Dub.mp3",
            "B_-_Shimon_-_Within_Reason_(Liftin'_Spirits_remix).mp3",
            "AA1_-_Ram_Trilogy_-_Scanners.mp3",
            "aa vista - exit wounds.mp3"
        );
        List<String> trackFileNames2 = asList(
            "01-international_observer-house_of_the_rising_dub-wws.mp3",
            "01-machine_drum-911_(feat_ie.merg).mp3",
            "01-chus_and_ceballos--nobody_freaks_like_us_(original_club_mix)-wus.mp3",
            "aa-chase_and_status-judgement_(informer)-sour.mp3"
        );
        List<String> trackFileNames3 = asList(
            "01. Guns of Navarone.mp3",
            "A.1.Sai.mp3",
            "0b2. Theme from Murdercapital (The Ride).mp3",
            "AA. Moongerm.mp3",
            "aa1_Cats and Dogs (Farina's Kennel Club Mix).mp3"
        );

        // Test
        trackFileNames1.forEach(file -> assertEquals(file, 1, MusicOps.matchTrackFileName(file)));
        trackFileNames2.forEach(file -> assertEquals(file, 2, MusicOps.matchTrackFileName(file)));
        trackFileNames3.forEach(file -> assertEquals(file, 3, MusicOps.matchTrackFileName(file)));
    }

    @Test
    public void printTags() {
        Path dir = Paths.get("/Volumes/alpaca/itunes/new");
        List<Path> paths = FileUtil.getAudioFilePaths(dir);
        log.debug(Str.of("Printing ${1} files").with(paths.size()));
        int batch = 500;
        iterate(0, i -> i < paths.size(), i -> i + batch).forEach(batchStart -> {
            int batchEnd = Math.min(batchStart + batch, paths.size());
            log.debug(Str.of("Batch ${1}-${2}").with(batchStart, batchEnd - 1));
            List<String> lines = iterate(batchStart, j -> j < batchEnd, j -> j + 1).mapToObj(fileIndex -> {
                Mp3File file = new Mp3File(paths.get(fileIndex));
                return Str.of("${1}§${2}§${3}§${4}§${5}§${6}§${7}").with(
                    file.getFilePath(),
                    defaultNonNullString(file.getTag(TRACK_NO), "--"),
                    defaultNonNullString(file.getTag(TRACK_TOTAL), "--"),
                    defaultNonNullString(file.getTag(TITLE), "--"),
                    defaultNonNullString(file.getTag(ARTIST), "--"),
                    defaultNonNullString(file.getTag(ALBUM), "--"),
                    defaultNonNullString(file.getTag(YEAR), "--")
                );
            }).collect(toList());
            log.debug(Str.of("Write ${1} lines").with(lines.size()));
            FileUtil.appendLines(Paths.get("/Users/vadim/Documents/work/vadim/empi/etc/tags-new.txt"), lines);
        });
    }

    @Test
    public void readFilePaths() throws IOException {
        TestFiles mock = new TestFiles(2, 5, 1).create();
        Path loveList = Paths.get(testDir + "/love.txt");
        List<String> lines = new ArrayList<>(asList("# comment line in a file"));
        List<String> expectedPaths = getRandomTrackPaths(mock.getPaths(), mock.getCount() / 2).stream().map(Path::toString).collect(toList());
        lines.addAll(expectedPaths);
        Files.write(loveList, lines);
        List<String> actualPaths = FileUtil.readFilePaths(loveList).stream().map(Path::toString).collect(toList());
        assertEquals(expectedPaths, actualPaths);
    }

    @Test
    public void setLovedTracks() {
        TestFiles mock = new TestFiles(2, 5, 1).create();
        MusicOps.addTracks(testPlaylist, testDir);
        List<Path> expectedLovedTracks = getRandomTrackPaths(mock.getPaths(), mock.getCount() / 2);
        MusicOps.setLovedTracks(testPlaylist, expectedLovedTracks);
        List<Path> actualTracks = MusicOps.getTracksLoved(testPlaylist).getFound();
        assertEquals(expectedLovedTracks, actualTracks);
    }

    @Test
    public void setLovedTracks2() {
        MusicOps.setLovedTracks("phone-01-2020", Paths.get("/Volumes/alpaca/itunes/phone-01-2020-incomplete.love"));
    }

    @Test
    public void sortArtworkPaths() {
        Path backCover = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008/Scans/back cover.jpg");
        Path book2 = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008/Scans/book2.jpg");
        Path book3 = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008/Scans/book3.jpg");
        Path cdNested = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008/Scans/cd.jpg");
        Path coverNested = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008/Scans/cover.jpg");
        Path picture1 = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008/picture1.jpg");
        Path cover = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008/cover.jpg");
        Path notArt = Paths.get("/Volumes/alpaca/itunes/phone-01-2020/Portishead - Third (Universal Island Records 1764013) 2008/not-art.jpg");

        List<Path> paths = asList(backCover, book2, book3, cdNested, coverNested, picture1, cover, notArt);

//        Pattern re = Pattern.compile(ARTWORK_COVER_RE, Pattern.CASE_INSENSITIVE);
//        assertTrue("Artwork regular expression doesn't match path", re.matcher("Folder.png").matches());
//        assertTrue("Artwork regular expression doesn't match path", re.matcher(coverNested.toString()).matches());

        assertEquals(asList(cover, coverNested, picture1, cdNested, notArt, backCover, book2, book3), MusicOps.sortPicturePaths(paths));
    }

    @Test
    public void sortTracksByLove() throws IOException {
        List<Path> lovedAlbumDirs = new ArrayList<>();
        List<Path> unlovedAlbumDirs = new ArrayList<>();
        Path sourceDir = FileUtil.mkdir(testDir, "source");
        Path goodDir = FileUtil.mkdir(testDir, "good");
        Path badDir = FileUtil.mkdir(testDir, "bad");

        // Album 1 with tracks 1 and 3 set to loved.
        TestFiles mock = new TestFiles(sourceDir).type(SINGLE_ALBUM).create();
        MusicOps.addTracksWithLove(testPlaylist, mock.dir(0), asList(mock.file(0), mock.file(2)));
        lovedAlbumDirs.add(goodDir.resolve(mock.dir(0).getFileName()));

        // Album 2 with two subfolders, with track 1 of subfolder 1 and track 1 of subfolder 2 set to loved.
        mock = new TestFiles(sourceDir).type(SINGLE_DOUBLE_ALBUM).create();
        MusicOps.addTracksWithLove(testPlaylist, mock.dir(0), asList(mock.file(0), mock.file(5)));
        lovedAlbumDirs.add(goodDir.resolve(mock.dir(0).getFileName()));

        // Album 3 without loved tracks.
        mock = new TestFiles(sourceDir).type(SINGLE_ALBUM).create();
        MusicOps.addTracks(testPlaylist, mock.dir(0));
        unlovedAlbumDirs.add(badDir.resolve(mock.dir(0).getFileName()));

        // Album 4 with two subfolders, without loved tracks.
        mock = new TestFiles(sourceDir).type(SINGLE_DOUBLE_ALBUM).create();
        MusicOps.addTracks(testPlaylist, mock.dir(0));
        unlovedAlbumDirs.add(badDir.resolve(mock.dir(0).getFileName()));

        // Discography with 1 album (loved) and 2 singles (unloved and loved).
        Path disco = FileUtil.mkdir(sourceDir, "artist discography");
        Path albums = FileUtil.mkdir(disco, "albums");
        Path singles = FileUtil.mkdir(disco, "singles");
        mock = new TestFiles(albums).type(SINGLE_ALBUM).create();
        MusicOps.addTracksWithLove(testPlaylist, mock.dir(0), asList(mock.file(0)));
        lovedAlbumDirs.add(goodDir.resolve(mock.dir(0).getFileName()));
        mock = new TestFiles(singles).dirCount(2).create();
        MusicOps.addTracksWithLove(testPlaylist, singles, asList(mock.file(6)));
        unlovedAlbumDirs.add(badDir.resolve(mock.dir(0).getFileName()));
        lovedAlbumDirs.add(goodDir.resolve(mock.dir(1).getFileName()));

        lovedAlbumDirs.sort(Comparator.naturalOrder());
        unlovedAlbumDirs.sort(Comparator.naturalOrder());

        // Test
        MusicOps.sortDirsByLove(testPlaylist, sourceDir, goodDir, badDir);
        assertEquals(lovedAlbumDirs, Files.list(goodDir).sorted().collect(toList()));
        assertEquals(unlovedAlbumDirs, Files.list(badDir).sorted().collect(toList()));
    }

    @Test
    public void tagFromFileName() {
        TestFiles mock = new TestFiles(testDir).type(SINGLE_ALBUM).create();
        MusicOps.addTracks(testPlaylist, mock.dir(0));
        MusicOps.tagFromFileName(testPlaylist);
    }

    private AudioArtwork getArtworkFromTag(Path file) {
        return new Mp3File(file).getTag2Artwork();
    }

    private List<Integer> getRandomIndexes(int maxIndex, int indexCount) {
        List<Integer> indexes = new ArrayList<>();
        while (indexes.size() < indexCount) {
            int[] availableIndexes = IntStream.range(0, maxIndex).filter(i -> !indexes.contains(i)).toArray();
            indexes.add(availableIndexes[Util.rand(availableIndexes.length)]);
        }
        indexes.sort(naturalOrder());
        return indexes;
    }

    private List<Path> getRandomTrackPaths(List<Path> trackPaths, int tracksCount) {
        return getRandomIndexes(trackPaths.size(), tracksCount).stream().map(trackPaths::get).collect(toList());
    }


    enum MockType {
        DEFAULT, SINGLE_ALBUM, SINGLE_DOUBLE_ALBUM, SINGLE_BASE_DIR
    }




    class TestFiles {

        String album;
        String artist;
        Path baseDir;
        boolean createAlbumSubdirs = false;
        List<Path> createdDirs = new ArrayList<>();
        List<Path> createdFiles = new ArrayList<>();
        int dirCount = 1;
        int dirDepth = 1;
        int filesCount = 5;
        List<Mp3Tag> mockedTags = new ArrayList<>(asList(ARTIST, ALBUM, TRACK_TOTAL, TRACK_NO, TITLE));
        boolean randomize;
        MockType type = DEFAULT;

        public TestFiles(Path baseDir) {
            this.baseDir = baseDir;
        }


        public TestFiles(Path baseDir, int dirCount, int dirFilesCount, int dirDepth, boolean createAlbumSubdirs, boolean randomize) {
            this(baseDir);
            this.dirCount = dirCount;
            this.filesCount = dirFilesCount;
            this.dirDepth = dirDepth;
            this.createAlbumSubdirs = createAlbumSubdirs;
            this.randomize = randomize;
        }

        public TestFiles(Path baseDir, int dirCount, int dirFilesCount, int dirDepth) {
            this(baseDir, dirCount, dirFilesCount, dirDepth, true, false);
        }

        public TestFiles(int dirCount, int dirFilesCount, int dirDepth, boolean createAlbumSubdirs, boolean randomize) {
            this(testDir, dirCount, dirFilesCount, dirDepth, createAlbumSubdirs, randomize);
        }

        public TestFiles(int dirCount, int dirFilesCount, int dirDepth) {
            this(testDir, dirCount, dirFilesCount, dirDepth, true, false);
        }

        public TestFiles create() {
            reset();
            createDir(baseDir, 0);
            createdDirs.sort(naturalOrder());
            createdFiles.sort(naturalOrder());
            return this;
        }

        public void createDir(Path dir, int depth) {
            if (depth < dirDepth) {
                IntStream.range(0, getDirCount(depth)).forEachOrdered(dirIndex -> {
                    artist = randWords(WORDS);
                    album = randWords(WORDS, 2);
                    List<String> existingSiblings = FileUtil.list(dir).stream().map(path -> path.getFileName().toString()).collect(toList());
                    String newSiblingName = getDirName(depth);
                    while (existingSiblings.contains(newSiblingName)) {
                        newSiblingName = getDirName(depth);
                    }
                    Path albumDir = FileUtil.mkdir(dir, newSiblingName);
                    createdDirs.add(albumDir);
                    createDir(albumDir, depth + 1);
                });
            }
            // If deepest dir in hierarchy, or randomly true - create random number of MP3s in this dir.
            else if (depth == dirDepth /*|| randBool()*/) {
                int filesCount = (randomize ? rand(this.filesCount) : this.filesCount);
                IntStream.rangeClosed(1, filesCount).forEachOrdered(fileIndex -> {
                    String trackName = getTrackName();
                    Path filePath = FileUtil.copy(testMp3, FileUtil.getPath(dir, fileIndex + " " + trackName + ".mp3"));
                    Mp3File mp3File = new Mp3File(filePath);
                    if (mockedTags.contains(ARTIST)) mp3File.tag(ARTIST, artist);
                    if (mockedTags.contains(ALBUM)) mp3File.tag(ALBUM, album);
                    if (mockedTags.contains(TRACK_TOTAL)) mp3File.tag(TRACK_TOTAL, filesCount);
                    if (mockedTags.contains(TRACK_NO)) mp3File.tag(TRACK_NO, fileIndex);
                    if (mockedTags.contains(TITLE)) mp3File.tag(TITLE, trackName);
                    mp3File.save();
                    createdFiles.add(filePath);
                });
            }
        }

        public Path dir(int i) {
            return createdDirs.get(i);
        }

        public TestFiles dirCount(int dirCount) {
            this.dirCount = dirCount;
            return this;
        }

        public TestFiles dirDepth(int dirDepth) {
            this.dirDepth = dirDepth;
            return this;
        }

        public Path file(int i) {
            return createdFiles.get(i);
        }

        public TestFiles filesCount(int filesCount) {
            this.filesCount = filesCount;
            return this;
        }

        public int getCount() {
            return getPaths().size();
        }

        public int getCreatedAlbumDirsCount() {
            return getCreatedDirs().size();
        }

        public List<Path> getCreatedDirs() {
            return createdDirs;
        }

        public List<Path> getPaths() {
            return createdFiles;
        }

        public TestFiles noAlbum() {
            mockedTags.remove(ALBUM);
            return this;
        }

        public TestFiles noArtist() {
            mockedTags.remove(ARTIST);
            return this;
        }

        public TestFiles noTitle() {
            mockedTags.remove(TITLE);
            return this;
        }

        public TestFiles noTrackNumber() {
            mockedTags.remove(TRACK_NO);
            return this;
        }

        public TestFiles randomize() {
            this.randomize = true;
            return this;
        }

        public void reset() {
            createdFiles.clear();
            createdDirs.clear();
        }

        public TestFiles type(MockType type) {
            this.type = type;
            switch (type) {
                case SINGLE_ALBUM:
                    dirDepth = 1;
                    break;
                case SINGLE_DOUBLE_ALBUM:
                    dirDepth = 2;
                    createAlbumSubdirs = true;
                    break;
            }
            return this;
        }

        private int getDirCount(int depth) {
            int defaultCount = (randomize ? rand(dirCount) : dirCount);
            switch (type) {
                case SINGLE_ALBUM:
                    switch (depth) {
                        case 0:
                            return 1;
                        default:
                            return defaultCount;
                    }
                case SINGLE_DOUBLE_ALBUM:
                    switch (depth) {
                        case 0:
                            return 1;
                        case 1:
                            return 2;
                        default:
                            return defaultCount;
                    }
                case SINGLE_BASE_DIR:
                    switch (depth) {
                        case 0:
                            return 1;
                        default:
                            return defaultCount;
                    }
                default:
                    return defaultCount;
            }
        }

        private String getDirName(int depth) {
            return (depth == dirDepth - 1 && dirDepth > 1 && createAlbumSubdirs ? Util.randWord(ALBUM_SUBDIRS) : artist + " - " + album);
        }

        private String getTrackName() {
            return randWords(WORDS, 3);
        }
    }
}