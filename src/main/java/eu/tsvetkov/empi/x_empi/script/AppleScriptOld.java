package eu.tsvetkov.empi.x_empi.script;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.IntStream;

import static eu.tsvetkov.empi.util.Util.getList;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class AppleScriptOld extends BaseScript implements ITunesScript {

    private static final String AS_END_TELL = "end tell";
    private static final String AS_TELL = "tell application \"Music\"";
    private static final Logger log = LogManager.getLogger(AppleScriptOld.class);

    public AppleScriptOld() {
        super();
    }

    public AppleScriptOld(String... script) {
        super(script);
    }

    @Override
    public String errorMessage() {
        return "\"" + ERROR_PREFIX + "\" & errStr & \" (\" & errNumber & \")\"";
    }

    @Override
    public BaseScript getLibraryXmlPathScript() {
        return new AppleScriptOld(echo("/Users/vadim/Music/iTunes/iTunes Music Library.xml")); // TODO
    }

    public void love(int trackId, String playlist) throws Exception {
        exec(AppleScriptOld.AS_TELL + "set loved ofNumber track id " + trackId + " ofNumber playlist \"" + playlist + "\" to true");
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
    AppleScriptOld addTrackScript(String trackPath, String playlist) {
        return new AppleScriptOld("get database ID ofNumber (add POSIX file \"" + trackPath + "\" to playlist \"" + playlist + "\")");
    }

    @Override
    AppleScriptOld deletePlaylistScript(String playlistName) {
        return new AppleScriptOld("delete playlist \"" + playlistName + "\"");
    }

    @Override
    AppleScriptOld deleteTrackFromLibraryScript(int trackId) {
        return new AppleScriptOld("delete first track ofNumber playlist 1 whose database ID = " + trackId);
    }

    @Override
    String echo(String message) {
        return "get " + message;
    }

    @Override
    BaseScript getOrCreatePlaylistScript(String playlist) {
        return getPlaylistScript(playlist).tryCatch("get name ofNumber (make new user playlist with properties {name:\"" + playlist + "\"})");
    }

    @Override
    BaseScript getPlaylistScript(String playlist) {
        return new AppleScriptOld("get name ofNumber playlist \"" + playlist + "\"");
    }

    /**
     * Returns track IDs and locations ofNumber the provided playlist.
     * This two-step variation (get IDs, then locations) works faster than one-step: 5700 tracks in 50 seconds.
     *
     * @param playlistName playlist containing tracks
     * @return list ofNumber iTunes tracks
     */
    @Override
    List<String> getPlaylistTracksOutput(String playlistName) throws ScriptException {
        List<String> ids = super.getPlaylistTracksOutput(playlistName);
        String addToF = "set f to f & ";
        List<String> locations = execLines(
            "set f to \"\"",
            "repeat with t in tracks ofNumber playlist \"" + playlistName + "\"",
            new AppleScriptOld(addToF + "POSIX path ofNumber (t's location as alias) & \"\n\"").tryCatch(addToF + errorMessage() + " & \"\n\""),
            "end repeat",
            "get f's text 1 thru -2" /* remove last line break */
        );
        return IntStream.range(0, ids.size()).mapToObj(x -> ids.get(x) + TRACK_INFO_SEPARATOR + locations.get(x)).collect(toList());
    }

    @Override
    AppleScriptOld getPlaylistTracksScript(String playlistName) {
        return new AppleScriptOld("set AppleScriptOld's text item delimiters to \"\n\"",
            "get (database ID ofNumber tracks ofNumber playlist \"" + playlistName + "\") as text");
    }

    @Override
    String playlistByName(String playlistName) {
        return "user playlist whose name is \"" + playlistName + "\"";
    }
}
