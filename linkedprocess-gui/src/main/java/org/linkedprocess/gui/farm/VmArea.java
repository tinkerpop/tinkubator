package org.linkedprocess.gui.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.Vm;
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
    protected Map<String, DefaultMutableTreeNode> treeMap = new HashMap<String, DefaultMutableTreeNode>();
    protected Map<String, VmViewerFrame> vmViewerMap = new HashMap<String, VmViewerFrame>();

    protected final static String SHUTDOWN = "shutdown";


    public VmArea(FarmGui farmGui) {
        super(new BorderLayout());
        this.farmGui = farmGui;
        CountrysideProxy countrysideProxy = new CountrysideProxy(farmGui.getFarm().getJid().getBareJid());
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
        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel();
        this.farmGui.getFarm().getConnection().addPacketListener(packetSnifferPanel, null);
        this.farmGui.getFarm().getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);

        RosterPanel rosterPanel = new RosterPanel(this.farmGui.getFarm().getRoster());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("virtual machines", treePanel);
        //tabbedPane.addTab("farm security", securityPanel);
        tabbedPane.addTab("roster", rosterPanel);
        tabbedPane.addTab("packets", packetSnifferPanel);

        this.add(tabbedPane, BorderLayout.CENTER);

        this.createTree();
        this.setVisible(true);
    }

    public boolean containsVmNode(Vm vm) {
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
        DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(this.farmGui.getFarm());
        this.treeMap.put(this.farmGui.getFarm().getJid().toString(), farmNode);
        for (Vm vm : this.farmGui.getFarm().getVms()) {
            DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vm);
            this.treeMap.put(vm.getVmId(), vmNode);
            vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", vm.getSpawningVilleinJid())));
            vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", vm.getVmSpecies())));
            model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
            this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
        }
        model.insertNodeInto(farmNode, this.treeRoot, this.treeRoot.getChildCount());
        this.tree.scrollPathToVisible(new TreePath(farmNode));
        model.reload();
    }

    public void updateTree(String vmId, LinkedProcess.Status status) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode farmNode = this.treeMap.get(this.farmGui.getFarm().getJid().toString());
        DefaultMutableTreeNode node = this.treeMap.get(vmId);
        if (node == null && status != LinkedProcess.Status.INACTIVE) {
            try {
                Vm vm = this.farmGui.getFarm().getVm(vmId);
                DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vm);
                vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", vm.getSpawningVilleinJid())));
                vmNode.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", vm.getVmSpecies())));
                model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
                this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                this.treeMap.put(vmId, vmNode);
                model.reload(vmNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (node != null && (status == LinkedProcess.Status.ACTIVE || status == LinkedProcess.Status.BUSY)) {
            node.removeAllChildren();
            Vm vm = (Vm) node.getUserObject();
            node.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("villein_jid", vm.getSpawningVilleinJid())));
            node.add(new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", vm.getVmSpecies())));
            model.reload(node);
        } else if (node != null && status == LinkedProcess.Status.INACTIVE) {
            node.removeAllChildren();
            model.removeNodeFromParent(node);
            this.treeMap.remove(vmId);
        }
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(SHUTDOWN)) {
            this.farmGui.getFarm().shutdown();
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
                if (selectedNode.getUserObject() instanceof Vm) {
                    Vm vm = (Vm) selectedNode.getUserObject();
                    VmViewerFrame vmViewer = this.vmViewerMap.get(vm.getVmId());
                    if (vmViewer == null) {
                        vmViewer = new VmViewerFrame(vm, farmGui.getFarm());
                        this.vmViewerMap.put(vm.getVmId(), vmViewer);
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
