package org.linkedprocess.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopPacketListener;

/**
 * User: marko
 * Date: Jul 24, 2009
 * Time: 10:50:06 AM
 */
public abstract class FarmPacketListener extends LopPacketListener {

    public FarmPacketListener(Farm farm) {
        super(farm);
    }

    public Farm getFarm() {
        return (Farm) this.lopClient;
    }

}
