package eu.tsvetkov.empi.dsl;

import eu.tsvetkov.empi.util.Util;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;

public class Str {

    private String str;
    private Class<?>[] classes;
    private Map<String, Method> methods = new HashMap<>();
    public static final Pattern RE_METHOD_CALL = Pattern.compile("\\$\\{(\\w+)\\([^{}]*\\)\\}", DOTALL | MULTILINE);
    public static final Pattern RE_METHOD_PARAMS = Pattern.compile("\\$\\{\\w+\\(\n?(.+)\n?\\)\\}", DOTALL | MULTILINE);

    public Str(Object... str) {
        this.str = Util.lines(str).replaceAll("`", "\"");
    }

    public static Str of(String... str) {
        return new Str(str);
    }

    public Str methodsIn(Class<?>... classes) {
        this.classes = classes;
        return this;
    }

    public String toString() {
        return str;
    }

    public String with(Object... params) {
        for(int i=1; i <= params.length; i ++) {
            String paramValue = String.valueOf(params[i-1]);

            // Not using str.replaceAll("${i}", paramValue) since "$" in paramValue is interpreted as a regex group index:
            // str = str.replaceAll("\\$\\{" + i + "\\}", paramValue);

            str = asList(str.split("\\$\\{" + i + "\\}")).stream().collect(Collectors.joining(paramValue));
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
                if(!found) {
                    str = str.replace(methodCall, "METHOD NOT FOUND: " + methodCall);
                }
            }
        }
        return str;
    }

    static List<String> methodCall(String str) {
        ArrayList<String> methodAndParams = new ArrayList<>();
        Matcher matcher = RE_METHOD_CALL.matcher(str);
        if(matcher.find()) {
            String method = matcher.group(1);
            methodAndParams.add(method);
        }
        matcher = RE_METHOD_PARAMS.matcher(str);
        if(matcher.find()) {
            String params = matcher.group(1);
            methodAndParams.addAll(asList(params.split("[,\n]")));
        }
        return methodAndParams;
    }
}
