package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.CommandNotAppliedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class RenameRegex extends Rename {

    public static final String ALL = "^.*$";
    public static final String GROUP = "(.+)";
    public static final String WORD = "((?!-)[^" + SEP + "]+)";

    protected Pattern from;
    protected String to;

    public RenameRegex(String regexFrom, String regexTo) {
        this.from = Pattern.compile(regexFrom);
        this.to = regexTo;
    }

    protected String replace(Matcher matcher) {
        return matcher.replaceAll(to);
    }

    @Override
    protected String transformFileName(String fileName) throws CommandException {
        Matcher matcher = from.matcher(fileName);
        if (!matcher.find()) {
            throw new CommandNotAppliedException("Regex '" + from + "' is not found in file name '" + fileName + "'");
        }
        return replace(matcher);
    }
}
