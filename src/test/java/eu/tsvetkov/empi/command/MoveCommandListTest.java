package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.command.move.MoveUp;
import eu.tsvetkov.empi.command.move.RenameArtistFromParentDir;
import eu.tsvetkov.empi.command.move.RenameRegex;
import org.junit.Test;

import static eu.tsvetkov.empi.command.move.Rename.*;
import static eu.tsvetkov.empi.command.move.RenameRegex.GROUP;
import static eu.tsvetkov.empi.command.move.RenameRegex.WORD;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class MoveCommandListTest extends CommandTest {

    @Test
    public void run() throws Exception {
        CommandList command = new CommandList(
            new RenameRegex(WORD + SEP + GROUP, "$2" + SEP + "$1"),
            new RenameArtistFromParentDir(),
            new MoveUp());
        runCommand(command, getTestFilePath(ARTIST2 + PSEP + YEAR_ALBUM2));
        assertTestPath(ARTIST2 + SEP_ARTIST_ALBUM + ALBUM2 + SEP + YEAR2);
    }
}
