package eu.tsvetkov.empi.util;

import com.ximpleware.*;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.FileException;
import eu.tsvetkov.empi.error.XmlException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ITunes {

        private static final Logger log = LogManager.getLogger(ITunes.class);
//    private static final SLogger log = new SLogger();
    public static final String ITUNES_LIB = "/Users/vadim/Music/iTunes/iTunes Music Library.xml";
    private final VTDNav nav;
    private String libPath;

    public ITunes(String libPath) throws CommandException {
        this.libPath = libPath;
        log.debug("Reading iTunes library from '" + libPath + "'");
        nav = getNav();
        log.debug("Successfully read iTunes library");
    }

    public static List<Track> getTracksInPlaylist(String playlistName) throws CommandException {

        // Read iTunes XML library.
        ITunes iTunes = new ITunes(ITUNES_LIB);

        // Get track IDs in the provided playlist.
        String playlistDict = "dict[key" + txt("Name") + "/" + sibl("string") + txt(playlistName) + "]";
        String xpathTrackIds = xpath("/plist", "dict", key("Playlists"), sibl("array"), playlistDict, key("Playlist Items"), sibl("array"),
            "dict", "integer");
        List<String> trackIds = iTunes.getValues(xpathTrackIds);
        log.debug("Found " + trackIds.size() + " track IDs");

        // Find tracks with these IDs and get their locations.
        String xpathToTrackId = xpath("/plist", "dict", key("Tracks"), sibl("dict"), "key");
        String xpathToLocation = xpath("./" + sibl("dict"), key("Location"), sibl("string"));
        List<String> trackLocations = iTunes.getValuesForKeys(xpathToTrackId, xpathToLocation, trackIds);

        // Create a list of track instances from pairs {track ID -> track location}.
        List<Track> tracks = IntStream.range(0, trackLocations.size())
            .mapToObj(x -> new Track(Integer.valueOf(trackIds.get(x)), cleanLocation(trackLocations.get(x)))).collect(toList());
        log.debug("Found " + tracks.size() + " tracks");

        return tracks;
    }

    public List<String> getValues(String xpath, Object... arguments) throws CommandException {
        return getValuesWithXpath(format(xpath.replace("'", "''"), arguments));
    }

    protected List<String> getValuesForKeys(String xpathToKey, String xpathToValue, List<String> matchingKeys) throws XmlException {
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
                    }
                    else {
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

    protected List<String> getValuesWithXpath(String xpath) throws XmlException {
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
        log.debug("Found " + values.size() + " values: " + Util.join(values, ", "));
        return values;
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

    private void selectXPath(AutoPilot p, String xpath) throws XmlException {
        try {
            p.selectXPath(xpath);
        } catch (XPathParseException e) {
            throw new XmlException("Error selecting the search XPath: " + xpath, e);
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

        @Override
        public String toString() {
            return "Track " + id + " '" + location + "'";
        }
    }
}
