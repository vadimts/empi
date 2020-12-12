package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.x_empi.script.BaseScript;

import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Util {

    public static final int ABBR_FILE = 30;
    public static final String BRACKET_LEFT = "[([{<]";
    public static final String BRACKET_RIGHT = "[)]}>]";
    public static final Comparator<Object> CASE_INSENSITIVE_COMPARATOR = Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER);
    public static final String ELLIPSIS = "‥";
    public static final String RE_INTERVAL = "[ _-]*?";
    public static final String RE_INTERVAL_NO_BLANKS = "[_-]*?";
    public static final String RE_TRACK_NUMBER = BRACKET_LEFT + "?" + "[0-9]+?" + BRACKET_RIGHT + "?" + "\\.?";
    public static final String RE_NUMBER_ARTIST_TITLE = RE_TRACK_NUMBER;
    public static final String SEP = "[ -]";
    public static final String SPACE = " ";
    public static final String WORD = BRACKET_LEFT + "?" + "((?!-)[^" + SEP + "]+)" + BRACKET_RIGHT + "?";
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

    public static <T> List<T> defaultList(T[] list, List<T> defaultList) {
        return (isNotEmpty(list) ? Arrays.asList(list) : defaultList);
    }

    public static String defaultNonNullString(String str, String defaultStr) {
        return (isBlank(str) || str.equals("null") ? defaultStr : str);
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

    public static String escape(String in) {
        String out = "";
        for (char c : in.toCharArray()) {
            if (c >= 128)
                out += "\\u" + String.format("%04X", (int) c);
            else
                out += c;
        }
        return out;
    }

    @SafeVarargs
    public static <T> T findFirst(List<T> list, Predicate<T>... predicates) {
        try {
            return list.stream().filter(item -> {
                for (int i = 0; i < predicates.length; i++) {
                    if (!predicates[i].test(item)) {
                        return false;
                    }
                }
                return true;
            }).findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static <T> T first(List<T> list) {
        return (list == null || list.isEmpty() ? null : list.get(0));
    }

    public static <T> T firstNonNull(T... objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] != null) return objects[i];
        }
        return null;
    }

    public static String[] getArray(Object... items) {
        return getList(items).parallelStream().toArray(String[]::new);
    }

    public static String getCurrentMethodName(int stackLevel) {
        return StackWalker.getInstance().walk(frames -> frames.skip(stackLevel).findFirst().map(StackWalker.StackFrame::getMethodName)).get();
    }

    public static String getCurrentMethodName(Class methodDeclaringClass) {
        return StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(frames -> frames.filter(x -> x.getDeclaringClass().equals(methodDeclaringClass) && !x.getMethodName().equals("<init>")).findFirst().map(StackWalker.StackFrame::getMethodName)).get();
    }

    public static String getCurrentMethodName() {
        return getCurrentMethodName(2);
    }

    public static <T> Class<T> getGenericType(Object superclassInstance) {
        return (Class<T>) ((ParameterizedType) superclassInstance.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public static List<String> getLines(Object o) {
        return asList(String.valueOf(o).split("\n"));
    }

    public static List<String> getList(Object... items) {
        List<String> list = new ArrayList<>();
        if (items != null) {
            for (Object item : items) {
                if (item == null || (item instanceof String && isBlank((String) item))) {
                    continue;
                }
                if (item instanceof Collection) {
                    ((Collection<Object>) item).parallelStream().filter(x -> isNotBlank(x.toString())).map(Object::toString).forEachOrdered(list::add);
                } else if (item instanceof Object[]) {
                    getList((Object[]) item).forEach(list::add);
                } else if (item instanceof BaseScript) {
                    list.addAll(((BaseScript) item).getScript());
                } else {
                    list.add(item.toString());
                }
            }
        }
        return list;
    }

    public static Map<String, List<String>> getMatchGroupLists(Pattern regex, List<String> groups, String str) {
        Matcher matcher = regex.matcher(str);
        Map<String, List<String>> groupMatches = new HashMap<>();
        groups.forEach(group -> groupMatches.put(group, new ArrayList<String>()));
        while (matcher.find()) {
            groups.forEach(group -> {
                groupMatches.get(group).addAll(asList(matcher.group(group).split("\n")).stream().filter(s -> !isBlank(s)).collect(toList()));
            });
        }
        return groupMatches;
    }

    public static <T> List<T> getMatches(List<T> list, Predicate<T> predicate) {
        return getMatches(list.stream(), predicate);
    }

    public static <T> List<T> getMatches(Stream<T> stream, Predicate<T> predicate) {
        return stream.filter(predicate).sorted().collect(toList());
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

    public static boolean isEmpty(final Object... array) {
        return (array == null || array.length == 0);
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isNotEmpty(final Collection collection) {
        return (collection != null && !collection.isEmpty());
    }

    public static boolean isNotEmpty(final Object... array) {
        return !isEmpty(array);
    }

    public static String join(Collection<?> objects, String separator) {
        return objects.stream().map(Objects::toString).filter(Util::isNotBlank).collect(joining(separator));
    }

    public static String join(Collection<?> objects, String alter, String separator) {
        return join(objects.parallelStream().map(x -> Str.of(alter).with(x)), separator);
    }

    public static String join(Stream<?> objects, String separator) {
        return objects.map(Object::toString).collect(joining(separator));
    }

    public static String join(Collection<String> objects) {
        return join(objects, "");
    }

    public static String join(String[] objects) {
        return join(asList(objects), "");
    }

    public static String join(Object[] objects, String separator) {
        return join(asList(objects), separator);
    }

    public static String joinLines(Stream<String> stream) {
        return stream.collect(Collectors.joining("\n"));
    }

    public static String joinLines(Object... objects) {
        return join(getList(objects), "\n");
    }

    public static String joinLinesPrefix(String prefix, Object... objects) {
        return joinLines(getList(objects).stream().map(x -> prefix + x).collect(toList()));
    }

    public static String joinLinesPrefix(Collection<?> lines, String prefix) {
        return joinLines(lines.stream().map(x -> prefix + x).collect(toList()));
    }

    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static String lines(Object... objects) {
        return join(getList(objects), "\n");
    }

    public static int min(int[] wordCount, int i) {
        return wordCount.length > 0 ? wordCount[0] : i;
    }

    public static <T> T newGenericTypeInstance(Object superclassInstance) throws Exception {
        return (T) getGenericType(superclassInstance).newInstance();
    }

    public static <T> List<T> nonNullList(T[] list) {
        return defaultList(list, emptyList());
    }

    public static <T, R> List<R> nonNullTransform(T param, Function<T, R>... transforms) {
        return nonNullList(transforms).stream().map(transform -> transform.apply(param)).collect(toList());
    }

    public static void out(Object s, Object... params) {
        System.out.println(MessageFormat.format(s.toString(), params));
    }

    public static String q(String... strings) {
        return quote(strings);
    }

    public static String quote(String... strings) {
        return quote(Arrays.asList(strings));
    }

    public static String quote(Collection<String> strings) {
        return strings.parallelStream().map(x -> "\"" + x + "\"").collect(joining(","));
    }

    public static int rand(int max, int... min) {
        int minimum = min(min, 2);
        return new Random().nextInt(max - minimum) + minimum;
    }

    public static boolean randBool() {
        return (rand(2, 0) == 1);
    }

    public static String randWord(List<String> words) {
        return randWords(words, 1);
    }

    public static String randWords(List<String> words, int... wordCount) {
        return IntStream.range(0, min(wordCount, 1)).mapToObj(x -> words.get(rand(words.size()))).collect(joining(" "));
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

    public static boolean startsWith(final String s, final String start) {
        return isNotBlank(s) && s.startsWith(start);
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

    public static String toShortString(List<String> names, List... lists) {
        String totalItemsName = names.get(0);
        AtomicInteger totalItemsCount = new AtomicInteger();
        List<String> itemNames = names.subList(1, names.size());
        List<List> itemLists = asList(lists);
        assert itemNames.size() == itemLists.size();
        String listsHeader = Util.join(IntStream.range(0, itemNames.size()).mapToObj(i -> {
            int listSize = itemLists.get(i).size();
            totalItemsCount.addAndGet(listSize);
            return Str.of("${1} ${2}").with(
                itemNames.get(i),
                listSize
            );
        }), ", ");
        return Str.of("${1} ${2}: ${3}").with(
            totalItemsCount,
            totalItemsName,
            listsHeader
        );
    }

    public static String toString(List<String> names, List... lists) {
        List<String> itemNames = names.subList(1, names.size());
        List<List> itemLists = asList(lists);
        String listsParts = Util.joinLines(IntStream.range(0, itemNames.size()).mapToObj(i -> Str.of("${1}: ${2}").with(
            itemNames.get(i),
            (itemLists.get(i).isEmpty() ? "none" : "\n" + joinLines(itemLists.get(i)))
        )));
        return Str.of("${1}", "${2}\n").with(
            toShortString(names, lists),
            listsParts
        );
    }

    public static <T, R> List<R> transform(Stream<T> stream, Function<T, R> transform) {
        return stream.map(transform::apply).collect(toList());
    }

    public static <T> T transform(T param, Function<T, T>... transforms) {
        T result = param;
        for (int i = 0; i < transforms.length; i++) {
            result = transforms[i].apply(result);
        }
        return result;
    }

    public static Function<String, String> transformLowerCase() {
        return param -> (param != null ? param.toLowerCase() : param);
    }
}
