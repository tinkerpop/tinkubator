package org.linkedprocess.xmpp.villein.operations;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.vm.PingJob;
import org.linkedprocess.xmpp.LopError;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.Packet;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 6, 2009
 * Time: 5:50:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class PingJobCommand extends Command {
    private final HandlerSet<LinkedProcess.JobStatus> resultHandlers;
    private final HandlerSet<LopError> errorHandlers;

    public PingJobCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<LinkedProcess.JobStatus>();
        this.errorHandlers = new HandlerSet<LopError>();
    }
    
    public void send(VmProxy vmStruct, JobStruct jobStruct, final Handler<LinkedProcess.JobStatus> statusHandler, final Handler<LopError> errorHandler) {

        String id = Packet.nextID();
        PingJob pingJob = new PingJob();
        pingJob.setTo(vmStruct.getFullJid());
        pingJob.setFrom(this.xmppVillein.getFullJid());
        pingJob.setJobId(jobStruct.getJobId());
        pingJob.setVmPassword(vmStruct.getVmPassword());
        pingJob.setType(IQ.Type.GET);
        pingJob.setPacketID(id);

        this.resultHandlers.addHandler(id, statusHandler);
        this.errorHandlers.addHandler(id, errorHandler);

        xmppVillein.getConnection().sendPacket(pingJob);
    }

    public void receiveNormal(final PingJob pingJob) {
        try {
            resultHandlers.handle(pingJob.getPacketID(), LinkedProcess.JobStatus.valueOf(pingJob.getValue()));
        } finally {
            resultHandlers.removeHandler(pingJob.getPacketID());
            errorHandlers.removeHandler(pingJob.getPacketID());
        }
    }

    public void receiveError(final PingJob pingJob) {
        try {
            errorHandlers.handle(pingJob.getPacketID(), pingJob.getLopError());
        } finally {
            resultHandlers.removeHandler(pingJob.getPacketID());
            errorHandlers.removeHandler(pingJob.getPacketID());
        }
    }
}
