package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.BaseTest;
import eu.tsvetkov.empi.error.CommandException;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static eu.tsvetkov.empi.command.tag.TranslateLatinToWinRU.isRussianInLatin;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class TagFromFileNameTest extends BaseTest {

    private TranslateLatinToWinRU command;

    @Before
    public void before() {
        command = new TranslateLatinToWinRU();
    }

    @Test
    public void javaString() throws UnsupportedEncodingException {
        assertEquals("ÀÁÂÃÄÅ¨ÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäå¸æçèéêëìíîïðñòóôõö÷øùúûüýþÿ",
          new String("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя".getBytes("cp1251"), "latin1"));
        assertEquals("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя",
          new String("ÀÁÂÃÄÅ¨ÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäå¸æçèéêëìíîïðñòóôõö÷øùúûüýþÿ".getBytes("latin1"), "cp1251"));
    }

    @Test
    public void transformRU() throws CommandException {
        assertTransform("Инна Таланова", "Èííà Òàëàíîâà");
        assertTransform("Приснись", "Ï›èñíèñü");
        assertTransform("Камерный ансамбль \"Рококо\"", "Êàìå›íûé àíñàìáëü «‹îêîêî»");
        assertTransform("Витражи", "Âèò›àæè");
        assertTransform("Ансамбль электромузыкальных инструментов п/у В. Мещерина", "Àíñàìáëü ‡ëåêò›îìóçûêàëüíûõ èíñò›óìåíòîâ ï/ó Â. Ìåùå›èíà");
        assertTransform("Осенний мотив", "Îñåííèé ìîòèâ");
        assertTransform("Эльвира Трафорова", "†ëüâè›à Ò›àôî›îâà");
        assertTransform("Не судьба", "Íå ñóäüáà");
        assertTransform("Камерный ансамбль \"Рококо\"", "Êàìå›íûé àíñàìáëü «‹îêîêî»");
        assertTransform("Разрыв-трава", "‹àç›ûâ-ò›àâà");
        assertTransform("В. Купревич и ансамбль \"Мелодия\"", "Â. Êóï›åâè÷ è àíñàìáëü «Ìåëîäèÿ»");
        assertTransform("Элегия", "†ëåãèÿ");
        assertTransform("Вадим Мулерман", "Âàäèì Ìóëå›ìàí");
        assertTransform("Приснилось мне", "Ï›èñíèëîñü ìíå");
        assertTransform("Зинаида Невская", "Çèíàèäà Íåâñêàÿ");
        assertTransform("На реке", "Íà ›åêå");
        assertTransform("Ансамбль А. Тартаковского", "Àíñàìáëü À. Òà›òàêîâñêîãî");
        assertTransform("Осень", "Îñåíü");
        assertTransform("Ансамбль п.у В. Игнатьева", "Àíñàìáëü ï.ó Â. Èãíàòüåâà");
        assertTransform("Ноктюрн", "Íîêòﬂ›í");
        assertTransform("Эмиль Горовец", "†ìèëü Ãî›îâåö");
        assertTransform("Ну, что?", "Íó, ÷òî?");
        assertTransform("Ансамбль \"Мелодия\"", "Àíñàìáëü «Ìåëîäèÿ»");
        assertTransform("Хорошо", "Õî›îøî");
        assertTransform("Майя Кристалинская", "Ìàéÿ Ê›èñòàëèíñêàÿ");
        assertTransform("Только любовь права", "Òîëüêî ëﬂáîâü ï›àâà");
        assertTransform("Александра Пахмутова, инструментальный ансамбль", "Àëåêñàíä›à Ïàõìóòîâà, èíñò›óìåíòàëüíûé àíñàìáëü");
        assertTransform("Три тополя на Плющихе", "Ò›è òîïîëÿ íà Ïëﬂùèõå");
        assertTransform("Ансамбль электромузыкальных инструментов п/у В. Мещерина", "Àíñàìáëü ‡ëåêò›îìóçûêàëüíûõ èíñò›óìåíòîâ ï/ó Â. Ìåùå›èíà");
        assertTransform("Всегда с тобой", "Âñåãäà ñ òîáîé");
        assertTransform("Камерный ансамбль \"Рококо\"", "Êàìå›íûé àíñàìáëü «‹îêîêî»");
        assertTransform("Гавайские гитары", "Ãàâàéñêèå ãèòà›û");
        assertTransform("Капиталина Лазаренко", "Êàïèòàëèíà Ëàçà›åíêî");
        assertTransform("Моя Гвинея", "Ìîÿ Ãâèíåÿ");
        assertTransform("Лариса Мондрус и Инго Граф", "Ëà›èñà Ìîíä›óñ è Èíãî Ã›àô");
        assertTransform("До свидания", "Äî ñâèäàíèÿ");
    }
    @Test
    public void testIsRussianInLatin() throws CommandException {
        assertFalse(isRussianInLatin(""));
        assertFalse(isRussianInLatin("0"));
        assertFalse(isRussianInLatin("1"));
        assertFalse(isRussianInLatin("b"));
        assertFalse(isRussianInLatin("El Niño"));
        assertFalse(isRussianInLatin(WORD_DE_ALL_UMLAUTS));
        assertTrue(isRussianInLatin("à"));
    }

    protected void assertTransform(String expected, String value) throws CommandException {
        assertTrue(isRussianInLatin(value));
        assertEquals(expected, command.transform(value));
    }
}
