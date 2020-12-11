package eu.tsvetkov.empi.ops;

import eu.tsvetkov.empi.itunes.AppleScript;
import eu.tsvetkov.empi.itunes.Script;
import eu.tsvetkov.empi.itunes.ScriptApp;
import eu.tsvetkov.empi.model.AudioArtwork;
import eu.tsvetkov.empi.model.Playlist;
import eu.tsvetkov.empi.model.TrackId;
import eu.tsvetkov.empi.model.TrackList;
import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.util.FileUtil;
import eu.tsvetkov.empi.util.SLogger;
import eu.tsvetkov.empi.util.Str;
import eu.tsvetkov.empi.util.Util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.tsvetkov.empi.itunes.AppleScript.getTrackList;
import static eu.tsvetkov.empi.util.FileUtil.fileCount;
import static eu.tsvetkov.empi.util.Str.Ansi.*;
import static eu.tsvetkov.empi.util.Str.cut;
import static eu.tsvetkov.empi.util.Util.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.iterate;

/**
 * TODO
 * - adding files sorted by track number if present
 * - adding files with a love list
 */

public class MusicOps {

    private static final List<String> ALBUM_SUBDIR_RE = asList(
        "0?[1-9]",
        "(?:cd|chapter|disc|disk|lp|part|reel|side|vol.?|volume|сторона).?(0?[1-9]|[a-d]|i*?v*?x*?|one|two|three|four|five).?(?:side)?.*",
        "(cd|itunes|songs|vinyl|vynil)",
        "(?:cd|vinyl|vynil)?.?(?:bonus|bônus|extra|instrumentals|mix|mixed).?(?:cd|tracks|trax)?"
    );
    private static final List<String> ARTWORK_NAMES_RE = asList(
        "(?:!|00|cd|vinyl)?[^0-9]?(cover|кавер|обложка)[^0-9]?\\(?(?:cd|front)?[^0-9]?(?:0*?1)?\\)?",   // rank 1
        "(album|folder)",                                                                               // rank 2
        "(?:00|cd|vinyl)?[^0-9]?front[^0-9]?(?:0*?1)?",                                                 // rank 3
        "(box|digipack|vinyl)",                                                                         // rank 4
        "(album)?art(work)?([\\-_{}0-9a-f]*)?(large|small)?",                                           // rank 5
        "(foto|pic(?:ture)?)[^0-9]?(?:0*?1)?",                                                          // rank 6
        "(cd|disc|disk)(?:0*?1)?",                                                                      // rank 7
        "(?:side|vinyl|сторона)?.?a(.side)?",                                                           // rank 8
        "(?:0*?|side[^0-9])?1",                                                                         // rank 9
        "label",                                                                                        // rank 10
        "(img|scan|скан)(?:0*?1)?",                                                                     // rank 11
        ".*\\b(cover|front)[_]?\\d*\\b.*"                                                               // rank 12
    );
    private static final String ARTWORK_SUBDIRS_RE = ".*(artwork|booklet|cover|scan|скан).*";
    private static final Path DUMMY_MP3 = Paths.get("/Users/vadim/Documents/work/vadim/empi/src/test/resources/mp3/01s.mp3");
    private static final String FILE_WITHOUT_EXTENSION_RE = "(?:.*" + FileUtil.SEP + ")?(.*)\\.[^.]*$";
    private static final List<String> TAGGING_LIST_NAMES = asList("MP3s", "🟢 CHANGED", "🔴 UNCHANGED");
    //    public static final String ARTWORK_COVER_RE = "(?:.*" + FileUtil.SEP + ")?((01|a|a.side|album|album.?art|art|artwork|cover|cd|disc|folder|front|label|scan|side.a|диск|кавер|обложка|скан))\\..*$";
    private static final int TRACKS_BATCH_SIZE = 500;
    private static final List<String> TRACK_FILE_NAME_RE = asList(
        "(?<no>\\d+|0?[a-dA-D]+[.]?\\d*)[ ._\\-]+(?<artist>.+)(?: - )(?<track>.+)",                        // number, artist, title
        "(?<no>\\d+|0?[a-dA-D]+[.]?\\d*)-(?<artist>[^-]+)-+(?<track>[^-]+)(?:-(?<group>[^-]+))?",          // number, artist, title, release group
        "(?<no>\\d+|0?[a-dA-D]+[.]?\\d*)[ ._\\-]+(?<track>.+)"                                             // number, title
    );
    private static final Pattern TRACK_NAME_FROM_FILE = Pattern.compile("\\d+[.]?[ ]?(?<trackName>.+)\\.mp3");
    private static Map<Path, AudioArtwork> cachedAlbumDirArtwork = new HashMap<>();
    private static Map<Path, Mp3File> cachedMp3Files = new HashMap<>();
    private static SLogger log = new SLogger();
    private static List<Path> pictureFiles;
    //    private static List<Path> pictureFiles;
    private static Path tmpArtworkDir;
    private static Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

