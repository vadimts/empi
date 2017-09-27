package eu.tsvetkov.empi.itunes.script;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class VBScriptTest extends BaseScriptTest<VBScript> {

    protected String getPlaylistCountScript() {
        return script.echo("iTunes.LibrarySource.Playlists.Count");
    }

    @Override
    String getTracksCountScript() {
        return script.echo("iTunes.LibraryPlaylist.Tracks.Count");
    }

    @Override
    protected ITunesScript getLastTrackTags(String playlistName) {
        return new VBScript("set tracks = " + script.playlistByName(playlistName) + ".Tracks",
                "set lastTrack = tracks.Item(tracks.Count)",
                script.echo("lastTrack.name & \",\" & lastTrack.artist & \",\" & lastTrack.album"));
    }

}
