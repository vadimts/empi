package eu.tsvetkov.empi.x_empi.script;

import eu.tsvetkov.empi.x_empi.error.itunes.ITunesException;
import eu.tsvetkov.empi.x_empi.util.ITunes.Track;

import java.util.List;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public interface ITunesScript {

    Track addTrack(String trackLocation, String playlist) throws ITunesException;

    void deletePlaylist(String playlistName) throws ITunesException;

    void deleteTrackFromLibrary(int trackId) throws ITunesException;

    String errorMessage();

    String exec(Object... commands) throws ScriptException;

    String getLibraryXmlPath() throws ITunesException;

    String getOrCreatePlaylist(String playlist) throws ITunesException;

    String getPlaylist(String playlist) throws ITunesException;

    List<Track> getPlaylistTracks(String playlistName) throws ITunesException;
//    String              tryCatch(String tryScript, String... catchScript);

}
