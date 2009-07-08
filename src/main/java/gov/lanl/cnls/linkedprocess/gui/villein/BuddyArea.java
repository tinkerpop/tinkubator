package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.gui.JTreeImage;
import gov.lanl.cnls.linkedprocess.gui.TreeNodeProperty;
import gov.lanl.cnls.linkedprocess.xmpp.villein.XmppVillein;
import gov.lanl.cnls.linkedprocess.xmpp.villein.FarmStruct;
import gov.lanl.cnls.linkedprocess.xmpp.villein.VmStruct;
import gov.lanl.cnls.linkedprocess.xmpp.villein.UserStruct;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 11:13:22 PM
 */
public class BuddyArea extends JPanel implements ActionListener {

    protected VilleinGui villeinGui;
    protected JTreeImage villeinTree;
    protected DefaultMutableTreeNode villeinTreeRoot;

    public BuddyArea(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
        this.villeinTreeRoot = new DefaultMutableTreeNode(this.villeinGui.getVillein());
        this.villeinTree = new JTreeImage(this.villeinTreeRoot, ImageHolder.cowBackground);
        this.villeinTree.setCellRenderer(new TreeRenderer());
        this.villeinTree.setModel(new DefaultTreeModel(villeinTreeRoot));

        JScrollPane vmTreeScroll = new JScrollPane(this.villeinTree);
        JButton shutdownButton = new JButton("shutdown");
        JButton addFarmButton = new JButton("add farm");
        shutdownButton.addActionListener(this);
        addFarmButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(new JTextField(15));
        buttonPanel.add(addFarmButton);
        buttonPanel.add(shutdownButton);
        
        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(buttonPanel, BorderLayout.SOUTH);
        treePanel.setOpaque(false);
        treePanel.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));

        this.add(treePanel);
    }

    public void actionPerformed(ActionEvent event) {


        if(event.getActionCommand().equals("add farm")) {
            this.villeinGui.getVillein().createUserStructsFromRoster();
            this.updateVillenTree();

        } else {
            this.villeinGui.shutDown();
        }
    }

    public void updateVillenTree() {
        villeinTreeRoot.removeAllChildren();
        DefaultTreeModel model = (DefaultTreeModel) villeinTree.getModel();
        System.out.println("The number of users is: " + this.villeinGui.getVillein().getUserStructs());
        for (UserStruct userStruct : this.villeinGui.getVillein().getUserStructs()) {
            DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(userStruct);
            for(FarmStruct farmStruct : userStruct.getFarmStructs()) {
                DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(userStruct);
                for(VmStruct vmStruct : farmStruct.getVmStructs()) {
                    DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vmStruct);
                    model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
                    vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vmStruct.getVmStatus().toString())));
                    vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vmStruct.getVmSpecies())));
                    vmNode.add(new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmStruct.getVmPassword())));


                }
                System.out.println("FARM NODE!!!!");
                model.insertNodeInto(farmNode, userNode, userNode.getChildCount());
                this.villeinTree.scrollPathToVisible(new TreePath(farmNode.getPath()));
            }

            model.insertNodeInto(userNode, this.villeinTreeRoot, this.villeinTreeRoot.getChildCount());
            this.villeinTree.scrollPathToVisible(new TreePath(userNode.getPath()));
        }
        model.reload();
    }


    private class TreeRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            this.setOpaque(false);
            this.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
            //this.setBackgroundSelectionColor(new Color(255,255,255,255));
            //this.setTextNonSelectionColor(new Color(255,255,255,255));

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            Object x = ((DefaultMutableTreeNode) value).getUserObject();
              if (x instanceof XmppVillein) {
                  this.setText(((XmppVillein) x).getFullJid());
                  if (((XmppVillein)x).getStatus() == LinkedProcess.VilleinStatus.ACTIVE) {
                      this.setIcon(ImageHolder.activeIcon);
                  } else {
                      this.setIcon(ImageHolder.inactiveIcon);
                  }
                  this.setToolTipText("villen");
              } else if (x instanceof UserStruct) {
                  UserStruct userStruct = (UserStruct) x;
                  this.setText(userStruct.getUserJid());
                  if (userStruct.getStatus() == Presence.Mode.available) {
                      this.setIcon(ImageHolder.activeIcon);
                  } else {
                      this.setIcon(ImageHolder.inactiveIcon);
                  }
                  this.setToolTipText("user_jid");
              } else if (x instanceof FarmStruct) {
                  FarmStruct farm = (FarmStruct) x;
                  this.setText(farm.getFarmJid());
                  if (farm.getFarmStatus() == LinkedProcess.FarmStatus.ACTIVE) {
                      this.setIcon(ImageHolder.activeIcon);
                  } else {
                      this.setIcon(ImageHolder.inactiveIcon);
                  }
                  this.setToolTipText("farm_jid");
              } else if(x instanceof VmStruct) {
                  VmStruct vm = (VmStruct) x;
                  this.setText(vm.getVmJid());
                  if (vm.getVmStatus() == LinkedProcess.VmStatus.ACTIVE) {
                      this.setIcon(ImageHolder.activeIcon);
                  } else {
                      this.setIcon(ImageHolder.inactiveIcon);
                  }
                  this.setToolTipText("farm_jid");

              } else if (x instanceof TreeNodeProperty) {

                  if (((TreeNodeProperty) x).getKey().equals("vm_status")) {
                      this.setIcon(ImageHolder.statusIcon);
                      this.setText(((TreeNodeProperty) x).getValue());
                      this.setToolTipText("vm_status");
                  } else if (((TreeNodeProperty) x).getKey().equals("vm_species")) {
                      this.setIcon(ImageHolder.speciesIcon);
                      this.setText(((TreeNodeProperty) x).getValue());
                      this.setToolTipText("vm_species");
                  } else if (((TreeNodeProperty) x).getKey().equals("vm_password")) {
                      this.setIcon(ImageHolder.passwordIcon);
                      this.setText(((TreeNodeProperty) x).getValue());
                      this.setToolTipText("vm_password");
                  }
              }
              return this;
        }
    }

}
