package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 2:25:59 PM
 */
public class DestroyListener implements PacketListener {

    private XmppFarm farm;

    public DestroyListener(XmppFarm farm) {
        this.farm = farm;
    }

    public void processPacket(Packet destroy) {

        try {
            XmppFarm.LOGGER.debug("Arrived DestroyListener:");
            XmppFarm.LOGGER.debug(destroy.toXML());

            Destroy returnDestroy = new Destroy();
            returnDestroy.setTo(destroy.getFrom());
            returnDestroy.setPacketID(destroy.getPacketID());
            
            String vmJid = ((Destroy)destroy).getVmJid();
            try {
                farm.destroyVirtualMachine(vmJid);
                returnDestroy.setVmJid(vmJid);
                returnDestroy.setType(IQ.Type.RESULT);
            } catch(ServiceRefusedException e) {
                returnDestroy.setType(IQ.Type.ERROR);
            }

            XmppFarm.LOGGER.debug("Sent DestroyListener:");
            XmppFarm.LOGGER.debug(returnDestroy.toXML());
            farm.getConnection().sendPacket(returnDestroy);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}