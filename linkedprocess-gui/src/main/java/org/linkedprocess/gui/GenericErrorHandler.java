package org.linkedprocess.gui;

import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.xmpp.villein.Handler;

import javax.swing.*;

/**
 * User: marko
* Date: Aug 7, 2009
* Time: 6:05:59 PM
*/
public class GenericErrorHandler implements Handler<XMPPError> {
    
    public void handle(XMPPError xmppError) {
         JOptionPane.showMessageDialog(null, xmppError.toXML(), "error processing command", JOptionPane.ERROR_MESSAGE);
    }
}
