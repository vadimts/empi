package eu.tsvetkov.empi.mp3;

import eu.tsvetkov.empi.util.Util;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public enum Mp3Tag {

    TITLE("ti", emoji(0x1F3B5)), TITLE_SORT("tis", emoji(0x1F3B5)+ Emoji.SORT),
    ARTIST("ar", emoji(0x1F603)), ARTIST_SORT("ars", emoji(0x1F603)+ Emoji.SORT),
    ALBUM("al", emoji(0x1F4C0)), ALBUM_SORT("als", emoji(0x1F4C0)+ Emoji.SORT),
    ALBUM_ARTIST("aa", emoji(0x1F60E)), ALBUM_ARTIST_SORT("aas", emoji(0x1F60E)+ Emoji.SORT),
    COMPOSER("co", emoji(0x1F913)), COMPOSER_SORT("cos", emoji(0x1F913)+ Emoji.SORT),
    GROUPING("gr", emoji(0x1F5C2)),
//    GENRE("ge", emoji(0x1F3B8)),
    GENRE("ge", emoji(0x1F3B9)),
    YEAR("ye", emoji(0x1F4C5)),
    TRACK_NO("tr", "#️⃣"), TRACK_TOTAL("tt", "#️⃣" + "#️⃣"),
    DISC_NO("di", "*️⃣"), DISC_TOTAL("dt", "*️⃣" + "*️⃣"),
    COMPILATION("co", emoji(0x1F300)),
    RATING("ra", "❤️"),
//    LOVED("lo", "❤️"),
    COMMENT("cm", emoji(0x1F4AC));

    public static final List<Mp3Tag> TAGS_ALL = asList(values());

    public static final List<Mp3Tag> TAGS_ID3V1 = asList(TITLE, ARTIST, ALBUM, GENRE, YEAR, TRACK_NO, COMMENT);
    public static final List<Mp3Tag> TAGS_SORT = asList(TITLE_SORT, ARTIST_SORT, ALBUM_SORT, ALBUM_ARTIST_SORT, COMPOSER_SORT);
    public static final List<Mp3Tag> TAGS_SORTABLE = asList(TITLE, ARTIST, ALBUM, ALBUM_ARTIST, COMPOSER);
    public static final Map<Mp3Tag, Mp3Tag> TAGS_SORTS = new HashMap<>();
    public static final String CODES_TAGS_ALL = listAllCodesTags();
    static {
        TAGS_SORTS.put(TITLE, TITLE_SORT);
        TAGS_SORTS.put(ARTIST, ARTIST_SORT);
        TAGS_SORTS.put(ALBUM, ALBUM_SORT);
        TAGS_SORTS.put(ALBUM_ARTIST, ALBUM_ARTIST_SORT);
        TAGS_SORTS.put(COMPOSER, COMPOSER_SORT);
    }
    public static String listAllCodesTags() {
        ArrayList<String> codesTags = new ArrayList<>();
        TAGS_ALL.forEach(value -> codesTags.add(value.getIcon() + " " + Util.capitalize(value.name().replace("_", " "))));
        return Util.join(codesTags, ", ");
    }

    public static Mp3Tag of(String code) {
        Mp3Tag mp3Tag = Tags.CODES_TAGS.get(code.toLowerCase());
        if(mp3Tag == null) {
            throw new NoSuchElementException("No Mp3Tag for code '" + code + "'");
        }
        return mp3Tag;
//        return Arrays.stream(values()).filter(e -> e.code.equals(code)).findFirst().get();
    }

    private String code;
    private String icon;

    private Mp3Tag(String code, String icon) {
        this.code = code;
        this.icon = icon;
        // Reference this mp3 tag by code in the static map.
        Tags.CODES_TAGS.put(code.toLowerCase(), this);
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Checks if this tag is contained in the provided list of tags.
     *
     * @param tags list of tags to check against
     * @return true if this tag is in the provided list, false otherwise
     */
    public boolean is(Mp3Tag... tags) {
        for (Mp3Tag tag : tags) {
           if(this.equals(tag)) {
               return true;
           }
        }
        return false;
    }

    public static class Tags {
        public static final Map<String, Mp3Tag> CODES_TAGS = new HashMap<>();
    }

    public static class Emoji {

        public static final String DIR = emoji(0x1F4C1);
        public static final String SORT = emoji(0x1F511);
        public static final String EXCLAMATION = "❗️";
        public static final String EXCLAMATION_EMPTY = "❕";
        public static final String HEART = "❤️";
        public static final String HEART_EMPTY = "♡";
    }

    private static String emoji(int icon) {
        return new String(new int[] {icon}, 0, 1);
    }
}
