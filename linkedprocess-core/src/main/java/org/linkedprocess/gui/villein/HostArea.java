package org.linkedprocess.gui.villein;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.gui.*;
import org.linkedprocess.gui.villein.vmcontrol.VmControlFrame;
import org.linkedprocess.xmpp.villein.FarmStruct;
import org.linkedprocess.xmpp.villein.HostStruct;
import org.linkedprocess.xmpp.villein.Struct;
import org.linkedprocess.xmpp.villein.VmStruct;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 11:13:22 PM
 */
public class HostArea extends JPanel implements ActionListener, MouseListener {

    protected VilleinGui villeinGui;
    protected JTree tree;
    protected JPopupMenu popupMenu;
    protected Object popupTreeObject;
    protected DefaultMutableTreeNode treeRoot;
    protected Set<String> supportedVmSpeciesActionCommands = new HashSet<String>();

    protected final static String DISCOVER_FEATURES = "discover features";
    protected final static String TERMINATE_VM = "terminate vm";
    protected final static String SPAWN_VM = "spawn vm";
    protected final static String ADD_HOST = "add host";
    protected final static String SHUTDOWN = "shutdown";
    protected final static String VM_CONTROL = "vm control";
    protected final static String PROBE = "probe";

    public HostArea(VilleinGui villeinGui) {
        super(new BorderLayout());
        this.villeinGui = villeinGui;
        HostStruct hostStruct = new HostStruct();
        hostStruct.setFullJid(LinkedProcess.generateBareJid(this.villeinGui.getXmppVillein().getFullJid()));
        this.treeRoot = new DefaultMutableTreeNode(hostStruct);
        this.tree = new JTree(this.treeRoot);
        this.tree.setCellRenderer(new TreeRenderer());
        this.tree.setModel(new DefaultTreeModel(treeRoot));
        this.tree.addMouseListener(this);
        this.tree.setRootVisible(false);
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));

        JScrollPane vmTreeScroll = new JScrollPane(this.tree);
        JButton shutdownButton = new JButton(SHUTDOWN);
        shutdownButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(shutdownButton);
        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(buttonPanel, BorderLayout.SOUTH);

        RosterPanel rosterPanel = new RosterPanel(this.villeinGui.getXmppVillein().getRoster());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("hosts", treePanel);
        tabbedPane.addTab("roster", rosterPanel);

        this.add(tabbedPane, BorderLayout.CENTER);

        this.villeinGui.getXmppVillein().createHostStructsFromRoster();
        this.createTree();
    }

    public void actionPerformed(ActionEvent event) {

        this.popupMenu.setVisible(false);

        if (event.getActionCommand().equals(TERMINATE_VM)) {
            if (this.popupTreeObject instanceof VmStruct) {
                VmStruct vmStruct = (VmStruct) this.popupTreeObject;
                this.villeinGui.getXmppVillein().terminateVirtualMachine(vmStruct);
                this.villeinGui.removeVmFrame(vmStruct);
            }
        } else if (event.getActionCommand().equals(VM_CONTROL)) {
            if (this.popupTreeObject instanceof VmStruct) {
                VmStruct vmStruct = (VmStruct) this.popupTreeObject;
                VmControlFrame vmControlFrame = this.villeinGui.getVmFrame(vmStruct.getFullJid());
                if (vmControlFrame == null) {
                    this.villeinGui.addVmFrame(vmStruct);
                } else {
                    vmControlFrame.setVisible(true);
                }
            }

        } else if (event.getActionCommand().equals(PROBE)) {
            if (this.popupTreeObject instanceof Struct) {
                Struct struct = (Struct) this.popupTreeObject;
                this.villeinGui.getXmppVillein().probeJid(struct.getFullJid());
            }
        } else if (event.getActionCommand().equals(DISCOVER_FEATURES)) {
            if (this.popupTreeObject instanceof FarmStruct) {
                FarmStruct farmStruct = (FarmStruct) this.popupTreeObject;
                FarmDiscoFrame farmDiscoFrame = new FarmDiscoFrame(farmStruct, this.villeinGui);
                farmDiscoFrame.setVisible(true);
            }
        } else if (event.getActionCommand().equals(SHUTDOWN)) {
            this.villeinGui.getXmppVillein().shutDown(null);
            this.villeinGui.loadLoginFrame();
        } else {
            for (String vmSpecies : this.supportedVmSpeciesActionCommands) {
                if (event.getActionCommand().equals(vmSpecies)) {
                    if (this.popupTreeObject instanceof FarmStruct) {
                        String farmJid = ((FarmStruct) this.popupTreeObject).getFullJid();
                        this.villeinGui.getXmppVillein().spawnVirtualMachine(farmJid, vmSpecies);
                        break;
                    }
                }
            }
        }
    }

    public void createTree() {
        treeRoot.removeAllChildren();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        for (HostStruct hostStruct : this.villeinGui.getXmppVillein().getHostStructs()) {
            DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(hostStruct);
            for (FarmStruct farmStruct : hostStruct.getFarmStructs()) {
                DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(farmStruct);
                for (VmStruct vmStruct : farmStruct.getVmStructs()) {
                    DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vmStruct);
                    model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                    DefaultMutableTreeNode temp;

                    if (vmStruct.getPresence() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vmStruct.getPresence().getType().toString()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                    }
                    if (vmStruct.getVmSpecies() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vmStruct.getVmSpecies()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                    }
                    /*if (vmStruct.getVmPassword() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmStruct.getVmPassword()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                    }*/
                }
                model.insertNodeInto(farmNode, hostNode, hostNode.getChildCount());
                this.tree.scrollPathToVisible(new TreePath(farmNode.getPath()));
            }

            model.insertNodeInto(hostNode, this.treeRoot, this.treeRoot.getChildCount());
            this.tree.scrollPathToVisible(new TreePath(hostNode.getPath()));
        }
        model.reload();
    }

    private DefaultMutableTreeNode getNode(DefaultMutableTreeNode root, String jid) {
        if (root.getUserObject() instanceof Struct) {
            Struct temp = (Struct) root.getUserObject();
            if (temp.getFullJid().equals(jid)) {
                return root;
            }
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode node = getNode((DefaultMutableTreeNode) root.getChildAt(i), jid);
            if (node != null)
                return node;
        }
        return null;
    }


    public void updateTree(String jid, boolean remove) {
        DefaultMutableTreeNode node = this.getNode(this.treeRoot, jid);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

        if (node != null) {
            if (remove) {
                node.removeAllChildren();
                model.removeNodeFromParent(node);
            } else {
                if (node.getUserObject() instanceof HostStruct) {
                    this.tree.scrollPathToVisible(new TreePath(node.getPath()));
                    model.reload(node);
                } else if (node.getUserObject() instanceof FarmStruct) {
                    this.tree.scrollPathToVisible(new TreePath(node.getPath()));
                    model.reload(node);
                } else {
                    node.removeAllChildren();
                    VmStruct vmStruct = (VmStruct) node.getUserObject();
                    DefaultMutableTreeNode temp;

                    if (vmStruct.getPresence() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vmStruct.getPresence().getType().toString()));
                        model.insertNodeInto(temp, node, node.getChildCount());
                    }
                    if (vmStruct.getVmSpecies() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vmStruct.getVmSpecies()));
                        model.insertNodeInto(temp, node, node.getChildCount());
                    }
                    /*if (vmStruct.getVmPassword() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmStruct.getVmPassword()));
                        model.insertNodeInto(temp, node, node.getChildCount());
                    }*/
                    model.reload(node);
                }
            }
        } else {
            if (!remove) {
                Struct parentStruct = this.villeinGui.getXmppVillein().getParentStruct(jid);
                DefaultMutableTreeNode parentNode = null;
                if (parentStruct != null) {
                    parentNode = this.getNode(this.treeRoot, parentStruct.getFullJid());
                }

                Struct struct = this.villeinGui.getXmppVillein().getStruct(jid);
                if (struct instanceof HostStruct) {
                    DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(struct);
                    model.insertNodeInto(hostNode, this.treeRoot, this.treeRoot.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(hostNode.getPath()));
                    model.reload(hostNode);
                } else if (struct instanceof FarmStruct) {
                    if (parentNode != null) {
                        DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(struct);
                        model.insertNodeInto(farmNode, parentNode, parentNode.getChildCount());
                        this.tree.scrollPathToVisible(new TreePath(farmNode.getPath()));
                        model.reload(farmNode);
                    } else {
                        parentStruct = this.villeinGui.getXmppVillein().getParentStruct(LinkedProcess.generateBareJid(jid));
                        parentNode = this.getNode(this.treeRoot, parentStruct.getFullJid());
                        if (parentNode != null) {
                            DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(struct);
                            model.insertNodeInto(farmNode, parentNode, parentNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(farmNode.getPath()));
                            model.reload(farmNode);
                        }
                    }
                } else if (struct instanceof VmStruct) {
                    if (parentNode != null) {
                        VmStruct vmStruct = (VmStruct) struct;
                        DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(struct);
                        DefaultMutableTreeNode temp;

                        if (vmStruct.getPresence() != null) {
                            temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_status", vmStruct.getPresence().getType().toString()));
                            model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(temp.getPath()));
                        }
                        if (vmStruct.getVmSpecies() != null) {
                            temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_species", vmStruct.getVmSpecies()));
                            model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(temp.getPath()));
                        }
                        /*if (vmStruct.getVmPassword() != null) {
                            temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmStruct.getVmPassword()));
                            model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(temp.getPath()));
                        }*/

                        model.insertNodeInto(vmNode, parentNode, parentNode.getChildCount());
                        this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                        model.reload(vmNode);

                    }
                }
            }
        }
    }

    public void mouseClicked(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        int selectedRow = tree.getRowForLocation(x, y);
        if (selectedRow != -1) {

            TreePath selectedPath = tree.getPathForLocation(x, y);
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            this.popupTreeObject = selectedNode.getUserObject();

            if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1) {
                if (this.popupTreeObject instanceof HostStruct) {
                    this.createHostPopupMenu();
                } else if (this.popupTreeObject instanceof FarmStruct) {
                    this.createFarmPopupMenu((FarmStruct) this.popupTreeObject);
                } else if (this.popupTreeObject instanceof VmStruct) {
                    this.createVmPopupMenu();
                }

                popupMenu.setLocation(x + villeinGui.getX(), y + villeinGui.getY());
                popupMenu.show(event.getComponent(), event.getX(), event.getY());

            } else if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() > 1) {
                if (this.popupTreeObject instanceof VmStruct) {
                    VmStruct vmStruct = (VmStruct) this.popupTreeObject;
                    VmControlFrame vmControlFrame = this.villeinGui.getVmFrame(vmStruct.getFullJid());
                    if (vmControlFrame == null) {
                        this.villeinGui.addVmFrame(vmStruct);
                    } else {
                        vmControlFrame.setVisible(true);
                    }

                }
            }

        }
    }

    public void createHostPopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Host");
        JMenuItem probeItem = new JMenuItem(PROBE);
        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        this.popupMenu.add(probeItem);
        probeItem.addActionListener(this);
    }

    public void createFarmPopupMenu(FarmStruct farmStruct) {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Farm");
        JMenuItem probeItem = new JMenuItem(PROBE);
        JMenuItem discoItem = new JMenuItem(DISCOVER_FEATURES);
        JMenu spawnMenu = new JMenu(SPAWN_VM);

        for (String vmSpecies : farmStruct.getSupportedVmSpecies()) {
            JMenuItem speciesItem = new JMenuItem(vmSpecies);
            speciesItem.addActionListener(this);
            this.supportedVmSpeciesActionCommands.add(vmSpecies);
            spawnMenu.add(speciesItem);
        }

        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        this.popupMenu.add(probeItem);
        this.popupMenu.add(discoItem);
        this.popupMenu.add(spawnMenu);
        discoItem.addActionListener(this);
        probeItem.addActionListener(this);
    }

    public void createVmPopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Virtual Machine");
        JMenuItem probeItem = new JMenuItem(PROBE);
        JMenuItem vmControlItem = new JMenuItem(VM_CONTROL);
        JMenuItem terminateVmItem = new JMenuItem(TERMINATE_VM);
        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        this.popupMenu.add(probeItem);
        this.popupMenu.add(vmControlItem);
        this.popupMenu.add(terminateVmItem);
        terminateVmItem.addActionListener(this);
        vmControlItem.addActionListener(this);
        probeItem.addActionListener(this);
    }

    public void mouseReleased(MouseEvent e) {
        this.popupMenu.setVisible(false);
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent event) {

    }


}
