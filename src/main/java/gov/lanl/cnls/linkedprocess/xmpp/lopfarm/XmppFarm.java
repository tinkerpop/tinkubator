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
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
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
    public static final String STATUS_MESSAGE_STARTING = STATUS_MESSAGE + " - starting";
    public static final String STATUS_MESSAGE_ACTIVE = STATUS_MESSAGE + " - active";
    public static final String STATUS_MESSAGE_FULL = STATUS_MESSAGE + " - full";
    public static final String STATUS_MESSAGE_TERMINATING = STATUS_MESSAGE + " - terminating";


    protected final Map<String, XmppVirtualMachine> machines;
    protected final VMScheduler scheduler;
    protected final VMScheduler.LopStatusEventHandler statusHandler;

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
        LOGGER.info("Starting " + STATUS_MESSAGE);

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE, new SpawnVmProvider());

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
        this.statusHandler = new StatusEventHandler(this);
        this.scheduler = new VMScheduler(new VMJobResultHandler(this), this.statusHandler);
        this.machines = new HashMap<String, XmppVirtualMachine>();

        PacketFilter spawnFilter = new AndFilter(new PacketTypeFilter(SpawnVm.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter subscribeFilter = new AndFilter(new PacketTypeFilter(Presence.class), new PresenceSubscriptionFilter());
        connection.addPacketListener(new SpawnVmListener(this), spawnFilter);
        connection.addPacketListener(new PresenceSubscriptionListener(this), subscribeFilter);
    }

    public void logon(String server, int port, String username, String password) throws XMPPException {
        super.logon(server, port, username, password, RESOURCE_PREFIX);
        connection.sendPacket(this.createPresence(LinkedProcess.FarmStatus.STARTING));
    }

    public Presence createPresence(final LinkedProcess.FarmStatus status) {
        switch (status) {
            case STARTING:
                return new Presence(Presence.Type.available, STATUS_MESSAGE_STARTING, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
            case ACTIVE:
                return new Presence(Presence.Type.available, STATUS_MESSAGE_ACTIVE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
            case ACTIVE_FULL:
                return new Presence(Presence.Type.available, STATUS_MESSAGE_FULL, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.dnd);
            case TERMINATING:
                return new Presence(Presence.Type.unavailable, STATUS_MESSAGE_TERMINATING, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.dnd);
            case TERMINATED:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    public VMScheduler getScheduler() {
        return this.scheduler;
    }

    public XmppVirtualMachine spawnVirtualMachine(String vmSpecies) throws VMAlreadyExistsException, VMSchedulerIsFullException, UnsupportedScriptEngineException {
        XmppVirtualMachine vm = new XmppVirtualMachine(this.getServer(), this.getPort(), this.getUsername(), this.getPassword(), this, XmppClient.generateRandomPassword());
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
        }

        return vm;
    }

    public void terminateVirtualMachine(String vmJid) throws VMWorkerNotFoundException {
        XmppVirtualMachine vm = this.machines.get(vmJid);
        if (null != vm) {
            vm.shutDown();
            this.machines.remove(vmJid);
        }
        this.scheduler.terminateVirtualMachine(vmJid);
    }

    public XmppVirtualMachine getVirtualMachine(String vmJid) throws VMWorkerNotFoundException {
        XmppVirtualMachine vm = this.machines.get(vmJid);
        if (vm == null) {
            throw new VMWorkerNotFoundException(vmJid);
        } else {
            return vm;
        }
    }

    public void shutDown() {

        String[] vmJids = new String[this.machines.size()];
        this.machines.keySet().toArray(vmJids);
        for (String vmJid : vmJids) {
            try {
                this.terminateVirtualMachine(vmJid);
            } catch (VMWorkerNotFoundException e) {
                LOGGER.severe(e.getMessage());
            }
        }

        this.scheduler.shutDown();
        this.connection.sendPacket(this.createPresence(LinkedProcess.FarmStatus.TERMINATED));
        try {
            this.scheduler.waitUntilFinished();
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
        super.shutDown();

    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
        discoManager.addFeature(LinkedProcess.LOP_FARM_NAMESPACE);

        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        for (ScriptEngineFactory factory : factories) {
            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();
            String feature = "lop:" + engName + "-" + engVersion + "/" + langName + "-" + langVersion;
            discoManager.addFeature(feature);
        }

        /*DataForm form = new DataForm("supported VMS");
        FormField field = new FormField("1234");
        field.setRequired(false);
        field.setLabel("JavaScript");
        ArrayList<FormField> list = new ArrayList<FormField>();
        list.add(field);
        form.addItem(new DataForm.Item(list));*/

    }
}
