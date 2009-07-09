package gov.lanl.cnls.linkedprocess.xmpp.vm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.Job;
import gov.lanl.cnls.linkedprocess.os.errors.JobNotFoundException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerIsFullException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import gov.lanl.cnls.linkedprocess.os.errors.JobAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.farm.XmppFarm;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import java.util.logging.Logger;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 11:01:06 AM
 */
public class XmppVirtualMachine extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppVirtualMachine.class);
    public static String RESOURCE_PREFIX = "LoPVM";

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
        pm.addIQProvider(LinkedProcess.EVALUATE_TAG, LinkedProcess.LOP_VM_NAMESPACE, new EvaluateProvider());
        pm.addIQProvider(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new JobStatusProvider());
        pm.addIQProvider(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new AbortJobProvider());
        pm.addIQProvider(LinkedProcess.TERMINATE_VM_TAG, LinkedProcess.LOP_VM_NAMESPACE, new TerminateVmProvider());

        try {
            this.logon(server, port, username, password);
            this.initiateFeatures();
            //this.printClientStatistics();
        } catch (XMPPException e) {
            LOGGER.severe("error: " + e);
            System.exit(1);
        }

        PacketFilter evalFilter = new AndFilter(new PacketTypeFilter(Evaluate.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter statusFilter = new AndFilter(new PacketTypeFilter(JobStatus.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter abandonFilter = new AndFilter(new PacketTypeFilter(AbortJob.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter terminateFilter = new AndFilter(new PacketTypeFilter(TerminateVm.class), new IQTypeFilter(IQ.Type.GET));

        this.addPacketListener(new EvaluateVmListener(this), evalFilter);
        this.addPacketListener(new JobStatusVmListener(this), statusFilter);
        this.addPacketListener(new AbortJobVmListener(this), abandonFilter);
        this.addPacketListener(new TerminateVmVmListener(this), terminateFilter);

    }

    protected void logon(String server, int port, String username, String password) throws XMPPException {

        super.logon(server, port, username, password, RESOURCE_PREFIX);
        connection.sendPacket(this.createPresence(LinkedProcess.VmStatus.ACTIVE));
    }

    public XmppFarm getFarm() {
        return this.farm;
    }

    public final Presence createPresence(final LinkedProcess.VmStatus status) {
        String statusMessage = "LoP VM v0.1";
        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, statusMessage, LinkedProcess.LOWEST_PRIORITY, Presence.Mode.available);
            case ACTIVE_FULL:
                return new Presence(Presence.Type.available, statusMessage, LinkedProcess.LOWEST_PRIORITY, Presence.Mode.dnd);
            case NOT_FOUND:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    public void abortJob(String jobId) throws VMWorkerNotFoundException, JobNotFoundException {
        this.farm.getScheduler().abortJob(this.getFullJid(), jobId);
    }

    public LinkedProcess.JobStatus getJobStatus(String jobId) throws VMWorkerNotFoundException, JobNotFoundException {
        return this.farm.getScheduler().getJobStatus(this.getFullJid(), jobId);
    }

    public void scheduleJob(Job job) throws VMWorkerNotFoundException, VMWorkerIsFullException, JobAlreadyExistsException {
        this.farm.getScheduler().scheduleJob(this.getFullJid(), job);
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
        discoManager.addFeature(LinkedProcess.LOP_VM_NAMESPACE);
    }

    protected void terminateSelf() throws VMWorkerNotFoundException  {
        this.farm.terminateVirtualMachine(this.getFullJid());
    }

    public boolean checkVmPassword(String vmPassword) {
        return this.vmPassword.equals(vmPassword);
    }

    public String getVmPassword() {
        return this.vmPassword;
    }

    public String getVmSpecies()  {
        return this.vmSpecies;     
    }

    public String getVilleinJid() {
        return this.villeinJid;
    }

    public void shutDown() {
        this.connection.sendPacket(this.createPresence(LinkedProcess.VmStatus.NOT_FOUND));
        super.shutDown();

    }

    public LinkedProcess.VmStatus getVmStatus() {
        return this.farm.getScheduler().getVirtualMachineStatus(this.getFullJid());
    }

}
