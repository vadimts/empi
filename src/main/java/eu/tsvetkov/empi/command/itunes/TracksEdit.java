package eu.tsvetkov.empi.command.itunes;

import eu.tsvetkov.empi.util.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TracksEdit {

    EditType type;
    List<Track> before = new ArrayList<>();
    List<Track> success = new ArrayList<>();
    List<Track> error = new ArrayList<>();
//    Map<String, Object> params = new HashMap<>();

    public TracksEdit(EditType type) {
        this.type = type;
    }

    public EditType getType() {
        return type;
    }

    public List<Track> getBefore() {
        return before;
    }

    public List<Track> getSuccess() {
        return success;
    }

    public List<Track> getError() {
        return error;
    }

    public int sizeBefore() {
        return before.size();
    }

    public int sizeAfter() {
        switch (type) {
            case DELETE:
                return before.size() - success.size();
            default:
                return success.size();
        }
    }

    public int sizeSuccess() {
        return success.size();
    }

    public int sizeError() {
        return error.size();
    }

    public enum EditType {
        ADD, DELETE, NOOP
    }
}
