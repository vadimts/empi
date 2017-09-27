package eu.tsvetkov.empi.itunes.script;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class AppleScriptTest extends BaseScriptTest<AppleScript> {

    protected String getPlaylistCountScript() {
        return script.echo("count of user playlists");
    }

    @Override
    String getTracksCountScript() {
        return script.echo("count of library's tracks");
    }

    @Override
    protected ITunesScript getLastTrackTags(String playlistName) {
        return new AppleScript("set lastTrack to last track of library",
            script.echo("lastTrack.name & \",\" & lastTrack.artist & \",\" & lastTrack.album"));
    }

}
