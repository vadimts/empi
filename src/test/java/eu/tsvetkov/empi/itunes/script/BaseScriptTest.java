package eu.tsvetkov.empi.itunes.script;

import eu.tsvetkov.empi.error.itunes.ITunesException;
import eu.tsvetkov.empi.util.Script;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static eu.tsvetkov.empi.util.ITunesTest.TEST_PLAYLIST;
import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class VBScriptTest {


    VBScript script = new VBScript();
    private String playlistName;
    private int originalPlaylistCount;

    @Before
    public void before() throws ITunesException {
        originalPlaylistCount = getPlaylistCount();
        playlistName = TEST_PLAYLIST + new Date().getTime();
        assertEquals(playlistName, Script.getOrCreatePlaylist(playlistName));
        assertEquals(originalPlaylistCount + 1, getPlaylistCount());
    }

    protected int getPlaylistCount() throws ITunesException {
        return Integer.valueOf(script.exec(script.echo("iTunes.LibrarySource.Playlists.Count")));
    }

    @After
    public void after() throws ITunesException {
        Script.deletePlaylist(playlistName);
        assertEquals(originalPlaylistCount, getPlaylistCount());
    }

    @Test
    public void play() throws ITunesException {
        String playlistCount = script.exec(script.echo("iTunes.LibrarySource.Playlists.Count"));
        System.out.println(playlistCount);
    }
}
