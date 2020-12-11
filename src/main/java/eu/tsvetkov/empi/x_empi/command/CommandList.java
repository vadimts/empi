package eu.tsvetkov.empi.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public abstract class CommandList<T extends Command> extends Command {

    protected List<T> commands = new ArrayList<>();

    public CommandList(T... commands) {
        this.commands.addAll(Arrays.asList(commands));
        setAllButLastDry();
    }

    public void add(T lastCommand) {
        commands.add(lastCommand);
        setAllButLastDry();
    }

    protected void setAllButLastDry() {
        if(commands.isEmpty()) {
            return;
        }
        for (T command : commands) {
            command.setDryRun(true);
        }
        commands.get(commands.size()-1).setDryRun(dryRun);
    }
}
