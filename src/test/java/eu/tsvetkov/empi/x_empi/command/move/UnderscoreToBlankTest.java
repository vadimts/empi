package eu.tsvetkov.empi.command.move;

import junit.framework.Assert;
import org.junit.Test;

import static eu.tsvetkov.empi.command.move.UnderscoreToBlank.isUnderscoreFormat;
import static junit.framework.Assert.assertTrue;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class UnderscoreToBlankTest {

    @Test
    public void isUnderscoreFormatTest() {
        assertTrue(isUnderscoreFormat("Herbaliser_1995-06_01_Repetitive Loop.mp3"));
    }
}
