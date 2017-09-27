package eu.tsvetkov.empi.command.move;

import eu.tsvetkov.empi.error.CommandException;

import java.nio.file.Path;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class MoveUp extends Move {

    @Override
    protected Path transformPath(Path sourcePath) throws CommandException {
        return sourcePath.getParent().getParent().resolve(sourcePath.getFileName());
    }
}
