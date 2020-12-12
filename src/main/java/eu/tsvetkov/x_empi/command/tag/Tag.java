package eu.tsvetkov.x_empi.command.tag;

import eu.tsvetkov.x_empi.error.CommandException;
import eu.tsvetkov.empi.mp3.Mp3Tag;

import java.util.Map;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Tag extends BaseTag {

    private final Mp3Tag mp3Tag;
    private final String regex;
    private final String replacement;

    public Tag(String metaRegex) {
        String[] tagValue = metaRegex.split(":");
        String[] regexReplacement = tagValue[1].split("=");
        mp3Tag = Mp3Tag.of(tagValue[0]);
        regex = (regexReplacement.length > 1 ? regexReplacement[0] : "^.*$");
        replacement = (regexReplacement.length > 1 ? regexReplacement[1] : regexReplacement[0]);
    }

    @Override
    protected boolean tagAffected(Mp3Tag tag, Map<Mp3Tag, String> tags) {
        return mp3Tag.equals(tag);
    }

    @Override
    protected String transform(String tagValue) throws CommandException {
        return (tagValue != null ? tagValue.replaceAll(regex, replacement) : replacement);
    }
}