    static {
        tmpArtworkDir = FileUtil.mkdir(tmpDir, "empi/pic");
    }

    public static void addTracks(String playlist, Path dir) {
        List<Path> audioFilePaths = FileUtil.getAudioFilePaths(dir);
        log.info(Str.of("Adding ${1} audio files to playlist '${2}'").with(audioFilePaths.size(), playlist));
        audioFilePaths.forEach(path -> runItems(AppleScript.addTrackFromLibOrFile(playlist, path, TrackId.of(path))));
    }

    public static void addTracksBatch(String playlist, Path dir) {
        List<Script> scripts = new ArrayList<>();
        requireNonNull(FileUtil.getAudioFilePaths(dir)).forEach(path -> scripts.add(AppleScript.addTrackFromLibOrFile(playlist, path, TrackId.of(path))));
        runScripts(scripts);
    }

    public static void addTracksLazyPath(String playlist, Path dir) {
        // Cleanup the temp directory.
        Path tmpEmpiDir = tmpDir.resolve("empi");
        FileUtil.rmDir(tmpEmpiDir);
        Path tmpMp3Dir = FileUtil.mkdir(tmpDir, "empi/mp3");
        tmpArtworkDir = FileUtil.mkdir(tmpDir, "empi/pic");

        List<List<Path>> audioAndArtworkFilePaths = findAudioAndArtworkFilePaths(dir);
        List<Path> audioFiles = audioAndArtworkFilePaths.get(0);
        pictureFiles = audioAndArtworkFilePaths.get(1);

        // Create temp artwork files, map them to dirs.
        log.debug("Creating temp artwork files in dir `${1}`", tmpArtworkDir);
        List<Path> artworkPaths = audioFiles.stream().map(path -> getArtworkPath(path, dir)).collect(toList());

        // Create necessary number of dummy MP3s.
        int audioFilePathCount = audioFiles.size();
        log.debug(Str.of("Creating ${1} dummy MP3s").with(audioFilePathCount));
        List<Path> dummyFiles = IntStream.rangeClosed(1, audioFilePathCount).mapToObj(i -> FileUtil.copy(DUMMY_MP3, tmpMp3Dir.resolve(i + ".mp3")).getFileName()).collect(toList());

        // Get the original playlist size.
        int playlistSize = getPlaylistSize(playlist);

        // Add dummies to the playlist in batches.
        runBatchScript(audioFilePathCount, TRACKS_BATCH_SIZE, i -> AppleScript.addTracksFromFilesInDir(playlist, tmpMp3Dir, dummyFiles.subList(i, Math.min(i + TRACKS_BATCH_SIZE, audioFilePathCount))));

        // Set actual file paths and artwork on the dummy tracks.
        log.debug(Str.of("Setting ${1} track locations").with(audioFilePathCount));
        runItems(AppleScript.setTrackLocationsAndArtwork(playlist, playlistSize, audioFiles, artworkPaths));
        audioFiles.forEach(audioFile -> {
            AudioArtwork artwork = getArtwork(audioFile, dir);
            if (artwork != null && artwork.isFile()) {
                getMp3File(audioFile).setTag2Artwork(artwork).save();
            }
        });

        // Cleanup the temp directory.
//        FileUtil.rmDir(tmpEmpiDir);
    }

