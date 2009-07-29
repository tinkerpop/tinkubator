package org.linkedprocess.xmpp.farm;

import org.linkedprocess.xmpp.LopIq;

/**
 * User: marko
 * Date: Jun 30, 2009
 * Time: 10:02:49 AM
 */
public abstract class FarmIq extends LopIq {

    protected String vmJid;

    public void setVmJid(String vmJid) {
        this.vmJid = vmJid;
    }

    public String getVmJid() {
        return this.vmJid;
    }
}
