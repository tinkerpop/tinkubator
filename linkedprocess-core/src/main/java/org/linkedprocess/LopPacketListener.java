/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jdom.Document;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class LopPacketListener implements PacketListener {
    public XmppClient xmppClient;

    public LopPacketListener(XmppClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    protected Document getDiscoInfo(Jid jid) {
        ServiceDiscoveryManager discoManager = this.xmppClient.getDiscoManager();
        try {
            return LinkedProcess.createXMLDocument(discoManager.discoverInfo(jid.toString()).toXML());
        } catch (Exception e) {
            XmppClient.LOGGER.warning(e.getMessage());
            return null;
        }
    }
}
