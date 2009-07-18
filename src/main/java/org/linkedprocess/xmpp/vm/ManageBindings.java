package org.linkedprocess.xmpp.vm;

import org.linkedprocess.xmpp.LopIq;
import org.linkedprocess.LinkedProcess;
import org.jdom.Element;

import java.util.Map;
import java.util.HashMap;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:37:57 PM
 */
public class ManageBindings extends LopIq {

    public enum ManageType {GET, SET}

    protected Map<String, String> bindings = new HashMap<String, String>();
    protected ManageType type;

    public void addBinding(String name, String value) {
        this.bindings.put(name, value);
    }

    public String getBinding(String name) {
        return this.bindings.get(name);
    }

    public void setManageType(ManageType type) {
        this.type = type;
    }

    public ManageType getManageType() {
        return this.type;
    }


    public String getChildElementXML() {

        Element manageBindingsElement = new Element(LinkedProcess.SUBMIT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE);

        if(this.vmPassword != null) {
            manageBindingsElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }
        if(this.errorType != null) {
            manageBindingsElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
            if(this.errorMessage != null) {
                manageBindingsElement.setText(this.errorMessage);
            }
        } else if(type == ManageType.GET) {
            for(String binding : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, binding);
                manageBindingsElement.addContent(b);
            }
        } else if(type == ManageType.SET) {
            for(String binding : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, binding);
                b.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, this.bindings.get(binding));
                manageBindingsElement.addContent(b);
            }
        }


        return LinkedProcess.xmlOut.outputString(manageBindingsElement);
    }
}
