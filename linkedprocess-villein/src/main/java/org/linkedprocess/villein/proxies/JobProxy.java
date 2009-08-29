/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.Error;
import org.linkedprocess.LinkedProcess;

/**
 * A JobProxy is a data structure representing a job. A job is submitted to and returned by a virtual machine.
 * A submitted job does not have a result or error. However, a submitted job should have an expression.
 * The expression is a code fragement that is to be executed by the virtual machine and must be in the language of the virtual machine species.
 * When a JobProxy is returned by a submit_job call, the result or error may be set along with the complete flag.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class JobProxy implements Comparable {
    protected String jobId;
    protected String result;
    protected String expression;
    protected Error error;
    protected boolean complete = false;

    /**
     * Get the identifier of the job.
     *
     * @return the identifier of the job
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Set the identified of the job. It is very important to ensure that jobs have unique identifiers as the handler mechanisms of the villein assume unique ids.
     *
     * @param jobId the identifier to set for the job.
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Get the result of a successfully executed/evaluated expression.
     *
     * @return the result of the expression
     */
    public String getResult() {
        return result;
    }

    /**
     * Set the result of a successfully executed/evaluated expression.
     *
     * @param result the result of the expression
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Get the error that occured during an execution/evaulation.
     *
     * @return the error of an evaluation if an error has occurred
     */
    public Error getLopError() {
        return error;
    }

    /**
     * Set the error that has occurred during an execution/evaluation.
     *
     * @param error the error of an evaluation if an error has occurred
     */
    public void setLopError(org.linkedprocess.Error error) {
        this.error = error;
    }

    /**
     * Get the expression of the job.
     *
     * @return the expression of the job
     */
    public String getExpression() {
        return this.expression;
    }

    /**
     * Set the expression of the job.
     *
     * @param expression the expression of the job
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * If the job is complete.
     *
     * @return is the job complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Set whether the job is complete.
     *
     * @param complete if the job is complete
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * Determines if the job was aborted. This is a helper method that checks for an error and if there is an error, whether its a job_aborted error.
     *
     * @return whether the job was aborted
     */
    public boolean wasAborted() {
        return (null != error && LinkedProcess.LopErrorType.JOB_ABORTED != error.getErrorType());
    }

    /**
     * Determines if the job was succesful. This is a helper method that checks if the job is complete and if there was not an error.
     *
     * @return whether the job was completed successfully
     */
    public boolean wasSuccessful() {
        return (complete && null == error);
    }

    /**
     * Generate a random job identifier (guaranted to be unique)
     *
     * @return the randomly generated job identifier
     */
    public static String generateRandomId() {
        return Packet.nextID();
    }

    public int compareTo(Object job) {
        if (job instanceof JobProxy) {
            return this.jobId.compareTo(((JobProxy) job).getJobId());
        } else {
            throw new ClassCastException();
        }
    }

    public String toString() {
        if (null == error)
            return "Job(id:'" + jobId + "', complete:'" + complete + "', result:'" + result + "')";
        else
            return "Job(id:'" + jobId + "', complete:'" + complete + "', error:'" + error.getErrorType().toString() + "')";
    }

}
