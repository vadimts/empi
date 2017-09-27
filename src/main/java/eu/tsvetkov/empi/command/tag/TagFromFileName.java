package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.command.move.RenameRegex;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.CommandNotAppliedException;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagFromFileName extends BaseTag {

    private Path sourcePath;

    @Override
    public TagMap run(Path sourcePath) throws CommandException {
        return super.run(this.sourcePath = sourcePath);
    }

    @Override
    protected TagMap transformTags(TagMap tagMap) throws CommandException {
        Matcher matcher = RenameRegex.TRACKNO_ARTIST_TITLE.matcher(getFileName());
        if(!matcher.matches()) {
            throw new CommandNotAppliedException("Could not read tags from file name '" + getFileName() + "'");
        }
        tagMap.getNewTags().put(Mp3Tag.TRACK_NO, matcher.group(1));
        tagMap.getNewTags().put(Mp3Tag.ARTIST, matcher.group(2));
        tagMap.getNewTags().put(Mp3Tag.TITLE, matcher.group(3));
        return tagMap;
    }

    protected String getFileName() {
        return sourcePath.getFileName().toString();
    }

    @Override
    protected boolean tagAffected(Mp3Tag tag, Map<Mp3Tag, String> tags) {
        return false;
    }

    @Override
    protected String transform(String tagValue) throws CommandException {
        return null;
    }
}
