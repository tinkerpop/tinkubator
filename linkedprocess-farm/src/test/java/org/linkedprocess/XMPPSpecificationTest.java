package org.linkedprocess;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.Collection;
import java.util.Vector;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DataForm;
import org.junit.After;
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
	protected static Vector<String> disc_info_features = new Vector<String>();
	protected static Vector<DataForm> disc_info_extended = new Vector<DataForm>();

	private void waitForResponse(Collection watching, int timeout) {
		int currentSize = watching.size();
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() < startTime + timeout) {
			if (watching.size() > currentSize) {
				return;
			}
			try {
				Thread.sleep(50);
				// System.out.println(watching.size() + " > " + currentSize);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	@After
	public void shutdown() {
		verifyAll();

	}

	public static MockXMPPConnection prepareMocksAndConnection(
			String connectionId, XMPPConnection xmppConnection)
			throws Exception {
		disc_info_features = new Vector<String>();
		ServiceDiscoveryManager farm_mdm = createMock(ServiceDiscoveryManager.class);
		MockXMPPConnection connection = mockXMPPConnection(connectionId,
				xmppConnection);
		mockStatic(ServiceDiscoveryManager.class);
		expect(ServiceDiscoveryManager.getInstanceFor(xmppConnection))
				.andReturn(farm_mdm);
		expectLastCall().anyTimes();
		expect(farm_mdm.getFeatures()).andReturn(disc_info_features.iterator()).anyTimes();
		farm_mdm.addFeature(MultiCaptureMatcher.multiCapture(disc_info_features));
		expectLastCall().anyTimes();
		farm_mdm.setExtendedInfo(MultiCaptureMatcher.multiCapture(disc_info_extended));
		expectLastCall().anyTimes();
		ServiceDiscoveryManager.setIdentityName(isA(String.class));
		expectLastCall().anyTimes();
		ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
		expectLastCall().anyTimes();
		// the magic of inserting the mock connections!
		// first call is cfor the LoPFarm
		expectNew(XMPPConnectionWrapper.class,
				isA(ConnectionConfiguration.class)).andReturn(connection);
		expectLastCall().anyTimes();
		return connection;

	}

	protected static MockXMPPConnection mockXMPPConnection(String id,
			XMPPConnection conn) throws Exception {
		MockXMPPConnection mocConnection = new MockXMPPConnection(
				new ConnectionConfiguration(server, port), id);
		mocConnection.setDelegate(conn);

		Roster mockRoster = createMock(Roster.class);
		mockRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		expectLastCall().anyTimes();
		mocConnection.setRoster(mockRoster);
		mockRoster.createEntry(CLIENT_JID, CLIENT_JID, null);
		expectLastCall().anyTimes();
		return mocConnection;

	}


}
