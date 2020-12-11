package eu.tsvetkov.empi.empi2;

import eu.tsvetkov.empi.mp3.Mp3File;

import java.nio.file.Path;

import static eu.tsvetkov.empi.mp3.Mp3Tag.*;
import static eu.tsvetkov.empi.util.Str.esc;

public class TrackId {

    String album;
    String artist;
    String name;
    long size;

    public TrackId(String name, String artist, String album, long size) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.size = size;
    }

    public static TrackId of(Mp3File mp3) {
        return new TrackId(esc(mp3.getTag(TITLE)), esc(mp3.getTag(ARTIST)), esc(mp3.getTag(ALBUM)), mp3.getSize());
    }

    public static TrackId of(Path path) {
        return of(new Mp3File(path));
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
