package org.linkedprocess.xmpp.villein.handlers;

import org.linkedprocess.xmpp.villein.VmStruct;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 11:48:42 PM
 */
public interface AbortJobHandler {

    public void handleAbortJobResult(VmStruct vmStruct, String jobId);
    //public void handleAbortJobError(VmStruct vmStruct, String jobId, XMPPError error);
}
