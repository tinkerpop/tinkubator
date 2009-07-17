package org.linkedprocess.gui.farm;

import org.linkedprocess.xmpp.farm.XmppFarm;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.gui.LoginArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Properties;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import org.jivesoftware.smack.XMPPException;

/**
 * User: marko
 * Date: Jul 6, 2009
 * Time: 10:32:32 AM
 */
public class FarmLoginArea extends LoginArea {

    protected FarmGui farmGui;
    protected final static String PROPERTIES_FILE = "farm_manager.properties";

    public FarmLoginArea(FarmGui farmGui) {
           super(ImageHolder.farmBackground.getImage(), PROPERTIES_FILE);
           this.farmGui = farmGui;
       }


    public void actionPerformed(ActionEvent event) {

        this.doRememberedProperties();
        
        try {
            if(event.getActionCommand().equals(LOGIN)) {
                this.statusLabel.setText("");
                XmppFarm farm = new XmppFarm(serverField.getText(), new Integer(this.portField.getText()), this.usernameField.getText(), this.passwordField.getText());
                this.farmGui.loadMainFrame(farm);
            } else if (event.getActionCommand().equals(QUIT)) {
                System.exit(0);
            }
        } catch(XMPPException e) {
            this.statusLabel.setText("Could not login.");
        }
    }

    public void paintComponent(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, null);
        super.paintComponent(g);
    }
}
