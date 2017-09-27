package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.error.CommandException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ITunesTest {

    @Test
    public void cleanLocation() throws CommandException {
        Assert.assertEquals("/mnt/alpaca/music/cd/Laid Back/_..Keep Smiling + Hole In the Sky/01 Elevator Boy.mp3",
            ITunes.cleanLocation("file:///mnt/alpaca/music/cd/Laid%20Back/_..Keep%20Smiling%20+%20Hole%20In%20the%20Sky/01%20Elevator%20Boy.mp3"));
    }
}
