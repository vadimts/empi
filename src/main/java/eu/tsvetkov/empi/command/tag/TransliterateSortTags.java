package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.CommandNotAppliedException;
import eu.tsvetkov.empi.error.Mp3Exception;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Pattern;

import static eu.tsvetkov.empi.util.Util.equal;
import static eu.tsvetkov.empi.util.Util.join;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TransliterateSortTags extends Tag {

    public static final String RUSSIAN_WORD_IN_LATIN = "\\b[ÀÁÂÃÄÅ¨ÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäå¸æçèéêëìíîïðñòóôõö÷øùúûüýþÿ]+\\b";
    public static final Pattern REGEX = Pattern.compile(RUSSIAN_WORD_IN_LATIN);

    @Override
    protected void tag(TagMap tagMap) throws Mp3Exception, CommandException {
        Map<Mp3Tag, String> oldTags = tagMap.getOldTags();
        if(!REGEX.matcher(join(oldTags.values())).find()) {
            throw new CommandNotAppliedException("No change in tags. " + tagMap);
        }

        for (Mp3Tag tag : oldTags.keySet()) {
            String oldValue = oldTags.get(tag);
            String newValue = translate(oldValue);
            if(!equal(oldValue, newValue)) {
                tagMap.getNewTags().put(tag, newValue);
            }
        }
    }

    protected String translate(String value) throws Mp3Exception {
        if (value == null || !REGEX.matcher(value).find()) {
            return value;
        }
        try {
            return cleanup(new String(value.getBytes("latin1"), "cp1251"));
        } catch (UnsupportedEncodingException e) {
            throw new Mp3Exception("Error translating tag value '" + value + "' from latin1 to cp1251");
        }
    }

    private static String cleanup(String s) {
        return s.replaceAll("[«»]", "\"");
    }
}
