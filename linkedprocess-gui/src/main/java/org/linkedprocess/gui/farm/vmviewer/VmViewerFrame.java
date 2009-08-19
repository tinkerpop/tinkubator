package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.gui.PacketSnifferPanel;
import org.linkedprocess.xmpp.vm.XmppVirtualMachine;

import javax.swing.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
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
