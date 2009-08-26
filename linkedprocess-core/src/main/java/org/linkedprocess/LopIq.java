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
import org.linkedprocess.LinkedProcess;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class LopIq extends IQ {

    protected String errorMessage;
    protected String vmId;

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public String getVmId() {
        return this.vmId;
    }

    public void setLopError(Error error) {
        super.setError(error);
    }

    public Error getLopError() {
        XMPPError xmppError = super.getError();
        LinkedProcess.LopErrorType errorType = null;
        for (PacketExtension extension : xmppError.getExtensions()) {
            errorType = LinkedProcess.LopErrorType.getErrorType(extension.getElementName());
            if(errorType != null)
                break;
        }
        return new Error(Error.stringConditionMap.get(xmppError.getCondition()), errorType, xmppError.getMessage(), this.getPacketID());
    }

}
