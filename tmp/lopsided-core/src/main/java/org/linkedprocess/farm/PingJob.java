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
 * A ping_job packet is modeled by this class.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PingJob extends FarmIq {

    protected LinkedProcess.JobStatus value;
    protected String jobId;

    /**
     * Set the job_id attribute of this packet.
     *
     * @param jobId the job_id attribute of this packet
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Get the job_id attribute of this packet.
     *
     * @return the job_id attribute of this packet
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * Set the status attribute of this packet.
     *
     * @param status the status attribute of this packet
     */
    public void setStatus(LinkedProcess.JobStatus status) {
        this.value = status;
    }

    /**
     * Get the status attribute of this packet.
     *
     * @return the status attribute of this packet
     */
    public String getStatus() {
        return this.value.toString();
    }

    /**
     * Get the ping_job component of this IQ packet.
     *
     * @return the ping_job component of this IQ packet
     */
    public String getChildElementXML() {

        Element pingJobElement = new Element(LinkedProcess.PING_JOB_TAG, LinkedProcess.LOP_FARM_NAMESPACE);
        if (this.vmId != null) {
            pingJobElement.setAttribute(LinkedProcess.VM_ID_ATTRIBUTE, this.vmId);
        }
        if (this.jobId != null) {
            pingJobElement.setAttribute(LinkedProcess.JOB_ID_ATTRIBUTE, this.jobId);
        }
        if (this.value != null) {
            pingJobElement.setAttribute(LinkedProcess.STATUS_ATTRIBUTE, this.value.toString());
        }
        return LinkedProcess.xmlOut.outputString(pingJobElement);
    }
}
