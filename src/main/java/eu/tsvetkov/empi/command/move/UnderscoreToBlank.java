package eu.tsvetkov.empi.command.move;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.CommandNotAppliedException;

import java.util.regex.Pattern;

import static eu.tsvetkov.empi.util.Util.WORD;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class UnderscoreToBlank extends Rename {

    public static final Pattern UNDERSCORE_FORMAT = Pattern.compile("_" + WORD + "[._]");

    @Override
    protected String transformFileName(String fileName) throws CommandException {
        if(!isUnderscoreFormat(fileName)) {
            throw new CommandNotAppliedException("File name '" + fileName + "' is not in underscore format");
        }
        return null;
    }

    public static boolean isUnderscoreFormat(String fileName) {
        return UNDERSCORE_FORMAT.matcher(fileName).find();
    }
}
