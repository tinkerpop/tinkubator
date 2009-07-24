package org.linkedprocess.xmpp.farm;

import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jul 24, 2009
 * Time: 10:50:06 AM
 */
public abstract class LopFarmListener extends LopListener {

    public LopFarmListener(XmppFarm xmppFarm) {
        super(xmppFarm);
    }

    public XmppFarm getXmppFarm() {
        return (XmppFarm) this.xmppClient;
    }

}
