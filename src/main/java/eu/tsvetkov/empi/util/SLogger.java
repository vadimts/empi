package eu.tsvetkov.empi.util;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class SLogger {

    public void debug(Object s) {
        System.out.println(s);
    }

    public void error(Exception e) {
        System.out.println(e);
    }

    public void trace(String s) {
//        System.out.println(s);
    }
}
