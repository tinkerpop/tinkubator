package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.gui.villein.LoginArea;
import gov.lanl.cnls.linkedprocess.xmpp.villein.XmppVillein;

import javax.swing.*;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 10:33:02 PM
 */
public class VilleinGui extends JFrame {

    protected static final String FRAME_TITLE = "Simple Linked Process Villein";

    protected XmppVillein villein;
    protected BuddyArea buddyArea;

    public VilleinGui() {
        super(VilleinGui.FRAME_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.loadLoginFrame();
    }

    public void loadLoginFrame() {
        this.getContentPane().removeAll();
        this.getContentPane().add(new LoginArea(this));
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }

    public void loadBuddyArea(XmppVillein villein) {
        this.villein = villein;
        this.getContentPane().removeAll();
        this.buddyArea = new BuddyArea(this);
        this.getContentPane().add(buddyArea);
        this.buddyArea.updateVillenTree();        
        this.setResizable(false);
        this.pack();
        this.setVisible(true);

    }

    public XmppVillein getVillein() {
        return this.villein;
    }

    public void shutDown() {
        if (this.villein != null)
            this.villein.shutDown();
        System.exit(0);
    }

    public static void main(String[] args) {
        new VilleinGui();
    }

}