    public static void addTracksWithLove(String playlist, Path dir, Path loveList) {
        getPlaylist(playlist);
        addTracks(playlist, dir);
        if (loveList != null) {
            setLovedTracks(playlist, FileUtil.readFilePaths(loveList));
        }
    }

    public static void addTracksWithLove(String playlist, Path dir, List<Path> lovedTrackPaths) {
        getPlaylist(playlist);
        addTracks(playlist, dir);
        setLovedTracks(playlist, lovedTrackPaths);
    }

    public static void deleteTracks(String playlist, String dir) {
        deleteTracks(playlist, Paths.get(dir));
    }

    public static void deleteTracks(String playlist, Path dir) {
        FileUtil.getAudioFilePaths(dir).forEach(path -> runItems(AppleScript.deleteTrackFromPlaylist(playlist, TrackId.of(getMp3File(path)))));
    }

    public static void deleteTracks(String playlist) {
        System.out.println("Deleting tracks from playlist '" + playlist + "'");
        runItems(AppleScript.deleteAllTracks(playlist));
    }

    public static void deleteTracksFromLibrary(Path dir) {
        FileUtil.getAudioFilePaths(dir).forEach(path -> runItems(AppleScript.deleteTrackFromLibrary(TrackId.of(path))));
    }

    public static void deleteTracksFromLibraryBatch(Path dir) {
        runScripts(FileUtil.getAudioFilePaths(dir).stream().map(x -> AppleScript.deleteTrackFromLibrary(TrackId.of(x))).collect(toList()));
    }

    public static void exportLove(String playlist, Path textFileDir) {
        TrackList lovedTracks = getTracksLoved(playlist);
        FileUtil.writeLines(textFileDir.resolve(playlist + ".love"), lovedTracks.toString());
        log.debug("Loved tracks\n" + lovedTracks.toShortString());
    }

    public static AudioArtwork findArtworkInPath(Path path, Path topDir) {
        if (!path.startsWith(topDir)) {
            return null;
        }
        // If searching in a dir:
        if (FileUtil.isDir(path)) {
            log.debug(Str.of("Searching artwork for dir path `${1}`").with(cut(path)));
            // For the top-level dir
            if (path.equals(topDir)) {
                List<Path> audioFilePaths = FileUtil.getAudioFilePaths(path);
                // If the dir contains a single album - search for artwork as usual, otherwise don't.
                if (isSingleAlbum(audioFilePaths)) {
                    log.debug(Str.of("Single album in top dir `${1}`").with(cut(path)));
                    return findArtworkInDir(path);
                } else {
                    log.debug(Str.of("Not searching in top dir `${1}`").with(cut(path)));
                    return null;
                }
            } else {
                AudioArtwork artwork = findArtworkInDir(path);
                // If not found in an album subdir, search in the parent album dir
                if (artwork == null && isAlbumSubdir(path)) {
                    log.debug(Str.of("Going up from album subdir to parent dir `${1}`").with(cut(path.getParent())));
                    artwork = findArtworkInDir(path.getParent());
                }
                return artwork;
            }
        }
        // Else if searching for an audio file - search in its parent dir.
        else {
            log.debug(Str.of("Searching artwork for audio file path `${1}`: go to parent dir").with(cut(path)));
            return findArtworkInPath(path.getParent(), topDir);
        }
    }

    public static List<List<Path>> findAudioAndArtworkFilePaths(Path dir) {
        List<List<Path>> paths = FileUtil.getFilePathsLists(dir, FileUtil::isAudioFile, FileUtil::isPictureFile);
        paths.set(1, sortPicturePaths(paths.get(1)));
        return paths;
    }

