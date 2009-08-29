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
import org.linkedprocess.Jid;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.villein.Dispatcher;
import org.linkedprocess.villein.Villein;

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
public abstract class XmppProxy {

    protected Document discoInfoDocument;
    protected Dispatcher dispatcher;
    protected Jid jid;
    protected LinkedProcess.Status status;

    /**
     * Get the jid of the XMPP proxy entity.
     *
     * @return the jid of the XMPP proxy entity
     */
    public Jid getJid() {
        return this.jid;
    }

    /**
     * Set the jid of the XMPP proxy entity.
     *
     * @param jid the jid of the XMPP proxy entity
     */
    public void setJid(Jid jid) {
        this.jid = jid;
    }

    /**
     * Set the status of the XMPP proxy entity.
     * For farms this is active, busy, or inactive.
     * For regitries this is active or inactive.
     *
     * @param status the status of the XMPP proxy entity
     */
    public void setStatus(LinkedProcess.Status status) {
        this.status = status;
    }

    /**
     * Get the status of the XMPP proxy entity.
     * For farms this is active, busy, or inactive.
     * For regitries this is active or inactive.
     *
     * @return the status of the XMPP proxy entity
     */
    public LinkedProcess.Status getStatus() {
        return this.status;
    }

    /**
     * Retrieve the disco#info XML document of this XMPP proxy entity.
     */
    public void refreshDiscoInfo() {
        if (this.dispatcher != null) {
            ServiceDiscoveryManager discoManager = this.dispatcher.getServiceDiscoveryManager();
            try {
                DiscoverInfo discoInfo = discoManager.discoverInfo(this.jid.toString());
                this.discoInfoDocument = LinkedProcess.createXMLDocument(discoInfo.toXML());
            } catch (Exception e) {
                Villein.LOGGER.warning("Problem loading disco#info: " + e.getMessage());
            }
        }
    }

    /**
     * Get the features supported by this XMPP proxy entity as specified in the var of the features of its disco#info document.
     * For farms, there must exist the feature http://linkedprocess.org/06/2006/Farm#.
     * For registries, there must exist the feature http://linkedprocess.org/06/2006/Registry#.
     *
     * @return the set of features supported by this XMPP entity
     */
    public Set<String> getFeatures() {
        Set<String> features = new HashSet<String>();
        if (null != this.discoInfoDocument) {
            Element queryElement = this.discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                for (Object featureElement : queryElement.getChildren(LinkedProcess.FEATURE_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE))) {
                    if (featureElement instanceof Element)
                        features.add(((Element) featureElement).getAttributeValue(LinkedProcess.VAR_ATTRIBUTE));
                }
            }
        }
        return features;
    }

    /**
     * Determines if the disco#info document of this XMPP proxy has a provided feature.
     *
     * @param feature the feature to check for in the disco#info of this XMPP proxy entity
     * @return whether the feature is supported by this XMPP proxy
     */
    public boolean hasFeature(String feature) {
        return this.getFeatures().contains(feature);
    }

    /**
     * Get a field from the disco#info extended data forms of this XMPP proxy.
     *
     * @param variable the variable name of the field to retrieve
     * @return the associated field of this variable
     */
    public Field getField(String variable) {
        List<Field> fields = this.getFields();
        for (Field field : fields) {
            if (field.getVariable().equals(variable))
                return field;
        }
        return null;
    }

    /**
     * Get the set of all fields associated with the disco#info of this XMPP proxy.
     * These fields are specified in the disco#info extended data forms.
     *
     * @return the set of all fields associated with the disco#info of this XMPP proxy
     */
    public List<Field> getFields() {
        List<Field> proxyFields = new ArrayList<Field>();
        if (null != this.discoInfoDocument) {
            Element queryElement = discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                Namespace xNamespace = Namespace.getNamespace(LinkedProcess.X_JABBER_DATA_NAMESPACE);
                Element xElement = queryElement.getChild(LinkedProcess.X_TAG, xNamespace);
                if (null != xElement) {
                    for (Object fieldElement : xElement.getChildren(LinkedProcess.FIELD_TAG, xNamespace)) {
                        if (fieldElement instanceof Element) {
                            String variable = ((Element) fieldElement).getAttributeValue(LinkedProcess.VAR_ATTRIBUTE);
                            Field field = new Field();
                            field.setVariable(variable);
                            field.setLabel(((Element) fieldElement).getAttributeValue(LinkedProcess.LABEL_ATTRIBUTE));
                            field.setType(((Element) fieldElement).getAttributeValue(LinkedProcess.TYPE_ATTRIBUTE));
                            for (Object valueElement : ((Element) fieldElement).getChildren(LinkedProcess.VALUE_TAG, xNamespace)) {
                                if (valueElement instanceof Element) {
                                    String val = ((Element) valueElement).getText();
                                    if (null != val)
                                        field.addValue(val);
                                    field.setOption(false);   // TODO: is it per field or per option?
                                }
                            }
                            for (Object optionElement : ((Element) fieldElement).getChildren(LinkedProcess.OPTION_TAG, xNamespace)) {
                                if (optionElement instanceof Element) {
                                    Element valueElement = ((Element) optionElement).getChild(LinkedProcess.VALUE_TAG, xNamespace);
                                    if (null != valueElement) {
                                        String val = valueElement.getText();
                                        if (null != val)
                                            field.addValue(val);
                                        field.setOption(true); // TODO: is it per field or per option?
                                    }
                                }
                            }
                            proxyFields.add(field);
                        }
                    }
                }
            }
        }
        return proxyFields;
    }

    public boolean equals(Object xmppProxy) {
        return xmppProxy instanceof XmppProxy && ((XmppProxy)xmppProxy).getJid().equals(this.jid);
    }

    /**
     * A hash code for this XMPP proxy based on the hashCode of its jid.
     *
     * @return the hashcode of this XMPP proxy
     */
    public int hashCode() {
        return this.jid.hashCode();
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.jid + "]";
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
