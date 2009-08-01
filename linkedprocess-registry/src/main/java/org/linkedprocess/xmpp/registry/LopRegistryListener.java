package org.linkedprocess.xmpp.registry;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 12:17:35 PM
 */
public abstract class LopRegistryListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.REGISTRY;

    public LopRegistryListener(XmppRegistry xmppRegistry) {
        super(xmppRegistry);
    }

    public XmppRegistry getXmppCountryside() {
        return (XmppRegistry) this.xmppClient;
    }

}
