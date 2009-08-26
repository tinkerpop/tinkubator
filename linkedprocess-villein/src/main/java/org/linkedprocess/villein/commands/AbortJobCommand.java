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
import org.linkedprocess.farm.AbortJob;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.JobProxy;
import org.linkedprocess.villein.proxies.VmProxy;

/**
 * The proxy by which an abort_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class AbortJobCommand extends Command {

    private final HandlerSet<String> successHandlers;
    private final HandlerSet<Error> errorHandlers;

    public AbortJobCommand(Villein xmppVillein) {
        super(xmppVillein);
        this.successHandlers = new HandlerSet<String>();
        this.errorHandlers = new HandlerSet<org.linkedprocess.Error>();
    }

    public void send(final VmProxy vmProxy, final JobProxy jobProxy, final Handler<String> successHandler, final Handler<Error> errorHandler) {
        String id = Packet.nextID();
        AbortJob abortJob = new AbortJob();
        abortJob.setTo(vmProxy.getFarmJid());
        abortJob.setFrom(this.villein.getFullJid());
        abortJob.setJobId(jobProxy.getJobId());
        abortJob.setVmId(vmProxy.getVmId());
        abortJob.setType(IQ.Type.GET);
        abortJob.setPacketID(id);
        this.successHandlers.addHandler(id, successHandler);
        this.errorHandlers.addHandler(id, errorHandler);
        villein.getConnection().sendPacket(abortJob);
    }

    public void receiveSuccess(final AbortJob abortJob) {
        try {
            JobProxy jobProxy = new JobProxy();
            jobProxy.setJobId(abortJob.getJobId());
            this.successHandlers.handle(abortJob.getPacketID(), abortJob.getJobId());
        } finally {
            this.successHandlers.removeHandler(abortJob.getPacketID());
            this.errorHandlers.removeHandler(abortJob.getPacketID());
        }

    }

    public void receiveError(final AbortJob abortJob) {
        try {
            this.errorHandlers.handle(abortJob.getPacketID(), abortJob.getLopError());
        } finally {
            this.successHandlers.removeHandler(abortJob.getPacketID());
            this.errorHandlers.removeHandler(abortJob.getPacketID());
        }
    }
}