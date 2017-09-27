package eu.tsvetkov.empi.command.itunes;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.util.ITunes;
import eu.tsvetkov.empi.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class SyncPlaylist {

    private static final Logger log = LogManager.getLogger(SyncPlaylist.class);
    private List<ITunes.Track> libTracks;
    private ITunes iTunes;

    public SyncPlaylist() throws CommandException {
        iTunes = new ITunes(ITunes.ITUNES_LIB);
    }

    public void diffDirectoryPlaylist(String directory, String playlistName) throws CommandException {
        List<String> files = Util.File.getMp3InDirectory(directory).collect(toList());
        List<ITunes.Track> tracks = iTunes.getPlaylistTracks(playlistName);

        Map<Boolean, List<ITunes.Track>> missingAndExistingTracks = tracks.parallelStream()
            .collect(partitioningBy(x -> Files.exists(Paths.get(x.getLocation()))));
        List<ITunes.Track> existingTracks = missingAndExistingTracks.get(Boolean.TRUE);
        List<ITunes.Track> missingTracks = missingAndExistingTracks.get(Boolean.FALSE);
        missingTracks.forEach(x -> checkLocationWithLib(x, playlistName));
        log.debug("existing " + existingTracks.size());
        existingTracks.forEach(log::debug);
        log.debug("missing " + missingTracks.size());
        missingTracks.forEach(log::debug);

        List<ITunes.Track> tracksOutsideDir = existingTracks.parallelStream().filter(x -> !files.contains(x.getLocation())).collect(toList());
        log.debug("outside " + tracksOutsideDir.size());
        tracksOutsideDir.forEach(log::debug);

        List<String> newFiles = files;
        log.debug("new " + newFiles.size());
        newFiles.forEach(log::debug);
        libTracks = null;
    }

    public void sync() {
        String playlist = "test";
        List<ITunes.Track> trackPlaylist = ITunes.Script.getPlaylistTracks(playlist);
    }

    protected void checkLocationWithLib(ITunes.Track track, String playlistName) {
        if (track.getLocation().startsWith("ERROR: ")) {
            if (libTracks == null) {
                try {
                    libTracks = ITunes.getPlaylistTracks(playlistName);
                } catch (CommandException e) {
                    log.error(e);
                }
            }
            ITunes.Track libTrack = ITunes.Track.getTrackById(ITunes.libTracks, track.getId());
            if (libTrack != null) {
                track.setLocation(libTrack.getLocation());
            } else {
                log.error("Location of track " + track.getId() + " not found in iTunes XML library.");
            }
        }
    }

}
