package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.provider.ProviderManager;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;

import java.util.Map;
import java.util.HashMap;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:02:49 AM
 */
public class XmppFarm extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppFarm.class);
    public static final String RESOURCE_PREFIX = "LoPFarm/";
    public static final String STATUS_MESSAGE = "LoP Farm v0.1";
    public static enum FarmPresence { AVAILABLE, UNAVAILABLE, TOO_MANY_JOBS }

    protected Map<String, XmppVirtualMachine> machines;
    protected FarmPresence currentPresence;
    protected Roster roster;
    protected VMScheduler scheduler;

    public XmppFarm(final String server, final int port, final String username, final String password) throws Exception {

        LOGGER.info("Starting LoP farm");

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(Spawn.SPAWN_TAGNAME, LinkedProcess.LOP_FARM_NAMESPACE, new SpawnProvider());
        pm.addIQProvider(Destroy.DESTROY_TAGNAME, LinkedProcess.LOP_FARM_NAMESPACE, new DestroyProvider());

        try {
            this.logon(server, port, username, password);
            this.initiateFeatures();
            this.printClientStatistics();
        } catch (XMPPException e) {
            LOGGER.error("error: " + e);
            System.exit(1);
        }

        this.roster = connection.getRoster();
        this.roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        this.scheduler = new VMScheduler();
        this.machines = new HashMap<String, XmppVirtualMachine>();
        

        PacketFilter spawnFilter = new AndFilter(new PacketTypeFilter(Spawn.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter destroyFilter = new AndFilter(new PacketTypeFilter(Destroy.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter subscribeFilter = new AndFilter(new PacketTypeFilter(Presence.class), new PresenceSubscriptionFilter());
        connection.addPacketListener(new SpawnListener(this), spawnFilter);
        connection.addPacketListener(new DestroyListener(this), destroyFilter);
        connection.addPacketListener(new PresenceSubscriptionListener(this), subscribeFilter);    
    }

    public void logon(String server, int port, String username, String password) throws XMPPException {

        super.logon(server, port, username, password, RESOURCE_PREFIX);
        connection.sendPacket(this.createFarmPresence(FarmPresence.AVAILABLE));
    }

  
    public Presence createFarmPresence(final FarmPresence type) {

        if(type == FarmPresence.AVAILABLE) {
            return new Presence(Presence.Type.available, STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
        } else if(type == FarmPresence.TOO_MANY_JOBS) {
            return new Presence(Presence.Type.available, STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.dnd);
        } else {
            return new Presence(Presence.Type.unavailable);
        }
    }

    public Roster getRoster() {
        return this.roster;
    }

    public String spawnVirtualMachine() {
        try {
            XmppVirtualMachine vm = new XmppVirtualMachine(this.getServer(), this.getPort(), this.getUsername(), this.getPassword());
            this.scheduler.addMachine(vm.getFullJid(), "JavaScript");
            this.machines.put(vm.getFullJid(), vm);
            return vm.getFullJid();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void destroyVirtualMachine(String vmJid) {
        System.out.println(this.machines);
        System.out.println(vmJid);
        XmppVirtualMachine vm = this.machines.get(vmJid);
        vm.shutDown();
        this.machines.remove(vmJid);
    }
}
