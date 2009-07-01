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
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.SpawnVm;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.SpawnVmListener;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.Evaluate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
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
	private String mockFarmId;
	private String mockClient;
	private String spawnPacketId = "123";
	private MockXMPPConnection mockFarmConn;
	private XmppFarm xmppFarm;
	private SpawnVm spawn;
	private ServiceDiscoveryManager mdm;
	private String mockVMID = "LopServer/VM1";
	private MockXMPPConnection mockVM1Conn;
	private XMPPConnection mockXmppConnection;

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
		expectNew(XMPPConnectionWrapper.class,
				isA(ConnectionConfiguration.class)).andReturn(mockVM1Conn);

		spawn = new SpawnVm();
		spawn.setVmSpecies("javascript");
		spawn.setPacketID(spawnPacketId);
		spawn.setFrom(mockClient);
		spawn.setType(IQ.Type.GET);
		// spawn.setVmJid("lp1@gmail.com");
	}

	private MockXMPPConnection mockXMPPConnection(String id) throws Exception {
		MockXMPPConnection mocConnection = new MockXMPPConnection(
				new ConnectionConfiguration(server, port), id);
		mocConnection.setDelegate(mockXmppConnection);

		Roster mockRoster = createMock(Roster.class);
		mockRoster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		expectLastCall().anyTimes();
		mocConnection.setRoster(mockRoster);
		return mocConnection;

	}

	@Test
	public void spawningOneVMShouldReturnResultIQPacket() throws Exception {

		// activate all mock objects
		replayAll();

		xmppFarm = new XmppFarm(server, port, username1, password1);

		// one presence packet should have been sent
		assertTrue(mockFarmConn.sentPackets.size() == 1);
		// now we should have 3 PacketListeners to the Farms XMPP connection
		ArrayList<PacketListener> packetListeners = mockFarmConn.packetListeners;
		ArrayList<Packet> sentPackets = mockFarmConn.sentPackets;
		assertTrue(packetListeners.size() == 3);
		// first registered listener should be our SpawnListener
		PacketListener spawnListener = packetListeners.get(0);
		assertTrue(spawnListener instanceof SpawnVmListener);
		// now, one presence packet should have been sent back from the Farm
		assertEquals(1, sentPackets.size());

		// let's send a spawn packet!
		spawnListener.processPacket(spawn);
		// now, a new packet should have been sent back from the VM
		assertEquals(2, sentPackets.size());
		// sent packet should refer to the same pID
		SpawnVm result = (SpawnVm) sentPackets.get(1);
		assertEquals(result.getPacketID(), spawnPacketId);
		assertEquals(result.getType(), IQ.Type.RESULT);
		assertFalse("The returned VM ID should not be the Farms id, right?",
				mockFarmConn.getUser().equals(mockVM1Conn.getUser()));
		// check the whole xml string
		// assertEquals("<iq id=\"" + spawnPacketId + "\" to=\"" + mockClient
		// + "\" type=\"result\"><" + LinkedProcess.SPAWN_VM_TAG
		// + " xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE + "\" "
		// + LinkedProcess.VM_JID_ATTRIBUTE + "=\"" + mockFarmId
		// + "\" /></iq>", result.toXML());
		// now we should have 3 more PacketListeners for the VM
		assertEquals(3, mockVM1Conn.packetListeners.size());

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

		// we should have one more VMConnection coming
		MockXMPPConnection mockXMPPConnection2 = mockXMPPConnection("VM2");
		expectNew(XMPPConnectionWrapper.class,
				isA(ConnectionConfiguration.class)).andReturn(
				mockXMPPConnection2);

		// activate all mock objects
		replayAll();

		// start testing
		xmppFarm = new XmppFarm(server, port, username1, password1);

		PacketListener spawnListener = mockFarmConn.packetListeners.get(0);

		assertEquals(1, mockFarmConn.sentPackets.size());
		// let's send a spawn packet!
		spawnListener.processPacket(spawn);
		//should be ok with one packet back
		assertEquals(2, mockFarmConn.sentPackets.size());

		// let's send one more spawn packet and fire up one more VM!
		spawnListener.processPacket(spawn);
		// // now, a one new packet should have been sent back from the new Farm
		assertEquals(3, mockFarmConn.sentPackets.size());
		// // sent packet should refer to the same pID
		SpawnVm result = (SpawnVm) mockFarmConn.sentPackets.get(2);
		assertEquals(
				"We should only be able to fire up one VM from one client? " +
				"I think it is because of the new random ID, no VM is like the other anymore.",
				IQ.Type.ERROR, result.getType());
		// check the whole xml string
		// assertEquals("<iq id=\"" + spawnPacketId + "\" to=\"" + mockClient
		// + "\" type=\"error\"><" + LinkedProcess.SPAWN_VM_TAG
		// + " xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE + "\" "
		// + "/></iq>", result.toXML());

	}
	//
	 @Test
	 public void spawningAVMAndThenSendingAnEvaluatePacket() throws Exception
	 {
	 // activate all mock objects
	 replayAll();
	
	 // start testing
	 xmppFarm = new XmppFarm(server, port, username1, password1);
	
	 PacketListener spawnListener = mockFarmConn.packetListeners.get(0);
	
	 // let's send a spawn packet!
	 spawnListener.processPacket(spawn);
	 // let's send one more spawn packet and fire up one more VM!
	
	 SpawnVm spawn = (SpawnVm) mockFarmConn.sentPackets.get(2);
	 String VMJiD = spawn.getVmJid();
	 PacketListener evalListener = mockVM1Conn.packetListeners.get(0);
	 Evaluate eval = new Evaluate();
	 eval.setExpression("2+3;");
	 eval.setPacketID("345");
	 eval.setFrom(VMJiD);
	 eval.setTo("blaj");
	 
	 evalListener.processPacket(eval);
	 //sleep a bit for the other end to evaluate
	 Thread.sleep(2000);
	 // now, a some new packets should have been sent back from the new VM
	 assertEquals("We should have gotten a packet back!", 2, mockVM1Conn.sentPackets
	 .size());
	 // sent packet should refer to the same pID
	 Evaluate result = (Evaluate) mockVM1Conn.sentPackets.get(2);
	 System.out.println(result.toXML());
	 assertEquals("we should get the same ID back?",eval.getPacketID(), result.getPacketID() );
	 assertEquals(IQ.Type.RESULT, result.getType());
	 
	//
	// // we should get an error back
	// // check the whole xml string
	// assertEquals("<iq id=\"" + eval.getPacketID() + "\" to=\"" + mockClient
	// + "\" type=\"result\"><evaluate>47.0</evaluate></iq>", result
	// .toXML());
	//
	}
}
