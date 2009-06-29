package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import gov.lanl.cnls.linkedprocess.os.ServiceRefusedException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:49 AM
 */
public class SpawnListener implements PacketListener {

    private XmppFarm farm;

    public SpawnListener(XmppFarm farm) {
        this.farm = farm;
    }

    public void processPacket(Packet spawn) {

        try {
            XmppFarm.LOGGER.info("Arrived SpawnListener:");
            XmppFarm.LOGGER.info(spawn.toXML());

            Spawn returnSpawn = new Spawn();
            returnSpawn.setTo(spawn.getFrom());
            returnSpawn.setPacketID(spawn.getPacketID());
            String vmJid;

            try {
                vmJid = farm.spawnVirtualMachine();
                returnSpawn.setVmJid(vmJid);
                returnSpawn.setType(IQ.Type.RESULT);
            } catch (ServiceRefusedException e) {
                returnSpawn.setType(IQ.Type.ERROR);
            }

            XmppFarm.LOGGER.info("Sent SpawnListener:");
            XmppFarm.LOGGER.info(returnSpawn.toXML());
            farm.getConnection().sendPacket(returnSpawn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

