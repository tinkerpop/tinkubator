/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein;

import org.jdom.Document;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.proxies.*;

/**
 * This class is repsonsible for managing all incoming presence packets and updating the proxies data structure appropriately.
 * Moreover, presence handlers can be provided in order to allow other areas of your code base access to these presence packets.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
class PresenceListener extends LopVilleinListener {


    public PresenceListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        XmppVillein.LOGGER.info("Presence received from " + presence.getFrom());
        XmppVillein.LOGGER.info(presence.toXML());

        Proxy proxy = this.getXmppVillein().getLopCloud().getProxy(presence.getFrom());

        if (proxy != null) {
            proxy.setPresence(presence);
            if (presence.getType() == Presence.Type.unavailable || presence.getType() == Presence.Type.unsubscribe || presence.getType() == Presence.Type.unsubscribed) {
                this.getXmppVillein().getLopCloud().removeProxy(presence.getFrom());
            }
        } else {
            if (LinkedProcess.isBareJid(presence.getFrom())) {
                CountrysideProxy countrysideProxy = new CountrysideProxy(presence.getFrom(), this.getXmppVillein().getDispatcher());
                countrysideProxy.setPresence(presence);
                this.getXmppVillein().getLopCloud().addCountrysideProxy(countrysideProxy);
                proxy = countrysideProxy;
            } else {
                DiscoverInfo discoInfo = this.getDiscoInfo(presence.getFrom());
                Document discoInfoDocument = null;
                try {
                    discoInfoDocument = LinkedProcess.createXMLDocument(discoInfo.toXML());
                } catch (Exception e) {
                    XmppVillein.LOGGER.warning("disco#info document is not valid XML: " + e.getMessage());
                }

                if (isFarm(discoInfo)) {
                    FarmProxy farmProxy = new FarmProxy(presence.getFrom(), this.getXmppVillein().getDispatcher(), discoInfoDocument);
                    farmProxy.setPresence(presence);
                    try {
                        this.getXmppVillein().getLopCloud().addFarmProxy(farmProxy);
                        proxy = farmProxy;
                    } catch (ParentProxyNotFoundException e) {
                        XmppVillein.LOGGER.warning("Parent proxy was not found: " + e.getMessage());
                    }
                } else if (isRegistry(discoInfo)) {
                    RegistryProxy registryProxy = new RegistryProxy(presence.getFrom(), this.getXmppVillein().getDispatcher(), discoInfoDocument);
                    registryProxy.setPresence(presence);
                    try {
                        this.getXmppVillein().getLopCloud().addRegistryProxy(registryProxy);
                        proxy = registryProxy;
                    } catch (ParentProxyNotFoundException e) {
                        XmppVillein.LOGGER.warning("Parent proxy was not found: " + e.getMessage());
                    }
                }
            }
        }

        if (proxy != null) {
            // Handlers
            for (PresenceHandler presenceHandler : this.getXmppVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(proxy, presence.getType());
            }
        }
    }
}

