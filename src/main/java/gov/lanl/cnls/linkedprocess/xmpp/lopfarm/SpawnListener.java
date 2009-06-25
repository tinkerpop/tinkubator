package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:49 AM
 */
public class SpawnListener implements PacketListener {
    
    private XMPPConnection connection;

    public SpawnListener(XMPPConnection connection) {
        this.connection = connection;
    }

    public void processPacket(Packet spawn) {

        try {
            XmppFarm.LOGGER.debug("Arrived SpawnListener:");
            XmppFarm.LOGGER.debug(spawn.toXML());

            Spawn returnSpawn = new Spawn();
            returnSpawn.setTo(spawn.getFrom());
            if (spawn.getPacketID() != null) {
                returnSpawn.setPacketID(spawn.getPacketID());
            }
            returnSpawn.setType(IQ.Type.RESULT);

            XmppFarm.LOGGER.debug("Sent SpawnListener:");
            XmppFarm.LOGGER.debug(returnSpawn.toXML());
            connection.sendPacket(returnSpawn);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

