package eu.tsvetkov.empi;

import eu.tsvetkov.empi.command.Command;
import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.CommandNotAppliedException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
* @author Vadim Tsvetkov (dev@tsvetkov.eu)
*/
class CommandFileVisitor extends SimpleFileVisitor<Path> {

    public static final Collection<Path> IGNORE = Arrays.asList(Paths.get(".DS_Store"));

    private Command command;
    private int changed = 0;
    private int skipped = 0;
    private int failed = 0;
    private List<Path> ignored = new ArrayList<>();

    public CommandFileVisitor(Command command) {
        this.command = command;
        ignored.addAll(IGNORE);
    }

    public String getResult() {
        return "Changed " + changed + ", skipped " + skipped + ", failed " + failed;
    }

    public void ignore(Path path) {
        ignored.add(path);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if(isIgnored(dir)) {
            return CONTINUE;
        }
        visitPath(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(isIgnored(file)) {
            return CONTINUE;
        }
        visitPath(file);
        return CONTINUE;
    }

    protected boolean isIgnored(Path path) {
        return ignored.contains(path) || ignored.contains(path.getFileName());
    }

    protected void visitPath(Path path) throws IOException {
        path = path.toAbsolutePath().toRealPath();
        try {
            System.out.println(path + "\n   ⤷   " + command.run(path));
            changed ++;
        } catch (CommandNotAppliedException e) {
            System.out.println(path + "\n  skip: " + e);
            skipped ++;
        } catch (CommandException e) {
            System.out.println(path + "\n  ERROR: " + e);
            failed ++;
        }
    }
}
