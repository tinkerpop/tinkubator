package org.linkedprocess.xmpp.villein.operations;

import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.errors.InvalidValueException;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.vm.ManageBindings;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Aug 7, 2009
 * Time: 5:38:55 PM
 */
public class SetBindingsCommand extends Command {
    private final HandlerSet<VMBindings> resultHandlers;
    private final HandlerSet<XMPPError> errorHandlers;

    public SetBindingsCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<VMBindings>();
        this.errorHandlers = new HandlerSet<XMPPError>();
    }

    public void send(final VmProxy vmStruct, VMBindings vmBindings, final Handler<XMPPError> errorHandler) {

        String id = Packet.nextID();
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setTo(vmStruct.getFullJid());
        manageBindings.setFrom(xmppVillein.getFullJid());
        manageBindings.setType(IQ.Type.SET);
        manageBindings.setVmPassword(vmStruct.getVmPassword());
        manageBindings.setBindings(vmBindings);
        manageBindings.setPacketID(id);

        Handler<VMBindings> autoResultHandler = new Handler<VMBindings>() {
            public void handle(VMBindings vmBindings) {
                try {
                    vmStruct.addVmBindings(vmBindings);
                } catch(InvalidValueException e) {
                    XmppVillein.LOGGER.severe(e.getMessage());
                }  
            }
        };

        this.resultHandlers.addHandler(id, autoResultHandler);
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
