package eu.tsvetkov.empi.dsl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static eu.tsvetkov.empi.util.Util.*;
import static java.util.regex.Pattern.DOTALL;

public class AppleScript {

    static final String MUSIC_BEGIN = "tell application \"Music\"";
    static final String MUSIC_END = "end tell";
    public static final String F1 = "FOUND_TRACKS_BEGIN";
    public static final String F2 = "FOUND_TRACKS_END";
    public static final String F3 = "FOUNDGROUP";
    public static final String M1 = "MISSING_TRACKS_BEGIN";
    public static final String M2 = "MISSING_TRACKS_END";
    public static final String M3 = "MISSINGGROUP";
//    public static final Pattern RE_TRACK_PATHS = Pattern.compile(F1 + "\n(?<" + F3 + ">((?!" + F3 + ").)+)\n" + F3 + "\n" + M1 + "\n(?<" + M3 + ">((?!" + M3 + ").)*)\n?" + M3, DOTALL);
    public static final Pattern RE_TRACK_PATHS = Pattern.compile(Str.of("${1}\n(?<${3}>((?!${2}).)+)\n${2}\n${4}\n(?<${6}>((?!${5}).)*)\n?${5}").with(F1, F2, F3, M1, M2, M3), DOTALL);

    public static TrackList getTrackList(String trackPaths) {
        Map<String, List<String>> matchGroupLists = getMatchGroupLists(RE_TRACK_PATHS, Arrays.asList(F3, M3), trackPaths);
        TrackList trackList = new TrackList();
        trackList.setFound(matchGroupLists.get(F3));
        trackList.setMissing(matchGroupLists.get(M3));
        return trackList;
    }

    public static String addTrack(String playlist, String filePath) {
        return Str.of("add `${2}` to playlist `${1}`").with(playlist, filePath);
    }

    public static String addTrackWith(String playlistTarget, String playlistSource, String name, String artist, String album, long size) {
        return Str.of("duplicate (track 1 of playlist `${2}` whose name = `${3}` and artist = `${4}` and album = `${5}` and size = ${6}) to playlist `${1}`").with(playlistTarget, playlistSource, name, artist, album, size);
    }

    public static String addTracks(String playlist, Collection<String> filePaths) {
        return Str.of("add [${2}] to playlist `${1}`").with(playlist, quote(filePaths));
    }

    public static String getTrackPaths(String playlist) {
        return Str.of(
            "set {foundTracks, missingTracks, pl, AppleScript's text item delimiters} to {{}, {}, playlist `${1}`, ASCII character 10}",
            "repeat with t in pl's tracks",
            tryC(
                "set foundTracks's end to ${trackPath(t)}",
                "set missingTracks's end to ${missingTrackInfo(t)}"
            ),
            "end repeat",
            "get {`${2}`, foundTracks, `${3}`, `${4}`, missingTracks, `${5}`} as string"
        ).methodsIn(AppleScript.class).with(playlist, F1, F2, M1, M2);
    }

    public static String getTrackPathsRange(String playlist, int from, int to) {
        return Str.of(
            "set {foundTracks, missingTracks, pl, AppleScript's text item delimiters} to {{}, {}, playlist `${1}`, ASCII character 10}",
            "repeat with i from ${2} to ${3}",
            "set t to track i of pl",
            tryC(
                "set foundTracks's end to ${trackPath(t)}",
                "set missingTracks's end to ${missingTrackInfo(t)}"
            ),
            "end repeat",
            "get {`${4}`, foundTracks, `${5}`, `${6}`, missingTracks, `${7}`} as string"
        ).methodsIn(AppleScript.class).with(playlist, from, to, F1, F2, M1, M2);
    }

    public static String getTrackPathSingle(String playlist, int trackIndex) {
        return Str.of("get ${trackPath(track ${2} of playlist `${1}`)}").methodsIn(AppleScript.class).with(playlist, trackIndex);
    }

    public static String trackPath(String var) {
        return Str.of("POSIX path of (location of ${1} as alias)").with(var);
    }

    public static String missingTrackInfo(String var) {
        return Str.of("${1}'s id & ${2} & ${1}'s track number & ${2} & ${1}'s name & ${2} & ${1}'s artist & ${2} & ${1}'s album").with(var, "\":::\"");
    }

    public static String getPlaylist(String playlist) {
        return Str.of(tryC("get name of playlist `${1}`", "get name of (make new user playlist with properties {name:`${1}`})")).with(playlist);
    }

    public static String getPlaylistSize(String playlist) {
        return Str.of("get count of playlist `${1}`'s tracks").with(playlist);
    }

    public static String deletePlaylist(String playlist) {
        return Str.of("delete playlist `${1}`").with(playlist);
    }

    public static String deleteTracks(String playlist) {
        return Str.of("delete tracks of playlist `${1}`").with(playlist);
    }

    public static String tryC(String tryScript, String catchScript) {
        return lines("try", tryScript, "on error errStr number errNumber", catchScript, "end try");
    }

    public static String wrapMusicScript(String scriptLines) {
        return (scriptLines.startsWith(MUSIC_BEGIN) ? scriptLines : MUSIC_BEGIN + "\n" + scriptLines + "\n" + MUSIC_END);
    }
}
