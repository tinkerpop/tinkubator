/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.registry;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.linkedprocess.testing.offline.MockXmppConnection;

public class MockRegistryXmppConnection extends MockXmppConnection {

	public PacketListener subscription, presence, discoItems;
	PacketInterceptor discoItemsInterceptor;

	public MockRegistryXmppConnection(ConnectionConfiguration connConfig,
			String id, XMPPConnection connection) {
		super(connConfig, id, connection);
	}

	@Override
	public void addPacketListener(PacketListener listener, PacketFilter filter) {
		super.addPacketListener(listener, filter);
		if (listener instanceof PresenceSubscriptionListener) {
			subscription = listener;
		}
		if (listener instanceof PresencePacketListener) {
			presence = listener;
		}

	}

	@Override
	public void addPacketWriterInterceptor(PacketInterceptor interceptor,
			PacketFilter filter) {
		this.discoItemsInterceptor = interceptor;

	}

	@Override
	public void sendPacket(Packet packet) {
		if (discoItemsInterceptor != null && packet instanceof DiscoverItems) {
			discoItemsInterceptor.interceptPacket(packet);
		}
		super.sendPacket(packet);
	}

}
