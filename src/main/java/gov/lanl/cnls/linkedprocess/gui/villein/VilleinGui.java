package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.gui.villein.VilleinLoginArea;
import gov.lanl.cnls.linkedprocess.xmpp.villein.*;
import gov.lanl.cnls.linkedprocess.xmpp.vm.Evaluate;
import gov.lanl.cnls.linkedprocess.Connection;

import javax.swing.*;

import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.XMPPConnection;

import java.util.Map;
import java.util.HashMap;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 10:33:02 PM
 */
public class VilleinGui extends JFrame {

    protected static final String FRAME_TITLE = "Simple Linked Process Villein";

    protected XmppVillein xmppVillein;
    protected BuddyArea buddyArea;
    protected Map<String, VmFrame> vmFrames = new HashMap<String, VmFrame>();

    public VilleinGui() {
        super(VilleinGui.FRAME_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.loadLoginFrame();
    }

    public Connection getConnection() {
        return this.xmppVillein.getConnection();
    }

    public void loadLoginFrame() {
        this.getContentPane().removeAll();
        this.getContentPane().add(new VilleinLoginArea(this));
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }

    public void loadBuddyArea(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;

        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
        PacketFilter evaluateFilter = new AndFilter(new PacketTypeFilter(Evaluate.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        this.xmppVillein.getConnection().addPacketListener(new PresenceGuiListener(this), presenceFilter);
        this.xmppVillein.getConnection().addPacketListener(new EvaluateGuiListener(this), evaluateFilter);

        this.getContentPane().removeAll();
        this.buddyArea = new BuddyArea(this);
        this.buddyArea.createTree();
        this.getContentPane().add(buddyArea);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);

    }

    public void addVmFrame(VmStruct vmStruct) {
        VmFrame vmFrame = new VmFrame(vmStruct, this);
        this.vmFrames.put(vmStruct.getFullJid(), vmFrame);
    }

    public VmFrame getVmFrame(String vmJid) {
        return this.vmFrames.get(vmJid);
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
