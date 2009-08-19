package org.linkedprocess;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DataForm;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;
import org.linkedprocess.testing.offline.MockXmppConnection;
import org.linkedprocess.testing.offline.OfflineTest;
import org.linkedprocess.XmppClient;
import org.linkedprocess.vm.XmppVm;
import org.linkedprocess.farm.SpawnVm;
import org.linkedprocess.farm.XmppFarm;
import static org.powermock.api.easymock.PowerMock.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Iterator;

@RunWith(PowerMockRunner.class)
@PrepareForTest({XmppFarm.class, XmppClient.class,
        ServiceDiscoveryManager.class})
public class OfflineFarmTest extends OfflineTest {

    private static final String FARM = "farm";
    private XmppFarm farm;
    private MockFarmXmppConnection connection;
    private ArrayList<Packet> sentPackets;

    //	@Before
    public void startFarm() throws Exception {

        XMPPConnection farmConn = createMock(XMPPConnection.class);
        connection = new MockFarmXmppConnection(new ConnectionConfiguration(
                server, port), "LoPFarm", farmConn);
        OfflineTest.prepareMocksAndConnection(farmConn, connection);

        sentPackets = connection.sentPackets;
        // first VM
        expectNew(XmppVm.class, isA(String.class),
                isA(Integer.class), isA(String.class), isA(String.class),
                isA(XmppFarm.class), isA(String.class), isA(String.class),
                isA(String.class))
                .andReturn(createMockVM(username + "LoPVM/1")).times(0, 1);
        expectNew(XmppVm.class, isA(String.class),
                isA(Integer.class), isA(String.class), isA(String.class),
                isA(XmppFarm.class), isA(String.class), isA(String.class),
                isA(String.class))
                .andReturn(createMockVM(username + "LoPVM/2")).times(0, 1);
        replayAll();
        // start the farm
        connection.clearPackets();
        farm = new XmppFarm(server, port, username, password, null);
    }

