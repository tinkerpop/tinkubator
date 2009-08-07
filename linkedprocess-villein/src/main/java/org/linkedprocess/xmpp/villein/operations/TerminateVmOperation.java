package org.linkedprocess.xmpp.villein.operations;

import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.structs.VmStruct;
import org.linkedprocess.xmpp.vm.TerminateVm;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Aug 7, 2009
 * Time: 4:53:16 PM
 */
public class TerminateVmOperation extends Operation {

    public TerminateVmOperation(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void send(final VmStruct vmStruct, final Handler<XMPPError> errorHandler) {
        String id = Packet.nextID();
        TerminateVm terminateVm = new TerminateVm();
        terminateVm.setTo(vmStruct.getFullJid());
        terminateVm.setFrom(this.xmppVillein.getFullJid());
        terminateVm.setVmPassword(vmStruct.getVmPassword());
        terminateVm.setType(IQ.Type.GET);
        terminateVm.setPacketID(id);
        this.errorHandlers.addHandler(id, errorHandler);
        xmppVillein.getConnection().sendPacket(terminateVm);
    }

    public void receiveNormal(final TerminateVm terminateVm) {
        this.errorHandlers.removeHandler(terminateVm.getPacketID());
    }

    public void receiveError(final TerminateVm terminateVm) {
        try {
            this.errorHandlers.handle(terminateVm.getPacketID(), terminateVm.getError());
        } finally {
            this.errorHandlers.removeHandler(terminateVm.getPacketID());
        }
    }
}