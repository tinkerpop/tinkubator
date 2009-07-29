package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.gui.villein.VilleinGui;
import org.linkedprocess.xmpp.villein.FarmStruct;

import javax.swing.*;

/**
 * User: marko
 * Date: Jul 15, 2009
 * Time: 4:09:34 PM
 */
public class FarmConfigurationFrame extends JFrame {

    protected FarmStruct farmStruct;
    protected VilleinGui villeinGui;

    public FarmConfigurationFrame(FarmStruct farmStruct, VilleinGui villeinGui) {
        super(farmStruct.getFullJid());
        this.farmStruct = farmStruct;
        this.villeinGui = villeinGui;
        this.getContentPane().add(new ViewFarmConfigurationPanel(farmStruct, villeinGui));
        this.pack();
        this.setSize(600, 600);
        this.setVisible(true);
        this.setResizable(true);
    }
}
