package eu.tsvetkov.empi.util;

import com.ximpleware.*;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.FileException;
import eu.tsvetkov.empi.error.ITunesException;
import eu.tsvetkov.empi.error.XmlException;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ITunes {

    private static final Logger log = LogManager.getLogger(ITunes.class);
    //    private static final SLogger log = new SLogger();
    public static final String ITUNES_LIB = "/Users/vadim/Music/iTunes/iTunes Music Library.xml";
    private Map<String, List<Track>> playlistTracks = new HashMap<>();
    private VTDNav nav;
    private String libPath;

    public ITunes(String libPath) throws CommandException {
        this.libPath = libPath;
    }

    public Track getTrackByIdFromXml(String playlistName, int trackId) throws CommandException {
        return getTrackById(getPlaylistTracksFromXml(playlistName), trackId);
    }

    static String cleanLocation(String value) {
        value = (value.startsWith("file://") ? value.replaceFirst("file://", "") : value);
        value = value.replaceAll("\\+", "%2b");
        try {
            value = URLDecoder.decode(value, "UTF8");
        } catch (UnsupportedEncodingException e) {
            // Won't happen as the encoding is provided.
        }
        return value;
    }

    static List<Track> getPlaylistTracksStat(String playlistName) throws CommandException {
        return new ITunes(ITUNES_LIB).getPlaylistTracksFromXml(playlistName);
    }

    static Track getTrackById(List<Track> tracks, int id) {
        log.debug("Getting track with ID " + id + " from " + tracks.size() + " tracks");
        return tracks.stream().filter(x -> x.getId() == id).findFirst().get();
    }

    static String key(String keyValue) {
        return "key" + txt(keyValue);
    }

    static String sibl(String elementName) {
        return "following-sibling::" + elementName;
    }

    static String txt(String textValue) {
        return "[text()='" + textValue + "']";
    }

    static String xpath(String... elements) {
        return Util.join(elements, "/");
    }

    private VTDNav getNav() throws CommandException {
        VTDGen vtd = new VTDGen();
        try {
            vtd.setDoc(Files.readAllBytes(Paths.get(libPath)));
            vtd.parse(false);
            return vtd.getNav();
        } catch (IOException e) {
            throw new FileException("Error reading iTunes library file '" + libPath + "'", e);
        } catch (ParseException e) {
            throw new XmlException("Error parsing XML in iTunes library file '" + libPath + "'", e);
        }
    }

    private List<Track> getPlaylistTracksFromXml(String playlistName) throws CommandException {
        if (!playlistTracks.containsKey(playlistName)) {
            playlistTracks.put(playlistName, readPlaylistTracksFromXml(playlistName));
        }
        return playlistTracks.get(playlistName);
    }

    private List<String> getValues(String xpath, Object... arguments) throws CommandException {
        return getValuesWithXpath(format(xpath.replace("'", "''"), arguments));
    }

    private List<String> getValuesForKeys(String xpathToKey, String xpathToValue, List<String> matchingKeys) throws XmlException {
        AutoPilot pilotKeys = new AutoPilot(nav);
        AutoPilot pilotValues = new AutoPilot(nav);
        ArrayList<String> values = new ArrayList<>(matchingKeys);
        selectXPath(pilotKeys, xpathToKey);
        selectXPath(pilotValues, xpathToValue);
        log.debug("Finding nodes to iterate with XPath: " + xpathToKey);

        try {
            while (pilotKeys.evalXPath() != -1) {
                String key = nav.toNormalizedString(nav.getText());
                if (matchingKeys.contains(key)) {
                    log.trace("Found key '" + key + "', finding value with XPath: " + xpathToValue);
                    if (pilotValues.evalXPath() != -1) {
                        String value = nav.toNormalizedString(nav.getText());
                        log.trace("Found value '" + value + "'");
                        values.replaceAll(x -> (key.equals(x) ? value : x));
                        nav.toElement(VTDNav.PARENT);
                    } else {
                        log.trace("Value for key '" + key + "' not found");
                        values.replaceAll(x -> (key.equals(x) ? "ERROR: value for key '" + key + "' not found" : x));
                    }
                    pilotValues.resetXPath();
                }
            }
        } catch (Exception e) {
            throw new XmlException("Error searching with XPath to key='" + xpathToKey + "'; XPath to value='" + xpathToValue + "'", e);
        }

        log.debug("Found " + values.size() + " values");
        pilotKeys.resetXPath();
        return values;
    }

    private List<String> getValuesWithXpath(String xpath) throws XmlException {
        AutoPilot p = new AutoPilot(nav);
        List<String> values = new ArrayList<>();
        log.debug("Finding values with XPath: " + xpath);
        selectXPath(p, xpath);

        try {
            while (p.evalXPath() != -1) {
                values.add(nav.toNormalizedString(nav.getText()));
            }
        } catch (Exception e) {
            throw new XmlException("Error searching with XPath: " + xpath, e);
        }

        p.resetXPath();
        log.debug("Found " + values.size() + " values");
        return values;
    }

    private List<Track> readPlaylistTracksFromXml(String playlistName) throws CommandException {
        readXmlLibrary();
        // Get track IDs in the provided playlist.
        String playlistDict = "dict[key" + txt("Name") + "/" + sibl("string") + txt(playlistName) + "]";
        String xpathTrackIds = xpath("/plist", "dict", key("Playlists"), sibl("array"), playlistDict, key("Playlist Items"), sibl("array"),
            "dict", "integer");
        List<String> trackIds = getValues(xpathTrackIds);
        log.debug("Found " + trackIds.size() + " track IDs");

        // Find tracks with these IDs and get their locations.
        String xpathToTrackId = xpath("/plist", "dict", key("Tracks"), sibl("dict"), "key");
        String xpathToLocation = xpath("./" + sibl("dict"), key("Location"), sibl("string"));
        List<String> trackLocations = getValuesForKeys(xpathToTrackId, xpathToLocation, trackIds);

        // Create a list of track instances from pairs {track ID -> track location}.
        List<Track> tracks = IntStream.range(0, trackLocations.size())
            .mapToObj(x -> new Track(Integer.valueOf(trackIds.get(x)), cleanLocation(trackLocations.get(x)))).collect(toList());
        log.debug("Found " + tracks.size() + " tracks");

        return tracks;
    }

    private void readXmlLibrary() throws CommandException {
        log.debug("Reading iTunes library from '" + libPath + "'");
        nav = getNav();
        log.debug("Successfully read iTunes library");
    }

    private void selectXPath(AutoPilot p, String xpath) throws XmlException {
        try {
            p.selectXPath(xpath);
        } catch (XPathParseException e) {
            throw new XmlException("Error selecting the search XPath: " + xpath, e);
        }
    }

    public enum Tag {

        NAME(Mp3Tag.TITLE), SORT_NAME(Mp3Tag.TITLE_SORT),
        ARTIST(Mp3Tag.ARTIST), SORT_ARTIST(Mp3Tag.ARTIST_SORT),
        ALBUM(Mp3Tag.ALBUM), SORT_ALBUM(Mp3Tag.ALBUM_SORT),
        ALBUM_ARTIST(Mp3Tag.ALBUM_ARTIST), SORT_ALBUM_ARTIST(Mp3Tag.ALBUM_ARTIST_SORT),
        COMPOSER(Mp3Tag.COMPOSER), SORT_COMPOSER(Mp3Tag.COMPOSER_SORT),
        GROUPING(Mp3Tag.GROUPING),
        GENRE(Mp3Tag.GENRE),
        YEAR(Mp3Tag.YEAR),
        TRACK_NUMBER(Mp3Tag.TRACK_NO), TRACK_COUNT(Mp3Tag.TRACK_TOTAL),
        DISC_NUMBER(Mp3Tag.DISC_NO), DISC_COUNT(Mp3Tag.DISC_TOTAL),
        COMPILATION(Mp3Tag.COMPILATION),
        RATING(Mp3Tag.RATING),
        //        LOVED(Mp3Tag.LOVED),
        COMMENT(Mp3Tag.COMMENT);

        private Mp3Tag mp3Tag;

        Tag(Mp3Tag mp3Tag) {
            this.mp3Tag = mp3Tag;
        }

        public static Tag of(Mp3Tag mp3Tag) {
            for (Tag tag : values()) {
                if (tag.mp3Tag.equals(mp3Tag)) {
                    return tag;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return name().toLowerCase().replace("_", " ");
        }
    }

    public static class Script {

        public static final String TELL = "tell application \"iTunes\"";
        public static final String END_TELL = "end tell";
        public static final String ERROR_PREFIX = "ERROR: ";

        public static String addTrack(Path track, String playlist) throws ITunesException {
            return addTrack(track.toString(), playlist);
        }

        public static String addTrack(String track, String playlist) throws ITunesException {
            return execLine("get database ID of (add POSIX file \"" + track + "\" to playlist \"" + playlist + "\")");
        }

        public static List<String> addTrackPaths(List<Path> tracks, String playlist) throws ITunesException {
            String files = tracks.parallelStream().map(Path::toString).collect(joining("\", POSIX file \""));
            return exec("set trks to (add {POSIX file \"" + files + "\"} to playlist \"" + playlist + "\")", "...TODO...");
        }

        public static List<String> addTracks(List<String> tracks, String playlist) {
            return exec("add {POSIX file \"" + tracks.parallelStream().collect(joining("\", POSIX file \"")) + "\"} to playlist \"" + playlist + "\"");
        }

        public static String deleteTrack(String trackId, String playlist) throws ITunesException {
            return execLine("delete first track of playlist \"" + playlist + "\" whose database ID = " + trackId);
        }

        public static String deleteTrackFromLibrary(String trackId) throws ITunesException {
            return execLine("delete first track of playlist 1 whose database ID = " + trackId);
        }

        public static List<String> exec(String... commands) {
            return exec(asList(commands));
        }

        public static List<String> exec(Collection commands) {
            try {
                String script = Util.join(Util.getList(TELL, commands, END_TELL), "\n");
                log.trace("Executing AppleScript:\n" + script);
                InputStream stream = new ProcessBuilder("osascript", "-e", script).start().getInputStream();
                return new BufferedReader(new InputStreamReader(stream)).lines().collect(toList());
            } catch (IOException e) {
                log.error(e);
                return null;
            }
        }

        public static String execLine(String... commands) throws ITunesException {
            return execLine(asList(commands));
        }

        public static String execLine(Collection commands) throws ITunesException {
            List<String> output = exec(commands);
            String outputString = null;
            if (output != null && !output.isEmpty()) {
                outputString = output.get(0);
                if (outputString.startsWith(ERROR_PREFIX)) {
                    throw new ITunesException(outputString.replace(ERROR_PREFIX, ""));
                }
            }
            return outputString;
        }

        public static String getOrCreatePlaylist(String playlist) throws ITunesException {
            return execLine(tryCatch(
                "get playlist \"" + playlist + "\"",
                "make new user playlist with properties {name:\"" + playlist + "\"}"
            ));
        }

        public static String getPlaylist(String playlist) throws ITunesException {
            return execLine(tryCatch("get playlist \"" + playlist + "\""));
        }

//        public static List<Track> getPlaylistTracks(String playlistName) {
//            String addToF = "set f to f & t's database ID & \" \" & ";
//            Stream<String> trackIdsAndLocations = exec(Util.getList(
//                "set f to \"\"",
//                "repeat with t in tracks of playlist \"" + playlistName + "\"",
//                tryCatch(addToF + "POSIX path of (t's location as alias) & \"\n\"", addToF + errorMessage() + " & \"\n\""),
//                "end repeat",
//                "get f's text 1 thru -2" /* remove last line break */
//            ));
//            return trackIdsAndLocations.map(x -> {
//                int space = x.indexOf(" ");
//                return new Track(Integer.valueOf(x.substring(0, space)), x.substring(space+1));
//            }).collect(toList());
//        }

        /**
         * Returns track IDs and locations of the provided playlist.
         * This two-step variation (get IDs, then locations) works faster than one-step: 5700 tracks in 50 seconds.
         *
         * @param playlistName
         *
         * @return
         */
        public static List<Track> getPlaylistTracks(String playlistName) {
            String addToF = "set f to f & ";
            List<String> ids = exec(Util.getList(
                "set AppleScript's text item delimiters to \"\n\"",
                "get (database ID of tracks of playlist \"" + playlistName + "\") as text"
            ));
            log.debug("Got " + ids.size() + " track IDs");
            List<String> locations = exec(Util.getList(
                "set f to \"\"",
                "repeat with t in tracks of playlist \"" + playlistName + "\"",
                tryCatch(addToF + "POSIX path of (t's location as alias) & \"\n\"", addToF + errorMessage() + " & \"\n\""),
                "end repeat",
                "get f's text 1 thru -2" /* remove last line break */
            ));
            log.debug("Got " + locations.size() + " track locations");
            List<Track> tracks = IntStream.range(0, ids.size()).mapToObj(x -> new Track(Integer.valueOf(ids.get(x)), locations.get(x))).collect(toList());
            log.debug("Returning " + tracks.size() + " tracks");
            return tracks;
        }

        protected static String errorMessage() {
            return "\"" + ERROR_PREFIX + "\" & errStr & \" (\" & errNumber & \")\"";
        }

        public void love(int trackId, String playlist) throws Exception {
            exec(ITunes.Script.TELL + "set loved of track id " + trackId + " of playlist \"" + playlist + "\" to true");
        }

        private static List<String> tryCatch(String tryScript, String... catchScript) {
            return tryCatch(asList(tryScript), catchScript);
        }

        private static List<String> tryCatch(Collection<String> tryScript, String... catchScript) {
            return Util.getList(
                "try",
                tryScript,
                "on error errStr number errNumber",
                (catchScript != null && catchScript.length > 0 ? catchScript : "get " + errorMessage()),
                "end try");
        }
    }

    public static class Track {

        private int id;
        private String location;

        public Track(int id, String location) {
            this.id = id;
            this.location = location;
        }

        public int getId() {
            return id;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return "Track " + id + " '" + location + "'";
        }

    }
}
