package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.gui.PacketSnifferPanel;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.farm.Farm;

import javax.swing.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class VmViewerFrame extends JFrame {

    protected Vm vm;


    public VmViewerFrame(Vm vm, Farm farm) {
        super(vm.getVmId());
        this.vm = vm;
        JTabbedPane tabbedPane = new JTabbedPane();

        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel();

        farm.getConnection().addPacketWriterInterceptor(packetSnifferPanel, new PacketSnifferPanel.VmFilter(vm.getVmId()));
        farm.getConnection().addPacketListener(packetSnifferPanel, new PacketSnifferPanel.VmFilter(vm.getVmId()));

        tabbedPane.addTab("virtual machine", new MetadataPanel(this.vm));
        tabbedPane.addTab("bindings", new ViewBindingsPanel(this.vm));
        tabbedPane.addTab("packets", packetSnifferPanel);
        this.getContentPane().add(tabbedPane);
        this.setResizable(true);
        this.pack();
        this.setVisible(true);
    }
}
