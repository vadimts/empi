package eu.tsvetkov.empi.itunes.script;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static eu.tsvetkov.empi.util.Util.*;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class AppleScript extends BaseScript {

    private static final Logger log = LogManager.getLogger(AppleScript.class);

    private static final String AS_TELL = "tell application \"iTunes\"";
    private static final String AS_END_TELL = "end tell";

    public AppleScript(String... script) {
        super(script);
    }

    @Override
    AppleScript addTrackScript(String trackPath, String playlist) {
        return new AppleScript("get database ID of (add POSIX file \"" + trackPath + "\" to playlist \"" + playlist + "\")");
    }

    @Override
    AppleScript deletePlaylistScript(String playlistName) {
        return new AppleScript("delete playlist \"" + playlistName + "\"");
    }

    @Override
    AppleScript deleteTrackFromLibraryScript(int trackId) {
        return new AppleScript("delete first track of playlist 1 whose database ID = " + trackId);
    }

    @Override
    public BaseScript getLibraryXmlPathScript() {
        return new AppleScript(echo("iTunes.libraryXMLPath")); // TODO
    }

    @Override
    BaseScript getOrCreatePlaylistScript(String playlist) {
        return getPlaylistScript(playlist).tryCatch("make new user playlist with properties {name:\"" + playlist + "\"}");
    }

    @Override
    BaseScript getPlaylistScript(String playlist) {
        return new AppleScript("get playlist \"" + playlist + "\"");
    }

    @Override
    public String errorMessage() {
        return "\"" + ERROR_PREFIX + "\" & errStr & \" (\" & errNumber & \")\"";
    }

    /**
     * Returns track IDs and locations of the provided playlist.
     * This two-step variation (get IDs, then locations) works faster than one-step: 5700 tracks in 50 seconds.
     *
     * @param playlistName playlist containing tracks
     * @return list of iTunes tracks
     */
    @Override
    List<String> getPlaylistTracksOutput(String playlistName) throws ScriptException {
        List<String> ids = super.getPlaylistTracksOutput(playlistName);
        String addToF = "set f to f & ";
        List<String> locations = execLines(
                "set f to \"\"",
                "repeat with t in tracks of playlist \"" + playlistName + "\"",
                new AppleScript(addToF + "POSIX path of (t's location as alias) & \"\n\"").tryCatch(addToF + errorMessage() + " & \"\n\""),
                "end repeat",
                "get f's text 1 thru -2" /* remove last line break */
        );
        log.debug("Got " + locations.size() + " track locations");
        return IntStream.range(0, ids.size()).mapToObj(x -> ids.get(x) + TRACK_INFO_SEPARATOR + locations.get(x)).collect(toList());
    }


    @Override
    AppleScript getPlaylistTracksScript(String playlistName) {
        return new AppleScript("set AppleScript's text item delimiters to \"\n\"",
        "get (database ID of tracks of playlist \"" + playlistName + "\") as text");
    }

    public void love(int trackId, String playlist) throws Exception {
        exec(AppleScript.AS_TELL + "set loved of track id " + trackId + " of playlist \"" + playlist + "\" to true");
    }

    @Override
    public List<String> tryCatchWrap(List<String> tryScript, List<String> catchScript) {
        return getList(
                "try",
                tryScript,
                "on error errStr number errNumber",
                catchScript,
                "end try");
    }

    @Override
    protected List<String> catchDefault() {
        return getList("get " + errorMessage());
    }

    @Override
    protected ProcessBuilder getExecProcessBuilder(String script) throws Exception {
        return new ProcessBuilder("osascript", "-e", script);
    }

    @Override
    protected List<String> newScript(Object... commands) {
        return getList(AS_TELL, commands, AS_END_TELL);
    }

    @Override
    String echo(String message) {
        return "get " + message;
    }

    @Override
    String playlistByName(String playlistName) {
        return "user playlist whose name is \"" + playlistName + "\"";
    }
}
