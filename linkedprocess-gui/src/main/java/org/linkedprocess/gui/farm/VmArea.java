package org.linkedprocess.gui.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.gui.PacketSnifferPanel;
import org.linkedprocess.gui.RosterPanel;
import org.linkedprocess.gui.TreeRenderer;
import org.linkedprocess.gui.farm.vmviewer.VmViewerFrame;
import org.linkedprocess.xmpp.villein.proxies.CountrysideProxy;
import org.linkedprocess.xmpp.vm.XmppVm;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class VmArea extends JPanel implements ActionListener, MouseListener {

    protected FarmGui farmGui;
    protected JTree tree;
    protected JTextArea farmFeaturesText;
    protected DefaultMutableTreeNode treeRoot;
    protected Map<String, DefaultMutableTreeNode> treeMap;
    protected Map<String, VmViewerFrame> vmViewerMap;

    protected final static String SHUTDOWN = "shutdown";


    public VmArea(FarmGui farmGui) {
        super(new BorderLayout());
        this.farmGui = farmGui;
        CountrysideProxy countrysideProxy = new CountrysideProxy(LinkedProcess.generateBareJid(farmGui.getXmppFarm().getFullJid()), null);
        this.treeRoot = new DefaultMutableTreeNode(countrysideProxy);
        this.tree = new JTree(this.treeRoot);
        this.tree.setCellRenderer(new TreeRenderer());
        this.tree.setShowsRootHandles(true);
        this.tree.setModel(new DefaultTreeModel(treeRoot));
        this.tree.addMouseListener(this);

        JScrollPane vmTreeScroll = new JScrollPane(this.tree);
        JButton shutdownButton = new JButton(SHUTDOWN);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(shutdownButton);
        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(buttonPanel, BorderLayout.SOUTH);

        //JPanel securityPanel = new JPanel();
        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel(this.farmGui.getXmppFarm().getFullJid());
        this.farmGui.getXmppFarm().getConnection().addPacketListener(packetSnifferPanel, null);
        this.farmGui.getXmppFarm().getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);

        RosterPanel rosterPanel = new RosterPanel(this.farmGui.getXmppFarm().getRoster());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("virtual machines", treePanel);
        //tabbedPane.addTab("farm security", securityPanel);
        tabbedPane.addTab("roster", rosterPanel);
        tabbedPane.addTab("packets", packetSnifferPanel);

        this.add(tabbedPane, BorderLayout.CENTER);

        this.treeMap = new HashMap<String, DefaultMutableTreeNode>();
        this.vmViewerMap = new HashMap<String, VmViewerFrame>();
        this.createTree();
        this.setVisible(true);
    }

    public boolean containsVmNode(XmppVm vm) {
        for (int i = 0; i < treeRoot.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
            if (node.getUserObject() == vm) {
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
        for (XmppVm xmppVm : this.farmGui.getXmppFarm().getVirtualMachines()) {
            DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(xmppVm);
            this.treeMap.put(xmppVm.getFullJid(), vmNode);
            vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", xmppVm.getVilleinJid())));
            vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_status", xmppVm.getVmStatus().toString())));
            vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", xmppVm.getVmSpecies())));
            //vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", xmppVm.getVmPassword())));
            //vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("running_time", xmppVm.getRunningTimeInSeconds() + " seconds")));
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
        if (node == null && status != LinkedProcess.VmStatus.NOT_FOUND) {
            try {
                XmppVm xmppVm = this.farmGui.getXmppFarm().getVirtualMachine(vmJid);
                DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(xmppVm);
                vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", xmppVm.getVilleinJid())));
                vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_status", xmppVm.getVmStatus().toString())));
                vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", xmppVm.getVmSpecies())));
                //vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", xmppVm.getVmPassword())));
                //vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("running_time", xmppVm.getRunningTimeInSeconds() + " seconds")));
                model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
                this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                this.treeMap.put(vmJid, vmNode);
                model.reload(vmNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (node != null && (status == LinkedProcess.VmStatus.ACTIVE || status == LinkedProcess.VmStatus.ACTIVE_FULL)) {
            node.removeAllChildren();
            XmppVm xmppVm = (XmppVm) node.getUserObject();
            node.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", xmppVm.getVilleinJid())));
            node.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_status", xmppVm.getVmStatus().toString())));
            node.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", xmppVm.getVmSpecies())));
            //node.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", xmppVm.getVmPassword())));
            //node.add(new DefaultMutableTreeNode(new TreeNodeProperty("running_time", xmppVm.getRunningTimeInSeconds() + " seconds")));
            model.reload(node);
        } else if (node != null && status == LinkedProcess.VmStatus.NOT_FOUND) {
            node.removeAllChildren();
            model.removeNodeFromParent(node);
            this.treeMap.remove(vmJid);
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(SHUTDOWN)) {
            this.farmGui.getXmppFarm().shutdown();
            this.farmGui.loadLoginFrame();
        }
    }

    public void mouseClicked(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        int selectedRow = tree.getRowForLocation(x, y);
        if (selectedRow != -1) {

            TreePath selectedPath = tree.getPathForLocation(x, y);
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

            if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() > 1) {
                if (selectedNode.getUserObject() instanceof XmppVm) {
                    XmppVm xmppVm = (XmppVm) selectedNode.getUserObject();
                    VmViewerFrame vmViewer = this.vmViewerMap.get(xmppVm.getFullJid());
                    if (vmViewer == null) {
                        vmViewer = new VmViewerFrame(xmppVm);
                        this.vmViewerMap.put(xmppVm.getFullJid(), vmViewer);
                    } else {
                        vmViewer.setVisible(true);
                    }

                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent event) {

    }

}
