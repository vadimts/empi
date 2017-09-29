package eu.tsvetkov.empi;

import eu.tsvetkov.empi.command.*;
import eu.tsvetkov.empi.command.move.Capitalize;
import eu.tsvetkov.empi.command.move.RenameClear;
import eu.tsvetkov.empi.command.move.RenameRegex;
import eu.tsvetkov.empi.command.tag.*;
import eu.tsvetkov.empi.mp3.Mp3Tag;
import eu.tsvetkov.empi.util.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Empi {

    public static void main(String[] args) throws IOException {
        String dir = ".";
        CommandList commands = new TagCommandList();
        boolean withParent = false;
        String argsString = Util.join(args, ", ");

        // Print help and exit, if desired.
        if(Util.isBlank(argsString) || "-h".equalsIgnoreCase(argsString)) {
            System.out.println("empi <COMMANDS> <DIR>\n" +
                "█ Commands:\n" +
                "-cap          Capitalize file names\n" +
                "-cREGEX       Clear strings matching REGEX in file names\n" +
                "-dry          Dry run\n" +
                "-h            This help\n" +
                "-i            Info containing all ID3 tags:\n" +
                "                  " + Mp3Tag.CODES_TAGS_ALL + "\n" +
                "-p            Also process parent directory\n" +
                "-rMETA-REGEX  Rename files as defined by META-REGEX\n" +
                "-tLat         Translate russian tags from Latin to Windows encoding\n" +
                "-tTra         Transliterate tags from russian to english and set them as sort tags");
            return;
        }

        System.out.println("args: " + argsString);

        for (String arg : args) {
            if (arg.matches("[^-].*")) {
                dir = arg;
            } else if (arg.matches("-cap")) {
                commands.add(new Capitalize());
            } else if (arg.matches("-c.*")) {
                commands.add(new RenameClear(arg.replaceFirst("-c", "")));
            } else if (arg.matches("-dry")) {
                commands.setDryRun(true);
            } else if (arg.matches("-i")) {
                commands.add(new TagInfo());
            } else if (arg.matches("-(p|parent)")) {
                withParent = true;
            } else if (arg.matches("-r" + RenameRegex.META_REGEX_ARG_MATCHER)) {
                commands.add(new RenameRegex(arg.replaceFirst("-r", "")));
            } else if (arg.matches("-s")) {
                commands.add(new RenameRegex(arg.replaceFirst("-r", "")));
            } else if (arg.matches("-tLat")) {
                commands.add(new TranslateLatinToWinRU());
            } else if (arg.matches("-tAl:.*=.*")) {
                commands.add(new Tag(arg.substring(2)));
            } else if (arg.matches("-tTra")) {
                commands.add(new TransliterateSortTags());
            }
        }

        Path path = Paths.get(dir);
        System.out.println("enc " + System.getProperty("file.encoding"));
        System.out.println("dir " + path.toAbsolutePath().normalize());

        CommandFileVisitor visitor = new CommandFileVisitor(commands);
        if(!withParent) {
            visitor.ignore(path);
        }
        visitor.run(path);
        System.out.println(visitor.getResult());
    }

}
