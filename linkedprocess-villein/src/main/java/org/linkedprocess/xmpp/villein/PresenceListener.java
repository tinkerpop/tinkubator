package org.linkedprocess.xmpp.villein;

import org.jdom.Document;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.proxies.*;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 11:57:44 AM
 */
public class PresenceListener extends LopVilleinListener {


    public PresenceListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        XmppVillein.LOGGER.info("Presence received from " + presence.getFrom());
        XmppVillein.LOGGER.info(presence.toXML());

        Proxy proxy = this.getXmppVillein().getProxy(presence.getFrom());

        if (proxy != null && (presence.getType() == Presence.Type.unavailable ||
                presence.getType() == Presence.Type.unsubscribe ||
                presence.getType() == Presence.Type.unsubscribed)) {
            this.getXmppVillein().removeProxy(presence.getFrom());
            // Handlers
            for (PresenceHandler presenceHandler : this.getXmppVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(proxy, presence.getType());
            }
            return;
        }


        if (null != proxy) {
            proxy.setPresence(presence);
        } else {
            if (LinkedProcess.isBareJid(presence.getFrom())) {
                CountrysideProxy countrysideProxy = new CountrysideProxy(presence.getFrom(), this.getXmppVillein().getDispatcher());
                countrysideProxy.setPresence(presence);
                this.getXmppVillein().addCountrysideProxy(countrysideProxy);
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
                    farmProxy.setSupportedVmSpecies(this.getSupportedVmSpecies(discoInfo));
                    try {
                        this.getXmppVillein().addFarmProxy(farmProxy);
                        proxy = farmProxy;
                    } catch (ParentProxyNotFoundException e) {
                        XmppVillein.LOGGER.warning("Parent proxy was not found: " + e.getMessage());
                    }
                } else if (isRegistry(discoInfo)) {
                    RegistryProxy registryProxy = new RegistryProxy(presence.getFrom(), this.getXmppVillein().getDispatcher(), discoInfoDocument);
                    registryProxy.setPresence(presence);
                    try {
                        this.getXmppVillein().addRegistryProxy(registryProxy);
                        proxy = registryProxy;
                    } catch (ParentProxyNotFoundException e) {
                        XmppVillein.LOGGER.warning("Parent proxy was not found: " + e.getMessage());
                    }
                }
            }
        }
        // Handlers
        for (PresenceHandler presenceHandler : this.getXmppVillein().getPresenceHandlers()) {
            presenceHandler.handlePresenceUpdate(proxy, presence.getType());
        }
    }
}
