package eu.tsvetkov.empi;

import eu.tsvetkov.empi.command.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eu.tsvetkov.empi.command.Rename.SEP;
import static eu.tsvetkov.empi.command.RenameRegex.GROUP;
import static eu.tsvetkov.empi.command.RenameRegex.WORD;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Empi {

    public static void main(String[] args) throws IOException {
        String dir = ".";
        CommandList commands = new CommandList();
        boolean withParent = false;

        String s = "";
        for (String arg : args) {
            s += arg + ", ";
        }
        System.out.println("args: " + s);

        for (String arg : args) {
            if (arg.matches("[^-].*")) {
                dir = arg;
            } else if (arg.matches("-cap")) {
                commands.add(new Capitalize());
            } else if (arg.matches("-c.*")) {
                commands.add(new RenameClear(arg.replaceFirst("-c", "")));
            } else if (arg.matches("-dry")) {
                commands.add(Flag.DRY_RUN);
            } else if (arg.matches("-(p|parent)")) {
                withParent = true;
            } else if (arg.matches("-r1-\\$")) {
                commands.add(new RenameRegex(WORD + SEP + GROUP, "$2" + SEP + "$1"));
            } else if (arg.matches("-r2-\\$")) {
                commands.add(new RenameRegex(WORD + SEP + WORD + SEP + GROUP, "$1" + SEP + "$3" + SEP + "$2"));
            } else if (arg.matches("-r3-\\$")) {
                commands.add(new RenameRegex(WORD + SEP + WORD + SEP + WORD + SEP + GROUP, "$1" + SEP + "$2" + SEP + "$4" + SEP + "$3"));
            }
        }

        Path path = Paths.get(dir);
        System.out.println("enc " + System.getProperty("file.encoding"));
        System.out.println("dir " + path.toAbsolutePath().normalize());

        CommandFileVisitor visitor = new CommandFileVisitor(commands);
        if(!withParent) {
            visitor.ignore(path);
        }
        Files.walkFileTree(path, visitor);
        System.out.println(visitor.getResult());
    }

}
