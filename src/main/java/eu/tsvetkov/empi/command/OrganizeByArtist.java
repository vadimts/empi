package eu.tsvetkov.empi.command;

import eu.tsvetkov.empi.error.CommandException;
import eu.tsvetkov.empi.error.Mp3Exception;
import eu.tsvetkov.empi.mp3.JTag;
import eu.tsvetkov.empi.mp3.Mp3Info;
import net.sf.junidecode.Junidecode;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
            Mp3Info f = new JTag(sourcePath);
            String artist = f.getTitle();
            System.out.println(artist);
            if(artist.matches(".*\\b[ÀÁÂÃÄÅ¨ÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäå¸æçèéêëìíîïðñòóôõö÷øùúûüýþÿ]+\\b.*")) {
                String artistFixed = new String(artist.getBytes("latin1"), "cp1251").replaceAll("[«»]", "\"");
                System.out.println(artistFixed);
                String artistTransl = Junidecode.unidecode(artistFixed);
                System.out.println(artistTransl);
                f.setArtist(artistFixed);
                f.setSortArtist(artistTransl);
//                f.save();
            }
        } catch (Exception e) {
        }
        return null;
    }
}
