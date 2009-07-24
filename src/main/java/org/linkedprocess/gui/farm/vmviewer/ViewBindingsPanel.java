package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.gui.villein.vmcontrol.VmControlFrame;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.vm.XmppVirtualMachine;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.TypedValue;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * User: marko
 * Date: Jul 24, 2009
 * Time: 4:44:14 PM
 */
public class ViewBindingsPanel  extends JPanel implements ActionListener {

    protected JTable bindingsTable;
    protected XmppVirtualMachine xmppVirtualMachine;
    protected int count = 0;
    protected static final String REFRESH = "refresh";
    protected static final String NULL = "null";


    public ViewBindingsPanel(XmppVirtualMachine xmppVirtualMachine) {
        super(new BorderLayout());
        this.xmppVirtualMachine = xmppVirtualMachine;
        this.setOpaque(false);
        DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{LinkedProcess.NAME_ATTRIBUTE, LinkedProcess.VALUE_ATTRIBUTE, LinkedProcess.DATATYPE_ATTRIBUTE, "null"});
        this.bindingsTable = new JTable(tableModel)  {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        JScrollPane scrollPane = new JScrollPane(this.bindingsTable);
        this.bindingsTable.setFillsViewportHeight(true);
        this.bindingsTable.setRowHeight(20);
        this.bindingsTable.setFont(new Font(null, Font.PLAIN, 13));
        this.bindingsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        this.bindingsTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        this.bindingsTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        this.bindingsTable.getColumnModel().getColumn(3).setPreferredWidth(50);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton(REFRESH);
        buttonPanel.add(refreshButton);
        refreshButton.addActionListener(this);
        buttonPanel.setOpaque(false);
        scrollPane.setOpaque(false);
        JLabel helpLabel = new JLabel("Manage bindings by highlighting particular rows to set and get.");
        helpLabel.setOpaque(false);
        this.add(helpLabel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

    }

    public void clearAllRows() {
        DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    public void refreshBindings() {
        try {
            VMBindings bindings = this.xmppVirtualMachine.getFarm().getVMScheduler().getAllBindings(this.xmppVirtualMachine.getFullJid());
            DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
            this.clearAllRows();
            for(String key : bindings.keySet()) {
                TypedValue typedValue = bindings.getTyped(key);
                if(typedValue == null) {
                    tableModel.addRow(new Object[]{key, "","", true});

                } else {
                    tableModel.addRow(new Object[]{key, typedValue.getValue(), typedValue.getDatatype().abbreviate(), false});
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand().equals(REFRESH)) {
            this.refreshBindings();
        }

    }
}