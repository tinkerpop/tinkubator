package gov.lanl.cnls.linkedprocess.gui;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * User: marko
 * Date: Jul 4, 2009
 * Time: 5:33:39 PM
 */
public class FarmGui extends JFrame implements ActionListener {

    protected static final String FRAME_TITLE = "Simple Linked Process Farm Manager";
    protected static final String SHOW_MANAGER = "show manager";
    protected static final String HIDE_MANAGER = "hide manager";
    protected static final String QUIT_MANAGER = "quit manager";
    protected XmppFarm farm;
    protected JTreeImage vmTree;
    protected DefaultMutableTreeNode vmTreeRoot;
    protected SystemTray systemTray;
    protected TrayIcon systemTrayIcon;
    protected MenuItem show;

    public static final ImageIcon farmBackground = new ImageIcon(FarmGui.class.getResource("farm.png"));
    public static final ImageIcon barnIcon = new ImageIcon(FarmGui.class.getResource("barn.png"));
    public static final ImageIcon speciesIcon = new ImageIcon(FarmGui.class.getResource("species.png"));
    public static final ImageIcon passwordIcon = new ImageIcon(FarmGui.class.getResource("password.png"));
    public static final ImageIcon timeIcon = new ImageIcon(FarmGui.class.getResource("time.png"));
    public static final ImageIcon spawnerIcon = new ImageIcon(FarmGui.class.getResource("spawner.png"));
    public static final ImageIcon statusIcon = new ImageIcon(FarmGui.class.getResource("status.png"));
    public static final ImageIcon activeIcon = new ImageIcon(FarmGui.class.getResource("active.png"));
    public static final ImageIcon inactiveIcon = new ImageIcon(FarmGui.class.getResource("inactive.png"));
    public static final Color GRAY_COLOR = new Color(200, 200, 200);

    public void loadMainFrame(final XmppFarm farm) {
        this.getContentPane().removeAll();
        this.farm = farm;
        farm.setStatusEventHandler(new GuiStatusEventHandler(this));
        this.vmTreeRoot = new DefaultMutableTreeNode(this.farm);
        this.vmTree = new JTreeImage(this.vmTreeRoot, farmBackground);
        this.vmTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.vmTree.setShowsRootHandles(false);
        this.vmTree.setCellRenderer(new TreeRenderer());
        this.vmTree.setModel(new DefaultTreeModel(vmTreeRoot));
        this.vmTree.setOpaque(false);

        JScrollPane vmTreeScroll = new JScrollPane(this.vmTree);
        JButton logoutButton = new JButton("shutdown farm");
        logoutButton.addActionListener(this);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(vmTreeScroll, BorderLayout.CENTER);
        panel.add(logoutButton, BorderLayout.SOUTH);
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(GRAY_COLOR, 2));

        this.getContentPane().add(panel);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }

    public void loadLoginFrame() {
        this.getContentPane().removeAll();
        this.getContentPane().add(new LoginPanel(this));
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }

    public FarmGui() {
        super(FRAME_TITLE);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        try {
            PopupMenu popup = new PopupMenu();
            MenuItem exit = new MenuItem(QUIT_MANAGER);
            if(this.isVisible())
                show = new MenuItem(SHOW_MANAGER);
            else
                show = new MenuItem(HIDE_MANAGER);
            popup.add(show);
            popup.addSeparator();
            popup.add(exit);
            popup.addActionListener(this);

            this.systemTray = SystemTray.getSystemTray();
            this.systemTrayIcon = new TrayIcon(barnIcon.getImage(), FRAME_TITLE, popup);
            this.systemTray.add(this.systemTrayIcon);
            this.systemTrayIcon.setImageAutoSize(true);

        } catch(Exception e) {
            e.printStackTrace();
        }

        
        this.loadLoginFrame();
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

    public XmppFarm getFarm() {
        return this.farm;
    }

    public void shutDown() {
        if(this.farm != null)
            this.farm.shutDown();
        System.exit(0);
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

                this.setIcon(barnIcon);
                this.setText(((XmppFarm) x).getFullJid());
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

        if(event.getSource() instanceof JButton) {
            this.farm.shutDown();
            this.loadLoginFrame();
        } else {
            if(event.getActionCommand().equals(SHOW_MANAGER)) {
                show.setLabel(HIDE_MANAGER);
                this.setVisible(true);
            } else if(event.getActionCommand().equals(HIDE_MANAGER)) {
                show.setLabel(SHOW_MANAGER);
                this.setVisible(false);
            } else {
                this.shutDown();  
            }

        }
    }

    public static void main(String[] args) {
        new FarmGui();
    }
}
