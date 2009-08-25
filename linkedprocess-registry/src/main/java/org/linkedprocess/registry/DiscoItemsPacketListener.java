/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.registry;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.DiscoverItems;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class DiscoItemsPacketListener extends RegistryPacketListener {

    public DiscoItemsPacketListener(Registry registry) {
        super(registry);
    }

    public void processPacket(Packet packet) {
        DiscoverItems discoItems = (DiscoverItems) packet;
        if (discoItems.getType() == IQ.Type.GET) {
            DiscoverItems returnDiscoItems = this.getXmppRegistry().createDiscoItems(discoItems.getFrom());
            returnDiscoItems.setPacketID(discoItems.getPacketID());
            this.getXmppRegistry().getConnection().sendPacket(returnDiscoItems);
            System.out.println(returnDiscoItems.toXML());
        }

    }
}
