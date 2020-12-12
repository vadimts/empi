package eu.tsvetkov.x_empi.command.tag;

import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;
import eu.tsvetkov.x_empi.command.Command;
import eu.tsvetkov.x_empi.error.CommandException;
import eu.tsvetkov.x_empi.error.CommandNotAppliedException;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class BaseTag extends Command<TagMap> {

    protected Mp3File mp3;

    @Override
    public TagMap run(Path sourcePath) throws CommandException {
        TagMap tagMap = getTagMap(sourcePath);
        tagMap = transformTags(tagMap);
        return (dryRun ? tagMap : tag(tagMap));
    }

    protected Mp3File getMp3File(Path sourcePath) {
        return new Mp3File(sourcePath);
    }

    protected TagMap getTagMap(Path sourcePath) {
        mp3 = getMp3File(sourcePath);
        return new TagMap(mp3.getTagMapAll());
    }

    protected TagMap tag(TagMap tagMap) {
        mp3.setTags(tagMap.getNewTags()).save();
        return tagMap;
    }

    protected abstract boolean tagAffected(Mp3Tag tag, Map<Mp3Tag, String> tags);

    protected abstract String transform(String tagValue) throws CommandException;

    protected void transformTag(Mp3Tag tag, TagMap tagMap) throws CommandException {
        tagMap.getNewTags().put(tag, transform(tagMap.getOldTags().get(tag)));
    }

    protected TagMap transformTags(TagMap tagMap) throws CommandException {
        Map<Mp3Tag, String> oldTags = tagMap.getOldTags();
        for (Mp3Tag tag : oldTags.keySet()) {
            if (tagAffected(tag, oldTags)) {
                transformTag(tag, tagMap);
            }
        }
        if (tagMap.getNewTags().isEmpty()) {
            throw new CommandNotAppliedException("No tags changed. " + tagMap);
        }
        return tagMap;
    }
}
