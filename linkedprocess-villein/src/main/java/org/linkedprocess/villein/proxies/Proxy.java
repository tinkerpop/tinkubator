/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.villein.Dispatcher;
import org.linkedprocess.villein.LopVillein;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Proxy is the base class for all proxies. A proxy is identified by either a bare or fully-qualified JID.
 * Information about a proxy can be accessed via disco#info and helpful interfaces to this document are provided by means
 * of the field inner class.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class Proxy implements Comparable {

    protected boolean available = false;
    protected String fullJid;
    protected final Dispatcher dispatcher;
    protected Document discoInfoDocument;

    public Proxy(final String fullJid, final Dispatcher dispatcher) {
        this.fullJid = fullJid;
        this.dispatcher = dispatcher;
        this.refreshDiscoInfo();
    }

    public Proxy(final String fullJid, final Dispatcher dispatcher, final Document discoInfoDocument) {
        this.fullJid = fullJid;
        this.dispatcher = dispatcher;
        this.discoInfoDocument = discoInfoDocument;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public void setFullJid(String fullJid) {
        this.fullJid = fullJid;
    }

    public String getFullJid() {
        return this.fullJid;
    }

    public void refreshDiscoInfo() {
        if(this.dispatcher != null) {
            ServiceDiscoveryManager discoManager = this.dispatcher.getServiceDiscoveryManager();
            try {
                DiscoverInfo discoInfo = discoManager.discoverInfo(this.getFullJid());
                this.discoInfoDocument = LinkedProcess.createXMLDocument(discoInfo.toXML());
            } catch (Exception e) {
                LopVillein.LOGGER.warning("Problem loading disco#info: " + e.getMessage());
            }
        }
    }

    public Set<String> getFeatures() {
        Set<String> features = new HashSet<String>();
        if (null != this.discoInfoDocument) {
            Element queryElement = this.discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                for (Element featureElement : (java.util.List<Element>) queryElement.getChildren(LinkedProcess.FEATURE_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE))) {
                    features.add(featureElement.getAttributeValue(LinkedProcess.VAR_ATTRIBUTE));
                }
            }
        }
        return features;
    }

    public Field getField(String variable) {
        List<Field> fields = this.getFields();
        for (Field field : fields) {
            if (field.getVariable().equals(variable))
                return field;
        }
        return null;
    }

    public List<Field> getFields() {
        List<Field> proxyFields = new ArrayList<Field>();
        if (null != this.discoInfoDocument) {
            Element queryElement = discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                Namespace xNamespace = Namespace.getNamespace(LinkedProcess.X_JABBER_DATA_NAMESPACE);
                Element xElement = queryElement.getChild(LinkedProcess.X_TAG, xNamespace);
                if (null != xElement) {
                    for (Element fieldElement : (java.util.List<Element>) xElement.getChildren(LinkedProcess.FIELD_TAG, xNamespace)) {
                        String variable = fieldElement.getAttributeValue(LinkedProcess.VAR_ATTRIBUTE);
                        Field field = new Field();
                        field.setVariable(variable);
                        field.setLabel(fieldElement.getAttributeValue(LinkedProcess.LABEL_ATTRIBUTE));
                        field.setType(fieldElement.getAttributeValue(LinkedProcess.TYPE_ATTRIBUTE));
                        for (Element valueElement : (java.util.List<Element>) fieldElement.getChildren(LinkedProcess.VALUE_TAG, xNamespace)) {
                            String val = valueElement.getText();
                            if (null != val)
                                field.addValue(val);
                            field.setOption(false);   // TODO: is it per field or per option?
                        }
                        for (Element optionElement : (java.util.List<Element>) fieldElement.getChildren(LinkedProcess.OPTION_TAG, xNamespace)) {
                            Element valueElement = optionElement.getChild(LinkedProcess.VALUE_TAG, xNamespace);
                            if (null != valueElement) {
                                String val = valueElement.getText();
                                if (null != val)
                                    field.addValue(val);
                                field.setOption(true); // TODO: is it per field or per option?
                            }
                        }
                        proxyFields.add(field);
                    }
                }
            }
        }
        return proxyFields;
    }

    public int compareTo(Object proxy) {
        if (proxy instanceof Proxy) {
            return this.fullJid.compareTo(((Proxy) proxy).getFullJid());
        } else {
            throw new ClassCastException();
        }
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.fullJid + "]";
    }


    public class Field {
        protected String variable;
        protected String label;
        protected String type;
        protected boolean option;
        protected Set<String> values = new HashSet<String>();


        public String getVariable() {
            return this.variable;
        }

        public void setVariable(String variable) {
            this.variable = variable;
        }

        public String getLabel() {
            return this.label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean getOption() {
            return this.option;
        }

        public void setOption(boolean option) {
            this.option = option;
        }

        public void addValue(String value) {
            this.values.add(value);
        }

        public Set<String> getValues() {
            return this.values;
        }

        public String getValue() {
            if (null != this.values && this.values.size() > 0)
                return this.values.iterator().next();
            else
                return null;
        }

        public int getIntegerValue() {
            return Integer.valueOf(this.getValue());
        }

        public long getLongValue() {
            return Long.valueOf(this.getValue());
        }

        public boolean getBooleanValue() {
            return Boolean.valueOf(this.getValue());
        }
    }
}
