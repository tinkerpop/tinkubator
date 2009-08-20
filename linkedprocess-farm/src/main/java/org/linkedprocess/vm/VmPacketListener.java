package org.linkedprocess.vm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class VmPacketListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.VM;

    public VmPacketListener(LopVm lopVm) {
        super(lopVm);
    }

    public LopVm getLopVm() {
        return (LopVm) this.lopClient;
    }

}
