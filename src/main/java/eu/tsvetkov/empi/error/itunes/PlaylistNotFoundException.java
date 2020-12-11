package eu.tsvetkov.empi.error.itunes;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class PlaylistNotFoundException extends ITunesException {

    public PlaylistNotFoundException(String playlistName) {
        super("Error getting iTunes playlist \"" + playlistName + "\"");
    }
}
