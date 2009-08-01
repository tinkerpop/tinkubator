package org.linkedprocess.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.linkedprocess.LinkedProcess;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;

/**
 * User: marko
 * Date: Jul 23, 2009
 * Time: 11:50:55 AM
 */
public abstract class LopListener implements PacketListener {
    public XmppClient xmppClient;

    public LopListener(XmppClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    protected DiscoverInfo getDiscoInfo(String jid) {
        ServiceDiscoveryManager discoManager = this.xmppClient.getDiscoManager();
        try {
            return discoManager.discoverInfo(jid);
        } catch (XMPPException e) {
            XmppClient.LOGGER.severe("XmppException with DiscoveryManager: " + e.getMessage());
            return null;
        }
    }

    protected boolean isVirtualMachine(DiscoverInfo discoInfo) {
        if(discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_VM_NAMESPACE);
        else
            return false;
    }

    protected boolean isFarm(DiscoverInfo discoInfo) {
        if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_FARM_NAMESPACE);
        else
            return false;
    }

    protected boolean isRegistry(DiscoverInfo discoInfo) {
         if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_REGISTRY_NAMESPACE);
        else
            return false;
    }

    protected Collection<String> getSupportedVmSpecies(DiscoverInfo discoInfo) {
        if (discoInfo != null) {
            List<String> supportedVmSpecies = new LinkedList<String>();
            try {
                Document doc = LinkedProcess.createXMLDocument(discoInfo.toXML());
                Element queryElement = doc.getRootElement().getChild("query", Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
                Element xElement = queryElement.getChild(LinkedProcess.X_TAG, Namespace.getNamespace(LinkedProcess.X_NAMESPACE));
                for (Element field : (List<Element>) xElement.getChildren()) {
                    if (field.getAttributeValue("var").equals(LinkedProcess.VM_SPECIES_ATTRIBUTE)) {
                        for (Element option : (List<Element>) field.getChildren("option", Namespace.getNamespace(LinkedProcess.X_NAMESPACE))) {
                            Element value = option.getChild("value", Namespace.getNamespace(LinkedProcess.X_NAMESPACE));
                            if (value != null) {
                                String vmSpecies = value.getText();
                                if (vmSpecies != null && vmSpecies.length() > 0)
                                    supportedVmSpecies.add(vmSpecies);
                            }
                        }
                    }
                }
                return supportedVmSpecies;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return new HashSet<String>();
        }
    }
}
