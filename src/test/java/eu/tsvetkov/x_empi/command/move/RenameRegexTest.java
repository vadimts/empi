package eu.tsvetkov.x_empi.command.move;

import eu.tsvetkov.empi.util.Util;
import eu.tsvetkov.x_empi.command.CommandTest;
import org.junit.Before;
import org.junit.Test;

import static eu.tsvetkov.empi.util.Util.SEP;
import static eu.tsvetkov.x_empi.command.move.RenameRegex.GROUP;
import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class RenameRegexTest extends CommandTest<RenameRegex> {

    @Before
    public void before() {
    }

    @Test
    public void getNewFileName() throws Exception {
        RenameRegex command = new RenameRegex(Util.WORD + SEP + GROUP, "$2" + SEP + "$1");
        assertEquals(ARTIST_ALBUM_SUFFIX + SEP + YEAR, command.transformFileName(getTestFileName(YEAR_ARTIST_ALBUM_SUFFIX)));
    }

    @Test
    public void parseMetaRegex() {
        assertParsedMeta("12=2-1", "^" + Util.WORD + " " + Util.WORD + "$", "$2-$1");
        assertParsedMeta("1 2=2 1", "^" + Util.WORD + " " + Util.WORD + "$", "$2 $1");
        assertParsedMeta("1-2=2-1", "^" + Util.WORD + "-" + Util.WORD + "$", "$2-$1");
        assertParsedMeta("1 - 2=2 - 1", "^" + Util.WORD + " - " + Util.WORD + "$", "$2 - $1");
        assertParsedMeta("1*L=* (1, L)", "^" + Util.WORD + SEP + GROUP + SEP + Util.WORD + "$", "$2 ($1, $3)");
    }

    @Test
    public void patterns() {
        assertEquals("two - one", "one - one".replaceFirst(Util.WORD, "two"));
        assertEquals("two - two", "one - one".replaceAll(Util.WORD, "two"));
        assertEquals("two - one", "(2016) - one".replaceFirst(Util.WORD, "two"));
        assertEquals("two - two", "(2016) - one".replaceAll(Util.WORD, "two"));
        assertEquals("two - two", "one-one - one".replaceAll(Util.WORD, "two"));
        assertEquals("two - two - two", "one-one - one-one - one".replaceAll(Util.WORD, "two"));
    }

    @Test
    public void run() throws Exception {
        RenameRegex command = new RenameRegex(Util.WORD + SEP + GROUP, "$2" + SEP + "$1");
        runCommand(command, getTestFilePath(YEAR_ARTIST_ALBUM_SUFFIX));
        assertTestPath(ARTIST_ALBUM_SUFFIX_YEAR);
    }

    protected void assertParsedMeta(String metaRegex, String expectedFrom, String expectedTo) {
        RenameRegex command = new RenameRegex(metaRegex);
        assertEquals(expectedFrom, command.from.pattern());
        assertEquals(expectedTo, command.to);
    }
}
