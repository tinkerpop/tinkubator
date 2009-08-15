package org.linkedprocess.registry;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.linkedprocess.testing.offline.MockXMPPConnection;
import org.linkedprocess.xmpp.registry.DiscoItemsListener;
import org.linkedprocess.xmpp.registry.PresenceListener;
import org.linkedprocess.xmpp.registry.PresenceSubscriptionListener;

public class MockRegistryXmppConnection extends MockXMPPConnection {

	public PacketListener subscription, presence, discoItems;

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
		if (listener instanceof PresenceListener) {
			presence = listener;
		}
		if (listener instanceof DiscoItemsListener) {
			discoItems = listener;
		}

	}

}
