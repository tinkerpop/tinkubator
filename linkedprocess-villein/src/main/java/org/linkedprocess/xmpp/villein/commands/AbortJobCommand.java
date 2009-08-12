package org.linkedprocess.xmpp.villein.commands;

import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.vm.AbortJob;
import org.linkedprocess.xmpp.LopError;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Aug 7, 2009
 * Time: 4:45:56 PM
 */
public class AbortJobCommand extends Command {

    private final HandlerSet<JobStruct> resultHandlers;
    private final HandlerSet<LopError> errorHandlers;

    public AbortJobCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<JobStruct>();
        this.errorHandlers = new HandlerSet<LopError>();
    }

    public void send(final VmProxy vmStruct, final JobStruct jobStruct, final Handler<JobStruct> resultHandler, final Handler<LopError> errorHandler) {
        String id = Packet.nextID();
        AbortJob abortJob = new AbortJob();
        abortJob.setTo(vmStruct.getFullJid());
        abortJob.setFrom(this.xmppVillein.getFullJid());
        abortJob.setJobId(jobStruct.getJobId());
        abortJob.setVmPassword(vmStruct.getVmPassword());
        abortJob.setType(IQ.Type.GET);
        abortJob.setPacketID(id);
        this.resultHandlers.addHandler(id, resultHandler);
        this.errorHandlers.addHandler(id, errorHandler);
        xmppVillein.getConnection().sendPacket(abortJob);
    }

    public void receiveNormal(final AbortJob abortJob) {
        try {
            JobStruct jobStruct = new JobStruct();
            jobStruct.setJobId(abortJob.getJobId());
            this.resultHandlers.handle(abortJob.getPacketID(), jobStruct);
        } finally {
            this.resultHandlers.removeHandler(abortJob.getPacketID());
            this.errorHandlers.removeHandler(abortJob.getPacketID());
        }

    }

    public void receiveError(final AbortJob abortJob) {
        try {
            this.errorHandlers.handle(abortJob.getPacketID(), abortJob.getLopError());
        } finally {
            this.resultHandlers.removeHandler(abortJob.getPacketID());
            this.errorHandlers.removeHandler(abortJob.getPacketID());
        }
    }
}