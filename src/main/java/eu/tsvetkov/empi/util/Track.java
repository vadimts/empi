package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.error.itunes.ITunesException;
import eu.tsvetkov.empi.itunes.script.BaseScript;

import static eu.tsvetkov.empi.util.Util.isBlank;
import static eu.tsvetkov.empi.util.Util.isEmpty;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Track {

    public static final String ERROR_LOCATION = BaseScript.ERROR_PREFIX + "invalid track location";

    private int id;
    private String path;

    public Track(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public Track(String... attributes) throws ITunesException {
        if (attributes == null || isEmpty(attributes)) {
            throw new ITunesException("Error creating iTunes track from attributes: " + attributes);
        }
        this.id = Integer.parseInt(attributes[0]);
        this.path = (attributes.length == 1 || isBlank(attributes[1]) ? ERROR_LOCATION : attributes[1]);
    }

    public static Track of(String... attributes) throws ITunesException {
        return new Track(attributes);
    }

    public int getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Track " + id + " \"" + path + "\"";
    }

    public static class NewTrack extends Track {

        public NewTrack(String path) {
            super(-1, path);
        }

        @Override
        public String toString() {
            return "New track \"" + getPath() + "\"";
        }
    }
}
