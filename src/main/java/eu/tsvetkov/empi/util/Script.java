package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.error.itunes.ITunesException;
import eu.tsvetkov.empi.itunes.script.AppleScript;
import eu.tsvetkov.empi.itunes.script.ITunesScript;
import eu.tsvetkov.empi.itunes.script.ScriptException;
import eu.tsvetkov.empi.itunes.script.VBScript;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static eu.tsvetkov.empi.util.Script.OperatingSystem.MAC;
import static eu.tsvetkov.empi.util.Script.OperatingSystem.WIN;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Script {

    private static final Logger log = LogManager.getLogger(Script.class);

    public static ITunesScript SCRIPT = getScript();
    static OperatingSystem os;

    enum OperatingSystem {
        MAC, WIN
    }

    public static ITunesScript getScript() {
        switch(getOs()) {
            case WIN:
                return new VBScript();
            default:
                return new AppleScript();
        }
    }

    static OperatingSystem getOs(String osName) {
        if(os == null) {
            if(osName != null && osName.toLowerCase().contains("windows")) {
                os = WIN;
            }
            else {
                os = MAC;
            }
        }
        return os;
    }

    static String getOsName() {
        return System.getProperty("os.name");
    }

    public static OperatingSystem getOs() {
        return getOs(getOsName());
    }


//    public static List<String> addTrackPaths(List<Path> tracks, String playlist) throws ITunesException {
//        String files = tracks.parallelStream().map(Path::toString).collect(joining("\", POSIX file \""));
//        return BaseScript.ITEM.exec("set trks to (add {POSIX file \"" + files + "\"} to playlist \"" + playlist + "\")", "...TODO...");
//    }
//
//    public static List<String> addTracks(List<String> tracks, String playlist) throws ITunesException {
//        return BaseScript.ITEM.exec("add {" + Util.join(tracks, "POSIX file \"%s\"", ", ") + "} to playlist \"" + playlist + "\"");
//    }
//
//    public static String deleteTrack(String trackId, String playlist) throws ITunesException {
//        return execLine("delete first track of playlist \"" + playlist + "\" whose database ID = " + trackId);
//    }

    public static Track addTrack(String trackLocation, String playlist) throws ITunesException {
        return SCRIPT.addTrack(trackLocation, playlist);
    }

    public static void deletePlaylist(String playlistName) throws ITunesException {
        SCRIPT.deletePlaylist(playlistName);
    }

    public static void deleteTrackFromLibrary(int trackId) throws ITunesException {
        SCRIPT.deleteTrackFromLibrary(trackId);
    }

    public static String exec(Object... commands) throws ScriptException {
        return SCRIPT.exec(commands);
    }

    public static String getOrCreatePlaylist(String playlist) throws ITunesException {
        return SCRIPT.getOrCreatePlaylist(playlist);
    }

    public static String getPlaylist(String playlist) throws ITunesException {
        return SCRIPT.getPlaylist(playlist);
    }

    public static List<Track> getPlaylistTracks(String playlistName) throws ITunesException {
        return SCRIPT.getPlaylistTracks(playlistName);
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
}
