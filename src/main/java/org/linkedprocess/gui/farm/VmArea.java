package org.linkedprocess.gui.farm;

import org.linkedprocess.xmpp.vm.XmppVirtualMachine;
import org.linkedprocess.xmpp.villein.HostStruct;
import org.linkedprocess.gui.*;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.ToContainsFilter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.HashMap;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 5:03:43 PM
 */
public class VmArea extends JPanel implements ActionListener {

    protected FarmGui farmGui;
    protected JTreeImage tree;
    protected JTextArea farmFeaturesText;
    protected DefaultMutableTreeNode treeRoot;
    protected Map<String, DefaultMutableTreeNode> treeMap;

    protected final static String SHUTDOWN = "shutdown";


     public VmArea(FarmGui farmGui) {
        super(new BorderLayout());
        this.farmGui = farmGui;
        HostStruct hostStruct = new HostStruct();
        hostStruct.setFullJid(LinkedProcess.generateBareJid(farmGui.getXmppFarm().getFullJid()));
        this.treeRoot = new DefaultMutableTreeNode(hostStruct);
        this.tree = new JTreeImage(this.treeRoot, ImageHolder.farmBackground);
        this.tree.setCellRenderer(new TreeRenderer());
        this.tree.setModel(new DefaultTreeModel(treeRoot));

        JScrollPane vmTreeScroll = new JScrollPane(this.tree);
        JButton shutdownButton = new JButton(SHUTDOWN);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(shutdownButton);
        buttonPanel.setOpaque(false);
        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(buttonPanel, BorderLayout.SOUTH);
        treePanel.setOpaque(false);

        //JPanel securityPanel = new JPanel();
        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel(this.farmGui.getXmppFarm().getFullJid());
        packetSnifferPanel.setSize(10,10);
        //PacketFilter fromToFilter = new OrFilter(new FromContainsFilter(farmGui.getXmppFarm().getFullJid()), new ToContainsFilter(farmGui.getXmppFarm().getFullJid()));
        this.farmGui.getXmppFarm().getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);
        this.farmGui.getXmppFarm().getConnection().addPacketListener(packetSnifferPanel, null);


        JTabbedPane tabbedPane = new JTabbedPane();


        tabbedPane.addTab("virtual machines", treePanel);
        //tabbedPane.addTab("farm security", securityPanel);
        tabbedPane.addTab("packets", packetSnifferPanel);

        this.add(tabbedPane, BorderLayout.CENTER);
 
        this.treeMap = new HashMap<String, DefaultMutableTreeNode>();
        this.createTree();
        this.setVisible(true);
    }

    public boolean containsVmNode(XmppVirtualMachine vm) {
        for(int i=0; i< treeRoot.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
            if(node.getUserObject() == vm) {
                return true;
            }
        }
        return false;
    }

    public void createTree() {
        treeRoot.removeAllChildren();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(this.farmGui.getXmppFarm());
        this.treeMap.put(this.farmGui.getXmppFarm().getFullJid(), farmNode);
        for (XmppVirtualMachine vm : this.farmGui.getXmppFarm().getVirtualMachines()) {
            DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vm);
            this.treeMap.put(vm.getFullJid(), vmNode);
            vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("villein_jid", vm.getVilleinJid())));
            vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vm.getVmStatus().toString())));
            vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vm.getVmSpecies())));
            vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vm.getVmPassword())));
            vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("running_time", ((float)vm.getRunningTime() / 60000.0f) + " sec.")));
            model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
            this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
        }
        model.insertNodeInto(farmNode, this.treeRoot, this.treeRoot.getChildCount());
        this.tree.scrollPathToVisible(new TreePath(farmNode));
        model.reload();
    }

    public void updateTree(String vmJid, LinkedProcess.VmStatus status) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode farmNode = this.treeMap.get(this.farmGui.getXmppFarm().getFullJid());
        DefaultMutableTreeNode node = this.treeMap.get(vmJid);
        if(node == null && status != LinkedProcess.VmStatus.NOT_FOUND) {
            try {
                XmppVirtualMachine xmppVm = this.farmGui.getXmppFarm().getVirtualMachine(vmJid);
                DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(xmppVm);
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("villein_jid", xmppVm.getVilleinJid())));
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", xmppVm.getVmStatus().toString())));
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", xmppVm.getVmSpecies())));
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", xmppVm.getVmPassword())));
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("running_time", ((float)xmppVm.getRunningTime() / 60000.0f) + " sec.")));
                model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
                this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                this.treeMap.put(vmJid, vmNode);
                model.reload(vmNode);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else if(node != null && (status == LinkedProcess.VmStatus.ACTIVE || status == LinkedProcess.VmStatus.ACTIVE_FULL)) {
            node.removeAllChildren();
            XmppVirtualMachine xmppVm = (XmppVirtualMachine) node.getUserObject();
            node.add(new DefaultMutableTreeNode(new TreeNodeProperty("villein_jid", xmppVm.getVilleinJid())));
            node.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", xmppVm.getVmStatus().toString())));
            node.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", xmppVm.getVmSpecies())));
            node.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", xmppVm.getVmPassword())));
            node.add(new DefaultMutableTreeNode(new TreeNodeProperty("running_time", ((float)xmppVm.getRunningTime() / 60000.0f) + " sec.")));
            model.reload(node);
        } else if(node != null && status == LinkedProcess.VmStatus.NOT_FOUND) {
            node.removeAllChildren();
            model.removeNodeFromParent(node);
            this.treeMap.remove(vmJid);
        }
    }

    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand().equals(SHUTDOWN)) {
            this.farmGui.getXmppFarm().shutDown();
            this.farmGui.loadLoginFrame();
        }
    }

}
