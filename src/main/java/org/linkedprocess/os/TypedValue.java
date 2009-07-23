package org.linkedprocess.os;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.NoSuchDatatypeException;

import java.util.logging.Logger;

/**
 * User: marko
* Date: Jul 21, 2009
* Time: 3:55:37 PM
*/
public class TypedValue {
    private static final Logger LOGGER = LinkedProcess.getLogger(TypedValue.class);
    
    private final VMBindings.XMLSchemaDatatype datatype;
    private final String value;

    public VMBindings.XMLSchemaDatatype getDatatype() {
        return datatype;
    }

    public String getValue() {
        return value;
    }

    public TypedValue(final VMBindings.XMLSchemaDatatype datatype,
                      final String value) {
        this.datatype = datatype;
        this.value = value;
    }

    public TypedValue(final Object value) {
        // Note: class inheritance is not taken into account
        VMBindings.XMLSchemaDatatype d;
        try {
            d = VMBindings.XMLSchemaDatatype.valueByClass(value.getClass());
        } catch (NoSuchDatatypeException e) {
            // Default to xsd:string if the data type is not known
            LOGGER.warning("no data type found for class: " + value.getClass() + ". Defaulting to xsd:string.");
            d = VMBindings.XMLSchemaDatatype.STRING;
        }
        this.datatype = d;

        // Note: toString() is assumed to be an appropriate serializer for all types.
        this.value = value.toString();
    }
}
