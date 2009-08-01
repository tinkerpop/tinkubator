package org.linkedprocess.xmpp.countryside;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 12:17:35 PM
 */
public abstract class LopCountrysideListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.COUNTRYSIDE;

    public LopCountrysideListener(XmppCountryside xmppCountryside) {
        super(xmppCountryside);
    }

    public XmppCountryside getXmppCountryside() {
        return (XmppCountryside) this.xmppClient;
    }

}
