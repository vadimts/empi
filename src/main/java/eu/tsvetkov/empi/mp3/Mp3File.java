package eu.tsvetkov.empi.mp3;

import eu.tsvetkov.empi.error.Mp3Exception;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static eu.tsvetkov.empi.mp3.Mp3Tag.*;
import static eu.tsvetkov.empi.util.Util.defaultString;
import static eu.tsvetkov.empi.util.Util.differNotBlank;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class Mp3File<T, U, V> {

    public static final String FILE_SUFFIX = ".mp3";

    T mp3File;
    U tag1;
    V tag2;
    Path filePath;

    public Mp3File(Path filePath) throws Mp3Exception {
        this.filePath = filePath;
        this.mp3File = getMp3File(filePath);
        this.tag2 = getID3v2();
        this.tag1 = getID3v1();
    }

    public static boolean isMp3File(Path filePath) {
        return (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().endsWith(FILE_SUFFIX));
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getTag(Mp3Tag tag) {
        return defaultString((tag2 != null ? getTag2(tag) : null), (TAGS_ID3V1.contains(tag) && tag1 != null ? getTag1(tag) : null));
    }

    public boolean getTagBoolean(Mp3Tag tag) {
        return "1".equals(getTag(tag));
    }

    public Map<Mp3Tag, String> getTagMap(List<Mp3Tag> tags) {
        Map<Mp3Tag, String> map = new LinkedHashMap<>();
        for (Mp3Tag tag : tags) {
            map.put(tag, getTag(tag));
        }
        return map;
    }

    public Map<Mp3Tag, String> getTagMapAll() {
        return getTagMap(TAGS_ALL);
    }

    public List<String> getTags(List<Mp3Tag> tags) {
        return new ArrayList<>(getTagMap(tags).values());
    }

    public List<String> getTagsAll() {
        return getTags(TAGS_ALL);
    }

    public abstract void save() throws Mp3Exception;

    public void setTag(Mp3Tag tag, String value) throws Mp3Exception {
        if (tag2 != null) setTag2(tag, value);
        if (TAGS_ID3V1.contains(tag) && tag1 != null) setTag1(tag, value);
    }

    public void setTagBoolean(Mp3Tag tag, Boolean value) throws Mp3Exception {
        setTag(tag, (value ? "1" : ""));
    }

    public Mp3File<T, U, V> setTags(Map<Mp3Tag, String> tagValues) throws Mp3Exception {
        for (Mp3Tag tag : tagValues.keySet()) {
            setTag(tag, tagValues.get(tag));
        }
        return this;
    }

    protected abstract U getID3v1();

    protected abstract V getID3v2();

    protected abstract T getMp3File(Path filePath) throws Mp3Exception;

    protected abstract String getTag1(Mp3Tag tag);

    protected abstract String getTag2(Mp3Tag tag);

    protected abstract void setTag1(Mp3Tag tag, String value) throws Mp3Exception;

    protected abstract void setTag2(Mp3Tag tag, String value) throws Mp3Exception;

    @Override
    public String toString() {
        String artist = getTag(ARTIST);
        String sortArtist = getTag(ARTIST_SORT);
        String title = getTag(TITLE);
        String sortTitle = getTag(TITLE_SORT);
        String album = getTag(ALBUM);
        String sortAlbum = getTag(ALBUM_SORT);
        String year = getTag(YEAR);
        return artist + (differNotBlank(artist, sortArtist) ? "/" + sortArtist : "")
            + " - " + title + (differNotBlank(title, sortTitle) ? "/" + sortTitle : "")
            + " ("
            + album + (differNotBlank(album, sortAlbum) ? "/" + sortAlbum : "")
            + ", " + year
            + (getTagBoolean(COMPILATION) ? ", COMP" : "")
            + ")"
            + ": ID3v" + (tag1 != null ? "1" : "") + (tag1 != null && tag2 != null ? "+" : "") + (tag2 != null ? "2" : "");
    }
}
