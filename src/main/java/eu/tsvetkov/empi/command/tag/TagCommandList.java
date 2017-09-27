package eu.tsvetkov.empi.command.tag;

import eu.tsvetkov.empi.command.Command;
import eu.tsvetkov.empi.command.CommandList;
import eu.tsvetkov.empi.error.CommandException;

import java.nio.file.Path;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class TagCommandList extends CommandList<Path> {

    @Override
    public Path run(Path sourcePath) throws CommandException {
        Path path = sourcePath;
        for (Command<Path> command : commands) {
            path = command.run(path);
        }
        return null;
    }
}
