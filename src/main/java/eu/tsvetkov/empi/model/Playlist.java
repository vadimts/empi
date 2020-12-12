package eu.tsvetkov.empi.model;

import eu.tsvetkov.empi.ops.MusicOps;

public class Playlist {
    String name;
    int size;

    public Playlist(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public Playlist(String name) {
        this(name, MusicOps.getPlaylistSize(name));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
