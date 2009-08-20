package org.linkedprocess.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopListener;

/**
 * User: marko
 * Date: Jul 24, 2009
 * Time: 10:50:06 AM
 */
public abstract class FarmPacketListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.FARM;

    public FarmPacketListener(LopFarm lopFarm) {
        super(lopFarm);
    }

    public LopFarm getLopFarm() {
        return (LopFarm) this.lopClient;
    }

}
