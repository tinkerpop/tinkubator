package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.gui.PacketSnifferPanel;
import org.linkedprocess.os.Vm;

import javax.swing.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class VmViewerFrame extends JFrame {

    protected Vm vm;


    public VmViewerFrame(Vm vm) {
        //super(vm.getResource());
        this.vm = vm;
        JTabbedPane tabbedPane = new JTabbedPane();

        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel();

        //vm.getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);
        //vm.getConnection().addPacketListener(packetSnifferPanel, null);

        tabbedPane.addTab("virtual machine", new MetadataPanel(this.vm));
        tabbedPane.addTab("bindings", new ViewBindingsPanel(this.vm));
        tabbedPane.addTab("packets", packetSnifferPanel);
        this.getContentPane().add(tabbedPane);
        this.setResizable(true);
        this.pack();
        this.setVisible(true);
    }
}
