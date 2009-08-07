package org.linkedprocess.xmpp.villein.newstuff;

import org.linkedprocess.os.Job;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.LinkedProcess;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 6, 2009
 * Time: 4:04:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class VirtualMachineProxy {
    private final Dispatcher dispatcher;
    private final Map<String, Handler<JobResult>> handlersByJobId;

    public VirtualMachineProxy(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        handlersByJobId = new HashMap<String, Handler<JobResult>>();
    }

    public void getJobStatus(final Job job,
                             final Handler<LinkedProcess.JobStatus> statusHandler,
                             final Handler<Exception> errorHandler) {
        dispatcher.getGetJobStatus().send(job, statusHandler, errorHandler);
    }

    public void scheduleJob(final Job job,
                            final Handler<JobResult> completedJobHandler,
                            final Handler<Exception> errorHandler) {

    }

    public void abortJob(final Job job,
                         final Handler<JobResult> abortedJobHandler,
                         final Handler<Exception> errorHandler) {

    }

    public void getBindings(final Set<String> bindingNames,
                            final Handler<VMBindings> bindingsHandler,
                            final Handler<Exception> errorHandler) {

    }

    public void setBindings(final VMBindings bindings,
                            final Handler<Exception> errorHandler) {

    }

    public void terminate(final Handler<Exception> errorHandler) {

    }
}
