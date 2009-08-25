/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.farm.os.VmBindings;
import org.linkedprocess.Error;
import org.linkedprocess.farm.ManageBindings;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.VmProxy;

import java.util.Set;

/**
 * The proxy by which a manage_bindings of type get is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class GetBindingsCommand extends Command {

    private final HandlerSet<VmBindings> successHandlers;
    private final HandlerSet<Error> errorHandlers;

    public GetBindingsCommand(Villein xmppVillein) {
        super(xmppVillein);
        this.successHandlers = new HandlerSet<VmBindings>();
        this.errorHandlers = new HandlerSet<org.linkedprocess.Error>();
    }

    public void send(VmProxy vmProxy, Set<String> bindingNames, final Handler<VmBindings> successHandler, final Handler<Error> errorHandler) {

        String id = Packet.nextID();
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setTo(vmProxy.getFarmJid());
        manageBindings.setFrom(villein.getFullJid());
        manageBindings.setType(IQ.Type.GET);
        manageBindings.setVmId(vmProxy.getVmId());
        manageBindings.setPacketID(id);
        VmBindings vmBindings = new VmBindings();
        for (String bindingName : bindingNames) {
            vmBindings.put(bindingName, null);
        }
        manageBindings.setBindings(vmBindings);

        this.successHandlers.addHandler(id, successHandler);
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
