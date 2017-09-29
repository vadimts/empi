package eu.tsvetkov.empi.command.itunes;

import java.text.ChoiceFormat;

import static eu.tsvetkov.empi.command.itunes.TracksEdit.EditType.ADD;
import static eu.tsvetkov.empi.command.itunes.TracksEdit.EditType.DELETE;
import static eu.tsvetkov.empi.command.itunes.TracksEdit.EditType.NOOP;
import static eu.tsvetkov.empi.util.Util.joinLines;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class PlaylistSync {

    static final String TRACKS_COUNT_FORMAT_BEFORE  = "%-7s : %6d trk: %6d ok,  %6d misplaced, %6d missing";
    static final String TRACKS_COUNT_FORMAT_AFTER   = "%-7s : %6d trk: %6d new, %6d misplaced, %6d missing";
    static final String TRACKS_COUNT_FORMAT_SUCCESS = "%-7s : %9s trk: %9s new, %9s misplaced, %9s missing";
    static final String TRACKS_COUNT_FORMAT_ERROR   = "%-7s : %6d trk: %6d new, %6d misplaced, %6d missing";

    String playlistName;
    TracksEdit unmodifiedTracks = new TracksEdit(NOOP);
    TracksEdit misplacedTracks = new TracksEdit(DELETE);
    TracksEdit missingTracks = new TracksEdit(DELETE);
    TracksEdit newTracks = new TracksEdit(ADD);

    public PlaylistSync(String playlistName) {
        this.playlistName = playlistName;
    }

    public TracksEdit getUnmodifiedTracks() {
        return unmodifiedTracks;
    }

    public TracksEdit getMisplacedTracks() {
        return misplacedTracks;
    }

    public TracksEdit getMissingTracks() {
        return missingTracks;
    }

    public TracksEdit getNewTracks() {
        return newTracks;
    }

    @Override
    public String toString() {
//        String totalErrors = String.format("%6d", errorTrackCount());
        return joinLines(
            "Syncing playlist \"" + playlistName + "\"",
            String.format(TRACKS_COUNT_FORMAT_BEFORE, "Before", beforeTrackCount(), unmodifiedTracks.sizeBefore(), misplacedTracks.sizeBefore(), missingTracks.sizeBefore()),
            String.format(TRACKS_COUNT_FORMAT_AFTER, "After", afterTrackCount(), newTracks.sizeAfter(), misplacedTracks.sizeAfter(), missingTracks.sizeAfter()),
            String.format(TRACKS_COUNT_FORMAT_SUCCESS, "Process", p(editedTrackCount(),errorTrackCount()),  p(newTracks), p(misplacedTracks), p(missingTracks))
        );
    }

    private String p(TracksEdit te) {
        return p(te.sizeSuccess(), te.sizeError());
    }

    private String p(int success, int error) {
        return "+" + String.valueOf(success) + "/!" + String.valueOf(error);
    }

    private int beforeTrackCount() {
        return unmodifiedTracks.sizeBefore() + misplacedTracks.sizeBefore() + missingTracks.sizeBefore();
    }

    private int afterTrackCount() {
        return unmodifiedTracks.sizeBefore() + misplacedTracks.sizeAfter() + missingTracks.sizeAfter() + newTracks.sizeAfter();
    }

    private int editedTrackCount() {
        return misplacedTracks.sizeSuccess() + missingTracks.sizeSuccess() + newTracks.sizeSuccess();
    }

    private int errorTrackCount() {
        return misplacedTracks.sizeError() + missingTracks.sizeError() + newTracks.sizeError();
    }

    class MyFormat extends ChoiceFormat {

        public MyFormat(String newPattern) {
            super(newPattern);
        }

    }
}
