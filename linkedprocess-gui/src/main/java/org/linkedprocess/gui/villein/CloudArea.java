package org.linkedprocess.gui.villein;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.gui.*;
import org.linkedprocess.gui.villein.vmcontrol.VmControlFrame;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.PresenceHandler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.*;

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
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class CloudArea extends JPanel implements ActionListener, MouseListener, PresenceHandler {

    protected VilleinGui villeinGui;
    protected JTree tree;
    protected JPopupMenu popupMenu;
    protected Object popupTreeObject;
    protected DefaultMutableTreeNode treeRoot;
    protected Set<String> supportedVmSpeciesActionCommands = new HashSet<String>();

    protected final static String DISCOVER_INFORMATION = "discover information";
    protected final static String DISCOVER_COUNTRYSIDES = "discover countrysides";
    protected final static String SET_FARM_PASSWORD = "set farm password";
    protected final static String TERMINATE_VM = "terminate vm";
    protected final static String SPAWN_VM = "spawn vm";
    protected final static String ADD_COUNTRYSIDE = "add countryside";
    protected final static String SHUTDOWN = "shutdown";
    protected final static String VM_CONTROL = "vm control";
    protected final static String PROBE = "probe";

    public CloudArea(VilleinGui villeinGui) {
        super(new BorderLayout());
        this.villeinGui = villeinGui;
        this.treeRoot = new DefaultMutableTreeNode(null);
        this.tree = new JTree(this.treeRoot);
        this.tree.setCellRenderer(new TreeRenderer());
        this.tree.setModel(new DefaultTreeModel(treeRoot));
        this.tree.addMouseListener(this);
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);
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
        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel();
        this.villeinGui.getXmppVillein().getConnection().addPacketListener(packetSnifferPanel, null);
        this.villeinGui.getXmppVillein().getConnection().addPacketWriterInterceptor(packetSnifferPanel, null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("cloud", treePanel);
        tabbedPane.addTab("roster", rosterPanel);
        tabbedPane.addTab("packets", packetSnifferPanel);

        this.add(tabbedPane, BorderLayout.CENTER);

        this.villeinGui.getXmppVillein().createCloudFromRoster();
        this.createTree();
    }

    public void actionPerformed(ActionEvent event) {

        this.popupMenu.setVisible(false);

        if (event.getActionCommand().equals(TERMINATE_VM)) {
            if (this.popupTreeObject instanceof VmProxy) {
                final VmProxy vmProxy = (VmProxy) this.popupTreeObject;
                Handler<Object> resultHandler = new Handler<Object>() {
                    public void handle(Object object) {
                        updateTree(vmProxy.getVmId(), true);
                    }
                };
                vmProxy.terminateVm(resultHandler, new GenericErrorHandler());
                this.villeinGui.removeVmFrame(vmProxy);
            }
        } else if (event.getActionCommand().equals(VM_CONTROL)) {
            if (this.popupTreeObject instanceof VmProxy) {
                VmProxy vmProxy = (VmProxy) this.popupTreeObject;
                VmControlFrame vmControlFrame = this.villeinGui.getVmFrame(vmProxy.getVmId());
                if (vmControlFrame == null) {
                    this.villeinGui.addVmFrame(vmProxy);
                } else {
                    vmControlFrame.setVisible(true);
                }
            }

        } else if (event.getActionCommand().equals(PROBE)) {
            if (this.popupTreeObject instanceof XmppProxy) {
                XmppProxy proxy = (XmppProxy) this.popupTreeObject;
                this.villeinGui.getXmppVillein().probeJid(proxy.getFullJid());
            }
        } else if (event.getActionCommand().equals(DISCOVER_INFORMATION)) {
            if (this.popupTreeObject instanceof FarmProxy) {
                FarmProxy farmProxy = (FarmProxy) this.popupTreeObject;
                JFrame farmFrame = new JFrame(farmProxy.getFullJid());
                farmFrame.getContentPane().add(new ViewFarmConfigurationPanel(farmProxy, villeinGui));
                farmFrame.pack();
                farmFrame.setSize(600, 600);
                farmFrame.setVisible(true);
                farmFrame.setResizable(true);
            }
        } else if (event.getActionCommand().equals(DISCOVER_COUNTRYSIDES)) {
            if (this.popupTreeObject instanceof RegistryProxy) {
                RegistryProxy registryProxy = (RegistryProxy) this.popupTreeObject;
                JFrame farmFrame = new JFrame(registryProxy.getFullJid());
                farmFrame.getContentPane().add(new ViewRegistryCountrysidesPanel(registryProxy, villeinGui));
                farmFrame.pack();
                farmFrame.setVisible(true);
                farmFrame.setResizable(true);
            }
        } else if (event.getActionCommand().equals(SET_FARM_PASSWORD)) {
            if (this.popupTreeObject instanceof FarmProxy) {
                FarmProxy farmProxy = (FarmProxy) this.popupTreeObject;
                farmProxy.setFarmPassword(JOptionPane.showInputDialog(null, "enter farm password", "set farm password", JOptionPane.QUESTION_MESSAGE).trim());
            }

        } else if (event.getActionCommand().equals(SHUTDOWN)) {
            this.villeinGui.getXmppVillein().shutdown();
            this.villeinGui.loadLoginFrame();
        } else {
            for (String vmSpecies : this.supportedVmSpeciesActionCommands) {
                if (event.getActionCommand().equals(vmSpecies)) {
                    if (this.popupTreeObject instanceof FarmProxy) {
                        FarmProxy farmProxy = (FarmProxy) this.popupTreeObject;
                        Handler<VmProxy> resultHandler = new Handler<VmProxy>() {
                            public void handle(VmProxy vmProxy) {
                                updateTree(vmProxy.getVmId(), false);
                            }
                        };
                        farmProxy.spawnVm(vmSpecies, resultHandler, new GenericErrorHandler());
                        break;
                    }
                }
            }
        }
    }

    public void createTree() {
        treeRoot.removeAllChildren();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        for (CountrysideProxy countrysideProxy : this.villeinGui.getXmppVillein().getCloud().getCountrysideProxies()) {
            DefaultMutableTreeNode countrysideNode = new DefaultMutableTreeNode(countrysideProxy);
            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                DefaultMutableTreeNode registryNode = new DefaultMutableTreeNode(registryProxy);
                model.insertNodeInto(registryNode, countrysideNode, countrysideNode.getChildCount());
                this.tree.scrollPathToVisible(new TreePath(registryNode.getPath()));
            }
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(farmProxy);
                for (VmProxy vmProxy : farmProxy.getVmProxies()) {
                    DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vmProxy);
                    model.insertNodeInto(vmNode, farmNode, farmNode.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                    DefaultMutableTreeNode temp;
                    //temp = new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_status", "" + vmStruct.isAvailable()));
                    //model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                    if (vmProxy.getVmSpecies() != null) {
                        temp = new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", vmProxy.getVmSpecies()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                    }
                    /*if (vmProxy.getVmPassword() != null) {
                        temp = new DefaultMutableTreeNode(new TreeNodeProperty("vm_password", vmProxy.getVmPassword()));
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

    private DefaultMutableTreeNode getNode(DefaultMutableTreeNode root, String identifier) {
        if (root.getUserObject() instanceof CountrysideProxy) {
            if (((CountrysideProxy) root.getUserObject()).getBareJid().equals(identifier))
                return root;
        }
        if (root.getUserObject() instanceof XmppProxy) {
            XmppProxy temp = (XmppProxy) root.getUserObject();
            if (temp.getFullJid().equals(identifier)) {
                return root;
            }
        }
        if (root.getUserObject() instanceof VmProxy) {
            if (((VmProxy) root.getUserObject()).getVmId().equals(identifier)) {
                return root;
            }
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode node = getNode((DefaultMutableTreeNode) root.getChildAt(i), identifier);
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
                if (node.getUserObject() instanceof CountrysideProxy || node.getUserObject() instanceof RegistryProxy || node.getUserObject() instanceof FarmProxy) {
                    this.tree.scrollPathToVisible(new TreePath(node.getPath()));
                    model.reload(node);
                } else if (node.getUserObject() instanceof VmProxy) {
                    node.removeAllChildren();
                    VmProxy vmProxy = (VmProxy) node.getUserObject();
                    if (vmProxy.getVmSpecies() != null) {
                        DefaultMutableTreeNode temp = new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", vmProxy.getVmSpecies()));
                        model.insertNodeInto(temp, node, node.getChildCount());
                    }
                    this.tree.scrollPathToVisible(new TreePath(node.getPath()));
                    model.reload(node);
                } else {
                    Villein.LOGGER.severe("Unknown node/proxy object: " + node.getUserObject());
                }
            }
        } else {
            if (!remove) {

                CountrysideProxy countrysideProxy = this.villeinGui.getXmppVillein().getCloud().getCountrysideProxy(jid);
                if (countrysideProxy != null) {
                    DefaultMutableTreeNode countrysideNode = new DefaultMutableTreeNode(countrysideProxy);
                    model.insertNodeInto(countrysideNode, this.treeRoot, this.treeRoot.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(countrysideNode.getPath()));
                    model.reload(countrysideNode);
                    return;
                }

                RegistryProxy registryProxy = this.villeinGui.getXmppVillein().getCloud().getRegistryProxy(jid);
                if (registryProxy != null) {
                    DefaultMutableTreeNode parentNode = this.getNode(this.treeRoot, LinkedProcess.generateBareJid(registryProxy.getFullJid()));
                    DefaultMutableTreeNode registryNode = new DefaultMutableTreeNode(registryProxy);
                    model.insertNodeInto(registryNode, parentNode, parentNode.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(registryNode.getPath()));
                    model.reload(registryNode);
                    return;

                }

                FarmProxy farmProxy = this.villeinGui.getXmppVillein().getCloud().getFarmProxy(jid);
                if (farmProxy != null) {
                    DefaultMutableTreeNode parentNode = this.getNode(this.treeRoot, LinkedProcess.generateBareJid(farmProxy.getFullJid()));
                    DefaultMutableTreeNode farmNode = new DefaultMutableTreeNode(farmProxy);
                    model.insertNodeInto(farmNode, parentNode, parentNode.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(farmNode.getPath()));
                    model.reload(farmNode);
                    return;

                }
                VmProxy vmProxy = this.villeinGui.getXmppVillein().getCloud().getVmProxy(jid);
                if (vmProxy != null) {
                    DefaultMutableTreeNode parentNode = this.getNode(this.treeRoot, vmProxy.getFarmJid());
                    DefaultMutableTreeNode vmNode = new DefaultMutableTreeNode(vmProxy);
                    DefaultMutableTreeNode temp;
                    if (vmProxy.getVmSpecies() != null) {
                        temp = new DefaultMutableTreeNode(new TreeRenderer.TreeNodeProperty("vm_species", vmProxy.getVmSpecies()));
                        model.insertNodeInto(temp, vmNode, vmNode.getChildCount());
                        this.tree.scrollPathToVisible(new TreePath(temp.getPath()));
                    }


                    model.insertNodeInto(vmNode, parentNode, parentNode.getChildCount());
                    this.tree.scrollPathToVisible(new TreePath(vmNode.getPath()));
                    model.reload(vmNode);

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
                if (this.popupTreeObject instanceof CountrysideProxy) {
                    this.createCountrysidePopupMenu();
                } else if (this.popupTreeObject instanceof RegistryProxy) {
                    this.createRegistryPopupMenu();
                } else if (this.popupTreeObject instanceof FarmProxy) {
                    this.createFarmPopupMenu((FarmProxy) this.popupTreeObject);
                } else if (this.popupTreeObject instanceof VmProxy) {
                    this.createVmPopupMenu();
                }

                popupMenu.setLocation(x + villeinGui.getX(), y + villeinGui.getY());
                popupMenu.show(event.getComponent(), event.getX(), event.getY());

            } else if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() > 1) {
                if (this.popupTreeObject instanceof VmProxy) {
                    VmProxy vmProxy = (VmProxy) this.popupTreeObject;
                    VmControlFrame vmControlFrame = this.villeinGui.getVmFrame(vmProxy.getVmId());
                    if (vmControlFrame == null) {
                        this.villeinGui.addVmFrame(vmProxy);
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
        //JMenuItem probeResource = new JMenuItem(PROBE);
        JMenuItem discoItems = new JMenuItem(DISCOVER_COUNTRYSIDES);

        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        //this.popupMenu.add(probeResource);
        this.popupMenu.add(discoItems);
        discoItems.addActionListener(this);
        //probeResource.addActionListener(this);
    }

    public void createFarmPopupMenu(FarmProxy farmProxy) {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Farm");
        //JMenuItem probeResource = new JMenuItem(PROBE);
        JMenuItem discoInfo = new JMenuItem(DISCOVER_INFORMATION);
        JMenuItem addFarmPassword = null;
        if (farmProxy.requiresFarmPassword()) {
            addFarmPassword = new JMenuItem(SET_FARM_PASSWORD);
            addFarmPassword.addActionListener(this);
        }
        JMenu spawnMenu = new JMenu(SPAWN_VM);

        for (String vmSpecies : farmProxy.getSupportedVmSpecies()) {
            JMenuItem speciesItem = new JMenuItem(vmSpecies);
            speciesItem.addActionListener(this);
            this.supportedVmSpeciesActionCommands.add(vmSpecies);
            spawnMenu.add(speciesItem);
        }

        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        //this.popupMenu.add(probeResource);
        this.popupMenu.add(discoInfo);
        if (null != addFarmPassword)
            this.popupMenu.add(addFarmPassword);
        this.popupMenu.add(spawnMenu);
        discoInfo.addActionListener(this);
        //probeResource.addActionListener(this);
    }

    public void createVmPopupMenu() {
        this.popupMenu = new JPopupMenu();
        this.popupMenu.setBorder(new BevelBorder(6));
        JLabel menuLabel = new JLabel("Virtual Machine");
        //JMenuItem probeResource = new JMenuItem(PROBE);
        JMenuItem vmControlItem = new JMenuItem(VM_CONTROL);
        JMenuItem terminateVmItem = new JMenuItem(TERMINATE_VM);
        menuLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.popupMenu.add(menuLabel);
        this.popupMenu.addSeparator();
        //this.popupMenu.add(probeResource);
        this.popupMenu.add(vmControlItem);
        this.popupMenu.add(terminateVmItem);
        terminateVmItem.addActionListener(this);
        vmControlItem.addActionListener(this);
        //probeResource.addActionListener(this);
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

    public void handlePresenceUpdate(XmppProxy proxy, boolean available) {
        updateTree(proxy.getFullJid(), !available);
    }
}
