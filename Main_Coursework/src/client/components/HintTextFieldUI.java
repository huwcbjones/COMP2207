package client.components;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * TextFieldUI, with input hint (like HTML text inputs)
 * Inspired from: http://stackoverflow.com/a/4962829/5909019
 * and edited/tweaked by Huw Jones
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class HintTextFieldUI extends BasicTextFieldUI implements FocusListener {

    private String hint;
    private boolean hideOnFocus;
    private Color color;

    public HintTextFieldUI(){
        this("");
    }
    public HintTextFieldUI(String hint) {
        this(hint, false);
    }

    public HintTextFieldUI(String hint, boolean hideOnFocus) {
        this(hint, hideOnFocus, Color.lightGray);
    }

    public HintTextFieldUI(String hint, boolean hideOnFocus, Color color) {
        this.hint = hint;
        this.hideOnFocus = hideOnFocus;
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.repaint();
    }

    private void repaint() {
        if (this.getComponent() != null) {
            this.getComponent().repaint();
        }
    }

    public boolean isHideOnFocus() {
        return this.hideOnFocus;
    }

    public void setHideOnFocus(boolean hideOnFocus) {
        this.hideOnFocus = hideOnFocus;
        this.repaint();
    }

    public String getHint() {
        return this.hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
        this.repaint();
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.hideOnFocus) this.repaint();

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.hideOnFocus) this.repaint();
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.getComponent().addFocusListener(this);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.getComponent().removeFocusListener(this);
    }

    @Override
    protected void paintSafely(Graphics g) {
        super.paintSafely(g);
        JTextComponent comp = this.getComponent();
        if (this.hint != null && comp.getText().length() == 0 && !(this.hideOnFocus && comp.hasFocus())) {
            if (this.color != null) {
                g.setColor(this.color);
            } else {
                g.setColor(comp.getForeground().brighter().brighter().brighter());
            }
            int padding = (comp.getHeight() - comp.getFont().getSize()) / 2;
            g.drawString(this.hint, 3, comp.getHeight() - padding - 2);
        }
    }
}
