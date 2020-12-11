package eu.tsvetkov.empi.itunes;

import eu.tsvetkov.empi.model.TrackId;
import eu.tsvetkov.empi.model.TrackList;
import eu.tsvetkov.empi.util.Str;
import eu.tsvetkov.empi.util.Util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static eu.tsvetkov.empi.util.Util.*;
import static java.util.regex.Pattern.DOTALL;
import static java.util.stream.Collectors.toList;

public class AppleScript {

    public static final String F1 = "FOUND_TRACKS_BEGIN";
    public static final String F2 = "FOUND_TRACKS_END";
    public static final String F3 = "FOUNDGROUP";
    public static final String LIBRARY_PLAYLIST = "Music";
    public static final String M1 = "MISSING_TRACKS_BEGIN";
    public static final String M2 = "MISSING_TRACKS_END";
    public static final String M3 = "MISSINGGROUP";
    public static final String QUOTE_PLACEHOLDER = "‴";
    public static final Pattern RE_TRACK_PATHS = Pattern.compile(Str.of("${1}\n(?<${3}>((?!${2}).)+)\n${2}\n${4}\n(?<${6}>((?!${5}).)*)\n?${5}").with(F1, F2, F3, M1, M2, M3), DOTALL);
    public static String CODE_GROUP = "code";
    public static final Pattern RE_SCRIPT_ERROR_MESSAGE = Pattern.compile(Str.of("^.+\\((?<${1}>-?\\d+)\\)$").with(CODE_GROUP));
    public static String EMPI_STATUS_PREFIX = "EMPI_STATUS=";

    public static Script addTrackFromFile(String playlist, Path filePath) {
        return Script.of(Str.of("add `${2}` to playlist `${1}`").with(playlist, filePath));
    }

    public static Script addTrackFromLibOrFile(String playlist, Path filePath, TrackId track) {
        return addTrackFromPlaylistOrFile(playlist, LIBRARY_PLAYLIST, filePath, track);
    }

    public static Script addTrackFromLibrary(String playlist, TrackId track) {
        return addTrackFromPlaylist(playlist, LIBRARY_PLAYLIST, track);
    }

    public static Script addTrackFromPlaylist(String playlistTarget, String playlistSource, TrackId track) {
        return Script.of(Str.of("set t to ${2}", "duplicate t to playlist `${1}`").with(playlistTarget, playlistTrack(playlistSource, track)));
    }

    public static Script addTrackFromPlaylistOrFile(String playlistTarget, String playlistSource, Path filePath, TrackId track) {
        return Script.of(tryCatch(
            Str.of("duplicate ${2} to playlist `${1}`", "get `${3}${4}`").with(playlistTarget, playlistTrack(playlistSource, track), EMPI_STATUS_PREFIX, Status.ADDED_TRACK_FROM_LIB.name()),
            Str.of("add `${2}` to playlist `${1}`", "get `${3}${4}`").with(playlistTarget, filePath, EMPI_STATUS_PREFIX, Status.ADDED_TRACK_FROM_FILE.name())
        ));
    }

    public static Script addTracks(String playlist, Collection<String> filePaths) {
        return Script.of(Str.of("add [${2}] to playlist `${1}`").with(playlist, quote(filePaths)));
    }

    public static Script addTracksFromFiles(String playlist, List<Path> audioFilePaths) {
        return Script.of(Str.of("add {${2}} to playlist `${1}`").with(playlist, Util.join(audioFilePaths, "`${1}`", ",")));
    }

    public static Script addTracksFromFilesInDir(String playlist, Path dir, List<Path> audioFilePaths) {
        return Script.of(Str.of(
            "set d to `${2}/`",
            "add {${3}} to playlist `${1}`"
        ).with(playlist, dir, Util.join(audioFilePaths, "d&`${1}`", ",")));
    }

    public static Script closeErrorDialog(ScriptApp app, String errorMessage) {
        return Script.of(Str.of(
            "set m to application process `${1}`",
            "repeat",
            "delay 1",
            "if ((count of m's windows) is 2 and value of static text 1 of m's front window contains `${2}`) then",
            "click button \"Cancel\" of m's front window",
            "end if",
            "end repeat"
        ).with(app, errorMessage)).asSystem();
    }

    public static Script deleteAllTracks(String playlist) {
        return Script.of(Str.of("delete tracks of playlist `${1}`").with(playlist));
    }

