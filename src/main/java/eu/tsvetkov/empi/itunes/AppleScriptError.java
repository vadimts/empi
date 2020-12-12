package eu.tsvetkov.empi.itunes;

import eu.tsvetkov.empi.util.Str;

import java.util.HashMap;
import java.util.Map;

public class AppleScriptError extends Throwable {

    public static Map<Integer, Class<? extends AppleScriptError>> ERRORS = new HashMap<>();
    public static AppleScriptError TRACK_NOT_IN_PLAYLIST = new AppleScriptError(-10006);

    static {
        ERRORS.put(-10006, TrackNotInLibrary.class);
    }

    private String message;
    private int number = 0;

    public AppleScriptError() {
    }

    public AppleScriptError(int number) {
        this.number = number;
    }

    public AppleScriptError(String message) {
        this.message = message;
    }

    public AppleScriptError(int number, String message) {
        this.number = number;
        this.message = message;
    }

    public AppleScriptError(Throwable e) {
        this(e.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return Str.of("APPLESCRIPT ERROR_FILE_UNREADABLE${1}${2}").with((number != 0 ? " #" + number : ""), (message != null ? ": " + message : ""));
    }

    public static class TrackNotInLibrary extends AppleScriptError {
        public TrackNotInLibrary() {
            super(-10006);
        }
    }
}
