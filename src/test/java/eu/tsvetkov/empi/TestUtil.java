package eu.tsvetkov.empi;

import eu.tsvetkov.empi.util.Track;
import eu.tsvetkov.empi.util.Track.NewTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TestUtil {

    public static final List<String> TRACK_PATHS = Arrays.asList(
        "C:\\etc\\empi\\target\\test-classes\\sync\\playlistDir\\existingTracks\\test-playlist-1506598833050-tags-de.mp3",
        "C:\\etc\\empi\\target\\test-classes\\sync\\playlistDir\\existingTracks\\1989 - Magnetic Mirror Master Mix (with The Upsetters)\\test-playlist-1506598833050-tags-de.mp3",
        "C:\\etc\\empi\\target\\test-classes\\sync\\playlistDir\\newTracks\\Lime_Dubs-Jade_and_Matt_U-LIME004-VINYL-2011-sweet\\test-playlist-1506598833050-tags-de.mp3"
    );

    public static List<Track> tracks(int count) {
        return IntStream.range(0, count).mapToObj(x -> new Track(10000+x, TRACK_PATHS.get(new Random().nextInt(TRACK_PATHS.size())))).collect(toList());
    }

    public static List<Track> tracks(List<NewTrack> newTracks) {
        List<Track> tracks = new ArrayList<>(newTracks);
        tracks.replaceAll(x -> new Track(new Random().nextInt(10000), x.getPath()));
        return tracks;
    }

    public static List<NewTrack> newTracks(int count) {
        return IntStream.range(0, count).mapToObj(x -> new NewTrack(TRACK_PATHS.get(new Random().nextInt(TRACK_PATHS.size())))).collect(toList());
    }
}
