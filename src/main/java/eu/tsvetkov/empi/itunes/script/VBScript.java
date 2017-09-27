package eu.tsvetkov.empi.itunes.script;

import eu.tsvetkov.empi.util.SLogger;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static eu.tsvetkov.empi.util.Util.getList;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class VBScript extends BaseScript implements ITunesScript {

    private static final SLogger log = new SLogger(); //LogManager.getLogger(BaseScript.class);

    private static final String VB_CREATE = "Set iTunes = CreateObject(\"iTunes.Application\")";
    public static final Charset VBSCRIPT_SOURCE_ENCODING = Charset.forName("UTF-16LE");

    public VBScript() {
        super();
    }

    VBScript(String... script) {
        super(script);
    }

    @Override
    BaseScript addTrackScript(String trackPath, String playlist) {
        return new VBScript(
            "set job = " + playlistByName(playlist) + ".addFile(\"" + trackPath + "\")",
            "do : loop while job.inProgress",
            echo("job.Tracks.Item(1).trackDatabaseID")
        );
    }

    @Override
    BaseScript deletePlaylistScript(String playlistName) {
        return new VBScript(playlistByName(playlistName) + ".delete");
    }

    @Override
    BaseScript deleteTrackFromLibraryScript(int trackId) {
        return new VBScript(
            "set tracks = iTunes.libraryPlaylist.tracks",
            "for each track in tracks",
            "if (track.trackDatabaseID = " + trackId + ") then track.delete",
            "next"
        );
    }

    @Override
    public BaseScript getLibraryXmlPathScript() {
        return new VBScript(echo("iTunes.libraryXMLPath"));
    }

    @Override
    BaseScript getOrCreatePlaylistScript(String playlistName) {
        return getPlaylistScript(playlistName).tryCatch("set playlist = iTunes.CreatePlaylist(\"" + playlistName + "\")", echo("playlist.name"));
    }

    @Override
    BaseScript getPlaylistScript(String playlistName) {
        return new VBScript(
            "set playlist = " + playlistByName(playlistName),
            "if (playlist is Nothing) then err.raise 1, \"empi\", \"Playlist '" + playlistName + "' not found\"",
            echo("playlist.name")
        );
    }

    @Override
    public String errorMessage() {
        return ERROR_PREFIX;
    }

    @Override
    public BaseScript getPlaylistTracksScript(String playlistName) {
        return new VBScript("set tracks = " + playlistByName(playlistName) + ".tracks",
            "for each track in tracks",
            echo("track.trackDatabaseID & \"" + TRACK_INFO_SEPARATOR + "\" & track.location"),
            "next"
        );
    }

    @Override
    List<String> catchDefault() {
        return getList(echo("\"" + ERROR_PREFIX + "Error \" & err.number & \": \" & err.description"));
    }

    @Override
    List<String> tryCatchWrap(List<String> tryScript, List<String> catchScript) {
        return getList(
            "err.clear",
            "on error resume next",
            tryScript,
            "If (err.number <> 0) then",
                catchScript,
                "err.clear",
            "end if",
            "on error goto 0");
    }

    static Path getTempFilePath() {
        return Paths.get(System.getProperty("java.io.tmpdir") + "empi.vbs");
    }

    static Path getOutFilePath() {
        return Paths.get(System.getProperty("java.io.tmpdir") + "empi.out");
    }

    @Override
    ProcessBuilder getExecProcessBuilder(String script) throws Exception {
//        log.debug(getTempFilePath());
        String vbscriptFilePath = Files.write(getTempFilePath(), getList(script), VBSCRIPT_SOURCE_ENCODING).toString();
        return new ProcessBuilder("cscript", "/nologo", vbscriptFilePath);
    }

    /**
     * Reads the script execution output from the UTF-8 text file written by VBScript using the ADODB stream.
     * The BOM character "\uFEFF" prepended to the file is omitted.
     *
     * @param process process containing the VBScript
     * @return list of output lines
     * @throws Exception any error
     */
    @Override
    List<String> getExecOutput(Process process) throws Exception {
        return Files.readAllLines(getOutFilePath()).stream().map(x -> x.replaceFirst("\uFEFF", "")).collect(toList());
    }

    @Override
    List<String> newScript(Object... commands) {
        return getList(VB_CREATE,
            "Set out=CreateObject(\"ADODB.Stream\") : out.open : out.type=2 : out.charset=\"utf-8\"",
            commands,
            "out.saveToFile \"" + getOutFilePath() + "\", 2 : out.close");
    }

    @Override
    String echo(String message) {
        return "out.writeText(" + message + " & vbCrLf)";
    }

    @Override
    String playlistByName(String playlistName) {
        return "iTunes.LibrarySource.Playlists.itemByName(\"" + playlistName + "\")";
    }
}
