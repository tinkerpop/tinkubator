package org.linkedprocess.gui;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * User: marko
 * Date: Jul 19, 2009
 * Time: 9:44:45 AM
 */
public class RosterPanel extends JPanel implements ActionListener, ListSelectionListener {

    protected JTable rosterTable;
    protected Roster roster;

    protected JLabel jidLabel;
    protected JTextField nameTextField;
    protected JLabel subscriptionLabel;


    protected final static String ADD = "add";
    protected final static String REMOVE = "remove";
    protected final static String UPDATE = "update";

    protected final static String BUFFER_SPACE = " ";

    public RosterPanel(Roster roster) {
        super(new BorderLayout());
        this.roster = roster;
        DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{"jid", "name"});
        this.rosterTable = new JTable(tableModel) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        this.rosterTable.setFillsViewportHeight(true);
        this.rosterTable.setRowHeight(18);
        this.rosterTable.setFont(new Font(null, Font.PLAIN, 12));
        this.rosterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.rosterTable.getSelectionModel().addListSelectionListener(this);
        this.rosterTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        this.rosterTable.getColumnModel().getColumn(1).setPreferredWidth(10);
        JScrollPane scrollPane1 = new JScrollPane(this.rosterTable);

        JPanel metadataPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        this.jidLabel = new JLabel();
        this.nameTextField = new JTextField(25);
        this.nameTextField.setEnabled(false);
        this.subscriptionLabel = new JLabel();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.1;
        metadataPanel.add(new JLabel("jid: "), c);
        c.gridx = 1;
        c.gridy = 0;
        metadataPanel.add(this.jidLabel, c);
        c.gridx = 0;
        c.gridy = 1;
        metadataPanel.add(new JLabel("name: "), c);
        c.gridx = 1;
        c.gridy = 1;
        metadataPanel.add(nameTextField, c);
        c.gridx = 0;
        c.gridy = 2;
        metadataPanel.add(new JLabel("subscription: "), c);
        c.gridx = 1;
        c.gridy = 2;
        metadataPanel.add(this.subscriptionLabel, c);
        c.gridx = 0;
        c.weighty = 0.3;
        c.gridy = 3;
        JButton updateButton = new JButton(UPDATE);
        updateButton.addActionListener(this);
        metadataPanel.add(updateButton,c);

        JScrollPane scrollPane2 = new JScrollPane(metadataPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.add(scrollPane1);
        splitPane.add(scrollPane2);
        splitPane.setDividerLocation(250);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton(ImageHolder.addIcon);
        addButton.setActionCommand(ADD);
        addButton.addActionListener(this);
        JButton removeButton = new JButton(ImageHolder.removeIcon);
        removeButton.setActionCommand(REMOVE);
        removeButton.addActionListener(this);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        this.add(splitPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.populateRosterTable();

    }

    public void valueChanged(ListSelectionEvent event) {
        ListSelectionModel listModel = (ListSelectionModel) event.getSource();
        DefaultTableModel tableModel = (DefaultTableModel) this.rosterTable.getModel();
        try {
            int row = listModel.getMinSelectionIndex();
            if(row > -1) {
                RosterEntry entry = this.roster.getEntry(tableModel.getValueAt(row, 0).toString());
                if(null != entry) {
                    this.jidLabel.setText(BUFFER_SPACE + entry.getUser());
                    this.nameTextField.setEnabled(true);
                    this.nameTextField.setText(entry.getName());
                    this.subscriptionLabel.setText(BUFFER_SPACE + entry.getType().toString());
                }
            } else {
                this.jidLabel.setText("");
                this.nameTextField.setText("");
                this.nameTextField.setEnabled(false);
                this.subscriptionLabel.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAllRows() {
        DefaultTableModel tableModel = (DefaultTableModel) this.rosterTable.getModel();
        while(tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    public void populateRosterTable() {
        this.clearAllRows();
        this.roster.reload();
        DefaultTableModel tableModel = (DefaultTableModel) this.rosterTable.getModel();
        for(RosterEntry entry : this.roster.getEntries()) {
            //System.out.println(entry.getDatatype() + entry.getUser() +entry.getName());
            tableModel.addRow(new Object[]{entry.getUser(), entry.getName()});
        }
    }

    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand().equals(ADD)) {
            DefaultTableModel tableModel = (DefaultTableModel) this.rosterTable.getModel();
            String value = JOptionPane.showInputDialog(null, "enter host jid", "add host", JOptionPane.QUESTION_MESSAGE);
            if(value != null && value.length() > 0) {
                try {
                    this.roster.createEntry(value, null, null);
                    tableModel.addRow(new Object[]{value, null});
                } catch(XMPPException e) {
                    e.printStackTrace();
                }
            }

        } else if(event.getActionCommand().equals(REMOVE)) {
            int row = this.rosterTable.getSelectedRow();
            if(row > -1) {
                DefaultTableModel tableModel = (DefaultTableModel) this.rosterTable.getModel();
                RosterEntry entry = this.roster.getEntry(tableModel.getValueAt(row, 0).toString());
                if(null != entry) {
                    try {
                        this.roster.removeEntry(entry);
                        tableModel.removeRow(row);
                    }
                    catch(XMPPException e) {
                        e.printStackTrace();
                    }
                }

            }
        } else if(event.getActionCommand().equals(UPDATE)) {
            int row = this.rosterTable.getSelectedRow();
            if(row > -1) {
                DefaultTableModel tableModel = (DefaultTableModel) this.rosterTable.getModel();
                RosterEntry entry = this.roster.getEntry(tableModel.getValueAt(row, 0).toString());
                String name = this.nameTextField.getText();
                if(null != name && name.length() > 0) {
                    entry.setName(name);
                    tableModel.setValueAt(name, row, 1);
                }
            }
        }
    }
}
