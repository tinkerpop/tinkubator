package org.linkedprocess.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 23, 2009
 * Time: 11:50:55 AM
 */
public abstract class LopListener implements PacketListener {
    public XmppClient xmppClient;

    public LopListener(XmppClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    protected void sendErrorPacket(ErrorIq.ClientType clientType, String fromJid, String toJid, String packetId, XMPPError.Type xmppErrorType, XMPPError.Condition condition, LinkedProcess.LopErrorType lopErrorType, String errorMessage) {
        ErrorIq errorIq = new ErrorIq();
        errorIq.setFrom(fromJid);
        errorIq.setTo(toJid);
        errorIq.setPacketID(packetId);
        errorIq.setXmppErrorType(xmppErrorType);
        errorIq.setLopErrorType(lopErrorType);
        errorIq.setErrorMessage(errorMessage);
        errorIq.setCondition(condition);
        errorIq.setClientType(clientType);

        XmppClient.LOGGER.info("Sent " + LopListener.class.getName());
        XmppClient.LOGGER.info(errorIq.toXML());
        this.xmppClient.getConnection().sendPacket(errorIq);
    }
}
