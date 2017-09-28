package eu.tsvetkov.empi.command.itunes;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.FileException;
import eu.tsvetkov.empi.util.ITunes;
import eu.tsvetkov.empi.util.ITunes.Track;
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
    private ITunes iTunes;

    public SyncPlaylist() throws CommandException {
        iTunes = new ITunes(ITunes.ITUNES_LIB);
    }

    public Result analyse(String directory, String playlistName) throws FileException {
        Result result = new Result();

        List<String> files = Util.File.getMp3InDirectory(directory).collect(toList());
        List<Track> tracks = ITunes.Script.getPlaylistTracks(playlistName);
        List<String> trackLocations = tracks.parallelStream().map(Track::getLocation).collect(toList());
        Map<Boolean, List<Track>> missingAndExistingTracks = tracks.parallelStream()
            .collect(partitioningBy(x -> Files.exists(Paths.get(x.getLocation()))));
        Map<Boolean, List<Track>> existingAndOutsideTracks = missingAndExistingTracks.get(Boolean.TRUE).parallelStream()
            .collect(partitioningBy(x -> files.contains(x.getLocation())));

        result.existingTracks = existingAndOutsideTracks.get(Boolean.TRUE);
        result.outsideTracks = existingAndOutsideTracks.get(Boolean.FALSE);
        result.missingTracks = missingAndExistingTracks.get(Boolean.FALSE);
        result.missingTracks.forEach(x -> checkLocationWithLibrary(x, playlistName));
        result.newTracks = files.parallelStream().filter(x -> !trackLocations.contains(x)).collect(toList());

        return result;
    }

    public void sync(String directory, String playlistName) throws CommandException {
        Result result = analyse(directory, playlistName);
//        newFiles.forEach(x -> ITunes.Script.addTrack(x, playlistName));
    }

    public void sync(String directory) throws CommandException {
        sync(directory, directory.substring(directory.lastIndexOf("/") + 1));
    }

    protected void checkLocationWithLibrary(Track track, String playlistName) {
        if (track.getLocation().startsWith(ITunes.Script.ERROR_PREFIX)) {
            try {
                Track libTrack = iTunes.getTrackByIdFromXml(playlistName, track.getId());
                if (libTrack != null) {
                    track.setLocation(libTrack.getLocation());
                } else {
                    log.error("Location of track " + track.getId() + " not found in iTunes XML library.");
                }
            } catch (CommandException e) {
                log.error("Error getting location of track " + track.getId(), e);
            }
        }
    }

    public class Result {

        List<Track> existingTracks;
        List<Track> outsideTracks;
        List<Track> missingTracks;
        List<String> newTracks;

        public List<Track> getExistingTracks() {
            return existingTracks;
        }

        public List<Track> getMissingTracks() {
            return missingTracks;
        }

        public List<String> getNewTracks() {
            return newTracks;
        }

        public List<Track> getOutsideTracks() {
            return outsideTracks;
        }
    }

}
