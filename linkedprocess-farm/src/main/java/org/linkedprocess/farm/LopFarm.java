package org.linkedprocess.farm;

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
import org.linkedprocess.LopClient;
import org.linkedprocess.os.VmScheduler;
import org.linkedprocess.os.errors.UnsupportedScriptEngineException;
import org.linkedprocess.os.errors.VmAlreadyExistsException;
import org.linkedprocess.os.errors.VmSchedulerIsFullException;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;
import org.linkedprocess.security.ServiceDiscoveryConfiguration;
import org.linkedprocess.security.SystemInfo;
import org.linkedprocess.security.VmSecurityManager;
import org.linkedprocess.vm.LopVm;

import javax.script.ScriptEngineFactory;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class LopFarm extends LopClient {

    public static Logger LOGGER = LinkedProcess.getLogger(LopFarm.class);
    public static final String RESOURCE_PREFIX = "LoPFarm";
    public static final String STATUS_MESSAGE = "LoPSideD Farm";

    protected String farmPassword;

    protected final Map<String, LopVm> machines;
    protected final VmScheduler vmScheduler;
    protected DataForm serviceExtension;

    public LopFarm(final String server, final int port, final String username, final String password, final String farmPassword) throws XMPPException {
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
        this.vmScheduler = new VmScheduler(new VmJobResultHandler(this), new StatusEventHandler(this));
        this.machines = new HashMap<String, LopVm>();

        PacketFilter spawnFilter = new AndFilter(new PacketTypeFilter(SpawnVm.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter subscribeFilter = new AndFilter(new PacketTypeFilter(Presence.class), new PresenceSubscriptionFilter());

        this.connection.addPacketListener(new SpawnVmPacketListener(this), spawnFilter);
        this.connection.addPacketListener(new PresenceSubscriptionPacketListener(this), subscribeFilter);
    }

    private void logon(String server, int port, String username, String password) throws XMPPException {
        super.logon(server, port, username, password, RESOURCE_PREFIX);
    }

    public void sendPresence(final LinkedProcess.FarmStatus status) {
        Presence presence;
        if (status == LinkedProcess.FarmStatus.ACTIVE) {
            presence = new Presence(Presence.Type.available, STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
        } else if (status == LinkedProcess.FarmStatus.ACTIVE_FULL) {
            presence = new Presence(Presence.Type.available, LopVm.STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.dnd);
        } else if (status == LinkedProcess.FarmStatus.INACTIVE) {
            presence = new Presence(Presence.Type.unavailable);
        } else {
            throw new IllegalStateException("unhandled state: " + status);
        }
        presence.setFrom(this.getFullJid());
        this.connection.sendPacket(presence);
    }

    public VmScheduler getVmScheduler() {
        return this.vmScheduler;
    }

    public LopVm spawnVirtualMachine(String spawningVilleinJid, String vmSpecies) throws VmAlreadyExistsException, VmSchedulerIsFullException, UnsupportedScriptEngineException {
        LopVm vm = new LopVm(this.getServer(), this.getPort(), this.getUsername(), this.getPassword(), this, spawningVilleinJid, vmSpecies, LinkedProcess.generateRandomPassword());
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

    public void terminateVirtualMachine(String vmJid) throws VmWorkerNotFoundException {
        LopVm vm = this.machines.get(vmJid);
        if (null != vm) {
            vm.shutdown();
            this.machines.remove(vmJid);
        }
    }

    public LopVm getVirtualMachine(String vmJid) throws VmWorkerNotFoundException {
        LopVm vm = this.machines.get(vmJid);
        if (vm == null) {
            throw new VmWorkerNotFoundException(vmJid);
        } else {
            return vm;
        }
    }

    public Collection<LopVm> getVirtualMachines() {
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
        super.shutdown();

    }

    public DataForm getServiceExtension() {
        return this.serviceExtension;
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(LopFarm.RESOURCE_PREFIX);
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
        }
        this.serviceExtension.addField(field);

        // Add security-related fields
        VmSecurityManager man = (VmSecurityManager) System.getSecurityManager();
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

    public void setStatusEventHandler(VmScheduler.LopStatusEventHandler statusHandler) {
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

        LopFarm farm = new LopFarm(server, port, username, password, null);
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
