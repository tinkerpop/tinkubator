package org.linkedprocess.xmpp.villein.proxies;

import org.linkedprocess.xmpp.villein.proxies.Proxy;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.Dispatcher;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.*;
import java.io.IOException;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:53:17 AM
 */
public class FarmProxy extends Proxy {

    protected Map<String, VmProxy> vmProxies = new HashMap<String, VmProxy>();
    protected Collection<String> supportedVmSpecies = new HashSet<String>();
    protected String farmPassword;
    protected Document discoInfoDocument;

    public FarmProxy(Dispatcher dispatcher) {
        super(dispatcher);
    }

    public VmProxy getVmProxy(String vmJid) {
        return vmProxies.get(vmJid);
    }

    public void addVmProxy(VmProxy vmProxy) {
        this.vmProxies.put(vmProxy.getFullJid(), vmProxy);
    }

    public Collection<VmProxy> getVmProxies() {
        return this.vmProxies.values();
    }

    public void removeVmProxy(String vmJid) {
        this.vmProxies.remove(vmJid);
    }

    public Collection<String> getSupportedVmSpecies() {
        return this.supportedVmSpecies;
    }

    public void addSupportedVmSpecies(String supportedVmSpecies) {
        this.supportedVmSpecies.add(supportedVmSpecies);
    }

    public void setSupportedVmSpecies(Collection<String> supportedVmSpecies) {
        this.supportedVmSpecies = supportedVmSpecies;
    }

    public String getFarmPassword() {
        return this.farmPassword;
    }

    public void setFarmPassword(String farmPassword) {
        this.farmPassword = farmPassword;
    }

    public void spawnVm(final String vmSpecies, final Handler<VmProxy> resultHandler, final Handler<XMPPError> errorHandler) {
        this.dispatcher.getSpawnVmCommand().send(this, vmSpecies, resultHandler, errorHandler);
    }


    public void refreshDiscoInfoDocument(XmppVillein xmppVillein) throws XMPPException, JDOMException, IOException {
        ServiceDiscoveryManager discoManager = xmppVillein.getDiscoManager();
        DiscoverInfo discoInfo = discoManager.discoverInfo(this.getFullJid());
        this.discoInfoDocument = LinkedProcess.createXMLDocument(discoInfo.toXML());
    }

    public Set<String> getFarmFeatures() {
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
        if (null != this.discoInfoDocument) {
            Element queryElement = discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                Namespace xNamespace = Namespace.getNamespace(LinkedProcess.X_JABBER_DATA_NAMESPACE);
                Element xElement = queryElement.getChild(LinkedProcess.X_TAG, xNamespace);
                if (null != xElement) {
                    for (Element fieldElement : (java.util.List<Element>) xElement.getChildren(LinkedProcess.FIELD_TAG, xNamespace)) {
                        String var = fieldElement.getAttributeValue(LinkedProcess.VAR_ATTRIBUTE);
                        if (null != var && var.equals(variable)) {
                            Field field = new Field();
                            field.setVariable(variable);
                            field.setLabel(fieldElement.getAttributeValue(LinkedProcess.LABEL_ATTRIBUTE));
                            field.setType(fieldElement.getAttributeValue(LinkedProcess.TYPE_ATTRIBUTE));
                            for (Element valueElement : (java.util.List<Element>) xElement.getChildren(LinkedProcess.FIELD_TAG, xNamespace)) {

                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public class Field {
        protected String variable;
        protected String label;
        protected String type;
        protected Set<String> values = new HashSet<String>();


        public String getVariable() {
            return variable;
        }

        public void setVariable(String variable) {
            this.variable = variable;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void addValue(String value) {
            this.values.add(value);
        }

        public Set<String> getValues() {
            return this.values;
        }

  
    }

}
