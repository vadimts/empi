package eu.tsvetkov.empi.command.itunes;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.itunes.script.BaseScript;
import eu.tsvetkov.empi.util.ITunes;
import eu.tsvetkov.empi.util.Track;
import eu.tsvetkov.empi.util.Script;
import eu.tsvetkov.empi.util.Track.NewTrack;
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
    private static ITunes iTunes;
    private boolean useSystemLibrary;
    private String libraryXmlPath;
    private PlaylistSync sync;

    public SyncPlaylist() {
    }

    public void setLibraryXmlPath(String libraryXmlPath) {
        this.libraryXmlPath = libraryXmlPath;
    }

    public PlaylistSync analyse(String directory, String playlistName) throws CommandException {
        try {
            sync = new PlaylistSync(playlistName);

            List<String> files = Util.File.getMp3InDirectory(directory).collect(toList());
            List<Track> tracks = Script.getPlaylistTracks(playlistName);
            List<String> trackLocations = tracks.parallelStream().map(Track::getPath).collect(toList());
            Map<Boolean, List<Track>> missingAndExistingTracks = tracks.parallelStream()
                .collect(partitioningBy(x -> Files.exists(Paths.get(x.getPath()))));
            Map<Boolean, List<Track>> existingAndOutsideTracks = missingAndExistingTracks.get(Boolean.TRUE).parallelStream()
                .collect(partitioningBy(x -> files.contains(x.getPath())));

            sync.unmodifiedTracks.before = existingAndOutsideTracks.get(Boolean.TRUE);
            sync.misplacedTracks.before = existingAndOutsideTracks.get(Boolean.FALSE);
            sync.missingTracks.before = missingAndExistingTracks.get(Boolean.FALSE);
            if(useSystemLibrary) {
                sync.missingTracks.before.forEach(x -> checkLocationWithLibrary(x, playlistName));
            }
            sync.newTracks.before = files.parallelStream().filter(x -> !trackLocations.contains(x)).map(NewTrack::new).collect(toList());

            return sync;
        } catch (Exception e) {
            throw new CommandException("Error analysing directory '" + directory + "' and playlist '" + playlistName + "'", e);
        }
    }

    public void setUseSystemLibrary(boolean useSystemLibrary) {
        this.useSystemLibrary = useSystemLibrary;
    }

    public void sync(String directory, String playlistName) throws CommandException {
        if(sync == null) {
            sync = analyse(directory, playlistName);
        }
        for(Track track : sync.newTracks.before) {
            try {
                sync.newTracks.success.add(Script.addTrack(track.getPath(), playlistName));
            } catch (Exception e) {
                sync.newTracks.error.add(track);
            }
        }
        for(Track track : sync.missingTracks.before) {
            try {
                Script.deleteTrackFromLibrary(track.getId());
                sync.missingTracks.success.add(track);
            } catch (Exception e) {
                sync.missingTracks.error.add(track);
            }
        }
        for(Track track : sync.misplacedTracks.before) {
            try {
                Script.deleteTrackFromLibrary(track.getId());
                sync.misplacedTracks.success.add(track);
            } catch (Exception e) {
                sync.misplacedTracks.error.add(track);
            }
        }
    }

    public void sync(String directory) throws CommandException {
        sync(directory, directory.substring(directory.lastIndexOf("/") + 1));
    }

    public PlaylistSync getSync() {
        return sync;
    }

    protected void checkLocationWithLibrary(Track track, String playlistName) {
        if (track.getPath().startsWith(BaseScript.ERROR_PREFIX)) {
            try {
                Track libTrack = getITunes().getTrackByIdFromXml(playlistName, track.getId());
                if (libTrack != null) {
                    track.setPath(libTrack.getPath());
                } else {
                    log.warn("Location of track " + track.getId() + " not found in iTunes XML library.");
                }
            } catch (CommandException e) {
                log.error("Error getting location of track " + track.getId(), e);
            }
        }
    }

    protected ITunes getITunes() throws CommandException {
        if (iTunes == null) {
            iTunes = new ITunes(libraryXmlPath);
        }
        return iTunes;
    }

}
