package org.linkedprocess.gui.villein;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.CountrysideStruct;
import org.linkedprocess.xmpp.villein.XmppVillein;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 5:05:38 PM
 */
public class ViewCountrysideFarmsPanel extends JPanel implements ActionListener {

    protected JTable farmsTable;
    protected static final String REFRESH = "refresh";
    protected CountrysideStruct countrysideStruct;
    protected VilleinGui villeinGui;
    protected Document discoItemsDocument;
    protected DiscoverItems discoItems;


    public ViewCountrysideFarmsPanel(CountrysideStruct countrysideStruct, VilleinGui villeinGui) {
        super(new BorderLayout());
        this.countrysideStruct = countrysideStruct;
        this.villeinGui = villeinGui;

        DefaultTableModel tableModel1 = new DefaultTableModel(new Object[][]{}, new Object[]{"farm jid", "farm name"});
        this.farmsTable = new JTable(tableModel1) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        this.farmsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.farmsTable.setFillsViewportHeight(true);
        this.farmsTable.setRowHeight(20);
        this.farmsTable.setFont(new Font(null, Font.PLAIN, 13));
        this.farmsTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        this.farmsTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton(REFRESH);
        buttonPanel.add(refreshButton);
        refreshButton.addActionListener(this);


        try {
            this.generateDiscoItemsDocument();
            this.refreshCountrysideFarms();
        } catch (Exception e) {
            XmppVillein.LOGGER.severe(e.getMessage());
        }

        JScrollPane scrollPane1 = new JScrollPane(this.farmsTable);

        this.add(scrollPane1, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(REFRESH)) {
            try {
                this.generateDiscoItemsDocument();
                this.refreshCountrysideFarms();
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

    private void generateDiscoItemsDocument() throws XMPPException, JDOMException, IOException {
        ServiceDiscoveryManager discoManager = this.villeinGui.getXmppVillein().getDiscoManager();
        this.discoItems = discoManager.discoverItems(this.countrysideStruct.getFullJid());
        PacketCollector collector = this.villeinGui.getXmppVillein().getConnection().createPacketCollector(new PacketTypeFilter(DiscoverItems.class));
        this.discoItemsDocument = LinkedProcess.createXMLDocument(collector.nextResult().toXML());
        collector.cancel();
    }


    private void refreshCountrysideFarms() {
        this.clearAllRows(this.farmsTable);
        if (null != this.discoItemsDocument) {
            Element queryElement = discoItemsDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_ITEMS_NAMESPACE));
            if (null != queryElement) {
                DefaultTableModel tableModel = (DefaultTableModel) this.farmsTable.getModel();
                for (Element itemElement : (java.util.List<Element>) queryElement.getChildren(LinkedProcess.ITEM_TAG, Namespace.getNamespace(LinkedProcess.DISCO_ITEMS_NAMESPACE))) {
                    tableModel.addRow(new Object[]{itemElement.getAttributeValue(LinkedProcess.JID_ATTRIBUTE), itemElement.getAttributeValue(LinkedProcess.NAME_ATTRIBUTE)});
                }
            }
        }
    }
}
