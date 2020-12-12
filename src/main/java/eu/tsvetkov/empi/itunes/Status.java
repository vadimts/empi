package eu.tsvetkov.empi.itunes;

import eu.tsvetkov.empi.util.Str;

public enum Status {

    ADDED_TRACK_FROM_FILE(3),
    ADDED_TRACK_FROM_LIB(2),
    GENERIC_SUCCESS(1),
    NULL(0),
    GENERIC_ERROR(-1),
    EXCEPTION(-2),
    TRACK_NOT_IN_PLAYLIST(-1728);

    private int code;

    Status(int code) {
        this.code = code;
    }

    public static Status of(Integer statusCode) {
        if (statusCode == null) {
            return NULL;
        }
        for (Status value : values()) {
            if (value.code == statusCode) {
                return value;
            }
        }
        return (statusCode > 0 ? GENERIC_SUCCESS : GENERIC_ERROR);
    }

    public static Status of(String statusName) {
        if (statusName == null) {
            return NULL;
        }
        for (Status value : values()) {
            if (value.name().equals(statusName)) {
                return value;
            }
        }
        return NULL;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return Str.of("${1}${2}").with(
            code < 0 ? "🔴" : "🟢",
            this.equals(NULL) ? "" : Str.of(" ${1} (${2})").with(name(), code)
        );
    }
}
