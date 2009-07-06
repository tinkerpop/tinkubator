package gov.lanl.cnls.linkedprocess.gui;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;

/**
 * User: marko
 * Date: Jul 6, 2009
 * Time: 3:08:33 PM
 */
public class JTreeImage extends JTree {

    private Image backgroundImage;

    public JTreeImage(MutableTreeNode node, ImageIcon backgroundImage) {
        super(node);
        this.backgroundImage = backgroundImage.getImage();
        Dimension size = new Dimension(this.backgroundImage.getWidth(null), this.backgroundImage.getHeight(null));
        this.setPreferredSize(size);
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        this.setSize(size);
        this.setLayout(null);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(this.backgroundImage, 0, 0, null);
        super.paintComponent(g);
    }
}