    public static List<String> getAllPlaylists() {
        return runItems(AppleScript.getAllPlaylistNames()).toList();
    }

    // TODO cache negative search results too!
    public static AudioArtwork getArtwork(Path audioFile, Path topDir) {
        if (cachedAlbumDirArtwork.containsKey(audioFile.getParent())) {
            AudioArtwork artwork = cachedAlbumDirArtwork.get(audioFile.getParent());
            log.debugGreen("Artwork cache for parent dir `${1}`: `${2}`", cut(audioFile.getParent()), cut(artwork.getPath()));
            return artwork;
        }
        if (cachedAlbumDirArtwork.containsKey(audioFile)) {
            AudioArtwork artwork = cachedAlbumDirArtwork.get(audioFile);
            log.debugGreen("Artwork cache for audio file `${1}`: `${2}`", cut(audioFile), cut(artwork.getPath()));
            return artwork;
        }
        AudioArtwork artwork = createTempArtworkFromFile(audioFile, tmpArtworkDir.resolve(fileCount(tmpArtworkDir) + ".jpg"), topDir);
        if (artwork != null) {
            log.debug("Caching temp artwork file `${1}`", cut(artwork.getPath()));
            cachedAlbumDirArtwork.put((artwork.isFile() ? audioFile.getParent() : audioFile), artwork);
        } else {
            log.debugRed("Artwork not found, returning null");
        }
        // TODO extract artwork from MP3 and cache for individual MP3's path
        return artwork;
    }

    public static Mp3File getMp3File(Path path) {
        if (!cachedMp3Files.containsKey(path)) {
            cachedMp3Files.put(path, new Mp3File(path));
        }
        return cachedMp3Files.get(path);
    }

    @SafeVarargs
    public static String getPlaylist(Predicate<String>... nameMatchers) {
        List<String> playlists = runItems(AppleScript.getAllPlaylistNames()).toList();
        return Util.findFirst(playlists, nameMatchers);
    }

    public static void getPlaylist(String playlist) {
        runItems(AppleScript.getPlaylist(playlist));
    }

    public static Integer getPlaylistSize(String playlist) {
        return Integer.valueOf(runItems(AppleScript.getPlaylistSize(playlist)).getOutput());
    }

    public static TrackList getTracks(String playlistName, int batchSize) {
        Playlist playlist = new Playlist(playlistName);
        return getTracksForScript(playlist, batchSize, i -> getTrackPathsBatchScript(playlist, i, batchSize));
    }

    public static TrackList getTracksLoved(String playlistName) {
        Playlist playlist = new Playlist(playlistName);
        return getTracksForScript(playlist, TRACKS_BATCH_SIZE, i -> getTrackPathsBatchScript(playlist, i, TRACKS_BATCH_SIZE, x -> Str.of("${1}'s loved").with(x)));
    }

    public static int matchArtworkFileName(String fileName) {
        return matchFileName(fileName, "artwork file name", ARTWORK_NAMES_RE, Str::toLowerCase);
    }

    public static int matchTrackFileName(String fileName) {
        return matchFileName(fileName, "track file name", TRACK_FILE_NAME_RE, s -> s.contains(" ") ? s : s.replaceAll("_", " "));
    }

    public static Results run(List<String> scripts) {
        return runScripts(scripts.stream().map(x -> new Script(x, Util.getCurrentMethodName(6))).collect(toList()));
    }

    public static ScriptRun run(String script) {
        return MusicOps.run(Collections.singletonList(script)).get(0);
    }

    public static ScriptRun runItems(Script script) {
        return runScripts(Collections.singletonList(script)).get(0);
    }

    public static int runItemsInteger(Script script) {
        return Integer.valueOf(runScripts(Collections.singletonList(script)).get(0).getOutput());
    }

    public static void setLovedTracks(String playlist, Path loveListFile) {
        setLovedTracks(playlist, FileUtil.readFilePaths(loveListFile));
    }

