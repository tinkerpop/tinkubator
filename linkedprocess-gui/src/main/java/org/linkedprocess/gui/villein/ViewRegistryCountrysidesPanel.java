package org.linkedprocess.gui.villein;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.proxies.RegistryProxy;
import org.linkedprocess.xmpp.villein.proxies.CountrysideProxy;
import org.linkedprocess.xmpp.villein.XmppVillein;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 5:05:38 PM
 */
public class ViewRegistryCountrysidesPanel extends JPanel implements ActionListener {

    protected JList farmlandList;
    protected static final String REFRESH = "refresh";
    protected static final String SUBSCRIBE = "subscribe";
    protected RegistryProxy registryProxy;
    protected VilleinGui villeinGui;
    //protected Document discoItemsDocument;


    public ViewRegistryCountrysidesPanel(RegistryProxy registryProxy, VilleinGui villeinGui) {
        super(new BorderLayout());
        this.registryProxy = registryProxy;
        this.villeinGui = villeinGui;

        this.farmlandList = new JList(new DefaultListModel());
        this.farmlandList.setCellRenderer(new CountrysideListRenderer());
        this.farmlandList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton subscribeButton = new JButton(SUBSCRIBE);
        JButton refreshButton = new JButton(REFRESH);
        buttonPanel.add(subscribeButton);
        subscribeButton.addActionListener(this);
        buttonPanel.add(refreshButton);
        refreshButton.addActionListener(this);


        this.refreshCountrysideFarms();

        JScrollPane scrollPane1 = new JScrollPane(this.farmlandList);

        this.add(scrollPane1, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(REFRESH)) {
            try {
                this.registryProxy.refreshDiscoItems();
                this.refreshCountrysideFarms();
            } catch (Exception e) {
                XmppVillein.LOGGER.severe(e.getMessage());
            }
        } else if (event.getActionCommand().equals(SUBSCRIBE)) {
            for (Object farmlandJid : this.farmlandList.getSelectedValues()) {
                villeinGui.getXmppVillein().requestSubscription(farmlandJid.toString());
            }
        }

    }

    /*private void generateDiscoItemsDocument() throws XMPPException, JDOMException, IOException {
        ServiceDiscoveryManager discoManager = this.villeinGui.getXmppVillein().getDiscoManager();
        this.discoItemsDocument = LinkedProcess.createXMLDocument(discoManager.discoverItems(this.registryProxy.getFullJid()).toXML());
        //PacketCollector collector = this.villeinGui.getXmppVillein().getConnection().createPacketCollector(new PacketTypeFilter(DiscoverItems.class));
        //this.discoItemsDocument = LinkedProcess.createXMLDocument(collector.nextResult().toXML());
        //collector.cancel();
    }*/

    private void refreshCountrysideFarms() {
        DefaultListModel listModel = (DefaultListModel) this.farmlandList.getModel();
        listModel.removeAllElements();
        for(CountrysideProxy countrysideProxy : registryProxy.getActiveCountrysides()) {
            listModel.addElement(countrysideProxy.getFullJid());
        }
    }
}
