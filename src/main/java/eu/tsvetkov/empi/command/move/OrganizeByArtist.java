package eu.tsvetkov.empi.command.move;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.mp3.JTag;
import eu.tsvetkov.empi.mp3.Mp3File;

import java.nio.file.Path;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class OrganizeByArtist extends Move {

    // Êàìåðíûé àíñàìáëü «Ðîêîêî» --- Камерный ансамбль «Рококо»
    // ÀÁÂÃÄÅ¨ÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäå¸æçèéêëìíîïðñòóôõö÷øùúûüýþÿ --- АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя

    @Override
    protected Path transformPath(Path sourcePath) throws CommandException {
        try {
            Mp3File f = new JTag(sourcePath);
        } catch (Exception e) {
        }
        return null;
    }
}
