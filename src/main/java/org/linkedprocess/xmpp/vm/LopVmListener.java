package org.linkedprocess.xmpp.vm;

import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jul 24, 2009
 * Time: 10:54:52 AM
 */
public abstract class LopVmListener extends LopListener {

    public LopVmListener(XmppVirtualMachine xmppVm) {
        super(xmppVm);
    }

    public XmppVirtualMachine getXmppVm() {
        return (XmppVirtualMachine) this.xmppClient;
    }

}