    public static void setLovedTracks(String playlist, List<Path> tracks) {
        log.debug(Str.of("Setting ${1} tracks as loved").with(tracks.size()));
        runScripts(tracks.stream().map(track -> AppleScript.loveTrack(playlist, TrackId.of(track))).collect(toList()));
    }

    public static void sortDirsByLove(String playlist, Path sourceDir, Path goodDir, Path badDir) {
        // TODO get the lists in one go:
        List<Path> tracks = getTracks(playlist).getFound();
        List<Path> lovedTrackPaths = getTracksLoved(playlist).getFound();
        // TODO: optimize partitioning:
        Set<Path> allAlbums = tracks.stream().map(track -> getAlbumDir(track, sourceDir)).collect(toSet());
        Set<Path> goodAlbums = lovedTrackPaths.stream().map(track -> getAlbumDir(track, sourceDir)).collect(toSet());
        Set<Path> badAlbums = allAlbums.stream().filter(album -> !goodAlbums.contains(album)).collect(toSet());
        // TODO special case: sourceDir contains a single album with possible subdirs
        goodAlbums.forEach(dir -> FileUtil.mvToDir(dir, goodDir));
        badAlbums.forEach(dir -> FileUtil.mvToDir(dir, badDir));
        // TODO: deal with missing files
    }

    public static List<Path> sortPicturePaths(List<Path> paths) {
        log.debug("Sorting ${1} pictures", paths.size());
        return Str.sort(paths, FILE_WITHOUT_EXTENSION_RE, MusicOps::matchArtworkFileName, FileUtil::getPathDepth);
    }

    public static void tagFromFileName(String playlist) {
        TrackList tracks = getTracks(playlist);
        log.debug(tracks);
        Map<Boolean, List<Path>> changedUnchangedFiles = tracks.getFound().stream().collect(Collectors.partitioningBy(path -> {
            String fileName = path.getFileName().toString();
            Matcher matcher = TRACK_NAME_FROM_FILE.matcher(fileName);
            if (matcher.find()) {
                getMp3File(path).tag(Mp3Tag.TITLE, matcher.group("trackName")).save();
                return true;
            }
            return false;
        }));
        log.debug(Util.toString(TAGGING_LIST_NAMES, changedUnchangedFiles.get(Boolean.TRUE), changedUnchangedFiles.get(Boolean.FALSE)));
    }

    private static AudioArtwork createTempArtworkFromFile(Path file, Path tempArtworkPath, Path topDir) {
        AudioArtwork artwork = findArtworkInPath(file, topDir);
        if (artwork != null) {
            log.debug("Found artwork in file `${1}", cut(artwork.getPath()));
        } else {
            artwork = findArtworkInAudioFile(file);
            if (artwork != null) {
                log.debug("Found artwork in MP3 tag of file `${1}", cut(artwork.getPath()));
            }
        }
        return (artwork != null ? artwork.downsizeImageIfNeeded(1000).save(tempArtworkPath) : null);
    }

    private static AudioArtwork findArtworkInAudioFile(Path path) {
        return new Mp3File(path).getTag2Artwork();
    }

