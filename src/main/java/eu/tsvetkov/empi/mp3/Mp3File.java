package eu.tsvetkov.empi.mp3;

import eu.tsvetkov.empi.model.AudioArtwork;
import eu.tsvetkov.empi.x_empi.error.Mp3Exception;
import eu.tsvetkov.empi.x_empi.error.NotSupportedFileException;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.LogManager;

import static eu.tsvetkov.empi.mp3.Mp3Tag.*;
import static eu.tsvetkov.empi.util.Util.defaultString;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Mp3File {

    public static final String FILE_SUFFIX = ".mp3";
    private static final Map<Mp3Tag, FieldKey> FIELD_KEYS = new HashMap<>();

    static {
        // Disable default console logging.
        LogManager.getLogManager().reset();

        FIELD_KEYS.put(TITLE, FieldKey.TITLE);
        FIELD_KEYS.put(TITLE_SORT, FieldKey.TITLE_SORT);
        FIELD_KEYS.put(ARTIST, FieldKey.ARTIST);
        FIELD_KEYS.put(ARTIST_SORT, FieldKey.ARTIST_SORT);
        FIELD_KEYS.put(ALBUM, FieldKey.ALBUM);
        FIELD_KEYS.put(ALBUM_SORT, FieldKey.ALBUM_SORT);
        FIELD_KEYS.put(ALBUM_ARTIST, FieldKey.ALBUM_ARTIST);
        FIELD_KEYS.put(ALBUM_ARTIST_SORT, FieldKey.ALBUM_ARTIST_SORT);
        FIELD_KEYS.put(COMPOSER, FieldKey.COMPOSER);
        FIELD_KEYS.put(COMPOSER_SORT, FieldKey.COMPOSER_SORT);
        FIELD_KEYS.put(GROUPING, FieldKey.GROUPING);
        FIELD_KEYS.put(GENRE, FieldKey.GENRE);
        FIELD_KEYS.put(YEAR, FieldKey.YEAR);
        FIELD_KEYS.put(TRACK_NO, FieldKey.TRACK);
        FIELD_KEYS.put(TRACK_TOTAL, FieldKey.TRACK_TOTAL);
        FIELD_KEYS.put(DISC_NO, FieldKey.DISC_NO);
        FIELD_KEYS.put(DISC_TOTAL, FieldKey.DISC_TOTAL);
        FIELD_KEYS.put(COMPILATION, FieldKey.IS_COMPILATION);
        FIELD_KEYS.put(RATING, FieldKey.RATING);
        FIELD_KEYS.put(COMMENT, FieldKey.COMMENT);
    }

    List<Mp3Exception> errors = new ArrayList<>();
    Path filePath;
    MP3File mp3File;
    ID3v1Tag tag1;
    AbstractID3v2Tag tag2;

    public Mp3File(String filePath) {
        this(Paths.get(filePath));
    }

    public Mp3File(Path filePath) {
        this.filePath = filePath;
        try {
            this.mp3File = getMp3File(filePath);
            this.tag2 = getID3v2();
            this.tag1 = getID3v1();
        } catch (Mp3Exception e) {
            errors.add(e);
        }
    }

    public static boolean isMp3File(Path filePath) {
        return (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().endsWith(FILE_SUFFIX));
    }

    public AudioArtwork getAudioArtwork(Artwork artwork) {
        return artwork != null ? AudioArtwork.from(artwork).setPath(this.filePath) : null;
    }

    public Path getFilePath() {
        return filePath;
    }

    public long getSize() {
        try {
            Field fileSizeField = MP3AudioHeader.class.getDeclaredField("fileSize");
            fileSizeField.setAccessible(true);
            return (Long) fileSizeField.get(mp3File.getMP3AudioHeader());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getTag(Mp3Tag tag) {
        return defaultString((tag2 != null ? getTag2(tag) : null), (TAGS_ID3V1.contains(tag) && tag1 != null ? getTag1(tag) : null));
    }

    public String getTag1(Mp3Tag tag) {
        return tag1.getFirst(FIELD_KEYS.get(tag));
    }

    public ID3v1Tag getTag1() {
        return tag1;
    }

    public AudioArtwork getTag1Artwork() {
        return (tag1 != null ? getAudioArtwork(tag1.getFirstArtwork()) : null);
    }

    public String getTag2(Mp3Tag tag) {
        try {
            return tag2.getFirst(FIELD_KEYS.get(tag));
        } catch (Exception e) {
            System.out.println("ScriptError getting tag " + tag);
            e.printStackTrace();
            throw e;
        }
    }

    public AbstractID3v2Tag getTag2() {
        return tag2;
    }

    public AudioArtwork getTag2Artwork() {
        return (tag2 != null ? getAudioArtwork(tag2.getFirstArtwork()) : null);
    }

    public Mp3File setTag2Artwork(AudioArtwork audioArtwork) {
        if (tag2 != null) {
            try {
                tag2.setField(audioArtwork.getArtwork());
            } catch (FieldDataInvalidException e) {
                e.printStackTrace();
            }
        }
        return this;
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

    public boolean isOk() {
        return (errors.isEmpty());
    }

    public void save() {
        try {
            mp3File.commit();
        } catch (CannotWriteException e) {
            errors.add(new Mp3Exception("ScriptError saving MP3 file at path '" + filePath + "'", e));
        }
    }

    public void setTag(Mp3Tag tag, String value) {
        if (tag2 != null) setTag2(tag, value);
        if (TAGS_ID3V1.contains(tag) && tag1 != null) setTag1(tag, value);
    }

    public void setTag1(Mp3Tag tag, String value) {
        try {
            tag1.setField(FIELD_KEYS.get(tag), value);
        } catch (FieldDataInvalidException e) {
            errors.add(new Mp3Exception("ScriptError setting Id3v1 tag " + tag + " = '" + value + "'", e));
        }
    }

    public void setTag2(Mp3Tag tag, String value) {
        try {
            tag2.setField(FIELD_KEYS.get(tag), value);
        } catch (FieldDataInvalidException e) {
            errors.add(new Mp3Exception("ScriptError setting Id3v2 tag " + tag + " = '" + value + "'", e));
        }
    }

    public void setTagBoolean(Mp3Tag tag, Boolean value) {
        setTag(tag, (value ? "1" : ""));
    }

    public Mp3File setTags(Map<Mp3Tag, String> tagValues) {
        for (Mp3Tag tag : tagValues.keySet()) {
            setTag(tag, tagValues.get(tag));
        }
        return this;
    }

    public Mp3File tag(Mp3Tag tag, Object value) {
        setTag(tag, String.valueOf(value));
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + " " + getSize();
    }

    protected ID3v1Tag getID3v1() {
        return mp3File.getID3v1Tag();
    }

    protected AbstractID3v2Tag getID3v2() {
        return mp3File.getID3v2Tag();
    }

    protected MP3File getMp3File(Path filePath) throws Mp3Exception {
        if (!isMp3File(filePath)) {
            throw new NotSupportedFileException("No MP3 file on path '" + filePath + "'");
        }

        try {
            return (MP3File) AudioFileIO.read(filePath.toFile());
        } catch (Exception e) {
            throw new Mp3Exception("Could not read MP3 file from path '" + filePath + "'", e);
        }
    }
}
