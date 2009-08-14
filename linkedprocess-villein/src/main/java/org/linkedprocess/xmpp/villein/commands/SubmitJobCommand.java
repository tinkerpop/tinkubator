/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.vm.SubmitJob;

/**
 * The proxy by which an submit_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * User: marko
 * Date: Aug 7, 2009
 * Time: 4:30:42 PM
 */
public class SubmitJobCommand extends Command {

    private final HandlerSet<JobStruct> resultHandlers;
    private final HandlerSet<JobStruct> errorHandlers;

    public SubmitJobCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<JobStruct>();
        this.errorHandlers = new HandlerSet<JobStruct>();
    }

    public void send(final VmProxy vmStruct, final JobStruct jobStruct, final Handler<JobStruct> resultHandler, final Handler<JobStruct> errorHandler) {

        if (null == jobStruct.getJobId())
            jobStruct.setJobId(Packet.nextID());

        SubmitJob submitJob = new SubmitJob();
        submitJob.setTo(vmStruct.getFullJid());
        submitJob.setFrom(xmppVillein.getFullJid());
        submitJob.setExpression(jobStruct.getExpression());
        submitJob.setVmPassword(vmStruct.getVmPassword());
        submitJob.setType(IQ.Type.GET);
        submitJob.setPacketID(jobStruct.getJobId());

        this.resultHandlers.addHandler(jobStruct.getJobId(), resultHandler);
        this.errorHandlers.addHandler(jobStruct.getJobId(), errorHandler);

        xmppVillein.getConnection().sendPacket(submitJob);
    }

    public void receiveNormal(final SubmitJob submitJob) {
        try {
            JobStruct jobStruct = new JobStruct();
            jobStruct.setJobId(submitJob.getPacketID());
            jobStruct.setResult(submitJob.getExpression());
            jobStruct.setComplete(true);
            resultHandlers.handle(submitJob.getPacketID(), jobStruct);
        } finally {
            resultHandlers.removeHandler(submitJob.getPacketID());
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
            resultHandlers.removeHandler(submitJob.getPacketID());
            errorHandlers.removeHandler(submitJob.getPacketID());
        }
    }
}
