package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.error.CommandException;

import java.nio.file.Path;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Flag extends Command {

    public static final Flag DRY_RUN = new Flag(1);
    public static final Flag WITH_PARENT = new Flag(2);

    private int id;

    public Flag(int id) {
        this.id = id;
    }

    @Override
    protected final Path transformPath(Path sourcePath) throws CommandException {
        return null;
    }
}
