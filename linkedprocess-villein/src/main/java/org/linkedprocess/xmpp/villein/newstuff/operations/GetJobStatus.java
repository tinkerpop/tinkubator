package org.linkedprocess.xmpp.villein.newstuff.operations;

import org.linkedprocess.os.Job;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.newstuff.Handler;
import org.linkedprocess.xmpp.XmppClient;
import org.jivesoftware.smack.packet.IQ;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 6, 2009
 * Time: 5:50:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetJobStatus {
    private final HandlerSet<LinkedProcess.JobStatus> resultHandlers;
    private final HandlerSet<Exception> errorHandlers;

    public GetJobStatus() {
        resultHandlers = new HandlerSet<LinkedProcess.JobStatus>();
        errorHandlers = new HandlerSet<Exception>();
    }
    
    public void send(final Job job,
                     final Handler<LinkedProcess.JobStatus> statusHandler,
                     final Handler<Exception> errorHandler) {
        String id = XmppClient.generateRandomJobId();
        resultHandlers.addHandler(id, statusHandler);
        errorHandlers.addHandler(id, errorHandler);

        //...
    }

    public void receiveNormal(final IQ iq) {

    }

    public void receiveError(final IQ iq) {

    }
}
