package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.gui.villein.LoginArea;
import gov.lanl.cnls.linkedprocess.xmpp.villein.*;
import gov.lanl.cnls.linkedprocess.xmpp.vm.AbortJob;
import gov.lanl.cnls.linkedprocess.xmpp.vm.Evaluate;
import gov.lanl.cnls.linkedprocess.xmpp.vm.JobStatus;
import gov.lanl.cnls.linkedprocess.xmpp.vm.TerminateVm;
import gov.lanl.cnls.linkedprocess.xmpp.farm.SpawnVm;

import javax.swing.*;

import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 10:33:02 PM
 */
public class VilleinGui extends JFrame {

    protected static final String FRAME_TITLE = "Simple Linked Process Villein";

    protected XmppVillein xmppVillein;
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
        this.xmppVillein = villein;

        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
        this.xmppVillein.getConnection().addPacketListener(new PresenceGuiListener(this), presenceFilter);

        this.getContentPane().removeAll();
        this.buddyArea = new BuddyArea(this);
        this.buddyArea.createTree();
        this.getContentPane().add(buddyArea);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);

    }

    public void updateTree(String jid, boolean remove) {
        this.buddyArea.updateTree(jid, remove);
    }

    public void createTree() {
        this.buddyArea.createTree();
    }

    public XmppVillein getXmppVillein() {
        return this.xmppVillein;
    }

    public BuddyArea getBuddyArea() {
        return this.buddyArea;
    }

    public void shutDown() {
        if (this.xmppVillein != null)
            this.xmppVillein.shutDown();
        System.exit(0);
    }

    public static void main(String[] args) {
        new VilleinGui();
    }

}
