package org.linkedprocess.os;

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
        STRING(XSD_NAMESPACE + "string", String.class),
        DOUBLE(XSD_NAMESPACE + "double", Double.class);


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

        public static String expandDatatypeAbbreviation(String abbreviatedDatatype) {
            return XSD_NAMESPACE + abbreviatedDatatype.substring(4);
        }

        public String abbreviate() {
            return "xsd:" + uri.substring(uri.indexOf("#")+1);
        }
        
        public Object createValue(final String v) {
            switch (this) {
                case INTEGER:
                    return new Integer(v);
                case STRING:
                    return v;
                case DOUBLE:
                    return new Double(v);
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
                           final TypedValue value) {
        if(null != value) {
            Object v = value.getDatatype().createValue(value.getValue());
            return put(key, v);
        } else {
            return put(key, null);
        }   
    }
}
