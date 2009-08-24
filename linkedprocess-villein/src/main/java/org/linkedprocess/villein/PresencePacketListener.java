/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein;

import org.jdom.Document;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.villein.proxies.*;

/**
 * This class is repsonsible for managing all incoming presence packets and updating the proxies data structure appropriately.
 * Moreover, presence handlers can be provided in order to allow other areas of your code base access to these presence packets.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
class PresencePacketListener extends VilleinPacketListener {


    public PresencePacketListener(LopVillein lopVillein) {
        super(lopVillein);
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        LopVillein.LOGGER.info("Presence received from " + presence.getFrom());
        LopVillein.LOGGER.info(presence.toXML());

        Proxy proxy = this.getLopVillein().getLopCloud().getProxy(presence.getFrom());

        if (proxy != null) {
            if (!PresencePacketListener.isAvailable(presence)) {
                proxy.setAvailable(false);
                this.getLopVillein().getLopCloud().removeProxy(presence.getFrom());
            }
        } else {
            if (PresencePacketListener.isAvailable(presence)) {
                if (LinkedProcess.isBareJid(presence.getFrom())) {
                    CountrysideProxy countrysideProxy = new CountrysideProxy(presence.getFrom());
                    countrysideProxy.setAvailable(PresencePacketListener.isAvailable(presence));
                    this.getLopVillein().getLopCloud().addCountrysideProxy(countrysideProxy);
                    proxy = countrysideProxy;
                } else {
                    DiscoverInfo discoInfo = this.getDiscoInfo(presence.getFrom());
                    Document discoInfoDocument = null;
                    try {
                        discoInfoDocument = LinkedProcess.createXMLDocument(discoInfo.toXML());
                    } catch (Exception e) {
                        LopVillein.LOGGER.warning("disco#info document is not valid XML: " + e.getMessage());
                    }

                    if (isFarm(discoInfo)) {
                        FarmProxy farmProxy = new FarmProxy(presence.getFrom(), this.getLopVillein().getDispatcher(), discoInfoDocument);
                        farmProxy.setAvailable(PresencePacketListener.isAvailable(presence));
                        try {
                            this.getLopVillein().getLopCloud().addFarmProxy(farmProxy);
                            proxy = farmProxy;
                        } catch (ParentProxyNotFoundException e) {
                            LopVillein.LOGGER.warning("Parent proxy was not found: " + e.getMessage());
                        }
                    } else if (isRegistry(discoInfo)) {
                        RegistryProxy registryProxy = new RegistryProxy(presence.getFrom(), this.getLopVillein().getDispatcher(), discoInfoDocument);
                        registryProxy.setAvailable(PresencePacketListener.isAvailable(presence));
                        try {
                            this.getLopVillein().getLopCloud().addRegistryProxy(registryProxy);
                            proxy = registryProxy;
                        } catch (ParentProxyNotFoundException e) {
                            LopVillein.LOGGER.warning("Parent proxy was not found: " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (proxy != null) {
            // Handlers
            for (PresenceHandler presenceHandler : this.getLopVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(proxy, PresencePacketListener.isAvailable(presence));
            }
        }
    }

    private static boolean isAvailable(Presence presence) {
        if (presence.getType() == Presence.Type.unavailable || presence.getType() == Presence.Type.unsubscribe || presence.getType() == Presence.Type.unsubscribed) {
            return false;
        } else {
            return true;
        }
    }
}

