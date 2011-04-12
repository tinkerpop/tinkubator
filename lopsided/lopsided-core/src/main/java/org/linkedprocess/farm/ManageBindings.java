/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.jdom.Element;
import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.TypedValue;
import org.linkedprocess.farm.os.VmBindings;
import org.linkedprocess.farm.os.errors.InvalidValueException;

/**
 * A manage_bindings packet is modeled by this class.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ManageBindings extends FarmIq {

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

    /**
     * Add a binding to this packet.
     *
     * @param name     the variable name of this binding
     * @param value    the variable value of this binding
     * @param datatype the variable datatype of this binding
     * @throws InvalidValueException thrown if the value can be typecast to the provided data type
     */
    public void addBinding(final String name, final String value, final String datatype) throws InvalidValueException {
        if (value == null && datatype == null) {
            this.bindings.putTyped(name, null);
        } else {
            TypedValue typeValue = new TypedValue(VmBindings.XMLSchemaDatatype.valueByURI(datatype), value);
            this.bindings.putTyped(name, typeValue);
        }
    }

    /**
     * Get a particular binding in this packets bindings.
     *
     * @param name the varible name of the binding to get
     * @return the value and datatype of the provided variable name
     */
    public TypedValue getBinding(final String name) {
        return this.bindings.getTyped(name);
    }

    /**
     * Get the set of all bindings of this packet.
     *
     * @return the bindings of this packet
     */
    public VmBindings getBindings() {
        return this.bindings;
    }

    /**
     * Set the set of all bindings of this packet.
     *
     * @param bindings the bindings of this packet
     */
    public void setBindings(final VmBindings bindings) {
        this.bindings = bindings;
    }

    /**
     * Get the manage_bidings component of this IQ packet.
     *
     * @return the manage_bindings component of this IQ packet
     */
    public String getChildElementXML() {

        Element manageBindingsElement = new Element(LinkedProcess.MANAGE_BINDINGS_TAG, LinkedProcess.LOP_FARM_NAMESPACE);

        if (this.vmId != null) {
            manageBindingsElement.setAttribute(LinkedProcess.VM_ID_ATTRIBUTE, this.vmId);
        }

        if (this.getType() == IQ.Type.GET) {
            for (String key : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG, LinkedProcess.LOP_FARM_NAMESPACE);
                b.setAttribute(LinkedProcess.NAME_ATTRIBUTE, key);
                manageBindingsElement.addContent(b);
            }
        } else if (this.getType() == IQ.Type.SET || this.getType() == IQ.Type.RESULT) {
            for (String key : this.bindings.keySet()) {
                Element b = new Element(LinkedProcess.BINDING_TAG, LinkedProcess.LOP_FARM_NAMESPACE);
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
