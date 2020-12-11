package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.CommandNotAppliedException;
import eu.tsvetkov.empi.error.Mp3Exception;
import eu.tsvetkov.empi.error.NotSupportedFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class Command<T> {

    private T before;
    private T after;

    protected boolean dryRun = false;

    protected Command() {
    }

    protected static CommandException getCommandException(Mp3Exception e) {
        return (e instanceof NotSupportedFileException ? new CommandNotAppliedException(e) : new CommandException(e));
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public abstract T run(Path sourcePath) throws CommandException;

    @Override
    public String toString() {
        return super.toString();
    }
}
