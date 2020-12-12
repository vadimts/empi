package eu.tsvetkov.empi.x_empi.command.tag;

import eu.tsvetkov.empi.x_empi.error.CommandException;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;
import net.sf.junidecode.Junidecode;

import java.util.Map;

import static eu.tsvetkov.empi.mp3.Mp3Tag.TAGS_SORTS;
import static eu.tsvetkov.empi.util.Util.isBlank;
import static java.lang.Character.UnicodeBlock;
import static java.lang.Character.UnicodeBlock.BASIC_LATIN;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TransliterateSortTags extends BaseTag {

//    public static final String RUSSIAN = "\\b[АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя]+\\b";
//    public static final String JAPANESE = "\\b[ピンキラダブルデラックス七色のしあわせ青空にとび出せ]+\\b";
    public static final int MIN_WORD_LENGTH = 4;

    @Override
    protected boolean tagAffected(Mp3Tag tag, Map<Mp3Tag, String> tags) {
        return TAGS_SORTS.containsKey(tag) && willTransliterate(tags.get(tag));
    }

    @Override
    protected String transform(String tagValue) {
        return Junidecode.unidecode(cleanup(tagValue));
    }

    @Override
    protected void transformTag(Mp3Tag tag, TagMap tagMap) throws CommandException {
        tagMap.getNewTags().put(TAGS_SORTS.get(tag), transform(tagMap.getOldTags().get(tag)));
    }

    private String cleanup(String s) {
        return s.replaceAll("е", "э")
                .replaceAll("Е", "Э")
                .replaceAll("ю", "ыу")
                .replaceAll("Ю", "ЫУ")
                .replaceAll("я", "ыа")
                .replaceAll("Я", "ЫА");
    }

    static boolean willTransliterate(String s) {
        if(isBlank(s)) {
            return false;
        }
        char[] chars = s.toCharArray();
        boolean latin = true;
        for (int i = 0; i < Math.min(chars.length, MIN_WORD_LENGTH); i ++) {
            latin &= BASIC_LATIN.equals(UnicodeBlock.of(chars[i]));
        }
        return !latin;
    }
}
