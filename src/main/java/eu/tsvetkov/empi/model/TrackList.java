package eu.tsvetkov.empi.empi2;

import eu.tsvetkov.empi.util.Util;

import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;

public class TrackList {

    public static final List<String> LIST_NAMES = asList("TRACKS", "🟢 FOUND", "🔴 MISSING");
    List<Path> found;
    List<Path> missing;

    public List<Path> getFound() {
        return found;
    }

    public void setFound(List<Path> found) {
        this.found = found;
    }

    public List<Path> getMissing() {
        return missing;
    }

    public void setMissing(List<Path> missing) {
        this.missing = missing;
    }

    public int getTotalCount() {
        return found.size() + missing.size();
    }

    public String toShortString() {
        return Util.toShortString(LIST_NAMES, found, missing);
    }

    @Override
    public String toString() {
        return Util.toString(LIST_NAMES, found, missing);
    }
}
