package org.linkedprocess.xmpp.villein.operations;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.structs.VmProxy;
import org.linkedprocess.xmpp.villein.structs.JobStruct;
import org.linkedprocess.xmpp.vm.JobStatus;
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
public class JobStatusOperation extends Operation {
    private final HandlerSet<LinkedProcess.JobStatus> resultHandlers;
    private final HandlerSet<XMPPError> errorHandlers;

    public JobStatusOperation(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<LinkedProcess.JobStatus>();
        this.errorHandlers = new HandlerSet<XMPPError>();
    }
    
    public void send(VmProxy vmStruct, JobStruct jobStruct, final Handler<LinkedProcess.JobStatus> statusHandler, final Handler<XMPPError> errorHandler) {

        String id = Packet.nextID();
        JobStatus jobStatus = new JobStatus();
        jobStatus.setTo(vmStruct.getFullJid());
        jobStatus.setFrom(this.xmppVillein.getFullJid());
        jobStatus.setJobId(jobStruct.getJobId());
        jobStatus.setVmPassword(vmStruct.getVmPassword());
        jobStatus.setType(IQ.Type.GET);
        jobStatus.setPacketID(id);

        this.resultHandlers.addHandler(id, statusHandler);
        this.errorHandlers.addHandler(id, errorHandler);

        xmppVillein.getConnection().sendPacket(jobStatus);
    }

    public void receiveNormal(final JobStatus jobStatus) {
        try {
            resultHandlers.handle(jobStatus.getPacketID(), LinkedProcess.JobStatus.valueOf(jobStatus.getValue()));
        } finally {
            resultHandlers.removeHandler(jobStatus.getPacketID());
            errorHandlers.removeHandler(jobStatus.getPacketID());
        }
    }

    public void receiveError(final JobStatus jobStatus) {
        try {
            errorHandlers.handle(jobStatus.getPacketID(), jobStatus.getError());
        } finally {
            resultHandlers.removeHandler(jobStatus.getPacketID());
            errorHandlers.removeHandler(jobStatus.getPacketID());
        }
    }
}
