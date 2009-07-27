package org.linkedprocess.xmpp.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jul 24, 2009
 * Time: 10:50:06 AM
 */
public abstract class LopFarmListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.FARM;

    public LopFarmListener(XmppFarm xmppFarm) {
        super(xmppFarm);
    }

    public XmppFarm getXmppFarm() {
        return (XmppFarm) this.xmppClient;
    }

}
