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
        XmppProxy xmppProxy = this.getVillein().getCloudProxy().getXmppProxy(new Jid(presence.getFrom()));
        LinkedProcess.Status status = PresencePacketListener.getStatus(presence);
        if (xmppProxy != null) {

            if (status == LinkedProcess.Status.INACTIVE) {
                cloudProxy.removeXmppProxy(new Jid(presence.getFrom()));
            } else {
                xmppProxy.setStatus(status);
            }

            if (isUnsubscribed(presence)) {
                cloudProxy.removeCountrysideProxy(new Jid(presence.getFrom()).getBareJid());
            }

        } else {
            CountrysideProxy countrysideProxy = cloudProxy.getCountrysideProxy(new Jid(presence.getFrom()).getBareJid());
            if (null == countrysideProxy) {
                cloudProxy.addCountrysideProxy(new CountrysideProxy(new Jid(presence.getFrom()).getBareJid()));
                for (PresenceHandler presenceHandler : this.getVillein().getPresenceHandlers()) {
                    presenceHandler.handlePresenceUpdate(new Jid(presence.getFrom()).getBareJid(), LinkedProcess.Status.ACTIVE);
                }
            }

            // Determine which type of XMPP proxy the presence packet is from
            DiscoverInfo discoInfo = this.getDiscoInfo(presence.getFrom());
            Document discoInfoDocument = null;
            try {
                discoInfoDocument = LinkedProcess.createXMLDocument(discoInfo.toXML());
            } catch (Exception e) {
                Villein.LOGGER.warning("disco#info document is not valid XML: " + e.getMessage());
            }

            if (isFarm(discoInfo)) {
                FarmProxy farmProxy = new FarmProxy(new Jid(presence.getFrom()), this.getVillein().getDispatcher(), discoInfoDocument);
                farmProxy.setStatus(status);
                try {
                    countrysideProxy.addFarmProxy(farmProxy);
                    xmppProxy = farmProxy;
                } catch (ParentProxyNotFoundException e) {
                    Villein.LOGGER.warning(e.getMessage());
                }
            } else if (isRegistry(discoInfo)) {
                RegistryProxy registryProxy = new RegistryProxy(new Jid(presence.getFrom()), this.getVillein().getDispatcher(), discoInfoDocument);
                registryProxy.setStatus(status);
                try {
                    countrysideProxy.addRegistryProxy(registryProxy);
                    xmppProxy = registryProxy;
                } catch (ParentProxyNotFoundException e) {
                    Villein.LOGGER.warning(e.getMessage());
                }
            } else {
                Villein.LOGGER.info("The following resource is not an LoP resource: " + presence.getFrom());
            }
        }

        if (xmppProxy != null) {
            // Handlers
            for (PresenceHandler presenceHandler : this.getVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(xmppProxy.getJid(), status);
            }
        }

        if (isUnsubscribed(presence)) {
            // Handlers
            for (PresenceHandler presenceHandler : this.getVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(new Jid(presence.getFrom()).getBareJid(), LinkedProcess.Status.INACTIVE);
            }
        }
    }

    private static LinkedProcess.Status getStatus(Presence presence) {
        if (presence.getType() == Presence.Type.unavailable || presence.getType() == Presence.Type.unsubscribe || presence.getType() == Presence.Type.unsubscribed) {
            return LinkedProcess.Status.INACTIVE;
        } else if (presence.getMode() == Presence.Mode.dnd) {
            return LinkedProcess.Status.BUSY;
        } else {
            return LinkedProcess.Status.ACTIVE;
        }
    }

    private static boolean isUnsubscribed(Presence presence) {
        return presence.getType() == Presence.Type.unsubscribe || presence.getType() == Presence.Type.unsubscribed;
    }

}

