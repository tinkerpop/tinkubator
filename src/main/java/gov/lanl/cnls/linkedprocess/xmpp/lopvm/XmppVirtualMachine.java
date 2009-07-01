package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.Job;
import gov.lanl.cnls.linkedprocess.os.errors.JobNotFoundException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerIsFullException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;

import java.util.logging.Logger;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 11:01:06 AM
 */
public class XmppVirtualMachine extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppVirtualMachine.class);
    public static String RESOURCE_PREFIX = "LoPVM";

    protected XmppFarm farm;

    public XmppVirtualMachine(final String server, final int port, final String username, final String password, XmppFarm farm) {

        this.farm = farm;

        LOGGER.info("Starting LoP virtual machine");
        // Registering the types of IQ packets/stanzas the the Lop VM can respond to.
        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(LinkedProcess.EVALUATE_TAG, LinkedProcess.LOP_VM_NAMESPACE, new EvaluateProvider());
        pm.addIQProvider(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new JobStatusProvider());
        pm.addIQProvider(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new AbortJobProvider());

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

        connection.addPacketListener(new EvaluateListener(this), evalFilter);
        connection.addPacketListener(new JobStatusListener(this), statusFilter);
        connection.addPacketListener(new AbortJobListener(this), abandonFilter);
    }

    protected void logon(String server, int port, String username, String password) throws XMPPException {

        super.logon(server, port, username, password, RESOURCE_PREFIX);
        connection.sendPacket(this.createPresence(LinkedProcess.VMStatus.ACTIVE));
    }

    public XmppFarm getFarm() {
        return this.farm;
    }

    public final Presence createPresence(final LinkedProcess.VMStatus status) {
        String statusMessage = "LoP v0.1";
        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, statusMessage, LinkedProcess.LOWEST_PRIORITY, Presence.Mode.available);
            case ACTIVE_FULL:
                return new Presence(Presence.Type.available, statusMessage, LinkedProcess.LOWEST_PRIORITY, Presence.Mode.dnd);
            case DOES_NOT_EXIST:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    public void abortJob(String jobId) throws VMWorkerNotFoundException, JobNotFoundException {
        this.farm.getScheduler().abortJob(this.getFullJid(), jobId);
    }

    public LinkedProcess.JobStatus getJobStatus(String jobId) throws VMWorkerNotFoundException {
        return this.farm.getScheduler().getJobStatus(this.getFullJid(), jobId);
    }

    public void scheduleJob(Job job) throws VMWorkerNotFoundException, VMWorkerIsFullException {
        this.farm.getScheduler().scheduleJob(this.getFullJid(), job);
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        discoManager.addFeature(LinkedProcess.LOP_VM_NAMESPACE);
    }

}
