package org.linkedprocess.xmpp.vm;

import org.jdom.Element;
import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VMBindings;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:37:57 PM
 */
public class ManageBindings extends VirtualMachineIq {

    protected VMBindings bindings = new VMBindings();

    public void addBinding(String name, String value) {
        this.bindings.put(name, value);
    }

    public VMBindings.TypedValue getBinding(String name) {
        return this.bindings.getTyped(name);
    }

    public VMBindings getBindings() {
        return this.bindings;
    }

    public void setBindings(VMBindings bindings) {
        this.bindings = bindings;
    }

    public String getChildElementXML() {

        Element manageBindingsElement = new Element(LinkedProcess.MANAGE_BINDINGS_TAG, LinkedProcess.LOP_VM_NAMESPACE);

        if (this.vmPassword != null) {
            manageBindingsElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }
        if (this.errorType != null) {
            manageBindingsElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
            if (this.errorMessage != null) {
                manageBindingsElement.setText(this.errorMessage);
            }
        } else if (this.getType() == IQ.Type.GET) {
            for (String key : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG, LinkedProcess.LOP_VM_NAMESPACE);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, key);
                manageBindingsElement.addContent(b);
            }
        } else if (this.getType() == IQ.Type.SET || this.getType() == IQ.Type.RESULT) {
            for (String key : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG, LinkedProcess.LOP_VM_NAMESPACE);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, key);
                VMBindings.TypedValue value = this.bindings.getTyped(key);
                if (null != value) {
                    b.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, value.getValue());
                    b.setAttribute(LinkedProcess.DATATYPE_ATTRIBUTE, value.getType().getURI());
                } else {
                    b.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, "null");
                }
                manageBindingsElement.addContent(b);
            }
        }

        return LinkedProcess.xmlOut.outputString(manageBindingsElement);
    }
}
