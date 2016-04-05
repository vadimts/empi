package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.error.CommandException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class Command {

    protected boolean dryRun = false;

    protected Command() {
    }

    public final Path run(Path sourcePath) throws CommandException {
        Path targetPath = transformPath(sourcePath);
        return (dryRun ? targetPath : move(sourcePath, targetPath));
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public String toString() {
        return super.toString();
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
