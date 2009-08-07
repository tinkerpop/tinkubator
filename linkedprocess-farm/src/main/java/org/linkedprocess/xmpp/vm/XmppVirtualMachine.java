package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DataForm;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.Job;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.errors.JobAlreadyExistsException;
import org.linkedprocess.os.errors.JobNotFoundException;
import org.linkedprocess.os.errors.VMWorkerIsFullException;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.XmppClient;
import org.linkedprocess.xmpp.farm.XmppFarm;

import java.util.Set;
import java.util.logging.Logger;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 11:01:06 AM
 */
public class XmppVirtualMachine extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppVirtualMachine.class);
    public static String RESOURCE_PREFIX = "LoPVM";
    public static String STATUS_MESSAGE = "LoPSideD Virtual Machine";

    protected final XmppFarm farm;
    protected final String vmPassword;
    protected final String vmSpecies;
    protected final String villeinJid;

    public XmppVirtualMachine(final String server, final int port, final String username, final String password, XmppFarm farm, final String villeinJid, final String vmSpecies, final String vmPassword) {

        this.farm = farm;
        this.vmPassword = vmPassword;
        this.vmSpecies = vmSpecies;
        this.villeinJid = villeinJid;

        LOGGER.info("Starting LoP virtual machine - password:" + vmPassword);
        // Registering the types of IQ packets/stanzas the the Lop VM can respond to.
        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(LinkedProcess.SUBMIT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new SubmitJobProvider());
        pm.addIQProvider(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new JobStatusProvider());
        pm.addIQProvider(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new AbortJobProvider());
        pm.addIQProvider(LinkedProcess.TERMINATE_VM_TAG, LinkedProcess.LOP_VM_NAMESPACE, new TerminateVmProvider());
        pm.addIQProvider(LinkedProcess.MANAGE_BINDINGS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new ManageBindingsProvider());

        try {
            this.logon(server, port, username, password);
            this.initiateFeatures();
            //this.printClientStatistics();
        } catch (XMPPException e) {
            LOGGER.severe("error: " + e);
            System.exit(1);
        }

        PacketFilter submitFilter = new AndFilter(new PacketTypeFilter(SubmitJob.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter statusFilter = new AndFilter(new PacketTypeFilter(JobStatus.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter abandonFilter = new AndFilter(new PacketTypeFilter(AbortJob.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter terminateFilter = new AndFilter(new PacketTypeFilter(TerminateVm.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter bindingsFilter = new AndFilter(new PacketTypeFilter(ManageBindings.class), new OrFilter(new IQTypeFilter(IQ.Type.GET), new IQTypeFilter(IQ.Type.SET)));
        
        this.connection.addPacketListener(new SubmitJobListener(this), submitFilter);
        this.connection.addPacketListener(new JobStatusListener(this), statusFilter);
        this.connection.addPacketListener(new AbortJobListener(this), abandonFilter);
        this.connection.addPacketListener(new TerminateVmListener(this), terminateFilter);
        this.connection.addPacketListener(new ManageBindingsListener(this), bindingsFilter);

    }

    protected void logon(String server, int port, String username, String password) throws XMPPException {
        super.logon(server, port, username, password, RESOURCE_PREFIX);
    }

    public XmppFarm getFarm() {
        return this.farm;
    }

    public final Presence createPresence(final LinkedProcess.VmStatus status) {

        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, XmppVirtualMachine.STATUS_MESSAGE, LinkedProcess.LOWEST_PRIORITY, Presence.Mode.available);
            case ACTIVE_FULL:
                return new Presence(Presence.Type.available, XmppVirtualMachine.STATUS_MESSAGE, LinkedProcess.LOWEST_PRIORITY, Presence.Mode.dnd);
            case NOT_FOUND:
                return new Presence(Presence.Type.unavailable);
            case INACTIVE:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    public void abortJob(String jobId) throws VMWorkerNotFoundException, JobNotFoundException {
        this.farm.getVmScheduler().abortJob(this.getFullJid(), jobId);
    }

    public LinkedProcess.JobStatus getJobStatus(String jobId) throws VMWorkerNotFoundException, JobNotFoundException {
        return this.farm.getVmScheduler().getJobStatus(this.getFullJid(), jobId);
    }

    public void scheduleJob(Job job) throws VMWorkerNotFoundException, VMWorkerIsFullException, JobAlreadyExistsException {
        this.farm.getVmScheduler().scheduleJob(this.getFullJid(), job);
    }

    public void setBindings(VMBindings bindings) throws VMWorkerNotFoundException {
        this.farm.getVmScheduler().setBindings(this.getFullJid(), bindings);
    }

    public VMBindings getBindings(Set<String> names) throws VMWorkerNotFoundException {
        return this.farm.getVmScheduler().getBindings(this.getFullJid(), names);
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(XmppVirtualMachine.RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
        this.getDiscoManager().addFeature(LinkedProcess.LOP_VM_NAMESPACE);

        DataForm serviceExtension = new DataForm(Form.TYPE_RESULT);

        FormField field = new FormField("vm_start_time");
        field.setLabel("the xsd:datetime of  when the virtual machine was started");
        field.setType(FormField.TYPE_LIST_SINGLE);
        field.addValue(this.getStartTimeAsXsdDateTime());
        serviceExtension.addField(field);
        this.getDiscoManager().setExtendedInfo(serviceExtension);
    }

    public void terminateSelf() throws VMWorkerNotFoundException {
        this.farm.terminateVirtualMachine(this.getFullJid());
    }

    public boolean checkVmPassword(String vmPassword) {
        return this.vmPassword.equals(vmPassword);
    }

    public String getVmPassword() {
        return this.vmPassword;
    }

    public String getVmSpecies() {
        return this.vmSpecies;
    }

    public String getVilleinJid() {
        return this.villeinJid;
    }

    public void shutDown() {
        super.shutDown(this.createPresence(LinkedProcess.VmStatus.INACTIVE));

    }

    public LinkedProcess.VmStatus getVmStatus() {
        return this.farm.getVmScheduler().getVirtualMachineStatus(this.getFullJid());
    }

}
