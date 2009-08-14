/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.vm.TerminateVm;

/**
 * The proxy by which a terminate_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * User: marko
 * Date: Aug 7, 2009
 * Time: 4:53:16 PM
 */
public class TerminateVmCommand extends Command {

    private final HandlerSet<Object> resultHandlers;
    private final HandlerSet<LopError> errorHandlers;

    public TerminateVmCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<Object>();
        this.errorHandlers = new HandlerSet<LopError>();
    }

    public void send(final VmProxy vmStruct, final Handler<Object> resultHandler, final Handler<LopError> errorHandler) {
        String id = Packet.nextID();
        TerminateVm terminateVm = new TerminateVm();
        terminateVm.setTo(vmStruct.getFullJid());
        terminateVm.setFrom(this.xmppVillein.getFullJid());
        terminateVm.setVmPassword(vmStruct.getVmPassword());
        terminateVm.setType(IQ.Type.GET);
        terminateVm.setPacketID(id);

        this.errorHandlers.addHandler(id, errorHandler);
        this.resultHandlers.addHandler(id, resultHandler);

        xmppVillein.getConnection().sendPacket(terminateVm);
    }

    public void receiveNormal(final TerminateVm terminateVm) {
        try {
            this.resultHandlers.handle(terminateVm.getPacketID(), null);
        } finally {
            this.resultHandlers.removeHandler(terminateVm.getPacketID());
            this.errorHandlers.removeHandler(terminateVm.getPacketID()); 
        }
    }

    public void receiveError(final TerminateVm terminateVm) {
        try {
            this.errorHandlers.handle(terminateVm.getPacketID(), terminateVm.getLopError());
        } finally {
            this.errorHandlers.removeHandler(terminateVm.getPacketID());
            this.resultHandlers.removeHandler(terminateVm.getPacketID());
        }
    }
}