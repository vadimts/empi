package eu.tsvetkov.empi.x_empi.command.move;

import eu.tsvetkov.empi.util.Util;
import eu.tsvetkov.empi.x_empi.error.CommandException;
import eu.tsvetkov.empi.x_empi.error.CommandNotAppliedException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class RenameRegex extends Rename {

    public static final String ALL = "^.*$";
    public static final String GROUP = "(.+)";
    public static final String META_CHAR = "[1-9L*]";
    public static final String META_REGEX_ARG_MATCHER = META_CHAR + "=.*";
    public static final String TRACKNO = "(\\d+)\\.?";
    public static final Pattern TRACKNO_ARTIST_TITLE = Pattern.compile(TRACKNO + " " + GROUP + " - " + GROUP + "\\..+");
    protected Pattern from;
    protected String to;

    public RenameRegex(String metaRegex) {
        parseMetaRegex(metaRegex);
    }

    public RenameRegex(String regexFrom, String regexTo) {
        this.from = Pattern.compile(regexFrom);
        this.to = regexTo;
    }

    protected void parseMetaRegex(String metaRegex) {
        List<String> fromTo = Util.split(metaRegex, '=');
        String metaFrom = fromTo.get(0);
        String metaTo = fromTo.get(1);
        List<Integer> fromGroupsIndex = new ArrayList<>();
        int groupsFrom = 0;

        String fromStr = "";
        for (int i = 0; i < metaFrom.length(); i++) {
            String ch = metaFrom.substring(i, i + 1);
            if (ch.matches("[1-9L]")) {
                fromStr += (ch.equals("1") ? "" : "") + Util.WORD + (ch.equals("L") ? "" : "")
                    + (i < metaFrom.length() - 1 && metaFrom.substring(i + 1, i + 2).matches(META_CHAR) ? " " : "");
                groupsFrom++;
            } else if (ch.equals("*")) {
                fromStr += GROUP + " ";
                groupsFrom++;
                fromGroupsIndex.add(groupsFrom);
            } else {
                fromStr += ch;
            }
        }
        from = Pattern.compile("^" + fromStr.trim() + "$");

        to = "";
        int currentFromGroupIndex = 0;
        for (int i = 0; i < metaTo.length(); i++) {
            String ch = metaTo.substring(i, i + 1);
            if (ch.matches("[1-9L]")) {
                to += "$" + (ch.equals("L") ? groupsFrom : ch);
            } else if (ch.equals("*")) {
                to += "$" + fromGroupsIndex.get(currentFromGroupIndex++);
            } else {
                to += ch;
            }
        }
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
