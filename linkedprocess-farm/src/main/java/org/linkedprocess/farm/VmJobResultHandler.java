/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.farm.os.JobResult;
import org.linkedprocess.farm.os.VmScheduler;
import org.linkedprocess.farm.os.errors.VmNotFoundException;
import org.linkedprocess.farm.os.Vm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class VmJobResultHandler implements VmScheduler.VmResultHandler {

    Farm farm;

    public VmJobResultHandler(Farm farm) {
        this.farm = farm;
    }

    public void handleResult(JobResult result) {
        try {
            Vm vm = farm.getVm(result.getJob().getVmId());
            IQ returnSubmitJob = result.generateReturnEvalulate();
            returnSubmitJob.setFrom(farm.getFullJid());
            farm.getConnection().sendPacket(returnSubmitJob);

            Farm.LOGGER.info("Sent " + VmJobResultHandler.class.getName());
            Farm.LOGGER.info(returnSubmitJob.toXML());

        } catch (VmNotFoundException e) {
            Farm.LOGGER.severe("Could not find virtual machine. Thus, can not send error message");
        }

    }
}
