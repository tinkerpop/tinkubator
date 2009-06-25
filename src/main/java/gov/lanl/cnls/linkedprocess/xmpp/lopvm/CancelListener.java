package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:45 PM
 */
public class CancelListener implements PacketListener {
     private XMPPConnection connection;

    public CancelListener(XMPPConnection connection) {
        this.connection = connection;
    }

    public void processPacket(Packet cancel) {

        try {
            XmppVirtualMachine.LOGGER.debug("Arrived CancelListener:");
            XmppVirtualMachine.LOGGER.debug(cancel.toXML());

            Cancel returnCancel = new Cancel();
            returnCancel.setTo(cancel.getFrom());
            if (cancel.getPacketID() != null) {
                returnCancel.setPacketID(cancel.getPacketID());
            }
            returnCancel.setType(IQ.Type.RESULT);

            XmppVirtualMachine.LOGGER.debug("Sent CancelListener:");
            XmppVirtualMachine.LOGGER.debug(returnCancel.toXML());
            connection.sendPacket(returnCancel);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}