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
import org.linkedprocess.Jid;
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


    public PresencePacketListener(Villein villein) {
        super(villein);
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        Villein.LOGGER.info("Presence received from " + presence.getFrom());
        Villein.LOGGER.info(presence.toXML());

        CloudProxy cloudProxy = this.getVillein().getCloudProxy();
        Jid presenceJid = new Jid(presence.getFrom());
        Jid countrysideJid = presenceJid.getBareJid();
        XmppProxy xmppProxy = this.getVillein().getCloudProxy().getXmppProxy(presenceJid);
        LinkedProcess.Status status = PresencePacketListener.getStatus(presence);


        if (isUnsubscribed(presence)) {
            /////////
            // If the presence is an unsubscribe, then remove the countryside from the cloud and alert the presence handlers.
            /////////
            cloudProxy.removeCountrysideProxy(countrysideJid);
            // Handlers
            for (PresenceHandler presenceHandler : this.getVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(countrysideJid, LinkedProcess.Status.INACTIVE);
            }
        } else if (xmppProxy != null) {
            /////////
            // If its not an unsubscribe and the presence is coming from a known XMPP proxy (e.g. Farm or registry), then
            // remove it from the cloud if its inactive or just update its status. Then alert all presence handlers.
            /////////
            if (status == LinkedProcess.Status.INACTIVE) {
                cloudProxy.removeXmppProxy(presenceJid);
            } else {
                xmppProxy.setStatus(status);
            }
            // Handlers
            for (PresenceHandler presenceHandler : this.getVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(presenceJid, status);
            }
        } else {
            /////////
            // Finally, it is an unknown XMPP proxy from a potentially unknown countryside.
            // Create the countryside and proxy and alert the presence handlers.
            /////////
            CountrysideProxy countrysideProxy = cloudProxy.getCountrysideProxy(countrysideJid);
            if (null == countrysideProxy) {
                cloudProxy.addCountrysideProxy(new CountrysideProxy(countrysideJid));
                for (PresenceHandler presenceHandler : this.getVillein().getPresenceHandlers()) {
                    presenceHandler.handlePresenceUpdate(countrysideJid, LinkedProcess.Status.ACTIVE);
                }
            }
            if (!presenceJid.isBareJid()) {
                // If its not a countryside jid (bare jid) then determine which type of XMPP proxy the presence packet is from
                Document discoInfoDocument = this.getDiscoInfo(presenceJid);
                if (discoInfoDocument != null) {
                    if (XmppProxy.isFarm(discoInfoDocument)) {
                        FarmProxy farmProxy = new FarmProxy(presenceJid, this.getVillein().getDispatcher(), discoInfoDocument);
                        farmProxy.setStatus(status);
                        try {
                            countrysideProxy.addFarmProxy(farmProxy);
                            // Handlers
                            for (PresenceHandler presenceHandler : this.getVillein().getPresenceHandlers()) {
                                presenceHandler.handlePresenceUpdate(presenceJid, status);
                            }
                        } catch (ParentProxyNotFoundException e) {
                            Villein.LOGGER.warning(e.getMessage());
                        }
                    } else if (XmppProxy.isRegistry(discoInfoDocument)) {
                        RegistryProxy registryProxy = new RegistryProxy(presenceJid, this.getVillein().getDispatcher(), discoInfoDocument, null);
                        registryProxy.setStatus(status);
                        try {
                            countrysideProxy.addRegistryProxy(registryProxy);
                            // Handlers
                            for (PresenceHandler presenceHandler : this.getVillein().getPresenceHandlers()) {
                                presenceHandler.handlePresenceUpdate(presenceJid, status);
                            }
                        } catch (ParentProxyNotFoundException e) {
                            Villein.LOGGER.warning(e.getMessage());
                        }
                    } else {
                        Villein.LOGGER.info("The following resource is not an LoP entity: " + presenceJid);
                    }
                }
            }
        }
    }

    private static LinkedProcess.Status getStatus(Presence presence) {
        if (presence.getType() == Presence.Type.unavailable || presence.getType() == Presence.Type.unsubscribe || presence.getType() == Presence.Type.unsubscribed) {
            return LinkedProcess.Status.INACTIVE;
        } else if (presence.isAway()) { // dnd, extended away, away
            return LinkedProcess.Status.BUSY;
        } else {
            return LinkedProcess.Status.ACTIVE;
        }
    }

    private static boolean isUnsubscribed(Presence presence) {
        return presence.getType() == Presence.Type.unsubscribe || presence.getType() == Presence.Type.unsubscribed;
    }

}

