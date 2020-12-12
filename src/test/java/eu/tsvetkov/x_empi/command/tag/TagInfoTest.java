package eu.tsvetkov.x_empi.command.tag;

import eu.tsvetkov.empi.mp3.TagMap;
import eu.tsvetkov.x_empi.BaseTest;
import eu.tsvetkov.x_empi.error.CommandException;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagInfoTest extends BaseTest {

    @Test
    public void tagInfo() throws CommandException {
        TagMap tagMap = new TagInfo().run(Paths.get("/Volumes/Volume_1/itunes/new/Mikey Dread - World War III (1980)/01 - The Jumping Master.mp3"));
        System.out.println(tagMap);
    }
}
