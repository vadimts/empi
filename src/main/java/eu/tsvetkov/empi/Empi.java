package eu.tsvetkov.empi;

import eu.tsvetkov.empi.ops.MusicOps;
import eu.tsvetkov.empi.util.SLogger;
import eu.tsvetkov.empi.util.Str;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNullElse;

public class Empi {

    static SLogger log = new SLogger();

    public static void main(String[] args) {
        Path workDir = Paths.get("").toAbsolutePath();
        if (args.length == 0 || args[0].equals("-?") || args[0].equals("-h") || args[0].equals("help")) {
            log.info(Str.of(
                "Working directory: ${1}",
                "Usage: empi [command] [required parameters...] (optional parameters...)",
                "Available commands:",
                "  * add [dir] (-p playlist) (-l love-list)",
                "    Adds audio files from the source directory as tracks to the destination playlist.",
                "    If the destination playlist does not exist, it will be created.",
                "    Parameters:",
                "    - dir",
                "      Path to the source directory.",
                "    - playlist",
                "      Name of the destination playlist. If omitted, the name of the source directory is used.",
                "    - love-list",
                "      Path to a text file with file paths of audio files that will be marked as loved tracks in the destination playlist.",
                "    Examples:",
                "    - empi add /itunes/new",
                "      Add audio files from the directory '/itunes/new' to the playlist 'new'.",
                "    - empi add /itunes/new -p my-playlist",
                "      Add audio files from the directory '/itunes/new' to the playlist 'my-playlist'.",
                "    - empi add /itunes/new -l /itunes/great-tracks.txt",
                "      Add audio files from the directory '/itunes/new' to the playlist 'new', mark the ones listed in the file 'great-tracks.txt' as loved."
            ).with(workDir));
            return;
        }
        if (args[0].equals("add")) {
            String dir = null;
            String playlist = null;
            Path loveListPath = null;
            log.debug(args[1]);
            for (int i = 1; i < args.length; i++) {
                if (args[i].equals("-p")) {
                    playlist = args[++i];
                } else if (args[i].equals("-l")) {
                    loveListPath = workDir.resolve(args[++i]);
                } else {
                    dir = args[i];
                }
            }
            Path dirPath = workDir.resolve(dir);
            playlist = requireNonNullElse(playlist, dirPath.getFileName().toString());
            MusicOps.addTracksWithLove(playlist, dirPath, loveListPath);
        }
    }
}
