package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.xmpp.vm.ManageBindings;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 11:57:06 PM
 */
public class BindingsPanel extends JPanel implements ActionListener, TableModelListener {

    protected JTable bindingsTable;
    protected VmFrame vmFrame;
    protected int count = 0;
    protected static final String GET = "get";
    protected static final String SET = "set";
    protected static final String ADD = "add";
    protected static final String REMOVE = "remove";


    public BindingsPanel(VmFrame vmFrame) {
        super(new BorderLayout());
        this.vmFrame = vmFrame;
        this.setOpaque(false);
        DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{LinkedProcess.NAME_ATTRIBUTE, LinkedProcess.VALUE_ATTRIBUTE});
        tableModel.addTableModelListener(this);
        this.bindingsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(this.bindingsTable);
        this.bindingsTable.setFillsViewportHeight(true);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton getButton = new JButton(GET);
        JButton setButton = new JButton(SET);
        JButton addButton = new JButton(ImageHolder.addIcon);
        addButton.setActionCommand(ADD);
        JButton removeButton = new JButton(ImageHolder.removeIcon);
        removeButton.setActionCommand(REMOVE);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(new JSeparator());
        buttonPanel.add(getButton);
        buttonPanel.add(setButton);
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        setButton.addActionListener(this);
        getButton.addActionListener(this);
        buttonPanel.setOpaque(false);
        scrollPane.setOpaque(false);

        JLabel helpLabel = new JLabel("Manage bindings by highlighting particular rows to set and get.");
        helpLabel.setOpaque(false);
        this.add(helpLabel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);


    }

    public void tableChanged(TableModelEvent event) {
        if (event.getType() == TableModelEvent.UPDATE || event.getType() == TableModelEvent.INSERT) {
            DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
            int row = event.getFirstRow();
            int column = event.getColumn();
            if (column == 0) {
                String editedName = (String) tableModel.getValueAt(row, column);
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (i != row) {
                        if (tableModel.getValueAt(i, 0).equals(editedName)) {
                            tableModel.setValueAt("name_already_exists-" + this.count, row, column);
                        }
                    }
                }
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
        if (event.getActionCommand().equals(ADD)) {
            tableModel.addRow(new Object[]{LinkedProcess.NAME_ATTRIBUTE + "-" + this.count, LinkedProcess.VALUE_ATTRIBUTE + "-" + this.count});
            count++;
        } else if (event.getActionCommand().equals(REMOVE)) {
            if (this.bindingsTable.getSelectedRow() > -1) {
                int smallestRow = Integer.MAX_VALUE;
                int x = 0;
                for (int row : this.bindingsTable.getSelectedRows()) {
                    tableModel.removeRow(row - x);
                    if (row - x < smallestRow)
                        smallestRow = row - x;
                    x++;
                }
                if (smallestRow > 0 && smallestRow != Integer.MAX_VALUE)
                    this.bindingsTable.setRowSelectionInterval(smallestRow - 1, smallestRow - 1);
                else if (tableModel.getRowCount() > 0)
                    this.bindingsTable.setRowSelectionInterval(0, 0);
            }

        } else if (event.getActionCommand().equals(GET)) {
            try {
                System.out.println(LinkedProcess.createPrettyXML(this.getManageBindings(IQ.Type.GET).toXML()));
            } catch (Exception e) {
            }
        } else if (event.getActionCommand().equals(SET)) {
            try {
                System.out.println(LinkedProcess.createPrettyXML(this.getManageBindings(IQ.Type.SET).toXML()));
            } catch (Exception e) {
            }
        }
    }

    public ManageBindings getManageBindings(IQ.Type setOrGet) {
        DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setType(setOrGet);
        manageBindings.setTo(this.vmFrame.getVmStruct().getFullJid());
        manageBindings.setVmPassword(this.vmFrame.getVmStruct().getVmPassword());
        for (int row : this.bindingsTable.getSelectedRows()) {
            manageBindings.addBinding((String) tableModel.getValueAt(row, 0), (String) tableModel.getValueAt(row, 1));
        }
        return manageBindings;
    }

    public void handleIncomingManageBindings(ManageBindings manageBindings) {
        DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
        if (manageBindings.getType() == IQ.Type.RESULT) {
            for (String name : manageBindings.getBindings().keySet()) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 0).equals(name)) {
                        tableModel.setValueAt(manageBindings.getBinding(name), i, 1);
                    }
                }
            }
        }
    }

    /*public void paintComponent(Graphics g) {
        g.drawImage(ImageHolder.cowBackground.getImage(), 0, 0, null);
        super.paintComponent(g);
    }*/
}
