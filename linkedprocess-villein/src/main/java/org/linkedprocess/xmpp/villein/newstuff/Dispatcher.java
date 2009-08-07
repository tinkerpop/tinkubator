package org.linkedprocess.xmpp.villein.newstuff;

import org.linkedprocess.xmpp.villein.newstuff.operations.GetJobStatus;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 6, 2009
 * Time: 5:37:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Dispatcher {
    private final GetJobStatus getJobStatus;

    public Dispatcher() {
        getJobStatus = new GetJobStatus();
    }

    public GetJobStatus getGetJobStatus() {
        return getJobStatus;
    }
}
