package org.linkedprocess.gui.villein;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.XmppVillein;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * User: marko
 * Date: Jul 28, 2009
 * Time: 11:15:56 PM
 */
public class ViewFarmConfigurationPanel extends JPanel implements ListSelectionListener, ActionListener {

    protected JTable configurationTable;
    protected JTable valuesTable;
    protected int count = 0;
    protected static final String REFRESH = "refresh";
    protected FarmProxy farmProxy;
    protected VilleinGui villeinGui;
    protected Document discoInfoDocument;
    protected DiscoverInfo discoInfo;
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

        try {
            this.generateDiscoInfoDocument();
            this.refreshFarmConfiguration();
            this.refreshFarmFeatures();
        } catch (Exception e) {
            XmppVillein.LOGGER.severe(e.getMessage());
        }


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
            Element fieldElement = getFieldElement(fieldVar);
            if (null != fieldElement) {
                DefaultTableModel tableModel = (DefaultTableModel) this.valuesTable.getModel();
                this.clearAllRows(this.valuesTable);
                java.util.List<Element> valuesList = (java.util.List<Element>) fieldElement.getChildren(LinkedProcess.VALUE_TAG, Namespace.getNamespace(LinkedProcess.X_JABBER_DATA_NAMESPACE));
                for (Element valueElement : valuesList) {
                    tableModel.addRow(new Object[]{valueElement.getText(), null, false});
                }
                for (Element optionElement : (java.util.List<Element>) fieldElement.getChildren(LinkedProcess.OPTION_TAG, Namespace.getNamespace(LinkedProcess.X_JABBER_DATA_NAMESPACE))) {
                    for (Element valueElement : (java.util.List<Element>) optionElement.getChildren(LinkedProcess.VALUE_TAG, Namespace.getNamespace(LinkedProcess.X_JABBER_DATA_NAMESPACE))) {
                        tableModel.addRow(new Object[]{valueElement.getText(), optionElement.getAttributeValue(LinkedProcess.LABEL_ATTRIBUTE), true});
                    }
                }
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(REFRESH)) {
            try {
                this.generateDiscoInfoDocument();
                this.refreshFarmConfiguration();
                this.refreshFarmFeatures();
            } catch (Exception e) {
                XmppVillein.LOGGER.severe(e.getMessage());
            }
        }

    }

    private void clearAllRows(JTable table) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    private void generateDiscoInfoDocument() throws XMPPException, JDOMException, IOException {
        ServiceDiscoveryManager discoManager = this.villeinGui.getXmppVillein().getDiscoManager();
        this.discoInfo = discoManager.discoverInfo(farmProxy.getFullJid());
        this.discoInfoDocument = LinkedProcess.createXMLDocument(this.discoInfo.toXML());
    }

    private Set<String> getFeatures() {
        Set<String> features = new HashSet<String>();
        if (null != this.discoInfoDocument) {
            Element queryElement = this.discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                for (Element featureElement : (java.util.List<Element>) queryElement.getChildren(LinkedProcess.FEATURE_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE))) {
                    features.add(featureElement.getAttributeValue(LinkedProcess.VAR_ATTRIBUTE));
                }
            }
        }
        return features;
    }

    private Element getFieldElement(String fieldVar) {
        if (null != this.discoInfoDocument) {
            Element queryElement = discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                Namespace xNamespace = Namespace.getNamespace(LinkedProcess.X_JABBER_DATA_NAMESPACE);
                Element xElement = queryElement.getChild(LinkedProcess.X_TAG, xNamespace);
                if (null != xElement) {
                    for (Element fieldElement : (java.util.List<Element>) xElement.getChildren(LinkedProcess.FIELD_TAG, xNamespace)) {
                        String var = fieldElement.getAttributeValue(LinkedProcess.VAR_ATTRIBUTE);
                        if (null != var && var.equals(fieldVar)) {
                            return fieldElement;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void refreshFarmConfiguration() {
        this.clearAllRows(this.configurationTable);
        if (null != this.discoInfoDocument) {
            Element queryElement = discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                Namespace xNamespace = Namespace.getNamespace(LinkedProcess.X_JABBER_DATA_NAMESPACE);
                Element xElement = queryElement.getChild(LinkedProcess.X_TAG, xNamespace);
                if (null != xElement) {
                    DefaultTableModel tableModel = (DefaultTableModel) this.configurationTable.getModel();
                    for (Element fieldElement : (java.util.List<Element>) xElement.getChildren(LinkedProcess.FIELD_TAG, xNamespace)) {
                        tableModel.addRow(new Object[]{fieldElement.getAttributeValue(LinkedProcess.VAR_ATTRIBUTE), fieldElement.getAttributeValue(LinkedProcess.LABEL_ATTRIBUTE), fieldElement.getAttributeValue(LinkedProcess.TYPE_ATTRIBUTE)});
                    }
                }
            }
        }
    }

    private void refreshFarmFeatures() {
        DefaultListModel listModel = (DefaultListModel) this.featuresList.getModel();
        listModel.clear();
        for (String feature : this.getFeatures()) {
            listModel.addElement(feature);
        }
    }


}
