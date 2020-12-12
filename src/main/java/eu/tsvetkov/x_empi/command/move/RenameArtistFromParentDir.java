package eu.tsvetkov.x_empi.command.move;

import eu.tsvetkov.x_empi.error.CommandException;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class RenameArtistFromParentDir extends Rename {

    @Override
    protected String transformFileName(String fileName) throws CommandException {
        return sourcePath.getParent().getFileName() + SEP_ARTIST_ALBUM + fileName;
    }
}
