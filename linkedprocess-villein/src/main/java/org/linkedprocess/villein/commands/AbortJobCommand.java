/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.LopError;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.LopVillein;
import org.linkedprocess.villein.proxies.JobStruct;
import org.linkedprocess.villein.proxies.VmProxy;
import org.linkedprocess.vm.AbortJob;

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
    private final HandlerSet<LopError> errorHandlers;

    public AbortJobCommand(LopVillein xmppVillein) {
        super(xmppVillein);
        this.successHandlers = new HandlerSet<String>();
        this.errorHandlers = new HandlerSet<LopError>();
    }

    public void send(final VmProxy vmStruct, final JobStruct jobStruct, final Handler<String> successHandler, final Handler<LopError> errorHandler) {
        String id = Packet.nextID();
        AbortJob abortJob = new AbortJob();
        abortJob.setTo(vmStruct.getFullJid());
        abortJob.setFrom(this.xmppVillein.getFullJid());
        abortJob.setJobId(jobStruct.getJobId());
        abortJob.setVmPassword(vmStruct.getVmPassword());
        abortJob.setType(IQ.Type.GET);
        abortJob.setPacketID(id);
        this.successHandlers.addHandler(id, successHandler);
        this.errorHandlers.addHandler(id, errorHandler);
        xmppVillein.getConnection().sendPacket(abortJob);
    }

    public void receiveSuccess(final AbortJob abortJob) {
        try {
            JobStruct jobStruct = new JobStruct();
            jobStruct.setJobId(abortJob.getJobId());
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