    private static AudioArtwork findArtworkInDir(Path dir) {
        final AudioArtwork[] artwork = new AudioArtwork[1];
        final Boolean[] found = new Boolean[]{false};
        final List<String> debugOutput = new ArrayList<>();

        log.debug(Str.of("Searching artwork in dir `${1}`:").with(cut(dir)));

        // Search in dir's children files
        findFirst(pictureFiles, file -> {
            artwork[0] = (dir.equals(file.getParent()) ? AudioArtwork.from(file) : null);
            found[0] = (artwork[0] != null && artwork[0].isCover());
            if (artwork[0] != null) {
                debugOutput.add(color(found[0] ? GREEN : RED, cut(artwork[0].getPath())));
            }
            return found[0];
        });
        log.debug(Str.of("* in files:", "${1}").with(defaultString(joinLines(debugOutput), color(RED, "Artwork not found"))));
        if (found[0]) {
            return artwork[0];
        }

        // Search in dir's sub directories
        debugOutput.clear();
        findFirst(pictureFiles, file -> {
            artwork[0] = (file.startsWith(dir) && Str.isLike(file.getParent().getFileName(), ARTWORK_SUBDIRS_RE) ? AudioArtwork.from(file) : null);
            found[0] = (artwork[0] != null && artwork[0].isCover());
            if (artwork[0] != null) {
                debugOutput.add(color(found[0] ? GREEN : RED, cut(artwork[0].getPath())));
            }
            return found[0];
        });
        log.debug(Str.of("* in sub-dirs:", "${1}").with(defaultString(joinLines(debugOutput), color(RED, "Artwork not found"))));

        return (found[0] ? artwork[0] : null);
    }

//    public static void sortPlaylistTracks(String playlist) {
//        List<String> foundTracks = getTrackAll(playlist).getFound();
//        foundTracks.sort(String::compareToIgnoreCase);
//        deleteAllTracks(playlist);
//        addTracksSingle(playlist, foundTracks.stream().distinct().collect(toList()));
//    }

    private static Path getAlbumDir(Path track, Path topDir) {
        Path dir = track.getParent();
        while (matchAlbumSubdir(dir) > 0 && !dir.equals(topDir)) {
            dir = dir.getParent();
        }
        return dir;
    }

    private static Path getArtworkPath(Path audioFile, Path topDir) {
        AudioArtwork artwork = getArtwork(audioFile, topDir);
        return (artwork != null ? artwork.getPath() : null);
    }

    private static TrackList getTrackListFromBatchScript(int totalCount, int batchSize, Function<Integer, Script> script) {
        return getTrackList(runBatchScript(totalCount, batchSize, script).getOutput());
    }

    private static TrackList getTrackListFromPlaylistScript(Playlist playlist, int batchSize, Function<Integer, Script> script) {
        return getTrackListFromBatchScript(playlist.getSize(), batchSize, script);
    }

    @SafeVarargs
    private static Script getTrackPathsBatchScript(Playlist playlist, int batchStartIndex, int batchSize, Function<String, String>... trackConditions) {
        return AppleScript.getPathsOfTrackRange(playlist.getName(), batchStartIndex + 1, Math.min(batchStartIndex + batchSize, playlist.getSize()), trackConditions);
    }

    private static TrackList getTracks(String playlistName) {
        Playlist playlist = new Playlist(playlistName);
        return getTracksForScript(playlist, TRACKS_BATCH_SIZE, i -> getTrackPathsBatchScript(playlist, i, TRACKS_BATCH_SIZE));
    }

    private static TrackList getTracksForScript(Playlist playlist, int batchSize, Function<Integer, Script> script) {
        Results closeErrorDialog = new Results(runItemsAsync(AppleScript.closeErrorDialog(ScriptApp.MUSIC, "file could not be found")));
        TrackList tracks = getTrackListFromPlaylistScript(playlist, batchSize, script);
        closeErrorDialog.stop();
        log.debug(closeErrorDialog.toString());
        return tracks;
    }

    private static boolean isAlbumSubdir(Path dir) {
        return matchAlbumSubdir(dir) > 0;
    }

    private static boolean isSingleAlbum(List<Path> audioFilePaths) {
        if (audioFilePaths.isEmpty()) {
            return false;
        }
        if (audioFilePaths.size() == 1) {
            return true;
        }
        String album = getMp3File(audioFilePaths.get(0)).getTag2(Mp3Tag.ALBUM);
        if (Str.isNullString(album)) {
            return false;
        }
        return !audioFilePaths.stream().anyMatch(audioFilePath -> !getMp3File(audioFilePath).getTag2(Mp3Tag.ALBUM).equals(album));
    }