    public static Script deletePlaylist(String playlist) {
        return Script.of(Str.of("delete playlist `${1}`").with(playlist));
    }

    // TODO batch this?
    public static Script deletePlaylistAndTracksFromLibrary(String playlist) {
        return Script.of(Str.of(
            "set ids to database ID of tracks of playlist `${1}`",
            "repeat with i in ids",
            "  delete (track 1 of playlist `${2}` whose database ID is i)",
            "end repeat",
            "delete playlist `${1}`"
        ).with(playlist, LIBRARY_PLAYLIST));
    }

    public static Script deleteTrackFromLibrary(TrackId track) {
        return deleteTrackFromPlaylist(LIBRARY_PLAYLIST, track);
    }

    public static Script deleteTrackFromPlaylist(String playlist, TrackId track) {
        return Script.of(Str.of("set t to ${1}", "delete t").with(playlistTrack(playlist, track)));
    }

    // {"item1", "item2", "item3 with \"extra quotes\""}
    public static List<String> extractApplescriptList(String applescriptList) {
        ArrayList<String> list = new ArrayList<>();
        String outputEscaped = applescriptList.replaceAll("\\\\\"", QUOTE_PLACEHOLDER);  // replace \" in the output with a placeholder to revert later.
        String[] items = outputEscaped.replaceFirst("\\{\"", "").replaceFirst("\"\\}", "").split("\", \"");
        Stream.of(items).forEach(item -> list.add(item.replaceAll(QUOTE_PLACEHOLDER, "\""))); // revert the placeholders.
        return list;
    }

    public static Script getAllPlaylistNames() {
        return Script.of("get name of playlists");
    }

    public static Script getLoveRange(String playlist, int from, int to) {
        return getPathsOfTrackRange(playlist, from, to, var -> Str.of("${1}'s loved").with(var));
    }

    public static Script getLovedTracksCount(String playlist) {
        return Script.of(Str.of("get count of (tracks of playlist `${1}` whose loved = true)").with(playlist));
    }

    public static Script getPathsOfTrackRange(String playlist, int from, int to, Function<String, String>... trackConditions) {
        String ifs = Util.join(nonNullTransform("t", trackConditions), " and ");
        return Script.of(Str.of(
            "set {foundTracks, missingTracks, pl} to {{}, {}, playlist `${1}`}",
            "repeat with i from ${2} to ${3}",
            "set t to track i of pl",
            (isNotBlank(ifs) ? Str.of("if ${1} then").with(ifs) : ""),
            tryCatch(
                "set foundTracks's end to ${trackPath(t)}",
                tryCatch(
                    Str.of("play t", "stop", "set foundTracks's end to ${trackPath(t)}"),
                    "set missingTracks's end to ${missingTrackInfo(t)}"
                )
            ),
            (isNotBlank(ifs) ? "end if" : ""),
            "end repeat",
            "set AppleScript's text item delimiters to ASCII character 10",
            "get {`${4}`, foundTracks, `${5}`, `${6}`, missingTracks, `${7}`} as string"
        ).methodsIn(AppleScript.class).with(playlist, from, to, F1, F2, M1, M2));
    }

    public static Script getPlaylist(String playlist) {
        return Script.of(Str.of(tryCatch("get name of playlist `${1}`", "get name of (make new user playlist with properties {name:`${1}`})")).with(playlist));
    }

    public static Script getPlaylistSize(String playlist) {
        return Script.of(Str.of("get count of playlist `${1}`'s tracks").with(playlist));
    }

    public static Script getPlaylistsOfTrack(TrackId track) {
        return Script.of(Str.of(
            "set {t, AppleScript's text item delimiters} to {${1}, ASCII character 10}",
            "get (name of t's playlists whose name ≠ `${2}` and name ≠ `all`) as string"
        ).with(playlistTrack(LIBRARY_PLAYLIST, track), LIBRARY_PLAYLIST));
    }

    public static Status getStatus(String message) {
        if (message.startsWith(EMPI_STATUS_PREFIX)) {
            return Status.of(message.replaceAll(EMPI_STATUS_PREFIX, ""));
        } else {
            Matcher matcher = RE_SCRIPT_ERROR_MESSAGE.matcher(message);
            return Status.of(matcher.matches() ? Integer.valueOf(matcher.group(CODE_GROUP)) : 0);
        }
    }

