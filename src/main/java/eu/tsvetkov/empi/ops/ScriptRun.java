package eu.tsvetkov.empi.ops;

import eu.tsvetkov.empi.itunes.AppleScript;
import eu.tsvetkov.empi.itunes.Script;
import eu.tsvetkov.empi.itunes.Status;
import eu.tsvetkov.empi.util.SLogger;
import eu.tsvetkov.empi.util.Str;
import eu.tsvetkov.empi.util.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static eu.tsvetkov.empi.itunes.Status.*;
import static eu.tsvetkov.empi.util.Util.joinLinesPrefix;
import static java.util.stream.Collectors.joining;

public class ScriptRun {

    private static SLogger log = new SLogger();

    ExecutorService executor;
    String output;
    Script script;
    long startTime;
    Status status;
    long stopTime;
    private Process process;

    ScriptRun(Script script) {
        this.script = script;
        this.startTime = new Date().getTime();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List<String> getOutputLines() {
        return Util.getLines(output);
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public boolean isAsync() {
        return (executor != null);
    }

    public boolean isError() {
        return status.getCode() < 0;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.stopTime = new Date().getTime();
    }

    public void setStatusCode(String codeStr) {
        this.status = Status.of(Integer.valueOf(codeStr));
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
        setStatus(GENERIC_SUCCESS);
    }

    public List<String> toList() {
        return AppleScript.extractApplescriptList(output);
    }

    @Override
    public String toString() {
        return toString(0, 1);
    }

    public String toString(int scriptIndex, int scriptCount) {
        boolean singleRun = (scriptIndex == 0);
        String prefix = "  ";
        return Str.of("APPLESCRIPT `${6}` ${1} ${5}ms: ${4}", "${2}", prefix + "OUTPUT", "${3}").with(
            (singleRun ? "" : Str.of(" ${1} of ${2}").with(scriptIndex + 1, scriptCount)),
            joinLinesPrefix("  " + prefix, (Object[]) script.getScript().split("\n")),
            "  " + prefix + output,
            status,
            stopTime - startTime,
            script.getName()
        );
    }

    private static String getOutputFromStream(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(joining("\n"));
    }

    private static String getRunError(Process scriptRun) {
        return getOutputFromStream(scriptRun.getErrorStream());
    }

    private static String getRunOutput(Process scriptRun) {
        return getOutputFromStream(scriptRun.getInputStream());
    }

    ScriptRun waitForProcess() {
        try {
            log.debug(Str.of("Running AppleScript ${1}").with(script.getName()));
            setProcess(MusicOps.getExecProcessBuilder(script.getScript()).start());
            process.waitFor();
            boolean ok = process.exitValue() == 0;
            setOutput(ok ? getRunOutput(process) : getRunError(process));
            Status status = AppleScript.getStatus(getOutput());
            setStatus(status == NULL ? (ok ? GENERIC_SUCCESS : GENERIC_ERROR) : status);
        } catch (Exception e) {
            setOutput(e.getMessage());
            setStatus(Status.EXCEPTION);
        }
        return this;
    }
}
