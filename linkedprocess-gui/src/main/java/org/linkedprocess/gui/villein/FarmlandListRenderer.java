package org.linkedprocess.gui.villein;

import org.linkedprocess.gui.ImageHolder;

import javax.swing.*;
import java.awt.*;

/**
 * User: marko
 * Date: Jul 31, 2009
 * Time: 11:41:34 PM
 */
public class FarmlandListRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setIcon(ImageHolder.farmlandIcon);
        return label;
    }
}
