package gov.lanl.cnls.linkedprocess.gui.farm;

import gov.lanl.cnls.linkedprocess.xmpp.vm.XmppVirtualMachine;
import gov.lanl.cnls.linkedprocess.xmpp.villein.UserStruct;
import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.gui.JTreeImage;
import gov.lanl.cnls.linkedprocess.gui.TreeNodeProperty;
import gov.lanl.cnls.linkedprocess.gui.TreeRenderer;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

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
public class MainArea extends JTabbedPane implements ActionListener {

    protected FarmGui farmGui;
    protected JTreeImage tree;
    protected JTextArea farmFeaturesText;
    protected DefaultMutableTreeNode treeRoot;
    protected Map<String, DefaultMutableTreeNode> treeMap;


     public MainArea(FarmGui farmGui) {
        this.farmGui = farmGui;
        UserStruct userStruct = new UserStruct();
        userStruct.setFullJid(LinkedProcess.generateBareJid(farmGui.getXmppFarm().getFullJid()));
        this.treeRoot = new DefaultMutableTreeNode(userStruct);
        this.tree = new JTreeImage(this.treeRoot, ImageHolder.farmBackground);
        this.tree.setCellRenderer(new TreeRenderer());
        this.tree.setModel(new DefaultTreeModel(treeRoot));

        JScrollPane vmTreeScroll = new JScrollPane(this.tree);
        JButton shutdownButton = new JButton("shutdown farm");
        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(shutdownButton, BorderLayout.SOUTH);
        treePanel.setOpaque(false);
        treePanel.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));

        JPanel securityPanel = new JPanel();
        this.addTab("virtual machines", treePanel);
        this.addTab("farm security", securityPanel); 
        /*try {
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            builder.setDTDHandler(null);
            builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            builder.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false);
            builder.setFeature("http://xml.org/sax/features/use-entity-resolver2", false);
            builder.setFeature("http://xml.org/sax/features/validation", false);
            builder.setFeature("http://xml.org/sax/features/namespaces", false);
            builder.setIgnoringBoundaryWhitespace(true);
            builder.setIgnoringElementContentWhitespace(true);
            System.out.println(this.farmGui.getFarmStruct().getServiceExtension().toXML());
            Document doc = builder.build(new BufferedReader(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + this.farmGui.getFarmStruct().getServiceExtension().toXML())));
            XMLOutputter out = new XMLOutputter();
            JPanel featuresPanel = new JPanel();
            this.farmFeaturesText = new JTextArea(out.outputString(doc));
            featuresPanel.add(this.farmFeaturesText);
            this.addTab("farm features", featuresPanel);
        } catch(Exception e) { e.printStackTrace(); }   */

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
        //System.out.println(node + "---------" + status);
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
        this.farmGui.getXmppFarm().shutDown();
        this.farmGui.loadLoginFrame();
    }
}
