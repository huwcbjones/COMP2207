package shared.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Image Utils
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public class ImageUtils {

    /**
     * Gets scaled version of a buffered image
     * @param image Image to scale
     * @param width Max width
     * @param height Max height
     * @return
     */
    public static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
        double scale = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
        Double newWidth = scale * image.getWidth();
        Double newHeight = scale * image.getHeight();
        Double xPos = ((double) width - newWidth) / 2d;
        Double yPos = ((double) height - newHeight) / 2d;

        Image tmp = image.getScaledInstance(newWidth.intValue(), newHeight.intValue(), Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(tmp, xPos.intValue(), yPos.intValue(), null);
        g2d.dispose();

        return scaledImage;
    }

    /**
     * Converts a BufferedImage (not serializable) to a byte array (serializable)
     *
     * @param image Buffered Image
     * @return byte array
     */
    public static byte[] imageToBytes(BufferedImage image) throws IOException {
        if (image == null) {
            return new byte[0];
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        output.flush();
        return output.toByteArray();
    }

    /**
     * Converts a byte array (serializable) to a BufferedImage (not serializable)
     *
     * @param bytes byte array
     * @return Buffered Image
     */
    public static BufferedImage bytesToImage(byte[] bytes) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        return ImageIO.read(input);
    }
}
