package gov.lanl.cnls.linkedprocess;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.SpawnListener;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { XmppFarm.class, XmppClient.class,
		ServiceDiscoveryManager.class })
// ignore fishy stuff
@PowerMockIgnore( { "javax.script", "org.apache.log4j" })
public class MockingTest {

	private static String username1 = "linked.process.4@gmail.com";
	private static String password1 = "linked45";

	private static String server = "talk1.l.google.com";
	private static int port = 5222;
	private List<Packet> sentPackets;
	private List<PacketListener> packetListeners;
	private String mockFarmId;

	@Before
	public void setup() throws Exception {
		ServiceDiscoveryManager mdm = createMock(ServiceDiscoveryManager.class);
		XMPPConnection mockConnection = createMock(XMPPConnection.class);
		mockStatic(ServiceDiscoveryManager.class);
		expect(ServiceDiscoveryManager.getInstanceFor(mockConnection))
				.andReturn(mdm).anyTimes();
		Iterator<String> features = new LinkedList<String>().iterator();
		expect(mdm.getFeatures()).andReturn(features).anyTimes();
		mdm.addFeature(isA(String.class));
		// 2 for Farm, 2 for VM
		expectLastCall().times(4);
		// a bit more explicit matchers to help PowerMock choose the right
		// constructor
		expectNew(XMPPConnection.class,
				new Class<?>[] { ConnectionConfiguration.class },
				isA(ConnectionConfiguration.class)).andReturn(mockConnection)
				.anyTimes();

		mockConnection.connect();
		// one time for the XMPPFarm, one time for the spawned VM
		expectLastCall().times(2);
		// just connection info, not important right now
		expect(mockConnection.getHost()).andReturn("mock.linkedprocess.org")
				.anyTimes();
		mockFarmId = "mockUser";
		expect(mockConnection.getUser()).andReturn(mockFarmId).anyTimes();
		expect(mockConnection.isAnonymous()).andReturn(false).anyTimes();
		expect(mockConnection.isConnected()).andReturn(true).anyTimes();
		expect(mockConnection.isSecureConnection()).andReturn(false).anyTimes();
		expect(mockConnection.isUsingCompression()).andReturn(false).anyTimes();
		expect(mockConnection.isUsingTLS()).andReturn(false).anyTimes();
		packetListeners = new LinkedList<PacketListener>();
		mockConnection.addPacketListener(MultiCaptureMatcher
				.multiCapture(packetListeners), isA(PacketFilter.class));
		expectLastCall().times(6);
		Roster mockRoster = createMock(Roster.class);
		mockRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		expect(mockConnection.getRoster()).andReturn(mockRoster).anyTimes();
		sentPackets = new ArrayList<Packet>();
		mockConnection
				.sendPacket(MultiCaptureMatcher.multiCapture(sentPackets));
		expectLastCall().anyTimes();
		mockConnection.login(isA(String.class), isA(String.class),
				isA(String.class));
		expectLastCall().times(2);
	}

	@Test
	public void testMockingXMPP() throws Exception {
		
		// create a spawn packet for later injection
		Packet spawnPacket = createMock(Packet.class);
		String spawnXML = "<not real XML>";
		expect(spawnPacket.toXML()).andReturn(spawnXML).anyTimes();
		String mockClient = "mockClient";
		expect(spawnPacket.getFrom()).andReturn(mockClient).anyTimes();
		String spawnPacketId = "123";
		expect(spawnPacket.getPacketID()).andReturn(spawnPacketId).anyTimes();

		//activate all mock objects
		replayAll();

		// start testing
		XmppFarm xmppFarm = new XmppFarm(server, port, username1, password1);
		
		
		// one presence packet should have been sent
		assertTrue(sentPackets.size() == 1);
		// now we should have 3 PacketListeners to the XMPP connection
		assertTrue(packetListeners.size() == 3);
		// first registered listener should be our SpawnListener
		PacketListener spawnListener = packetListeners.get(0);
		assertTrue(spawnListener instanceof SpawnListener);

		// let's send a spawn packet!
		spawnListener.processPacket(spawnPacket);
		
		// now, a new packet should have been sent back from the VM
		assertEquals(3, sentPackets.size());
		// sent packet should refer to the same pID
		IQ result = (IQ) sentPackets.get(2);
		assertEquals(result.getPacketID(), spawnPacketId);
		assertEquals(result.getType(), IQ.Type.RESULT);
		// check the whole xml string
		assertEquals(
				"<spawn xmlns=\"http://linkedprocess.org/protocol#LoPFarm\" vm_jid=\""
						+ mockFarmId + "\" />", result.getChildElementXML());

		// now we should have 3 more PacketListeners for the VM
		assertTrue(packetListeners.size() == 6);
		verifyAll();
	}
}
