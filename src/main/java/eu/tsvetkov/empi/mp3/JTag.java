package eu.tsvetkov.empi.mp3;

import eu.tsvetkov.empi.error.Mp3Exception;
import eu.tsvetkov.empi.error.NotSupportedFileException;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

import static eu.tsvetkov.empi.mp3.Mp3Tag.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class JTag extends Mp3File<MP3File, ID3v1Tag, AbstractID3v2Tag> {

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

    public JTag(Path filePath) throws Mp3Exception {
        super(filePath);
    }

    @Override
    public String getTag1(Mp3Tag tag) {
        return tag1.getFirst(FIELD_KEYS.get(tag));
    }

    @Override
    public String getTag2(Mp3Tag tag) {
        try {
            return tag2.getFirst(FIELD_KEYS.get(tag));
        } catch (Exception e) {
            System.out.println("Error getting tag " + tag);
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void save() throws Mp3Exception {
        try {
            mp3File.commit();
        } catch (CannotWriteException e) {
            throw new Mp3Exception("Error saving MP3 file at path '" + filePath + "'", e);
        }
    }

    @Override
    public void setTag1(Mp3Tag tag, String value) throws Mp3Exception {
        try {
            tag1.setField(FIELD_KEYS.get(tag), value);
        } catch (FieldDataInvalidException e) {
            throw new Mp3Exception("Error setting Id3v1 tag " + tag + " = '" + value + "'", e);
        }
    }

    @Override
    public void setTag2(Mp3Tag tag, String value) throws Mp3Exception {
        try {
            tag2.setField(FIELD_KEYS.get(tag), value);
        } catch (FieldDataInvalidException e) {
            throw new Mp3Exception("Error setting Id3v2 tag " + tag + " = '" + value + "'", e);
        }
    }

    @Override
    protected ID3v1Tag getID3v1() {
        return mp3File.getID3v1Tag();
    }

    @Override
    protected AbstractID3v2Tag getID3v2() {
        return mp3File.getID3v2Tag();
    }

    @Override
    protected MP3File getMp3File(Path filePath) throws Mp3Exception {
        if(!isMp3File(filePath)) {
            throw new NotSupportedFileException("No MP3 file on path '" + filePath + "'");
        }

        try {
            return (MP3File) AudioFileIO.read(filePath.toFile());
        } catch (Exception e) {
            throw new Mp3Exception("Could not read MP3 file from path '" + filePath + "'", e);
        }
    }
}
