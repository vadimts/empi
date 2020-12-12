package eu.tsvetkov.empi.itunes;

import eu.tsvetkov.empi.util.Util;

public class Script {
    private String app;
    private String name;
    private String script;

    public Script(String script, String name) {
        this.script = script;
        this.name = name;
        this.app = "Music";
    }

    public Script(String script) {
        this(script, Util.getCurrentMethodName(AppleScript.class));
    }

    public static Script of(String script) {
        return new Script(script);
    }

    public Script asSystem() {
        this.app = "System Events";
        return this;
    }

    public String getName() {
        return name;
    }

    public String getScript() {
        return AppleScript.wrapAppScript(app, script);
    }
}
