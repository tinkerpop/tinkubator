/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.os;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 6:34:20 PM
 */
public class Job {
    private final String jobId;
    private final String vmId;
    private final String villeinJid;
    private final String expression;
    private long timeSpent;

    public Job(final String vmId,
               final String villeinJid,
               final String jobId,
               final String expression) {
        this.vmId = vmId;
        this.villeinJid = villeinJid;
        this.jobId = jobId;
        this.expression = expression;

        timeSpent = 0;
    }

    public String getVilleinJid() {
        return villeinJid;
    }

    public String getJobId() {
        return jobId;
    }

    public String getVmId() {
        return vmId;
    }

    public String getExpression() {
        return expression;
    }

    public void increaseTimeSpent(final long time) {
        timeSpent += time;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public String toString() {
        return "Job("
                + "jobId:'" + jobId + "'"
                + ", vmJid:'" + vmId + "'"
                + ", villeinJid:'" + villeinJid + "'"
                + ", expression:'" + expression + "')";

    }
}
