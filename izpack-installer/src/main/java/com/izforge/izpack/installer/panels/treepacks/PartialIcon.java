package com.izforge.izpack.installer.panels.treepacks;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

/**
 * Special checkbox icon which shows partially selected nodes.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class PartialIcon implements Icon {
    protected int getControlSize() {
        return 13;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        int controlSize = getControlSize();
        g.setColor(MetalLookAndFeel.getControlShadow());
        g.fillRect(x, y, controlSize - 1, controlSize - 1);
        drawBorder(g, x, y, controlSize, controlSize);

        g.setColor(Color.green);
        drawCheck(c, g, x, y);
    }

    private void drawBorder(Graphics g, int x, int y, int w, int h) {
        g.translate(x, y);

        // outer frame rectangle
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        g.setColor(new Color(0.4f, 0.4f, 0.4f));
        g.drawRect(0, 0, w - 2, h - 2);

        // middle frame
        g.setColor(MetalLookAndFeel.getControlHighlight());
        g.setColor(new Color(0.6f, 0.6f, 0.6f));
        g.drawRect(1, 1, w - 2, h - 2);

        // background
        g.setColor(new Color(0.99f, 0.99f, 0.99f));
        g.fillRect(2, 2, w - 3, h - 3);

        //some extra lines for FX
        g.setColor(MetalLookAndFeel.getControl());
        g.drawLine(0, h - 1, 1, h - 2);
        g.drawLine(w - 1, 0, w - 2, 1);
        g.translate(-x, -y);
    }

    protected void drawCheck(Component c, Graphics g, int x, int y) {
        int controlSize = getControlSize();
        g.setColor(new Color(0.0f, 0.7f, 0.0f));

        g.fillOval(x + controlSize / 2 - 2, y + controlSize / 2 - 2, 6, 6);
    }

    public int getIconWidth() {
        return getControlSize();
    }

    public int getIconHeight() {
        return getControlSize();
    }
}
