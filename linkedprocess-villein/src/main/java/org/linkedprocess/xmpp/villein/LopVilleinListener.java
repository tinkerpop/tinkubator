package org.linkedprocess.xmpp.villein;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 12:13:39 PM
 */
public abstract class LopVilleinListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.VILLEIN;

    public LopVilleinListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public XmppVillein getXmppVillein() {
        return (XmppVillein) this.xmppClient;
    }
}
