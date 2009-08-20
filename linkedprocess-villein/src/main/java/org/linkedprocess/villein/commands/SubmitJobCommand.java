/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.XmppVillein;
import org.linkedprocess.villein.proxies.JobStruct;
import org.linkedprocess.villein.proxies.VmProxy;
import org.linkedprocess.vm.SubmitJob;

/**
 * The proxy by which an submit_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SubmitJobCommand extends Command {

    private final HandlerSet<JobStruct> successHandlers;
    private final HandlerSet<JobStruct> errorHandlers;

    public SubmitJobCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.successHandlers = new HandlerSet<JobStruct>();
        this.errorHandlers = new HandlerSet<JobStruct>();
    }

    public void send(final VmProxy vmStruct, final JobStruct jobStruct, final Handler<JobStruct> successHandler, final Handler<JobStruct> errorHandler) {

        if (null == jobStruct.getJobId())
            jobStruct.setJobId(Packet.nextID());

        SubmitJob submitJob = new SubmitJob();
        submitJob.setTo(vmStruct.getFullJid());
        submitJob.setFrom(xmppVillein.getFullJid());
        submitJob.setExpression(jobStruct.getExpression());
        submitJob.setVmPassword(vmStruct.getVmPassword());
        submitJob.setType(IQ.Type.GET);
        submitJob.setPacketID(jobStruct.getJobId());

        this.successHandlers.addHandler(jobStruct.getJobId(), successHandler);
        this.errorHandlers.addHandler(jobStruct.getJobId(), errorHandler);

        xmppVillein.getConnection().sendPacket(submitJob);
    }

    public void receiveSuccess(final SubmitJob submitJob) {
        try {
            JobStruct jobStruct = new JobStruct();
            jobStruct.setJobId(submitJob.getPacketID());
            jobStruct.setResult(submitJob.getExpression());
            jobStruct.setComplete(true);
            successHandlers.handle(submitJob.getPacketID(), jobStruct);
        } finally {
            successHandlers.removeHandler(submitJob.getPacketID());
            errorHandlers.removeHandler(submitJob.getPacketID());
        }
    }

    public void receiveError(final SubmitJob submitJob) {
        try {
            JobStruct jobStruct = new JobStruct();
            jobStruct.setJobId(submitJob.getPacketID());
            jobStruct.setLopError(submitJob.getLopError());
            jobStruct.setComplete(true);
            errorHandlers.handle(submitJob.getPacketID(), jobStruct);
        } finally {
            successHandlers.removeHandler(submitJob.getPacketID());
            errorHandlers.removeHandler(submitJob.getPacketID());
        }
    }
}
