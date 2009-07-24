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

        if (this.vmPassword != null) {
            terminateVmElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }

        return LinkedProcess.xmlOut.outputString(terminateVmElement);
    }
}
