package org.linkedprocess.gui.villein;

import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.villein.LopVillein;
import org.linkedprocess.villein.proxies.CountrysideProxy;
import org.linkedprocess.villein.proxies.RegistryProxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
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
                LopVillein.LOGGER.severe(e.getMessage());
            }
        } else if (event.getActionCommand().equals(SUBSCRIBE)) {
            for (Object farmlandJid : this.farmlandList.getSelectedValues()) {
                villeinGui.getXmppVillein().requestSubscription(farmlandJid.toString());
            }
        }

    }

    /*private void generateDiscoItemsDocument() throws XMPPException, JDOMException, IOException {
        ServiceDiscoveryManager discoManager = this.villeinGui.getLopVillein().getDiscoManager();
        this.discoItemsDocument = LinkedProcess.createXMLDocument(discoManager.discoverItems(this.registryProxy.getFullJid()).toXML());
        //PacketCollector collector = this.villeinGui.getLopVillein().getConnection().createPacketCollector(new PacketTypeFilter(DiscoverItems.class));
        //this.discoItemsDocument = LinkedProcess.createXMLDocument(collector.nextResult().toXML());
        //collector.cancel();
    }*/

    private void refreshCountrysideFarms() {
        DefaultListModel listModel = (DefaultListModel) this.farmlandList.getModel();
        listModel.removeAllElements();
        for (CountrysideProxy countrysideProxy : registryProxy.getActiveCountrysides()) {
            listModel.addElement(countrysideProxy.getJid());
        }
    }

    private class CountrysideListRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(ImageHolder.countrysideIcon);
            return label;
        }
    }

}
