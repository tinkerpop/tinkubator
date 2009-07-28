package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DataForm;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VMScheduler;
import org.linkedprocess.os.errors.UnsupportedScriptEngineException;
import org.linkedprocess.os.errors.VMAlreadyExistsException;
import org.linkedprocess.os.errors.VMSchedulerIsFullException;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.security.ServiceDiscoveryConfiguration;
import org.linkedprocess.security.SystemInfo;
import org.linkedprocess.security.VMSecurityManager;
import org.linkedprocess.xmpp.XmppClient;
import org.linkedprocess.xmpp.vm.XmppVirtualMachine;

import javax.script.ScriptEngineFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    public static final String STATUS_MESSAGE_ACTIVE = STATUS_MESSAGE + " - active";
    public static final String STATUS_MESSAGE_FULL = STATUS_MESSAGE + " - full";

    protected String farmPassword;

    protected final Map<String, XmppVirtualMachine> machines;
    protected final VMScheduler vmScheduler;
    protected DataForm serviceExtension;

    public XmppFarm(final String server, final int port, final String username, final String password) throws XMPPException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(resourceAsStream);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Starting " + STATUS_MESSAGE);

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE, new SpawnVmProvider());


        this.logon(server, port, username, password);
        this.initiateFeatures();
        //this.printClientStatistics();

        this.roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        this.vmScheduler = new VMScheduler(new VMJobResultHandler(this), new StatusEventHandler(this));
        this.machines = new HashMap<String, XmppVirtualMachine>();

        PacketFilter spawnFilter = new AndFilter(new PacketTypeFilter(SpawnVm.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter subscribeFilter = new AndFilter(new PacketTypeFilter(Presence.class), new PresenceSubscriptionFilter());

        this.connection.addPacketListener(new SpawnVmListener(this), spawnFilter);
        this.connection.addPacketListener(new PresenceSubscriptionListener(this), subscribeFilter);
    }

    private void logon(String server, int port, String username, String password) throws XMPPException {
        super.logon(server, port, username, password, RESOURCE_PREFIX);
    }

    public Presence createPresence(final LinkedProcess.FarmStatus status) {
        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, STATUS_MESSAGE_ACTIVE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
            case ACTIVE_FULL:
                return new Presence(Presence.Type.available, STATUS_MESSAGE_FULL, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.dnd);
            case TERMINATED:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    public VMScheduler getVmScheduler() {
        return this.vmScheduler;
    }

    public XmppVirtualMachine spawnVirtualMachine(String spawningAppJid, String vmSpecies) throws VMAlreadyExistsException, VMSchedulerIsFullException, UnsupportedScriptEngineException {
        XmppVirtualMachine vm = new XmppVirtualMachine(this.getServer(), this.getPort(), this.getUsername(), this.getPassword(), this, spawningAppJid, vmSpecies, XmppClient.generateRandomPassword());
        String vmJid = vm.getFullJid();
        this.machines.put(vmJid, vm);
        boolean exceptionThrown = true;
        try {
            this.vmScheduler.spawnVirtualMachine(vmJid, vmSpecies);
            exceptionThrown = false;

        } finally {
            if (exceptionThrown) {
                vm.shutDown();
                this.machines.remove(vmJid);
            }
        }
        return vm;
    }

    public void terminateVirtualMachine(String vmJid) throws VMWorkerNotFoundException {
        XmppVirtualMachine vm = this.machines.get(vmJid);
        if (null != vm) {
            vm.shutDown();
            this.machines.remove(vmJid);
        }
    }

    public XmppVirtualMachine getVirtualMachine(String vmJid) throws VMWorkerNotFoundException {
        XmppVirtualMachine vm = this.machines.get(vmJid);
        if (vm == null) {
            throw new VMWorkerNotFoundException(vmJid);
        } else {
            return vm;
        }
    }

    public VMScheduler getVMScheduler() {
        return this.vmScheduler;
    }

    public Collection<XmppVirtualMachine> getVirtualMachines() {
        return this.machines.values();
    }

    public void shutDown() {
        LOGGER.info("shutting down XmppFarm");

        this.vmScheduler.shutDown();
        try {
            this.vmScheduler.waitUntilFinished();
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
        super.shutDown(this.createPresence(LinkedProcess.FarmStatus.TERMINATED));

    }

    public DataForm getServiceExtension() {
        return this.serviceExtension;
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(XmppFarm.RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
        discoManager.addFeature(LinkedProcess.LOP_FARM_NAMESPACE);

        this.serviceExtension = new DataForm(Form.TYPE_RESULT);

        FormField field = new FormField("vm_species");
        field.setRequired(true);
        field.setLabel("supported virtual machine species");
        field.setType(FormField.TYPE_LIST_SINGLE);
        List<ScriptEngineFactory> factories = LinkedProcess.getSupportedScriptEngineFactories();
        for (ScriptEngineFactory factory : factories) {
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();
            String engName = factory.getEngineName();
            String engVersion;
            try {
                engVersion = factory.getEngineVersion();
            } catch (java.lang.NoClassDefFoundError e) {
                // FIXME: temporary kludge for a Groovy dependency issue.
                engVersion = "?";
            }

            String value = langName.toLowerCase();
            String label = langName + " " + langVersion + " (" + engName + " " + engVersion + ")";
            field.addOption(new FormField.Option(label, value));
            //field.addOption(new FormField.Option(engName + ":" + engVersion, factory.getNames().get(0)));
        }
        this.serviceExtension.addField(field);

        // Add security-related fields
        VMSecurityManager man = (VMSecurityManager) System.getSecurityManager();
        ServiceDiscoveryConfiguration conf = new ServiceDiscoveryConfiguration(man);
        conf.addFields(this.serviceExtension);

        // Add system info
        SystemInfo.addFields(this.serviceExtension);

        discoManager.setExtendedInfo(this.serviceExtension);
    }

    public void setStatusEventHandler(VMScheduler.LopStatusEventHandler statusHandler) {
        this.vmScheduler.setStatusEventHandler(statusHandler);
    }

    public String getFarmPassword() {
        return this.farmPassword;
    }

    public static void main(final String[] args) throws Exception {
        Properties props = LinkedProcess.getConfiguration();
        String server = props.getProperty(LinkedProcess.FARM_SERVER);
        int port = Integer.valueOf(props.getProperty(LinkedProcess.FARM_PORT));
        String userName = props.getProperty(LinkedProcess.FARM_USERNAME);
        String password = props.getProperty(LinkedProcess.FARM_PASSWORD);

        XmppFarm farm = new XmppFarm(server, port, userName, password);
        StatusEventHandler h = new StatusEventHandler(farm);
        farm.setStatusEventHandler(h);

        Object o = "";
        try {
            synchronized (o) {
                // Never break out until the process is killed.
                while (true) {
                    o.wait();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
