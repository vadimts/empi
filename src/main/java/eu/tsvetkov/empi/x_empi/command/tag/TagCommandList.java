package eu.tsvetkov.empi.x_empi.command.tag;

import eu.tsvetkov.empi.x_empi.command.CommandList;
import eu.tsvetkov.empi.x_empi.error.CommandException;
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
        TagMap tagMap = commands.get(0).getTagMap(sourcePath);
        return commands.get(0).tag(transformTags(tagMap));
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
