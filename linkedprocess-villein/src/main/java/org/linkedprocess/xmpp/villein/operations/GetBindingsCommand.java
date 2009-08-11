package org.linkedprocess.xmpp.villein.operations;

import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
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
public class GetBindingsCommand extends Command {

    private final HandlerSet<VMBindings> resultHandlers;
    private final HandlerSet<XMPPError> errorHandlers;

    public GetBindingsCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<VMBindings>();
        this.errorHandlers = new HandlerSet<XMPPError>();
    }

    public void send(VmProxy vmStruct, Set<String> bindingNames, final Handler<VMBindings> resultHandler, final Handler<XMPPError> errorHandler) {

        String id = Packet.nextID();
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setTo(vmStruct.getFullJid());
        manageBindings.setFrom(xmppVillein.getFullJid());
        manageBindings.setType(IQ.Type.GET);
        manageBindings.setVmPassword(vmStruct.getVmPassword());
        manageBindings.setPacketID(id);
        VMBindings vmBindings = new VMBindings();
        for(String bindingName : bindingNames) {
            vmBindings.put(bindingName, null);
        }
        manageBindings.setBindings(vmBindings);

        this.resultHandlers.addHandler(id, resultHandler);
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
