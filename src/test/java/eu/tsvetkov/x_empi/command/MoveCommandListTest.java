package eu.tsvetkov.x_empi.command;

import eu.tsvetkov.empi.util.Util;
import eu.tsvetkov.x_empi.command.move.*;
import org.junit.Test;

import static eu.tsvetkov.empi.util.Util.WORD;
import static eu.tsvetkov.x_empi.command.move.Rename.SEP_ARTIST_ALBUM;
import static eu.tsvetkov.x_empi.command.move.RenameRegex.GROUP;

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
