/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.os;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.errors.NoSuchDatatypeException;

import java.util.logging.Logger;

/**
 * User: marko
 * Date: Jul 21, 2009
 * Time: 3:55:37 PM
 */
public class TypedValue {
    private static final Logger LOGGER = LinkedProcess.getLogger(TypedValue.class);

    private final VmBindings.XMLSchemaDatatype datatype;
    private final String value;

    public VmBindings.XMLSchemaDatatype getDatatype() {
        return datatype;
    }

    public String getValue() {
        return value;
    }

    public TypedValue(final VmBindings.XMLSchemaDatatype datatype,
                      final String value) {
        this.datatype = datatype;
        this.value = value;
    }

    public TypedValue(final Object value) {
        // Note: class inheritance is not taken into account
        VmBindings.XMLSchemaDatatype d;
        try {
            d = VmBindings.XMLSchemaDatatype.valueByClass(value.getClass());
        } catch (NoSuchDatatypeException e) {
            // Default to xsd:string if the data type is not known
            LOGGER.warning("no data type found for class: " + value.getClass() + ". Defaulting to xsd:string.");
            d = VmBindings.XMLSchemaDatatype.STRING;
        }
        this.datatype = d;

        // Note: toString() is assumed to be an appropriate serializer for all types.
        this.value = value.toString();
    }
}
