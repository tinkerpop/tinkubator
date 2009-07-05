package gov.lanl.cnls.linkedprocess.gui;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;

/**
 * User: marko
 * Date: Jul 4, 2009
 * Time: 5:33:39 PM
 */
public class FarmGui extends JFrame implements TreeSelectionListener {

    protected XmppFarm farm;
    protected JTree vmTree;
    protected DefaultMutableTreeNode vmTreeRoot;

    public class Properties {

        private final String key;
        private final String value;

        public Properties(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

    }

    public FarmGui(final XmppFarm farm) {

        super("LoP Farm Manager");
        this.farm = farm;
        farm.setStatusEventHandler(new GuiStatusEventHandler(this));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.vmTreeRoot = new DefaultMutableTreeNode(this.farm);
        this.vmTree = new JTree(this.vmTreeRoot);
        this.vmTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.vmTree.setShowsRootHandles(true);
        this.vmTree.setCellRenderer(new TreeRenderer());
        this.vmTree.setModel(new DefaultTreeModel(vmTreeRoot));

        JScrollPane vmTreeScroll = new JScrollPane(this.vmTree);

        JPanel panel = new JPanel();
        panel.add(vmTreeScroll);
        panel.setOpaque(true);
        //panel.setForeground(Color.black);
        //panel.setBackground(Color.white);

        this.getContentPane().add(panel);
        this.setResizable(false);
        //this.setSize(450, 400);
        this.pack();
        this.setVisible(true);
        /*try {
            //SystemTray.getSystemTray().add(new TrayIcon(Toolkit.getDefaultToolkit().getImage(FarmGui.class.getResource("farm.png")), "My Caption", null));
            this.setIconImage(Toolkit.getDefaultToolkit().getImage(FarmGui.class.getResource("farm.png")));
        } catch(Exception e) { e.printStackTrace(); } */
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
        for (XmppVirtualMachine vm : this.farm.getVirtualMachines()) {
            DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vm);
            //if (!this.containsVmNode(vm)) {
                vmNode.add(new DefaultMutableTreeNode(new Properties("vm_species", vm.getVmSpecies())));
                vmNode.add(new DefaultMutableTreeNode(new Properties("vm_password", vm.getVmPassword())));
                vmNode.add(new DefaultMutableTreeNode(new Properties("running_time", ((float)vm.getRunningTime() / 60000.0f) + " sec.")));
                model.insertNodeInto(vmNode, this.vmTreeRoot, this.vmTreeRoot.getChildCount());
                this.vmTree.scrollPathToVisible(new TreePath(vmNode.getPath()));
            //}
        }
        model.reload();
    }

    public XmppFarm getFarm() {
        return this.farm;
    }

    private class TreeRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            Object x = ((DefaultMutableTreeNode) value).getUserObject();
            if (x instanceof XmppFarm) {
                this.setIcon(new ImageIcon(FarmGui.class.getResource("farm.png")));
                this.setText(((XmppFarm) x).getFullJid());
                this.setToolTipText("farm");
            } else if (x instanceof XmppVirtualMachine) {
                XmppVirtualMachine vm = (XmppVirtualMachine) x;
                this.setText(vm.getFullJid());
                if (vm.getVmStatus() == LinkedProcess.VMStatus.ACTIVE) {
                    this.setIcon(new ImageIcon(FarmGui.class.getResource("active.png")));
                } else {
                    this.setIcon(new ImageIcon(FarmGui.class.getResource("inactive.png")));
                }
                this.setToolTipText("vm_jid");
            } else if (x instanceof Properties) {
                if (((Properties) x).getKey().equals("vm_species")) {
                    this.setIcon(new ImageIcon(FarmGui.class.getResource("species.png")));
                    this.setText(((Properties) x).getValue());
                    this.setToolTipText("vm_species");
                } else if (((Properties) x).getKey().equals("vm_password")) {
                    this.setIcon(new ImageIcon(FarmGui.class.getResource("password.png")));
                    this.setText(((Properties) x).getValue());
                    this.setToolTipText("vm_password");
                } else if (((Properties) x).getKey().equals("running_time")) {
                    this.setIcon(new ImageIcon(FarmGui.class.getResource("time.png")));
                    this.setText(((Properties) x).getValue());
                    this.setToolTipText("running_time");
                }
            }

            return this;
        }

    }

    public void valueChanged(TreeSelectionEvent ev) {
        //this.updateVirtualMachineTree();
    }


    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                XmppFarm farm = new XmppFarm("xmpp.linkedprocess.org", 5222, "linked.process.1", "linked12");
                new FarmGui(farm);

            }
        });
    }
}
