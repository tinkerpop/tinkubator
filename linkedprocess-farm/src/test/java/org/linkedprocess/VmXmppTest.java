package org.linkedprocess;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.expectNew;

public class VmXmppTest extends XMPPSpecificationTest {
	//
	// @Test
	// public void checkingStatusOnNonExistingJobShouldReturnError() throws
	// Exception {
	// expectNew(XMPPConnectionWrapper.class,
	// isA(ConnectionConfiguration.class)).andReturn(mockVM1Conn);
	//
	// // activate all mock objects
	// replayAll();
	//
	// // start the farm
	// xmppFarm = new XmppFarm(server, port, username, password);
	// SpawnVm spawn = createSpawnPacket();
	//
	// // let's send a spawn packet to the SpwnVMListener!
	// mockFarmConn.packetListeners.get(0).processPacket(spawn);
	// // get the password
	// SpawnVm vmAcc = (SpawnVm) mockFarmConn.sentPackets
	// .get(mockFarmConn.sentPackets.size() - 1);
	// String vmPassword = vmAcc.getVmPassword();
	// String vmJid = vmAcc.getVmJid();
	//
	// JobStatus status = new JobStatus();
	// status.setVmPassword(vmPassword);
	// status.setTo("dummy");
	// status.setPacketID(spawnPacketId);
	//
	// //check without job id
	// mockVM1Conn.clearPackets();
	// mockVM1Conn.packetListeners.get(1).processPacket(status);
	// waitForResponse(mockFarmConn.sentPackets, 1000);
	// // now, a new packet should have been sent back from the VM
	// assertEquals(1, mockVM1Conn.sentPackets.size());
	// // sent packet should refer to the same pID
	// JobStatus result = (JobStatus) mockVM1Conn.sentPackets.get(0);
	// assertEquals(spawnPacketId, result.getPacketID());
	// assertEquals(IQ.Type.ERROR, result.getType());
	// //assertEquals(LinkedProcess.LopErrorType.MALFORMED_PACKET,
	// result.getErrorType());
	//
	// //non-existent job
	// status.setJobId("test");
	// mockVM1Conn.clearPackets();
	// mockVM1Conn.packetListeners.get(1).processPacket(status);
	// waitForResponse(mockFarmConn.sentPackets, 1000);
	// // now, a new packet should have been sent back from the VM
	// assertEquals(1, mockVM1Conn.sentPackets.size());
	// // sent packet should refer to the same pID
	// result = (JobStatus) mockVM1Conn.sentPackets.get(0);
	// assertEquals(spawnPacketId, result.getPacketID());
	// assertEquals(IQ.Type.ERROR, result.getType());
	// //assertEquals(LopErrorType.JOB_NOT_FOUND, result.getErrorType());
	//
	// }
	//	
	//	
	//
	// @Test
	// public void
	// sendingAnEvalPacketWithoutVMPasswordShouldReturnErrorAndWithPasswordAResult()
	// throws Exception {
	// expectNew(XMPPConnectionWrapper.class,
	// isA(ConnectionConfiguration.class)).andReturn(mockVM1Conn);
	//
	// // activate all mock objects
	// replayAll();
	//
	// // start the farm
	// xmppFarm = new XmppFarm(server, port, username, password);
	// SpawnVm spawn = createSpawnPacket();
	//
	// // let's send a spawn packet to the SpwnVMListener!
	// mockFarmConn.packetListeners.get(0).processPacket(spawn);
	// // get the password
	// SpawnVm vmAcc = (SpawnVm) mockFarmConn.sentPackets
	// .get(mockFarmConn.sentPackets.size() - 1);
	// String vmPassword = vmAcc.getVmPassword();
	// String vmJid = vmAcc.getVmJid();
	//
	// // send the eval packet, password missing
	// SubmitJob eval = new SubmitJob();
	// eval.setPacketID(spawnPacketId);
	// eval.setExpression("20 + 52;");
	// eval.setTo(vmJid);
	// eval.setFrom(CLIENT_JID);
	// mockVM1Conn.clearPackets();
	// // ArrayList<Packet> vmsentPackets = mockVM1Conn.sentPackets;
	// mockVM1Conn.packetListeners.get(0).processPacket(eval);
	// waitForResponse(mockFarmConn.sentPackets, 1000);
	// // now, a new packet should have been sent back from the VM
	// assertEquals(1, mockVM1Conn.sentPackets.size());
	// // sent packet should refer to the same pID
	// SubmitJob result = (SubmitJob) mockVM1Conn.sentPackets.get(0);
	// assertEquals(result.getPacketID(), spawnPacketId);
	// assertEquals(IQ.Type.ERROR, result.getType());
	// //assertEquals(LinkedProcess.LopErrorType.MALFORMED_PACKET,
	// result.getErrorType());
	//
	// // now, try with a wrong password
	// eval.setVmPassword("wrong");
	// mockVM1Conn.clearPackets();
	// assertEquals(0, mockVM1Conn.sentPackets.size());
	// mockVM1Conn.packetListeners.get(0).processPacket(eval);
	// waitForResponse(mockVM1Conn.sentPackets, 1500);
	// // now, a new packet should have been sent back from the VM
	// assertEquals(1, mockVM1Conn.sentPackets.size());
	// // sent packet should refer to the same pID
	// result = (SubmitJob) mockVM1Conn.sentPackets.get(0);
	// assertEquals(result.getPacketID(), spawnPacketId);
	// assertEquals(IQ.Type.ERROR, result.getType());
	// //assertEquals(LopErrorType.WRONG_VM_PASSWORD, result.getErrorType());
	//
	// // now, try with a valid password
	// eval.setVmPassword(vmPassword);
	// mockVM1Conn.clearPackets();
	// assertEquals(0, mockVM1Conn.sentPackets.size());
	// mockVM1Conn.packetListeners.get(0).processPacket(eval);
	// waitForResponse(mockVM1Conn.sentPackets, 1500);
	// // now, a new packet should have been sent back from the VM
	// assertEquals(1, mockVM1Conn.sentPackets.size());
	// // sent packet should refer to the same pID
	// result = (SubmitJob) mockVM1Conn.sentPackets.get(0);
	// assertEquals(result.getPacketID(), spawnPacketId);
	// assertEquals(IQ.Type.RESULT, result.getType());
	// assertEquals("72", result.getExpression());
	//
	// // now, try with a wrong evaluation
	// eval.setExpression("buh+2sdf;==");
	// mockVM1Conn.clearPackets();
	// assertEquals(0, mockVM1Conn.sentPackets.size());
	// mockVM1Conn.packetListeners.get(0).processPacket(eval);
	// waitForResponse(mockVM1Conn.sentPackets, 1500);
	// // now, a new packet should have been sent back from the VM
	// assertEquals(1, mockVM1Conn.sentPackets.size());
	// // sent packet should refer to the same pID
	// result = (SubmitJob) mockVM1Conn.sentPackets.get(0);
	// assertEquals(result.getPacketID(), spawnPacketId);
	// assertEquals(IQ.Type.ERROR, result.getType());
	// //assertEquals(LopErrorType.EVALUATION_ERROR, result.getErrorType());
	//
	// // shut down the VM
	// mockVM1Conn.clearPackets();
	// TerminateVm terminate = new TerminateVm();
	// terminate.setVmPassword(vmPassword);
	// terminate.setTo(vmJid);
	// mockVM1Conn.packetListeners.get(3).processPacket(terminate);
	//
	// ArrayList<Packet> sentPackets = mockVM1Conn.sentPackets;
	// assertEquals("we should get one result back", 1, sentPackets
	// .size());
	// assertEquals(IQ.Type.RESULT, ((IQ) sentPackets.get(0)).getType());
	// // check right shutdown messages
	// xmppFarm.shutDown();
	// }
	//    
	// @Test
	// public void sendingATerminatePacketShouldCloseTheVM() throws Exception {
	// expectNew(XMPPConnectionWrapper.class,
	// isA(ConnectionConfiguration.class)).andReturn(mockVM1Conn);
	//
	// // activate all mock objects
	// replayAll();
	//
	// // start the farm
	// xmppFarm = new XmppFarm(server, port, username, password);
	// SpawnVm spawn = createSpawnPacket();
	//
	// // let's send a spawn packet to the SpwnVMListener!
	// mockFarmConn.packetListeners.get(0).processPacket(spawn);
	// // get the password
	// SpawnVm vmAcc = (SpawnVm) mockFarmConn.sentPackets
	// .get(mockFarmConn.sentPackets.size() - 1);
	// String vmPassword = vmAcc.getVmPassword();
	// String vmJid = vmAcc.getVmJid();
	//
	// TerminateVm terminate = new TerminateVm();
	// terminate.setVmPassword(vmPassword);
	// terminate.setTo(vmJid);
	// mockVM1Conn.clearPackets();
	// mockVM1Conn.packetListeners.get(3).processPacket(terminate);
	// ArrayList<Packet> sentPackets = mockVM1Conn.sentPackets;
	// System.out.println(sentPackets);
	// assertEquals("we should get back one IQ TERMIANTE RESULT ", 1,
	// sentPackets
	// .size());
	// // first one - IQ
	// TerminateVm result = (TerminateVm) sentPackets.get(0);
	// assertEquals(IQ.Type.RESULT, result.getType());
	// assertEquals(terminate.getPacketID(), result.getPacketID());
	// }
	//
	//   
	//
	// }

//	@Test
//	public void sendingASpawnPacketShouldStartASeparateVM() throws Exception {
//		ArrayList<Packet> sentPackets = connection.sentPackets;
//		connection.clearPackets();
//		SpawnVm spawn = createSpawnPacket();
//		spawn.setType(IQ.Type.GET);
//		// let's send a spawn packet to the SpwnVMListener!
//		connection.packetListeners.get(0).processPacket(spawn);
//		// now, a new packet should have been sent back from the VM
//		assertEquals(1, sentPackets.size());
//		// sent packet should refer to the same pID
//		SpawnVm result = (SpawnVm) sentPackets.get(0);
//		assertEquals(result.getPacketID(), spawnPacketId);
//		assertEquals(result.getType(), IQ.Type.RESULT);
//		assertFalse("The returned VM ID should not be the Farms id, right?",
//				connection.getUser().equals(mockVM1Conn.getUser()));
//		// now we should have 3 PacketListeners for the VM
//		ArrayList<PacketListener> vm1packetListeners = mockVM1Conn.packetListeners;
//		assertEquals(5, vm1packetListeners.size());
//		assertTrue(vm1packetListeners.get(0) instanceof SubmitJobListener);
//		assertTrue(vm1packetListeners.get(1) instanceof JobStatusListener);
//		assertTrue(vm1packetListeners.get(2) instanceof AbortJobListener);
//		assertTrue(vm1packetListeners.get(3) instanceof TerminateVmListener);
//		assertTrue(vm1packetListeners.get(4) instanceof ManageBindingsListener);
//
//		// try one more VM with the same spawn packet as the first!
//		connection.clearPackets();
//		connection.packetListeners.get(0).processPacket(spawn);
//		assertEquals(1, sentPackets.size());
//		result = (SpawnVm) sentPackets.get(0);
//		assertEquals(result.getPacketID(), spawnPacketId);
//		assertEquals(
//				"We should not be able to spwn the same type of VM twice!",
//				result.getType(), IQ.Type.RESULT);
//		mockVM1Conn.clearPackets();
//		connection.clearPackets();
//		farm.shutDown();
//		// sentPacketsVM1 = mockVM1Conn.sentPackets;
//		System.out.println(sentPackets);
//		assertEquals("We were expecting one UNAVAILABE", 1, sentPackets.size());
//		assertEquals(Presence.Type.unavailable, ((Presence) sentPackets.get(0))
//				.getType());
//		// VM is not longer sending an unavailable ...
//		// assertEquals(1, sentPacketsVM1.size());
//		// assertEquals(Presence.Type.unavailable, ((Presence) sentPacketsVM1
//		// .get(0)).getType());
//	}
}
