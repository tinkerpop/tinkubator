/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.XMPPError;

/**
 * The base class of all Linked Process IQ packets.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class LopIq extends IQ {

    protected String errorMessage;
    protected String vmId;

    /**
     * All Linked Process IQ packets maintain a vm_id attribute.
     * This method sets the vm_id attribute.
     *
     * @param vmId the vm_id attribute
     */
    public void setVmId(final String vmId) {
        this.vmId = vmId;
    }

    /**
     * Get the vm_id attribute of the Linked Process IQ packet.
     *
     * @return the vm_id attribute
     */
    public String getVmId() {
        return this.vmId;
    }

    /**
     * Linked Process IQ packets can contain an error.
     * These errors are specific to Linked Process but also extend the requirements of IQ-based errors in XMPP.
     * Use this method to add an error to the Linked Process IQ packet.
     *
     * @param error the error of the packet
     */
    public void setLopError(final Error error) {
        super.setError(error);
    }

    /**
     * The get the associated error of the Linked Process IQ packet.
     *
     * @return the Linked Process IQ packet errror (null if no error exists)
     */
    public Error getLopError() {
        XMPPError xmppError = super.getError();
        LinkedProcess.LopErrorType errorType = null;
        for (PacketExtension extension : xmppError.getExtensions()) {
            errorType = LinkedProcess.LopErrorType.getErrorType(extension.getElementName());
            if (errorType != null)
                break;
        }
        return new Error(Error.stringConditionMap.get(xmppError.getCondition()), errorType, xmppError.getMessage(), this.getPacketID());
    }

}
