package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.FarmStruct;

import javax.swing.*;

/**
 * User: marko
 * Date: Jul 15, 2009
 * Time: 4:09:34 PM
 */
public class FarmDiscoFrame extends JFrame {

    protected FarmStruct farmStruct;
    protected VilleinGui villeinGui;

    public FarmDiscoFrame(FarmStruct farmStruct, VilleinGui villeinGui) {
        super(farmStruct.getFullJid());
        this.farmStruct = farmStruct;
        this.villeinGui = villeinGui;
        JTextArea discoTextArea = new JTextArea(25, 35);
        discoTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(discoTextArea);
        try {
            discoTextArea.setText(LinkedProcess.createPrettyXML(this.getFarmSecurity()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.getContentPane().add(scrollPane);
        this.pack();

    }

    public String getFarmSecurity() throws XMPPException {
        ServiceDiscoveryManager discoManager = this.villeinGui.getXmppVillein().getDiscoManager();
        DiscoverInfo disco = discoManager.discoverInfo(farmStruct.getFullJid());
        return disco.toXML();
    }

}
