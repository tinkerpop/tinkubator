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
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.PingJob;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.JobProxy;
import org.linkedprocess.villein.proxies.VmProxy;

/**
 * The proxy by which a ping_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PingJobCommand extends Command {
    private final HandlerSet<LinkedProcess.JobStatus> successHandlers;
    private final HandlerSet<LopError> errorHandlers;

    public PingJobCommand(Villein xmppVillein) {
        super(xmppVillein);
        this.successHandlers = new HandlerSet<LinkedProcess.JobStatus>();
        this.errorHandlers = new HandlerSet<LopError>();
    }

    public void send(VmProxy vmProxy, JobProxy jobProxy, final Handler<LinkedProcess.JobStatus> successHandler, final Handler<LopError> errorHandler) {

        String id = Packet.nextID();
        PingJob pingJob = new PingJob();
        pingJob.setTo(vmProxy.getFarmProxy().getJid().toString());
        pingJob.setFrom(this.villein.getJid().toString());
        pingJob.setJobId(jobProxy.getJobId());
        pingJob.setVmId(vmProxy.getVmId());
        pingJob.setType(IQ.Type.GET);
        pingJob.setPacketID(id);

        this.successHandlers.addHandler(id, successHandler);
        this.errorHandlers.addHandler(id, errorHandler);

        villein.getConnection().sendPacket(pingJob);
    }

    public void receiveSuccess(final PingJob pingJob) {
        try {
            successHandlers.handle(pingJob.getPacketID(), LinkedProcess.JobStatus.valueOf(pingJob.getStatus()));
        } finally {
            successHandlers.removeHandler(pingJob.getPacketID());
            errorHandlers.removeHandler(pingJob.getPacketID());
        }
    }

    public void receiveError(final PingJob pingJob) {
        try {
            errorHandlers.handle(pingJob.getPacketID(), pingJob.getLopError());
        } finally {
            successHandlers.removeHandler(pingJob.getPacketID());
            errorHandlers.removeHandler(pingJob.getPacketID());
        }
    }
}
