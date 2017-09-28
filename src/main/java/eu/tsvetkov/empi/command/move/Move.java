package eu.tsvetkov.empi.command.move;

import eu.tsvetkov.empi.command.Command;
import eu.tsvetkov.empi.error.CommandException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class Move extends Command<Path> {

    public final Path run(Path sourcePath) throws CommandException {
        Path targetPath = transformPath(sourcePath);
        return (dryRun ? targetPath : move(sourcePath, targetPath));
    }

    protected final Path move(Path sourcePath, Path targetPath) throws CommandException {
        try {
            return Files.move(sourcePath, targetPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new CommandException(e);
        }
    }

    protected abstract Path transformPath(Path sourcePath) throws CommandException;
}
