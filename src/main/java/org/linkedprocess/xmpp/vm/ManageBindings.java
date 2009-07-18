package org.linkedprocess.xmpp.vm;

import org.linkedprocess.xmpp.LopIq;
import org.linkedprocess.LinkedProcess;
import org.jdom.Element;
import org.jivesoftware.smack.packet.IQ;

import java.util.Map;
import java.util.HashMap;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:37:57 PM
 */
public class ManageBindings extends VirtualMachineIq {

    protected Map<String, String> bindings = new HashMap<String, String>();

    public void addBinding(String name, String value) {
        this.bindings.put(name, value);
    }

    public String getBinding(String name) {
        return this.bindings.get(name);
    }

    public Map<String, String> getBindings() {
        return this.bindings;
    }

    public void setBindings(Map<String, String> bindings) {
        this.bindings = bindings;
    }

    public String getChildElementXML() {

        Element manageBindingsElement = new Element(LinkedProcess.MANAGE_BINDINGS_TAG, LinkedProcess.LOP_VM_NAMESPACE);

        if(this.vmPassword != null) {
            manageBindingsElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }
        if(this.errorType != null) {
            manageBindingsElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
            if(this.errorMessage != null) {
                manageBindingsElement.setText(this.errorMessage);
            }
        } else if(this.getType() == IQ.Type.GET) {
            for(String binding : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG, LinkedProcess.LOP_VM_NAMESPACE);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, binding);
                manageBindingsElement.addContent(b);
            }
        } else if(this.getType() == IQ.Type.SET || this.getType() == IQ.Type.RESULT) {
            for(String binding : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG, LinkedProcess.LOP_VM_NAMESPACE);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, binding);
                if(this.bindings.get(binding) != null)
                    b.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, this.bindings.get(binding));
                else
                    b.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, "null");
                manageBindingsElement.addContent(b);
            }
        }


        return LinkedProcess.xmlOut.outputString(manageBindingsElement);
    }
}
