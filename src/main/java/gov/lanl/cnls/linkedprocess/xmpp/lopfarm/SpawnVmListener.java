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
public class SpawnVmListener implements PacketListener {

    private XmppFarm farm;

    public SpawnVmListener(XmppFarm farm) {
        this.farm = farm;
    }

    public void processPacket(Packet spawnVm) {

        try {
            XmppFarm.LOGGER.info("Arrived SpawnListener:");
            XmppFarm.LOGGER.info(spawnVm.toXML());

            SpawnVm returnSpawnVm = new SpawnVm();
            returnSpawnVm.setTo(spawnVm.getFrom());
            returnSpawnVm.setPacketID(spawnVm.getPacketID());
            String vmJid;

            try {
                vmJid = farm.spawnVirtualMachine(((SpawnVm)spawnVm).getVmSpecies());
                returnSpawnVm.setVmJid(vmJid);
                returnSpawnVm.setType(IQ.Type.RESULT);
            } catch (ServiceRefusedException e) {
                returnSpawnVm.setErrorMessage(e.getMessage());
                returnSpawnVm.setType(IQ.Type.ERROR);
            }

            XmppFarm.LOGGER.info("Sent SpawnListener:");
            XmppFarm.LOGGER.info(returnSpawnVm.toXML());
            farm.getConnection().sendPacket(returnSpawnVm);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

