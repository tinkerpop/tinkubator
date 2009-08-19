package org.linkedprocess.xmpp.vm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class LopVmListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.VM;

    public LopVmListener(XmppVm xmppVm) {
        super(xmppVm);
    }

    public XmppVm getXmppVm() {
        return (XmppVm) this.xmppClient;
    }

}
