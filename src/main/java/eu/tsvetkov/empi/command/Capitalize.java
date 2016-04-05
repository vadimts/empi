package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.error.CommandException;

import java.util.regex.Matcher;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Capitalize extends RenameRegex {

    private static final String FIRST_LETTER = "(?:^|[ ._-])([^ ._-])";

    public Capitalize() {
        super(FIRST_LETTER, "");
    }

    @Override
    public String toString() {
        return "Capitalize";
    }

    @Override
    protected String replace(Matcher matcher) {
        StringBuffer output = new StringBuffer();
        matcher.useAnchoringBounds(false).useTransparentBounds(true).reset();
        while (matcher.find()) {
            String capitalizedLetter = matcher.group().toUpperCase();
            matcher.appendReplacement(output, capitalizedLetter);
        }
        matcher.appendTail(output);
        return output.toString();
    }

    @Override
    protected String transformFileName(String fileName) throws CommandException {
        if (!isDirectory()) {
            int lastDotIndex = fileName.lastIndexOf(".");
            if(fileName.lastIndexOf(".") > 0) {
                return super.transformFileName(fileName.substring(0, lastDotIndex)) + fileName.substring(lastDotIndex);
            }
        }
        return super.transformFileName(fileName);
    }
}
