package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.error.ITunesException;
import eu.tsvetkov.empi.itunes.script.AppleScript;
import eu.tsvetkov.empi.itunes.script.BaseScript;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static eu.tsvetkov.empi.itunes.script.BaseScript.ERROR_PREFIX;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Script {

    private static final Logger log = LogManager.getLogger(Script.class);

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

    public static String deleteTrackFromLibrary(String trackId) throws ITunesException {
        return BaseScript.ITEM.exec("delete first track of playlist 1 whose database ID = " + trackId);
    }

    public static String getOrCreatePlaylist(String playlist) throws ITunesException {
        return BaseScript.ITEM.exec(BaseScript.ITEM.tryCatch(
                "get playlist \"" + playlist + "\"",
                "make new user playlist with properties {name:\"" + playlist + "\"}"
        ));
    }

    public static String getPlaylist(String playlist) throws ITunesException {
        return BaseScript.ITEM.exec(BaseScript.ITEM.tryCatch("get playlist \"" + playlist + "\""));
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
