package org.linkedprocess.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopPacketListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class FarmPacketListener extends LopPacketListener {

    public FarmPacketListener(Farm farm) {
        super(farm);
    }

    public Farm getFarm() {
        return (Farm) this.lopClient;
    }

}
