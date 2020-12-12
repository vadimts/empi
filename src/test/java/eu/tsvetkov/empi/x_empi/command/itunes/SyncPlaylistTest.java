package eu.tsvetkov.empi.command.itunes;

import eu.tsvetkov.empi.TestUtil;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.util.Track;
import eu.tsvetkov.empi.util.Track.NewTrack;
import org.junit.Test;

import java.util.List;

import static eu.tsvetkov.empi.TestUtil.newTracks;
import static eu.tsvetkov.empi.TestUtil.tracks;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class SyncPlaylistTest {

    @Test
    public void playlistSync() throws CommandException {
        System.out.println(getPlaylistSync("test-playlist", 200, 20, 10, 100, false));
        System.out.println(getPlaylistSync("test-playlist", 200, 20, 10, 100, true));
    }

    PlaylistSync getPlaylistSync(String playlistName, int cntUnmodified, int cntMisplaced, int cntMissing, int cntNew, boolean withErrors) {
        int k = (withErrors ? 3 : 1);

        PlaylistSync sync = new PlaylistSync(playlistName);

        sync.getUnmodifiedTracks().getBefore().addAll(TestUtil.tracks(cntUnmodified));

        List<Track> misplaced = TestUtil.tracks(cntMisplaced);
        sync.getMisplacedTracks().getBefore().addAll(misplaced);
        sync.getMisplacedTracks().getSuccess().addAll(misplaced.subList(0, cntMisplaced/ k));

        List<Track> missing = TestUtil.tracks(cntMissing);
        sync.getMissingTracks().getBefore().addAll(missing);
        sync.getMissingTracks().getSuccess().addAll(missing.subList(0, cntMissing/ k));

        List<NewTrack> newTracks = newTracks(cntNew);
        List<Track> addedTracks = tracks(newTracks);
        sync.getNewTracks().getBefore().addAll(newTracks);
        sync.getNewTracks().getSuccess().addAll(addedTracks.subList(0, cntNew/ k));

        if(withErrors) {
            sync.getMisplacedTracks().getError().addAll(misplaced.subList(cntMisplaced/ k, cntMisplaced));
            sync.getMissingTracks().getError().addAll(missing.subList(cntMissing/ k, cntMissing));
            sync.getNewTracks().getError().addAll(addedTracks.subList(cntNew/ k, cntNew));
        }

        return sync;
    }
}
