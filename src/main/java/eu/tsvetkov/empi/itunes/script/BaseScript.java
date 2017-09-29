package eu.tsvetkov.empi.itunes.script;

import eu.tsvetkov.empi.error.itunes.ITunesException;
import eu.tsvetkov.empi.error.itunes.PlaylistNotFoundException;
import eu.tsvetkov.empi.util.Track;
import eu.tsvetkov.empi.util.SLogger;
import eu.tsvetkov.empi.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static eu.tsvetkov.empi.util.Util.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class BaseScript implements ITunesScript {

    private static final SLogger log = new SLogger(); //LogManager.getLogger(BaseScript.class);

    public static final String ERROR_PREFIX = "ERROR_";
    private static final String LOG_PREFIX = "    ";
    static final String TRACK_INFO_SEPARATOR = ">";

    protected List<String> script = new ArrayList<>();

    public BaseScript() {
    }

    BaseScript(String... script) {
        this.script = getList(script);
    }

    @Override
    public Track addTrack(String trackPath, String playlistName) throws ITunesException {
        log.debug("Adding track '" + trackPath + "' to playlist '" + playlistName + "'");
        try {
            return new Track(Integer.parseInt(exec(addTrackScript(trackPath, playlistName))), trackPath);
        } catch (ScriptException e) {
            throw new ITunesException("Error adding track", e);
        }
    }

    @Override
    public void deletePlaylist(String playlistName) throws ITunesException {
        log.debug("Deleting playlist '" + playlistName + "'");
        try {
            exec(deletePlaylistScript(playlistName));
        } catch (ScriptException e) {
            throw new ITunesException("Error deleting playlist", e);
        }
    }

    @Override
    public void deleteTrackFromLibrary(int trackId) throws ITunesException {
        log.debug("Deleting track '" + trackId + "' from iTunes library");
        try {
            exec(deleteTrackFromLibraryScript(trackId));
        } catch (ScriptException e) {
            throw new ITunesException("Error deleting track", e);
        }
    }

    @Override
    public String getLibraryXmlPath() throws ITunesException {
        try {
            return exec(getLibraryXmlPathScript());
        } catch (ScriptException e) {
            throw new ITunesException("Error getting path to iTunes library XML file", e);
        }
    }

    @Override
    public String getOrCreatePlaylist(String playlistName) throws ITunesException {
        try {
            log.debug("Getting or creating playlist '" + playlistName + "'");
            return exec(getOrCreatePlaylistScript(playlistName));
        } catch (ScriptException e) {
            throw new ITunesException("Error getting or creating playlist", e);
        }
    }

    @Override
    public String getPlaylist(String playlistName) throws ITunesException {
        try {
            log.debug("Getting playlist '" + playlistName + "'");
            return exec(getPlaylistScript(playlistName));
        } catch (ScriptException e) {
            throw new PlaylistNotFoundException(playlistName);
        }
    }

    @Override
    public List<Track> getPlaylistTracks(String playlistName) throws ITunesException {
        log.debug("Getting tracks of playlist '" + playlistName + "'");
        try {
            List<String> output = getPlaylistTracksOutput(playlistName);
            List<Track> tracks = new ArrayList<>();
            for (String trackString : output) {
                tracks.add(new Track(trackString.split(TRACK_INFO_SEPARATOR)));
            }
            return tracks;
        } catch (ScriptException e) {
            throw new ITunesException("Error getting tracks", e);
        }
    }

    public List<String> getScript() {
        return script;
    }

    List<String> getPlaylistTracksOutput(String playlistName) throws ScriptException {
        return execLines(getPlaylistTracksScript(playlistName));
    }

    List<String> execLinesUnchecked(Object... commands) throws ScriptException {
        List<String> script = newScript(commands);
        List<String> output = null;
        ScriptException error = null;
        try {
            log.debug("----- EXECUTE " + this.getClass().getSimpleName() + " ----------------------------------------------------\n" + joinLinesPrefix(LOG_PREFIX, script));
            Process process = getExecProcessBuilder(joinLines(script)).start();
            process.waitFor();
            output = getExecOutput(process);
            for (String s : output) {
                if(startsWith(s, ERROR_PREFIX)) {
                    error = new ScriptException(s.replaceFirst(ERROR_PREFIX, ""), output);
                    break;
                }
            }
            String joinedOutput = joinLinesPrefix(LOG_PREFIX, "OUTPUT", (isBlank(join(output)) ? "(empty)" : output));
            log.debug("\n" + joinedOutput);
            if(process.exitValue() != 0) {
                error = new ScriptException(joinLines(getExecError(process)), output);
            }
        } catch (Exception e) {
            error = new ScriptException(e.getMessage(), output);
        } finally {
            if(error != null) {
                log.error("\n" + LOG_PREFIX + "ERROR");
                log.error(LOG_PREFIX + error.getMessage());
            }
            log.debug("---------------------------------------------------------------------------");
        }
        if(error != null) {
            throw error;
        }
        return output;
    }

    List<String> execLines(List<String> tryScript, List<String> catchScript) throws ScriptException {
        return execLinesUnchecked(tryCatchWrap(tryScript, catchScript));
    }

    List<String> execLines(Object... commands) throws ScriptException {
        return execLines(getList(commands), catchDefault());
    }

    String exec(List<String> tryScript, List<String> catchScript) throws ScriptException {
        return joinLines(execLines(tryScript, catchScript));
    }

    public String exec(Object... commands) throws ScriptException {
        return joinLines(execLines(commands));
    }

    List<String> getExecError(Process process) throws Exception {
        return getOutputFromStream(process.getErrorStream());
    }

    List<String> getExecOutput(Process process) throws Exception {
        return getOutputFromStream(process.getInputStream());
    }

    BaseScript tryCatch(String... catchScript) {
        script = tryCatchWrap(script, Util.isNotEmpty(catchScript) ? getList(catchScript) : catchDefault());
        return this;
    }

    @Override
    public String toString() {
        return joinLines(script);
    }

    private static List<String> getOutputFromStream(InputStream processInputStream) {
        return new BufferedReader(new InputStreamReader(processInputStream)).lines().collect(toList());
    }

    abstract BaseScript addTrackScript(String trackPath, String playlist);

    abstract BaseScript deletePlaylistScript(String playlistName);

    abstract BaseScript deleteTrackFromLibraryScript(int trackId);

    abstract BaseScript getOrCreatePlaylistScript(String playlistName);

    abstract BaseScript getPlaylistScript(String playlistName);

    abstract BaseScript getPlaylistTracksScript(String playlistName);

    abstract List<String> catchDefault();

    abstract List<String> tryCatchWrap(List<String> tryScript, List<String> catchScript);

    abstract ProcessBuilder getExecProcessBuilder(String script) throws Exception;

    abstract List<String> newScript(Object... commands);

    abstract String echo(String message);

    abstract String playlistByName(String playlistName);

    public abstract BaseScript getLibraryXmlPathScript();
}
