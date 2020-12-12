package eu.tsvetkov.empi.x_empi.command.tag;

import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;
import eu.tsvetkov.empi.x_empi.BaseTest;
import eu.tsvetkov.empi.x_empi.error.CommandException;
import junit.framework.Assert;
import org.junit.Test;

import static eu.tsvetkov.empi.mp3.Mp3Tag.ALBUM;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagTest extends BaseTest {

    private TagMap tagMap;

    @Test
    public void transformAlbum() throws CommandException {
        tagMap = new Tag("Al:" + ALBUM_NEW).transformTags(createTagMap());
        assertNewTag(ALBUM, ALBUM_NEW);

        tagMap = new Tag("Al:^.*$=" + ALBUM_NEW).transformTags(createTagMap());
        assertNewTag(ALBUM, ALBUM_NEW);

        tagMap = createTagMap();
        tagMap.getOldTags().put(ALBUM, "old album 01");
        tagMap = new Tag("Al:^.* (\\d+)$=new album $1").transformTags(tagMap);
        assertNewTag(ALBUM, "new album 01");

        tagMap = createTagMap();
        tagMap.getOldTags().put(ALBUM, "old_album_name");
        tagMap = new Tag("Al:_= ").transformTags(tagMap);
        assertNewTag(ALBUM, "old album name");
    }

    protected void assertNewTag(Mp3Tag tag, String newValue) {
        Assert.assertEquals(1, tagMap.getNewTags().size());
        Assert.assertEquals(newValue, tagMap.getNewTags().get(tag));
    }
}
