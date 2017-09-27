package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.command.CommandList;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.Mp3Exception;
import eu.tsvetkov.empi.mp3.TagMap;

import java.nio.file.Path;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagCommandList extends CommandList<BaseTag> {

    public TagCommandList(BaseTag... commands) {
        super(commands);
    }

    @Override
    public TagMap run(Path sourcePath) throws CommandException {
        try {
            TagMap tagMap = commands.get(0).getTagMap(sourcePath);
            return commands.get(0).tag(transformTags(tagMap));
        } catch (Mp3Exception e) {
            throw getCommandException(e);
        }
    }

    protected TagMap transformTags(TagMap tagMap) throws CommandException {
        TagMap oldTagMap = new TagMap(tagMap.getOldTags());
        for (BaseTag command : commands) {
            tagMap = command.transformTags(tagMap);
            oldTagMap.getNewTags().putAll(tagMap.getNewTags());
            tagMap.mergeNewTags();
        }
        return oldTagMap;
    }
}
