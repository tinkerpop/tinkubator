package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.gui.*;
import org.linkedprocess.gui.villein.vmcontrol.VmControlFrame;
import org.linkedprocess.xmpp.villein.*;
import org.linkedprocess.xmpp.villein.structs.*;
import org.linkedprocess.xmpp.villein.handlers.PresenceHandler;
import org.linkedprocess.xmpp.villein.handlers.SpawnVmHandler;

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
public class CountrysideArea extends JPanel implements ActionListener, MouseListener, SpawnVmHandler, PresenceHandler {

    protected VilleinGui villeinGui;
    protected JTree tree;
    protected JPopupMenu popupMenu;
    protected Object popupTreeObject;
    protected DefaultMutableTreeNode treeRoot;
    protected Set<String> supportedVmSpeciesActionCommands = new HashSet<String>();

    protected final static String FARM_CONFIGURATION = "farm configuration";
    protected final static String REGISTRY_COUNTRYSIDES = "countrysides";
    protected final static String TERMINATE_VM = "terminate vm";
    protected final static String SPAWN_VM = "spawn vm";
    protected final static String ADD_COUNTRYSIDE = "add countryside";
    protected final static String SHUTDOWN = "shutdown";
    protected final static String VM_CONTROL = "vm control";
    protected final static String PROBE = "probe";

    public CountrysideArea(VilleinGui villeinGui) {
        super(new BorderLayout());
        this.villeinGui = villeinGui;
        CountrysideStruct countrysideStruct = new CountrysideStruct();
        countrysideStruct.setFullJid(LinkedProcess.generateBareJid(this.villeinGui.getXmppVillein().getFullJid()));
        this.treeRoot = new DefaultMutableTreeNode(countrysideStruct);
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
        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel(this.villeinGui.getXmppVillein().getFullJid());
        this.villeinGui.getXmppVillein().getConnection().addPacketListener(packetSnifferPanel, null);
        this.villeinGui.getXmppVillein().getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("countrysides", treePanel);
        tabbedPane.addTab("roster", rosterPanel);
        tabbedPane.addTab("packets", packetSnifferPanel);

        this.add(tabbedPane, BorderLayout.CENTER);

        this.villeinGui.getXmppVillein().createCountrysideStructsFromRoster();
        this.createTree();
    }

