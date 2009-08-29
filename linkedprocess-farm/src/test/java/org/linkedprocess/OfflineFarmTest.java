package org.linkedprocess;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.ArrayList;
import java.util.Iterator;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.linkedprocess.farm.Farm;
import org.linkedprocess.farm.SpawnVm;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.testing.offline.OfflineTest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Farm.class, XmppClient.class,
        ServiceDiscoveryManager.class})
public class OfflineFarmTest extends OfflineTest {

    private static final String FARM = "farm";
    private Farm farm;
    private MockFarmXmppConnection connection;
    private ArrayList<Packet> sentPackets;

    //	@Before
    public void startFarm() throws Exception {

        XMPPConnection farmConn = createMock(XMPPConnection.class);
        connection = new MockFarmXmppConnection(new ConnectionConfiguration(
                server, port), "LoPFarm", farmConn);
        OfflineTest.prepareMocksAndConnection(farmConn, connection);

        sentPackets = connection.sentPackets;
        replayAll();
        // start the farm
        connection.clearPackets();
        farm = new Farm(server, port, username, password, null);
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
        //assertNotNull(result.getVmPassword());
        assertEquals(1, farm.getVms().size());
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
        assertNotNull(result.getVmId());
        assertEquals(1, farm.getVms().size());
        // try to shutdown the VM just created to make shure it exits
        Vm vm = farm.getVms().iterator().next();
		farm.terminateVm(vm.getVmId());
        assertEquals(0, farm.getVms().size());

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
        //assertNotNull(result.getVmPassword());
        //assertNotNull(result.getVmJid());
        assertEquals(1, farm.getVms().size());

        // send one more
        connection.clearPackets();
        connection.receiveSpawn(spawn);
        assertEquals(2, farm.getVms().size());
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
        //assertTrue(result.toXML().contains(LinkedProcess.VM_PASSWORD_ATTRIBUTE));
        assertEquals(1, farm.getVms().size());

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
        assertEquals(7, packetListeners.size());
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
