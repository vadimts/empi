package eu.tsvetkov.x_empi.command.itunes;

import eu.tsvetkov.x_empi.error.CommandException;
import eu.tsvetkov.x_empi.util.ITunes;
import eu.tsvetkov.x_empi.util.ITunes.Track;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class SyncPlaylist {

    private static final Logger log = LogManager.getLogger(SyncPlaylist.class);
    private static ITunes iTunes;
    private Result analysis;
    private Result error;
    private String libraryXmlPath;
    private Result success;
    private boolean useSystemLibrary;

    public SyncPlaylist() {
    }

//    public Result analyse(String directory, String playlistName) throws CommandException {
//        try {
//            analysis = new Result();
//
//            List<String> files = Util.File.getMp3InDirectory(directory).collect(toList());
//            List<Track> tracks = Script.getPlaylistTracks(playlistName);
//            List<String> trackLocations = tracks.parallelStream().map(Track::getPath).collect(toList());
//            Map<Boolean, List<Track>> missingAndExistingTracks = tracks.parallelStream()
//                .collect(partitioningBy(x -> Files.exists(Paths.get(x.getPath()))));
//            Map<Boolean, List<Track>> existingAndOutsideTracks = missingAndExistingTracks.get(Boolean.TRUE).parallelStream()
//                .collect(partitioningBy(x -> files.contains(x.getPath())));
//
//            analysis.existingTracks = existingAndOutsideTracks.get(Boolean.TRUE);
//            analysis.outsideTracks = existingAndOutsideTracks.get(Boolean.FALSE);
//            analysis.missingTracks = missingAndExistingTracks.get(Boolean.FALSE);
//            if (useSystemLibrary) {
//                analysis.missingTracks.forEach(x -> checkLocationWithLibrary(x, playlistName));
//            }
//            analysis.newTrackPaths = files.parallelStream().filter(x -> !trackLocations.contains(x)).collect(toList());
//
//            return analysis;
//        } catch (Exception e) {
//            throw new CommandException("ScriptError analysing directory '" + directory + "' and playlist '" + playlistName + "'", e);
//        }
//    }
//
//    public Result getAnalysis() {
//        return analysis;
//    }
//
//    public Result getError() {
//        return error;
//    }
//
//    public Result getSuccess() {
//        return success;
//    }
//
//    public void setLibraryXmlPath(String libraryXmlPath) {
//        this.libraryXmlPath = libraryXmlPath;
//    }
//
//    public void setUseSystemLibrary(boolean useSystemLibrary) {
//        this.useSystemLibrary = useSystemLibrary;
//    }

    public void sync(String directory) throws CommandException {
        sync(directory, directory.substring(directory.lastIndexOf("/") + 1));
    }

    public void sync(String directory, String playlistName) throws CommandException {
//        success = new Result();
//        error = new Result();
//        if (analysis == null) {
//            analysis = analyse(directory, playlistName);
//        }
//        for (Track track : analysis.getMissingTracks()) {
//            try {
//                Script.deleteTrackFromLibrary(track.getId());
//                success.getMissingTracks().add(track);
//            } catch (Exception e) {
//                error.getMissingTracks().add(track);
//            }
//        }
//        for (Track track : analysis.getOutsideTracks()) {
//            try {
//                Script.deleteTrackFromLibrary(track.getId());
//                success.getOutsideTracks().add(track);
//            } catch (Exception e) {
//                error.getOutsideTracks().add(track);
//            }
//        }
//        for (String trackPath : analysis.getNewTrackPaths()) {
//            try {
//                Track track = Script.addTrack(trackPath, playlistName);
//                success.getNewTracks().add(track);
//            } catch (Exception e) {
//                error.getNewTrackPaths().add(trackPath);
//            }
//        }
    }

//    protected void checkLocationWithLibrary(Track track, String playlistName) {
//        if (track.getPath().startsWith(BaseScript.ERROR_PREFIX)) {
//            try {
//                Track libTrack = getITunes().getTrackByIdFromXml(playlistName, track.getId());
//                if (libTrack != null) {
//                    track.setPath(libTrack.getPath());
//                } else {
//                    log.error("Location ofNumber track " + track.getId() + " not found in iTunes XML library.");
//                }
//            } catch (CommandException e) {
//                log.error("ScriptError getting location ofNumber track " + track.getId(), e);
//            }
//        }
//    }
//
//    protected ITunes getITunes() throws CommandException {
//        if (iTunes == null) {
//            iTunes = new ITunes(libraryXmlPath);
//        }
//        return iTunes;
//    }




    public class Result {

        List<Track> existingTracks = new ArrayList<>();
        List<Track> missingTracks = new ArrayList<>();
        List<String> newTrackPaths = new ArrayList<>();
        List<Track> newTracks = new ArrayList<>();
        List<Track> outsideTracks = new ArrayList<>();

        public List<Track> getExistingTracks() {
            return existingTracks;
        }

        public List<Track> getMissingTracks() {
            return missingTracks;
        }

        public List<String> getNewTrackPaths() {
            return newTrackPaths;
        }

        public List<Track> getNewTracks() {
            return newTracks;
        }

        public List<Track> getOutsideTracks() {
            return outsideTracks;
        }
    }

}
