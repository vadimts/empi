package eu.tsvetkov.x_empi.error.itunes;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class PlaylistNotFoundException extends ITunesException {

    public PlaylistNotFoundException(String playlistName) {
        super("ScriptError getting iTunes playlist \"" + playlistName + "\"");
    }
}
