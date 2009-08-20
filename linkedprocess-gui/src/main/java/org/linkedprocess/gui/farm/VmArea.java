package org.linkedprocess.gui.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.vm.LopVm;
import org.linkedprocess.gui.PacketSnifferPanel;
import org.linkedprocess.gui.RosterPanel;
import org.linkedprocess.gui.TreeRenderer;
import org.linkedprocess.gui.farm.vmviewer.VmViewerFrame;
import org.linkedprocess.villein.proxies.CountrysideProxy;

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
        CountrysideProxy countrysideProxy = new CountrysideProxy(LinkedProcess.generateBareJid(farmGui.getXmppFarm().getFullJid()));
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

    public boolean containsVmNode(LopVm vm) {
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
        for (LopVm lopVm : this.farmGui.getXmppFarm().getVirtualMachines()) {
            DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(lopVm);
            this.treeMap.put(lopVm.getFullJid(), vmNode);
            vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", lopVm.getSpawningVilleinJid())));
            vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_status", ""+VmArea.isAvailable(lopVm.getVmStatus()))));
            vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", lopVm.getVmSpecies())));
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
                LopVm lopVm = this.farmGui.getXmppFarm().getVirtualMachine(vmJid);
                DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(lopVm);
                vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", lopVm.getSpawningVilleinJid())));
                vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_status", ""+VmArea.isAvailable(lopVm.getVmStatus()))));
                vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", lopVm.getVmSpecies())));
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
            LopVm lopVm = (LopVm) node.getUserObject();
            node.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", lopVm.getSpawningVilleinJid())));
            node.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_status", ""+VmArea.isAvailable(lopVm.getVmStatus()))));
            node.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", lopVm.getVmSpecies())));
            //node.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", xmppVm.getVmPassword())));
            //node.add(new DefaultMutableTreeNode(new TreeNodeProperty("running_time", xmppVm.getRunningTimeInSeconds() + " seconds")));
            model.reload(node);
        } else if (node != null && status == LinkedProcess.VmStatus.NOT_FOUND) {
            node.removeAllChildren();
            model.removeNodeFromParent(node);
            this.treeMap.remove(vmJid);
        }
    }

    private static boolean isAvailable(LinkedProcess.VmStatus vmStatus) {
        return(vmStatus == LinkedProcess.VmStatus.ACTIVE);
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
                if (selectedNode.getUserObject() instanceof LopVm) {
                    LopVm lopVm = (LopVm) selectedNode.getUserObject();
                    VmViewerFrame vmViewer = this.vmViewerMap.get(lopVm.getFullJid());
                    if (vmViewer == null) {
                        vmViewer = new VmViewerFrame(lopVm);
                        this.vmViewerMap.put(lopVm.getFullJid(), vmViewer);
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
