package gov.lanl.cnls.linkedprocess.gui.farm;

import gov.lanl.cnls.linkedprocess.xmpp.vm.XmppVirtualMachine;
import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.gui.JTreeImage;
import gov.lanl.cnls.linkedprocess.gui.TreeNodeProperty;
import gov.lanl.cnls.linkedprocess.gui.TreeRenderer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 5:03:43 PM
 */
public class MainArea extends JTabbedPane implements ActionListener {

    protected FarmGui farmGui;
    protected JTreeImage vmTree;
    protected JTextArea farmFeaturesText;
    protected DefaultMutableTreeNode vmTreeRoot;


     public MainArea(FarmGui farmGui) {
        this.farmGui = farmGui;
        this.vmTreeRoot = new DefaultMutableTreeNode(this.farmGui.getXmppFarm());
        this.vmTree = new JTreeImage(this.vmTreeRoot, ImageHolder.farmBackground);
        this.vmTree.setCellRenderer(new TreeRenderer());
        this.vmTree.setModel(new DefaultTreeModel(vmTreeRoot));

        JScrollPane vmTreeScroll = new JScrollPane(this.vmTree);
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

         
        this.setVisible(true);
    }

       public boolean containsVmNode(XmppVirtualMachine vm) {
        for(int i=0; i<vmTreeRoot.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) vmTreeRoot.getChildAt(i);
            if(node.getUserObject() == vm) {
                return true;
            }
        }
        return false;
    }

    public void updateVirtualMachineTree() {
        vmTreeRoot.removeAllChildren();
        DefaultTreeModel model = (DefaultTreeModel) vmTree.getModel();
        for (XmppVirtualMachine vm : this.farmGui.getXmppFarm().getVirtualMachines()) {
            DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vm);
            //if (!this.containsVmNode(vm)) {
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("villein_jid", vm.getVilleinJid())));
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vm.getVmStatus().toString())));
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vm.getVmSpecies())));
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vm.getVmPassword())));
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("running_time", ((float)vm.getRunningTime() / 60000.0f) + " sec.")));
                model.insertNodeInto(vmNode, this.vmTreeRoot, this.vmTreeRoot.getChildCount());
                this.vmTree.scrollPathToVisible(new TreePath(vmNode.getPath()));
            //}
        }
        model.reload();
    }

    public void actionPerformed(ActionEvent event) {
        this.farmGui.getXmppFarm().shutDown();
        this.farmGui.loadLoginFrame();
    }
}
