package eu.tsvetkov.empi.command.itunes;

import eu.tsvetkov.empi.error.CommandException;
import org.junit.Test;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class SyncPlaylistTest {

    @Test
    public void sync() throws CommandException {
        SyncPlaylist command = new SyncPlaylist();
//        command.sync("/mnt/alpaca/music/phone");
        command.sync("/Users/vadim/Music/test");
    }
}
