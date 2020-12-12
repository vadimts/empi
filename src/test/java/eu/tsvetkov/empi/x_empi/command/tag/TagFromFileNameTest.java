package eu.tsvetkov.empi.x_empi.command.tag;

import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;
import eu.tsvetkov.empi.x_empi.BaseTest;
import eu.tsvetkov.empi.x_empi.error.CommandException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagFromFileNameTest extends BaseTest {

    public static final String ARTIST = "artist";
    public static final String TITLE = "track_title_(feat._other_artist)";
    public static final String TRACK_NO = "01";
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
