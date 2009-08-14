package org.linkedprocess.xmpp.villein.patterns;

import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.xmpp.LopError;

/**
 * User: marko
 * Date: Aug 11, 2009
 * Time: 10:17:19 AM
 */
public class CommandException extends Exception {
    protected LopError lopError;

    public CommandException(LopError lopError) {
        this.lopError = lopError;
    }

    public LopError getLopError() {
        return this.lopError;
    }
}
