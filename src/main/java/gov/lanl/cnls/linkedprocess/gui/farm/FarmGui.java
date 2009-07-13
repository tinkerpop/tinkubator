package gov.lanl.cnls.linkedprocess.gui.farm;

import gov.lanl.cnls.linkedprocess.xmpp.farm.XmppFarm;
import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    protected XmppFarm xmppFarm;

    protected SystemTray systemTray;
    protected TrayIcon systemTrayIcon;
    protected MenuItem show;
    protected MainArea mainArea;


    public void loadLoginFrame() {
        this.getContentPane().removeAll();
        this.getContentPane().add(new FarmLoginArea(this));
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }

    public void loadMainFrame(XmppFarm xmppFarm) {
        this.xmppFarm = xmppFarm;
        this.getContentPane().removeAll();
        this.mainArea = new MainArea(this);
        this.getContentPane().add(this.mainArea);
        this.setResizable(false);
        this.pack();
        xmppFarm.setStatusEventHandler(new FarmGuiStatusEventHandler(this));
    }

    public void createVirtualMachineTree() {
        this.mainArea.createTree();
    }

    public void updateVirtualMachineTree(String vmJid, LinkedProcess.VmStatus status) {
        this.mainArea.updateTree(vmJid, status);
    }

    public FarmGui() {
        super(FarmGui.FRAME_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            PopupMenu popup = new PopupMenu();
            MenuItem exit = new MenuItem(QUIT_MANAGER);
            if (this.isVisible())
                show = new MenuItem(FarmGui.SHOW_MANAGER);
            else
                show = new MenuItem(FarmGui.HIDE_MANAGER);
            popup.add(show);
            popup.addSeparator();
            popup.add(exit);
            popup.addActionListener(this);

            this.systemTray = SystemTray.getSystemTray();
            this.systemTrayIcon = new TrayIcon(ImageHolder.farmIcon.getImage(), FarmGui.FRAME_TITLE, popup);
            this.systemTray.add(this.systemTrayIcon);
            this.systemTrayIcon.setImageAutoSize(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.loadLoginFrame();
    }


    public XmppFarm getXmppFarm() {
        return this.xmppFarm;
    }

    public void shutDown() {
        if (this.xmppFarm != null)
            this.xmppFarm.shutDown();
        System.exit(0);
    }

    public void actionPerformed(ActionEvent event) {

        if (event.getActionCommand().equals(FarmGui.SHOW_MANAGER)) {
            show.setLabel(HIDE_MANAGER);
            this.setVisible(true);
        } else if (event.getActionCommand().equals(FarmGui.HIDE_MANAGER)) {
            show.setLabel(SHOW_MANAGER);
            this.setVisible(false);
        } else if (event.getActionCommand().equals(FarmGui.QUIT_MANAGER)) {
            this.shutDown();
        }

    }

    public static void main(String[] args) {
        new FarmGui();
    }
}
