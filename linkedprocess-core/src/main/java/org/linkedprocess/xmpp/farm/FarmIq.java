package org.linkedprocess.xmpp.farm;

import org.linkedprocess.xmpp.LopIq;

/**
 * User: marko
 * Date: Jun 30, 2009
 * Time: 10:02:49 AM
 */
public abstract class FarmIq extends LopIq {

    protected String farmPassword;

    public void setFarmPassword(String farmPassword) {
        this.farmPassword = farmPassword;
    }

    public String getFarmPassword() {
        return this.farmPassword;
    }
}
