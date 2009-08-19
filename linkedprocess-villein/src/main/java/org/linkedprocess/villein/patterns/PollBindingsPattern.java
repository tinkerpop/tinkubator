/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.patterns;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopError;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.villein.proxies.ResultHolder;
import org.linkedprocess.villein.proxies.VmProxy;

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
    protected VmBindings desiredBindings;
    protected long pollingInterval;
    protected BindingsChecker bindingsChecker;
    protected Handler<VmBindings> resultHandler;
    protected Handler<LopError> errorHandler;

    public void startPattern(final VmProxy vmProxy, final VmBindings desiredBindings, final BindingsChecker bindingsChecker, Handler<VmBindings> resultHandler, Handler<LopError> errorHandler, long pollingInterval) {
        this.vmProxy = vmProxy;
        this.desiredBindings = desiredBindings;
        this.bindingsChecker = bindingsChecker;
        this.resultHandler = resultHandler;
        this.errorHandler = errorHandler;
        this.pollingInterval = pollingInterval;
        new Thread(this).start();
    }

    /**
     * Puts a monitor on wait for a certain number of milliseconds.
     *
     * @param monitor the monitor object to wait
     * @param timeout the number of milliseconds to wait (use -1 to wait indefinately)
     */
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

    /**
     * This initiates the polling thread. Note that a thread is created and started in the constructor of this method.
     */
    public void run() {
        Object monitor = new Object();


        while (true) {
            ResultHolder<VmBindings> resultBindings = null;
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
