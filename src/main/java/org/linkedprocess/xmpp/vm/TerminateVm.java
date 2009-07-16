package org.linkedprocess.xmpp.vm;

import org.jdom.Element;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:25:52 PM
 */
public class TerminateVm extends VirtualMachineIq {

    public String getChildElementXML() {

        Element terminateVmElement = new Element(LinkedProcess.TERMINATE_VM_TAG, LinkedProcess.LOP_VM_NAMESPACE);

        if(this.vmPassword != null) {
            terminateVmElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }       
        if(this.errorType != null) {
            terminateVmElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
            if(this.errorMessage != null) {
                terminateVmElement.setText(this.errorMessage);
            }
        }


        return LinkedProcess.xmlOut.outputString(terminateVmElement);
    }
}
