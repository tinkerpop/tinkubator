package org.linkedprocess.gui.villein;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.Proxy;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ViewFarmConfigurationPanel extends JPanel implements ListSelectionListener, ActionListener {

    protected JTable configurationTable;
    protected JTable valuesTable;
    protected int count = 0;
    protected static final String REFRESH = "refresh";
    protected FarmProxy farmProxy;
    protected VilleinGui villeinGui;
    protected JList featuresList;


    public ViewFarmConfigurationPanel(FarmProxy farmProxy, VilleinGui villeinGui) {
        super(new BorderLayout());
        this.farmProxy = farmProxy;
        this.villeinGui = villeinGui;

        DefaultTableModel tableModel1 = new DefaultTableModel(new Object[][]{}, new Object[]{LinkedProcess.VAR_ATTRIBUTE, LinkedProcess.LABEL_ATTRIBUTE, LinkedProcess.TYPE_ATTRIBUTE});
        this.configurationTable = new JTable(tableModel1) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        this.configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.configurationTable.getSelectionModel().addListSelectionListener(this);
        this.configurationTable.setFillsViewportHeight(true);
        this.configurationTable.setRowHeight(20);
        this.configurationTable.setFont(new Font(null, Font.PLAIN, 13));
        this.configurationTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        this.configurationTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        this.configurationTable.getColumnModel().getColumn(2).setPreferredWidth(50);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton(REFRESH);
        buttonPanel.add(refreshButton);
        refreshButton.addActionListener(this);

        DefaultTableModel tableModel2 = new DefaultTableModel(new Object[][]{}, new Object[]{LinkedProcess.VALUE_TAG, LinkedProcess.LABEL_ATTRIBUTE, LinkedProcess.OPTION_TAG});
        this.valuesTable = new JTable(tableModel2) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        this.valuesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        this.valuesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        this.valuesTable.getColumnModel().getColumn(2).setPreferredWidth(20);
        this.valuesTable.setFillsViewportHeight(true);
        this.valuesTable.setRowHeight(20);
        this.valuesTable.setFont(new Font(null, Font.PLAIN, 13));

        DefaultListModel listModel = new DefaultListModel();
        this.featuresList = new JList(listModel);
        this.featuresList.setVisibleRowCount(3);
        this.featuresList.setBorder(BorderFactory.createTitledBorder("Farm Features"));


        this.refreshFarmFeatures();
        this.refreshFarmConfiguration();


        JScrollPane scrollPane1 = new JScrollPane(this.configurationTable);
        JScrollPane scrollPane2 = new JScrollPane(this.valuesTable);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.add(scrollPane1);
        splitPane.add(scrollPane2);
        JScrollPane scrollPane3 = new JScrollPane(this.featuresList);

        this.add(scrollPane3, BorderLayout.NORTH);
        this.add(splitPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }


    public void valueChanged(ListSelectionEvent event) {
        ListSelectionModel listModel = (ListSelectionModel) event.getSource();
        if (this.configurationTable.getSelectedRow() > -1) {
            String fieldVar = this.configurationTable.getValueAt(listModel.getMinSelectionIndex(), 0).toString();
            Proxy.Field field = farmProxy.getField(fieldVar);
            if (null != field) {
                DefaultTableModel tableModel = (DefaultTableModel) this.valuesTable.getModel();
                this.clearAllRows(this.valuesTable);
                Set<String> values = field.getValues();
                for (String value : values) {
                    tableModel.addRow(new Object[]{value, field.getLabel(), field.getOption()});
                }
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(REFRESH)) {
            try {
                this.farmProxy.refreshDiscoInfo();
                this.refreshFarmFeatures();
                this.refreshFarmConfiguration();
            } catch (Exception e) {
                XmppVillein.LOGGER.warning(e.getMessage());
            }
        }

    }

    private void clearAllRows(JTable table) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    private void refreshFarmFeatures() {
        DefaultListModel listModel = (DefaultListModel) this.featuresList.getModel();
        listModel.clear();
        for (String feature : this.farmProxy.getFeatures()) {
            listModel.addElement(feature);
        }
    }

    private void refreshFarmConfiguration() {
        DefaultTableModel tableModel = (DefaultTableModel) this.configurationTable.getModel();
        this.clearAllRows(this.configurationTable);
        java.util.List<Proxy.Field> fields = this.farmProxy.getFields();
        for (Proxy.Field field : fields) {
            tableModel.addRow(new Object[]{field.getVariable(), field.getLabel(), field.getType()});
        }
    }


}
