package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.itunes.AppleScript;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;

import static eu.tsvetkov.empi.itunes.AppleScript.*;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class UtilTest {

    @Test
    public void encodeNormalize() throws UnsupportedEncodingException {
        assertEquals("%C3%9F", URLEncoder.encode(Normalizer.normalize("ß", Normalizer.Form.NFKD), "UTF8"));
        assertEquals("ö", Normalizer.normalize("ö", Normalizer.Form.NFD));
        assertEquals("o%CC%88", URLEncoder.encode(Normalizer.normalize("ö", Normalizer.Form.NFD), "UTF8"));
        assertEquals("Melancholisch%20Scho%CC%88n", URLEncoder.encode(Normalizer.normalize("Melancholisch Schön", Normalizer.Form.NFKD), "UTF8").replace("+", "%20"));
        assertEquals("%E9%8A%80%E6%B2%B3%E3%81%A8%E8%BF%B7%E8%B7%AF", URLEncoder.encode("銀河と迷路", "UTF8"));
        assertEquals("%E9%8A%80%E6%B2%B3%E3%81%A8%E8%BF%B7%E8%B7%AF", URLEncoder.encode(Normalizer.normalize("銀河と迷路", Normalizer.Form.NFKD), "UTF8"));
        assertEquals("ö", Normalizer.normalize("ö", Normalizer.Form.NFKD));
        assertEquals("Ф", Normalizer.normalize("Ф", Normalizer.Form.NFKD));
        assertEquals("abc", Util.join(new String[]{"a", "b", "c"}));
        assertEquals("a,b,c", Util.join(new String[]{"a", "b", "c"}, ","));
        assertEquals("aabbcc", Util.join(new String[]{"aa", "bb", "cc"}));
        assertEquals("aa,bb,cc", Util.join(new String[]{"aa", "bb", "cc"}, ","));
    }

    @Test
    public void getCurrentMethodName() {
        assertEquals("getCurrentMethodName", Util.getCurrentMethodName());
        assertEquals("testMethod", new Object() {
            String testMethod() {
                return Util.getCurrentMethodName();
            }
        }.testMethod());
    }

    @Test
    public void getMatchGroupLists() {
        String s1 = F1 + "\nfound track 11\nfound track 12\n" + F2 + "\n" + M1 + "\nmissing track 11\nmissing track 12\n" + M2 + "\n"
            + F1 + "\nfound track 21\nfound track 22\n" + F2 + "\n" + M1 + "\nmissing track 21\nmissing track 22\n" + M2;
        String s2 = F1 + "\nfound track 11\nfound track 12\n" + F2 + "\n" + M1 + "\n\n" + M2 + "\n"
            + F1 + "\nfound track 21\nfound track 22\n" + F2 + "\n" + M1 + "\n\n" + M2 + "\n";
        Map<String, List<String>> matchGroupLists1 = Util.getMatchGroupLists(AppleScript.RE_TRACK_PATHS, asList(F3, M3), s1);
        assertEquals(asList("found track 11", "found track 12", "found track 21", "found track 22"), matchGroupLists1.get(F3));
        assertEquals(asList("missing track 11", "missing track 12", "missing track 21", "missing track 22"), matchGroupLists1.get(M3));
        Map<String, List<String>> matchGroupLists2 = Util.getMatchGroupLists(AppleScript.RE_TRACK_PATHS, asList(F3, M3), s2);
        assertEquals(asList("found track 11", "found track 12", "found track 21", "found track 22"), matchGroupLists2.get(F3));
        assertEquals(EMPTY_LIST, matchGroupLists2.get(M3));
    }

    @Test
    public void joinStringArray() {
        assertEquals("abc", Util.join(new String[]{"a", "b", "c"}));
        assertEquals("a,b,c", Util.join(new String[]{"a", "b", "c"}, ","));
        assertEquals("aabbcc", Util.join(new String[]{"aa", "bb", "cc"}));
        assertEquals("aa,bb,cc", Util.join(new String[]{"aa", "bb", "cc"}, ","));
        assertEquals("POSIX file \"track1\", POSIX file \"track2\"", Util.join(asList("track1", "track2"), "POSIX file `${1}`", ", "));
    }
}
