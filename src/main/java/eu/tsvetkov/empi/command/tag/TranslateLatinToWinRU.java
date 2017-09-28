package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.mp3.Mp3Tag;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Pattern;

import static eu.tsvetkov.empi.util.Util.isNotBlank;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TranslateLatinToWinRU extends BaseTag {

    public static final String RUSSIAN_WORD_IN_LATIN = "\\b[ÀÁÂÃÄÅ¨ÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäå¸æçèéêëìíîïðñòóôõö÷øùúûüýþÿ]+\\b";
    public static final Pattern REGEX = Pattern.compile(RUSSIAN_WORD_IN_LATIN);

    @Override
    protected boolean tagAffected(Mp3Tag tag, Map<Mp3Tag, String> tags) {
        return isRussianInLatin(tags.get(tag));
    }

    protected static boolean isRussianInLatin(String value) {
        return isNotBlank(value) && REGEX.matcher(value).find();
    }

    @Override
    protected String transform(String tagValue) throws CommandException {
        try {
            return new String(cleanup(tagValue).getBytes("latin1"), "cp1251");
        } catch (UnsupportedEncodingException e) {
            throw new CommandException("Error translating tag value '" + tagValue + "' from latin1 to cp1251");
        }
    }

    private static String cleanup(String s) {
        return s.replaceAll("[«»]", "\"")
            .replaceAll("‹", "Ð")  // Fix russian "Р".
            .replaceAll("›", "ð")  // Fix russian "р".
            .replaceAll("†", "Ý")  // Fix russian "э".
            .replaceAll("‡", "ý")  // Fix russian "Э".
            .replaceAll("ﬂ", "þ"); // Fix russian "ю".
    }
}
