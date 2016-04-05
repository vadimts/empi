package eu.tsvetkov.empi;

import org.junit.Test;

import java.io.IOException;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class EmpiTest {

    @Test
    public void main() throws IOException {
        Empi.main(new String[]{"target/test-classes/mp3", "-cap", "-dry"});
    }
}
