package org.linkedprocess.gui.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.xmpp.farm.XmppFarm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class FarmGui extends JFrame implements ActionListener {

    protected static final String FRAME_TITLE = "LoPSideD Farm";
    protected static final String SHOW_FARM = "show farm";
    protected static final String HIDE_FARM = "hide farm";
    protected static final String QUIT_FARM = "quit farm";
    protected XmppFarm xmppFarm;

    protected SystemTray systemTray;
    protected TrayIcon systemTrayIcon;
    protected MenuItem show;
    protected VmArea vmArea;

    public void loadLoginFrame() {
        this.getContentPane().removeAll();
        this.getContentPane().add(new FarmLoginArea(this));
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }

    public void loadVmArea(XmppFarm xmppFarm) {
        this.xmppFarm = xmppFarm;
        this.getContentPane().removeAll();
        this.vmArea = new VmArea(this);
        this.getContentPane().add(this.vmArea);
        this.setResizable(true);
        this.pack();
        //this.setSize(442, 491);
        xmppFarm.setStatusEventHandler(new FarmGuiStatusEventHandler(this));
    }

    public void createVirtualMachineTree() {
        this.vmArea.createTree();
    }

    public void updateVirtualMachineTree(String vmJid, LinkedProcess.VmStatus status) {
        this.vmArea.updateTree(vmJid, status);
    }

    public FarmGui() {
        super(FarmGui.FRAME_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            PopupMenu popup = new PopupMenu();
            MenuItem exit = new MenuItem(QUIT_FARM);
            if (this.isVisible())
                show = new MenuItem(FarmGui.SHOW_FARM);
            else
                show = new MenuItem(FarmGui.HIDE_FARM);
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
            this.xmppFarm.shutdown();
        System.exit(0);
    }

    public void actionPerformed(ActionEvent event) {

        if (event.getActionCommand().equals(FarmGui.SHOW_FARM)) {
            show.setLabel(HIDE_FARM);
            this.setVisible(true);
        } else if (event.getActionCommand().equals(FarmGui.HIDE_FARM)) {
            show.setLabel(SHOW_FARM);
            this.setVisible(false);
        } else if (event.getActionCommand().equals(FarmGui.QUIT_FARM)) {
            this.shutDown();
        }
    }

    public static void main(String[] args) {
        new FarmGui();
    }
}
