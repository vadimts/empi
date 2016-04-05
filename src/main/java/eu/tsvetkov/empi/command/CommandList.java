package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.error.CommandException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class CommandList extends Command {

    private List<Command> commands = new ArrayList<>();

    public CommandList(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    public void add(Command command) {
        if (command.equals(Flag.DRY_RUN)) {
            setDryRun(true);
        } else {
            commands.add(command);
        }
    }

    @Override
    protected Path transformPath(Path sourcePath) throws CommandException {
        Path path = sourcePath;
        for (Command command : commands) {
            path = command.transformPath(path);
        }
        return path;
    }
}
