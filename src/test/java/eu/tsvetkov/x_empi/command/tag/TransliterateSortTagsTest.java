package eu.tsvetkov.x_empi.command.tag;

import eu.tsvetkov.x_empi.BaseTest;
import eu.tsvetkov.x_empi.error.CommandNotAppliedException;
import org.junit.Before;
import org.junit.Test;

import static eu.tsvetkov.x_empi.command.tag.TransliterateSortTags.willTransliterate;
import static org.junit.Assert.*;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TransliterateSortTagsTest extends BaseTest {

    private TransliterateSortTags command;

    @Before
    public void before() {
        command = new TransliterateSortTags();
    }

    @Test
    public void transliterateDE() {
        assertTransform(WORD_DE_ALL_UMLAUTS_TRANSL, WORD_DE_ALL_UMLAUTS);
        assertTransform(WORD_DE_ALL_UMLAUTS_TRANSL.toUpperCase(), WORD_DE_ALL_UMLAUTS.toUpperCase());
    }

    @Test
    public void transliterateJP() {
        assertTransform("pi", "ピ");
        assertTransform("pinkiradaburuderatukusu", "ピンキラダブルデラックス");
    }

    @Test
    public void transliterateRU() {
        assertTransform("I", "И");
        assertTransform("Inna Talanova", "Инна Таланова");
        assertTransform("Prisnis'", "Приснись");
        assertTransform("Kamernyi ansambl' \"Rokoko\"", "Камерный ансамбль \"Рококо\"");
        assertTransform("Vitrazhi", "Витражи");
        assertTransform("Ansambl' elektromuzykal'nykh instrumentov p/u V. Meshcherina", "Ансамбль электромузыкальных инструментов п/у В. Мещерина");
        assertTransform("Osennii motiv", "Осенний мотив");
        assertTransform("El'vira Traforova", "Эльвира Трафорова");
        assertTransform("Ne sud'ba", "Не судьба");
        assertTransform("Kamernyi ansambl' \"Rokoko\"", "Камерный ансамбль \"Рококо\"");
        assertTransform("Razryv-trava", "Разрыв-трава");
        assertTransform("V. Kuprevich i ansambl' \"Melodiya\"", "В. Купревич и ансамбль \"Мелодия\"");
        assertTransform("Eleghiya", "Элегия");
        assertTransform("Vadim Mulerman", "Вадим Мулерман");
        assertTransform("Prisnilos' mne", "Приснилось мне");
        assertTransform("Zinaida Nevskaya", "Зинаида Невская");
        assertTransform("Na reke", "На реке");
        assertTransform("Ansambl' A. Tartakovskogho", "Ансамбль А. Тартаковского");
        assertTransform("Osen'", "Осень");
        assertTransform("Ansambl' p.u V. Ighnat'eva", "Ансамбль п.у В. Игнатьева");
        assertTransform("Noktyurn", "Ноктюрн");
        assertTransform("Emil' Gorovets", "Эмиль Горовец");
        assertTransform("Nu, chto?", "Ну, что?");
        assertTransform("Ansambl' \"Melodiya\"", "Ансамбль \"Мелодия\"");
        assertTransform("Khorosho", "Хорошо");
        assertTransform("Maiya Kristalinskaya", "Майя Кристалинская");
        assertTransform("Tol'ko lyubov' prava", "Только любовь права");
        assertTransform("Aleksandra Pakhmutova, instrumental'nyi ansambl'", "Александра Пахмутова, инструментальный ансамбль");
        assertTransform("Tri topolya na Plyushchikhe", "Три тополя на Плющихе");
        assertTransform("Ansambl' elektromuzykal'nykh instrumentov p/u V. Meshcherina", "Ансамбль электромузыкальных инструментов п/у В. Мещерина");
        assertTransform("Vseghda s toboi", "Всегда с тобой");
        assertTransform("Kamernyi ansambl' \"Rokoko\"", "Камерный ансамбль \"Рококо\"");
        assertTransform("Gavaiskie ghitary", "Гавайские гитары");
        assertTransform("Kapitalina Lazarenko", "Капиталина Лазаренко");
        assertTransform("Moya Gvineya", "Моя Гвинея");
        assertTransform("Larisa Mondrus i Ingho Graf", "Лариса Мондрус и Инго Граф");
        assertTransform("Do svidaniya", "До свидания");
    }

    @Test
    public void transliteration() throws CommandNotAppliedException {
        assertFalse(willTransliterate("S"));
        assertFalse(willTransliterate("Scientist"));
        assertFalse(willTransliterate("Scientist Rids The World Of The Evil Curse Of The Vampires"));
        assertEquals("S", command.transform("S"));
        assertEquals("Scientist", command.transform("Scientist"));
        assertEquals("Scientist Rids The World Of The Evil Curse Of The Vampires", command.transform("Scientist Rids The World Of The Evil Curse Of The Vampires"));
    }

    protected void assertTransform(String expected, String value) {
        assertTrue(willTransliterate(value));
        assertEquals(expected, command.transform(value));
    }
}