    private XmppVm createMockVM(String id) {
        XmppVm mockVM = createMock(XmppVm.class);

        expect(mockVM.getFullJid()).andReturn(id).anyTimes();
        mockVM.shutdown();
        expectLastCall().anyTimes();
        try {
            mockVM.terminateSelf();
        } catch (VmWorkerNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        expectLastCall().anyTimes();
        MockXmppConnection vmConnection = new MockXmppConnection(
                new ConnectionConfiguration(server, port), "vm1", null);
        expect(mockVM.getConnection()).andReturn(vmConnection).anyTimes();
        expect(mockVM.getVmPassword()).andReturn(password).anyTimes();
        return mockVM;
    }

    private SpawnVm createSpawnPacket() {
        SpawnVm spawn = new SpawnVm();
        spawn.setVmSpecies(JAVASCRIPT);
        spawn.setPacketID(IQ_PACKET_ID);
        spawn.setFrom(CLIENT_JID);
        spawn.setType(IQ.Type.GET);
        spawn.setFarmPassword(PASSWORD);

        return spawn;
    }

    @Test
    public void tryingToSpawnAVMFromFarmWithoutPWDShouldIgnoreFarmPwdTag()
            throws Exception {
        startFarm();

        // omitting the VM Password tag
        SpawnVm spawn = new SpawnVm();
        spawn.setVmSpecies(JAVASCRIPT);
        spawn.setPacketID(IQ_PACKET_ID);
        spawn.setFrom(CLIENT_JID);
        spawn.setType(IQ.Type.GET);
        connection.clearPackets();
        connection.receiveSpawn(spawn);
        assertEquals(1, sentPackets.size());
        SpawnVm result = (SpawnVm) sentPackets.get(0);
        assertEquals(result.getPacketID(), IQ_PACKET_ID);
        assertEquals(IQ.Type.RESULT, result.getType());
        assertNotNull(result.getVmPassword());
        assertEquals(1, farm.getVirtualMachines().size());
    }

    @Test
    public void tryingToSpawnAVMFromFarmWithoutPWDShouldIgnoreFarmPwd()
            throws Exception {
        startFarm();

        // try to add a password
        connection.clearPackets();
        SpawnVm spawn = createSpawnPacket();
        spawn.setFarmPassword("dummy");
        connection.receiveSpawn(spawn);
        assertEquals(1, sentPackets.size());
        SpawnVm result = (SpawnVm) connection.getLastPacket();
        assertEquals(result.getPacketID(), IQ_PACKET_ID);
        assertEquals(IQ.Type.RESULT, result.getType());
        assertNotNull(result.getVmPassword());
        assertNotNull(result.getVmJid());
        assertEquals(1, farm.getVirtualMachines().size());
        // try to shutdown the VM just created to make shure it exists
        farm.terminateVirtualMachine(result.getVmJid());
        assertEquals(0, farm.getVirtualMachines().size());

    }

    @Test
    public void multipleSpawnsShouldStartSeparateVMs() throws Exception {
        startFarm();

        connection.clearPackets();
        SpawnVm spawn = createSpawnPacket();
        connection.receiveSpawn(spawn);
        assertEquals(1, sentPackets.size());
        SpawnVm result = (SpawnVm) connection.getLastPacket();
        assertEquals(result.getPacketID(), IQ_PACKET_ID);
        assertEquals(IQ.Type.RESULT, result.getType());
        assertNotNull(result.getVmPassword());
        assertNotNull(result.getVmJid());
        assertEquals(1, farm.getVirtualMachines().size());

        // send one more
        connection.clearPackets();
        connection.receiveSpawn(spawn);
        assertEquals(2, farm.getVirtualMachines().size());
        result = (SpawnVm) connection.getLastPacket();
        assertEquals(IQ.Type.RESULT, result.getType());

    }

    @Test
    public void startingFarmShouldPopulateMetaInfForDiscInfo() throws Exception {
        startFarm();

        // check LoPFarm for registered features
        assertTrue(connection.getDiscInfoFeatures().contains(
                LinkedProcess.DISCO_INFO_NAMESPACE));
        DataForm firstDataform = connection.dataformCapture.getValue();
        Iterator<FormField> fields = firstDataform.getFields();
        assertTrue(fields.hasNext());
        assertEquals("supported virtual machine species", fields.next()
                .getLabel());

    }

    @Test
    public void tryingToSpawnAVMFromFarmWithPWDShouldCheckPwd()
            throws Exception {
        LinkedProcess.getConfiguration().put(
                LinkedProcess.FARM_PASSWORD_PROPERTY, password);
        startFarm();


        // omitting the VM Password tag
        SpawnVm spawn = new SpawnVm();
        spawn.setVmSpecies(JAVASCRIPT);
        spawn.setPacketID(IQ_PACKET_ID);
        spawn.setFrom(CLIENT_JID);
        spawn.setType(IQ.Type.GET);
        connection.clearPackets();
        connection.receiveSpawn(spawn);

        assertEquals(1, sentPackets.size());
        SpawnVm result = (SpawnVm) sentPackets.get(0);
        assertEquals(IQ.Type.ERROR, result.getType());

        // try to use a wrong password
        connection.clearPackets();
        spawn.setFarmPassword("dummy");
        connection.receiveSpawn(spawn);
        assertEquals(1, sentPackets.size());
        result = (SpawnVm) sentPackets.get(0);
        assertEquals(result.getPacketID(), IQ_PACKET_ID);
        assertTrue(result.toXML().contains(
                LinkedProcess.LopErrorType.WRONG_FARM_PASSWORD.toString()));

        // use right PWD
        connection.clearPackets();
        spawn.setFarmPassword(password);
        connection.receiveSpawn(spawn);
        assertEquals(1, sentPackets.size());
        result = (SpawnVm) connection.getLastPacket();
        assertTrue(result.toXML().contains(LinkedProcess.VM_PASSWORD_ATTRIBUTE));
        assertEquals(1, farm.getVirtualMachines().size());

    }

    @Test
    public void sendingWrongSpawnRequestsShouldReturnErrorPackets()
            throws Exception {
        // make sure farm is not PWD protected
        LinkedProcess.getConfiguration().remove(
                LinkedProcess.FARM_PASSWORD_PROPERTY);

        startFarm();

        // missing VMSpec
        connection.clearPackets();
        SpawnVm spawn = new SpawnVm();
        // spawn.setVmSpecies(JAVASCRIPT);
        spawn.setPacketID(IQ_PACKET_ID);
        spawn.setFrom(CLIENT_JID);
        spawn.setType(IQ.Type.GET);
        spawn.setPacketID(IQ_PACKET_ID);
        connection.receiveSpawn(spawn);

        assertEquals(1, sentPackets.size());
        SpawnVm result = (SpawnVm) connection.getLastPacket();
        assertEquals(IQ_PACKET_ID, result.getPacketID());
        assertEquals(IQ.Type.ERROR, result.getType());
        assertEquals(400, result.getError().getCode());

        // wrong VM_Spec
        spawn = new SpawnVm();
        spawn.setVmSpecies("dummy spec");
        spawn.setPacketID(IQ_PACKET_ID);
        spawn.setFrom(CLIENT_JID);
        spawn.setType(IQ.Type.GET);
        spawn.setPacketID(IQ_PACKET_ID);
        connection.clearPackets();
        connection.receiveSpawn(spawn);

        assertEquals(1, sentPackets.size());
        result = (SpawnVm) connection.getLastPacket();
        assertEquals(IQ_PACKET_ID, result.getPacketID());
        assertEquals(IQ.Type.ERROR, result.getType());
        assertTrue(result.toXML().contains(
                LinkedProcess.LopErrorType.SPECIES_NOT_SUPPORTED.toString()));

    }

    @Test
    public void subscribingToAFarmsRosterShouldResultInTOnePresencePacketsBack()
            throws Exception {
        startFarm();

        assertNotNull(connection.spawn);
        assertNotNull(connection.subscribe);
        Presence subscriptionPacket = new Presence(Presence.Type.subscribe);
        subscriptionPacket.setTo(FARM);
        subscriptionPacket.setFrom(CLIENT_JID);
        connection.clearPackets();
        connection.receiveSubscribe(subscriptionPacket);
        assertEquals(1, sentPackets.size());
        // subscription acc
        Presence subscripbed = (Presence) sentPackets.get(0);
        assertEquals(Presence.Type.subscribed, subscripbed.getType());

    }

    @Test
    public void checkCorrectStartupAndShutdownPacketsAndListeners()
            throws Exception {
        startFarm();
        assertEquals(1, sentPackets.size());
        // Farm started
        Presence presence = (Presence) connection.getLastPacket();
        assertEquals(Presence.Type.available, presence.getType());
        assertEquals(LinkedProcess.HIGHEST_PRIORITY, presence.getPriority());

        // now we should have 2 PacketListeners to the Farms XMPP connection
        ArrayList<PacketListener> packetListeners = connection.packetListeners;
        assertEquals(2, packetListeners.size());
        assertNotNull(connection.spawn);
        assertNotNull(connection.subscribe);

        // now, shut down the farm
        connection.clearPackets();
        farm.shutdown();

        assertEquals(1, sentPackets.size());

        assertEquals(Presence.Type.unavailable, ((Presence) connection
                .getLastPacket()).getType());
    }

    @After
    public void shutdown() {
        verifyAll();
    }

}
