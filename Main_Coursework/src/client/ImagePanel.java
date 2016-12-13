package client;

import shared.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Loads image into JPanel
 *
 * @author Huw Jones
 * @since 20/11/2015
 */
public class ImagePanel extends JPanel implements SwingConstants {

    private BufferedImage image;

    private int verticalAlignment = CENTER;
    private int horizontalAlignment = LEADING;

    public ImagePanel() {
        this.setBackground(Color.WHITE);
    }

    public ImagePanel(BufferedImage image) {
        super();
        this.image = image;
    }

    /**
     * Returns the alignment of the label's contents along the Y axis.
     *
     * @return The value of the verticalAlignment property, one of the
     * following constants defined in <code>SwingConstants</code>:
     * <code>TOP</code>,
     * <code>CENTER</code>, or
     * <code>BOTTOM</code>.
     * @see SwingConstants
     * @see #setVerticalAlignment
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the alignment of the label's contents along the Y axis.
     * <p>
     * The default value of this property is CENTER.
     *
     * @param alignment One of the following constants
     *                  defined in <code>SwingConstants</code>:
     *                  <code>TOP</code>,
     *                  <code>CENTER</code> (the default), or
     *                  <code>BOTTOM</code>.
     * @beaninfo bound: true
     * enum: TOP    SwingConstants.TOP
     * CENTER SwingConstants.CENTER
     * BOTTOM SwingConstants.BOTTOM
     * attribute: visualUpdate true
     * description: The alignment of the label's contents along the Y axis.
     * @see SwingConstants
     * @see #getVerticalAlignment
     */
    public void setVerticalAlignment(int alignment) {
        if (alignment == verticalAlignment) return;
        int oldValue = verticalAlignment;
        verticalAlignment = checkVerticalKey(alignment, "verticalAlignment");
        firePropertyChange("verticalAlignment", oldValue, verticalAlignment);
        repaint();
    }

    /**
     * Returns the alignment of the label's contents along the X axis.
     *
     * @return The value of the horizontalAlignment property, one of the
     * following constants defined in <code>SwingConstants</code>:
     * <code>LEFT</code>,
     * <code>CENTER</code>,
     * <code>RIGHT</code>,
     * <code>LEADING</code> or
     * <code>TRAILING</code>.
     * @see #setHorizontalAlignment
     * @see SwingConstants
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the alignment of the label's contents along the X axis.
     * <p>
     * This is a JavaBeans bound property.
     *
     * @param alignment One of the following constants
     *                  defined in <code>SwingConstants</code>:
     *                  <code>LEFT</code>,
     *                  <code>CENTER</code> (the default for image-only labels),
     *                  <code>RIGHT</code>,
     *                  <code>LEADING</code> (the default for text-only labels) or
     *                  <code>TRAILING</code>.
     * @beaninfo bound: true
     * enum: LEFT     SwingConstants.LEFT
     * CENTER   SwingConstants.CENTER
     * RIGHT    SwingConstants.RIGHT
     * LEADING  SwingConstants.LEADING
     * TRAILING SwingConstants.TRAILING
     * attribute: visualUpdate true
     * description: The alignment of the label's content along the X axis.
     * @see SwingConstants
     * @see #getHorizontalAlignment
     */
    public void setHorizontalAlignment(int alignment) {
        if (alignment == horizontalAlignment) return;
        int oldValue = horizontalAlignment;
        horizontalAlignment = checkHorizontalKey(alignment,
                "horizontalAlignment");
        firePropertyChange("horizontalAlignment",
                oldValue, horizontalAlignment);
        repaint();
    }

    /**
     * Verify that key is a legal value for the horizontalAlignment properties.
     *
     * @param key     the property value to check
     * @param message the IllegalArgumentException detail message
     * @throws IllegalArgumentException if key isn't LEFT, CENTER, RIGHT,
     *                                  LEADING or TRAILING.
     * @see #setHorizontalAlignment
     */
    protected int checkHorizontalKey(int key, String message) {
        if ((key == LEFT) ||
                (key == CENTER) ||
                (key == RIGHT) ||
                (key == LEADING) ||
                (key == TRAILING)) {
            return key;
        } else {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Verify that key is a legal value for the
     * verticalAlignment or verticalTextPosition properties.
     *
     * @param key     the property value to check
     * @param message the IllegalArgumentException detail message
     * @throws IllegalArgumentException if key isn't TOP, CENTER, or BOTTOM.
     * @see #setVerticalAlignment
     */
    protected int checkVerticalKey(int key, String message) {
        if ((key == TOP) || (key == CENTER) || (key == BOTTOM)) {
            return key;
        } else {
            throw new IllegalArgumentException(message);
        }
    }

    public void setImage(BufferedImage image) {
        this.setImage(image, false);
    }

    public void setImage(BufferedImage image, boolean repaint) {
        this.image = image;
        if(image == null) return;
        this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        if (repaint) this.repaint();
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image == null) return;

        Graphics2D g2d = (Graphics2D) g;
        int xpos = 0;
        int ypos = 0;

        BufferedImage image = ImageUtils.getScaledImage(this.image, this.getWidth(), this.getHeight());

        switch (horizontalAlignment){
            case LEFT:
                xpos = 0;
                break;
            case CENTER:
                xpos = (this.getWidth() - image.getWidth()) / 2;
                break;
            case RIGHT:
                xpos = this.getWidth() - image.getWidth();
                break;
        }
        switch (verticalAlignment){
            case TOP:
                ypos = 0;
                break;
            case CENTER:
                ypos = (this.getHeight() - image.getHeight()) / 2;
                break;
            case BOTTOM:
                ypos = this.getHeight() - image.getHeight();
                break;
        }

        g2d.drawImage(image, xpos, ypos, null);
    }
}