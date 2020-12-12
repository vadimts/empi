package eu.tsvetkov.empi.util;

import static eu.tsvetkov.empi.util.Str.Ansi.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class SLogger {

    public void debug(Object s) {
        System.out.println(s);
    }

    public void debug(String s, Object... params) {
        debug(Str.str(s, params));
    }

    public void debugBlue(Object s) {
        debug(color(BLUE, s));
    }

    public void debugBlue(String s, Object... params) {
        debugBlue(Str.str(s, params));
    }

    public void debugGreen(Object s) {
        debug(color(GREEN, s));
    }

    public void debugGreen(String s, Object... params) {
        debugGreen(Str.str(s, params));
    }

    public void debugRed(Object s) {
        debug(color(RED, s));
    }

    public void error(Exception e) {
        e.printStackTrace();
    }

    public void error(Object s) {
        System.out.println((char) 27 + "[31m" + s);
    }

    public void error(Object s, Exception e) {
        error(s);
        error(e);
    }

    public void info(Object s) {
        System.out.println(s);
    }

    public void log(Object s) {
        info(s);
    }

    public void trace(String s) {
//        System.out.println(s);
    }
}
