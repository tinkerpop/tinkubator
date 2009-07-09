package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.gui.JTreeImage;
import gov.lanl.cnls.linkedprocess.gui.TreeNodeProperty;
import gov.lanl.cnls.linkedprocess.gui.TreeRenderer;
import gov.lanl.cnls.linkedprocess.xmpp.villein.FarmStruct;
import gov.lanl.cnls.linkedprocess.xmpp.villein.VmStruct;
import gov.lanl.cnls.linkedprocess.xmpp.villein.UserStruct;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import org.jivesoftware.smack.RosterEntry;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 11:13:22 PM
 */
public class BuddyArea extends JPanel implements ActionListener, MouseListener {

    protected VilleinGui villeinGui;
    protected JTreeImage villeinTree;
    protected JTextField addBuddyField;
    protected JPopupMenu popupMenu;
    protected Object popupTreeObject;
    protected DefaultMutableTreeNode villeinTreeRoot;


    public BuddyArea(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
        this.villeinTreeRoot = new DefaultMutableTreeNode(this.villeinGui.getXmppVillein());
        this.villeinTree = new JTreeImage(this.villeinTreeRoot, ImageHolder.cowBackground);
        this.villeinTree.setCellRenderer(new TreeRenderer());
        this.villeinTree.setModel(new DefaultTreeModel(villeinTreeRoot));
        this.villeinTree.addMouseListener(this);
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));

        JScrollPane vmTreeScroll = new JScrollPane(this.villeinTree);
        JButton shutdownButton = new JButton("shutdown");
        JButton addFarmButton = new JButton("add farm");
        shutdownButton.addActionListener(this);
        addFarmButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.addBuddyField = new JTextField(15);
        buttonPanel.add(this.addBuddyField);
        buttonPanel.add(addFarmButton);
        buttonPanel.add(shutdownButton);
        
        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(buttonPanel, BorderLayout.SOUTH);
        treePanel.setOpaque(false);
        treePanel.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));

        this.add(treePanel);

        this.updateVilleinTree();
    }

    public void actionPerformed(ActionEvent event) {

        this.popupMenu.setVisible(false);
        if(event.getActionCommand().equals("add farm")) {
            if(this.addBuddyField.getText() != null && this.addBuddyField.getText().length() > 0)
                this.villeinGui.getXmppVillein().requestSubscription(this.addBuddyField.getText());
        } else if(event.getActionCommand().equals("unsubscribe")) {
            if(this.popupTreeObject instanceof UserStruct) {
                String jid = ((UserStruct)this.popupTreeObject).getUserJid();
                this.villeinGui.getXmppVillein().requestUnsubscription(jid, true);
                this.popupTreeObject = null;
            }
        } else if(event.getActionCommand().equals("terminate vm")) {
            if(this.popupTreeObject instanceof VmStruct) {
                String vmJid = ((VmStruct)this.popupTreeObject).getVmJid();
                String vmPassword = ((VmStruct)this.popupTreeObject).getVmPassword();
                this.villeinGui.getXmppVillein().terminateVirtualMachine(vmJid, vmPassword);
            }
        } else if(event.getActionCommand().equals("shutdown")) {

            this.villeinGui.shutDown();
        }


        this.villeinGui.getXmppVillein().createUserStructsFromRoster();
        this.updateVilleinTree();
    }

    public void updateVilleinTree() {
        villeinTreeRoot.removeAllChildren();
        DefaultTreeModel model = (DefaultTreeModel) villeinTree.getModel();
        //System.out.println("The number of users is: " + this.villeinGui.getXmppVillein().getUserStructs());
        for (UserStruct userStruct : this.villeinGui.getXmppVillein().getUserStructs()) {
            DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(userStruct);
            for(FarmStruct farmStruct : userStruct.getFarmStructs()) {
                DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(farmStruct);
                for(VmStruct vmStruct : farmStruct.getVmStructs()) {
                    DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vmStruct);
                    model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
                    this.villeinTree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                    DefaultMutableTreeNode temp;
                    LinkedProcess.VmStatus status = vmStruct.getVmStatus();
                    if(status != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", status.toString()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                        this.villeinTree.scrollPathToVisible(new TreePath(temp.getPath()));
                    }
                    if(vmStruct.getVmSpecies() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vmStruct.getVmSpecies()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                        this.villeinTree.scrollPathToVisible(new TreePath(temp.getPath()));
                    }
                    if(vmStruct.getVmPassword() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmStruct.getVmPassword()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                        this.villeinTree.scrollPathToVisible(new TreePath(temp.getPath()));
                    }
                }
                model.insertNodeInto(farmNode, userNode, userNode.getChildCount());
                this.villeinTree.scrollPathToVisible(new TreePath(farmNode.getPath()));
            }

            model.insertNodeInto(userNode, this.villeinTreeRoot, this.villeinTreeRoot.getChildCount());
            this.villeinTree.scrollPathToVisible(new TreePath(userNode.getPath()));
        }
        model.reload();
    }

    public void mouseClicked(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        int selectedRow = villeinTree.getRowForLocation(x, y);
        System.out.println(event);
        System.out.println(selectedRow);        
        if(selectedRow != -1)
        {
            if(event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1) {
                TreePath selectedPath = villeinTree.getPathForLocation(x, y);
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
                Object nodeObject = selectedNode.getUserObject();
                if(nodeObject instanceof UserStruct) {
                    this.popupTreeObject = nodeObject;
                    popupMenu.removeAll();
                    JMenuItem unsubscribeItem = new JMenuItem("unsubscribe");
                    JLabel menuLabel = new JLabel("Host");
                    menuLabel.setHorizontalTextPosition(JLabel.CENTER);
                    
                    popupMenu.add(menuLabel);
                    popupMenu.addSeparator();
                    popupMenu.add(unsubscribeItem);
                    unsubscribeItem.addActionListener(this);
                    popupMenu.setLocation(x, y);
                    popupMenu.setVisible(true);
                } else if(nodeObject instanceof VmStruct) {
                    this.popupTreeObject = nodeObject;
                    popupMenu.removeAll();
                    JMenuItem terminateVmItem = new JMenuItem("terminate vm");
                    JLabel menuLabel = new JLabel("Virtual Machine");
                    menuLabel.setHorizontalTextPosition(JLabel.CENTER);
                    popupMenu.add(menuLabel);
                    popupMenu.addSeparator();
                    popupMenu.add(terminateVmItem);
                    terminateVmItem.addActionListener(this);
                    popupMenu.setLocation(x, y);
                    popupMenu.setVisible(true);
                }

            } else if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() > 1) {
                TreePath selectedPath = villeinTree.getPathForLocation(x, y);
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
                Object nodeObject = selectedNode.getUserObject();
                if(nodeObject instanceof FarmStruct) {
                    new FarmFrame(this.villeinGui, (FarmStruct)nodeObject);
                }    
            }

         }

    }

    public void mouseReleased(MouseEvent e) {
        System.out.println(e);
        this.popupMenu.setVisible(false);
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {
        
    }

    public void mousePressed(MouseEvent event) {

    }





}
