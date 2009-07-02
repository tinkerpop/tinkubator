package gov.lanl.cnls.linkedprocess;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import gov.lanl.cnls.linkedprocess.xmpp.XMPPConnectionWrapper;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.PresenceSubscriptionListener;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.SpawnVm;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.SpawnVmListener;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.TerminateVmListener;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.AbortJobListener;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.Evaluate;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.EvaluateListener;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.JobStatusListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.junit.After;
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
public class XMPPSpecificationTest {

	private static String username1 = "linked.process.4@gmail.com";
	private static String password1 = "linked45";

	private static String server = "talk1.l.google.com";
	private static int port = 5222;
	private String mockFarmId;
	private String mockClient = "mockClient";
	private String spawnPacketId = "123";
	private MockXMPPConnection mockFarmConn;
	private XmppFarm xmppFarm;
	// private SpawnVm spawn;
	private ServiceDiscoveryManager mdm;
	private MockXMPPConnection mockVM1Conn;
	private XMPPConnection mockXmppConnection;
	private Roster mockRoster;

	@Test
	public void subscribingToAFarmsRosterShouldResultInThreePresencePacketsBack()
			throws Exception {

		// activate all mock objects
		replayAll();

		xmppFarm = new XmppFarm(server, port, username1, password1);
		ArrayList<Packet> sentPackets = mockFarmConn.sentPackets;
		assertEquals(2, sentPackets.size());
		// get rid of the startup stuff
		mockFarmConn.clearPackets();
		// now we should have 3 PacketListeners to the Farms XMPP connection
		ArrayList<PacketListener> packetListeners = mockFarmConn.packetListeners;
		assertTrue(packetListeners.size() == 3);
		// third registered listener should be our SpawnListener
		PresenceSubscriptionListener subscriptionListener = (PresenceSubscriptionListener) packetListeners
				.get(2);
		Presence subscriptionPacket = new Presence(Presence.Type.subscribe);
		subscriptionPacket.setTo(mockFarmId);
		subscriptionPacket.setFrom(mockClient);
		subscriptionListener.processPacket(subscriptionPacket);
		assertEquals(3, sentPackets.size());
		// subscription acc
		Presence p0 = (Presence) sentPackets.get(0);
        assertEquals(Presence.Type.subscribed, p0.getType());
		// subscribe request to the client
        Presence p1 = (Presence) sentPackets.get(1);
        assertEquals(Presence.Type.subscribe, p1.getType());
        // Farm status
		Presence p2 = (Presence) sentPackets.get(2);
        assertEquals(Presence.Type.available, p2.getType());
        assertEquals(p2.getPriority(), LinkedProcess.HIGHEST_PRIORITY);
        assertEquals(p2.getStatus(), XmppFarm.STATUS_MESSAGE_ACTIVE);
        xmppFarm.shutDown();

	}

	@Test
	public void checkCorrectStartupAndShutdownPacketsAndListeners()
			throws Exception {

		// activate all mock objects
		replayAll();

		// start the farm
		xmppFarm = new XmppFarm(server, port, username1, password1);

		// two presence packets should have been sent upon connecting
		ArrayList<Packet> sentPackets = mockFarmConn.sentPackets;
		assertEquals(2, sentPackets.size());
		// Farm started
		assertEquals(Presence.Type.available, ((Presence) sentPackets.get(0))
				.getType());
		assertEquals(XmppFarm.STATUS_MESSAGE_STARTING, ((Presence) sentPackets
				.get(0)).getStatus());
		assertEquals(LinkedProcess.HIGHEST_PRIORITY, ((Presence) sentPackets
				.get(0)).getPriority());
		// scheduler started
		assertEquals(Presence.Type.available, ((Presence) sentPackets.get(1))
				.getType());
		assertEquals(XmppFarm.STATUS_MESSAGE_ACTIVE, ((Presence) sentPackets.get(1))
				.getStatus());
		assertEquals(LinkedProcess.HIGHEST_PRIORITY, ((Presence) sentPackets
				.get(1)).getPriority());

		// now we should have 3 PacketListeners to the Farms XMPP connection
		ArrayList<PacketListener> packetListeners = mockFarmConn.packetListeners;
		assertTrue(packetListeners.size() == 3);
		assertTrue(packetListeners.get(0) instanceof SpawnVmListener);
		assertTrue(packetListeners.get(1) instanceof TerminateVmListener);
		assertTrue(packetListeners.get(2) instanceof PresenceSubscriptionListener);

		// shut the farm down
		xmppFarm.shutDown();
		assertEquals(4, sentPackets.size());
		// scheduler shut down
		assertEquals(Presence.Type.unavailable, ((Presence) sentPackets.get(2))
				.getType());
		assertEquals(XmppFarm.STATUS_MESSAGE_TERMINATING,
				((Presence) sentPackets.get(2)).getStatus());
		assertEquals(LinkedProcess.HIGHEST_PRIORITY, ((Presence) sentPackets
				.get(2)).getPriority());
		// The Farm terminated
		assertEquals(Presence.Type.unavailable, ((Presence) sentPackets.get(3))
				.getType());
		assertEquals(null, ((Presence) sentPackets.get(3)).getStatus());

	}

