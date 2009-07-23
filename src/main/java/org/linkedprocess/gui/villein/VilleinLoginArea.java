package org.linkedprocess.gui.villein;

import org.jivesoftware.smack.XMPPException;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.gui.LoginArea;
import org.linkedprocess.xmpp.villein.XmppVillein;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * User: marko
 * Date: Jul 6, 2009
 * Time: 10:32:32 AM
 */
public class VilleinLoginArea extends LoginArea {

    protected VilleinGui villeinGui;
    protected final static String PROPERTIES_FILE = "vm_manager.properties";

    public VilleinLoginArea(VilleinGui villeinGui) {
        super(ImageHolder.cowBackground.getImage(), PROPERTIES_FILE);
        this.villeinGui = villeinGui;
    }

    public void actionPerformed(ActionEvent event) {

        this.doRememberedProperties();

        try {
            if (event.getActionCommand().equals(LOGIN)) {
                this.statusLabel.setText("");
                XmppVillein villein = new XmppVillein(serverField.getText(), new Integer(this.portField.getText()), this.usernameField.getText(), this.passwordField.getText());
                this.villeinGui.loadHostArea(villein);
            } else if (event.getActionCommand().equals(QUIT)) {
                System.exit(0);
            }
        } catch (XMPPException e) {
            this.statusLabel.setText("Could not login.");
        }
    }

    public void paintComponent(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, null);
        super.paintComponent(g);
    }
}
