package org.linkedprocess.os;

/**
 * User: marko
* Date: Jul 21, 2009
* Time: 3:55:37 PM
*/
public class TypedValue {
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
        datatype = VMBindings.XMLSchemaDatatype.valueByClass(value.getClass());

        // Note: toString() is assumed to be an appropriate serializer for all types.
        this.value = value.toString();
    }
}
