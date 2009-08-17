package org.linkedprocess.xmpp;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.PacketExtension;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 10:14:25 AM
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
        for(PacketExtension extension : xmppError.getExtensions()) {
            lopErrorType = LinkedProcess.LopErrorType.getErrorType(extension.getElementName());
            if(extension.getNamespace().equals(LinkedProcess.LOP_VM_NAMESPACE))
                clientType = LinkedProcess.ClientType.VM;
            else if(extension.getNamespace().equals(LinkedProcess.LOP_FARM_NAMESPACE))
                clientType = LinkedProcess.ClientType.FARM;
            break;
        }
        return new LopError(LopError.stringConditionMap.get(xmppError.getCondition()), lopErrorType, xmppError.getMessage(), clientType, this.getPacketID());
    }

}
