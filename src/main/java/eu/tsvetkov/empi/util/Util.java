package eu.tsvetkov.empi.util;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Util {

    public static String defaultString(String str) {
        return defaultString(str, null);
    }

    public static String defaultString(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    public static boolean equalNotBlank(final CharSequence cs1, final CharSequence cs2) {
        return isNotBlank(cs1) && isNotBlank(cs2) && equal(cs1, cs2);
    }

    public static boolean differNotBlank(final CharSequence cs1, final CharSequence cs2) {
        return isNotBlank(cs1) && isNotBlank(cs2) && !equal(cs1, cs2);
    }

    public static boolean equal(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        return cs1.equals(cs2);
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static String getString(byte[] bytes) {
        return new String(bytes);
    }
}
