/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.ArrayList;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.junit.Before;
import org.junit.Test;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.testing.offline.OfflineTest;
import org.linkedprocess.registry.LopRegistry;

public class OfflineRegistryTest extends OfflineTest {

	private LopRegistry reg;
	private ArrayList<Packet> sentPackets;
	private MockRegistryXmppConnection connection;

	@Before
	public void startVM() throws Exception {
		XMPPConnection registryConn = createMock(XMPPConnection.class);
		connection = new MockRegistryXmppConnection(
				new ConnectionConfiguration(server, port), "LoPRegistry",
				registryConn);
		OfflineTest.prepareMocksAndConnection(registryConn, connection);
		sentPackets = connection.sentPackets;
		replayAll();
		// start the farm
		reg = new LopRegistry(server, port, username, password);
	}

	@Test
	public void correctStartupAndShutdown() {
		assertEquals(3, connection.packetListeners.size());
		assertNotNull(connection.subscription);
		assertNotNull(connection.presence);
		assertNotNull(connection.discoItems);
	}

	@Test
	public void subscriptionShouldBeOfTypeBoth() {
		connection.clearPackets();
		Presence sub = new Presence(Presence.Type.subscribe);
		sub.setFrom(CLIENT_JID);
		connection.subscription.processPacket(sub);
		connection.presence.processPacket(sub);
		assertEquals(2, connection.sentPackets.size());
		assertEquals(Presence.Type.subscribed, ((Presence) sentPackets.get(0))
				.getType());
		// the subscription request from the registry
		assertEquals(Presence.Type.subscribe, ((Presence) sentPackets.get(1))
				.getType());
		// answer the subscription
		sub.setType(Presence.Type.subscribed);
		connection.presence.processPacket(sub);
	}

	@Test
	public void presenceFromActiveSubscriptionShouldListCountrysideInRegistryAsActive() {

		// first, subscribe to the registry
		Presence presence = new Presence(Presence.Type.subscribe);
		presence.setFrom(CLIENT_JID);
		connection.subscription.processPacket(presence);
		connection.presence.processPacket(presence);
		connection.clearPackets();

		// now, send available from farm
		presence.setType(Presence.Type.available);
		connection.presence.processPacket(presence);

		// now, test discovering the farms countryside
		connection.clearPackets();
		DiscoverItems di = new DiscoverItems();
		di.setFrom(CLIENT_JID);
		connection.discoItems.processPacket(di);
		assertEquals(1, sentPackets.size());
		DiscoverItems result = (DiscoverItems) connection.getLastPacket();
		assertEquals(LinkedProcess.generateBareJid(CLIENT_JID), result
				.getItems().next().getEntityID());
		
		//having the farm unavailable should exclude it from the listing
		// now, send unavailable from farm
		presence.setType(Presence.Type.unavailable);
		connection.presence.processPacket(presence);
		// now, test discovering the farm
		connection.clearPackets();
		connection.discoItems.processPacket(di);
		assertEquals(1, sentPackets.size());
		result = (DiscoverItems) connection.getLastPacket();
		assertFalse(result.getItems().hasNext());
		
	}

	@Test
	public void presenceWithoutSubscriptionShouldWorkToo() {

		connection.clearPackets();
		Presence presence = new Presence(Presence.Type.available);
		presence.setFrom(CLIENT_JID);
		connection.presence.processPacket(presence);
		connection.clearPackets();
		DiscoverItems di = new DiscoverItems();
		di.setFrom(CLIENT_JID);
		connection.discoItems.processPacket(di);
		assertEquals(1, sentPackets.size());
		DiscoverItems result = (DiscoverItems) connection.getLastPacket();
		assertEquals(LinkedProcess.generateBareJid(CLIENT_JID), result
				.getItems().next().getEntityID());
		
	}
}
