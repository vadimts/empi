package eu.tsvetkov.empi.command.move;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class RenameClear extends RenameRegex {

    public RenameClear(String regexFrom) {
        super(regexFrom, "");
    }
}
