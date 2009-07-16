package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.gui.LoginArea;
import gov.lanl.cnls.linkedprocess.xmpp.villein.XmppVillein;

import java.awt.event.ActionEvent;
import java.awt.*;

import org.jivesoftware.smack.XMPPException;

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
            if(event.getActionCommand().equals("login")) {
                this.statusLabel.setText("");
                XmppVillein villein = new XmppVillein(serverField.getText(), new Integer(this.portField.getText()), this.usernameField.getText(), this.passwordField.getText());
                this.villeinGui.loadHostArea(villein);
            } else if (event.getActionCommand().equals("quit")) {
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
