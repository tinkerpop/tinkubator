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
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PingJob extends FarmIq {

    protected LinkedProcess.JobStatus value;
    protected String jobId;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return this.jobId;
    }

    public void setValue(LinkedProcess.JobStatus status) {
        this.value = status;
    }

    public String getValue() {
        return this.value.toString();
    }

    public String getChildElementXML() {

        Element pingJobElement = new Element(LinkedProcess.PING_JOB_TAG, LinkedProcess.LOP_FARM_NAMESPACE);
        if (this.vmId != null) {
            pingJobElement.setAttribute(LinkedProcess.VM_ID_ATTRIBUTE, this.vmId);
        }
        if (this.jobId != null) {
            pingJobElement.setAttribute(LinkedProcess.JOB_ID_ATTRIBUTE, this.jobId);
        }
        if (this.value != null) {
            pingJobElement.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, this.value.toString());
        }
        return LinkedProcess.xmlOut.outputString(pingJobElement);
    }
}
