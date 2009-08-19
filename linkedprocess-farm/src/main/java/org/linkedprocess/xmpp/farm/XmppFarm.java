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
import org.linkedprocess.LinkedProcessFarm;
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
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class XmppFarm extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppFarm.class);
    public static final String RESOURCE_PREFIX = "LoPFarm";
    public static final String STATUS_MESSAGE = "LoPSideD Farm";

    protected String farmPassword;

    protected final Map<String, XmppVirtualMachine> machines;
    protected final VMScheduler vmScheduler;
    protected DataForm serviceExtension;

    public XmppFarm(final String server, final int port, final String username, final String password, final String farmPassword) throws XMPPException {
        LOGGER.info("Starting " + STATUS_MESSAGE);
        if (null == farmPassword) {
            this.farmPassword = LinkedProcess.getConfiguration().getProperty(LinkedProcess.FARM_PASSWORD_PROPERTY);
            if (null != this.farmPassword) {
                this.farmPassword = this.farmPassword.trim();
            }
        } else {
            this.farmPassword = farmPassword;
        }
        LOGGER.info("LoPSideD Farm Password: " + this.farmPassword);

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
                return new Presence(Presence.Type.available, STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
            case ACTIVE_FULL:
                return new Presence(Presence.Type.unavailable);
            case INACTIVE:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    public VMScheduler getVmScheduler() {
        return this.vmScheduler;
    }

    public XmppVirtualMachine spawnVirtualMachine(String spawningAppJid, String vmSpecies) throws VMAlreadyExistsException, VMSchedulerIsFullException, UnsupportedScriptEngineException {
        XmppVirtualMachine vm = new XmppVirtualMachine(this.getServer(), this.getPort(), this.getUsername(), this.getPassword(), this, spawningAppJid, vmSpecies, LinkedProcess.generateRandomPassword());
        String vmJid = vm.getFullJid();
        this.machines.put(vmJid, vm);
        boolean exceptionThrown = true;
        try {
            this.vmScheduler.spawnVirtualMachine(vmJid, vmSpecies);
            exceptionThrown = false;

        } finally {
            if (exceptionThrown) {
                vm.shutdown();
                this.machines.remove(vmJid);
            }
        }
        return vm;
    }

    public void terminateVirtualMachine(String vmJid) throws VMWorkerNotFoundException {
        XmppVirtualMachine vm = this.machines.get(vmJid);
        if (null != vm) {
            vm.shutdown();
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

    public void shutdown() {
        LOGGER.info("shutting down XmppFarm");

        this.vmScheduler.shutdown();
        try {
            this.vmScheduler.waitUntilFinished();
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
        super.shutdown(this.createPresence(LinkedProcess.FarmStatus.INACTIVE));

    }

    public DataForm getServiceExtension() {
        return this.serviceExtension;
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(XmppFarm.RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
        this.getDiscoManager().addFeature(LinkedProcess.LOP_FARM_NAMESPACE);

        this.serviceExtension = new DataForm(Form.TYPE_RESULT);

        // Add VM species
        FormField field = new FormField("vm_species");
        field.setRequired(true);
        field.setLabel("supported virtual machine species");
        field.setType(FormField.TYPE_LIST_SINGLE);
        List<ScriptEngineFactory> factories = LinkedProcessFarm.getSupportedScriptEngineFactories();
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

        // Add configuration-based fields
        addConfigurationBasedFields(this.serviceExtension);

        this.getDiscoManager().setExtendedInfo(this.serviceExtension);
    }

    // TODO: move this
    private void addConfigurationBasedFields(final DataForm serviceExtension) {
        serviceExtension.addField(
                ConfigurationBasedField.FARM_START_TIME.toField(
                        this.getStartTimeAsXsdDateTime()));

        serviceExtension.addField(
                ConfigurationBasedField.FARM_PASSWORD_REQUIRED.toField(
                        "" + (null != this.farmPassword)));

        Properties p = LinkedProcess.getConfiguration();
        serviceExtension.addField(
                ConfigurationBasedField.MAX_CONCURRENT_VIRTUAL_MACHINES.toField(
                        p.getProperty(LinkedProcess.MAX_CONCURRENT_VIRTUAL_MACHINES_PROPERTY)));
        serviceExtension.addField(
                ConfigurationBasedField.JOB_QUEUE_CAPACITY.toField(
                        p.getProperty(LinkedProcess.JOB_QUEUE_CAPACITY_PROPERTY)));
        serviceExtension.addField(
                ConfigurationBasedField.JOB_TIMEOUT.toField(
                        p.getProperty(LinkedProcess.JOB_TIMEOUT_PROPERTY)));
        serviceExtension.addField(
                ConfigurationBasedField.VIRTUAL_MACHINE_TIME_TO_LIVE.toField(
                        p.getProperty(LinkedProcess.VIRTUAL_MACHINE_TIME_TO_LIVE_PROPERTY)));
    }

    // TODO: move this
    private enum ConfigurationBasedField {
        MAX_CONCURRENT_VIRTUAL_MACHINES(FormField.TYPE_TEXT_SINGLE, LinkedProcess.MAX_CONCURRENT_VIRTUAL_MACHINES, "the number of concurrent virtual machines which this farm can support before it rejects a request to spawn a new virtual machine"),
        JOB_QUEUE_CAPACITY(FormField.TYPE_TEXT_SINGLE, LinkedProcess.JOB_QUEUE_CAPACITY, "the number of jobs which a virtual machine can hold in its queue before it rejects requests to submit additional jobs"),
        JOB_TIMEOUT(FormField.TYPE_TEXT_SINGLE, LinkedProcess.JOB_TIMEOUT, "the number of milliseconds for which a job may execute before it is aborted by this farm"),
        VIRTUAL_MACHINE_TIME_TO_LIVE(FormField.TYPE_TEXT_SINGLE, LinkedProcess.VM_TIME_TO_LIVE, "the number of milliseconds for which a virtual machine may exist before it is terminated by this farm"),
        FARM_PASSWORD_REQUIRED(FormField.TYPE_BOOLEAN, LinkedProcess.FARM_PASSWORD_REQUIRED, "whether a password is required to spawn additional virtual machines on this farm"),
        FARM_START_TIME(FormField.TYPE_TEXT_SINGLE, LinkedProcess.FARM_START_TIME, "the xsd:dateTime at which this farm was started");

        private final String formType;
        private final String specName;
        private final String label;

        private ConfigurationBasedField(final String formType,
                                        final String specName,
                                        final String label) {
            this.formType = formType;
            this.specName = specName;
            this.label = label;
        }

        public String getSpecName() {
            return specName;
        }

        public String getLabel() {
            return label;
        }

        // Note: only single-valued fields here.
        public FormField toField(final String value) {
            FormField field = new FormField(specName);
            field.setLabel(label);
            field.setType(formType);
            field.addValue(value);
            return field;
        }
    }

    public void setStatusEventHandler(VMScheduler.LopStatusEventHandler statusHandler) {
        this.vmScheduler.setStatusEventHandler(statusHandler);
    }

    public String getFarmPassword() {
        return this.farmPassword;
    }

    public static void main(final String[] args) throws Exception {
        Properties props = LinkedProcess.getConfiguration();
        String server = props.getProperty(LinkedProcess.FARM_SERVER_PROPERTY);
        int port = Integer.valueOf(props.getProperty(LinkedProcess.FARM_PORT_PROPERTY));
        String username = props.getProperty(LinkedProcess.FARM_USERNAME_PROPERTY);
        String password = props.getProperty(LinkedProcess.FARM_USERPASSWORD_PROPERTY);

        XmppFarm farm = new XmppFarm(server, port, username, password, null);
        StatusEventHandler h = new StatusEventHandler(farm);
        farm.setStatusEventHandler(h);

        Object monitor = new Object();
        try {
            synchronized (monitor) {
                // Never break out until the process is killed.
                while (true) {
                    monitor.wait();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
