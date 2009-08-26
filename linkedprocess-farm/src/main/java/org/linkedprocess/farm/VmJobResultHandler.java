/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.linkedprocess.farm.os.JobResult;
import org.linkedprocess.farm.os.VmScheduler;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class VmJobResultHandler implements VmScheduler.VmResultHandler {

    protected Farm farm;

    public VmJobResultHandler(Farm farm) {
        this.farm = farm;
    }

    public void handleResult(JobResult result) {

        SubmitJob returnSubmitJob = result.generateReturnSubmitJob();
        returnSubmitJob.setFrom(farm.getFullJid());
        farm.getConnection().sendPacket(returnSubmitJob);

        Farm.LOGGER.info("Sent " + VmJobResultHandler.class.getName());
        Farm.LOGGER.info(returnSubmitJob.toXML());
    }
}