    private static int matchAlbumSubdir(Path dir) {
        String name = dir.getFileName().toString().toLowerCase();
        for (int i = 0; i < ALBUM_SUBDIR_RE.size(); i++) {
            if (Str.getCachedRegexPattern(ALBUM_SUBDIR_RE.get(i)).matcher(name).matches()) {
                log.debug(Str.of("Matched as audio file in album subdir: `${1}` (priority ${2})").with(dir, i + 1));
                return i + 1;
            }
        }
        log.debug(Str.of("Not matched as audio file in album subdir: `${1}`").with(dir));
        return 0;
    }

    private static int matchArtworkFileName(Path filePath) {
        return (matchArtworkFileName(filePath.getFileName().toString()));
    }

    @SafeVarargs
    private static int matchFileName(String fileName, String paramName, List<String> regexList, Function<String, String>... transforms) {
        String name = Util.transform(fileName.substring(0, fileName.lastIndexOf(".")), transforms);
        for (int i = 0; i < regexList.size(); i++) {
            if (Str.getCachedRegexPattern(regexList.get(i)).matcher(name).matches()) {
                log.debug(Str.of("Matched as `${1}` (rank ${3}): `${2}`").with(paramName, fileName, i + 1));
                return i + 1;
            }
        }
        log.debug(Str.of("Not matched as `${1}`: `${2}`").with(paramName, fileName));
        return Integer.MAX_VALUE;
    }

    private static Results runBatchScript(int totalCount, int batch, Function<Integer, Script> getScript) {
        return runScripts(Util.transform(iterate(0, i -> i < totalCount, i -> i + batch), getScript));
    }

    private static ScriptRun runItemsAsync(Script script) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ScriptRun scriptRun = new ScriptRun(script);
        executor.submit(() -> {
            scriptRun.setExecutor(executor);
            scriptRun.waitForProcess();
        });
        return scriptRun;
    }

    private static Results runScripts(List<Script> scripts) {
        Results results = new Results();
        scripts.stream().filter(Objects::nonNull).forEach(script -> results.add(new ScriptRun(script).waitForProcess()));
        log.debug(results.toString());
        return results;
    }


    public static class Results {
        List<ScriptRun> scriptRuns;

        Results() {
            this.scriptRuns = new ArrayList<>();
        }

        Results(ScriptRun... scriptRuns) {
            this(Arrays.asList(scriptRuns));
        }

        Results(List<ScriptRun> scriptRuns) {
            this.scriptRuns = scriptRuns;
        }

        public void add(ScriptRun scriptRun) {
            scriptRuns.add(scriptRun);
        }

        public ScriptRun get(int i) {
            return scriptRuns.get(i);
        }

        public String getOutput() {
            return joinLines(scriptRuns.stream().map(ScriptRun::getOutput));
        }

        public void stop() {
            scriptRuns.stream().filter(ScriptRun::isAsync).forEach(ScriptRun::stop);
        }

        @Override
        public String toString() {
            if (scriptRuns.isEmpty()) {
                return "Nothing to do";
            }
            long startMillis = scriptRuns.get(0).startTime;
            long stopMillis = scriptRuns.get(scriptRuns.size() - 1).stopTime;
            long successRuns = scriptRuns.stream().filter(x -> !x.isError()).count();
            long errorRuns = scriptRuns.stream().filter(ScriptRun::isError).count();
            boolean singleRun = (scriptRuns.size() == 1);
            StringBuilder output = new StringBuilder((singleRun ? "" : Str.of("█ SCRIPTS: ${1}\n").with(scriptRuns.size())));
            for (int i = 0; i < scriptRuns.size(); i++) {
                output.append(scriptRuns.get(i).toString(i, scriptRuns.size())).append("\n");
            }
            return output + (singleRun ? "" : Str.of("\nSCRIPTS: ${1}, SUCCESS ${2}, ERROR_FILE_UNREADABLE ${3}, done in ${4} ms\n").with(
                scriptRuns.size(),
                successRuns,
                errorRuns,
                stopMillis - startMillis
            ));
        }
    }

    static ProcessBuilder getExecProcessBuilder(String script) {
        return new ProcessBuilder("osascript", "-s", "s", "-e", script);
    }
}
