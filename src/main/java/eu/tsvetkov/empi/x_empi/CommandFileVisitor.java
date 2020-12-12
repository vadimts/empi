package eu.tsvetkov.empi.x_empi;

import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.util.Util;
import eu.tsvetkov.empi.x_empi.command.Command;
import eu.tsvetkov.empi.x_empi.error.CommandException;
import eu.tsvetkov.empi.x_empi.error.CommandNotAppliedException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static eu.tsvetkov.empi.mp3.Mp3Tag.Emoji;
import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
class CommandFileVisitor extends SimpleFileVisitor<Path> {

    public static final Collection<Path> IGNORE = Arrays.asList(Paths.get(".DS_Store"));
    private int changed = 0;
    private Command command;
    private int failed = 0;
    private List<Path> ignored = new ArrayList<>();
    private List<Path> pathsToVisit = new ArrayList<>();
    private int skipped = 0;

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
        if (isIgnored(dir)) {
            return CONTINUE;
        }
        pathsToVisit.add(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return super.preVisitDirectory(dir, attrs);
    }

    public void run(Path path) throws IOException {
        pathsToVisit = new ArrayList<>();
        Files.walkFileTree(path, this);
        pathsToVisit.stream().sorted().forEach(this::visitPath);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (isIgnored(file) || !Mp3File.isMp3File(file)) {
            return CONTINUE;
        }
        pathsToVisit.add(file);
        return CONTINUE;
    }

    protected boolean isIgnored(Path path) {
        return ignored.contains(path) || ignored.contains(path.getFileName());
    }

    protected void visitPath(Path path) {
        if (Files.isDirectory(path)) {
            System.out.println("\n" + Emoji.DIR + " " + path);
        }

        try {
            path = path.toAbsolutePath().toRealPath();
            Object commandResult = command.run(path);
            System.out.println(Util.abbrFilename(path) + "   " + commandResult);
            changed++;
        } catch (CommandNotAppliedException e) {
//            System.out.println(path + "\n  skip: " + e);
            skipped++;
        } catch (CommandException | IOException e) {
            System.out.println(path + "\n  ERROR_FILE_UNREADABLE: " + e);
            failed++;
        }
    }
}
