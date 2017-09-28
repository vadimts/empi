package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.FileException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class UtilTest {

    @Test
    public void getMp3InDirectory() throws FileException {
        Stream<String> files = Util.File.getMp3InDirectory("/Users/vadim/Music/test");
//        Stream<String> files = Util.File.getMp3InDirectory("/mnt/alpaca/music/cd");
        List<String> fileList = files.collect(Collectors.toList());
        fileList.forEach(System.out::println);
        System.out.println("Total " + fileList.size());
    }

    @Test
    public void getPlaylistTracksScript() {
        long now = new Date().getTime();
        System.out.println(now);
        List<ITunes.Track> fs = ITunes.Script.getPlaylistTracks("cd");
        fs.forEach(System.out::println);
        System.out.println(new Date().getTime() - now);
    }

    @Test
    public void getPlaylistTracks() throws CommandException {
        long now = new Date().getTime();
        System.out.println(now);
        List<ITunes.Track> tracks = ITunes.getPlaylistTracksStat("cd");
        tracks.forEach(System.out::println);
        System.out.println(new Date().getTime() - now);
    }

    @Test
    public void getTrack() throws CommandException {
        List<ITunes.Track> tracks = ITunes.Script.getPlaylistTracks("cd");
        long now = new Date().getTime();
        System.out.println(now);
        ITunes.Track track = ITunes.getTrackById(tracks, 192000);
        System.out.println(new Date().getTime() - now);
        System.out.println(track);
    }

    @Test
    public void joinStringArray() throws UnsupportedEncodingException {
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
}
