/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.jdom.Element;
import org.linkedprocess.LinkedProcess;

/**
 * An abort_job packet is modeled by this class.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class AbortJob extends FarmIq {

    protected String jobId;

    /**
     * Set the job_id attribute of this packet.
     *
     * @param jobId the job_id attribute of this packet
     */
    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    /**
     * Get the identifier of the job to abort
     *
     * @return the job_id attribute of this packet
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * Get the abort_job component of this IQ packet.
     *
     * @return the abort_job component of this IQ packet
     */
    public String getChildElementXML() {

        Element abandonJobElement = new Element(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_FARM_NAMESPACE);
        if (this.vmId != null) {
            abandonJobElement.setAttribute(LinkedProcess.VM_ID_ATTRIBUTE, this.vmId);
        }
        if (this.jobId != null) {
            abandonJobElement.setAttribute(LinkedProcess.JOB_ID_ATTRIBUTE, this.jobId);
        }

        return LinkedProcess.xmlOut.outputString(abandonJobElement);
    }
}
