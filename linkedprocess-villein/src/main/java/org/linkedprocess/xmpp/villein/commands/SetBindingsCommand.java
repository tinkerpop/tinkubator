/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.os.errors.InvalidValueException;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.vm.ManageBindings;

/**
 * The proxy by which a manage_bindings of type set is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SetBindingsCommand extends Command {
    private final HandlerSet<VmBindings> resultHandlers;
    private final HandlerSet<LopError> errorHandlers;

    public SetBindingsCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<VmBindings>();
        this.errorHandlers = new HandlerSet<LopError>();
    }

    public void send(final VmProxy vmStruct, VmBindings vmBindings, final Handler<VmBindings> resultHandler, final Handler<LopError> errorHandler) {

        String id = Packet.nextID();
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setTo(vmStruct.getFullJid());
        manageBindings.setFrom(xmppVillein.getFullJid());
        manageBindings.setType(IQ.Type.SET);
        manageBindings.setVmPassword(vmStruct.getVmPassword());
        manageBindings.setBindings(vmBindings);
        manageBindings.setPacketID(id);

        Handler<VmBindings> autoResultHandler = new Handler<VmBindings>() {
            public void handle(VmBindings vmBindings) {
                try {
                    vmStruct.addVmBindings(vmBindings);
                } catch (InvalidValueException e) {
                    XmppVillein.LOGGER.severe(e.getMessage());
                }
                resultHandler.handle(vmBindings);
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
            errorHandlers.handle(manageBindings.getPacketID(), manageBindings.getLopError());
        } finally {
            resultHandlers.removeHandler(manageBindings.getPacketID());
            errorHandlers.removeHandler(manageBindings.getPacketID());
        }
    }
}
