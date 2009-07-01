package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.VMScheduler;
import gov.lanl.cnls.linkedprocess.os.errors.UnsupportedScriptEngineException;
import gov.lanl.cnls.linkedprocess.os.errors.VMAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.os.errors.VMSchedulerIsFullException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:02:49 AM
 */
public class XmppFarm extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppFarm.class);
    public static final String RESOURCE_PREFIX = "LoPFarm";
    public static final String STATUS_MESSAGE = "LoP Farm v0.1";

    // FIXME: reuse VMScheduler.FarmStatus.  In particular, "too many jobs" will not happen.  "Too many VMs" will happen.
    public static enum FarmStatus {
        AVAILABLE, UNAVAILABLE, TOO_MANY_JOBS
    }

    protected final Map<String, XmppVirtualMachine> machines;
    protected final VMScheduler scheduler;
    protected FarmStatus currentStatus;

    public XmppFarm(final String server, final int port, final String username, final String password) {
    	InputStream resourceAsStream = getClass().getResourceAsStream("/logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(resourceAsStream);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        LOGGER.info("Starting LoP farm");

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE, new SpawnVmProvider());
        pm.addIQProvider(LinkedProcess.TERMINATE_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE, new TerminateVmProvider());

        try {
            this.logon(server, port, username, password);
            this.initiateFeatures();
            //this.printClientStatistics();
        } catch (XMPPException e) {
            LOGGER.severe("error: " + e);
            System.exit(1);
        }

        this.roster = this.connection.getRoster();
        this.roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        this.scheduler = new VMScheduler(new VMJobResultHandler(this));
        this.machines = new HashMap<String, XmppVirtualMachine>();

        PacketFilter spawnFilter = new AndFilter(new PacketTypeFilter(SpawnVm.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter destroyFilter = new AndFilter(new PacketTypeFilter(TerminateVm.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter subscribeFilter = new AndFilter(new PacketTypeFilter(Presence.class), new PresenceSubscriptionFilter());
        connection.addPacketListener(new SpawnVmListener(this), spawnFilter);
        connection.addPacketListener(new TerminateVmListener(this), destroyFilter);
        connection.addPacketListener(new PresenceSubscriptionListener(this), subscribeFilter);
    }

    public void logon(String server, int port, String username, String password) throws XMPPException {
        super.logon(server, port, username, password, RESOURCE_PREFIX);
        connection.sendPacket(this.createFarmPresence(FarmStatus.AVAILABLE));
    }

    public Presence createFarmPresence(final FarmStatus status) {
        switch (status) {
            case AVAILABLE:
                return new Presence(Presence.Type.available, STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
            case TOO_MANY_JOBS:
                return new Presence(Presence.Type.available, STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.dnd);
            case UNAVAILABLE:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    public VMScheduler getScheduler() {
        return this.scheduler;
    }

    public String spawnVirtualMachine(String vmSpecies) throws VMAlreadyExistsException, VMSchedulerIsFullException, UnsupportedScriptEngineException {
        XmppVirtualMachine vm = new XmppVirtualMachine(this.getServer(), this.getPort(), this.getUsername(), this.getPassword(), this);
        String fullJid = vm.getFullJid();
        boolean exceptionThrown = true;
        try {
            this.scheduler.spawnVirtualMachine(fullJid, vmSpecies);
            exceptionThrown = false;

        } finally {
            if (exceptionThrown) {
                vm.shutDown();
            }
             this.machines.put(fullJid, vm);
            System.out.println(this.machines);
        }

        return fullJid;
    }

    public void destroyVirtualMachine(String vmJid) throws VMWorkerNotFoundException {
        XmppVirtualMachine vm = this.machines.get(vmJid);
        if (null != vm) {
            vm.shutDown();
            this.machines.remove(vmJid);
        }
        this.scheduler.terminateVirtualMachine(vmJid);
    }

    public XmppVirtualMachine getVirtualMachine(String vmJid) throws VMWorkerNotFoundException {
        XmppVirtualMachine vm = this.machines.get(vmJid);
        if(vm == null) {
            throw new VMWorkerNotFoundException("worker not found.");
        } else {
            return vm;
        }
    }

    public void shutDown() {
        this.connection.sendPacket(this.createFarmPresence(FarmStatus.UNAVAILABLE));
        this.scheduler.shutDown();
        try {
            this.scheduler.waitUntilFinished();
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
        super.shutDown();

    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        discoManager.addFeature(LinkedProcess.LOP_FARM_NAMESPACE);
    }
}
