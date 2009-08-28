/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.JobProxy;
import org.linkedprocess.villein.proxies.VmProxy;
import org.linkedprocess.farm.SubmitJob;

/**
 * The proxy by which an submit_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SubmitJobCommand extends Command {

    private final HandlerSet<JobProxy> successHandlers;
    private final HandlerSet<JobProxy> errorHandlers;

    public SubmitJobCommand(Villein xmppVillein) {
        super(xmppVillein);
        this.successHandlers = new HandlerSet<JobProxy>();
        this.errorHandlers = new HandlerSet<JobProxy>();
    }

    public void send(final VmProxy vmProxy, final JobProxy jobProxy, final Handler<JobProxy> successHandler, final Handler<JobProxy> errorHandler) {

        if (null == jobProxy.getJobId())
            jobProxy.setJobId(JobProxy.generateRandomId());

        SubmitJob submitJob = new SubmitJob();
        submitJob.setTo(vmProxy.getFarmProxy().getJid().toString());
        submitJob.setFrom(villein.getJid().toString());
        submitJob.setExpression(jobProxy.getExpression());
        submitJob.setVmId(vmProxy.getVmId());
        submitJob.setType(IQ.Type.GET);
        submitJob.setPacketID(jobProxy.getJobId());

        this.successHandlers.addHandler(jobProxy.getJobId(), successHandler);
        this.errorHandlers.addHandler(jobProxy.getJobId(), errorHandler);

        villein.getConnection().sendPacket(submitJob);
    }

    public void receiveSuccess(final SubmitJob submitJob) {
        try {
            JobProxy jobProxy = new JobProxy();
            jobProxy.setJobId(submitJob.getPacketID());
            jobProxy.setResult(submitJob.getExpression());
            jobProxy.setComplete(true);
            successHandlers.handle(submitJob.getPacketID(), jobProxy);
        } finally {
            successHandlers.removeHandler(submitJob.getPacketID());
            errorHandlers.removeHandler(submitJob.getPacketID());
        }
    }

    public void receiveError(final SubmitJob submitJob) {
        try {
            JobProxy jobProxy = new JobProxy();
            jobProxy.setJobId(submitJob.getPacketID());
            jobProxy.setLopError(submitJob.getLopError());
            jobProxy.setComplete(true);
            errorHandlers.handle(submitJob.getPacketID(), jobProxy);
        } finally {
            successHandlers.removeHandler(submitJob.getPacketID());
            errorHandlers.removeHandler(submitJob.getPacketID());
        }
    }
}
