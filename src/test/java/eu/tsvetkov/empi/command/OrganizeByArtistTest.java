package eu.tsvetkov.empi.command;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class OrganizeByArtistTest extends CommandTest<OrganizeByArtist> {

    OrganizeByArtist command = new OrganizeByArtist();

    @Test
    public void transformPath() throws Exception {
//        command.transformPath(getTestFilePath("id3v2/Télépopmusik-Genetic_World-_JAL80_-WEB-2001-EPiCFAiL/01 Breathe ft. Angela McCluskey.mp3"));
//        command.transformPath(getTestFilePath("Télépopmusik-Genetic_World-_JAL80_-WEB-2001-EPiCFAiL/06 Let's Go Again ft. Gonzales & Peaches.mp3"));
//        command.transformPath(getTestFilePath("Raappana/2007 Paiva On Nuori/01. Übergang on nuori.mp3"));
//        command.transformPath(getTestFilePath("id3v2/Scientist Rids The World Of The Evil Curse Of The Vampires/01 The Voodoo Curse.mp3"));
        command.transformPath(getTestFilePath("id3v2/Soviet Light Music  2/01. Инна Таланова - Приснись.mp3"));
        command.transformPath(getTestFilePath("id3v2/Soviet Light Music  2/02. Камерный ансамбль «Рококо» - Витражи.mp3"));
    }

    @Test
    public void transformNewPaths() throws Exception {
        loadTracks();
        for(int i = 0; i < 120; i ++) {
            int j = new Random().nextInt(TRACKS.size());
            Path sourcePath = Paths.get(TRACKS.get(j));
            command.transformPath(sourcePath);
        }
    }
}
