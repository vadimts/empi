package eu.tsvetkov.empi.model;

import eu.tsvetkov.empi.util.FileUtil;
import eu.tsvetkov.empi.util.SLogger;
import eu.tsvetkov.empi.util.Str;
import eu.tsvetkov.empi.util.Util;
import org.jaudiotagger.tag.datatype.Artwork;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eu.tsvetkov.empi.model.AudioArtwork.Status.*;
import static java.util.Arrays.asList;

public class AudioArtwork {

    private static SLogger log = new SLogger();

    private Exception error = null;
    private BufferedImage image = null;
    private boolean isFile = false;
    private Path path;
    private float ratio = 0;
    private Status status = OK;

    public AudioArtwork(Path path) {
        this.path = path;
    }

    public static AudioArtwork from(Artwork artwork) {
        String imageUrl = artwork.getImageUrl();
        AudioArtwork audioArtwork = new AudioArtwork(Paths.get(imageUrl == null || imageUrl.trim().isEmpty() ? "empty-path" : imageUrl));
        try {
            audioArtwork.setImage(artwork.getImage());
        } catch (IOException e) {
            audioArtwork.error = e;
            audioArtwork.status = ERROR_FILE_UNREADABLE;
        }
        return audioArtwork;
    }

    public static AudioArtwork from(Path path) {
        AudioArtwork audioArtwork = new AudioArtwork(path);
        audioArtwork.isFile = true;
        if (path == null || Files.notExists(path)) {
            audioArtwork.status = NOT_FOUND;
        } else {
            try {
                audioArtwork.setImage(ImageIO.read(path.toFile()));
            } catch (IOException e) {
                audioArtwork.error = e;
                audioArtwork.status = ERROR_FILE_UNREADABLE;
            }
        }
        return audioArtwork;
    }

    public AudioArtwork downsizeImageIfNeeded(int maxWidth) {
        if (image.getWidth() > maxWidth) {
            float zoom = (float) maxWidth / image.getWidth();
            int resizedWidth = maxWidth;
            int resizedHeight = (int) (image.getHeight() * zoom);
            BufferedImage resizedImage = new BufferedImage(resizedWidth, resizedHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = resizedImage.createGraphics();
            //            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            //            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(image.getScaledInstance(resizedWidth, resizedHeight, Image.SCALE_SMOOTH), 0, 0, null);
            setImage(resizedImage);
            log.debugGreen("Downsized image to ${1}px", resizedWidth);
        } else {
            log.debugBlue("Image is smaller than max width, no need to downsize: ${1}px <= ${2}px", image.getWidth(), maxWidth);
        }
        return this;
    }

    public Artwork getArtwork() {
        Artwork artwork = new Artwork();
        artwork.setBinaryData(getImageBytes());
        artwork.setMimeType(FileUtil.getFileExtension(path));
        artwork.setPictureType(0);
        return artwork;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.status = (image != null ? (image.getWidth() > 0 ? OK : ERROR_EMPTY_IMAGE) : ERROR_NULL_IMAGE);
        this.ratio = (image != null && image.getWidth() > 0 ? (float) image.getWidth() / image.getHeight() : 0);
    }

    public byte[] getImageBytes() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(getImage(), FileUtil.getFileExtension(path), bytes);
            return bytes.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public Path getPath() {
        return path;
    }

    public AudioArtwork setPath(Path path) {
        this.path = path;
        return this;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public boolean isCover() {
        return isOk() && getWidth() >= 200 && isSquare();
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isOk() {
        return status.equals(OK);
    }

    public boolean isSquare() {
        return ratio > 0.8 && ratio < 1.2;
    }

    public AudioArtwork save(Path newPath) {
        try {
            String newPathString = newPath.toString();
            ImageIO.write(image, FileUtil.getFileExtension(newPathString), newPath.toFile());
            setPath(newPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public String toString() {
        return Util.join(asList((status.equals(OK) ? Str.of("[${1} x ${2}]").with(image.getWidth(), image.getHeight()) : ""), status, path), " ");
    }

    enum Status {
        OK, ERROR_FILE_UNREADABLE, ERROR_NULL_IMAGE, ERROR_EMPTY_IMAGE, NOT_FOUND;

        @Override
        public String toString() {
            return (this.equals(OK) ? "" : this.name());
        }
    }
}
