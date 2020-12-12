package eu.tsvetkov.empi.itunes;

import org.junit.Assert;
import org.junit.Test;

public class AppleScriptTest {
    @Test
    public void getError() {
        Assert.assertEquals(Integer.valueOf(-1000), AppleScript.getStatus("this is an applescript error output (-1000)"));
    }
}
