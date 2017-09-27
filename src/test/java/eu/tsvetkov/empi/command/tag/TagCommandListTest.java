package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.BaseTest;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.Mp3Exception;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.mp3.TagMap;
import org.junit.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static eu.tsvetkov.empi.mp3.Mp3Tag.*;
import static junit.framework.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagCommandListTest extends BaseTest {

    public static final Map<Mp3Tag, String> RU_IN_LATIN = new HashMap<>();

    public static final String TITLE_RU_LAT = "Âèò›àæè";
    public static final String TITLE_RU = "Витражи";
    public static final String TITLE_RU_TRSL = "Vitrazhi";

    public static final String ARTIST_RU_LAT = "Êàìå›íûé àíñàìáëü «‹îêîêî»";
    public static final String ARTIST_RU = "Камерный ансамбль \"Рококо\"";
    public static final String ARTIST_RU_TRSL = "Kamernyi ansambl' \"Rokoko\"";

    public static final String ALBUM_EN = "Soviet Light Music vol.2";

    static {
        RU_IN_LATIN.put(TITLE, TITLE_RU_LAT);
        RU_IN_LATIN.put(ARTIST, ARTIST_RU_LAT);
        RU_IN_LATIN.put(ALBUM, ALBUM_EN);
    }
    private TagMap tagMap;

    @Test
    public void latinToWinRUAndTransliterate() throws CommandException {
        tagMap = new TagMap(RU_IN_LATIN);
        BaseTag latinToRu = new TranslateLatinToWinRU() {
            @Override
            protected TagMap getTagMap(Path sourcePath) throws Mp3Exception {
                return tagMap;
            }
        };
        BaseTag transliterate = new TransliterateSortTags() {
            @Override
            protected TagMap getTagMap(Path sourcePath) throws Mp3Exception {
                return tagMap;
            }
        };
        TagCommandList commands = new TagCommandList(latinToRu, transliterate);
        tagMap = commands.transformTags(tagMap);

        Map<Mp3Tag, String> oldTags = tagMap.getOldTags();
        assertEquals(3, oldTags.size());
        assertEquals(TITLE_RU_LAT, oldTags.get(TITLE));
        assertEquals(ARTIST_RU_LAT, oldTags.get(ARTIST));
        assertEquals(ALBUM_EN, oldTags.get(ALBUM));
        Map<Mp3Tag, String> newTags = tagMap.getNewTags();
        assertEquals(4, newTags.size());
        assertEquals(TITLE_RU, newTags.get(TITLE));
        assertEquals(TITLE_RU_TRSL, newTags.get(TITLE_SORT));
        assertEquals(ARTIST_RU, newTags.get(ARTIST));
        assertEquals(ARTIST_RU_TRSL, newTags.get(ARTIST_SORT));
    }
}
