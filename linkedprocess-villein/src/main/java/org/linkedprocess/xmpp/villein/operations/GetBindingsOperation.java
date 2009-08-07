package org.linkedprocess.xmpp.villein.operations;

import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.structs.VmStruct;
import org.linkedprocess.xmpp.vm.ManageBindings;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.IQ;

import java.util.Set;

/**
 * User: marko
 * Date: Aug 7, 2009
 * Time: 5:33:09 PM
 */
public class GetBindingsOperation extends Operation {

   private final HandlerSet<VMBindings> resultHandlers;

    public GetBindingsOperation(XmppVillein xmppVillein) {
        super(xmppVillein);
        resultHandlers = new HandlerSet<VMBindings>();
    }

    public void send(VmStruct vmStruct, Set<String> bindingNames, final Handler<VMBindings> statusHandler, final Handler<XMPPError> errorHandler) {

        String id = Packet.nextID();
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setTo(vmStruct.getFullJid());
        manageBindings.setFrom(xmppVillein.getFullJid());
        manageBindings.setType(IQ.Type.GET);
        manageBindings.setVmPassword(vmStruct.getVmPassword());
        VMBindings vmBindings = new VMBindings();
        for(String bindingName : bindingNames) {
            vmBindings.put(bindingName, null);
        }
        manageBindings.setBindings(vmBindings);
        xmppVillein.getConnection().sendPacket(manageBindings);

        this.resultHandlers.addHandler(id, statusHandler);
        this.errorHandlers.addHandler(id, errorHandler);

        xmppVillein.getConnection().sendPacket(manageBindings);
    }

    public void receiveNormal(final ManageBindings manageBindings) {
        try {
            resultHandlers.handle(manageBindings.getPacketID(), manageBindings.getBindings());
        } finally {
            resultHandlers.removeHandler(manageBindings.getPacketID());
            errorHandlers.removeHandler(manageBindings.getPacketID());
        }
    }

    public void receiveError(final ManageBindings manageBindings) {
        try {
            errorHandlers.handle(manageBindings.getPacketID(), manageBindings.getError());
        } finally {
            resultHandlers.removeHandler(manageBindings.getPacketID());
            errorHandlers.removeHandler(manageBindings.getPacketID());
        }
    }
}
