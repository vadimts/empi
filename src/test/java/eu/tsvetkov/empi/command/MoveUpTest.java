package eu.tsvetkov.empi.command;

import org.junit.Test;

import static eu.tsvetkov.empi.command.Rename.PSEP;
import static eu.tsvetkov.empi.command.Rename.SEP;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class MoveUpTest extends CommandTest {

    public static final String DIR_ALBUM = YEAR2 + SEP + ALBUM2;

    @Test
    public void run() throws Exception {
        runCommand(new MoveUp(), getTestFilePath(ARTIST2 + PSEP + DIR_ALBUM));
        assertTestPath(DIR_ALBUM);
    }
}
