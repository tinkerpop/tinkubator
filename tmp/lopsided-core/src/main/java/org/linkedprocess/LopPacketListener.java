/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class LopPacketListener implements PacketListener {
    public XmppClient xmppClient;

    public LopPacketListener(XmppClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    protected Document getDiscoInfoDocument(Jid jid) {
        ServiceDiscoveryManager discoManager = this.xmppClient.getDiscoManager();
        try {
            DiscoverInfo discoverInfo = discoManager.discoverInfo(jid.toString());
			String discoverInfoXml = discoverInfo.toXML();
			return LinkedProcess.createXMLDocument(discoverInfoXml);
        } catch (Exception e) {
            XmppClient.LOGGER.warning(e.getMessage());
            System.out.println(e);
            return null;
        }
    }

    protected Document getDiscoItemsDocument(Jid jid) {
        ServiceDiscoveryManager discoManager = this.xmppClient.getDiscoManager();
        try {
            return LinkedProcess.createXMLDocument(discoManager.discoverItems(jid.toString()).toXML());
        } catch (Exception e) {
            XmppClient.LOGGER.warning(e.getMessage());
            return null;
        }
    }

    protected static Set<String> getFeatures(Document discoInfoDocument) {
        Set<String> features = new HashSet<String>();
        if (null != discoInfoDocument) {
            Element queryElement = discoInfoDocument.getRootElement().getChild(LinkedProcess.QUERY_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
            if (null != queryElement) {
                for (Object featureElement : queryElement.getChildren(LinkedProcess.FEATURE_TAG, Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE))) {
                    if (featureElement instanceof Element)
                        features.add(((Element) featureElement).getAttributeValue(LinkedProcess.VAR_ATTRIBUTE));
                }
            }
        }
        return features;
    }

    public static boolean isRegistry(Document discoInfoDocument) {
        return LopPacketListener.getFeatures(discoInfoDocument).contains(LinkedProcess.LOP_REGISTRY_NAMESPACE);
    }

    public static boolean isFarm(Document discoInfoDocument) {
        return LopPacketListener.getFeatures(discoInfoDocument).contains(LinkedProcess.LOP_FARM_NAMESPACE);
    }
}
