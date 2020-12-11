package eu.tsvetkov.empi;

import eu.tsvetkov.empi.mp3.AudioFile;
import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static eu.tsvetkov.empi.util.Util.out;

public class Empi2 {

    public static void main(String[] args) {
        Path workDir = Paths.get(".");

        String allArgs = Util.join(args, ", ");
        out("Arguments: {0}", allArgs);
        for(String arg : args) {
            if(arg.matches("[^-].*")) {
                workDir = Paths.get(arg);
                out("Work directory: {0}", workDir);
            }
        }

        try {
//            processFile(workDir);
            getAudioFiles(workDir).forEach(Util::out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static Stream<Path> getAudioFiles(Path dir) throws IOException {
        return Files.walk(dir).sorted(Util.CASE_INSENSITIVE_COMPARATOR).filter(AudioFile::isAudioFile);
    }

    private static void processFile(Path file) throws IOException {
        if(Files.isDirectory(file)) {
            Files.list(file).filter(Mp3File::isMp3File).sorted(Util.CASE_INSENSITIVE_COMPARATOR).forEach(System.out::println);
        }
        else {

        }
    }
}
