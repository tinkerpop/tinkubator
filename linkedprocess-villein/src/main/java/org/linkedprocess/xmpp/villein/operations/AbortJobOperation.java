package org.linkedprocess.xmpp.villein.operations;

import org.linkedprocess.xmpp.villein.structs.VmStruct;
import org.linkedprocess.xmpp.villein.structs.JobStruct;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.vm.AbortJob;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Aug 7, 2009
 * Time: 4:45:56 PM
 */
public class AbortJobOperation extends Operation {

    public AbortJobOperation(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void send(final VmStruct vmStruct, final JobStruct jobStruct, final Handler<XMPPError> errorHandler) {
        String id = Packet.nextID();
        AbortJob abortJob = new AbortJob();
        abortJob.setTo(vmStruct.getFullJid());
        abortJob.setFrom(this.xmppVillein.getFullJid());
        abortJob.setJobId(jobStruct.getJobId());
        abortJob.setVmPassword(vmStruct.getVmPassword());
        abortJob.setType(IQ.Type.GET);
        abortJob.setPacketID(id);
        this.errorHandlers.addHandler(id, errorHandler);
        xmppVillein.getConnection().sendPacket(abortJob);
    }

    public void receiveNormal(final AbortJob abortJob) {
        this.errorHandlers.removeHandler(abortJob.getPacketID());
    }

    public void receiveError(final AbortJob abortJob) {
        try {
            this.errorHandlers.handle(abortJob.getPacketID(), abortJob.getError());
        } finally {
            this.errorHandlers.removeHandler(abortJob.getPacketID());
        }
    }
}