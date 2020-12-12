package eu.tsvetkov.x_empi.util;

import com.ximpleware.*;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.util.Util;
import eu.tsvetkov.x_empi.error.CommandException;
import eu.tsvetkov.x_empi.error.FileException;
import eu.tsvetkov.x_empi.error.XmlException;
import eu.tsvetkov.x_empi.error.itunes.ITunesException;
import eu.tsvetkov.x_empi.script.BaseScript;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static eu.tsvetkov.empi.util.Util.isBlank;
import static eu.tsvetkov.empi.util.Util.isEmpty;
import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ITunes {

    //    private static final SLogger log = new SLogger();
    public static final String ITUNES_LIB = "/Users/vadim/Music/iTunes/iTunes Music Library.xml";
    private static final Logger log = LogManager.getLogger(ITunes.class);
    private String libPath;
    private VTDNav nav;
    private Map<String, List<Track>> playlistTracks = new HashMap<>();

    public ITunes(String libPath) throws CommandException {
        this.libPath = libPath;
    }

    public Track getTrackByIdFromXml(String playlistName, int trackId) throws CommandException {
        return getTrackById(getPlaylistTracksFromXml(playlistName), trackId);
    }

    private VTDNav getNav() throws CommandException {
        VTDGen vtd = new VTDGen();
        try {
            vtd.setDoc(Files.readAllBytes(Paths.get(libPath)));
            vtd.parse(false);
            return vtd.getNav();
        } catch (IOException e) {
            throw new FileException("ScriptError reading iTunes library file '" + libPath + "'", e);
        } catch (ParseException e) {
            throw new XmlException("ScriptError parsing XML in iTunes library file '" + libPath + "'", e);
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
                        values.replaceAll(x -> (key.equals(x) ? "ERROR_FILE_UNREADABLE: value for key '" + key + "' not found" : x));
                    }
                    pilotValues.resetXPath();
                }
            }
        } catch (Exception e) {
            throw new XmlException("ScriptError searching with XPath to key='" + xpathToKey + "'; XPath to value='" + xpathToValue + "'", e);
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
            throw new XmlException("ScriptError searching with XPath: " + xpath, e);
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

        // Create a list ofNumber track instances from pairs {track ID -> track location}.
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
            throw new XmlException("ScriptError selecting the search XPath: " + xpath, e);
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




    public static class Track {

        public static final String ERROR_LOCATION = BaseScript.ERROR_PREFIX + "track location not found";

        private int id;
        private String path;

        public Track(int id, String path) {
            this.id = id;
            this.path = path;
        }

        public Track(String... attributes) throws ITunesException {
            if (attributes == null || isEmpty(attributes)) {
                throw new ITunesException("ScriptError creating iTunes track from attributes: " + attributes);
            }
            this.id = Integer.parseInt(attributes[0]);
            this.path = (attributes.length == 1 || isBlank(attributes[1]) ? ERROR_LOCATION : attributes[1]);
        }

        public static Track of(String... attributes) throws ITunesException {
            return new Track(attributes);
        }

        public int getId() {
            return id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return "Track " + id + " \"" + path + "\"";
        }

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
}
