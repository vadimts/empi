package eu.tsvetkov.empi.util;

import org.junit.Test;

import static eu.tsvetkov.empi.util.Str.methodCall;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class StrTest {

    /**
     * Mock method for method substitution in Util.str()
     */
    public static String tryC(String tryScript, String catchScript) {
        return "try {\n" + tryScript + "\n} catch {\n" + catchScript + "\n}";
    }

    @Test
    public void ecs() {
        assertEquals("Камерный ансамбль \\\"Рококо\\\"", Str.esc("Камерный ансамбль \"Рококо\""));
        assertEquals("Неаполитанский танец - из балета \\\"Лебединое озеро\\\"", Str.esc("Неаполитанский танец - из балета \"Лебединое озеро\""));
        assertEquals("Finale (Final) - part 14 of \\\"The Carnival of the Animals\\\" (Le carnaval des animaux) suite", Str.esc("Finale (Final) - part 14 of \"The Carnival of the Animals\" (Le carnaval des animaux) suite"));
//        set loved of (track 1 of playlist "good-new" whose name = "Под Водой" and artist = "Soundtrack Film Soundtrack" and album = "Даун Хаус" and size = 7438464) to true
    }

    @Test
    public void str() {
        String str = new Str(
            "set {foundTracks, missingTracks} to {{}, {}}",
            "repeat with t in playlist `${1}`'s tracks",
            "${tryCatch(",
            "set foundTracks's end to ${trackPath(t)}",
            "set missingTracks's end to ${3}",
            ")}",
            "end repeat",
            "set AppleScriptOld's text item delimiters to ASCII character 10",
            "get {`${4}`, foundTracks, `${5}`, `${6}`, missingTracks, `${7}`} as string",
            "get ${trackPath(track ${2} ofNumber playlist \"${1}\")}"
        ).methodsIn(StrTest.class).with("my-playlist", "10", "missing", "F1", "F2", "M1", "M2");

        String[] lines = str.split("\n");
        assertEquals("set {foundTracks, missingTracks} to {{}, {}}", lines[0]);
        assertEquals("repeat with t in playlist \"my-playlist\"'s tracks", lines[1]);
        assertEquals(tryC("set foundTracks's end to " + trackPath("t"), "set missingTracks's end to missing"),
            Util.joinLines(lines[2], lines[3], lines[4], lines[5], lines[6]));
        assertEquals("end repeat", lines[7]);
        assertEquals("set AppleScriptOld's text item delimiters to ASCII character 10", lines[8]);
        assertEquals("get {\"F1\", foundTracks, \"F2\", \"M1\", missingTracks, \"M2\"} as string", lines[9]);
        assertEquals("get " + trackPath("track 10 ofNumber playlist \"my-playlist\""), lines[10]);
    }

    @Test
    public void strMethodCall() {
        assertEquals(asList("methodWithoutParams"), methodCall("${methodWithoutParams()}"));
        assertEquals(asList("method", "p1", "p2", "p3"), methodCall("${method(p1,p2,p3)}"));
        assertEquals(asList("tryCatch", "my-try-code", "my-catch-code"), methodCall("${tryCatch(\nmy-try-code\nmy-catch-code\n)}"));
    }

    /**
     * Mock method for method substitution in Util.str()
     */
    public String trackPath(String t) {
        return "POSIX path ofNumber (location ofNumber " + t + " as alias)";
    }

}
