package gov.lanl.cnls.linkedprocess;

import static org.easymock.EasyMock.capture;
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
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.SpawnVm;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.SpawnVmListener;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.Evaluate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.easymock.Capture;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
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
public class MockingTest {

	private static String username1 = "linked.process.4@gmail.com";
	private static String password1 = "linked45";

	private static String server = "talk1.l.google.com";
	private static int port = 5222;
	private List<Packet> sentPackets;
	private List<PacketListener> packetListeners;
	private String mockFarmId;
	private String mockClient;
	private String spawnPacketId = "123";
	private XMPPConnection mockFarmConn;
	private XmppFarm xmppFarm;
	private SpawnVm spawn;
	private ServiceDiscoveryManager mdm;
	private String mockVMID = "LopServer/VM1";
	private XMPPConnection mockVM1Conn;

	@Before
	public void setup() throws Exception {
		mdm = createMock(ServiceDiscoveryManager.class);
		mockFarmId = "LopServer/LoPFarm";
		mockFarmConn = mockXMPPConnection(mockFarmId);
		mockVM1Conn = mockXMPPConnection(mockVMID );
		expect(ServiceDiscoveryManager.getInstanceFor(mockFarmConn))
		.andReturn(mdm);
		expect(ServiceDiscoveryManager.getInstanceFor(mockVM1Conn))
		.andReturn(mdm);

		Iterator<String> features = new LinkedList<String>().iterator();
		expect(mdm.getFeatures()).andReturn(features).anyTimes();
		mdm.addFeature(isA(String.class));
		// 2 for Farm, 2 for each VM
		expectLastCall().anyTimes();
		// a bit more explicit matchers to help PowerMock choose the right
		// constructor for the farm
		expectNew(XMPPConnection.class,
				new Class<?>[] { ConnectionConfiguration.class },
				isA(ConnectionConfiguration.class)).andReturn(mockFarmConn);
		//next time, return the VM connection
		expectNew(XMPPConnection.class,
				new Class<?>[] { ConnectionConfiguration.class },
				isA(ConnectionConfiguration.class)).andReturn(mockVM1Conn);


		spawn = new SpawnVm();
		spawn.setVmSpecies("javascript");
		spawn.setPacketID(spawnPacketId);
		spawn.setFrom(mockClient);
        //spawn.setVmJid("lp1@gmail.com");
	}

	private XMPPConnection mockXMPPConnection(String username) throws Exception {
		//XMPPConnection mocConnection = new MockXMPPConnection(new ConnectionConfiguration(server, port, "hej"));
		XMPPConnection mocConnection = createMock(XMPPConnection.class);
		mockStatic(ServiceDiscoveryManager.class);
		mocConnection.connect();
		expectLastCall().anyTimes();
		Capture<String> resourceCapture = new Capture<String>();
		mocConnection.login(isA(String.class), isA(String.class),
				capture(resourceCapture) );
		expectLastCall().anyTimes();
		expect(mocConnection.getHost()).andReturn("mock.linkedprocess.org")
		.anyTimes();
		expect(mocConnection.getUser()).andReturn(username+LinkedProcess.FORWARD_SLASH + (resourceCapture.hasCaptured()?resourceCapture.getValue():"nothingyet")).anyTimes();
		expect(mocConnection.isAnonymous()).andReturn(false).anyTimes();
		expect(mocConnection.isConnected()).andReturn(true).anyTimes();
		expect(mocConnection.isSecureConnection()).andReturn(false).anyTimes();
		expect(mocConnection.isUsingCompression()).andReturn(false).anyTimes();
		expect(mocConnection.isUsingTLS()).andReturn(false).anyTimes();
		packetListeners = new LinkedList<PacketListener>();
		mocConnection.addPacketListener(MultiCaptureMatcher
				.multiCapture(packetListeners), isA(PacketFilter.class));
		expectLastCall().anyTimes();
		Roster mockRoster = createMock(Roster.class);
		mockRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		expect(mocConnection.getRoster()).andReturn(mockRoster).anyTimes();
		sentPackets = new ArrayList<Packet>();
		mocConnection
		.sendPacket(MultiCaptureMatcher.multiCapture(sentPackets));
		expectLastCall().anyTimes();
		// shutdown
		mocConnection.disconnect(isA(Presence.class));
		mocConnection.disconnect();
		return mocConnection;
		
	}

