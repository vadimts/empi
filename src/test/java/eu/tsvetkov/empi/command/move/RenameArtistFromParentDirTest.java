package eu.tsvetkov.empi.command.move;

import eu.tsvetkov.empi.command.CommandTest;
import eu.tsvetkov.empi.command.move.RenameArtistFromParentDir;
import org.junit.Test;

import static eu.tsvetkov.empi.command.move.Rename.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class RenameArtistFromParentDirTest extends CommandTest {

    @Test
    public void run() throws Exception {
        runCommand(new RenameArtistFromParentDir(), getTestFilePath(ARTIST2 + PSEP + YEAR2 + SEP + ALBUM2));
        assertTestPath(ARTIST2 + PSEP + ARTIST2 + SEP_ARTIST_ALBUM + YEAR2 + SEP + ALBUM2);
    }
}