    public void actionPerformed(ActionEvent event) {

        this.popupMenu.setVisible(false);

        if (event.getActionCommand().equals(TERMINATE_VM)) {
            if (this.popupTreeObject instanceof VmStruct) {
                VmStruct vmStruct = (VmStruct) this.popupTreeObject;
                this.villeinGui.getXmppVillein().sendTerminateVirtualMachine(vmStruct);
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
        } else if (event.getActionCommand().equals(FARM_CONFIGURATION)) {
            if (this.popupTreeObject instanceof FarmStruct) {
                FarmStruct farmStruct = (FarmStruct) this.popupTreeObject;
                JFrame farmFrame = new JFrame(farmStruct.getFullJid());
                farmFrame.getContentPane().add(new ViewFarmConfigurationPanel(farmStruct, villeinGui));
                farmFrame.pack();
                farmFrame.setSize(600, 600);
                farmFrame.setVisible(true);
                farmFrame.setResizable(true);
            }
        } else if (event.getActionCommand().equals(REGISTRY_COUNTRYSIDES)) {
            if (this.popupTreeObject instanceof RegistryStruct) {
                RegistryStruct registryStruct = (RegistryStruct) this.popupTreeObject;
                JFrame farmFrame = new JFrame(registryStruct.getFullJid());
                farmFrame.getContentPane().add(new ViewRegistryCountrysidesPanel(registryStruct, villeinGui));
                farmFrame.pack();
                farmFrame.setVisible(true);
                farmFrame.setResizable(true);
            }
        } else if (event.getActionCommand().equals(SHUTDOWN)) {
            this.villeinGui.getXmppVillein().shutDown(null);
            this.villeinGui.loadLoginFrame();
        } else {
            for (String vmSpecies : this.supportedVmSpeciesActionCommands) {
                if (event.getActionCommand().equals(vmSpecies)) {
                    if (this.popupTreeObject instanceof FarmStruct) {
                        String farmJid = ((FarmStruct) this.popupTreeObject).getFullJid();
                        this.villeinGui.getXmppVillein().sendSpawnVirtualMachine(farmJid, vmSpecies);
                        break;
                    }
                }
            }
        }
    }

    public void createTree() {
        treeRoot.removeAllChildren();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        for (CountrysideStruct countrysideStruct : this.villeinGui.getXmppVillein().getCountrysideStructs()) {
            DefaultMutableTreeNode countrysideNode = new DefaultMutableTreeNode(countrysideStruct);
            for (RegistryStruct registryStruct : countrysideStruct.getRegistryStructs()) {
                DefaultMutableTreeNode registryNode = new DefaultMutableTreeNode(registryStruct);
                model.insertNodeInto(registryNode, countrysideNode, countrysideNode.getChildCount());
                this.tree.scrollPathToVisible(new TreePath(registryNode.getPath()));
            }
            for (FarmStruct farmStruct : countrysideStruct.getFarmStructs()) {
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
                model.insertNodeInto(farmNode, countrysideNode, countrysideNode.getChildCount());
                this.tree.scrollPathToVisible(new TreePath(farmNode.getPath()));
            }

            model.insertNodeInto(countrysideNode, this.treeRoot, this.treeRoot.getChildCount());
            this.tree.scrollPathToVisible(new TreePath(countrysideNode.getPath()));
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
                if (node.getUserObject() instanceof CountrysideStruct || node.getUserObject() instanceof RegistryStruct || node.getUserObject() instanceof FarmStruct) {
                    this.tree.scrollPathToVisible(new TreePath(node.getPath()));
                    model.reload(node);
                } else if (node.getUserObject() instanceof VmStruct) {
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
                    this.tree.scrollPathToVisible(new TreePath(node.getPath()));
                    model.reload(node);
                } else {
                    XmppVillein.LOGGER.severe("Unknown node/struct object: " + node.getUserObject());
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
                if (struct instanceof CountrysideStruct) {
                    DefaultMutableTreeNode countrysideStruct = new DefaultMutableTreeNode(struct);
                    model.insertNodeInto(countrysideStruct, this.treeRoot, this.treeRoot.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(countrysideStruct.getPath()));
                    model.reload(countrysideStruct);
                } else if (struct instanceof RegistryStruct || struct instanceof FarmStruct) {
                    if (parentNode != null) {
                        DefaultMutableTreeNode otherNode = new DefaultMutableTreeNode(struct);
                        model.insertNodeInto(otherNode, parentNode, parentNode.getChildCount());
                        this.tree.scrollPathToVisible(new TreePath(otherNode.getPath()));
                        model.reload(otherNode);
                    } else {
                        parentStruct = this.villeinGui.getXmppVillein().getParentStruct(LinkedProcess.generateBareJid(jid));
                        parentNode = this.getNode(this.treeRoot, parentStruct.getFullJid());
                        if (parentNode != null) {
                            DefaultMutableTreeNode otherNode = new DefaultMutableTreeNode(struct);
                            model.insertNodeInto(otherNode, parentNode, parentNode.getChildCount());
                            this.tree.scrollPathToVisible(new TreePath(otherNode.getPath()));
                            model.reload(otherNode);
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
                if (this.popupTreeObject instanceof CountrysideStruct) {
                    this.createCountrysidePopupMenu();
                } else if (this.popupTreeObject instanceof RegistryStruct) {
                    this.createRegistryPopupMenu();
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

    public void createCountrysidePopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Countryside");
        JMenuItem probeItem = new JMenuItem(PROBE);
        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        this.popupMenu.add(probeItem);
        probeItem.addActionListener(this);
    }

    public void createRegistryPopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Registry");
        JMenuItem probeResource = new JMenuItem(PROBE);
        JMenuItem discoItems = new JMenuItem(REGISTRY_COUNTRYSIDES);

        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        this.popupMenu.add(probeResource);
        this.popupMenu.add(discoItems);
        discoItems.addActionListener(this);
        probeResource.addActionListener(this);
    }

    public void createFarmPopupMenu(FarmStruct farmStruct) {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Farm");
        JMenuItem probeResource = new JMenuItem(PROBE);
        JMenuItem discoInfo = new JMenuItem(FARM_CONFIGURATION);
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
        this.popupMenu.add(probeResource);
        this.popupMenu.add(discoInfo);
        this.popupMenu.add(spawnMenu);
        discoInfo.addActionListener(this);
        probeResource.addActionListener(this);
    }

    public void createVmPopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Virtual Machine");
        JMenuItem probeResource = new JMenuItem(PROBE);
        JMenuItem vmControlItem = new JMenuItem(VM_CONTROL);
        JMenuItem terminateVmItem = new JMenuItem(TERMINATE_VM);
        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        this.popupMenu.add(probeResource);
        this.popupMenu.add(vmControlItem);
        this.popupMenu.add(terminateVmItem);
        terminateVmItem.addActionListener(this);
        vmControlItem.addActionListener(this);
        probeResource.addActionListener(this);
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

    public void handleSuccessfulSpawnVm(VmStruct vmStruct) {
        villeinGui.updateHostAreaTree(vmStruct.getFullJid(), false);
    }

    public void handlePresenceUpdate(Struct struct, Presence.Type presenceType) {
        if (presenceType == Presence.Type.unavailable || presenceType == Presence.Type.unsubscribe || presenceType == Presence.Type.unsubscribed) {
            this.villeinGui.updateHostAreaTree(struct.getFullJid(), true);
        } else {
            this.villeinGui.updateHostAreaTree(struct.getFullJid(), false);
        }
    }
}
