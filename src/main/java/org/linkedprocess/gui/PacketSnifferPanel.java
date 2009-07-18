package org.linkedprocess.gui;

import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.farm.SpawnVm;
import org.linkedprocess.LinkedProcess;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * User: marko
 * Date: Jul 18, 2009
 * Time: 11:20:09 AM
 */
public class PacketSnifferPanel extends JPanel implements ListSelectionListener, ActionListener {

    protected JTable packetTable;
    protected JTextArea packetTextArea;
    protected List<Packet> packetList;

    public PacketSnifferPanel() {
        super(new BorderLayout());
        DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{"type", "from", "to"});
        this.packetTable = new JTable(tableModel);
        this.packetTable.setFillsViewportHeight(true);

        this.packetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.packetTable.getSelectionModel().addListSelectionListener(this);
        this.packetTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        this.packetTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        this.packetTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        this.packetTextArea = new JTextArea(5, 8);
        this.packetTextArea.setEditable(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton clearButton = new JButton("clear");
        buttonPanel.add(clearButton);
        clearButton.addActionListener(this);

        JScrollPane scrollPane1 = new JScrollPane(this.packetTable);
        JScrollPane scrollPane2 = new JScrollPane(this.packetTextArea);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.add(scrollPane1);
        splitPane.add(scrollPane2);
        splitPane.setDividerLocation(180);
        this.packetList = new ArrayList<Packet>();
        this.add(splitPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void addPacket(Packet packet) {
        DefaultTableModel tableModel = (DefaultTableModel) this.packetTable.getModel();
        tableModel.addRow(new Object[]{LinkedProcess.getBareClassName(packet.getClass()), packet.getFrom(), packet.getTo()});
        this.packetList.add(packet);

    }

    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand().equals("clear")) {
            DefaultTableModel tableModel = (DefaultTableModel) this.packetTable.getModel();
            while(tableModel.getRowCount() > 0) {
                tableModel.removeRow(0);
            }
            this.packetList.clear();
            this.packetTextArea.setText("");
        }
    }

    public void valueChanged(ListSelectionEvent event) {
        ListSelectionModel listModel = (ListSelectionModel)event.getSource();
        try {
            if(packetTable.getSelectedRow() > -1)
                this.packetTextArea.setText(LinkedProcess.createPrettyXML(packetList.get(listModel.getMinSelectionIndex()).toXML()));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame("PacketSniffer");
        PacketSnifferPanel packetSniffer = new PacketSnifferPanel();
        SpawnVm spawnVm = new SpawnVm();
        spawnVm.setTo("jid@jid.com");
        spawnVm.setFrom("jid@jid.com2");

        //packetSniffer.addPacket(spawnVm);
        frame.getContentPane().add(packetSniffer);
        frame.pack();
        frame.setVisible(true);



             
    }
}
