package eu.tsvetkov.empi.mp3;

import eu.tsvetkov.empi.error.Mp3Exception;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;

import java.nio.file.Path;
import java.util.logging.LogManager;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class JTag extends Mp3Info<MP3File,ID3v1Tag,AbstractID3v2Tag> {

    static {
        // Disable default console logging.
        LogManager.getLogManager().reset();
    }

    public JTag(Path filePath) throws Mp3Exception {
        super(filePath);
    }

    @Override
    public String getSortAlbum() {
        return tag2.getFirst(FieldKey.ALBUM_SORT);
    }

    @Override
    public void setSortAlbum(String sortAlbum) {
        try {
            tag2.setField(FieldKey.ALBUM_SORT, sortAlbum);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    public String getSortAlbumArtist() {
        return tag2.getFirst(FieldKey.ALBUM_ARTIST_SORT);
    }

    @Override
    public void setSortAlbumArtist(String sortAlbumArtist) {
        try {
            tag2.setField(FieldKey.ALBUM_ARTIST_SORT, sortAlbumArtist);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    public String getSortArtist() {
        return tag2.getFirst(FieldKey.ARTIST_SORT);
    }

    @Override
    public void setSortArtist(String sortArtist) {
        try {
            tag2.setField(FieldKey.ARTIST_SORT, sortArtist);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    public String getSortComposer() {
        return tag2.getFirst(FieldKey.COMPOSER_SORT);
    }

    @Override
    public void setSortComposer(String sortComposer) {
        try {
            tag2.setField(FieldKey.COMPOSER_SORT, sortComposer);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    public String getSortTitle() {
        return tag2.getFirst(FieldKey.TITLE_SORT);
    }

    @Override
    public void setSortTitle(String sortTitle) {
        try {
            tag2.setField(FieldKey.TITLE_SORT, sortTitle);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    public boolean isCompilation() {
        return "1".equals(tag2.getFirst(FieldKey.IS_COMPILATION));
    }

    @Override
    public void setCompilation(boolean compilation) {
        try {
            tag2.setField(FieldKey.IS_COMPILATION, (compilation ? "1" : ""));
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
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
    protected String getAlbumArtistTag2() {
        return tag2.getFirst(FieldKey.ALBUM_ARTIST);
    }

    @Override
    protected void setAlbumArtistTag2(String albumArtist) {
        try {
            tag2.setField(FieldKey.ALBUM_ARTIST, albumArtist);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getAlbumTag1() {
        return tag1.getFirst(FieldKey.ALBUM);
    }

    @Override
    protected void setAlbumTag1(String album) {
        try {
            tag1.setField(FieldKey.ALBUM, album);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getAlbumTag2() {
        return tag2.getFirst(FieldKey.ALBUM);
    }

    @Override
    protected void setAlbumTag2(String album) {
        try {
            tag2.setField(FieldKey.ALBUM, album);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getArtistTag1() {
        return tag1.getFirst(FieldKey.ARTIST);
    }

    @Override
    protected void setArtistTag1(String artist) {
        try {
            tag1.setField(FieldKey.ARTIST, artist);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getArtistTag2() {
        return tag2.getFirst(FieldKey.ARTIST);
    }

    @Override
    protected void setArtistTag2(String artist) {
        try {
            tag2.setField(FieldKey.ARTIST, artist);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
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
        try {
            return (MP3File) AudioFileIO.read(filePath.toFile());
        } catch (Exception e) {
            throw new Mp3Exception("Could not read MP3 file from path '" + filePath + "'", e);
        }
    }

    @Override
    protected String getTitleTag1() {
        return tag1.getFirst(FieldKey.TITLE);
    }

    @Override
    protected void setTitleTag1(String title) {
        try {
            tag1.setField(FieldKey.TITLE, title);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getTitleTag2() {
        return tag2.getFirst(FieldKey.TITLE);
    }

    @Override
    protected void setTitleTag2(String title) {
        try {
            tag2.setField(FieldKey.TITLE, title);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getTrackNumberTag1() {
        return tag1.getFirst(FieldKey.TRACK);
    }

    @Override
    protected void setTrackNumberTag1(String trackNumber) {
        try {
            tag1.setField(FieldKey.TRACK, trackNumber);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getTrackNumberTag2() {
        return tag2.getFirst(FieldKey.TITLE);
    }

    @Override
    protected void setTrackNumberTag2(String trackNumber) {
        try {
            tag2.setField(FieldKey.TRACK, trackNumber);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getYearTag1() {
        return tag1.getFirst(FieldKey.YEAR);
    }

    @Override
    protected void setYearTag1(String year) {
        try {
            tag1.setField(FieldKey.YEAR, year);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }

    @Override
    protected String getYearTag2() {
        return tag2.getFirst(FieldKey.YEAR);
    }

    @Override
    protected void setYearTag2(String year) {
        try {
            tag2.setField(FieldKey.YEAR, year);
        } catch (FieldDataInvalidException e) {
            System.out.println("Error setting tag field: " + e);
        }
    }
}
