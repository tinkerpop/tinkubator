package org.linkedprocess.vm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class LopVmListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.VM;

    public LopVmListener(LopVm lopVm) {
        super(lopVm);
    }

    public LopVm getXmppVm() {
        return (LopVm) this.lopClient;
    }

}
