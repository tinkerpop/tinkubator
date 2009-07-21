package org.linkedprocess.os;

import org.openrdf.model.vocabulary.XMLSchema;

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
        INTEGER(XSD_NAMESPACE + "integer", Integer.class),
        STRING(XSD_NAMESPACE + "string", String.class);

        private final String uri;
        private final Class javaClass;

        private XMLSchemaDatatype(final String uri,
                                  final Class javaClass) {
            this.uri = uri;
            this.javaClass = javaClass;
        }

        public static XMLSchemaDatatype valueByClass(final Class javaClass) {
            for (XMLSchemaDatatype d : values()) {
                if (d.javaClass.equals(javaClass)) {
                    return d;
                }
            }

            throw new IllegalArgumentException("no datatype for class: " + javaClass);
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
        
        public Object createValue(final String v) {
            switch (this) {
                case INTEGER:
                    return new Integer(v);
                case STRING:
                    return v;
                default:
                    throw new RuntimeException("no object constructor for data type: " + this);
            }
        }
    }

    public class TypedValue {
        private final XMLSchemaDatatype type;
        private final String value;

        public XMLSchemaDatatype getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public TypedValue(final XMLSchemaDatatype type,
                          final String value) {
            this.type = type;
            this.value = value;
        }

        public TypedValue(final Object value) {
            // Note: class inheritance is not taken into account
            type = XMLSchemaDatatype.valueByClass(value.getClass());

            // Note: toString() is assumed to be an appropriate serializer for all types.
            this.value = value.toString();
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
                           final TypedValue value) {
        Object v = value.getType().createValue(value.getValue());
        return put(key, v);
    }
}
