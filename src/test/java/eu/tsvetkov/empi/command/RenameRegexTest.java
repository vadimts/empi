package eu.tsvetkov.empi.command;

import org.junit.Test;

import static eu.tsvetkov.empi.command.Rename.SEP;
import static eu.tsvetkov.empi.command.RenameRegex.GROUP;
import static eu.tsvetkov.empi.command.RenameRegex.WORD;
import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class RenameRegexTest extends CommandTest<RenameRegex> {

    @Test
    public void patterns() {
        assertEquals("two - one", "one - one".replaceFirst(WORD, "two"));
        assertEquals("two - two", "one - one".replaceAll(WORD, "two"));
        assertEquals("two - one", "(2016) - one".replaceFirst(WORD, "two"));
        assertEquals("two - two", "(2016) - one".replaceAll(WORD, "two"));
        assertEquals("two - two", "one-one - one".replaceAll(WORD, "two"));
        assertEquals("two - two - two", "one-one - one-one - one".replaceAll(WORD, "two"));
    }

    @Test
    public void getNewFileName() throws Exception {
        RenameRegex command = new RenameRegex(WORD + SEP + GROUP, "$2" + SEP + "$1");
        assertEquals(ARTIST_ALBUM_SUFFIX + SEP + YEAR, command.transformFileName(getTestFileName(YEAR_ARTIST_ALBUM_SUFFIX)));
    }

    @Test
    public void run() throws Exception {
        RenameRegex command = new RenameRegex(WORD + SEP + GROUP, "$2" + SEP + "$1");
        runCommand(command, getTestFilePath(YEAR_ARTIST_ALBUM_SUFFIX));
        assertTestPath(ARTIST_ALBUM_SUFFIX_YEAR);
    }
}
