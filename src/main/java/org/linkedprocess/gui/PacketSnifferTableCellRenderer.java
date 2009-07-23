package org.linkedprocess.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * User: marko
 * Date: Jul 18, 2009
 * Time: 5:18:38 PM
 */
public class PacketSnifferTableCellRenderer extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if ((Integer) table.getValueAt(row, 0) == 0)
            this.setIcon(ImageHolder.letterIcon);
        else
            this.setIcon(ImageHolder.mailboxIcon);
        this.setName("");
        this.setText("");

        return this;
    }

}
