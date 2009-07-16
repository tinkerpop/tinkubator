package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.Connection;
import gov.lanl.cnls.linkedprocess.xmpp.villein.VmStruct;
import gov.lanl.cnls.linkedprocess.xmpp.villein.XmppVillein;
import gov.lanl.cnls.linkedprocess.xmpp.vm.SubmitJob;
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

    public void loadHostArea(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;

        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
        PacketFilter evaluateFilter = new AndFilter(new PacketTypeFilter(SubmitJob.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        this.xmppVillein.getConnection().addPacketListener(new PresenceGuiListener(this), presenceFilter);
        this.xmppVillein.getConnection().addPacketListener(new EvaluateGuiListener(this), evaluateFilter);

        this.getContentPane().removeAll();
        this.hostArea = new HostArea(this);
        this.hostArea.createTree();
        this.getContentPane().add(hostArea);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);

    }

    public void addVmFrame(VmStruct vmStruct) {
        VmFrame vmFrame = new VmFrame(vmStruct, this);
        this.vmFrames.put(vmStruct.getFullJid(), vmFrame);
    }

    public void removeVmFrame(VmStruct vmStruct) {
        VmFrame vmFrame = this.vmFrames.remove(vmStruct.getFullJid());
        if(vmFrame != null) {
            vmFrame.setVisible(false);
            vmFrame = null;
        }
    }

    public VmFrame getVmFrame(String vmJid) {
        return this.vmFrames.get(vmJid);
    }

    public void updateTree(String jid, boolean remove) {
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