    public static TrackList getTrackList(String trackPaths) {
        Map<String, List<String>> matchGroupLists = getMatchGroupLists(RE_TRACK_PATHS, Arrays.asList(F3, M3), trackPaths);
        TrackList trackList = new TrackList();
        trackList.setFound(matchGroupLists.get(F3).stream().map(Paths::get).collect(toList()));
        trackList.setMissing(matchGroupLists.get(M3).stream().map(Paths::get).collect(toList()));
        return trackList;
    }

    public static Script getTrackPathSingle(String playlist, int trackIndex) {
        return Script.of(Str.of("get ${trackPath(track ${2} of playlist `${1}`)}").methodsIn(AppleScript.class).with(playlist, trackIndex));
    }

    public static Script loveTrack(String playlist, TrackId track, boolean loved) {
        return Script.of(Str.of("set loved of ${1} to ${2}").with(playlistTrack(playlist, track), loved));
    }

    public static Script loveTrack(String playlist, TrackId track) {
        return loveTrack(playlist, track, true);
    }

    public static String missingTrackInfo(String var) {
        return Str.of("(${1}'s database id & ${2} & ${1}'s track number & ${2} & ${1}'s name & ${2} & ${1}'s artist & ${2} & ${1}'s album & ${2} & ${1}'s size as string)").with(var, "\":::\"");
    }

    public static String playlistTrack(String playlist, TrackId trackId) {
        return Str.of("(track 1 of playlist `${1}` whose name = `${2}` and artist = `${3}` and album = `${4}` and size = ${5})").with(playlist, trackId.getName(), trackId.getArtist(), trackId.getAlbum(), trackId.getSize());
    }

    public static Script setTrackArtworks(String playlist, int fromIndex, List<Path> artworkPaths) {
        return Script.of(Str.of(
            "set artworkPaths to {${3}}",
            "repeat with i from 1 to artworkPaths's length",
            "set t to track (${2} + i) of playlist `${1}`",
            "set artworkData to (read artworkPaths's item i as picture)",
            "set data of artwork 1 of t to artworkData",
            "end repeat"
        ).with(playlist, fromIndex, Util.join(artworkPaths, "`${1}`", ",")));
    }

    public static Script setTrackLocations(String playlist, int fromIndex, List<Path> trackPaths) {
        return Script.of(Str.of(
            "set trackPaths to {${3}}",
            "repeat with i from 1 to trackPaths's length",
            "set t to track (${2} + i) of playlist `${1}`",
            "set location of t to trackPaths's item i",
            "end repeat"
        ).with(playlist, fromIndex, Util.join(trackPaths, "`${1}`", ",")));
    }

    public static Script setTrackLocationsAndArtwork(String playlist, int fromIndex, List<Path> trackPaths, List<Path> artworkPaths) {
        return Script.of(Str.of(
            "set {trackPaths, artworkPaths} to {{${3}}, {${4}}}",
            "repeat with i from 1 to trackPaths's length",
            "  set t to track (${2} + i) of playlist `${1}`",
            "  set location of t to trackPaths's item i",
            "  if (artworkPaths's item i ≠ \"null\")",
            "    set data of artwork 1 of t to (read artworkPaths's item i as picture)",
            "  end if",
            "end repeat"
        ).with(playlist, fromIndex, Util.join(trackPaths, "`${1}`", ","), Util.join(artworkPaths, "`${1}`", ",")));
    }

    public static String trackPath(String var) {
        return Str.of("POSIX path of (location of ${1} as alias)").with(var);
    }

    public static String tryCatch(Object tryScript, Object catchScript) {
        return lines("try", tryScript, "on error errStr number errNumber", catchScript, "end try");
    }

    public static String tryGetErrorNumber(String tryScript) {
        return tryCatch(tryScript, Str.of("get `${1}` & ${2}").with(EMPI_STATUS_PREFIX, "errNumber"));
    }

    public static String wrapAppScript(String app, String script) {
        return Str.of("tell application `${1}`", "${2}", "end tell").with(app, script);
    }

    public static String wrapMusicScript(String script) {
        return wrapAppScript("Music", script);
    }

    public static String wrapSystemScript(String script) {
        return wrapAppScript("System Events", script);
    }
}