	@Test
	public void sendingASpawnPacketShouldStartASeparateVM() throws Exception {
		expectNew(XMPPConnectionWrapper.class,
				isA(ConnectionConfiguration.class)).andReturn(mockVM1Conn);

		// activate all mock objects
		replayAll();

		// start the farm
		xmppFarm = new XmppFarm(server, port, username1, password1);
		ArrayList<Packet> sentPackets = mockFarmConn.sentPackets;
		mockFarmConn.clearPackets();
		SpawnVm spawn = new SpawnVm();
		spawn.setVmSpecies("javascript");
		spawn.setPacketID(spawnPacketId);
		spawn.setFrom(mockClient);
		spawn.setType(IQ.Type.GET);
		// let's send a spawn packet to the SpwnVMListener!
		mockFarmConn.packetListeners.get(0).processPacket(spawn);
		// now, a new packet should have been sent back from the VM
		assertEquals(1, sentPackets.size());
		// sent packet should refer to the same pID
		SpawnVm result = (SpawnVm) sentPackets.get(0);
		assertEquals(result.getPacketID(), spawnPacketId);
		assertEquals(result.getType(), IQ.Type.RESULT);
		assertFalse("The returned VM ID should not be the Farms id, right?",
				mockFarmConn.getUser().equals(mockVM1Conn.getUser()));
		// now we should have 3 PacketListeners for the VM
		ArrayList<PacketListener> vm1packetListeners = mockVM1Conn.packetListeners;
		assertEquals(3, vm1packetListeners.size());
		assertTrue(vm1packetListeners.get(0) instanceof EvaluateListener);
		assertTrue(vm1packetListeners.get(1) instanceof JobStatusListener);
		assertTrue(vm1packetListeners.get(2) instanceof AbortJobListener);
		xmppFarm.shutDown();
	}

	@Test
	public void sendingAnEvalPacketShouldReturnAResult() throws Exception {
		expectNew(XMPPConnectionWrapper.class,
				isA(ConnectionConfiguration.class)).andReturn(mockVM1Conn);

		// activate all mock objects
		replayAll();

		// start the farm
		xmppFarm = new XmppFarm(server, port, username1, password1);
		SpawnVm spawn = new SpawnVm();
		spawn.setVmSpecies("javascript");
		spawn.setPacketID(spawnPacketId);
		spawn.setFrom(mockClient);
		spawn.setType(IQ.Type.GET);
		// let's send a spawn packet to the SpwnVMListener!
		mockFarmConn.packetListeners.get(0).processPacket(spawn);

		// send the eval packet
		Evaluate eval = new Evaluate();
		eval.setPacketID(spawnPacketId);
		eval.setExpression("20 + 52;");
		eval.setTo(mockVM1Conn.getUser());
		eval.setFrom(mockClient);
		mockVM1Conn.clearPackets();
		// ArrayList<Packet> vmsentPackets = mockVM1Conn.sentPackets;
		mockVM1Conn.packetListeners.get(0).processPacket(eval);
		// wait for processing
		Thread.sleep(1000);
		// now, a new packet should have been sent back from the VM
		assertEquals(1, mockVM1Conn.sentPackets.size());
		// sent packet should refer to the same pID
		Evaluate result = (Evaluate) mockVM1Conn.sentPackets.get(0);
		assertEquals(result.getPacketID(), spawnPacketId);
		assertEquals(result.getType(), IQ.Type.RESULT);
		assertEquals("72.0", result.getExpression());
		xmppFarm.shutDown();
	}

	@After
	public void shutdown() {
		// shut down
		// xmppFarm.shutDown();
		verifyAll();

	}


	@Before
	public void setup() throws Exception {
		mdm = createMock(ServiceDiscoveryManager.class);
		mockXmppConnection = createMock(XMPPConnection.class);
		mockFarmId = "LopServer/LoPFarm";
		mockFarmConn = mockXMPPConnection("mockfarm");
		mockVM1Conn = mockXMPPConnection("VM1");
		mockStatic(ServiceDiscoveryManager.class);
		expect(ServiceDiscoveryManager.getInstanceFor(mockXmppConnection))
				.andReturn(mdm);
		expectLastCall().anyTimes();
		Iterator<String> features = new LinkedList<String>().iterator();
		expect(mdm.getFeatures()).andReturn(features).anyTimes();
		mdm.addFeature(isA(String.class));
		// 2 for Farm, 2 for each VM
		expectLastCall().anyTimes();

		// the magic of inserting the mock connections!
		// first call is cfor the LoPFarm
		expectNew(XMPPConnectionWrapper.class,
				isA(ConnectionConfiguration.class)).andReturn(mockFarmConn);
		// next time, return the VM1 connection

	}

	private MockXMPPConnection mockXMPPConnection(String id) throws Exception {
		MockXMPPConnection mocConnection = new MockXMPPConnection(
				new ConnectionConfiguration(server, port), id);
		mocConnection.setDelegate(mockXmppConnection);

		mockRoster = createMock(Roster.class);
		mockRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		expectLastCall().anyTimes();
		mocConnection.setRoster(mockRoster);
		mockRoster.createEntry(mockClient, mockClient, null);
		expectLastCall().anyTimes();
		return mocConnection;

	}

}
