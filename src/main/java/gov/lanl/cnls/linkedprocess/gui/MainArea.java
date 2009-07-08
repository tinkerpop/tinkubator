package gov.lanl.cnls.linkedprocess.gui;

import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeCellRenderer;
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
    protected DefaultMutableTreeNode vmTreeRoot;
    public static final ImageIcon speciesIcon = new ImageIcon(FarmGui.class.getResource("species.png"));
    public static final ImageIcon passwordIcon = new ImageIcon(FarmGui.class.getResource("password.png"));
    public static final ImageIcon timeIcon = new ImageIcon(FarmGui.class.getResource("time.png"));
    public static final ImageIcon spawnerIcon = new ImageIcon(FarmGui.class.getResource("spawner.png"));
    public static final ImageIcon statusIcon = new ImageIcon(FarmGui.class.getResource("status.png"));
    public static final ImageIcon activeIcon = new ImageIcon(FarmGui.class.getResource("active.png"));
    public static final ImageIcon inactiveIcon = new ImageIcon(FarmGui.class.getResource("inactive.png"));

     public MainArea(FarmGui farmGui) {
        this.farmGui = farmGui;
        this.vmTreeRoot = new DefaultMutableTreeNode(this.farmGui.getFarm());
        this.vmTree = new JTreeImage(this.vmTreeRoot, FarmGui.farmBackground);
        this.vmTree.setCellRenderer(new TreeRenderer());
        this.vmTree.setModel(new DefaultTreeModel(vmTreeRoot));

        JScrollPane vmTreeScroll = new JScrollPane(this.vmTree);
        JButton shutdownButton = new JButton("shutdown farm");
        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(shutdownButton, BorderLayout.SOUTH);
        treePanel.setOpaque(false);
        treePanel.setBorder(BorderFactory.createLineBorder(FarmGui.GRAY_COLOR, 2));

        JPanel securityPanel = new JPanel();

        this.addTab("virtual machines", treePanel);
        this.addTab("farm security", securityPanel);
         
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
        for (XmppVirtualMachine vm : this.farmGui.getFarm().getVirtualMachines()) {
            DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vm);
            //if (!this.containsVmNode(vm)) {
                vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("spawning_app", vm.getSpawningAppJid())));
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

    private class TreeRenderer extends DefaultTreeCellRenderer {
          public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
              this.setOpaque(false);
              this.setBackgroundNonSelectionColor(new Color(0,0,0,0));
              //this.setBackgroundSelectionColor(new Color(255,255,255,255));
              //this.setTextNonSelectionColor(new Color(255,255,255,255));

              super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

              Object x = ((DefaultMutableTreeNode) value).getUserObject();
              if (x instanceof XmppFarm) {
                  this.setText(((XmppFarm) x).getFullJid());
                  if (((XmppFarm)x).getScheduler().getSchedulerStatus() == LinkedProcess.FarmStatus.ACTIVE) {
                      this.setIcon(activeIcon);
                  } else {
                      this.setIcon(inactiveIcon);
                  }
                  this.setToolTipText("farm");
              } else if (x instanceof XmppVirtualMachine) {
                  XmppVirtualMachine vm = (XmppVirtualMachine) x;
                  this.setText(vm.getFullJid());
                  if (vm.getVmStatus() == LinkedProcess.VMStatus.ACTIVE) {
                      this.setIcon(activeIcon);
                  } else {
                      this.setIcon(inactiveIcon);
                  }
                  this.setToolTipText("vm_jid");
              } else if (x instanceof TreeNodeProperty) {
                  if (((TreeNodeProperty) x).getKey().equals("spawning_app")) {
                      this.setIcon(spawnerIcon);
                      this.setText(((TreeNodeProperty) x).getValue());
                      this.setToolTipText("spawning_app");
                  } else if (((TreeNodeProperty) x).getKey().equals("vm_status")) {
                      this.setIcon(statusIcon);
                      this.setText(((TreeNodeProperty) x).getValue());
                      this.setToolTipText("vm_status");
                  } else if (((TreeNodeProperty) x).getKey().equals("vm_species")) {
                      this.setIcon(speciesIcon);
                      this.setText(((TreeNodeProperty) x).getValue());
                      this.setToolTipText("vm_species");
                  } else if (((TreeNodeProperty) x).getKey().equals("vm_password")) {
                      this.setIcon(passwordIcon);
                      this.setText(((TreeNodeProperty) x).getValue());
                      this.setToolTipText("vm_password");
                  } else if (((TreeNodeProperty) x).getKey().equals("running_time")) {
                      this.setIcon(timeIcon);
                      this.setText(((TreeNodeProperty) x).getValue());
                      this.setToolTipText("running_time");
                  }
              }
              return this;
          }
      }

      public void actionPerformed(ActionEvent event) {
              this.farmGui.getFarm().shutDown();
              this.farmGui.loadLoginFrame();    
      }

}
