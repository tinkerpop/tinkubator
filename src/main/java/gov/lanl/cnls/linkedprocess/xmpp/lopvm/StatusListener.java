package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:54:11 PM
 */
public class StatusListener implements PacketListener {
     private XMPPConnection connection;

    public StatusListener(XMPPConnection connection) {
        this.connection = connection;
    }

    public void processPacket(Packet status) {

        try {
            XmppVirtualMachine.LOGGER.debug("Arrived StatusListener:");
            XmppVirtualMachine.LOGGER.debug(status.toXML());

            Status returnStatus = new Status();
            returnStatus.setTo(status.getFrom());
            if (status.getPacketID() != null) {
                returnStatus.setPacketID(status.getPacketID());
            }
            returnStatus.setType(IQ.Type.RESULT);

            XmppVirtualMachine.LOGGER.debug("Sent StatusListener:");
            XmppVirtualMachine.LOGGER.debug(returnStatus.toXML());
            connection.sendPacket(returnStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
