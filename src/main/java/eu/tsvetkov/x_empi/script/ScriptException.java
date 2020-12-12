package eu.tsvetkov.x_empi.script;

import java.util.List;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ScriptException extends Exception {

    private List<String> scriptOutput;

    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, List<String> scriptOutput) {
        super(message);
        this.scriptOutput = scriptOutput;
    }

    public List<String> getScriptOutput() {
        return scriptOutput;
    }
}
