package eu.tsvetkov.empi.x_empi.command.tag;

import eu.tsvetkov.empi.x_empi.error.CommandException;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;

import java.util.Map;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagInfo extends BaseTag {

    public TagInfo() {
        dryRun = true;
    }

    @Override
    protected boolean tagAffected(Mp3Tag tag, Map<Mp3Tag, String> tags) {
        return true;
    }

    @Override
    protected String transform(String tagValue) throws CommandException {
        return null;
    }

    @Override
    protected TagMap transformTags(TagMap tagMap) throws CommandException {
        return tagMap;
    }
}
