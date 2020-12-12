package eu.tsvetkov.x_empi.command.move;

import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.x_empi.error.CommandException;

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
            Mp3File f = new Mp3File(sourcePath);
        } catch (Exception e) {
        }
        return null;
    }
}
