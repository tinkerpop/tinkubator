package org.linkedprocess.gui;

import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.LopError;

import javax.swing.*;

/**
 * User: marko
* Date: Aug 7, 2009
* Time: 6:05:59 PM
*/
public class GenericErrorHandler implements Handler<LopError> {
    
    public void handle(LopError lopError) {
        if(lopError.getMessage() != null)
            JOptionPane.showMessageDialog(null, lopError.getLopErrorType().toString() + "\n" + lopError.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(null, lopError.getLopErrorType().toString(), "error", JOptionPane.ERROR_MESSAGE);
    }
}
