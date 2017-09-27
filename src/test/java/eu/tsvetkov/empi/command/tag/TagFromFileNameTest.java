package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.BaseTest;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.Mp3Exception;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagFromFileNameTest extends BaseTest {

    public static final String TRACK_NO = "01";
    public static final String ARTIST = "artist";
    public static final String TITLE = "track_title_(feat._other_artist)";
    private TagFromFileName command;
    private String fileName;

    @Before
    public void before() {
        command = new TagFromFileName() {
            @Override
            protected String getFileName() {
//                return TRACK_NO + "-" + ARTIST + "--" + TITLE + "-team.mp3";
                return fileName;
            }
        };
    }

    @Test
    public void transformTags() throws CommandException {
        fileName = TRACK_NO + " " + ARTIST + " - " + TITLE + ".mp3";
        TagMap tagMap = command.transformTags(createTagMap());

        assertEquals(TRACK_NO, tagMap.getNewTags().get(Mp3Tag.TRACK_NO));
        assertEquals(ARTIST, tagMap.getNewTags().get(Mp3Tag.ARTIST));
        assertEquals(TITLE, tagMap.getNewTags().get(Mp3Tag.TITLE));
    }

}
