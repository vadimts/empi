package eu.tsvetkov.empi.util;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;

public class Str {

    public static final Pattern RE_METHOD_CALL = Pattern.compile("\\$\\{(\\w+)\\([^{}]*\\)\\}", DOTALL | MULTILINE);
    public static final Pattern RE_METHOD_PARAMS = Pattern.compile("\\$\\{\\w+\\(\n?(.+)\n?\\)\\}", DOTALL | MULTILINE);
    public static final Map<String, Pattern> regexPatternCache = new HashMap<>();
    private Class<?>[] classes;
    private Map<String, Method> methods = new HashMap<>();
    private String str;

    public Str(Object... str) {
        this.str = Util.lines(str).replaceAll("`", "\"");
    }

    public static String cut(Object o) {
        if (o instanceof Path) {
            String s = o.toString();
            return (s.length() > 80 ? s.substring(0, 30) + " ... " + s.substring(s.length() - 50, s.length()) : s);
        } else {
            return o.toString();
        }
    }

    public static String esc(String s) {
        return s.replaceAll("\"", "\\\"");
    }

    public static Pattern getCachedRegexPattern(String regex) {
        if (!regexPatternCache.containsKey(regex)) {
            regexPatternCache.put(regex, Pattern.compile(regex));
        }
        return regexPatternCache.get(regex);
    }

    public static boolean isLike(Object o, String regex) {
        return getCachedRegexPattern(regex).matcher(o.toString().toLowerCase()).matches();
    }

    public static boolean isNullString(String s) {
        return s == null || s.equals("") || s.equals("null");
    }

    public static List<String> methodCall(String str) {
        ArrayList<String> methodAndParams = new ArrayList<>();
        Matcher matcher = RE_METHOD_CALL.matcher(str);
        if (matcher.find()) {
            String method = matcher.group(1);
            methodAndParams.add(method);
        }
        matcher = RE_METHOD_PARAMS.matcher(str);
        if (matcher.find()) {
            String params = matcher.group(1);
            methodAndParams.addAll(asList(params.split("[,\n]")));
        }
        return methodAndParams;
    }

    public static Str of(Object... str) {
        return new Str(str);
    }

    @SafeVarargs
    public static <T, R extends Comparable> List<T> sort(List<T> list, String pattern, Function<T, R>... priorComparisons) {
        list.sort((o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 != null && o2 == null) {
                return -1;
            } else if (o1 == null && o2 != null) {
                return 1;
            } else {
                String s1 = o1.toString().toLowerCase();
                String s2 = o2.toString().toLowerCase();
                Matcher matcher1 = getCachedRegexPattern(pattern).matcher(s1);
                Matcher matcher2 = getCachedRegexPattern(pattern).matcher(s2);
                if (!matcher1.matches() && !matcher2.matches()) {
                    return s1.compareTo(s2);
                } else if (matcher1.matches() && !matcher2.matches()) {
                    return -1;
                } else if (!matcher1.matches() && matcher2.matches()) {
                    return 1;
                } else {
                    for (int i = 0; i < priorComparisons.length; i++) {
                        Function<T, R> comparison = priorComparisons[i];
                        R comparisonParam1 = comparison.apply(o1);
                        R comparisonParam2 = comparison.apply(o2);
                        int comparisonResult = Objects.compare(comparisonParam1, comparisonParam2, Comparator.naturalOrder());
                        if (comparisonResult != 0) {
                            return comparisonResult;
                        }
                    }
                    String artworkName1 = matcher1.group(1);
                    String artworkName2 = matcher2.group(1);
                    return artworkName1.compareTo(artworkName2);
                }
            }
        });

        return list;
    }

    public static String str(String s, Object... params) {
        return Str.of(s).with(params);
    }

    public static String toLowerCase(String s) {
        return (s != null ? s.toLowerCase() : s);
    }

    public Str methodsIn(Class<?>... classes) {
        this.classes = classes;
        return this;
    }

    public String toString() {
        return str;
    }

    public String with(Object... params) {
        for (int i = 1; i <= params.length; i++) {
            String paramValue = String.valueOf(params[i - 1]);

            // Not using str.replaceAll("${i}", paramValue) since "$" in paramValue is interpreted as a regex group index:
            // str = str.replaceAll("\\$\\{" + i + "\\}", paramValue);

            boolean suffix = str.endsWith("${" + i + "}");
            str = Arrays.stream(str.split("\\$\\{" + i + "\\}")).collect(Collectors.joining(paramValue));
            str = (suffix ? str + paramValue : str);
        }
        while (RE_METHOD_CALL.matcher(str).find()) {
            Matcher matcher = RE_METHOD_CALL.matcher(str);
            if (matcher.find()) {
                String methodCall = matcher.group(0);
                List<String> methodWithParams = methodCall(methodCall);
                String methodName = methodWithParams.get(0);
                Object[] methodParams = methodWithParams.subList(1, methodWithParams.size()).toArray();
                boolean found = false;
                for (Class c : classes) {
                    Object classInstance = null;
                    try {
                        classInstance = c.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (Method method : c.getMethods()) {
                        if (method.getName().equals(methodName)) {
                            try {
                                str = str.replace(methodCall, String.valueOf(method.invoke(classInstance, methodParams)));
                                found = true;
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (!found) {
                    str = str.replace(methodCall, "METHOD NOT FOUND: " + methodCall);
                }
            }
        }
        return str;
    }

    public enum Ansi {
        END("\u001B[0m"),
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m");

        String code;

        Ansi(String code) {
            this.code = code;
        }

        public static String blue(Object s) { return color(BLUE, s); }

        public static String color(Ansi color, Object s) { return color + s.toString() + END; }

        public static String green(Object s) { return color(GREEN, s); }

        public static String red(Object s) { return color(RED, s); }

        @Override
        public String toString() { return code; }
    }
}
