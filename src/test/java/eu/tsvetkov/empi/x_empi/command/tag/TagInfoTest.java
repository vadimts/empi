package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.BaseTest;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.Mp3Exception;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static eu.tsvetkov.empi.mp3.Mp3Tag.*;
import static junit.framework.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagInfoTest extends BaseTest {

    @Test
    public void tagInfo() throws CommandException {
        TagMap tagMap = new TagInfo().run(Paths.get("/Volumes/Volume_1/music/new/Mikey Dread - World War III (1980)/01 - The Jumping Master.mp3"));
        System.out.println(tagMap);
    }
}
