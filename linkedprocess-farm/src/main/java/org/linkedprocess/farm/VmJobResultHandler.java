/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VmScheduler;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;
import org.linkedprocess.vm.LopVm;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 4:23:32 PM
 */
public class VmJobResultHandler implements VmScheduler.VmResultHandler {

    LopFarm lopFarm;

    public VmJobResultHandler(LopFarm lopFarm) {
        this.lopFarm = lopFarm;
    }

    public void handleResult(JobResult result) {
        try {
            LopVm vm = lopFarm.getVirtualMachine(result.getJob().getVmJid());
            IQ returnSubmitJob = result.generateReturnEvalulate();
            vm.getConnection().sendPacket(returnSubmitJob);

            LopVm.LOGGER.info("Sent " + VmJobResultHandler.class.getName());
            LopVm.LOGGER.info(returnSubmitJob.toXML());

        } catch (VmWorkerNotFoundException e) {
            LopVm.LOGGER.severe("Could not find virtual machine. Thus, can not send error message");
        }

    }
}
