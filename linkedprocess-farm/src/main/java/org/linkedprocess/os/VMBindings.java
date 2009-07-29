package org.linkedprocess.os;

import org.linkedprocess.os.errors.InvalidValueException;
import org.linkedprocess.os.errors.NoSuchDatatypeException;

import javax.script.Bindings;
import java.util.HashMap;

/**
 * Author: josh
 * Date: Jul 21, 2009
 * Time: 12:47:54 PM
 */
public class VMBindings extends HashMap<String, Object> implements Bindings {
    private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

    public enum XMLSchemaDatatype {

        BOOLEAN(XSD_NAMESPACE + "boolean", Boolean.class),
        DOUBLE(XSD_NAMESPACE + "double", Double.class),
        INTEGER(XSD_NAMESPACE + "integer", Integer.class),
        LONG(XSD_NAMESPACE + "long", Long.class),
        STRING(XSD_NAMESPACE + "string", String.class);

        private final String uri;
        private final Class javaClass;

        private XMLSchemaDatatype(final String uri,
                                  final Class javaClass) {
            this.uri = uri;
            this.javaClass = javaClass;
        }

        public static XMLSchemaDatatype valueByClass(final Class javaClass) throws NoSuchDatatypeException {
            for (XMLSchemaDatatype d : values()) {
                if (d.javaClass.equals(javaClass)) {
                    return d;
                }
            }

            throw new NoSuchDatatypeException("no datatype for class: " + javaClass);
        }

        public static XMLSchemaDatatype valueByURI(final String uri) {
            for (XMLSchemaDatatype d : values()) {
                if (d.uri.equals(uri)) {
                    return d;
                }
            }

            throw new IllegalArgumentException("no datatype for URI: " + uri);
        }

        public String getURI() {
            return uri;
        }

        public static String expandDatatypeAbbreviation(String abbreviatedDatatype) {
            return XSD_NAMESPACE + abbreviatedDatatype.substring(4);
        }

        public String abbreviate() {
            return "xsd:" + uri.substring(uri.indexOf("#") + 1);
        }

        public Object createValue(final String v) {
            switch (this) {
                case BOOLEAN:
                    return Boolean.valueOf(v);
                case DOUBLE:
                    return Double.valueOf(v);
                case INTEGER:
                    return Integer.valueOf(v);
                case LONG:
                    return Long.valueOf(v);
                case STRING:
                    return v;
                default:
                    throw new RuntimeException("no object constructor for data type: " + this);
            }
        }
    }

    public VMBindings() {
        super();
    }

    public TypedValue getTyped(final String key) {
        Object v = get(key);
        return null == v ? null : new TypedValue(v);
    }

    public Object putTyped(final String key,
                           final TypedValue value) throws InvalidValueException {
        if (null != value) {
            try {
                Object v = value.getDatatype().createValue(value.getValue());
                return put(key, v);
            } catch (NumberFormatException e) {
                throw new InvalidValueException("bad value for datatype <"
                        + value.getDatatype().getURI() + ">: " + value.getValue());
            }
        } else {
            return put(key, null);
        }
    }
}
