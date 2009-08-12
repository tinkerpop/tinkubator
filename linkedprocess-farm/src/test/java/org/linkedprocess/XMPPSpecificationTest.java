package org.linkedprocess;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.linkedprocess.xmpp.XMPPConnectionWrapper;
import org.linkedprocess.xmpp.XmppClient;
import org.linkedprocess.xmpp.farm.XmppFarm;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { XmppFarm.class, XmppClient.class,
		ServiceDiscoveryManager.class })
// ignore fishy stuff
@PowerMockIgnore( { "javax.script", "org.apache.log4j" })
public class XMPPSpecificationTest {

	protected static final String JAVASCRIPT = "javascript";
	protected static final String PASSWORD = "password";
	protected static String username = "linked.process.4@gmail.com";
	protected static String password = "linked45";

	protected static String server = "talk1.l.google.com";
	protected static int port = 5222;
	protected static String CLIENT_JID = "mockClient";
	protected static String IQ_PACKET_ID = "123";

	

	@After
	public void shutdown() {
		verifyAll();

	}
	@Before
	public void mockStatics() {
		mockStatic(ServiceDiscoveryManager.class);
		
		ServiceDiscoveryManager.setIdentityName(isA(String.class));
		expectLastCall().anyTimes();
		ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
		expectLastCall().anyTimes();
	}
	
	public static MockXMPPConnection prepareMocksAndConnection(
			String connectionId, XMPPConnection xmppConnection)
			throws Exception {
		ServiceDiscoveryManager mdm = createMock(ServiceDiscoveryManager.class);
		MockXMPPConnection connection = mockXMPPConnection(connectionId,
				xmppConnection);
		expect(ServiceDiscoveryManager.getInstanceFor(xmppConnection)).andReturn(mdm);
		expectLastCall().anyTimes();
		expect(mdm.getFeatures()).andReturn(connection.getDiscInfoFeatures().iterator()).anyTimes();
		mdm.addFeature(MultiCaptureMatcher.multiCapture(connection.disc_info_features));
		expectLastCall().anyTimes();
		
		mdm.setExtendedInfo(capture(connection.dataformCapture));
		expectLastCall().anyTimes();
		// the magic of inserting the mock connections!
		// first call is cfor the LoPFarm
		expectNew(XMPPConnectionWrapper.class,
				isA(ConnectionConfiguration.class)).andReturn(connection);
		return connection;

	}

	protected static MockXMPPConnection mockXMPPConnection(String id,
			XMPPConnection conn) throws Exception {
		MockXMPPConnection mockConnection = new MockXMPPConnection(
				new ConnectionConfiguration(server, port), id);
		mockConnection.setDelegate(conn);

		Roster mockRoster = createMock(Roster.class);
		mockRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		expectLastCall().anyTimes();
		mockConnection.setRoster(mockRoster);
		mockRoster.createEntry(CLIENT_JID, CLIENT_JID, null);
		expectLastCall().anyTimes();
		return mockConnection;

	}


}
