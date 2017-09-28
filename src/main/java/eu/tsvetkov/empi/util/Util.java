package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.FileException;
import eu.tsvetkov.empi.mp3.Mp3File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static eu.tsvetkov.empi.util.ITunes.Track;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Util {

    public static final String SEP = "[ -]";
    public static final String RE_INTERVAL = "[ _-]*?";
    public static final String RE_INTERVAL_NO_BLANKS = "[_-]*?";
    public static final String BRACKET_LEFT = "[([{<]";
    public static final String BRACKET_RIGHT = "[)]}>]";
    public static final String WORD = BRACKET_LEFT + "?" + "((?!-)[^" + SEP + "]+)" + BRACKET_RIGHT + "?";
    public static final String RE_TRACK_NUMBER = BRACKET_LEFT + "?" + "[0-9]+?" + BRACKET_RIGHT + "?" + "\\.?";
    public static final String RE_NUMBER_ARTIST_TITLE = RE_TRACK_NUMBER;

    public static final String SPACE = " ";
    public static final String ELLIPSIS = "‥";
    public static final int ABBR_FILE = 30;
    private static final int PAD_LIMIT = 8192;

    public static String abbr(final String str, final int length, final String suffix) {
        final String s = defaultString(str, "");
        return (s.length() > length ? s.substring(0, length - 1) + suffix : rightPad(s, length, SPACE));
    }

    public static String abbr(final String str, final int length) {
        return abbr(str, length, ELLIPSIS);
    }

    public static String abbrFilename(final Path filePath) {
        return abbrMiddle(filePath.getFileName().toString(), ABBR_FILE, ELLIPSIS);
    }

    public static String abbrMiddle(final String str, final int length, final String suffix) {
        final String s = defaultString(str, "");
        return (s.length() > length ? s.substring(0, length / 2 - 1) + suffix + s.substring(s.length() - length / 2, s.length()) : rightPad(s, length, SPACE));
    }

    public static String abbrMiddle(final String str, final int length) {
        return abbrMiddle(str, length, ELLIPSIS);
    }

    public static String capitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        return String.valueOf(Character.toTitleCase(str.charAt(0))) + str.toLowerCase().substring(1);
    }

    public static String defaultString(String str) {
        return defaultString(str, "");
    }

    public static String defaultString(String str, String defaultStr) {
        return (isBlank(str) ? defaultStr : str);
    }

    public static String determineWordInterval(String str) {
        return defaultString(str, null);
    }

    public static boolean differNotBlank(final CharSequence cs1, final CharSequence cs2) {
        return isNotBlank(cs1) && isNotBlank(cs2) && !equal(cs1, cs2);
    }

    public static boolean equal(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        return !(cs1 == null || cs2 == null) && cs1.equals(cs2);
    }

    public static boolean equalNotBlank(final CharSequence cs1, final CharSequence cs2) {
        return isNotBlank(cs1) && isNotBlank(cs2) && equal(cs1, cs2);
    }

    public static List<String> getList(Object... items) {
        ArrayList<String> list = new ArrayList<>();
        for (Object item : items) {
            if (item == null || (item instanceof String && isBlank((String) item))) {
                continue;
            }
            if (item instanceof Collection) {
                list.addAll((Collection<String>) item);
            } else if (item instanceof String[]) {
                list.addAll(asList((String[]) item));
            } else {
                list.add((String) item);
            }
        }
        return list;
    }

    public static final <T, E extends Exception> Stream<T> getStreamWithoutException(ThrowingStreamMethod<T, E> method, T param) {
        try {
            return method.run(param);
        } catch (Exception e) {
            e.printStackTrace();
            return Stream.<T>empty();
        }
    }

    public static String getString(byte[] bytes) {
        return new String(bytes);
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static String join(Collection<String> objects, String separator) {
        String output = "";
        for (Iterator<String> i = objects.iterator(); i.hasNext(); ) {
            output += i.next() + (i.hasNext() ? separator : "");
        }
        return output;
    }

    public static String join(Collection<String> objects) {
        return join(objects, "");
    }

    public static String join(String[] objects) {
        return join(asList(objects), "");
    }

    public static String join(String[] objects, String separator) {
        return join(asList(objects), separator);
    }

    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static String rightPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isBlank(padStr)) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
//        if (padLen == 1 && pads <= PAD_LIMIT) {
//            return rightPad(str, size, padStr.substring(0, 1));
//        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }

    public static final <T, E extends Exception> T runWithoutException(ThrowingMethod<T, E> method) {
        try {
            return method.run();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> split(final String str, final char separatorChar) {
        return split(str, separatorChar, true);
    }

    public static List<String> split(final String str, final char separatorChar, final boolean preserveAllTokens) {
        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return Collections.EMPTY_LIST;
        }
        final List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list;
    }

    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    public static class File {
        /**
         * Returns a sorted list of MP3 files in the given directory.
         *
         * @param directory directory to search in
         * @return strings containing MP3 file paths
         */
        public static Stream<String> getMp3InDirectory(String directory) throws FileException {
            Path pathDirectory = Paths.get(directory);
            if(!Files.exists(pathDirectory) || !Files.isDirectory(pathDirectory)) {
                throw new FileException("Path '" + directory + "' doesn't exist or is not a directory");
            }
            return getStreamWithoutException(Files::walk, pathDirectory).filter(Mp3File::isMp3File).map(Object::toString).sorted();
        }

    }
}
