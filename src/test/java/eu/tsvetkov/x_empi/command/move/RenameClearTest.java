package eu.tsvetkov.x_empi.command.move;

import eu.tsvetkov.x_empi.command.CommandTest;
import eu.tsvetkov.x_empi.error.CommandException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class RenameClearTest extends CommandTest<RenameClear> {

    @Test
    public void run() throws Exception {
        runCommand(new RenameClear(SUFFIX), getTestFilePath(YEAR_ARTIST_ALBUM_SUFFIX));
        assertTestPath(YEAR_ARTIST_ALBUM);
    }

    @Test
    public void transformFileName() throws CommandException {
        assertEquals("[2009] Dom Hz & Synkro - Dub Discipline | Sacred Moments (DUBOUT007)",
            new RenameClear("-WEB").transformFileName("[2009] Dom Hz & Synkro - Dub Discipline | Sacred Moments (DUBOUT007)-WEB"));
    }
}
