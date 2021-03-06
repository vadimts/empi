package eu.tsvetkov.x_empi.command.move;

import eu.tsvetkov.empi.util.Util;
import eu.tsvetkov.x_empi.error.CommandException;
import eu.tsvetkov.x_empi.error.CommandNotAppliedException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class Rename extends Move {

    public static final String PSEP = File.separator;
    public static final String SEP_ARTIST_ALBUM = Util.SEP + "-" + Util.SEP;

    protected Path sourcePath;

    protected boolean isDirectory() {
        return Files.isDirectory(sourcePath);
    }

    protected abstract String transformFileName(String fileName) throws CommandException;

    @Override
    protected final Path transformPath(Path sourcePath) throws CommandException {
        this.sourcePath = sourcePath;
        Path targetPath = sourcePath.getParent().resolve(Paths.get(transformFileName(sourcePath.getFileName().toString())));
        if (sourcePath.equals(targetPath)) {
            throw new CommandNotAppliedException("Command " + this + " didn't change file name '" + sourcePath.getFileName() + "'");
        }
        return targetPath;
    }
}
