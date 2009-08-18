/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp;

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
    protected String vmPassword;

    public void setVmPassword(String vmPassword) {
        this.vmPassword = vmPassword;
    }

    public String getVmPassword() {
        return this.vmPassword;
    }

    public void setLopError(LopError lopError) {
        this.setError(lopError);
    }

    public LopError getLopError() {
        XMPPError xmppError = this.getError();
        LinkedProcess.LopErrorType lopErrorType = null;
        LinkedProcess.ClientType clientType = null;
        for (PacketExtension extension : xmppError.getExtensions()) {
            lopErrorType = LinkedProcess.LopErrorType.getErrorType(extension.getElementName());
            if (extension.getNamespace().equals(LinkedProcess.LOP_VM_NAMESPACE))
                clientType = LinkedProcess.ClientType.VM;
            else if (extension.getNamespace().equals(LinkedProcess.LOP_FARM_NAMESPACE))
                clientType = LinkedProcess.ClientType.FARM;
            break;
        }
        return new LopError(LopError.stringConditionMap.get(xmppError.getCondition()), lopErrorType, xmppError.getMessage(), clientType, this.getPacketID());
    }

}
