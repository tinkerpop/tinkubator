package org.linkedprocess.xmpp.villein.patterns;

import org.jivesoftware.smack.packet.XMPPError;

/**
 * User: marko
 * Date: Aug 11, 2009
 * Time: 10:17:19 AM
 */
public class OperationException extends Exception {
    protected XMPPError xmppError;

    public OperationException(XMPPError xmppError) {
        this.xmppError = xmppError;
    }

    public XMPPError getXMPPError() {
        return this.xmppError;
    }
}
