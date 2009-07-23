package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.gui.PacketSnifferPanel;
import org.linkedprocess.xmpp.vm.XmppVirtualMachine;

import javax.swing.*;

/**
 * User: marko
 * Date: Jul 21, 2009
 * Time: 9:21:16 AM
 */
public class VmViewerFrame extends JFrame {

    protected XmppVirtualMachine xmppVm;


    public VmViewerFrame(XmppVirtualMachine xmppVm) {
        super(xmppVm.getFullJid());
        this.xmppVm = xmppVm;
        JTabbedPane tabbedPane = new JTabbedPane();

        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel(this.xmppVm.getFullJid());

        //PacketFilter fromToFilter = new OrFilter(new FromContainsFilter(xmppVm.getFullJid()), new ToContainsFilter(xmppVm.getFullJid()));
        xmppVm.getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);
        xmppVm.getConnection().addPacketListener(packetSnifferPanel, null);

        tabbedPane.addTab("virtual machine", new MetadataPanel(this.xmppVm));
        tabbedPane.addTab("packets", packetSnifferPanel);
        this.getContentPane().add(tabbedPane);
        this.setResizable(false);
        this.pack();
        //this.setSize(438,448);
        this.setSize(442, 491);
        this.setVisible(true);
    }
}
