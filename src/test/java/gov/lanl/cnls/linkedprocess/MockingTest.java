package gov.lanl.cnls.linkedprocess;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.verify;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replay;

import java.util.Iterator;
import java.util.LinkedList;

import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;

import static org.powermock.api.easymock.PowerMock.mockStatic;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({XmppFarm.class, XmppClient.class, ServiceDiscoveryManager.class})
public class MockingTest {

	private static String username1 = "linked.process.4@gmail.com";
	private static String password1 = "linked45";

	private static String server = "talk1.l.google.com";
	private static int port = 5222;

	@Test
	public void testMockingXMPP() throws Exception {
		XMPPConnection mockConnection = createMock(XMPPConnection.class);
		mockStatic(ServiceDiscoveryManager.class);
		ServiceDiscoveryManager mdm = createMock(ServiceDiscoveryManager.class);
		expect(ServiceDiscoveryManager.getInstanceFor(mockConnection)).andReturn(mdm );
		Iterator<String> features = new LinkedList<String>().iterator();
		expect(mdm.getFeatures()).andReturn(features );
		mdm.addFeature(LinkedProcess.LOP_NAMESPACE);
		mdm.addFeature(LinkedProcess.DISCO_INFO_NAMESPACE);
		ConnectionConfiguration mockConfig = createMock(ConnectionConfiguration.class);
		expectNew(ConnectionConfiguration.class, notNull(), notNull())
				.andReturn(mockConfig);
		expectNew(XMPPConnection.class, mockConfig).andReturn(mockConnection).anyTimes();
		
		mockConnection.connect();
		expect(mockConnection.getHost()).andReturn("mock.linkedprocess.org")
				.anyTimes();
		expect(mockConnection.getUser()).andReturn("mockUser").anyTimes();
		expect(mockConnection.isAnonymous()).andReturn(false).anyTimes();
		expect(mockConnection.isConnected()).andReturn(true).anyTimes();
		expect(mockConnection.isSecureConnection()).andReturn(false).anyTimes();
		expect(mockConnection.isUsingCompression()).andReturn(false).anyTimes();
		expect(mockConnection.isUsingTLS()).andReturn(false).anyTimes();
		Roster mockRoster = createMock(Roster.class);
		mockRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		expect(mockConnection.getRoster()).andReturn(mockRoster ).anyTimes();
		
		mockConnection.sendPacket(isA(Packet.class));

		mockConnection.login(isA(String.class), isA(String.class),
				isA(String.class));
		replay(ServiceDiscoveryManager.class);
		replay(mockConfig, ConnectionConfiguration.class);
		replay(mockConnection, XMPPConnection.class);
		replay(mdm);
		replay(mockRoster);

		XmppFarm xmppFarm = new XmppFarm(server, port, username1, password1);
		verify(mockConnection, XMPPConnection.class);
	}

}
