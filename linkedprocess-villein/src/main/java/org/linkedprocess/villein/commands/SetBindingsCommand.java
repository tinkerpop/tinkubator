/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.Error;
import org.linkedprocess.farm.ManageBindings;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.VmProxy;

/**
 * The proxy by which a manage_bindings of type set is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SetBindingsCommand extends Command {
    private final HandlerSet<VmBindings> successHandlers;
    private final HandlerSet<Error> errorHandlers;

    public SetBindingsCommand(Villein xmppVillein) {
        super(xmppVillein);
        this.successHandlers = new HandlerSet<VmBindings>();
        this.errorHandlers = new HandlerSet<Error>();
    }

    public void send(final VmProxy vmProxy, VmBindings vmBindings, final Handler<VmBindings> sucessHandler, final Handler<Error> errorHandler) {

        String id = Packet.nextID();
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setTo(vmProxy.getFarmJid());
        manageBindings.setFrom(villein.getFullJid());
        manageBindings.setType(IQ.Type.SET);
        manageBindings.setVmId(vmProxy.getVmId());
        manageBindings.setBindings(vmBindings);
        manageBindings.setPacketID(id);

        Handler<VmBindings> autoResultHandler = new Handler<VmBindings>() {
            public void handle(VmBindings vmBindings) {
                vmProxy.addVmBindings(vmBindings);
                sucessHandler.handle(vmBindings);
            }
        };

        this.successHandlers.addHandler(id, autoResultHandler);
        this.errorHandlers.addHandler(id, errorHandler);

        villein.getConnection().sendPacket(manageBindings);
    }

    public void receiveSuccess(final ManageBindings manageBindings) {
        try {
            successHandlers.handle(manageBindings.getPacketID(), manageBindings.getBindings());
        } finally {
            successHandlers.removeHandler(manageBindings.getPacketID());
            errorHandlers.removeHandler(manageBindings.getPacketID());
        }
    }

    public void receiveError(final ManageBindings manageBindings) {
        try {
            errorHandlers.handle(manageBindings.getPacketID(), manageBindings.getLopError());
        } finally {
            successHandlers.removeHandler(manageBindings.getPacketID());
            errorHandlers.removeHandler(manageBindings.getPacketID());
        }
    }
}
