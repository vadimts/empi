package eu.tsvetkov.x_empi.command;

import eu.tsvetkov.x_empi.BaseTest;
import eu.tsvetkov.x_empi.command.move.Rename;
import eu.tsvetkov.x_empi.error.CommandException;
import org.junit.After;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.tsvetkov.empi.util.Util.SEP;
import static junit.framework.Assert.assertTrue;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class CommandTest<C extends Command> extends BaseTest {

    public static final String ALBUM1 = "Dub Discipline (DUBOUT007)";
    public static final String ALBUM2 = "Paiva On Nuori";
    public static final String ALBUM2_TRACK = "01. Übergang on nuori.mp3";
    public static final String ARTIST1 = "Dom Hz & Synkro";
    public static final String ARTIST2 = "Raappana";
    public static final String ARTIST_ALBUM = ARTIST1 + Rename.SEP_ARTIST_ALBUM + ALBUM1;
    public static final List<String> DIRS = new ArrayList<>();
    public static final String SECOND_LETTER_SHORT_WHITESPACE = " ‎– ";
    public static final String SUFFIX = "-WEB";
    public static final String ARTIST_ALBUM_SUFFIX = ARTIST_ALBUM + SUFFIX;
    public static final List<String> TRACKS = new ArrayList<>();
    public static final String YEAR = "[2009]";
    public static final String ARTIST_ALBUM_SUFFIX_YEAR = ARTIST_ALBUM_SUFFIX + SEP + YEAR;
    public static final String YEAR2 = "2007";
    public static final String YEAR_ALBUM2 = YEAR2 + SEP + ALBUM2;
    public static final String YEAR_ARTIST_ALBUM = YEAR + SEP + ARTIST_ALBUM;
    public static final String YEAR_ARTIST_ALBUM_SUFFIX = YEAR_ARTIST_ALBUM + SUFFIX;
    public static Map<String, String[]> albums = new HashMap<>();

    static {
        albums.put("ru", new String[]{"муслим магомаев", "избранное", "2009"});
        albums.put("jp", new String[]{"DJ Krush", "覚醒 Kakusei", ""});
    }

    private Path sourcePath;
    private Path targetPath;

    @After
    public void after() throws IOException, CommandException {
        if (sourcePath != null && targetPath != null && (!Files.exists(sourcePath) || !sourcePath.equals(targetPath))) {
            Files.move(targetPath, sourcePath, StandardCopyOption.ATOMIC_MOVE);
        }
    }

    protected static void loadDirs() {
        try {
            DIRS.addAll(Files.readAllLines(Paths.get("target/test-classes/dirs.txt"), Charset.defaultCharset()));
        } catch (IOException e) {
        }
    }

    protected static void loadTracks() {
        try {
            TRACKS.addAll(Files.readAllLines(Paths.get("target/test-classes/tracks.txt"), Charset.defaultCharset()));
        } catch (IOException e) {
        }
    }

    protected void runCommand(C command, Path path) throws CommandException {
        assertTrue("Test file doesn't exist", Files.exists(path));
        sourcePath = path;
//        targetPath = command.run(path);
    }
}
