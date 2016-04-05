package eu.tsvetkov.empi.mp3;

import eu.tsvetkov.empi.error.Mp3Exception;

import java.nio.file.Path;

import static eu.tsvetkov.empi.util.Util.defaultString;
import static eu.tsvetkov.empi.util.Util.differNotBlank;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class Mp3Info<T, U, V> {

    T mp3File;
    U tag1;
    V tag2;
    Path filePath;

    public Mp3Info(Path filePath) throws Mp3Exception {
        this.filePath = filePath;
        this.mp3File = getMp3File(filePath);
        this.tag2 = getID3v2();
        this.tag1 = getID3v1();
    }

    public String getAlbum() {
        return defaultString((tag2 != null ? getAlbumTag2() : null), (tag1 != null ? getAlbumTag1() : null));
    }

    public void setAlbum(String album) {
        if(tag1 != null) setAlbumTag1(album);
        if(tag2 != null) setAlbumTag2(album);
    }

    public String getAlbumArtist() {
        return defaultString((tag2 != null ? getAlbumArtistTag2() : null), (tag1 != null ? getArtistTag1() : null));
    }

    public void setAlbumArtist(String albumArtist) {
        if(tag2 != null) setAlbumArtistTag2(albumArtist);
    }

    public String getArtist() {
        return defaultString((tag2 != null ? getArtistTag2() : null), (tag1 != null ? getArtistTag1() : null));
    }

    public void setArtist(String artist) {
        if(tag1 != null) setArtistTag1(artist);
        if(tag2 != null) setArtistTag2(artist);
    }

    public abstract String getSortAlbum();

    public abstract void setSortAlbum(String sortAlbum);

    public abstract String getSortAlbumArtist();

    public abstract void setSortAlbumArtist(String sortAlbumArtist);

    public abstract String getSortArtist();

    public abstract void setSortArtist(String sortArtist);

    public abstract String getSortComposer();

    public abstract void setSortComposer(String sortComposer);

    public abstract String getSortTitle();

    public abstract void setSortTitle(String sortTitle);

    public String getTitle() {
        return defaultString((tag2 != null ? getTitleTag2() : null), (tag1 != null ? getTitleTag1() : null));
    }

    public void setTitle(String title) {
        if(tag1 != null) setTitleTag1(title);
        if(tag2 != null) setTitleTag2(title);
    }

    public String getTrackNumber() {
        return defaultString((tag2 != null ? getTrackNumberTag2() : null), (tag1 != null ? getTrackNumberTag1() : null));
    }

    public void setTrackNumber(String trackNumber) {
        if(tag1 != null) setTrackNumberTag1(trackNumber);
        if(tag2 != null) setTrackNumberTag2(trackNumber);
    }

    public String getYear() {
        return defaultString((tag2 != null ? getYearTag2() : null), (tag1 != null ? getYearTag1() : null));
    }

    public void setYear(String year) {
        if(tag1 != null) setYearTag1(year);
        if(tag2 != null) setYearTag2(year);
    }

    public abstract boolean isCompilation();

    public abstract void setCompilation(boolean compilation);

    public abstract void save() throws Mp3Exception;

    protected abstract String getAlbumArtistTag2();

    protected abstract void setAlbumArtistTag2(String albumArtist);

    protected abstract String getAlbumTag1();

    protected abstract void setAlbumTag1(String album);

    protected abstract String getAlbumTag2();

    protected abstract void setAlbumTag2(String album);

    protected abstract String getArtistTag1();

    protected abstract void setArtistTag1(String artist);

    protected abstract String getArtistTag2();

    protected abstract void setArtistTag2(String artist);

    protected abstract U getID3v1();

    protected abstract V getID3v2();

    protected abstract T getMp3File(Path filePath) throws Mp3Exception;

    protected abstract String getTitleTag1();

    protected abstract void setTitleTag1(String title);

    protected abstract String getTitleTag2();

    protected abstract void setTitleTag2(String title);

    protected abstract String getTrackNumberTag1();

    protected abstract void setTrackNumberTag1(String trackNumber);

    protected abstract String getTrackNumberTag2();

    protected abstract void setTrackNumberTag2(String trackNumber);

    protected abstract String getYearTag1();

    protected abstract void setYearTag1(String year);

    protected abstract String getYearTag2();

    protected abstract void setYearTag2(String year);

    @Override
    public String toString() {
        String artist = getArtist();
        String sortArtist = getSortArtist();
        String title = getTitle();
        String sortTitle = getSortTitle();
        String album = getAlbum();
        String sortAlbum = getSortAlbum();
        String year = getYear();
        return artist + (differNotBlank(artist, sortArtist) ? "/" + sortArtist : "")
            + " - " + title + (differNotBlank(title, sortTitle) ? "/" + sortTitle : "")
            + " ("
                + album + (differNotBlank(album, sortAlbum) ? "/" + sortAlbum : "")
                + ", " + year
                + (isCompilation() ? ", COMP" : "")
            + ")"
            + ": ID3v" + (tag1 != null ? "1" : "") + (tag1 != null && tag2 != null ? "+" : "") + (tag2 != null ? "2" : "");
    }
}
