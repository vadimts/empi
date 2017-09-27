package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.util.Util;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import static eu.tsvetkov.empi.mp3.Mp3Tag.ARTIST;
import static eu.tsvetkov.empi.mp3.Mp3Tag.TITLE;
import static eu.tsvetkov.empi.util.Util.RE_INTERVAL;
import static eu.tsvetkov.empi.util.Util.RE_TRACK_NUMBER;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagInfo extends Tag {

    @Override
    protected boolean tagAffected(Mp3Tag tag, Map<Mp3Tag, String> tags) {
        return ((ARTIST.equals(tag) || TITLE.equals(tag)) && Util.isBlank(tags.get(tag)));
    }

    @Override
    protected String transform(String tagValue) throws CommandException {
        Path filePath = mp3.getFilePath();
        String fileName = filePath.getFileName().toString();
        if(Pattern.compile("(" + RE_TRACK_NUMBER + ")?" + RE_INTERVAL).matcher(fileName).matches()) {

        }
        return null;
    }
}
