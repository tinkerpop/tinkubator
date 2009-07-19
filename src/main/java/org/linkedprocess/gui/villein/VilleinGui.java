package org.linkedprocess.gui.villein;

import org.linkedprocess.Connection;
import org.linkedprocess.xmpp.villein.VmStruct;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.vm.SubmitJob;
import org.linkedprocess.xmpp.vm.AbortJob;
import org.linkedprocess.xmpp.vm.ManageBindings;
import org.linkedprocess.xmpp.farm.SpawnVm;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;

import javax.swing.JFrame;
import java.util.HashMap;
import java.util.Map;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 10:33:02 PM
 */
public class VilleinGui extends JFrame {

    protected static final String FRAME_TITLE = "Simple Linked Process Villein";

    protected XmppVillein xmppVillein;
    protected HostArea hostArea;
    protected Map<String, VmControlFrame> vmFrames = new HashMap<String, VmControlFrame>();

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

    public void loadHostArea(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;

        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
        PacketFilter submitFilter = new AndFilter(new PacketTypeFilter(SubmitJob.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter abortFilter = new AndFilter(new PacketTypeFilter(AbortJob.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter spawnFilter = new AndFilter(new PacketTypeFilter(SpawnVm.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter bindingsFilter = new AndFilter(new PacketTypeFilter(ManageBindings.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));


        this.xmppVillein.getConnection().addPacketListener(new PresenceListener(this), presenceFilter);
        this.xmppVillein.getConnection().addPacketListener(new SubmitJobListener(this), submitFilter);
        this.xmppVillein.getConnection().addPacketListener(new AbortJobListener(this), abortFilter);
        this.xmppVillein.getConnection().addPacketListener(new SpawnVmListener(this), spawnFilter);
        this.xmppVillein.getConnection().addPacketListener(new ManageBindingsListener(this), bindingsFilter);

        this.getContentPane().removeAll();
        this.hostArea = new HostArea(this);
        this.hostArea.createTree();
        this.getContentPane().add(hostArea);
        this.setResizable(false);
        this.pack();
        this.setSize(440,488);
        this.setVisible(true);

    }

    public void addVmFrame(VmStruct vmStruct) {
        VmControlFrame vmControlFrame = new VmControlFrame(vmStruct, this);
        this.vmFrames.put(vmStruct.getFullJid(), vmControlFrame);
    }

    public void removeVmFrame(VmStruct vmStruct) {
        VmControlFrame vmControlFrame = this.vmFrames.remove(vmStruct.getFullJid());
        if(vmControlFrame != null) {
            vmControlFrame.setVisible(false);
            vmControlFrame = null;
        }
    }

    public VmControlFrame getVmFrame(String vmJid) {
        return this.vmFrames.get(vmJid);
    }

    public void updateHostAreaTree(String jid, boolean remove) {
        this.hostArea.updateTree(jid, remove);
    }

    public void createTree() {
        this.hostArea.createTree();
    }

    public XmppVillein getXmppVillein() {
        return this.xmppVillein;
    }

    public HostArea getHostArea() {
        return this.hostArea;
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
