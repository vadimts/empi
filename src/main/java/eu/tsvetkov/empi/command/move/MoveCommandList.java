package eu.tsvetkov.empi.command.move;

import eu.tsvetkov.empi.command.CommandList;
import eu.tsvetkov.empi.error.CommandException;

import java.nio.file.Path;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class MoveCommandList extends CommandList<Move> {

    public MoveCommandList(Move... commands) {
        super(commands);
    }

    @Override
    public Path run(Path sourcePath) throws CommandException {
        Path path = sourcePath;
        for (Move command : commands) {
            path = command.run(path);
        }
        return sourcePath;
    }
}
