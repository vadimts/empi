package eu.tsvetkov.empi.x_empi.command;

import eu.tsvetkov.empi.x_empi.error.CommandException;
import eu.tsvetkov.empi.x_empi.error.CommandNotAppliedException;
import eu.tsvetkov.empi.x_empi.error.Mp3Exception;
import eu.tsvetkov.empi.x_empi.error.NotSupportedFileException;

import java.nio.file.Path;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class Command<T> {

    protected boolean dryRun = false;
    private T after;
    private T before;

    protected Command() {
    }

    public abstract T run(Path sourcePath) throws CommandException;

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    protected static CommandException getCommandException(Mp3Exception e) {
        return (e instanceof NotSupportedFileException ? new CommandNotAppliedException(e) : new CommandException(e));
    }
}
