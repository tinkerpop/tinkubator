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
        super(xmppVm.getResource());
        this.xmppVm = xmppVm;
        JTabbedPane tabbedPane = new JTabbedPane();

        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel(this.xmppVm.getFullJid());

        xmppVm.getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);
        xmppVm.getConnection().addPacketListener(packetSnifferPanel, null);

        tabbedPane.addTab("virtual machine", new MetadataPanel(this.xmppVm));
        tabbedPane.addTab("bindings", new ViewBindingsPanel(this.xmppVm));
        tabbedPane.addTab("packets", packetSnifferPanel);
        this.getContentPane().add(tabbedPane);
        this.setResizable(true);
        this.pack();
        this.setVisible(true);
    }
}
