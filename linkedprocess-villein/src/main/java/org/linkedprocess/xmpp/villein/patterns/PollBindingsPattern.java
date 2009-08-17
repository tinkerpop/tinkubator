/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.villein.proxies.ResultHolder;

import java.util.logging.Logger;

/**
 * The PollBindingsPattern allows you to monitor the state of the bindings of a particular virtual machine.
 * When the actual state of the virtul machine's bindings reach some desired bindings state (as defined by an equivalence relation),
 * then a result handler is called. This is generally useful when a job is executing and the state of that job must be monitored.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PollBindingsPattern implements Runnable {

    private static final Logger LOGGER = LinkedProcess.getLogger(PollBindingsPattern.class);
    private static final long TIMEOUT = 2000;

    protected VmProxy vmProxy;
    protected VMBindings desiredBindings;
    protected long pollingInterval;
    protected BindingsChecker bindingsChecker;
    protected Handler<VMBindings> resultHandler;
    protected Handler<LopError> errorHandler;

    public void startPattern(final VmProxy vmProxy, final VMBindings desiredBindings, final BindingsChecker bindingsChecker, Handler<VMBindings> resultHandler, Handler<LopError> errorHandler, long pollingInterval) {
        this.vmProxy = vmProxy;
        this.desiredBindings = desiredBindings;
        this.bindingsChecker = bindingsChecker;
        this.resultHandler = resultHandler;
        this.errorHandler = errorHandler;
        this.pollingInterval = pollingInterval;
        new Thread(this).start();
    }


    private static void monitorSleep(final Object monitor, final long timeout) {
        try {
            synchronized (monitor) {
                if (timeout > 0)
                    monitor.wait(timeout);
                else
                    monitor.wait();
            }
        } catch (InterruptedException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    public void run() {
        Object monitor = new Object();


        while (true) {
            ResultHolder<VMBindings> resultBindings = null;
            try {
                resultBindings = SynchronousPattern.getBindings(vmProxy, this.desiredBindings.keySet(), TIMEOUT);
            } catch (TimeoutException e) {
                LOGGER.warning(e.getMessage());
            }

            if (resultBindings != null) {
                if (!resultBindings.successfulResult()) {
                    this.errorHandler.handle(resultBindings.getLopError());
                    break;
                } else {
                    if (this.bindingsChecker.areEquivalent(resultBindings.getResult(), this.desiredBindings)) {
                        resultHandler.handle(resultBindings.getResult());
                        break;
                    } else {
                        monitorSleep(monitor, this.pollingInterval);
                    }
                }
            }
        }


    }
}
