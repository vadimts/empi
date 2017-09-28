package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.command.move.*;
import eu.tsvetkov.empi.util.Util;
import org.junit.Test;

import static eu.tsvetkov.empi.command.move.Rename.*;
import static eu.tsvetkov.empi.command.move.RenameRegex.GROUP;
import static eu.tsvetkov.empi.util.Util.WORD;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class MoveCommandListTest extends MoveCommandTest {

    @Test
    public void run() throws Exception {
        MoveCommandList command = new MoveCommandList(
            new RenameRegex(WORD + Util.SEP + GROUP, "$2" + Util.SEP + "$1"),
            new RenameArtistFromParentDir(),
            new MoveUp());
//        runCommand(command, getTestFilePath(ARTIST2 + PSEP + YEAR_ALBUM2));
        assertTestPath(ARTIST2 + SEP_ARTIST_ALBUM + ALBUM2 + Util.SEP + YEAR2);
    }
}
