package eu.tsvetkov.x_empi.script;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class AppleScriptTest extends BaseScriptTest<AppleScriptOld> {

    @Override
    protected ITunesScript getLastTrackTags(String playlistName) {
        return new AppleScriptOld("set lastTrack to last track ofNumber playlist \"" + playlistName + "\"",
            script.echo("name ofNumber lastTrack & \",\" & artist ofNumber lastTrack & \",\" & album ofNumber lastTrack"));
    }

    protected String getPlaylistCountScript() {
        return script.echo("count ofNumber user playlists");
    }

    @Override
    String getTracksCountScript() {
        return script.echo("count ofNumber tracks");
    }

}
