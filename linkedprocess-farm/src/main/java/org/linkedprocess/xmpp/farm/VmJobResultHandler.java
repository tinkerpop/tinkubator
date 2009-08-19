/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VmScheduler;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;
import org.linkedprocess.xmpp.vm.XmppVm;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 4:23:32 PM
 */
public class VmJobResultHandler implements VmScheduler.VmResultHandler {

    XmppFarm xmppFarm;

    public VmJobResultHandler(XmppFarm xmppFarm) {
        this.xmppFarm = xmppFarm;
    }

    public void handleResult(JobResult result) {
        try {
            XmppVm vm = xmppFarm.getVirtualMachine(result.getJob().getVmJid());
            IQ returnSubmitJob = result.generateReturnEvalulate();
            vm.getConnection().sendPacket(returnSubmitJob);

            XmppVm.LOGGER.info("Sent " + VmJobResultHandler.class.getName());
            XmppVm.LOGGER.info(returnSubmitJob.toXML());

        } catch (VmWorkerNotFoundException e) {
            XmppVm.LOGGER.severe("Could not find virtual machine. Thus, can not send error message");
        }

    }
}
