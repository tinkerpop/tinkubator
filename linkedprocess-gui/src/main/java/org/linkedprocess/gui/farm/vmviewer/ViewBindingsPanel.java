package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.vm.XmppVm;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ViewBindingsPanel extends JPanel implements ActionListener, ListSelectionListener {

    protected JTable bindingsTable;
    protected XmppVm xmppVm;
    protected JTextArea valueTextArea;
    protected int count = 0;
    protected static final String REFRESH = "refresh";


    public ViewBindingsPanel(XmppVm xmppVm) {
        super(new BorderLayout());
        this.xmppVm = xmppVm;
        DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{LinkedProcess.NAME_ATTRIBUTE, LinkedProcess.VALUE_ATTRIBUTE, LinkedProcess.DATATYPE_ATTRIBUTE, "null"});

        this.bindingsTable = new JTable(tableModel) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        this.bindingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.bindingsTable.getSelectionModel().addListSelectionListener(this);
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

        this.valueTextArea = new JTextArea();

        JScrollPane scrollPane1 = new JScrollPane(this.bindingsTable);
        JScrollPane scrollPane2 = new JScrollPane(this.valueTextArea);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(320);
        splitPane.add(scrollPane1);
        splitPane.add(scrollPane2);

        this.add(splitPane, BorderLayout.CENTER);
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
            VMBindings bindings = this.xmppVm.getFarm().getVmScheduler().getAllBindings(this.xmppVm.getFullJid());
            DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
            this.clearAllRows();
            for (String key : bindings.keySet()) {
                TypedValue typedValue = bindings.getTyped(key);
                if (typedValue == null) {
                    tableModel.addRow(new Object[]{key, "", "", true});

                } else {
                    tableModel.addRow(new Object[]{key, typedValue.getValue(), typedValue.getDatatype().abbreviate(), false});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void valueChanged(ListSelectionEvent event) {
        ListSelectionModel listModel = (ListSelectionModel) event.getSource();
        if (this.bindingsTable.getSelectedRow() > -1)
            this.valueTextArea.setText(this.bindingsTable.getValueAt(listModel.getMinSelectionIndex(), 1).toString());
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(REFRESH)) {
            this.refreshBindings();
        }

    }
}