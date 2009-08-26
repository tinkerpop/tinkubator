/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.registry;


import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PresencePacketListener extends RegistryPacketListener {


    public PresencePacketListener(Registry registry) {
        super(registry);
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        Registry.LOGGER.info("Arrived " + PresencePacketListener.class.getName());
        Registry.LOGGER.info(presence.toXML());

        if (presence.isAvailable()) {
            DiscoverInfo discoInfo = this.getDiscoInfo(packet.getFrom());
            if (isFarm(discoInfo)) {
                Registry.LOGGER.info("Registering farm: " + packet.getFrom());
                this.getRegistry().addActiveFarm(packet.getFrom());
            }
        } else {
            Registry.LOGGER.info("Unregistering resource: " + packet.getFrom());
            this.getRegistry().removeActiveFarm(packet.getFrom());
        }
    }
}
