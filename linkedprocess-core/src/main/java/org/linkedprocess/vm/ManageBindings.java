/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.vm;

import org.jdom.Element;
import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.os.errors.InvalidValueException;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 6:37:57 PM
 */
public class ManageBindings extends VirtualMachineIq {

    protected VmBindings bindings = new VmBindings();
    protected String badDatatypeMessage;
    protected String invalidValueMessage;

    public String getInvalidValueMessage() {
        return invalidValueMessage;
    }

    public void setInvalidValueMessage(final String invalidValueMessage) {
        this.invalidValueMessage = invalidValueMessage;
    }

    public String getBadDatatypeMessage() {
        return badDatatypeMessage;
    }

    public void setBadDatatypeMessage(final String badDatatypeMessage) {
        this.badDatatypeMessage = badDatatypeMessage;
    }

    public void addBinding(String name, String value, String datatype) throws InvalidValueException {
        if (value == null && datatype == null) {
            this.bindings.putTyped(name, null);
        } else {
            TypedValue typeValue = new TypedValue(VmBindings.XMLSchemaDatatype.valueByURI(datatype), value);
            this.bindings.putTyped(name, typeValue);
        }
    }

    public TypedValue getBinding(String name) {
        return this.bindings.getTyped(name);
    }

    public VmBindings getBindings() {
        return this.bindings;
    }

    public void setBindings(VmBindings bindings) {
        this.bindings = bindings;
    }

    public String getChildElementXML() {

        Element manageBindingsElement = new Element(LinkedProcess.MANAGE_BINDINGS_TAG, LinkedProcess.LOP_VM_NAMESPACE);

        if (this.vmPassword != null) {
            manageBindingsElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }

        if (this.getType() == IQ.Type.GET) {
            for (String key : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG, LinkedProcess.LOP_VM_NAMESPACE);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, key);
                manageBindingsElement.addContent(b);
            }
        } else if (this.getType() == IQ.Type.SET || this.getType() == IQ.Type.RESULT) {
            for (String key : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG, LinkedProcess.LOP_VM_NAMESPACE);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, key);
                TypedValue value = this.bindings.getTyped(key);
                if (null != value) {
                    b.setAttribute(LinkedProcess.VALUE_ATTRIBUTE, value.getValue());
                    b.setAttribute(LinkedProcess.DATATYPE_ATTRIBUTE, value.getDatatype().getURI());
                }
                manageBindingsElement.addContent(b);
            }
        }

        return LinkedProcess.xmlOut.outputString(manageBindingsElement);
    }
}
