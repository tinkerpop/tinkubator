package org.linkedprocess.testing.offline;

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
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.XMPPConnectionWrapper;
import org.linkedprocess.xmpp.XmppClient;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { XmppClient.class, ServiceDiscoveryManager.class })
// ignore fishy stuff
@PowerMockIgnore( { "javax.script", "org.apache.log4j" })
public class OfflineTest {

	protected static final String JAVASCRIPT = "javascript";
	protected static final String PASSWORD = "password";
	protected static String username = "linked.process.4@offline.com";
	protected static String password = "linked45";

	protected static String server = "talk1.l.google.com";
	protected static int port = 5222;
	protected static String CLIENT_JID = "mockClient@mock.linkedprocess.org/LoPCLient/1234";
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

	public static void prepareMocksAndConnection(XMPPConnection xmppConnection,
			MockXMPPConnection mockConnection) throws Exception {
		ServiceDiscoveryManager mdm = createMock(ServiceDiscoveryManager.class);
		Roster mockRoster = createMock(Roster.class);
		mockRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		expectLastCall().anyTimes();
		mockConnection.setRoster(mockRoster);
		mockRoster.createEntry(CLIENT_JID, CLIENT_JID, null);
		expectLastCall().anyTimes();
		expect(ServiceDiscoveryManager.getInstanceFor(xmppConnection))
				.andReturn(mdm);
		expectLastCall().anyTimes();
		expect(mdm.getFeatures()).andReturn(
				mockConnection.getDiscInfoFeatures().iterator()).anyTimes();
		mdm.addFeature(MultiCaptureMatcher
				.multiCapture(mockConnection.disc_info_features));
		expectLastCall().anyTimes();

		mdm.setExtendedInfo(capture(mockConnection.dataformCapture));
		expectLastCall().anyTimes();
		DiscoverInfo farmInfo = new DiscoverInfo();
		farmInfo.addFeature(LinkedProcess.LOP_FARM_NAMESPACE);
		expect(mdm.discoverInfo(CLIENT_JID)).andReturn(farmInfo ).anyTimes();
		// the magic of inserting the mock connections!
		expectNew(XMPPConnectionWrapper.class,
				isA(ConnectionConfiguration.class)).andReturn(mockConnection);

	}
}
