package eu.tsvetkov.empi.mp3;

import java.util.LinkedHashMap;
import java.util.Map;

import static eu.tsvetkov.empi.mp3.Mp3Tag.*;
import static eu.tsvetkov.empi.util.Util.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagMap {
    public static final int TAG_LENGTH_MAX = 17;
    public static final int TAG_LENGTH_LONG = 8;
    public static final int TAG_LENGTH_MID = 4;
    public static final int TAG_LENGTH_SHORT = 3;
    private Map<Mp3Tag, String> oldTags = new LinkedHashMap<>();
    private Map<Mp3Tag, String> newTags = new LinkedHashMap<>();

    public TagMap(Map<Mp3Tag, String> tags) {
        this.oldTags.putAll(tags);
    }

    public Map<Mp3Tag, String> getOldTags() {
        return oldTags;
    }

    public Map<Mp3Tag, String> getNewTags() {
        return newTags;
    }

    public void mergeNewTags() {
        oldTags.putAll(newTags);
        newTags = new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        String resultOld = "";
        String resultNew = "";
        for (Mp3Tag tag : oldTags.keySet()) {
            String oldTag = normalizeTag(oldTags.get(tag));
            String icon = tag.getIcon();
            if (newTags.containsKey(tag)) {
                resultNew += (isNotBlank(resultNew) ? " " : "") + icon + " " + oldTag + " >> " + newTags.get(tag);
            } else {
                String tagValue;
                switch (tag) {
                    case TITLE_SORT:
                    case ARTIST_SORT:
                    case ALBUM_SORT:
                    case ALBUM_ARTIST_SORT:
                    case COMPOSER_SORT:
                    case TRACK_TOTAL:
                    case DISC_TOTAL:
                        tagValue = "";
                        break;
                    case TRACK_NO:
                        String trackTotal = normalizeTag(oldTags.get(TRACK_TOTAL));
                        tagValue = (isNotBlank(oldTag) ? icon + " " + abbr(oldTag, TAG_LENGTH_SHORT) + (isNotBlank(trackTotal) ? "/" + abbr(trackTotal, TAG_LENGTH_SHORT) : "") : "");
                        break;
                    case DISC_NO:
                        String discTotal = normalizeTag(oldTags.get(DISC_TOTAL));
                        tagValue = (isNotBlank(oldTag) ? icon + " " + abbr(oldTag, TAG_LENGTH_SHORT)  + (isNotBlank(discTotal) ? "/" + abbr(discTotal, TAG_LENGTH_SHORT) : "") : "");
                        break;
                    case YEAR:
                        tagValue = icon + " " + abbr(oldTag, TAG_LENGTH_MID);
                        break;
                    case RATING:
                        tagValue = (isNotBlank(oldTag) ? Emoji.HEART : Emoji.HEART_EMPTY);
                        break;
                    case COMPILATION:
                        tagValue = (isNotBlank(oldTag) ? Emoji.EXCLAMATION : Emoji.EXCLAMATION_EMPTY);
                        break;
                    default:
                        String sortTagValue = normalizeTag(oldTags.get(TAGS_SORTS.get(tag)));
                        tagValue = icon + " " + (isNotBlank(sortTagValue)
                            ? abbr(oldTag, TAG_LENGTH_LONG) + Emoji.SORT + " " + abbr(sortTagValue, TAG_LENGTH_LONG)
                            : abbr(oldTag, (isNotBlank(oldTag) ? TAG_LENGTH_MAX : TAG_LENGTH_MID)));
                }
                resultOld += (isNotBlank(resultOld) && isNotBlank(tagValue) ? " " : "") + tagValue;
            }
        }
        return resultOld + (isNotBlank(resultNew) ? "\nChange: " + resultNew : "");
    }

    private String normalizeTag(String tag) {
        return (isNotBlank(tag) && !"null".equals(tag) ? tag : "");
    }
}
