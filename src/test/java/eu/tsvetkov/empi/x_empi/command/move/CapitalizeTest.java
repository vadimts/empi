package eu.tsvetkov.empi.command.move;

import eu.tsvetkov.empi.command.CommandTest;
import eu.tsvetkov.empi.error.CommandException;
import org.apache.commons.lang3.text.WordUtils;
import org.junit.Test;

import static eu.tsvetkov.empi.util.Util.SEP;
import static eu.tsvetkov.empi.command.move.Rename.SEP_ARTIST_ALBUM;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class CapitalizeTest extends CommandTest<Capitalize> {

    private boolean isDirectory = false;
    Capitalize command = new Capitalize() {
        @Override
        protected boolean isDirectory() {
            return isDirectory;
        }
    };

    @Test
    public void transformTestDirName() throws CommandException {
        isDirectory = true;
        for (String dir : DIRS) {
            System.out.println(command.transformFileName(dir));
        }
    }

    @Test
    public void transformFileName() throws CommandException {
        isDirectory = true;
        assertEquals("Artist", command.transformFileName("artist"));
        assertEquals("Artist.Artist", command.transformFileName("artist.artist"));
        assertEquals("B. B. King", command.transformFileName("b. b. king"));
        assertEquals("Art.Ist - Album", command.transformFileName("art.ist - album"));
        assertEquals("Art-Ist - Album", command.transformFileName("art-ist - album"));
        assertEquals("Art_Ist - Album", command.transformFileName("art_ist - album"));
        isDirectory = false;
        assertEquals("Track", command.transformFileName("track"));
        assertEquals("T.mp3", command.transformFileName("t.mp3"));
        assertEquals("Track.mp3", command.transformFileName("track.mp3"));
        assertEquals("Track.Track.mp3", command.transformFileName("track.track.mp3"));
        assertEquals("Artist - Track.mp3", command.transformFileName("artist - track.mp3"));
        assertEquals("Art.Ist - Track.mp3", command.transformFileName("art.ist - track.mp3"));
        assertEquals("Art.Ist - Tra.Ck.mp3", command.transformFileName("art.ist - tra.ck.mp3"));
        assertEquals("Art_Ist - Tra_Ck.mp3", command.transformFileName("art_ist - tra_ck.mp3"));
    }

    @Test
    public void run() throws Exception {
        Capitalize command = new Capitalize();
        String lang = "ru";
        String artist = albums.get(lang)[0];
        String album = albums.get(lang)[1];
        String year = albums.get(lang)[2];
        String dir = artist + SEP_ARTIST_ALBUM + album + (isNotBlank(year) ? SEP + year : "");
        runCommand(command, getTestFilePath(dir));
        String dirCapitalized = WordUtils.capitalize(dir);
        assertTestPath(dirCapitalized);
    }

}
