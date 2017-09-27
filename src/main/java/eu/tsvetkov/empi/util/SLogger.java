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

    public void error(Object s) {
        System.out.println(s);
    }

    public void error(Object s, Exception e) {
        error(s);
        error(e);
    }

    public void trace(String s) {
//        System.out.println(s);
    }
}
