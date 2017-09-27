package eu.tsvetkov.empi.command.move;

import eu.tsvetkov.empi.command.Command;
import eu.tsvetkov.empi.error.CommandException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class MoveCommandList<T> extends Command {

    protected List<Command> commands = new ArrayList<>();

    public MoveCommandList(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    public void add(Command command) {
        commands.add(command);
    }

    @Override
    public T run(Path sourcePath) throws CommandException {
        T path = sourcePath;
        for (Command command : commands) {
            command.run(path);
        }
        return null;
    }
}