	@Test
	public void spawningOneVMShouldReturnResultIQPacket() throws Exception {

		// activate all mock objects
		replayAll();

		xmppFarm = new XmppFarm(server, port, username1, password1);

		// one presence packet should have been sent
		assertTrue(sentPackets.size() == 1);
		// now we should have 3 PacketListeners to the XMPP connection
		assertTrue(packetListeners.size() == 3);
		// first registered listener should be our SpawnListener
		PacketListener spawnListener = packetListeners.get(0);
		assertTrue(spawnListener instanceof SpawnVmListener);

		// let's send a spawn packet!
		spawnListener.processPacket(spawn);

		// now, a new packet should have been sent back from the VM
		assertEquals(3, sentPackets.size());
		// sent packet should refer to the same pID
		SpawnVm result = (SpawnVm) sentPackets.get(2);
		assertEquals(result.getPacketID(), spawnPacketId);
		assertEquals(result.getType(), IQ.Type.RESULT);
		assertFalse("The returned VM ID should not be the Farms id, right?",
				result.getVmJid().equals(mockFarmId));
		// check the whole xml string
		assertEquals("<iq id=\"" + spawnPacketId + "\" to=\"" + mockClient
				+ "\" type=\"result\"><" + LinkedProcess.SPAWN_VM_TAG
				+ " xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE + "\" "
				+ LinkedProcess.VM_JID_ATTRIBUTE + "=\"" + mockFarmId
				+ "\" /></iq>", result.toXML());
		// now we should have 3 more PacketListeners for the VM
		assertTrue(packetListeners.size() == 6);

	}

	@After
	public void shutdown() {
		// shut down
		xmppFarm.shutDown();
		verifyAll();

	}

	@Test
	public void spawningTwoVMInSameFarmShouldReturnErrorSpawnPacket()
			throws Exception {
		Packet spawnPacket2 = createMock(Packet.class);
		String spawnXML = "<not real XML>";
		expect(spawnPacket2.toXML()).andReturn(spawnXML).anyTimes();
		expect(spawnPacket2.getFrom()).andReturn(mockClient).anyTimes();
		String spawnPacket2Id = "345";
		expect(spawnPacket2.getPacketID()).andReturn(spawnPacket2Id).anyTimes();

		// the second VM should disconnect
		// mockConnection.disconnect(isA(Presence.class));
		// mockConnection.disconnect();

		// activate all mock objects
		replayAll();

		// start testing
		xmppFarm = new XmppFarm(server, port, username1, password1);

		PacketListener spawnListener = packetListeners.get(0);

		// let's send a spawn packet!
		spawnListener.processPacket(spawn);
		// let's send one more spawn packet and fire up one more VM!
		spawnListener.processPacket(spawnPacket2);
		// now, a some new packets should have been sent back from the new VM
		assertEquals(5, sentPackets.size());
		// sent packet should refer to the same pID
		IQ result = (IQ) sentPackets.get(4);
		assertEquals(result.getPacketID(), spawnPacket2Id);

		// we should get an error back, or should we?
		assertEquals(IQ.Type.ERROR, result.getType());
		// check the whole xml string
		assertEquals("<iq id=\"" + spawnPacket2Id + "\" to=\"" + mockClient
				+ "\" type=\"error\"><" + LinkedProcess.SPAWN_VM_TAG
				+ " xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE + "\" "
				+ "/></iq>", result.toXML());

	}

	@Test
	public void spawningAVMAndThenSendingAnEvaluatePacket() throws Exception {
		Evaluate eval = new Evaluate();
		eval.setExpression("for(int i=0; i<10; i++) { i; };");
		eval.setPacketID("345");
		eval.setFrom(mockClient);

		// activate all mock objects
		replayAll();

		// start testing
		xmppFarm = new XmppFarm(server, port, username1, password1);

		PacketListener spawnListener = packetListeners.get(0);

		// let's send a spawn packet!
		spawnListener.processPacket(spawn);
		// let's send one more spawn packet and fire up one more VM!

		PacketListener evalListener = packetListeners.get(3);
		evalListener.processPacket(eval);
		// now, a some new packets should have been sent back from the new VM
		assertEquals("We should have gotten a packet back!", 4, sentPackets
				.size());
		// sent packet should refer to the same pID
		IQ result = (IQ) sentPackets.get(3);
		assertEquals(result.getPacketID(), eval.getPacketID());

		// we should get an error back
		assertEquals(IQ.Type.RESULT, result.getType());
		// check the whole xml string
		assertEquals("<iq id=\"" + eval.getPacketID() + "\" to=\"" + mockClient
				+ "\" type=\"result\"><evaluate>47.0</evaluate></iq>", result
				.toXML());

	}
}
