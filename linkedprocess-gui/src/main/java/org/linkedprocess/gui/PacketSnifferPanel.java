package org.linkedprocess.gui;

import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.LinkedProcess;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PacketSnifferPanel extends JPanel implements ListSelectionListener, ActionListener, PacketInterceptor, PacketListener {

    protected JTable packetTable;
    protected JTextArea packetTextArea;
    protected JTextField maxSavedField;
    protected List<Packet> packetList;
    protected String jid;

    protected final static String CLEAR = "clear";

    public PacketSnifferPanel(String jid) {
        super(new BorderLayout());
        this.jid = jid;
        DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{"i/o", "type", "from", "to"});
        this.packetTable = new JTable(tableModel) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        this.packetTable.setFillsViewportHeight(true);

        this.packetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.packetTable.getSelectionModel().addListSelectionListener(this);
        this.packetTable.getColumnModel().getColumn(0).setPreferredWidth(25);
        this.packetTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        this.packetTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        this.packetTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        this.packetTable.getColumnModel().getColumn(0).setCellRenderer(new PacketSnifferTableCellRenderer());

        this.packetTextArea = new JTextArea();
        this.packetTextArea.setEditable(false);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton clearButton = new JButton(CLEAR);
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftButtonPanel.add(clearButton);
        clearButton.addActionListener(this);
        this.maxSavedField = new JTextField(4);
        rightButtonPanel.add(new JLabel("max saved packets"));
        rightButtonPanel.add(this.maxSavedField);
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        JScrollPane scrollPane1 = new JScrollPane(this.packetTable);
        JScrollPane scrollPane2 = new JScrollPane(this.packetTextArea);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.add(scrollPane1);
        splitPane.add(scrollPane2);
        splitPane.setDividerLocation(250);
        this.packetList = new ArrayList<Packet>();
        this.add(splitPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void addPacket(Packet packet) {
        DefaultTableModel tableModel = (DefaultTableModel) this.packetTable.getModel();
        int max = Integer.MAX_VALUE;
        if (this.maxSavedField.getText().length() > 0) {
            try {
                max = Integer.parseInt(this.maxSavedField.getText());
                int rowCount = tableModel.getRowCount();
                if (rowCount + 1 > max) {
                    this.clearBottomRows(rowCount + 1 - max);
                }

            } catch (NumberFormatException e) {
                this.maxSavedField.setText("");
            }
        }


        if (max > 0) {
            Integer sentOrRecieved;
            if (packet.getFrom() != null && packet.getFrom().equals(this.jid)) {
                sentOrRecieved = 0;
            } else if (packet.getTo() != null && packet.getTo().equals(this.jid)) {
                sentOrRecieved = 1;
            } else {
                sentOrRecieved = 0;
            }
            tableModel.addRow(new Object[]{sentOrRecieved, LinkedProcess.getBareClassName(packet.getClass()), packet.getFrom(), packet.getTo()});
            this.packetList.add(packet);
        }

    }

    public void clearAllRows() {
        DefaultTableModel tableModel = (DefaultTableModel) this.packetTable.getModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        this.packetList.clear();
    }

    public void clearBottomRows(int bottomNumber) {
        DefaultTableModel tableModel = (DefaultTableModel) this.packetTable.getModel();
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < bottomNumber; i++) {
            if (i < rowCount) {
                tableModel.removeRow(0);
                this.packetList.remove(0);
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(CLEAR)) {
            this.clearAllRows();
            this.packetTextArea.setText("");
        }
    }

    public void valueChanged(ListSelectionEvent event) {
        ListSelectionModel listModel = (ListSelectionModel) event.getSource();
        try {
            if (packetTable.getSelectedRow() > -1)
                this.packetTextArea.setText(LinkedProcess.createPrettyXML(packetList.get(listModel.getMinSelectionIndex()).toXML()).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "").trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void interceptPacket(Packet packet) {
        this.addPacket(packet);
    }

    public void processPacket(Packet packet) {
        this.addPacket(packet);
    }

    private class PacketSnifferTableCellRenderer extends DefaultTableCellRenderer {
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

}
