package org.linkedprocess.xmpp.villein.handlers;

import org.linkedprocess.xmpp.villein.Job;
import org.linkedprocess.xmpp.villein.VmStruct;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 11:33:31 PM
 */
public interface SubmitJobHandler {

    public void handleSubmitJob(VmStruct vmStruct, Job job);

}
