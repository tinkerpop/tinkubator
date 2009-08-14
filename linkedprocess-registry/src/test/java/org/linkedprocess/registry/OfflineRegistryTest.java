package org.linkedprocess.registry;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.ArrayList;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Before;
import org.junit.Test;
import org.linkedprocess.testing.offline.OfflineTest;
import org.linkedprocess.xmpp.registry.XmppRegistry;

public class OfflineRegistryTest extends OfflineTest {

	private XmppRegistry reg;
	private ArrayList<Packet> sentPackets;
	private MockRegistryXmppConnection connection;

	@Before
	public void startVM() throws Exception {
		XMPPConnection registryConn = createMock(XMPPConnection.class);
		connection = new MockRegistryXmppConnection(new ConnectionConfiguration(
				server, port), "LoPRegistry", registryConn);
		OfflineTest.prepareMocksAndConnection(registryConn, connection);
		sentPackets = connection.sentPackets;
		replayAll();
		// start the farm
		reg = new XmppRegistry(server, port, username, password);
	}
	
	@Test
	public void correctStartupAndShutdown() {
		assertEquals(3, connection.packetListeners.size());
	}
}
