package org.linkedprocess.xmpp.villein.operations;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.structs.JobStruct;
import org.linkedprocess.xmpp.villein.structs.VmStruct;
import org.linkedprocess.xmpp.vm.SubmitJob;

/**
 * User: marko
 * Date: Aug 7, 2009
 * Time: 4:30:42 PM
 */
public class SubmitJobOperation extends Operation {

    private final HandlerSet<JobStruct> resultHandlers;

    public SubmitJobOperation(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<JobStruct>();
    }

    public void send(final VmStruct vmStruct, final JobStruct jobStruct, final Handler<JobStruct> resultHandler, final Handler<XMPPError> errorHandler) {

        if (null == jobStruct.getJobId())
            jobStruct.setJobId(Packet.nextID());

        SubmitJob submitJob = new SubmitJob();
        submitJob.setTo(vmStruct.getFullJid());
        submitJob.setFrom(xmppVillein.getFullJid());
        submitJob.setExpression(jobStruct.getExpression());
        submitJob.setVmPassword(vmStruct.getVmPassword());
        submitJob.setType(IQ.Type.GET);
        submitJob.setPacketID(jobStruct.getJobId());

        this.resultHandlers.addHandler(jobStruct.getJobId(), resultHandler);
        this.errorHandlers.addHandler(jobStruct.getJobId(), errorHandler);

        xmppVillein.getConnection().sendPacket(submitJob);
    }

    public void receiveNormal(final SubmitJob submitJob) {
        try {
            JobStruct jobStruct = new JobStruct();
            jobStruct.setResult(submitJob.getExpression());
            jobStruct.setJobId(submitJob.getPacketID());
            resultHandlers.handle(submitJob.getPacketID(), jobStruct);
        } finally {
            resultHandlers.removeHandler(submitJob.getPacketID());
            errorHandlers.removeHandler(submitJob.getPacketID());
        }

    }

    public void receiveError(final SubmitJob submitJob) {
        try {
            errorHandlers.handle(submitJob.getPacketID(), submitJob.getError());
        } finally {
            resultHandlers.removeHandler(submitJob.getPacketID());
            errorHandlers.removeHandler(submitJob.getPacketID());
        }
    }
}
