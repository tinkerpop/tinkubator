package org.linkedprocess.xmpp.villein.handlers;

import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.villein.VmStruct;

/**
 * User: marko
 * Date: Aug 5, 2009
 * Time: 12:07:48 AM
 */
public interface ManageBindingsHandler {

    public void handleManageBindingsResult(VmStruct vmStruct, VMBindings vmBindings);
}
