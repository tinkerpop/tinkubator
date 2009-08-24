package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.gui.PacketSnifferPanel;
import org.linkedprocess.vm.LopVm;

import javax.swing.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class VmViewerFrame extends JFrame {

    protected LopVm lopVm;


    public VmViewerFrame(LopVm lopVm) {
        super(lopVm.getResource());
        this.lopVm = lopVm;
        JTabbedPane tabbedPane = new JTabbedPane();

        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel();

        lopVm.getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);
        lopVm.getConnection().addPacketListener(packetSnifferPanel, null);

        tabbedPane.addTab("virtual machine", new MetadataPanel(this.lopVm));
        tabbedPane.addTab("bindings", new ViewBindingsPanel(this.lopVm));
        tabbedPane.addTab("packets", packetSnifferPanel);
        this.getContentPane().add(tabbedPane);
        this.setResizable(true);
        this.pack();
        this.setVisible(true);
    }
}
