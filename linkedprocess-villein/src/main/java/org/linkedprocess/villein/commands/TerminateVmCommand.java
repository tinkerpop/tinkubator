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
import org.linkedprocess.Jid;
import org.linkedprocess.farm.TerminateVm;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.VmProxy;

/**
 * The proxy by which a terminate_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class TerminateVmCommand extends Command {

    private final HandlerSet<Object> successHandlers;
    private final HandlerSet<org.linkedprocess.Error> errorHandlers;

    public TerminateVmCommand(Villein xmppVillein) {
        super(xmppVillein);
        this.successHandlers = new HandlerSet<Object>();
        this.errorHandlers = new HandlerSet<Error>();
    }

    public void send(final VmProxy vmProxy, final Handler<Object> successHandler, final Handler<Error> errorHandler) {
        String id = Packet.nextID();
        TerminateVm terminateVm = new TerminateVm();
        terminateVm.setTo(vmProxy.getFarmProxy().getJid().toString());
        terminateVm.setFrom(this.villein.getJid().toString());
        terminateVm.setVmId(vmProxy.getVmId());
        terminateVm.setType(IQ.Type.GET);
        terminateVm.setPacketID(id);

        this.errorHandlers.addHandler(id, errorHandler);
        this.successHandlers.addHandler(id, successHandler);

        villein.getConnection().sendPacket(terminateVm);
    }

    public void receiveSuccess(final TerminateVm terminateVm) {
        FarmProxy farmProxy = this.villein.getCloudProxy().getFarmProxy(new Jid(terminateVm.getFrom()));
        if (null != farmProxy) {
            farmProxy.removeVmProxy(terminateVm.getVmId());
        }

        try {
            this.successHandlers.handle(terminateVm.getPacketID(), null);
        } finally {
            this.successHandlers.removeHandler(terminateVm.getPacketID());
            this.errorHandlers.removeHandler(terminateVm.getPacketID());
        }
    }

    public void receiveError(final TerminateVm terminateVm) {
        try {
            this.errorHandlers.handle(terminateVm.getPacketID(), terminateVm.getLopError());
        } finally {
            this.errorHandlers.removeHandler(terminateVm.getPacketID());
            this.successHandlers.removeHandler(terminateVm.getPacketID());
        }
    }
}